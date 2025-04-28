package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.OnItemSelectUnSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterSuperMarket
import com.mykaimeal.planner.adapter.CategoryProductAdapter
import com.mykaimeal.planner.adapter.IngredientsAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentBasketDetailSuperMarketBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.model.BasketDetailsSuperMarketModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.model.BasketDetailsSuperMarketModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.model.Product
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.viewmodel.BasketDetailsSuperMarketViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.BasketScreenModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Store
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.StoreData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.viewmodel.BasketScreenViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.SuperMarketModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BasketDetailSuperMarketFragment : Fragment(), OnItemClickListener,
    OnItemSelectUnSelectListener {

    private lateinit var binding: FragmentBasketDetailSuperMarketBinding
    private lateinit var itemSectionAdapter: CategoryProductAdapter
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var adapter: AdapterSuperMarket? = null
    private var rcvBottomDialog: RecyclerView? = null
    private lateinit var basketScreenViewModel: BasketScreenViewModel
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var latitude = "0.0"
    private var longitude = "0.0"
    private var storeUid: String? = ""
    private var storeName: String? = ""
    private var storeImage: String? = ""
    private var ingredientList: MutableList<Ingredient> = mutableListOf()
    private var stores: MutableList<Store> = mutableListOf()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentBasketDetailSuperMarketBinding.inflate(layoutInflater, container, false)

        basketScreenViewModel = ViewModelProvider(requireActivity())[BasketScreenViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        itemSectionAdapter = CategoryProductAdapter(requireActivity(),ingredientList, this)
        binding.recyclerItemList.adapter = itemSectionAdapter


        backButton()

        if (basketScreenViewModel.dataBasket!=null){
            showDataInUIFromBasket(basketScreenViewModel.dataBasket!!)
        }else{
            launchApi()
        }

        initialize()

        return binding.root


    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (basketScreenViewModel.dataStore.equals("yes",true)){
                        (activity as MainActivity?)?.upBasket()
                    }
                    findNavController().navigateUp()
                }
            })
    }

    private fun launchApi(){
        if (BaseApplication.isOnline(requireActivity())){
            getBasketDetailsApi()
        }else{
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun getBasketDetailsApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.getStoreProductUrl {
                BaseApplication.dismissMe()
                handleApiBasketDetailsResponse(it)
            }
        }
    }

    private fun initialize() {

        binding.relTescoMarket.setOnClickListener{
            bottomSheetDialog()
        }

        binding.imageBackIcon.setOnClickListener {
            if (basketScreenViewModel.dataStore.equals("yes",true)){
                (activity as MainActivity?)?.upBasket()
            }
            findNavController().navigateUp()
        }

        binding.rlGoToCheckout.setOnClickListener {
            if (binding.rlGoToCheckout.isClickable) {
                findNavController().navigate(R.id.checkoutScreenFragment)
            } else {
                showAlert(getString(R.string.available_products), false)
            }
        }

    }

    private fun handleApiBasketDetailsResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessBasketResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessBasketResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, BasketDetailsSuperMarketModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                if (apiModel.data != null) {
                    showDataInUI(apiModel.data)
                }
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

    @SuppressLint("SetTextI18n")
    private fun showDataInUI(data: BasketDetailsSuperMarketModelData?) {
        bottomSheetDialog?.dismiss()
        (activity as MainActivity?)?.upBasket()
        ingredientList.clear()
        val ingredient: MutableList<Ingredient> = mutableListOf()
        ingredient.clear()
        data?.product?.let {
            ingredient.addAll(it)
        }
        val localStores: MutableList<Store> = mutableListOf()
        data?.store?.let {
            localStores.add(Store(null,null,null,it.store_name,
                it.store_uuid,storeImage,null,1,null,null))
        }
        val localData=BasketScreenModelData(ingredient,null,localStores,null)
        basketScreenViewModel.setBasketData(localData)
        basketScreenViewModel.setBasketDetailsStore("yes")
        showDataInUIFromBasket(basketScreenViewModel.dataBasket)
    }

    @SuppressLint("SetTextI18n")
    private fun showDataInUIFromBasket(data: BasketScreenModelData?) {
        val activeStores = data?.stores?.firstOrNull { it.is_slected == 1 }
        activeStores?.let {
            if (it.image != null) {
                Glide.with(requireActivity())
                    .load(it.image)
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
                    .into(binding.tescoLogoImage)
            } else {
                binding.layProgess.root.visibility = View.GONE
            }
        }
        ingredientList.clear()
        data?.ingredient?.let {
            ingredientList.addAll(it)
        }
        if (ingredientList.size > 0) {
            binding.recyclerItemList.visibility = View.VISIBLE
            itemSectionAdapter.updateList(ingredientList)
            val count=getTotalPrice()
            // Now format total properly
            val formattedTotal = if (count % 1 == 0.0) {
                count.toInt().toString() // if 10.0 → show 10
            } else {
                count.toString()         // if 10.5 → show 10.5
            }
            binding.textPrice.text="$$formattedTotal"
        } else {
            binding.textPrice.text="$0"
            binding.recyclerItemList.visibility = View.GONE
        }
    }

    private fun bottomSheetDialog() {
        if (BaseApplication.isOnline(requireActivity())){
            getSuperMarketsList()
        }else{
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun getSuperMarketsList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.getSuperMarket({
                BaseApplication.dismissMe()
                handleMarketApiResponse(it)
            },latitude, longitude)
        }
    }

    private fun handleMarketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleMarketSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleMarketSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuperMarketModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success==true) {
                showUIData(apiModel.data)
            } else {
               handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showUIData(data: MutableList<Store>?) {
        try {
            data?.let {
                stores.clear()
                stores.addAll(it)
            }
            bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog)
            bottomSheetDialog?.setContentView(R.layout.bottom_sheet_select_super_market_near_me)
            rcvBottomDialog = bottomSheetDialog!!.findViewById(R.id.rcvBottomDialog)
            bottomSheetDialog?.show()
            if (stores.size>0){
                adapter = AdapterSuperMarket(data, requireActivity(), this)
                rcvBottomDialog?.adapter = adapter
            }
        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {

    }

    override fun itemSelectUnSelect(id: Int?, status: String?, type: String?, position: Int?) {
        if (BaseApplication.isOnline(requireActivity())) {
            if (type.equals("Product",true)){
                if (status.equals("swap")){
                    basketScreenViewModel.setBasketProductDetail(null)
                    val item= position?.let { ingredientList[it] }
                    val bundle = Bundle().apply {
                        putString("id",item?.id.toString())
                        putString("SwapProId",item?.product_id.toString())
                        putString("SwapProName",item?.name.toString())
                        putString("foodId",item?.food_id.toString())
                        putString("schId",item?.sch_id.toString())
                        putString("image",item?.pro_img.toString())
                    }

                    Log.d("id*****","******"+item?.id.toString())
                    Log.d("pro_id*****","******"+item?.product_id.toString())
                    Log.d("name*****","******"+item?.name.toString())
                    Log.d("food_id*****","******"+item?.food_id.toString())
                    Log.d("sch_id*****","******"+item?.sch_id.toString())
                    findNavController().navigate(R.id.basketProductDetailsFragment,bundle)

                }else {
                    removeAddIngServing(position, status.toString())
                }
            }
            if (type.equals("SuperMarket",true)){
                storeUid = position?.let { stores[it].store_uuid.toString() }
                storeName = position?.let { stores[it].store_name.toString() }
                storeImage = position?.let { stores[it].image.toString() }
                selectSuperMarketApi()
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun selectSuperMarketApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.selectStoreProductUrl({
                BaseApplication.dismissMe()
                handleSelectSupermarketApiResponse(it)
            }, storeName, storeUid)
        }
    }

    private fun handleSelectSupermarketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuperMarketResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuperMarketResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                launchApi()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun removeAddIngServing(position: Int?, type: String) {
        val item= position?.let { ingredientList[it] }
        if (type.equals("plus",true) || type.equals("minus",true)) {
            var count = item?.sch_id
            val foodId= item?.food_id
            count = when (type.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            increaseIngRecipe(foodId,count.toString(),item,position)
        }
    }

    private fun increaseIngRecipe(foodId: String?, quantity: String, item: Ingredient?, position: Int?) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.basketIngIncDescUrl({
                BaseApplication.dismissMe()
                handleApiIngResponse(it,item,quantity,position)
            },foodId,quantity)
        }
    }

    private fun handleApiIngResponse(result: NetworkResult<String>, item: Ingredient?, quantity: String, position: Int?) {
        when (result) {
            is NetworkResult.Success -> handleSuccessIngResponse(result.data.toString(),item,quantity,position)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessIngResponse(data: String, item: Ingredient?, quantity: String, position: Int?) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                item?.sch_id = quantity.toInt()
                ingredientList[position!!] = item!!
                // Update the adapter
                itemSectionAdapter.updateList(ingredientList)
                val count=getTotalPrice()
                binding.textPrice.text="$$count"
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    @SuppressLint("DefaultLocale")
    private fun getTotalPrice(): Double {
        val total = ingredientList.sumOf {
            val priceString = it.pro_price?.replace("$", "")?.trim()
            if (priceString != null && !priceString.equals("Not available", ignoreCase = true) && !priceString.equals("Not", ignoreCase = true)) {
                (priceString.toDoubleOrNull() ?: 0.0) * (it.sch_id?.toInt() ?: 0)
            } else {
                0.0
            }
        }
        return String.format("%.2f", total).toDouble()
    }

}