package com.mykaimeal.planner.fragment.mainfragment.hometab.missingingredientsfragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterMissingIngredientAvailableItem
import com.mykaimeal.planner.adapter.AdapterMissingIngredientsItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentMissingIngredientsBinding
import com.mykaimeal.planner.fragment.mainfragment.hometab.missingingredientsfragment.model.MissingIngredientModel
import com.mykaimeal.planner.fragment.mainfragment.hometab.missingingredientsfragment.model.MissingIngredientModelData
import com.mykaimeal.planner.fragment.mainfragment.hometab.missingingredientsfragment.viewmodel.MissingIngredientViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MissingIngredientsFragment : Fragment(), OnItemSelectListener {

    private lateinit var binding: FragmentMissingIngredientsBinding
    private var adapterMissingIngredientsItem: AdapterMissingIngredientsItem? = null
    private var adapterMissingIngAvailItem: AdapterMissingIngredientAvailableItem? = null
    private var selectAll:Boolean?=false
    private val missingIngredientList = mutableListOf<MissingIngredientModelData>()
    private val availableIngredientList = mutableListOf<MissingIngredientModelData>()
    private var shcId:String?=""
    private var recipeUri:String?=""
    private var foodIds = mutableListOf<String>()
    private var foodName = mutableListOf<String>()
    private var statusType = mutableListOf<String>()
    private lateinit var missingIngredientViewModel: MissingIngredientViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMissingIngredientsBinding.inflate(inflater, container, false)

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.llIndicator?.visibility = View.VISIBLE
        mainActivity?.binding?.llBottomNavigation?.visibility = View.VISIBLE

        missingIngredientViewModel = ViewModelProvider(requireActivity())[MissingIngredientViewModel::class.java]

        shcId = arguments?.getString("schId", "")?:""
        recipeUri = arguments?.getString("uri", "")?:""

        backButton()

        initialize()

        return binding.root
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun initialize() {

        binding.imgBackMissingIng.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.checkBoxImg.setOnClickListener{
            if (missingIngredientList.size>0){
                 updatechecBox()
            }
        }

        binding.tvAddToBasket.setOnClickListener{
            clearList()
            if (BaseApplication.isOnline(requireActivity())) {
                if (missingIngredientList.size  > 0) {
                    try {
                        var status=false
                        // Iterate through the ingredients and add them to the array if status is true
                        missingIngredientList.forEach { ingredientsModel ->
                            if (ingredientsModel.status) {
                                foodIds.add(ingredientsModel.foodId.toString())
                                foodName.add(ingredientsModel.food.toString())
                                statusType.add("0")
                                status=true
                            }
                        }
                        if (status){
                            addToCartApi()
                        }else{
                            BaseApplication.alertError(requireContext(), ErrorMessage.ingredientError, false)
                        }
                    } catch (e: Exception) {
                        BaseApplication.alertError(requireContext(), e.message, false)
                    }
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }


        binding.tvPurchasedBtn.setOnClickListener{
             clearList()
//            availableIngredientList.clear()
            if (BaseApplication.isOnline(requireActivity())) {
                if (missingIngredientList.size > 0) {
                    try {
                        var valueAdd = true
                        var status=false
                        if (valueAdd){
                            missingIngredientList.forEach { ingredientsModel ->
                                if (ingredientsModel.status) {
                                    foodIds.add(ingredientsModel.foodId.toString())
                                    foodName.add(ingredientsModel.food.toString())
                                    statusType.add("1")
                                    availableIngredientList.add(ingredientsModel)
                                    status=true
                                    valueAdd=false
                                }
                            }
                        }
                        if (status){
                            addToCartApi()
                        }else{
                            BaseApplication.alertError(requireContext(), ErrorMessage.ingredientError, false)
                        }
                    } catch (e: Exception) {
                        BaseApplication.alertError(requireContext(), e.message, false)
                    }
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }


        adapterMissingIngredientsItem = AdapterMissingIngredientsItem(missingIngredientList, requireActivity(),this)
        binding.rcyIngredientsRecipe.adapter = adapterMissingIngredientsItem

        adapterMissingIngAvailItem = AdapterMissingIngredientAvailableItem(availableIngredientList, requireActivity())
        binding.rcyAddedIngredientsRecipes.adapter = adapterMissingIngAvailItem

        if (BaseApplication.isOnline(requireActivity())) {
            missingIngredientApi()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

    }

    private fun clearList(){
        foodIds.clear()
        foodName.clear()
        statusType.clear()
    }

    private fun updatechecBox(){
        selectAll = !selectAll!! // Toggle the selectAll value
        // Update the drawable based on the selectAll state
        val drawableRes = if (selectAll as Boolean) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
        binding.checkBoxImg.setImageResource(drawableRes)
        // Update the status of each ingredient dynamically
        missingIngredientList.forEach {
                ingredient -> ingredient.status = selectAll as Boolean
        }
        // Notify adapter with updated data
        adapterMissingIngredientsItem?.updateList(missingIngredientList)
    }

    private fun missingIngredientApi() {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                missingIngredientViewModel.getMissingIngredientsApi({
                    BaseApplication.dismissMe()
                    handleApiMissingIngResponse(it)
                }, recipeUri,shcId)
            }
    }

    private fun handleApiMissingIngResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessMissingResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessMissingResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, MissingIngredientModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                showDataInUi(apiModel.data)
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun showDataInUi(data: MutableList<MissingIngredientModelData>?) {
        try {
            if (data!=null && data.size>0){
                missingIngredientList.clear()
                availableIngredientList.clear()
              // Assuming you have a response object of type MissingIngredientModel
                data.forEach { ingredient ->
                    if (ingredient.is_missing == 0) {
                        missingIngredientList.add(ingredient) // Add to missing ingredients list
                    } else {
                        availableIngredientList.add(ingredient) // Add to available ingredients list
                    }
                }

                showLatestData()

            }else{
                hideData()
            }
        }catch (e:Exception){
            hideData()
            Log.d("MissingIngredient@@@@","Data List:------"+e.message)
        }
    }

    private fun showLatestData(){
        if (missingIngredientList.size>0){
            binding.rcyIngredientsRecipe.visibility=View.VISIBLE
            binding.llBasketPurchasedBtn.visibility=View.VISIBLE
            binding.relIngredientsMissing.visibility=View.VISIBLE
            adapterMissingIngredientsItem?.updateList(missingIngredientList)
        }else{
            binding.rcyIngredientsRecipe.visibility=View.GONE
            binding.llBasketPurchasedBtn.visibility=View.GONE
            binding.relIngredientsMissing.visibility=View.GONE
        }
        if (availableIngredientList.size>0){
            binding.rcyAddedIngredientsRecipes.visibility=View.VISIBLE
            adapterMissingIngAvailItem?.updateList(availableIngredientList)
            binding.relAddedIngredients.visibility=View.VISIBLE
        }else{
            binding.rcyAddedIngredientsRecipes.visibility=View.GONE
            binding.relAddedIngredients.visibility=View.GONE
        }
    }


    private fun hideData(){
        binding.rcyIngredientsRecipe.visibility=View.GONE
        binding.llBasketPurchasedBtn.visibility=View.GONE
        binding.relIngredientsMissing.visibility=View.GONE
        binding.rcyAddedIngredientsRecipes.visibility=View.GONE
        binding.relAddedIngredients.visibility=View.GONE
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun addToCartApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            missingIngredientViewModel.addToCartUrlApi({
                BaseApplication.dismissMe()
                handleCartApiResponse(it)
            }, foodIds, "1",foodName,statusType)
        }
    }

    private fun handleCartApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCartResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCartResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                if (statusType[0].equals("0",true)){
                    (activity as MainActivity?)?.upDateHomeData()
                    findNavController().navigateUp()
                }else{
                    clearList()
                    missingIngredientList.removeIf { ingredientsModel ->
                        ingredientsModel.status
                    }
                    if (missingIngredientList.size>0){
                        showLatestData()
                    }else{
                        (activity as MainActivity?)?.upDateHomeData()
                        findNavController().navigateUp()
                    }
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {
        missingIngredientList.forEachIndexed { index, ingredient ->
            if (index == position) {
                ingredient.status = missingIngredientList[position].status != true
            }
        }
        selectAll = !missingIngredientList.any { !it.status }
        // Update the drawable based on the selectAll state
        val drawableRes = if (selectAll as Boolean) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
        binding.checkBoxImg.setImageResource(drawableRes)
        // Notify adapter with updated data
        adapterMissingIngredientsItem?.updateList(missingIngredientList)

    }
}