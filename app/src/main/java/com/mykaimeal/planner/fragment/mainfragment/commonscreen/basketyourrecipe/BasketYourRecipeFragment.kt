package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.YourRecipeAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentBasketYourRecipeBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.model.BasketYourRecipeModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.model.BasketYourRecipeModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.model.Dinner
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.viewmodel.BasketYourRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.CookedTabModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BasketYourRecipeFragment : Fragment(),OnItemClickListener,OnItemSelectListener {

    private lateinit var binding: FragmentBasketYourRecipeBinding
    private lateinit var basketYourRecipeViewModel: BasketYourRecipeViewModel
    private var yourRecipeAdapter: YourRecipeAdapter?=null
    private var basketYourModelData: BasketYourRecipeModelData?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding= FragmentBasketYourRecipeBinding.inflate(layoutInflater, container, false)

        basketYourRecipeViewModel = ViewModelProvider(requireActivity())[BasketYourRecipeViewModel::class.java]

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        initialize()

        return binding.root
    }

    private fun initialize() {

        if (BaseApplication.isOnline(requireActivity())) {
            getYourRecipeList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun getYourRecipeList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketYourRecipeViewModel.getYourRecipeUrl {
                BaseApplication.dismissMe()
                handleApiYourRecipeResponse(it)
            }
        }
    }

    private fun handleApiYourRecipeResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessYourRecipeResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessYourRecipeResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, BasketYourRecipeModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                if (apiModel.data!= null) {
                    showDataInUI(apiModel.data)
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataInUI(data: BasketYourRecipeModelData?) {

        basketYourModelData=data

        if (data?.Breakfast!=null && data.Breakfast.size>0){
            binding.rcyBreakfast.visibility=View.VISIBLE
            binding.tvBreakFast.visibility=View.VISIBLE
            yourRecipeAdapter = YourRecipeAdapter(data.Breakfast,requireActivity(), this,"Breakfast")
            binding.rcyBreakfast.adapter = yourRecipeAdapter
        }else{
            binding.rcyBreakfast.visibility=View.GONE
            binding.tvBreakFast.visibility=View.GONE
        }

        if (data?.Lunch!=null && data.Lunch.size>0){
            binding.rcyLunch.visibility=View.VISIBLE
            binding.tvLunch.visibility=View.VISIBLE
            yourRecipeAdapter = YourRecipeAdapter(data.Lunch,requireActivity(), this,"Lunch")
            binding.rcyLunch.adapter = yourRecipeAdapter
        }else{
            binding.rcyLunch.visibility=View.GONE
            binding.tvLunch.visibility=View.GONE
        }

        if (data?.Dinner!=null && data.Dinner.size>0){
            binding.rcyDinner.visibility=View.VISIBLE
            binding.tvDinner.visibility=View.VISIBLE
            yourRecipeAdapter = YourRecipeAdapter(data.Dinner,requireActivity(), this,"Dinner")
            binding.rcyDinner.adapter = yourRecipeAdapter
        }else{
            binding.rcyDinner.visibility=View.GONE
            binding.tvDinner.visibility=View.GONE
        }

        if (data?.Snacks!=null && data.Snacks.size>0){
            binding.rcySnacks.visibility=View.VISIBLE
            binding.tvSnacks.visibility=View.VISIBLE
            yourRecipeAdapter = YourRecipeAdapter(data.Snacks,requireActivity(), this,"Snacks")
            binding.rcySnacks.adapter = yourRecipeAdapter
        }else{
            binding.rcySnacks.visibility=View.GONE
            binding.tvSnacks.visibility=View.GONE
        }

        if (data?.Brunch!=null && data.Brunch.size>0){
            binding.rcyTeaTimes.visibility=View.VISIBLE
            binding.tvTeaTime.visibility=View.VISIBLE
            yourRecipeAdapter = YourRecipeAdapter(data.Brunch,requireActivity(), this,"Brunch")
            binding.rcyTeaTimes.adapter = yourRecipeAdapter
        }else{
            binding.rcyTeaTimes.visibility=View.GONE
            binding.tvTeaTime.visibility=View.GONE
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {

    }

    private fun removeRecipeBasketDialog(recipeId: String?, position: Int?, type: String) {
        val dialogAddItem: Dialog = context?.let { Dialog(it) }!!
        dialogAddItem.setContentView(R.layout.alert_dialog_remove_recipe_basket)
        dialogAddItem.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddItem.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val tvDialogCancelBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogRemoveBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogRemoveBtn)
        dialogAddItem.show()
        dialogAddItem.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogAddItem.dismiss()
        }

        tvDialogRemoveBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                removeBasketRecipeApi(recipeId.toString(), dialogAddItem,position,type)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
//            dialogAddItem.dismiss()
        }
    }

    private fun removeBasketRecipeApi(
        recipeId: String,
        dialogRemoveDay: Dialog,
        position: Int?,
        type: String
    ) {
        BaseApplication.showMe(requireActivity())
        lifecycleScope.launch {
            basketYourRecipeViewModel.removeBasketUrlApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        val gson = Gson()
                        val cookedModel = gson.fromJson(it.data, CookedTabModel::class.java)
                        if (cookedModel.code == 200 && cookedModel.success) {

                            when (type) {
                                "Breakfast" -> yourRecipeAdapter?.removeItem(position!!)
                                "Lunch" -> yourRecipeAdapter?.removeItem(position!!)
                                "Dinner" -> yourRecipeAdapter?.removeItem(position!!)
                                "Snacks" -> yourRecipeAdapter?.removeItem(position!!)
                                "Brunch" -> yourRecipeAdapter?.removeItem(position!!)
                            }
                            dialogRemoveDay.dismiss()
                        } else {
                            if (cookedModel.code == ErrorMessage.code) {
                                showAlert(cookedModel.message, true)
                            } else {
                                showAlert(cookedModel.message, false)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlert(it.message, false)
                    }

                    else -> {
                        showAlert(it.message, false)
                    }
                }
            }, recipeId)
        }
    }


    override fun itemSelect(position: Int?, recipeId: String?, type: String?) {
        if (recipeId=="Minus"){
            if (BaseApplication.isOnline(requireActivity())) {
                removeAddServing(type ?: "", position, "minus")
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }else if (recipeId=="Plus"){
            if (BaseApplication.isOnline(requireActivity())) {
                removeAddServing(type ?: "", position, "plus")
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }else{
            removeRecipeBasketDialog(recipeId,position, type.toString())
        }

    }

    private fun removeAddServing(status: String, position: Int?, type: String) {

        val (mealList, adapter) = when (status) {
            "BreakFast" -> basketYourModelData?.Breakfast to yourRecipeAdapter
            "Lunch" -> basketYourModelData?.Lunch to yourRecipeAdapter
            "Dinner" -> basketYourModelData?.Dinner to yourRecipeAdapter
            "Snacks" -> basketYourModelData?.Snacks to yourRecipeAdapter
            "Brunch" -> basketYourModelData?.Brunch to yourRecipeAdapter
            else -> null to null
        }

        val item = mealList?.get(position!!)

        if (type.equals("plus",true) || type.equals("minus",true)) {
            var count = item?.serving?.toInt()
            val uri= item?.uri
            count = when (type.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            increaseQuantityRecipe(uri,count.toString(),item,position,mealList,adapter)
        }
    }

    private fun increaseQuantityRecipe(uri: String?, quantity: String, item: Dinner?, position: Int?,
                                       mealList: MutableList<Dinner>?, adapter: YourRecipeAdapter?) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketYourRecipeViewModel.basketYourRecipeIncDescUrl({
                BaseApplication.dismissMe()
                handleApiQuantityResponse(it,item,quantity,position,mealList,adapter)
            },uri,quantity)
        }
    }

    private fun handleApiQuantityResponse(result: NetworkResult<String>, item: Dinner?, quantity: String, position: Int?,
                                          mealList: MutableList<Dinner>?, adapter: YourRecipeAdapter?
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessQuantityResponse(result.data.toString(),item,quantity,position,mealList,adapter)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessQuantityResponse(data: String, item: Dinner?, quantity: String,
        position: Int?, mealList: MutableList<Dinner>?, adapter: YourRecipeAdapter?) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                item?.serving = quantity.toInt().toString()
                if (item != null) {
                    mealList?.set(position!!, item)
                }
                // Update the adapter
                if (mealList != null) {
                    adapter?.updateList(mealList)
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


}