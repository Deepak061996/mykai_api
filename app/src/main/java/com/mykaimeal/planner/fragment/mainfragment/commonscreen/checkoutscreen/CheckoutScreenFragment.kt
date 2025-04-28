package com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.mykaimeal.planner.OnItemLongClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterCardPreferredItem
import com.mykaimeal.planner.adapter.AdapterCheckoutIngredientsItem
import com.mykaimeal.planner.adapter.AdapterGetAddressItem
import com.mykaimeal.planner.adapter.IngredientsShoppingAdapter
import com.mykaimeal.planner.adapter.PlacesAutoCompleteAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentCheckoutScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addressmapfullscreen.model.AddAddressModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.AddressPrimaryResponse
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.GetAddressListModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.GetAddressListModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model.CheckoutScreenModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model.CheckoutScreenModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.viewmodel.CheckoutScreenViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen.model.GetCardMealMeModelData
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
class CheckoutScreenFragment : Fragment(), OnMapReadyCallback, OnItemLongClickListener,OnItemSelectListener {
    private lateinit var binding: FragmentCheckoutScreenBinding
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
    private lateinit var checkoutScreenViewModel: CheckoutScreenViewModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private lateinit var sessionManagement: SessionManagement
    private var rcySavedAddress: RecyclerView? = null
    private var dialogMiles: Dialog? = null
    private var selectType: String? = ""
    private var addressId: String? = ""
    private val tAG = "CheckOut"

    private lateinit var adapterCheckoutIngredients: AdapterCheckoutIngredientsItem
    private lateinit var adapterCardPreferred: AdapterCardPreferredItem
    private var cardMealMe: MutableList<GetCardMealMeModelData> = mutableListOf()

    private var latitude: String? = ""
    private var longitude: String? = ""
    private var totalPrices: String? = ""
    private var cardId: String? = ""
    private var statusTypes: String? = "Home"

    private lateinit var edtStreetName: EditText
    private lateinit var edtStreetNumber: EditText
    private lateinit var edtApartNumber: EditText
    private lateinit var edtCity: EditText
    private lateinit var edtStates: EditText
    private lateinit var edtPostalCode: EditText
    private lateinit var edtAddress: EditText
    private lateinit var tvAddress: AutoCompleteTextView
    private var userAddress: String? = ""
    private var streetName: String? = ""
    private var streetNum: String? = ""
    private var apartNum: String? = ""
    private var city: String? = ""
    private var states: String? = ""
    private var zipcode: String? = ""
    private var country: String? = ""
    private lateinit var commonWorkUtils: CommonWorkUtils
    private var adapterGetAddressItem: AdapterGetAddressItem? = null
    private var addressList: MutableList<GetAddressListModelData> = mutableListOf()
    private var phoneNumber: String? = ""
    private var notes: String? = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCheckoutScreenBinding.inflate(layoutInflater, container, false)

        checkoutScreenViewModel = ViewModelProvider(requireActivity())[CheckoutScreenViewModel::class.java]

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        sessionManagement = SessionManagement(requireContext())
        commonWorkUtils = CommonWorkUtils(requireActivity())

        adapterCardPreferred = AdapterCardPreferredItem(requireContext(),cardMealMe, this,0)
        binding.rcyCardDetails.adapter = adapterCardPreferred

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        backButton()

        initialize()

