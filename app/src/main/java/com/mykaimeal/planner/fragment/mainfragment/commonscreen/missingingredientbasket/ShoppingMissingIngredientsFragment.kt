package com.mykaimeal.planner.fragment.mainfragment.commonscreen.missingingredientbasket

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
import com.mykaimeal.planner.adapter.ShoppingMissingIngredientsAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentShoppingMissingIngredientsBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.missingingredientbasket.model.MissingIngBasketModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.missingingredientbasket.model.MissingIngBasketModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.missingingredientbasket.viewmodel.MissingIngredientBasketViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShoppingMissingIngredientsFragment : Fragment(), OnItemSelectListener {

    private lateinit var binding: FragmentShoppingMissingIngredientsBinding
    private var ingredientList: MutableList<MissingIngBasketModelData>?=null
    private var selectAll: Boolean? = false
    lateinit var adapter: ShoppingMissingIngredientsAdapter
    private lateinit var missingIngBasketViewModel: MissingIngredientBasketViewModel
    private var foodIds = mutableListOf<String>()
    private var foodName = mutableListOf<String>()
    private var statusType = mutableListOf<String>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            FragmentShoppingMissingIngredientsBinding.inflate(layoutInflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val bundle = Bundle().apply {
                        putString("id", "yes")
                    }
                    findNavController().navigate(R.id.orderHistoryFragment, bundle)
                }
            })

        missingIngBasketViewModel =
            ViewModelProvider(requireActivity())[MissingIngredientBasketViewModel::class.java]

        initialize()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {

        binding.imageBackIcon.setOnClickListener {
            val bundle = Bundle().apply {
                putString("id", "yes")
            }
            findNavController().navigate(R.id.orderHistoryFragment, bundle)
        }

        binding.tvAddToBasket.setOnClickListener {
            clearList()
            if (BaseApplication.isOnline(requireActivity())) {
                if (ingredientList!!.size  > 0) {
                    try {
                        var status=false
                        // Iterate through the ingredients and add them to the array if status is true
                        ingredientList?.forEach { ingredientsModel ->
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
           /* val bundle = Bundle().apply {
                putString("id", "yes")
            }
            findNavController().navigate(R.id.orderHistoryFragment, bundle)*/
        }

        binding.tvPurchasedBtn.setOnClickListener {
            clearList()
            if (BaseApplication.isOnline(requireActivity())) {
                if (ingredientList!!.size > 0) {
                    try {
                        var status=false
                        ingredientList?.forEach { ingredientsModel ->
                            if (ingredientsModel.status) {
                                foodIds.add(ingredientsModel.foodId.toString())
                                foodName.add(ingredientsModel.food.toString())
                                statusType.add("1")
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

           /* val bundle = Bundle().apply {
                putString("id", "yes")
            }
            findNavController().navigate(R.id.orderHistoryFragment, bundle)*/
        }

        binding.tvSelectAllBtn.setOnClickListener {
            selectAll = !selectAll!! // Toggle the value

            // Update checkbox drawable
            val drawableRes = if (selectAll == true) {
                R.drawable.orange_checkbox_images
            } else {
                R.drawable.orange_uncheck_box_images
            }
            binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)

             // Update each ingredient's status in the data list
            ingredientList?.forEach { ingredient ->
                ingredient.status = selectAll as Boolean
            }

            // Notify adapter with updated list
            adapter.updateList(ingredientList)
        }

        if (BaseApplication.isOnline(requireActivity())) {
            missingIngredientApi()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun addToCartApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            missingIngBasketViewModel.addToCartUrlApi({
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
                    findNavController().navigateUp()
                }else{
                    clearList()
                    ingredientList!!.removeIf { ingredientsModel ->
                        ingredientsModel.status
                    }
                    if (ingredientList!!.size>0){
                        /*showLatestData()*/
                    }else{
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

    private fun clearList(){
        foodIds.clear()
        foodName.clear()
        statusType.clear()
    }

    private fun missingIngredientApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            missingIngBasketViewModel.getMissingIngBasketUrl {
                BaseApplication.dismissMe()
                handleApiMissingIngResponse(it)
            }
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
            val apiModel = Gson().fromJson(data, MissingIngBasketModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                showDataInUi(apiModel.data)
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataInUi(data: MutableList<MissingIngBasketModelData>?) {
        try {
            if (data != null && data.size > 0) {
                ingredientList=data
                adapter = ShoppingMissingIngredientsAdapter(ingredientList, requireActivity(), this)
                binding.rcvIngredients.adapter = adapter

            }
        } catch (e: Exception) {
            Log.d("MissingIngredient@@@@", "Data List:------" + e.message)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {
        ingredientList?.forEachIndexed { index, ingredient ->
            if (index == position) {
                ingredient.status = ingredientList?.get(position)?.status != true
            }
        }
        // Notify adapter with updated data
        adapter.updateList(ingredientList)

        selectAll = ingredientList?.all { it.status } == true

        // Update the drawable based on the selectAll state
        val drawableRes = if (selectAll as Boolean) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
        binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
    }

}