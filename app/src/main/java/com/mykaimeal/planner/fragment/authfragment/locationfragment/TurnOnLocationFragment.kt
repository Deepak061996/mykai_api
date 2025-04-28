package com.mykaimeal.planner.fragment.authfragment.locationfragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentTurnOnLocationBinding
import com.mykaimeal.planner.fragment.authfragment.enteryouraddress.AddressViewModel
import com.mykaimeal.planner.fragment.authfragment.locationfragment.model.LocationModel
import com.mykaimeal.planner.fragment.authfragment.locationfragment.viewmodel.LocationViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addressmapfullscreen.model.AddAddressModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class TurnOnLocationFragment : Fragment() {
    private var _binding: FragmentTurnOnLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private var latitude: String = "0"
    private var longitude: String = "0"
    private var address: String? = ""
    private var tAG: String = "Location"

    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var edtStreetName: EditText
    private lateinit var edtStreetNumber: EditText
    private lateinit var edtApartNumber: EditText
    private lateinit var edtCity: EditText
    private lateinit var edtStates: EditText
    private lateinit var edtPostalCode: EditText
    private lateinit var edtAddress: EditText
    private var setStatus = "Home"
    private var streetName: String? = ""
    private var streetNum: String? = ""
    private var apartNum: String? = ""
    private var city: String? = ""
    private var states: String? = ""
    private var country: String? = ""
    private var zipcode: String? = ""
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentTurnOnLocationBinding.inflate(inflater, container, false)

        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        commonWorkUtils = CommonWorkUtils(requireActivity())
        // This is use for LocationServices declaration
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        /// handle on back pressed
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })

        ///main function using all triggered of this screen
        initialize()

        return binding.root
    }

    private fun initialize() {

        /// handle on back pressed
        binding.imgBackTurnLocation.setOnClickListener {
            requireActivity().finish()
        }

        binding.rlShareLocation.setOnClickListener {
            if (BaseApplication.isOnline(requireContext())){
                // This condition for check location run time permission
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
                }
            }else{
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (requestCode == 100 && grantResults.isNotEmpty() && (grantResults[0] + grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                displayLocationSettingsRequest(requireContext())
            } else {
                findNavController().navigate(R.id.enterYourAddressFragment)
            }
        }

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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Activity.RESULT_OK == resultCode) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
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

                    address = getAddressFromLatLng(location.latitude, location.longitude)

                    getAddressFromLocation(location.latitude, location.longitude)

                    fullAddressDialog()

                    /*locationApi()*/

                } else {
//                    // When location result is null
//                    // initialize location request
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

                            address = getAddressFromLatLng(location1.latitude, location1.longitude)

                            getAddressFromLocation(location1.latitude, location1.longitude)

                            fullAddressDialog()

                            /*locationApi()*/
                        }
                    }
//                    // Request location updates
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

        if (address != "") {
            edtAddress.setText(address.toString())
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
                    address = edtAddress.text.toString().trim()
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
            locationViewModel.addAddressUrl(
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
                setStatus
            )
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
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
                findNavController().navigate(R.id.turnOnNotificationsFragment)
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

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            geocoder.getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
                ?.getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if an error occurs
        }
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

    /// implement location api & redirection
    private fun locationApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            locationViewModel.updateLocation({
                    BaseApplication.dismissMe()
                    when (it) {
                        is NetworkResult.Success -> {
                            try {
                                val gson = Gson()
                                val locationModel = gson.fromJson(it.data, LocationModel::class.java)
                                if (locationModel.code == 200 && locationModel.success) {
                                    findNavController().navigate(R.id.turnOnNotificationsFragment)
                                } else {
                                    if (locationModel.code == ErrorMessage.code) {
                                        showAlertFunction(locationModel.message, true)
                                    } else {
                                        showAlertFunction(locationModel.message, false)
                                    }
                                }
                            }catch (e:Exception){
                                Log.d("Location On","message:-- "+e.message)
                            }
                        }

                        is NetworkResult.Error -> {
                            showAlertFunction(it.message, false)
                        }

                        else -> {
                            showAlertFunction(it.message, false)
                        }
                    }
                }, "1"
            )
        }
    }

    /// show error message
    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}