        return binding.root
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun initialize() {

//        if (checkoutScreenViewModel.dataCheckOut!=null){
//            showDataInUI(checkoutScreenViewModel.dataCheckOut)
//        }else{
//            loadApi()
//        }

        loadApi()

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.layEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putString("latitude", latitude.toString())
                putString("longitude", longitude.toString())
                putString("address", userAddress.toString())
                putString("addressId", "")
                putString("type", "Checkout")
            }
            findNavController().navigate(R.id.addressMapFullScreenFragment, bundle)
        }

        binding.relSetHomes.setOnClickListener {
            addressDialog()
        }

        binding.textPayBtn.setOnClickListener {
            if (validatation()){
                val bundle = Bundle().apply {
                    putString("totalPrices", totalPrices)
                    putString("cardId", cardId)
                }
                findNavController().navigate(R.id.addTipScreenFragment, bundle)
            }

        }

        binding.relSetMeetAtDoor.setOnClickListener {
            findNavController().navigate(R.id.dropOffOptionsScreenFragment)
        }

        binding.relAddNumber.setOnClickListener {
            findNavController().navigate(R.id.addNumberVerifyFragment)
        }

        binding.tvAddCard.setOnClickListener {
            findNavController().navigate(R.id.paymentCreditDebitFragment)
        }

        binding.relSuperMarketsItems.setOnClickListener {
            if (binding.relIngredients.visibility == View.VISIBLE) {
                binding.relIngredients.visibility = View.GONE
                binding.imageDown.setImageResource(R.drawable.drop_down_icon)
            } else {
                binding.imageDown.setImageResource(R.drawable.drop_up_icon)
                binding.relIngredients.visibility = View.VISIBLE
            }
        }

    }


    private fun loadApi(){
        if (BaseApplication.isOnline(requireContext())) {
            getCheckoutApi()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun getCheckoutApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            checkoutScreenViewModel.getCheckoutScreenUrl {
                BaseApplication.dismissMe()
                handleApiCheckoutResponse(it)
            }
        }
    }

    private fun handleApiCheckoutResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCheckoutResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessCheckoutResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CheckoutScreenModel::class.java)
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


    @SuppressLint("SetTextI18n")
    private fun showDataInUI(data: CheckoutScreenModelData?) {
        checkoutScreenViewModel.setCheckOutData(data)
        data?.let {
            if (it.phone != null && it.country_code != null) {
                val rawNumber = it.phone.toString().filter { it.isDigit() }
                val formattedNumber = if (rawNumber.length >= 10) {
                    "-${rawNumber.substring(0, 3)}-${
                        rawNumber.substring(
                            3,
                            6
                        )
                    }-${rawNumber.substring(6, 10)}"
                } else {
                    rawNumber // fallback in case the number is shorter than 10 digits
                }
                binding.tvAddNumber.text = it.country_code + formattedNumber
                binding.tvAddNumber.setTextColor(Color.parseColor("#000000"))
            }
            if (it.address != null) {
                if (!it.address.type.isNullOrEmpty() &&
                    !it.address.apart_num.isNullOrEmpty() &&
                    !it.address.street_name.isNullOrEmpty() &&
                    !it.address.city.isNullOrEmpty() &&
                    !it.address.state.isNullOrEmpty() &&
                    !it.address.country.isNullOrEmpty() &&
                    !it.address.zipcode.isNullOrEmpty() &&
                    !it.address.latitude.isNullOrEmpty() &&
                    !it.address.longitude.isNullOrEmpty()
                ) {
                    // Build full address string
                    val fullAddress = listOf(
                        it.address.apart_num,
                        it.address.street_name,
                        it.address.city,
                        it.address.state,
                        it.address.country,
                        it.address.zipcode
                    ).joinToString(" ")
                    // Store in variable if needed
                    userAddress = fullAddress
                    // Set full address to TextView
                    binding.tvAddressNames.text = fullAddress
                    if (it.address.latitude != null && it.address.longitude != null) {
                        latitude = it.address.latitude
                        longitude = it.address.longitude
                        val lat = latitude?.toDoubleOrNull()
                            ?: 0.0  // Convert String to Double, default to 0.0 if null
                        val lng = longitude?.toDoubleOrNull() ?: 0.0
                        updateMarker(lat, lng)
                    }
                }
            }
            if (it.Store != null) {
                binding.tvSuperMarketName.text = it.Store.toString()
            }
            if (it.note != null) {
                if (it.note.pickup != null) {
                    binding.tvSetDoorStep.text = it.note.pickup.toString()
                }
                if (it.note.description != null) {
                    binding.tvDeliveryInstructions.text = it.note.description.toString()
                    binding.tvDeliveryInstructions.setTextColor(Color.parseColor("#000000"))
                }
            }
            if (it.net_total != null) {
                val roundedSubTotal = it.net_total.let {
                    BigDecimal(it).setScale(2, RoundingMode.HALF_UP).toDouble()
                }
                binding.textSubTotalPrices.text = "$$roundedSubTotal"
            }
            if (it.tax != null) {
                val roundedBagFees = it.tax.let {
                    BigDecimal(it).setScale(2, RoundingMode.HALF_UP).toDouble()
                }
                binding.textBagFees.text = "$$roundedBagFees"
            }
            it.processing?.let { processing ->
                val roundedServices = BigDecimal(processing).setScale(2, RoundingMode.HALF_UP).toDouble()
                binding.textServicesPrice.text = "$$roundedServices"
            }
            it.delivery?.let { delivery ->
                val roundedDelivery = BigDecimal(delivery).setScale(2, RoundingMode.HALF_UP).toDouble()
                binding.textDeliveryPrice.text = "$$roundedDelivery"
            }
            cardMealMe.clear()
            if (it.card!=null){
                cardMealMe.addAll(it.card) 
            }
            if (cardMealMe.size > 0) {
                binding.relCardDetails.visibility = View.VISIBLE
                adapterCardPreferred.updateList(cardMealMe)
            } else {
                binding.relCardDetails.visibility = View.GONE
            }
            if (it.total != null) {
                val roundedTotal = it.total.let {
                    BigDecimal(it).setScale(2, RoundingMode.HALF_UP).toDouble()
                }
                totalPrices = roundedTotal.toString()
                binding.textTotalAmounts.text = "$$roundedTotal"
            }
            if (it.ingredient_count != null) {
                binding.tvItemsCount.text = it.ingredient_count.toString() + " Items"
            }
            if (!it.ingredient.isNullOrEmpty()) {
                adapterCheckoutIngredients =
                    AdapterCheckoutIngredientsItem(it.ingredient, requireActivity())
                binding.rcyIngredients.adapter = adapterCheckoutIngredients
            }
            if (it.store_image!=null){
                Glide.with(requireActivity())
                    .load(it.store_image)
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
                    .into(binding.imageWelmart)
            }else{
                binding.layProgess.root.visibility = View.GONE
            }
        }
    }

    private fun updateMarker(lat: Double, longi: Double) {
        Log.d("Location", "****** $lat, $longi")
        val newYork = LatLng(lat, longi)

        mMap?.clear()
        val customMarker = bitmapDescriptorFromVector(
            R.drawable.marker_icon, 50, 50
        ) // Change with your drawable
        mMap?.addMarker(MarkerOptions().position(newYork).icon(customMarker))

        // 🔹 Disable map movement
        mMap?.uiSettings?.apply {
            isScrollGesturesEnabled = false  // ❌ Disable scrolling
            isZoomGesturesEnabled = false    // ❌ Disable zooming
            isTiltGesturesEnabled = false    // ❌ Disable tilt
            isRotateGesturesEnabled = false  // ❌ Disable rotation
        }
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 20f))


    }

    private fun addressDialog() {
        dialogMiles = context?.let { Dialog(it) }!!
        dialogMiles?.setContentView(R.layout.alert_dialog_addresses_popup)
        dialogMiles?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogMiles?.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        val apiKey = getString(R.string.api_key)
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }
        val placesApi = PlaceAPI.Builder().apiKey(apiKey).build(requireContext())
        val relDone = dialogMiles?.findViewById<RelativeLayout>(R.id.relDone)
        val llSetWork = dialogMiles?.findViewById<LinearLayout>(R.id.llSetWork)
        val llSetHome = dialogMiles?.findViewById<LinearLayout>(R.id.llSetHome)
        val relTrialBtn = dialogMiles?.findViewById<RelativeLayout>(R.id.relTrialBtn)
        val imageHome = dialogMiles?.findViewById<ImageView>(R.id.imageHome)
        val imageWork = dialogMiles?.findViewById<ImageView>(R.id.imageWork)
        tvAddress = dialogMiles?.findViewById(R.id.tvAddress)!!
        rcySavedAddress = dialogMiles?.findViewById(R.id.rcySavedAddress)
        dialogMiles?.show()
        getAddressList()

        dialogMiles?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
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
        tvAddress.setAdapter(PlacesAutoCompleteAdapter(requireContext(), placesApi))

        tvAddress.setOnItemClickListener { parent, _, position, _ ->
            val place = parent.getItemAtPosition(position) as Place
            tvAddress.setText(place.description)
            userAddress = place.description
            getPlaceDetails(place.id, placesApi)
        }

        relDone?.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (tvAddress.text.toString().isNotEmpty()){
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

    private fun getAddressList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            checkoutScreenViewModel.getAddressUrl {
                BaseApplication.dismissMe()
                handleApiGetAddressResponse(it)
            }
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

    private fun showDataInAddressUI(data: MutableList<GetAddressListModelData>?) {
        addressList.clear()
        data?.let {
            addressList.addAll(it)
        }
        if (addressList.size>0){
            adapterGetAddressItem = AdapterGetAddressItem(data, requireActivity(), this)
            rcySavedAddress?.adapter = adapterGetAddressItem
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
                    tvAddress.setText(userAddress)
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
                            tvAddress.setText(userAddress)
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


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            displayLocationSettingsRequest(requireContext())
        } else {
            showLocationError(requireContext(), ErrorMessage.locationError)
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

    private fun fullAddressDialog() {
        val dialogMiles: Dialog = context?.let { Dialog(it) }!!
        dialogMiles.setContentView(R.layout.alert_dialog_address_popup)
        dialogMiles.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogMiles.setCancelable(false)
        dialogMiles.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val relConfirm = dialogMiles.findViewById<RelativeLayout>(R.id.relConfirm)
        val imageCross = dialogMiles.findViewById<ImageView>(R.id.imageCross)
        edtStreetName = dialogMiles.findViewById(R.id.edtStreetName)
        edtStreetNumber = dialogMiles.findViewById(R.id.edtStreetNumber)
        edtApartNumber = dialogMiles.findViewById(R.id.edtApartNumber)
        edtCity = dialogMiles.findViewById(R.id.edtCity)
        edtStates = dialogMiles.findViewById(R.id.edtStates)
        edtPostalCode = dialogMiles.findViewById(R.id.edtPostalCode)
        edtAddress = dialogMiles.findViewById(R.id.edtAddress)
        dialogMiles.show()

        if (streetName != "") {
            edtStreetName.setText(streetName.toString())
        }

        if (streetNum != "") {
            edtStreetNumber.setText(streetNum.toString())
        }

        if (apartNum != "") {
            edtApartNumber.setText(apartNum.toString())
        }

        if (city != "") {
            edtCity.setText(city.toString())
        }

        if (states != "") {
            edtStates.setText(states.toString())
        }

        if (userAddress != "") {
            edtAddress.setText(userAddress.toString())
        }

        if (zipcode != "") {
            edtPostalCode.setText(zipcode.toString())
        }

        dialogMiles.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        relConfirm.setOnClickListener {
            if (BaseApplication.isOnline(requireContext())) {
                if (validate()) {
                    streetName = edtStreetName.text.toString().trim()
                    streetNum = edtStreetNumber.text.toString().trim()
                    apartNum = edtApartNumber.text.toString().trim()
                    city = edtCity.text.toString().trim()
                    states = edtStates.text.toString().trim()
                    userAddress = edtAddress.text.toString().trim()
                    zipcode = edtPostalCode.text.toString().trim()
                    addFullAddressApi()
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
            dialogMiles.dismiss()
        }

        imageCross.setOnClickListener {
            dialogMiles.dismiss()
        }
    }

    private fun addFullAddressApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            checkoutScreenViewModel.addAddressUrl(
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
                getCheckoutApi()
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

    private fun validatation(): Boolean {
        // Check if email/phone is empty
        if (binding.tvAddNumber.text.toString().equals("Add number",true)) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.validPhone, false)
            return false
        } else if (binding.tvSetDoorStep.text.toString().equals("",true)) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.validPickUp, false)
            return false
        }
        return true
    }

    private fun validate(): Boolean {
        // Check if email/phone is empty
        if (edtStreetName.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.streetNameError, false)
            return false
        } else if (edtStreetNumber.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.streetNumberError, false)
            return false
        } else if (edtApartNumber.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.apartNumberError, false)
            return false
        } else if (edtCity.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.cityEnterError, false)
            return false
        } else if (edtStates.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.statesEnterError, false)
            return false
        } else if (edtAddress.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.addressError, false)
            return false
        } else if (edtPostalCode.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.postalCodeError, false)
            return false
        }
        return true
    }

    private fun addressPrimaryApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            checkoutScreenViewModel.makeAddressPrimaryUrl({
                BaseApplication.dismissMe()
                handleApiPrimaryResponse(it)
            }, addressId)
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
    private fun handleApiAddressPrimaryResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, AddressPrimaryResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogMiles?.dismiss()
                loadApi()
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


    override fun onMapReady(gmap: GoogleMap) {

        Log.d("Location latitude", "********$latitude")
        Log.d("Location longitude", "********$longitude")
        mMap = gmap

        val lat = latitude?.toDoubleOrNull() ?: 0.0  // Convert String to Double, default to 0.0 if null
        val lng = longitude?.toDoubleOrNull() ?: 0.0
        val newYork = LatLng(lat, lng)
        val customMarker = bitmapDescriptorFromVector(
            R.drawable.map_marker_icon,
            50,
            50
        ) // Change with your drawable

        /*        val newYork = LatLng(40.7128, -74.0060)
                val customMarker = bitmapDescriptorFromVector(R.drawable.current_location_marker,50,50) // Change with your drawable*/
//        val customMarker = bitmapDescriptorFromVector(R.drawable.marker_icon,50,50) // Change with your drawable
        mMap?.addMarker(
            MarkerOptions()
                .position(newYork)
                .icon(customMarker)
        )

        // 🔹 Disable map movement
        mMap?.uiSettings?.apply {
            isScrollGesturesEnabled = false  // ❌ Disable scrolling
            isZoomGesturesEnabled = false    // ❌ Disable zooming
            isTiltGesturesEnabled = false    // ❌ Disable tilt
            isRotateGesturesEnabled = false  // ❌ Disable rotation
        }

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 20f))
    }

    private fun bitmapDescriptorFromVector(
        vectorResId: Int, width: Int, height: Int): BitmapDescriptor? {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(requireContext(), vectorResId)
        if (vectorDrawable == null) {
            return null
        }
        // Create a new bitmap with desired width and height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Set bounds for the drawable
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    // Manage MapView Lifecycle
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun itemLongClick(position: Int?, latitudeValue: String?, fullAddress: String?, isZiggleEnabled: String) {
        if (isZiggleEnabled.equals("SelectPrimary",true)) {
            selectType = isZiggleEnabled
            addressId = position.toString()
        }
        if (isZiggleEnabled.equals("Edit",true)) {
            dialogMiles?.dismiss()
            val bundle = Bundle().apply {
                putString("latitude", addressList[position!!].latitude)
                putString("longitude", addressList[position].longitude)
                putString("address", fullAddress)
                putString("addressId", addressList[position].id.toString())
                putString("type", "Checkout")
            }
            findNavController().navigate(R.id.addressMapFullScreenFragment, bundle)
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

    override fun itemSelect(position: Int?, status: String?, type: String?) {

        cardId=cardMealMe[position!!].id.toString()
    }

}