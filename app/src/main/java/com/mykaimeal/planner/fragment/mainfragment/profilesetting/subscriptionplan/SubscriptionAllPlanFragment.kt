package com.mykaimeal.planner.fragment.mainfragment.profilesetting.subscriptionplan

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.SubscriptionAdaptor
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.AppConstant
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.alertError
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentHomeSubscriptionAllPlanBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.subscriptionplan.viewmodel.SubscriptionPlanViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.HomeViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.HomeApiResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.SubscriptionModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.stream.Collectors



@AndroidEntryPoint
class SubscriptionAllPlanFragment : Fragment() {

    private var _binding: FragmentHomeSubscriptionAllPlanBinding?=null
    private val binding get() = _binding!!
    var adapter: SubscriptionAdaptor? = null
    var datalist: ArrayList<SubscriptionModel> = arrayListOf()
    private var billingClient: BillingClient? = null
    private var premiumMonthly = ""
    private var premiumAnnual = ""
    private var premiumWeekly = ""
    private var planID = AppConstant.Premium_Weekly
    private var planType = "Starter"
    private lateinit var viewModel: SubscriptionPlanViewModel
    private lateinit var sessionManagement: SessionManagement
    private var screen:String=""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding=FragmentHomeSubscriptionAllPlanBinding.inflate(layoutInflater, container, false)
        screen= arguments?.getString("screen","")?:""


        if (!screen.equals("login",true)){
            (activity as? MainActivity)?.binding?.apply {
                llIndicator.visibility = View.GONE
                llBottomNavigation.visibility = View.GONE
            }
        }

        viewModel = ViewModelProvider(requireActivity())[SubscriptionPlanViewModel::class.java]

