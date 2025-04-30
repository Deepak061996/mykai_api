package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.AuthActivity
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentSettingProfileBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.SettingViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.ProfileRootResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class SettingProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSettingProfileBinding
    private var isAboutAppExpanded: Boolean = false
    private var isPostalCodeExpanded: Boolean = false
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var sessionManagement: SessionManagement
    private lateinit var viewModel: SettingViewModel
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var latitude = "0.0"
    private var longitude = "0.0"
    private var postalCode:String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentSettingProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SettingViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupUIVisibility()

        setupBackPressedCallback()

        commonWorkUtils = CommonWorkUtils(requireActivity())
        sessionManagement = SessionManagement(requireContext())

        setupClickListeners()

        // When screen load then api call
        fetchDataOnLoad()

//        initialize()

        return binding.root
    }

    private fun initialize() {
        if (BaseApplication.isOnline(requireActivity())){
            // This condition for check location run time permission
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation()
            } else {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 100
                )
            }
        }else{
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)

        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            // When location service is enabled
            // Get last location
            fusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                // Initialize location
                // Check condition
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    // When location result is not
                    getAddress(location.latitude,location.longitude)

                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()

                    binding.etEnterCode.setText(postalCode)

                } else {
                    // When location result is null
                    // initialize location request
                    val locationRequest = LocationRequest()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10000)
                        .setFastestInterval(1000)
                    // Initialize location call back
                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            // location
                            val location1 = locationResult.lastLocation
                            latitude = location1!!.latitude.toString()
                            longitude = location1.longitude.toString()

                            getAddress(location1.latitude,location1.longitude)
                            binding.etEnterCode.setText(postalCode)
                        }
                    }
                    fusedLocationClient!!.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()
                    )
                }
            }
        } else {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        }
    }

    private fun getAddress(lat: Double, longi: Double): String {
        var address = ""
        try {
            val addresses: List<Address>?
            val geocoder = Geocoder(requireActivity(), Locale.getDefault())
            addresses = geocoder.getFromLocation(
                lat,
                longi,
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses!![0].getAddressLine(0)
            //            String state = addresses.get(0).getAdminArea();
            val postCode = addresses[0].postalCode
            postalCode = postCode

        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return address
    }

    private fun fetchDataOnLoad() {
        if (BaseApplication.isOnline(requireActivity())) {
            fetchUserProfileData()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun  userDeleteData(dialog: Dialog) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.userDeleteData { result ->
                BaseApplication.dismissMe()
                handleApiLogOutResponse(result,dialog)
            }
        }
    }

    private fun  userLogOutData(dialog: Dialog) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.userLogOutData { result ->
                BaseApplication.dismissMe()
                handleApiLogOutResponse(result,dialog)
            }
        }
    }

    private fun fetchUserProfileData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.userProfileData { result ->
                BaseApplication.dismissMe()
                handleApiResponse(result)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> processSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleApiLogOutResponse(result: NetworkResult<String>, dialog: Dialog) {
        when (result) {
            is NetworkResult.Success -> processSuccessLogOutResponse(result.data.toString(),dialog)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun processSuccessResponse(response: String) {
        try {
            val apiModel = Gson().fromJson(response, ProfileRootResponse::class.java)
            Log.d("@@@ Response profile", "message :- $response")
            if (apiModel.code == 200 && apiModel.success) {
                viewModel.setProfileData(apiModel.data)
                updateUI(apiModel.data)
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

    private fun processSuccessLogOutResponse(response: String, dialog: Dialog) {
        try {
            val apiModel = Gson().fromJson(response, ProfileRootResponse::class.java)
            Log.d("@@@ Response profile", "message :- $response")
            if (apiModel.code == 200 && apiModel.success) {
                dialog.dismiss()
                sessionManagement.sessionClear()
                startActivity(Intent(requireActivity(), AuthActivity::class.java).apply {
                    putExtra("type", "login")
                    putExtra("backType", "no")
                })
                requireActivity().finish()
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

    @SuppressLint("SetTextI18n")
    private fun updateUI(data: Data) {

        binding.tvBio.isEnabled =false

        if (data.name!=null){
            binding.tvUserName.text = data.name
        }

        if (data.bio!=null){
            if (!data.bio.equals("null")){
                binding.tvBio.visibility =View.GONE
                binding.tvBio.setText(data.bio)
            }else{
                binding.tvBio.visibility =View.GONE
            }
        }else{
            binding.tvBio.visibility =View.GONE
        }

        if (data.profile_img!=null){
            Glide.with(this)
                .load(BaseUrl.imageBaseUrl+data.profile_img)
                .placeholder(R.drawable.mask_group_icon)
                .error(R.drawable.mask_group_icon)
                .into(binding.imageProfile)
        }

        if ((data.calories ?: 0) == 0 ) {
            binding.tvCalories.text =""+0
        } else {
            binding.tvCalories.text =""+data.calories!!.toInt()
        }

        if ( (data.carbs ?: 0) == 0) {
            binding.tvCarbs.text =""+0
        } else {
            binding.tvCarbs.text =""+data.carbs!!.toInt()
        }

        if ((data.fat ?: 0) == 0) {
            binding.tvFat.text =""+0
        } else {
            binding.tvFat.text =""+data.fat!!.toInt()
        }

        if ( (data.protien ?: 0) == 0) {
            binding.tvProtein.text =""+0
        } else {
            binding.tvProtein.text =""+data.protien!!.toInt()
        }


    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun setupUIVisibility() {
        (activity as MainActivity?)?.apply {
            binding.llIndicator.visibility = View.GONE
            binding.llBottomNavigation.visibility = View.GONE
        }
    }

    private fun setupBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.clearData()
                    findNavController().navigateUp()
                }
            }
        )
    }

    private fun setupClickListeners() {
        binding.apply {
            arrayOf(
                imageEditProfile, relMyWallet,relOrderHistory, relHealthData, relPreferences, imageNameEditable,
                imageEditTargets, imageProfile, imgBackProfileSetting, imgThreeDotIcon,relNotifications, relPrivacyTerms, relAboutApp,
                relPostalCode, rlSubmitBtn, relFeedbackSupport, relPrivacyPolicy, relSubscriptionPlan
            ).forEach { it.setOnClickListener(this@SettingProfileFragment) }
        }
    }

    override fun onClick(item: View) {
        when (item.id) {
            R.id.imgBackProfileSetting -> backButton()
            R.id.imgThreeDotIcon -> toggleMenuVisibility()
            R.id.relAboutApp -> toggleAboutAppVisibility()
            R.id.relPostalCode -> togglePostalCodeVisibility()
            R.id.rlSubmitBtn -> handlePostalCodeSubmit()
            R.id.imageEditProfile -> enableProfileEditing()
            R.id.imageNameEditable -> disableProfileEditing()
            R.id.imageEditTargets -> moveToNextScreen()
            R.id.relMyWallet -> navigateToFragment(R.id.walletFragment)
            R.id.relOrderHistory -> {
                val bundle = Bundle().apply {
                    putString("screen","no")
                }
                findNavController().navigate(R.id.orderHistoryFragment,bundle)
            }
            R.id.relHealthData -> navigateToFragment(R.id.healthDataFragment)
            R.id.relFeedbackSupport -> navigateToFragment(R.id.feedbackFragment)
            R.id.imageProfile -> navigateToFragment(R.id.editProfileFragment)
            R.id.relPreferences -> navigateToFragment(R.id.preferencesFragment)
            R.id.relNotifications -> navigateToFragment(R.id.notificationFragment)
            R.id.relPrivacyTerms -> navigateToFragment(R.id.termsConditionFragment)
            R.id.relPrivacyPolicy -> navigateToFragment(R.id.privacyPolicyFragment)
            R.id.relSubscriptionPlan ->{
                val bundle = Bundle()
                bundle.putString("screen","main")
                findNavController().navigate(R.id.subscriptionPlanOverViewFragment,bundle)
            }
        }
    }

    private fun backButton() {
        viewModel.clearData()
        findNavController().navigateUp()
    }

    private fun moveToNextScreen() {
        binding.relMacroNutTrg.setBackgroundResource(R.drawable.profile_editable_bg)
        navigateToFragment(R.id.healthDataFragment)
    }

    private fun toggleMenuVisibility() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_profile, null)
        val popupWindow = PopupWindow(popupView, 500, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(binding.imgThreeDotIcon,  0, 0, Gravity.END)
        // Access views inside the inflated layout using findViewById
        val relLogout = popupView?.findViewById<RelativeLayout>(R.id.relLogout)
        val relDeleteAccount = popupView?.findViewById<RelativeLayout>(R.id.relDeleteAccount)

        relLogout?.setOnClickListener {
            popupWindow.dismiss()
            showLogoutDialog()
        }

        relDeleteAccount?.setOnClickListener {
            popupWindow.dismiss()
            showRemoveAccountDialog()
        }

    }

    private fun toggleAboutAppVisibility() {
        isAboutAppExpanded = !isAboutAppExpanded
        binding.apply {
            imgDropAboutApp.setImageResource(
                if (isAboutAppExpanded) R.drawable.drop_up_small_icon else R.drawable.drop_down_small_icon
            )
            relPrivacyPolicy.visibility = if (isAboutAppExpanded) View.VISIBLE else View.GONE
            relPrivacyTerms.visibility = if (isAboutAppExpanded) View.VISIBLE else View.GONE
        }
    }

    private fun togglePostalCodeVisibility() {
        isPostalCodeExpanded = !isPostalCodeExpanded
        binding.apply {
            imgDropPostCode.setImageResource(
                if (isPostalCodeExpanded) R.drawable.drop_up_small_icon else R.drawable.drop_down_small_icon
            )
            relEnterCode.visibility = if (isPostalCodeExpanded) View.VISIBLE else View.GONE
        }
    }

    private fun handlePostalCodeSubmit() {
        binding.etEnterCode.text?.toString()?.trim().takeIf { it.isNullOrEmpty() }?.let {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.postCode, false)
        } ?: postCodeApi()/*closePostalCodeSection()*/
    }

    private fun postCodeApi() {
        if (BaseApplication.isOnline(requireActivity())) {
            updatePostCodeApi()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun updatePostCodeApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.updatePostCodeApi({ result ->
                BaseApplication.dismissMe()
                handlePostalApiResponse(result)
            },postalCode,longitude, latitude)
        }

    }

    private fun handlePostalApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> processSuccessPostalResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun processSuccessPostalResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                closePostalCodeSection()
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                /*findNavController().navigateUp()*/
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

    private fun closePostalCodeSection() {
        binding.apply {
            imgDropPostCode.setImageResource(R.drawable.drop_up_small_icon)
            relEnterCode.visibility = View.GONE
        }
        isPostalCodeExpanded = false
    }

    private fun enableProfileEditing() {
        binding.tvBio.isEnabled = true
        binding.tvBio.visibility = View.GONE
        binding.imageEditTargets.visibility = View.VISIBLE
        binding.imageProfile.isClickable = true
        binding.imageNameEditable.visibility = View.VISIBLE
        binding.relProfileNameImage.setBackgroundResource(R.drawable.profile_editable_bg)
    }

    private fun disableProfileEditing() {
        if (BaseApplication.isOnline(requireActivity())) {
            if (binding.tvBio.text.toString().trim().isEmpty()){
                BaseApplication.alertError(requireContext(), ErrorMessage.bioError, false)
            }else{
                upDateProfile()
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun upDateProfile() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.userProfileUpdateBioApi(
                {
                    BaseApplication.dismissMe()
                    handleApiUpdateResponse(it)
                }, binding.tvBio.text.toString())
        }
    }


    private fun handleApiUpdateResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleUpdateSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleUpdateSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ProfileRootResponse::class.java)
            Log.d("@@@ Health profile", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                binding.apply {
                    tvBio.visibility = View.GONE
                    tvBio.isEnabled = false
                    imageNameEditable.visibility = View.GONE
                    imageEditTargets.visibility = View.GONE
                    relProfileNameImage.setBackgroundResource(R.drawable.calendar_events_bg)
                    sessionManagement.setUserName(binding.tvUserName.text.toString())
                    apiModel.data.profile_img?.let { sessionManagement.setImage(it) }
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


    private fun navigateToFragment(destinationId: Int) {
        findNavController().navigate(destinationId)
    }

    private fun showLogoutDialog() {
        val dialog=Dialog(requireContext())
        dialog.setContentView(R.layout.alert_dialog_logout_popup)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.tvDialogCancelBtn).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<TextView>(R.id.tvDialogLogoutBtn)?.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                userLogOutData(dialog)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
        dialog.show()
    }

    private fun showRemoveAccountDialog() {
        val dialog=Dialog(requireContext())
        dialog.setContentView(R.layout.alert_dialog_delete_account)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.tvDialogCancelBtn).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<TextView>(R.id.tvDialogRemoveBtn)?.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                userDeleteData(dialog)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
        dialog.show()
    }

}
