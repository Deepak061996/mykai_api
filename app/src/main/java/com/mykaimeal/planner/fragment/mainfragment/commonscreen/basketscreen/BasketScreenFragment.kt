package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemLongClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.SuperMarketListAdapter
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterGetAddressItem
import com.mykaimeal.planner.adapter.IngredientsAdapter
import com.mykaimeal.planner.adapter.BasketYourRecipeAdapter
import com.mykaimeal.planner.adapter.PlacesAutoCompleteAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentBasketScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addressmapfullscreen.model.AddAddressModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.AddressPrimaryResponse
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.BasketScreenModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.BasketScreenModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.GetAddressListModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.GetAddressListModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Recipes
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Store
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.viewmodel.BasketScreenViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.listener.OnPlacesDetailsListener
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.Place
import com.mykaimeal.planner.model.PlaceAPI
import com.mykaimeal.planner.model.PlaceDetails
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@AndroidEntryPoint
class BasketScreenFragment : Fragment(), OnItemLongClickListener, OnItemSelectListener {

    private lateinit var binding: FragmentBasketScreenBinding
    private var adapter: SuperMarketListAdapter? = null
    private var adapterGetAddressItem: AdapterGetAddressItem? = null
    private var adapterRecipe: BasketYourRecipeAdapter? = null
    private  var adapterIngredients: IngredientsAdapter? = null
    private lateinit var basketScreenViewModel: BasketScreenViewModel
    private var rcySavedAddress: RecyclerView? = null
    private var recipe: MutableList<Recipes> = mutableListOf()
    private var ingredientList: MutableList<Ingredient> = mutableListOf()
    private var storeUid: String? = ""
    private var storeName: String? = ""
    private var dialogMiles: Dialog? = null
    private var dialogAddress: Dialog?=null
    private var tvAddress: AutoCompleteTextView?=null
    private var statusTypes: String? = "Home"
    private var latitude: String? = ""
    private var longitude: String? = ""
    private var addressId: String? = ""
    private var stores: MutableList<Store> = mutableListOf()
    private var addressList: MutableList<GetAddressListModelData>? = null

    private var userLatitude: String? = ""
    private var userLongitude: String? = ""
    private  var edtStreetName: EditText?=null
    private  var edtStreetNumber: EditText?=null
    private  var edtApartNumber: EditText?=null
    private  var edtCity: EditText?=null
    private  var edtStates: EditText?=null
    private  var edtPostalCode: EditText?=null
    private  var edtAddress: EditText?=null
    private var userAddress: String? = ""
    private var streetName: String? = ""
    private var streetNum: String? = ""
    private var apartNum: String? = ""
    private var city: String? = ""
    private var states: String? = ""
    private var zipcode: String? = ""
    private var country: String? = ""
    private var selectType: String? = ""
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private lateinit var commonWorkUtils: CommonWorkUtils
    private var hasShownPopup = false
    private var tAG: String = "Location"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBasketScreenBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        basketScreenViewModel = ViewModelProvider(requireActivity())[BasketScreenViewModel::class.java]
        commonWorkUtils = CommonWorkUtils(requireActivity())

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        adapter = SuperMarketListAdapter(stores, requireActivity(), this)
        binding.rcvSuperMarket.adapter = adapter

        adapterRecipe = BasketYourRecipeAdapter(recipe, requireActivity(), this)
        binding.rcvYourRecipes.adapter = adapterRecipe

        adapterIngredients = IngredientsAdapter(ingredientList, requireActivity(), this)
        binding.rcvIngredients.adapter = adapterIngredients

        buttonBack()

        basketScreenViewModel.setBasketDetailsStore("no")

        if ((activity as? MainActivity)?.Subscription_status==1){
            binding.btnLock.visibility=View.VISIBLE
            viewModelData()
        }else{
            binding.btnLock.visibility=View.GONE
            if (!hasShownPopup) {
                addressDialog()
                hasShownPopup = true
            } else {
                viewModelData()
            }
        }


        binding.textShoppingList.setOnClickListener {
            findNavController().navigate(R.id.shoppingListFragment)
        }

        binding.pullToRefresh.setOnRefreshListener {
            launchApi()
        }

        binding.btnLock.setOnClickListener {
            subscriptionAlert(requireContext())
        }