        binding.rlNextBtn.isClickable=false
        binding.rlNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)

        sessionManagement = SessionManagement(requireContext())


        if (!sessionManagement.getProviderName().equals("",true)){
            if (!sessionManagement.getProviderName().equals("null",true)){
                binding.tvTextNames.text = sessionManagement.getProviderName()
            }
        }

        if (!sessionManagement.getProviderImage().equals("",true)){
             Glide.with(requireContext())
                 .load(sessionManagement.getProviderImage())
                 .placeholder(R.drawable.mask_group_icon)
                 .error(R.drawable.mask_group_icon)
                 .into(binding.imageProfile)
        }


        backButton()

        initialize()

        startBillingApi()

        return binding.root
    }

    private fun apiStatus() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.subscriptionPurchaseType {
                BaseApplication.dismissMe()
                handleApiResponse(it,"status")
            }
        }
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    movToPlan()
                }
            })
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun initialize() {
        if (sessionManagement.getImage() != null) {
            Glide.with(requireContext())
                .load(BaseUrl.imageBaseUrl + sessionManagement.getImage())
                .placeholder(R.drawable.mask_group_icon)
                .error(R.drawable.mask_group_icon)
                .into(binding.imageProfile)
        }



        binding.crossImages.setOnClickListener{
            movToPlan()
        }

        // Usage
        binding.relSubscriptionBasic.setOnClickListener {
            selectPlan(
                binding.relSubscriptionBasic,
                binding.tvStarter,
                binding.imgBasicClick,
                binding.imagePrevious,
                binding.tvNewKai,
                binding.tvNewDollar,
                listOf(binding.relPopularPlan, binding.relBestPlan),
                listOf(binding.tvPopular, binding.tvBest),
                listOf(binding.imagePreviousMonthly, binding.imagePreviousyearly),
                listOf(binding.imgPopularClick, binding.imgBaseClick),
                listOf(binding.tvProkaiUser, binding.tvLovekaiUser),
                listOf(binding.tvNewDollarMonthly, binding.tvNewDollaryearly),AppConstant.Premium_Weekly,"Starter")
        }
        binding.relPopularPlan.setOnClickListener {
            selectPlan(
                binding.relPopularPlan,
                binding.tvPopular,
                binding.imgPopularClick,
                binding.imagePreviousMonthly,
                binding.tvProkaiUser,
                binding.tvNewDollarMonthly,
                listOf(binding.relSubscriptionBasic, binding.relBestPlan),
                listOf(binding.tvStarter, binding.tvBest),
                listOf(binding.imagePrevious, binding.imagePreviousyearly),
                listOf(binding.imgBasicClick, binding.imgBaseClick),
                listOf(binding.tvNewKai, binding.tvLovekaiUser),
                listOf(binding.tvNewDollar, binding.tvNewDollaryearly),AppConstant.Premium_Monthly,"Popular"
            )
        }
        binding.relBestPlan.setOnClickListener {
            selectPlan(
                binding.relBestPlan,
                binding.tvBest,
                binding.imgBaseClick,
                binding.imagePreviousyearly,
                binding.tvLovekaiUser,
                binding.tvNewDollaryearly,
                listOf(binding.relSubscriptionBasic, binding.relPopularPlan),
                listOf(binding.tvStarter, binding.tvPopular),
                listOf(binding.imagePrevious, binding.imagePreviousMonthly),
                listOf(binding.imgBasicClick, binding.imgPopularClick),
                listOf(binding.tvNewKai, binding.tvProkaiUser),
                listOf(binding.tvNewDollar, binding.tvNewDollarMonthly),AppConstant.Premium_Annual,"Best")
        }
        binding.rlNextBtn.setOnClickListener {
            if (binding.rlNextBtn.isClickable){
                if (isOnline(requireContext())) {
                    if (!planID.equals("",true)){
                        planPurchases()
                    }else{
                        alertError(requireContext(), ErrorMessage.planError, false)
                    }
                } else {
                    alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun planPurchases() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                billingClient?.startConnection(this)
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            listOf(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(planID)
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            )
                        ).build()

                    billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult1, productDetailsList ->
                        for (productDetails in productDetailsList) {
                            var offerToken = ""
                            productDetails.subscriptionOfferDetails?.let { offerDetailsList ->
                                for (offerDetails in offerDetailsList) {
                                    if (offerDetails.offerId?.equals("freetrail", ignoreCase = true) == true) {
                                        offerToken = offerDetails.offerToken
                                    }
                                }
                            }

                            val productDetailsParamsList = listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                    .build()
                            )
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()
                            billingClient?.launchBillingFlow(requireActivity(), billingFlowParams)
                        }
                    }
                }
            }
        })
    }


    private fun movToPlan(){
        if (screen.equals("login",true)){
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }else{
            findNavController().navigateUp()
        }
    }

    private fun selectPlan(selectedPlan: View, tvPopular: TextView,selectedImage: ImageView, crossImage: ImageView, selectedUserText: TextView, selectedDollarText: TextView, otherPlans: List<View>, textshow: List<TextView>, otherCroseImage: List<ImageView>, otherImages: List<ImageView>, otherUserTexts: List<TextView>, otherDollarTexts: List<TextView>, planIDUser:String,planTypeStatus:String) {
        planID=planIDUser
        planType=planTypeStatus
        selectedPlan.setBackgroundResource(R.drawable.subscription_click_bg)
        tvPopular.setBackgroundResource(R.drawable.basic_click_bg)
        tvPopular.setTextColor(Color.parseColor("#121212"))
        selectedImage.setImageResource(R.drawable.selected_plan_icon)
        crossImage.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN)
        selectedUserText.setTextColor(Color.parseColor("#FFFFFF"))
        selectedDollarText.setTextColor(Color.parseColor("#FFFFFF"))
        textshow.forEach { it.setBackgroundResource(R.drawable.popular_click_bg)}
        otherPlans.forEach { it.setBackgroundResource(R.drawable.subscription_unclick_bg)}
        otherCroseImage.forEach { it.setColorFilter(Color.parseColor("#757575"), PorterDuff.Mode.SRC_IN)}
        otherImages.forEach { it.setImageResource(R.drawable.unselelected_plan_icon)}
        textshow.forEach { it.setTextColor(Color.parseColor("#FFFFFF"))}
        otherUserTexts.forEach { it.setTextColor(Color.parseColor("#000000"))}
        otherDollarTexts.forEach { it.setTextColor(Color.parseColor("#000000"))}
    }

    private fun startBillingApi() {
//        BaseApplication.showMe(requireActivity())
        billingClient = BillingClient.newBuilder(requireActivity())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        getPrices()

    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult: BillingResult, purchases: List<Purchase>? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d("Testing", "Hello")
                        val orderId = purchase.orderId
                        val purchaseToken1 = purchase.purchaseToken
                        Log.d("TESTING_SPARK", "$orderId orderId")
                        Log.d("TESTING_Spark", "$purchaseToken1 purchase token1")
                        binding.rlNextBtn.isClickable=false
                        binding.rlNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                        handlePurchase(purchase)
                    }
                }
            } else {
                binding.rlNextBtn.isClickable=true
                binding.rlNextBtn.setBackgroundResource(R.drawable.gray_btn_select_background)
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Toast.makeText(
                        requireActivity(), "Already Subscribed", Toast.LENGTH_LONG).show()

                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> Toast.makeText(
                        requireActivity(), "FEATURE_NOT_SUPPORTED", Toast.LENGTH_LONG).show()

                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> Toast.makeText(
                        requireActivity(), "BILLING_UNAVAILABLE", Toast.LENGTH_LONG).show()

                    BillingClient.BillingResponseCode.USER_CANCELED -> Toast.makeText(
                        requireActivity(), "USER_CANCELLED", Toast.LENGTH_LONG).show()

                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Toast.makeText(
                        requireActivity(), "DEVELOPER_ERROR", Toast.LENGTH_LONG).show()

                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> Toast.makeText(
                        requireActivity(), "ITEM_UNAVAILABLE", Toast.LENGTH_LONG).show()

                    BillingClient.BillingResponseCode.NETWORK_ERROR -> Toast.makeText(
                        requireActivity(), "NETWORK_ERROR", Toast.LENGTH_LONG).show()

                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> Toast.makeText(
                        requireActivity(), "SERVICE_DISCONNECTED", Toast.LENGTH_LONG).show()

                    else -> Toast.makeText(
                        requireActivity(), "Error " + billingResult.debugMessage, Toast.LENGTH_LONG).show()
                }
            }
        }

    private fun handlePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener {  billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                }
            }
        billingClient!!.consumeAsync(consumeParams, listener)
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                     if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                         sessionManagement.setSubscriptionId(purchase.orderId.toString())
                         sessionManagement.setPurchaseToken(purchase.purchaseToken)
                         sessionManagement.setPlanType(planType)

                         Log.d("****", "subscription_id ${purchase.orderId}")
                         Log.d("**** ", "subscription_PurchaseToken ${purchase.purchaseToken}")
                         Log.d("****", "planType $planType")

                         requireActivity().runOnUiThread(Runnable {
                             callingPurchaseSubscriptionApi(purchase.orderId, purchase.purchaseToken)
                         })
                     }
                }
            } else {
                Toast.makeText(requireActivity(), "Already Subscribed", Toast.LENGTH_LONG).show()
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {

            Toast.makeText(requireActivity(), "Subscription Pending", Toast.LENGTH_LONG).show()
        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {

            Toast.makeText(requireActivity(), "UNSPECIFIED_STATE", Toast.LENGTH_LONG).show()
        }
    }

    private fun callingPurchaseSubscriptionApi(orderId: String?, purchaseToken: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.subscriptionGoogle( {
                BaseApplication.dismissMe()
                handleApiResponse(it,"purchase")
            }, planType,purchaseToken,orderId)
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>,type:String) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString(),type)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String,type:String) {
        try {
            val apiModel = Gson().fromJson(data, HomeApiResponse::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (type.equals("purchase",true)){
                    sessionManagement.setSubscriptionId("")
                    sessionManagement.setPurchaseToken("")
                    binding.rlNextBtn.isClickable=false
                    binding.rlNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                    Toast.makeText(requireContext(),apiModel.message,Toast.LENGTH_SHORT).show()
                    if (screen.equals("login",true)){
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }else{
                    when(apiModel.data?.active_plan){
                        AppConstant.Premium_Monthly ->  selectPlan(
                            binding.relPopularPlan,
                            binding.tvPopular,
                            binding.imgPopularClick,
                            binding.imagePreviousMonthly,
                            binding.tvProkaiUser,
                            binding.tvNewDollarMonthly,
                            listOf(binding.relSubscriptionBasic, binding.relBestPlan),
                            listOf(binding.tvStarter, binding.tvBest),
                            listOf(binding.imagePrevious, binding.imagePreviousyearly),
                            listOf(binding.imgBasicClick, binding.imgBaseClick),
                            listOf(binding.tvNewKai, binding.tvLovekaiUser),
                            listOf(binding.tvNewDollar, binding.tvNewDollaryearly),AppConstant.Premium_Monthly,"Popular"
                        )
                        AppConstant.Premium_Annual -> selectPlan(
                            binding.relBestPlan,
                            binding.tvBest,
                            binding.imgBaseClick,
                            binding.imagePreviousyearly,
                            binding.tvLovekaiUser,
                            binding.tvNewDollaryearly,
                            listOf(binding.relSubscriptionBasic, binding.relPopularPlan),
                            listOf(binding.tvPopular, binding.tvStarter),
                            listOf(binding.imagePrevious, binding.imagePreviousMonthly),
                            listOf(binding.imgBasicClick, binding.imgPopularClick),
                            listOf(binding.tvNewKai, binding.tvProkaiUser),
                            listOf(binding.tvNewDollar, binding.tvNewDollarMonthly),AppConstant.Premium_Annual,"Best")


                            AppConstant.Premium_Weekly ->  selectPlan(
                            binding.relSubscriptionBasic,
                            binding.tvStarter,
                            binding.imgBasicClick,
                            binding.imagePrevious,
                            binding.tvNewKai,
                            binding.tvNewDollar,
                            listOf(binding.relPopularPlan, binding.relBestPlan),
                            listOf(binding.tvPopular, binding.tvBest),
                            listOf(binding.imagePreviousMonthly, binding.imagePreviousyearly),
                            listOf(binding.imgPopularClick, binding.imgBaseClick),
                            listOf(binding.tvProkaiUser, binding.tvLovekaiUser),
                            listOf(binding.tvNewDollarMonthly, binding.tvNewDollaryearly),AppConstant.Premium_Weekly,"Starter")
                        else -> {
                            selectPlan(
                                binding.relSubscriptionBasic,
                                binding.tvStarter,
                                binding.imgBasicClick,
                                binding.imagePrevious,
                                binding.tvNewKai,
                                binding.tvNewDollar,
                                listOf(binding.relPopularPlan, binding.relBestPlan),
                                listOf(binding.tvPopular, binding.tvBest),
                                listOf(binding.imagePreviousMonthly, binding.imagePreviousyearly),
                                listOf(binding.imgPopularClick, binding.imgBaseClick),
                                listOf(binding.tvProkaiUser, binding.tvLovekaiUser),
                                listOf(binding.tvNewDollarMonthly, binding.tvNewDollaryearly),AppConstant.Premium_Weekly,"Starter")
                        }
                    }
                    if (apiModel.data?.Subscription_status==1){
                        binding.rlNextBtn.isClickable=true
                        binding.rlNextBtn.setBackgroundResource(R.drawable.green_btn_background)
                    }else{
                        binding.rlNextBtn.isClickable=false
                        binding.rlNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                    }
                    if (screen.equals("login",true)){
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
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
        alertError(requireContext(), message, status)
    }

    private fun getPrices() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val executorService = Executors.newSingleThreadExecutor()
                    executorService.execute {
                        val ids = mutableListOf(AppConstant.Premium_Monthly, AppConstant.Premium_Annual, AppConstant.Premium_Weekly) // your product IDs
                        val productList = ids.stream().map { productId: String? ->
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(productId!!)
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            }.collect(Collectors.toList())
                        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(productList).build()


                        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult1, productDetailsList ->
                            for (productDetails in productDetailsList) {
                                Log.d("******", productDetails.productId)
                                productDetails.subscriptionOfferDetails?.let { offerDetailsList ->
                                    for (subscriptionOfferDetails in offerDetailsList) {
                                        val formattedPrice = subscriptionOfferDetails.pricingPhases
                                            .pricingPhaseList
                                            .firstOrNull()
                                            ?.formattedPrice

                                        when (productDetails.productId) {
                                            AppConstant.Premium_Monthly -> premiumMonthly = "$formattedPrice / Monthly"
                                            AppConstant.Premium_Annual -> premiumAnnual = "$formattedPrice / Yearly"
                                            AppConstant.Premium_Weekly -> premiumWeekly = "$formattedPrice / Weekly"
                                        }
                                    }
                                }
                            }
                        }
                    }
                    requireActivity().runOnUiThread {
                        try {
                            Thread.sleep(1000)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        binding.tvNewDollar.text=premiumWeekly
                        binding.tvNewDollarMonthly.text=premiumMonthly
                        binding.tvNewDollaryearly.text=premiumAnnual
                        BaseApplication.dismissMe()
                    }
                }
            }
            override fun onBillingServiceDisconnected() {
                billingClient!!.startConnection(this)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        apiStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}