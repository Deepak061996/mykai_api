package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentBaseketIngredientsDetailsBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketproductsdetailsscreen.model.BasketDetailsModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketproductsdetailsscreen.viewmodel.BasketProductsDetailsViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.viewmodel.BasketScreenViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BasketIngredientsDetailsFragment : Fragment() {

    private lateinit var binding: FragmentBaseketIngredientsDetailsBinding
    private var proId: String = ""
    private var proName: String = ""
    private var name: String = ""
    private var image: String = ""
    private var foodId: String = ""
    private var schId: String = ""
    private var unitSize: String = ""
    private var price: String = ""
    private var type: String = ""
    private var Removetype: String = ""
    private lateinit var basketScreenViewModel: BasketScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBaseketIngredientsDetailsBinding.inflate(inflater, container, false)

        proId = arguments?.getString("SwapProId", "") ?: ""
        proName = arguments?.getString("SwapProName", "") ?: ""
        foodId = arguments?.getString("foodId", "") ?: ""
        schId = arguments?.getString("schId", "") ?: ""
        Removetype = arguments?.getString("type", "") ?: ""


        basketScreenViewModel = ViewModelProvider(requireActivity())[BasketScreenViewModel::class.java]

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })


        if (Removetype.equals("old",true)){
            binding.tvRemoveBasket.visibility=View.VISIBLE
            val result = basketScreenViewModel.dataBasket?.ingredient?.find { it.product_id.equals(proId) }
            proName=result?.pro_name.toString()
            name=result?.name.toString()
            image=result?.pro_img.toString()
            unitSize=result?.unit_of_measurement.toString()
            price=result?.price.toString()
        }else{
            binding.tvRemoveBasket.visibility=View.GONE
            val result = basketScreenViewModel.dataBasketItemDetails?.find { it.product_id.equals(proId) }
            proName=result?.name.toString()
            name=result?.name_query.toString()
            image=result?.image.toString()
            unitSize=result?.unit_of_measurement.toString()
            price=result?.formatted_price.toString()
        }

        initialize()

        return binding.root
    }

    private fun initialize() {


        showDataProductsInUI()


        binding.relDoneBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                removeAddIngServing()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        binding.relBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvRemoveBasket.setOnClickListener {
            type = "delete"
            if (BaseApplication.isOnline(requireActivity())) {
                removeAddIngServing()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        binding.imageMinusIcon.setOnClickListener {
            type = "minus"
            if (binding.textCount.text.toString().toInt() > 1) {
                val data = binding.textCount.text.toString().toInt() - 1
                updateValue(data.toString())
            } else {
                Toast.makeText(requireActivity(), ErrorMessage.servingError, Toast.LENGTH_LONG)
                    .show()
            }
        }

        binding.imageAddIcon.setOnClickListener {
            type = "plus"
            if (binding.textCount.text.toString().toInt() < 99) {
                val data = binding.textCount.text.toString().toInt() + 1
                updateValue(data.toString())
            }
        }
    }

    private fun removeAddIngServing() {
        val count = if (type.equals("plus", true) || type.equals("minus", true)) {
            binding.textCount.text.toString().trim().toIntOrNull() ?: 0
        } else {
            0
        }
        increaseIngRecipe(count.toString())
    }

    @SuppressLint("DefaultLocale")
    private fun updateValue(data: String) {
        binding.textCount.text = data
    }

    private fun increaseIngRecipe(quantity: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.basketIngIncDescUrl({
                BaseApplication.dismissMe()
                handleApiIngResponse(it,quantity)
            }, foodId, quantity)
        }
    }

    private fun handleApiIngResponse(result: NetworkResult<String>,quantity:String) {
        when (result) {
            is NetworkResult.Success -> handleSuccessIngResponse(result.data.toString(),quantity)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessIngResponse(data: String,quantity:String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (type.equals("delete",true)) {
                    val index = basketScreenViewModel.dataBasket?.ingredient?.indexOfFirst { it.id == id.toInt() }
                    basketScreenViewModel.dataBasket?.ingredient?.removeAt(index!!)
                    findNavController().navigate(R.id.basketScreenFragment)
                } else {
                    if (Removetype.equals("old",true)){
                        val index = basketScreenViewModel.dataBasket?.ingredient?.indexOfFirst { it.id == id.toInt() }
                        val updateData= basketScreenViewModel.dataBasket?.ingredient?.get(index!!)
                        updateData?.sch_id=quantity.toInt()
                        basketScreenViewModel.dataBasket?.ingredient?.set(index!!,updateData!!)
                        basketScreenViewModel.setBasketData(basketScreenViewModel.dataBasket)
                    }else{
//                        val index = basketScreenViewModel.dataBasketItemDetails?.indexOfFirst { it.food_id.equals(foodId) }
                        basketScreenViewModel.dataBasketItemDetails?.forEachIndexed { index, basketProductsDetailsModelData ->
                             if (basketScreenViewModel.dataBasketItemDetails!![index].food_id.equals(foodId)){
                                 val updateData= basketScreenViewModel.dataBasketItemDetails?.get(index)
                                 updateData?.sch_id=quantity.toInt()
                                 basketScreenViewModel.dataBasketItemDetails?.set(index,updateData!!)
                             }
                        }
                        basketScreenViewModel.setBasketProductDetail(basketScreenViewModel.dataBasketItemDetails)
                    }
                    findNavController().navigateUp()
                }
            } else {
               handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }


    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun showDataProductsInUI() {

        proName.let {
                binding.tvIngredientsName.text = it
            }

        schId.let {
                binding.textCount.text = it
            }

        unitSize.let {
                binding.tvQuantity.text = it
            }

        price.let {
                binding.tvActualPrices.text = it
            }

        image.let {
                Glide.with(requireActivity())
                    .load(it)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.layProgess.root.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.layProgess.root.visibility = View.GONE
                            return false
                        }
                    })
                    .into(binding.imageData)
            }?: run {
                binding.layProgess.root.visibility = View.GONE
            }


    }

}