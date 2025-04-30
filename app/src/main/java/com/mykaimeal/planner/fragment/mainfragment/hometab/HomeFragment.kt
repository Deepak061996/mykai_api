package com.mykaimeal.planner.fragment.mainfragment.hometab

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
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
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.HomeSuperMarketList
import com.mykaimeal.planner.adapter.RecipeCookedAdapter
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentHomeBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.HomeViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.HomeApiResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.SuperMarketModels
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.SuperMarketModelsData
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener, OnItemClickListener, OnItemSelectListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var recipeCookedAdapter: RecipeCookedAdapter? = null
    private var adapterSuperMarket: HomeSuperMarketList? = null
    private var recySuperMarket: RecyclerView? = null
    private lateinit var sessionManagement: SessionManagement
    private lateinit var viewModel: HomeViewModel
    private lateinit var userDataLocal: com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.DataModel
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private var latitude: String = "0"
    private var longitude: String = "0"
    private var storeUuid: String = ""
    private var storeName: String = ""
    private var cookstatus = false
    private var tAG: String = "Location"
    private var superMarketData: MutableList<SuperMarketModelsData>? = null
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> =
        mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sessionManagement = SessionManagement(requireContext())

        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager =
            requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        val main = (activity as MainActivity?)
        if (main != null) {
            main.alertStatus = false
            main.changeBottom("home")
            main.binding.apply {
                llIndicator.visibility = View.VISIBLE
                llBottomNavigation.visibility = View.VISIBLE
            }
            if (main.Subscription_status == 1) {
                binding.imgFreeTrial.visibility = View.VISIBLE
            } else {
                binding.imgFreeTrial.visibility = View.GONE
            }
        }

        cookbookList.clear()
        val data =
            com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data(
                "",
                "",
                0,
                "",
                "Favorites",
                0,
                "",
                0
            )
        cookbookList.add(0, data)

        backButton()
        initialize()

        if (viewModel.data != null) {
            showData(viewModel.data)
        } else {
            // When screen load then api call
            fetchDataOnLoad()
        }


        return binding.root
    }

    private fun backButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val startMain = Intent(Intent.ACTION_MAIN)
                    startMain.addCategory(Intent.CATEGORY_HOME)
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(startMain)
                }
            })
    }

    private fun getLatLong() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }

    private fun fetchDataOnLoad() {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                viewModel.homeDetailsRequest {
                    BaseApplication.dismissMe()
                    binding.pullToRefresh.isRefreshing = false
                    handleApiResponse(it, "HomeData")
                }
            }
        } else {
            binding.pullToRefresh.isRefreshing = false
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun supermarketOnLoad() {
        if (BaseApplication.isOnline(requireActivity())) {
            superMarketDetailsData()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun superMarketDetailsData() {
        lifecycleScope.launch {
            viewModel.getSuperMarket({
                handleMarketApiResponse(it)
            }, latitude, longitude)
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>, type: String) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString(), type)
            is NetworkResult.Error -> {
                subscriptionImage()
                showAlert(result.message, false)
            }

            else -> {
                subscriptionImage()
                showAlert(result.message, false)
            }
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
            val apiModel = Gson().fromJson(data, SuperMarketModels::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                showUIData(apiModel.data)
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showUIData(data: MutableList<SuperMarketModelsData>?) {
        try {
            if (data != null) {
                superMarketData = data
                val dialogAddItem: Dialog = context?.let { Dialog(it) }!!
                dialogAddItem.setContentView(R.layout.alert_dialog_super_market)
                dialogAddItem.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogAddItem.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                recySuperMarket = dialogAddItem.findViewById(R.id.recySuperMarket)
                val rlDoneBtn = dialogAddItem.findViewById<RelativeLayout>(R.id.rlDoneBtn)
                dialogAddItem.setCancelable(false)
                dialogAddItem.show()
                dialogAddItem.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                adapterSuperMarket = HomeSuperMarketList(data, requireActivity(), this, 0)
                recySuperMarket!!.adapter = adapterSuperMarket

                rlDoneBtn.setOnClickListener {
                    if (!storeUuid.equals("", true)) {
                        if (BaseApplication.isOnline(requireActivity())) {
                            BaseApplication.showMe(requireContext())
                            lifecycleScope.launch {
                                viewModel.superMarketSaveRequest({
                                    BaseApplication.dismissMe()
                                    dialogAddItem.dismiss()
                                    handleApiResponse(it, "storeData")
                                }, storeUuid, storeName)
                            }
                        } else {
                            BaseApplication.alertError(
                                requireContext(),
                                ErrorMessage.networkError,
                                false
                            )
                        }
                    }
                }

            }

        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String, type: String) {
        try {
            val apiModel = Gson().fromJson(data, HomeApiResponse::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            subscriptionImage()
            if (apiModel.code == 200 && apiModel.success) {
                if (type.equals("HomeData", true)) {
                    showData(apiModel.data)
                } else {
                    userDataLocal.is_supermarket = 0
                    viewModel.setData(userDataLocal)
                    Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                handleError(apiModel.code, apiModel.message)
            }

        } catch (e: Exception) {
            subscriptionImage()
            showAlert(e.message, false)
        }
    }

    private fun subscriptionImage() {
        if ((activity as MainActivity?)?.Subscription_status == 1) {
            binding.imgFreeTrial.visibility = View.VISIBLE
        } else {
            binding.imgFreeTrial.visibility = View.GONE
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showData(data: com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.DataModel?) {
        try {
            viewModel.setData(data!!)
            userDataLocal = data

            if (userDataLocal.userData != null && userDataLocal.userData!!.size > 0) {
                binding.relPlanMeal.visibility = View.GONE
                binding.llRecipesCooked.visibility = View.VISIBLE
                recipeCookedAdapter =
                    RecipeCookedAdapter(userDataLocal.userData, requireActivity(), this)
                binding.rcyRecipesCooked.adapter = recipeCookedAdapter
            } else {
                binding.relPlanMeal.visibility = View.VISIBLE
                binding.llRecipesCooked.visibility = View.GONE
            }

            if (userDataLocal.graph_value == 0) {
                binding.relMonthlySavingsss.visibility = View.GONE
                binding.relCheckSavingsss.visibility = View.VISIBLE
            } else {
                binding.relMonthlySavingsss.visibility = View.VISIBLE
                binding.relCheckSavingsss.visibility = View.GONE
            }

            /*    if (userDataLocal.graph_value == 0) {
                    binding.relMonthlySavingsss.visibility = View.VISIBLE
                    binding.relCheckSavingsss.visibility = View.GONE
                } else {
                    binding.relMonthlySavingsss.visibility = View.GONE
                    binding.relCheckSavingsss.visibility = View.VISIBLE
                }*/

            if (userDataLocal.date != null && !userDataLocal.date.equals("", true)) {
                val name = BaseApplication.getColoredSpanned(
                    "Next meal to be cooked on ",
                    "#3C4541"
                ) + BaseApplication.getColoredSpanned(data.date + ".", "#06C169")
                binding.tvHomeDesc.text = Html.fromHtml(name)
            } else {
                binding.tvHomeDesc.text =
                    "Your cooking schedule is empty! Tap the button below to add a meal and get started."
            }

            fun updateCount(breakfast: Int?) {
                if (breakfast!! != 0) {
                    cookstatus = true
                }
                Log.d("status ,", "******$cookstatus")

                if (cookstatus) {
                    binding.rlSeeAllBtn.visibility = View.VISIBLE
                    binding.imageCookedMeals.visibility = View.GONE
                } else {
                    binding.imageCookedMeals.visibility = View.VISIBLE
                    binding.rlSeeAllBtn.visibility = View.GONE
                }
            }

            if (userDataLocal.frezzer != null) {

                if (userDataLocal.frezzer?.Breakfast != null) {
                    binding.tvfreezerbreakfast.text = "" + userDataLocal.frezzer?.Breakfast
                    updateCount(userDataLocal.frezzer?.Breakfast)

                }
                if (userDataLocal.frezzer?.Lunch != null) {
                    binding.tvfreezerlunch.text = "" + userDataLocal.frezzer?.Lunch
                    updateCount(userDataLocal.frezzer?.Lunch)
                }
                if (userDataLocal.frezzer?.Dinner != null) {
                    binding.tvfreezerdinner.text = "" + userDataLocal.frezzer?.Dinner
                    updateCount(userDataLocal.frezzer?.Dinner)
                }

                if (userDataLocal.frezzer?.Snacks != null) {
                    binding.laySnack.visibility = View.VISIBLE
                    binding.tvfreezersnack.text = "" + userDataLocal.frezzer?.Snacks
                    updateCount(userDataLocal.frezzer?.Snacks)
                } else {
                    binding.tvfreezersnack.visibility = View.GONE
                }

                if (userDataLocal.frezzer?.Teatime != null) {
                    binding.layTeatime.visibility = View.VISIBLE
                    binding.tvfreezerteatime.text = "" + userDataLocal.frezzer?.Teatime
                    updateCount(userDataLocal.frezzer?.Breakfast)
                } else {
                    binding.layTeatime.visibility = View.GONE
                }
            }

            if (userDataLocal.fridge != null) {
                if (userDataLocal.fridge?.Breakfast != null) {
                    binding.tvfridgebreakfast.text = "" + userDataLocal.fridge?.Breakfast
                    updateCount(userDataLocal.fridge?.Breakfast)
                }
                if (userDataLocal.fridge?.Lunch != null) {
                    binding.tvfridgelunch.text = "" + userDataLocal.fridge?.Lunch
                    updateCount(userDataLocal.fridge?.Lunch)
                }
                if (userDataLocal.fridge?.Dinner != null) {
                    binding.tvfridgedinner.text = "" + userDataLocal.fridge?.Dinner
                    updateCount(userDataLocal.fridge?.Dinner)
                }
                if (userDataLocal.fridge?.Snacks != null) {
                    binding.laySnack.visibility = View.VISIBLE
                    binding.tvfridgesnack.text = "" + userDataLocal.fridge?.Snacks
                    updateCount(userDataLocal.fridge?.Snacks)
                } else {
                    binding.laySnack.visibility = View.GONE
                }

                if (userDataLocal.fridge?.Teatime != null) {
                    binding.layTeatime.visibility = View.VISIBLE
                    binding.tvfridgeteatime.text = "" + userDataLocal.fridge?.Teatime
                    updateCount(userDataLocal.fridge?.Teatime)
                } else {
                    binding.layTeatime.visibility = View.GONE
                }
            }

            userDataLocal.monthly_savings?.let {
                if (sessionManagement.getUserName() != null) {
                    binding.tvMonthlySavingsDesc.text =
                        "Good job ${sessionManagement.getUserName()}, you are on track to save ${it} this month"
                }
            }

            userDataLocal.is_supermarket?.let {
                if (it == 1) {
                    supermarketOnLoad()
//                    getLatLong()
                }
            }


        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {

        if (sessionManagement.getImage() != null) {
            Glide.with(requireContext())
                .load(BaseUrl.imageBaseUrl + sessionManagement.getImage())
                .placeholder(R.drawable.mask_group_icon)
                .error(R.drawable.mask_group_icon)
                .into(binding.imageProfile)
        }

        if (sessionManagement.getUserName() != null) {
            val name = BaseApplication.getColoredSpanned(
                "Hello",
                "#06C169"
            ) + BaseApplication.getColoredSpanned(", " + sessionManagement.getUserName(), "#000000")
            binding.tvName.text = Html.fromHtml(name)
            binding.tvMonthlySavingsDesc.text =
                "Good job ${sessionManagement.getUserName()}, you are on track to save £0 this month"
        }


        binding.rlSeeAllBtn.setOnClickListener(this)
        binding.textSeeAll.setOnClickListener(this)
        binding.imageCookedMeals.setOnClickListener(this)
        binding.imgFreeTrial.setOnClickListener(this)
        binding.imgBasketIcon.setOnClickListener(this)
        binding.imageProfile.setOnClickListener(this)
        binding.rlPlanAMealBtn.setOnClickListener(this)
        binding.imgHearRedIcons.setOnClickListener(this)
        binding.imagePlanMeal.setOnClickListener(this)
        binding.tvPlanMeal.setOnClickListener(this)
//        binding!!.imageRecipeSeeAll.setOnClickListener(this)
//        binding!!.relMonthlySavings.setOnClickListener(this)
        binding.imageCheckSav.setOnClickListener(this)
        binding.rlLayCheckSavings.setOnClickListener(this)

        binding.pullToRefresh.setOnRefreshListener {
            // When screen load then api call
            fetchDataOnLoad()
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
                    supermarketOnLoad()
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
                            supermarketOnLoad()
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

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.textSeeAll -> {
                findNavController().navigate(R.id.fullCookedScheduleFragment)
            }

            R.id.rlSeeAllBtn -> {
                findNavController().navigate(R.id.cookedFragment)
            }

            R.id.imageCheckSav -> {
                findNavController().navigate(R.id.statisticsGraphFragment)
            }

            R.id.rlLayCheckSavings -> {
                findNavController().navigate(R.id.statisticsGraphFragment)
            }

            R.id.imagePlanMeal -> {
                findNavController().navigate(R.id.planFragment)
            }

            R.id.imageCookedMeals -> {
                findNavController().navigate(R.id.cookedFragment)
            }

            R.id.imgBasketIcon -> {
                (activity as MainActivity?)?.upBasket()
                findNavController().navigate(R.id.basketScreenFragment)
            }

            R.id.imgHearRedIcons -> {
                findNavController().navigate(R.id.cookBookFragment)
            }

            R.id.imageProfile -> {
                findNavController().navigate(R.id.settingProfileFragment)
            }

            R.id.rlPlanAMealBtn -> {
                findNavController().navigate(R.id.planFragment)
            }

            R.id.tvPlanMeal -> {
                findNavController().navigate(R.id.planFragment)
            }

            R.id.imgFreeTrial -> {
                findNavController().navigate(R.id.subscriptionPlanOverViewFragment)
            }

            /*  R.id.imgFreeTrial -> {
                  findNavController().navigate(R.id.homeSubscriptionFragment)
              }*/
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        when (status) {
            "1" -> {
                val bundle = Bundle().apply {
                    putString("uri", type)
                    putString("schId", position.toString())
                }
                findNavController().navigate(R.id.missingIngredientsFragment, bundle)
            }

            "2" -> {

            }

            "4" -> {
                if (BaseApplication.isOnline(requireActivity())) {

                    if ((activity as? MainActivity)?.Subscription_status == 1) {
                        if ((activity as? MainActivity)?.favorite!! <= 2) {
                            // Safely get the item and position
                            val newLikeStatus =
                                if (userDataLocal.userData?.get(position!!)?.is_like == 0) "1" else "0"
                            if (newLikeStatus.equals("0", true)) {
                                recipeLikeAndUnlikeData(position, newLikeStatus, "", null)
                            } else {
                                addFavTypeDialog(position, newLikeStatus)
                            }
                        } else {
                            (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                        }
                    } else {
                        // Safely get the item and position
                        val newLikeStatus =
                            if (userDataLocal.userData?.get(position!!)?.is_like == 0) "1" else "0"
                        if (newLikeStatus.equals("0", true)) {
                            recipeLikeAndUnlikeData(position, newLikeStatus, "", null)
                        } else {
                            addFavTypeDialog(position, newLikeStatus)
                        }
                    }

                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }

            "5" -> {
                val bundle = Bundle().apply {
                    putString("uri", type)
                    putString("mealType", userDataLocal.userData?.get(position!!)?.recipe?.type)
                }
                findNavController().navigate(R.id.recipeDetailsFragment, bundle)
            }
        }
    }

    private fun addFavTypeDialog(position: Int?, likeType: String) {
        val dialogAddRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogAddRecipe.setContentView(R.layout.alert_dialog_add_recipe)
        dialogAddRecipe.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogAddRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogAddRecipe.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        spinnerActivityLevel = dialogAddRecipe.findViewById(R.id.spinnerActivityLevel)
        val relCreateNewCookBook =
            dialogAddRecipe.findViewById<RelativeLayout>(R.id.relCreateNewCookBook)
        val imgCheckBoxOrange = dialogAddRecipe.findViewById<ImageView>(R.id.imgCheckBoxOrange)
        spinnerActivityLevel.setItems(cookbookList.map { it.name })
        dialogAddRecipe.show()
        dialogAddRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        getCookBookList()
        relCreateNewCookBook.setOnClickListener {
            relCreateNewCookBook.setBackgroundResource(R.drawable.light_green_rectangular_bg)
            imgCheckBoxOrange.setImageResource(R.drawable.orange_uncheck_box_images)
            dialogAddRecipe.dismiss()
            val bundle = Bundle()
            bundle.putString("value", "New")
            bundle.putString("uri", userDataLocal.userData?.get(position!!)?.recipe?.uri)
            findNavController().navigate(R.id.createCookBookFragment, bundle)
        }
        rlDoneBtn.setOnClickListener {
            if (spinnerActivityLevel.text.toString().equals("", true)) {
                BaseApplication.alertError(
                    requireContext(),
                    ErrorMessage.selectCookBookError,
                    false
                )
            } else {
                val cookBookType = cookbookList[spinnerActivityLevel.selectedIndex].id
                recipeLikeAndUnlikeData(
                    position,
                    likeType,
                    cookBookType.toString(),
                    dialogAddRecipe
                )
            }
        }
    }

    private fun getCookBookList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
                    cookbookList.retainAll { it == cookbookList[0] }
                    cookbookList.addAll(apiModel.data)
                    // OR directly modify the original list
                    spinnerActivityLevel.setItems(cookbookList.map { it.name })
                }
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleApiCookBookResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun recipeLikeAndUnlikeData(
        position: Int?,
        likeType: String,
        cookbooktype: String,
        dialogAddRecipe: Dialog?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.likeUnlikeRequest(
                {
                    BaseApplication.dismissMe()
                    handleLikeAndUnlikeApiResponse(it, position, dialogAddRecipe)
                },
                userDataLocal.userData?.get(position!!)?.recipe?.uri.toString(),
                likeType,
                cookbooktype
            )
        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(
                result.data.toString(),
                position,
                dialogAddRecipe
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(
        data: String,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                val item = userDataLocal.userData?.getOrNull(position!!) ?: return
                // Toggle the is_like value
                item.is_like = if (item.is_like == 0) 1 else 0
                // Update the list at the specific position
                userDataLocal.userData!![position!!] = item
                recipeCookedAdapter?.updateList(userDataLocal.userData)
                dialogAddRecipe?.dismiss()
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {

        storeUuid = position?.let { superMarketData?.get(it)?.store_uuid.toString() }.toString()
        storeName = position?.let { superMarketData?.get(it)?.store_name.toString() }.toString()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            displayLocationSettingsRequest(requireContext())
        } else {
//            findNavController().navigate(R.id.enterYourAddressFragment)
//            showLocationError(requireContext(), ErrorMessage.locationError)
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
                    Log.i(
                        tAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
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

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    tAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )

            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Activity.RESULT_OK == resultCode) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        if (requestCode == 200) {
            // This condition for check location run time permission
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation()
            } else {
                showLocationError(requireContext(), ErrorMessage.locationError)
            }
        }
    }

    private fun showLocationError(context: Context?, errorMsg: String?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.alert_dialog_box_error)
        val tvTitle: TextView = dialog!!.findViewById(R.id.tv_text)
        val btnOk: RelativeLayout = dialog.findViewById(R.id.btn_okay)
        tvTitle.text = errorMsg
        btnOk.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 200)
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}