        initialize()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    fun subscriptionAlert(context: Context){
        val dialog= Dialog(context, R.style.BottomSheetDialog)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.alert_dialog_subscription_error)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = layoutParams
        val tvTitle: TextView =dialog.findViewById(R.id.tv_text)
        val btnOk: RelativeLayout =dialog.findViewById(R.id.btn_okay)
        val btnCancel: ImageView =dialog.findViewById(R.id.crossImages)
        val layroot: RelativeLayout =dialog.findViewById(R.id.layroot)

        layroot.setOnClickListener {
            dialog.dismiss()
        }

        tvTitle.text=ErrorMessage.subscriptionError
        btnOk.setOnClickListener {
            dialog.dismiss()
            val bundle = Bundle()
            bundle.putString("screen","main")
            findNavController().navigate(R.id.homeSubscriptionAllPlanFragment,bundle)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            findNavController().navigateUp()
        }
        dialog.show()
    }

    private fun viewModelData(){
        if (basketScreenViewModel.dataBasket!=null){
            showDataInUI(basketScreenViewModel.dataBasket!!)
        }else{
            launchApi()
        }
    }

    private fun buttonBack(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun launchApi(){
        if (BaseApplication.isOnline(requireActivity())) {
            getBasketList()
        } else {
            binding.pullToRefresh.isRefreshing=false
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun initialize() {

        binding.textConfirmOrder.isClickable=false

        binding.textSeeAll1.setOnClickListener {
            findNavController().navigate(R.id.superMarketsNearByFragment)
        }

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.textSeeAll2.setOnClickListener {
            findNavController().navigate(R.id.basketYourRecipeFragment)
        }

        binding.textSeeAll3.setOnClickListener {
            findNavController().navigate(R.id.shoppingMissingIngredientsFragment)
        }

        binding.textConfirmOrder.setOnClickListener {
            if (binding.textConfirmOrder.isClickable) {
                findNavController().navigate(R.id.basketDetailSuperMarketFragment)
            } else {
                showAlert(getString(R.string.available_products), false)
            }
        }

    }

    private fun getAddressList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.getAddressUrl {
                BaseApplication.dismissMe()
                handleApiGetAddressResponse(it)
            }
        }
    }


    private fun getBasketList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.getBasketUrl({
                BaseApplication.dismissMe()
                binding.pullToRefresh.isRefreshing=false
                handleApiBasketResponse(it)
            }, "", userLatitude, userLongitude)
        }
    }

    private fun addressPrimaryApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.makeAddressPrimaryUrl({
                BaseApplication.dismissMe()
                handleApiPrimaryResponse(it)
            }, addressId)
        }
    }

    private fun handleApiBasketResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessBasketResponse(result.data.toString())
            is NetworkResult.Error ->{
                binding.textConfirmOrder.isClickable=false
                showAlert(result.message, false)
            }
            else ->{
                binding.textConfirmOrder.isClickable=false
                showAlert(result.message, false)
            }
        }
    }

    private fun handleApiPrimaryResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleApiAddressPrimaryResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessBasketResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, BasketScreenModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                if (apiModel.data != null) {
                    showDataInUI(apiModel.data)
                }else{
                    binding.textConfirmOrder.isClickable=false
                }
            } else {
                binding.textConfirmOrder.isClickable=false
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            binding.textConfirmOrder.isClickable=false
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
    private fun handleApiAddressPrimaryResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, AddressPrimaryResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogAddress?.dismiss()
                dialogMiles?.dismiss()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleApiGetAddressResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessGetAddressResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessGetAddressResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, GetAddressListModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
                    showDataInAddressUI(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataInAddressUI(data: MutableList<GetAddressListModelData>?) {
        addressList = data
        adapterGetAddressItem = AdapterGetAddressItem(addressList, requireActivity(), this)
        rcySavedAddress?.adapter = adapterGetAddressItem

    }

    @SuppressLint("SetTextI18n")
    private fun showDataInUI(data: BasketScreenModelData) {
        basketScreenViewModel.setBasketData(data)
        data.billing?.let {
            if (it.recipes != null) {
                binding.textRecipeCount.text = it.recipes.toString()
            }
        }

        stores.clear()
        recipe.clear()
        ingredientList.clear()

        data.stores?.let {
            stores.addAll(it)
        }
        data.recipe?.let {
            recipe.addAll(it)
        }
        data.ingredient?.let {
            ingredientList.addAll(it)
        }
        if (stores.size > 0) {
            binding.rlSuperMarket.visibility = View.VISIBLE
            adapter?.updateList(stores)
        } else {
            binding.rlSuperMarket.visibility = View.GONE
        }

        if (recipe.size > 0) {
            binding.rlYourRecipes.visibility = View.VISIBLE
             adapterRecipe?.updateList(recipe)
        } else {
            binding.rlYourRecipes.visibility = View.GONE
        }

        if (ingredientList.size > 0) {
            binding.rlIngredients.visibility = View.VISIBLE
            adapterIngredients?.updateList(ingredientList)
            val count=getTotalPrice()
            if (count==0.0){
                binding.textConfirmOrder.isClickable=false
                binding.textNetTotalProduct.text="0"
                binding.textTotalAmount.text="0"
            }else{
                // Now format total properly
                val formattedTotal = if (count % 1 == 0.0) {
                    count.toInt().toString() // if 10.0 → show 10
                } else {
                    count.toString()         // if 10.5 → show 10.5
                }
                binding.textConfirmOrder.isClickable=true
                binding.textNetTotalProduct.text="$$formattedTotal*"
                binding.textTotalAmount.text="$$formattedTotal*"
            }
        } else {
            binding.textConfirmOrder.isClickable=false
            binding.textNetTotalProduct.text="0"
            binding.textTotalAmount.text="0"
            binding.rlIngredients.visibility = View.GONE
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun addressDialog() {
        dialogAddress = context?.let { Dialog(it) }
        dialogAddress?.setContentView(R.layout.alert_dialog_addresses_popup)
        dialogAddress?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddress?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val apiKey = getString(R.string.api_key)
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }

        val placesApi = PlaceAPI.Builder().apiKey(apiKey).build(requireContext())

        val relDone = dialogAddress?.findViewById<RelativeLayout>(R.id.relDone)
        val llSetWork = dialogAddress?.findViewById<LinearLayout>(R.id.llSetWork)
        val llSetHome = dialogAddress?.findViewById<LinearLayout>(R.id.llSetHome)
        val relTrialBtn = dialogAddress?.findViewById<RelativeLayout>(R.id.relTrialBtn)
        val imageHome = dialogAddress?.findViewById<ImageView>(R.id.imageHome)
        val imageWork = dialogAddress?.findViewById<ImageView>(R.id.imageWork)
        tvAddress = dialogAddress?.findViewById(R.id.tvAddress)
        rcySavedAddress = dialogAddress?.findViewById(R.id.rcySavedAddress)

        dialogAddress?.show()

        dialogAddress?.setOnDismissListener {
            // Call your API here when dialog is dismissed
            launchApi()
        }

        getAddressList()

        dialogAddress?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        llSetHome?.setOnClickListener {
            statusTypes = "Home"
            llSetHome.setBackgroundResource(R.drawable.outline_address_green_border_bg)
            llSetWork?.setBackgroundResource(R.drawable.height_type_bg)

            imageHome?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_orange), PorterDuff.Mode.SRC_IN)
            imageWork?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_grays), PorterDuff.Mode.SRC_IN)
        }

        llSetWork?.setOnClickListener {
            statusTypes = "Work"
            llSetHome?.setBackgroundResource(R.drawable.height_type_bg)
            llSetWork.setBackgroundResource(R.drawable.outline_address_green_border_bg)

            imageHome?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_grays), PorterDuff.Mode.SRC_IN)
            imageWork?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.light_orange), PorterDuff.Mode.SRC_IN)
        }

        relTrialBtn?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
            }
        }

        tvAddress?.setAdapter(PlacesAutoCompleteAdapter(requireContext(), placesApi))

        tvAddress?.setOnItemClickListener { parent, _, position, _ ->
            val place = parent.getItemAtPosition(position) as Place
            tvAddress?.setText(place.description)
            userAddress = place.description
            getPlaceDetails(place.id, placesApi)
        }

        relDone?.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (tvAddress?.text.toString().isNotEmpty()){
                    fullAddressDialog()
                }else{
                    if (!selectType.equals("")) {
                        addressPrimaryApi()
                    }else{
                        dialogMiles?.dismiss()
                    }
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            displayLocationSettingsRequest(requireContext())
        } else {
            showLocationError(requireContext(), ErrorMessage.locationError)
        }
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Activity.RESULT_OK == resultCode) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == 200) {
            // This condition for check location run time permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                showLocationError(requireContext(), ErrorMessage.locationError)
            }
        }
    }


    private fun showLocationError(context: Context?, msg: String?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.setCancelable(true)
        dialog?.setContentView(R.layout.alert_dialog_box_error)
        val tvTitle: TextView = dialog!!.findViewById(R.id.tv_text)
        val btnOk: RelativeLayout = dialog.findViewById(R.id.btn_okay)
        val root: RelativeLayout = dialog.findViewById(R.id.root)
        tvTitle.text = msg

        root.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 200)
        }
        dialog.show()
    }



    private fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 1000
        locationRequest.numUpdates = 1
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i(tAG, "All location settings are satisfied.")
                    getCurrentLocation()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(tAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ")
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.resolution?.let {
                            startIntentSenderForResult(it.intentSender, 100, null, 0, 0, 0, null)
                        }

                    } catch (e: IntentSender.SendIntentException) {
                        Log.i(tAG, "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(tAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.")

            }
        }
    }


    private fun fullAddressDialog() {
        dialogMiles = context?.let { Dialog(it) }
        dialogMiles?.setContentView(R.layout.alert_dialog_address_popup)
        dialogMiles?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogMiles?.setCancelable(false)
        dialogMiles?.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val relConfirm = dialogMiles?.findViewById<RelativeLayout>(R.id.relConfirm)
        val imageCross = dialogMiles?.findViewById<ImageView>(R.id.imageCross)
        edtStreetName = dialogMiles?.findViewById(R.id.edtStreetName)
        edtStreetNumber = dialogMiles?.findViewById(R.id.edtStreetNumber)
        edtApartNumber = dialogMiles?.findViewById(R.id.edtApartNumber)
        edtCity = dialogMiles?.findViewById(R.id.edtCity)
        edtStates = dialogMiles?.findViewById(R.id.edtStates)
        edtPostalCode = dialogMiles?.findViewById(R.id.edtPostalCode)
        edtAddress = dialogMiles?.findViewById(R.id.edtAddress)
        dialogMiles?.show()

        if (streetName != "") {
            edtStreetName?.setText(streetName.toString())
        }

        if (streetNum != "") {
            edtStreetNumber?.setText(streetNum.toString())
        }

        if (apartNum != "") {
            edtApartNumber?.setText(apartNum.toString())
        }

        if (city != "") {
            edtCity?.setText(city.toString())
        }

        if (states != "") {
            edtStates?.setText(states.toString())
        }

        if (userAddress != "") {
            edtAddress?.setText(userAddress.toString())
        }

        if (zipcode != "") {
            edtPostalCode?.setText(zipcode.toString())
        }

        dialogMiles?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        relConfirm?.setOnClickListener {
            if (BaseApplication.isOnline(requireContext())) {
                if (validate()) {
                    streetName = edtStreetName?.text.toString().trim()
                    streetNum = edtStreetNumber?.text.toString().trim()
                    apartNum = edtApartNumber?.text.toString().trim()
                    city = edtCity?.text.toString().trim()
                    states = edtStates?.text.toString().trim()
                    userAddress = edtAddress?.text.toString().trim()
                    zipcode = edtPostalCode?.text.toString().trim()
                    addFullAddressApi()
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
            dialogMiles?.dismiss()
        }

        imageCross?.setOnClickListener {
            dialogMiles?.dismiss()
        }
    }


    private fun validate(): Boolean {
        // Check if email/phone is empty
        if (edtStreetName?.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.streetNameError, false)
            return false
        } else if (edtStreetNumber?.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.streetNumberError, false)
            return false
        } else if (edtApartNumber?.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.apartNumberError, false)
            return false
        } else if (edtCity?.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.cityEnterError, false)
            return false
        } else if (edtStates?.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.statesEnterError, false)
            return false
        } else if (edtAddress?.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.addressError, false)
            return false
        } else if (edtPostalCode?.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.postalCodeError, false)
            return false
        }
        return true
    }

    private fun addFullAddressApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.addAddressUrl(
                {
                    BaseApplication.dismissMe()
                    handleApiAddAddressResponse(it)
                },
                latitude,
                longitude,
                streetName,
                streetNum,
                apartNum,
                city,
                states,
                country,
                zipcode,
                "1",
                "",
                statusTypes
            )
        }
    }

    private fun handleApiAddAddressResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddAddressResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddAddressResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, AddAddressModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                dialogAddress?.dismiss()
                dialogMiles?.dismiss()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun getPlaceDetails(placeId: String, placesApi: PlaceAPI) {
        placesApi.fetchPlaceDetails(placeId, object : OnPlacesDetailsListener {
            override fun onError(errorMessage: String) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }

            }

            override fun onPlaceDetailsFetched(placeDetails: PlaceDetails) {
                try {
                    latitude = placeDetails.lat.toString()
                    longitude = placeDetails.lng.toString()
                    requireActivity().runOnUiThread {
                        getAddressFromLocation(placeDetails.lat, placeDetails.lng)
                    }

                } catch (e: Exception) {
                    BaseApplication.alertError(requireContext(), e.message, false)
                }
            }
        })
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireActivity(), Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                streetName = address.thoroughfare ?: "" // Street Name
                streetNum = address.subThoroughfare ?: "" // Street Number
                apartNum = address.premises ?: "" // Apartment Number
                city = address.locality ?: "" // City
                states = address.adminArea ?: "" // State/Province
                country = address.countryName ?: "" // Country
                zipcode = address.postalCode ?: "" // Zip Code
                userAddress=address.getAddressLine(0) ?: ""  // Full Address

                Log.d("Address", "Street Name: $streetName")
                Log.d("Address", "Street Number: $streetNum")
                Log.d("Address", "Apartment Number: $apartNum")
                Log.d("Address", "City: $city")
                Log.d("Address", "Country: $country")
                Log.d("Address", "ZIP Code: $zipcode")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            // When location service is enabled
            // Get last location
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                // Initialize location
                val location = task.result
                // Check condition
                if (location != null) {
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    requireActivity().runOnUiThread {
                        getAddressFromLocation(location.latitude, location.longitude)
                    }
                    tvAddress?.setText(userAddress)
                } else {
                    // When location result is null
                    val locationRequest =
                        LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1)
                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            // Initialize
                            // location
                            val location1 = locationResult.lastLocation
                            latitude = location1!!.latitude.toString()
                            longitude = location1.longitude.toString()
                            requireActivity().runOnUiThread {
                                getAddressFromLocation(location1.latitude, location1.longitude)
                            }
                            tvAddress?.setText(userAddress)
                        }
                    }
                    // Request location updates
                    mFusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()!!
                    )

                }
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        }
    }


    private fun removeRecipeBasketDialog(recipeId: String?, position: Int?) {
        val dialogAddItem: Dialog = context?.let { Dialog(it) }!!
        dialogAddItem.setContentView(R.layout.alert_dialog_remove_recipe_basket)
        dialogAddItem.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddItem.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvDialogCancelBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogRemoveBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogRemoveBtn)
        dialogAddItem.show()
        dialogAddItem.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogAddItem.dismiss()
        }

        tvDialogRemoveBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                removeBasketRecipeApi(recipeId.toString(), dialogAddItem, position)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun removeBasketRecipeApi(recipeId: String, dialogRemoveDay: Dialog, position: Int?) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.removeBasketUrlApi({
                BaseApplication.dismissMe()
                handleApiRemoveBasketResponse(it, position, dialogRemoveDay)
            }, recipeId)
        }
    }

    private fun handleApiRemoveBasketResponse(
        result: NetworkResult<String>,
        position: Int?,
        dialogRemoveDay: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessRemoveBasketResponse(
                result.data.toString(),
                position,
                dialogRemoveDay
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessRemoveBasketResponse(data: String, position: Int?, dialogRemoveDay: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogRemoveDay.dismiss()
                recipe.removeAt(position!!)
                // Update the adapter
                if (recipe.size>0) {
                    adapterRecipe?.updateList(recipe)
                    binding.rcvYourRecipes.visibility=View.VISIBLE
                }else{
                    binding.rcvYourRecipes.visibility=View.GONE
                }
                launchApi()
            } else {
               handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    override fun itemSelect(position: Int?, recipeId: String?, type: String?) {
        if (BaseApplication.isOnline(requireActivity())) {
            if (type.equals("YourRecipe",true)) {
                if (recipeId.equals("remove",true)){
                    val data=recipe[position!!]
                    removeRecipeBasketDialog(data.id.toString(), position)
                }else{
                    removeAddRecipeServing(position, recipeId.toString())
                }
            }
            if (type.equals("SuperMarket",true)) {
                storeName = position?.let { stores[it].store_name.toString() }
                storeUid = position?.let { stores[it].store_uuid.toString() }
                selectSuperMarketApi()
            }
            if (type.equals("Ingredients",true)){
                removeAddIngServing(position, recipeId.toString())
            }
        }else{
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun selectSuperMarketApi() {
        lifecycleScope.launch {
            BaseApplication.showMe(requireContext())
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


    private fun removeAddRecipeServing(position: Int?, type: String) {
        val item = position?.let { recipe.get(it) }
        if (type.equals("plus", true) || type.equals("minus", true)) {
            var count = item?.serving?.toInt()
            val uri = item?.uri
            count = when (type.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            increaseQuantityRecipe(uri, count.toString(), item, position)
        }
    }


    private fun removeAddIngServing(position: Int?, type: String) {
        val item = position?.let { ingredientList.get(it) }
        val foodId = item?.food_id
        val qty: String
        if (type.equals("plus", true) || type.equals("minus", true)) {
            var count = item?.sch_id
            count = when (type.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            qty= count.toString()
        } else {
            qty="0"
        }
        increaseIngRecipe(foodId, qty, item, position)
    }

    private fun increaseIngRecipe(foodId: String?, quantity: String, item: Ingredient?, position: Int?) { lifecycleScope.launch {
        BaseApplication.showMe(requireContext())
            basketScreenViewModel.basketIngIncDescUrl({
                BaseApplication.dismissMe()
                handleApiIngResponse(it, item, quantity, position)
            }, foodId, quantity)
        }
    }

    private fun handleApiIngResponse(
        result: NetworkResult<String>,
        item: Ingredient?,
        quantity: String,
        position: Int?
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessIngResponse(
                result.data.toString(),
                item,
                quantity,
                position
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessIngResponse(
        data: String,
        item: Ingredient?,
        quantity: String,
        position: Int?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (!quantity.equals("0",true)) {
                    // Toggle the is_like value
                    item?.sch_id = quantity.toInt()
                    if (item != null) {
                        ingredientList[position!!] = item
                    }
                }else{
                    ingredientList.removeAt(position!!)
                }
                if (ingredientList.size>0){
                    // Update the adapter
                    adapterIngredients?.updateList(ingredientList)
                    val count=getTotalPrice()
                    if (count==0.0){
                        binding.textConfirmOrder.isClickable=false
                        binding.textNetTotalProduct.text="0"
                        binding.textTotalAmount.text="0"
                    }else{
                        // Now format total properly
                            val formattedTotal = if (count % 1 == 0.0) {
                                count.toInt().toString() // if 10.0 → show 10
                            } else {
                                count.toString()         // if 10.5 → show 10.5
                            }
                        binding.textConfirmOrder.isClickable=true
                        binding.textNetTotalProduct.text="$$formattedTotal*"
                        binding.textTotalAmount.text="$$formattedTotal*"
                    }
                }else{
                    binding.textConfirmOrder.isClickable=false
                    binding.rcvIngredients.visibility = View.GONE
                    binding.textNetTotalProduct.text="$0*"
                    binding.textTotalAmount.text="$0*"
                }
            } else {
                binding.textConfirmOrder.isClickable=false
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            binding.textConfirmOrder.isClickable=false
            showAlert(e.message, false)
        }
    }

    private fun increaseQuantityRecipe(
        uri: String?,
        quantity: String,
        item: Recipes?,
        position: Int?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketScreenViewModel.basketYourRecipeIncDescUrl({
                BaseApplication.dismissMe()
                handleApiQuantityResponse(it, item, quantity, position)
            }, uri, quantity)
        }
    }

    private fun handleApiQuantityResponse(
        result: NetworkResult<String>,
        item: Recipes?,
        quantity: String,
        position: Int?
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessQuantityResponse(
                result.data.toString(),
                item,
                quantity,
                position
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessQuantityResponse(
        data: String,
        item: Recipes?,
        quantity: String,
        position: Int?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                item?.serving = quantity.toInt().toString()
                if (item != null) {
                    recipe[position!!] = item
                }
                // Update the adapter
                adapterRecipe?.updateList(recipe)
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }



    override fun itemLongClick(position: Int?, status: String?, type: String?, isZiggleEnabled: String) {
       if (isZiggleEnabled.equals("SelectPrimary",true)) {
            selectType = isZiggleEnabled
            addressId = position.toString()
        }
        if (isZiggleEnabled.equals("Edit",true)) {
            dialogMiles?.dismiss()
            val bundle = Bundle().apply {
                putString("latitude", addressList?.get(position!!)?.latitude)
                putString("longitude", addressList?.get(position!!)?.longitude)
                putString("address", type)
                putString("addressId", addressList?.get(position!!)?.id.toString())
                putString("type", "Checkout")
            }
            findNavController().navigate(R.id.addressMapFullScreenFragment, bundle)
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