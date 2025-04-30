package com.mykaimeal.planner.fragment.mainfragment.profilesetting.subscriptionplan

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterOnBoardingSubscriptionItem
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
import com.google.gson.Gson
import com.mykaimeal.planner.basedata.AppConstant
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.alertError
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentSubscriptionPlanOverViewBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.subscriptionplan.viewmodel.SubscriptionPlanViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.HomeApiResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.OnSubscriptionModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.stream.Collectors


@AndroidEntryPoint
class SubscriptionPlanOverViewFragment : Fragment() {
    private lateinit var binding: FragmentSubscriptionPlanOverViewBinding
    private lateinit var slideUp: Animation
    var datalist: ArrayList<OnSubscriptionModel> = arrayListOf()
    private lateinit var viewModel: SubscriptionPlanViewModel
    private var adapters: AdapterOnBoardingSubscriptionItem? = null
    private var lastPlan:String=""
    private var screen:String=""
    private var currentIndex:Int=0
    private var billingClient: BillingClient? = null
    private lateinit var sessionManagement: SessionManagement

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSubscriptionPlanOverViewBinding.inflate(layoutInflater, container, false)

        screen= arguments?.getString("screen","")?:""


        if (!screen.equals("login",true)){
            (activity as? MainActivity)?.binding?.apply {
                llIndicator.visibility = View.GONE
                llBottomNavigation.visibility = View.GONE
            }
        }
        viewModel = ViewModelProvider(requireActivity())[SubscriptionPlanViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    movToScreen()
                }
            })



        binding.crossImages.visibility = View.INVISIBLE // Initially hide the ImageView

        Handler(Looper.getMainLooper()).postDelayed({
            binding.crossImages.visibility = View.VISIBLE // Show the ImageView after 5 seconds
        }, 5000)

        startBillingApi()

        initialize()

        return binding.root
    }

    private fun startBillingApi() {
        billingClient = BillingClient.newBuilder(requireActivity())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        getPrices()

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
                                    }
                                }
                            }
                        }
                    }
                }
            }
            override fun onBillingServiceDisconnected() {
                billingClient!!.startConnection(this)
            }
        })
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
                    handlePurchase(purchase)
                }
            }
        } else {
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
                        when (lastPlan) {
                            AppConstant.Premium_Monthly -> sessionManagement.setPlanType("Popular")
                            AppConstant.Premium_Annual -> sessionManagement.setPlanType("Best")
                            AppConstant.Premium_Weekly -> sessionManagement.setPlanType("Starter")
                        }
                        Log.d("****", "subscription_id ${purchase.orderId}")
                        Log.d("**** ", "subscription_PurchaseToken ${purchase.purchaseToken}")
                        Log.d("****", "planType $lastPlan")

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
            }, lastPlan,purchaseToken,orderId)
        }
    }


        private fun setUpOnBoardingIndicator() {
            binding.layOnboardingIndicator.removeAllViews() // Clear previous indicators

            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(8, 0, 8, 0)

            for (i in datalist.indices) {
                val img = ImageView(requireContext())
                img.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.subs_indicator_inactive)
                )
                img.layoutParams = layoutParams
                binding.layOnboardingIndicator.addView(img)
            }
        }


     private fun currentOnBoardingIndicator(index: Int) {
         currentIndex = index // Save index
         val childCount = binding.layOnboardingIndicator.childCount
         for (i in 0 until childCount) {
             val imageView = binding.layOnboardingIndicator.getChildAt(i) as ImageView
             val drawableRes = if (i == currentIndex ) {
                 R.drawable.subs_indicator_active
             } else {
                 R.drawable.subs_indicator_inactive
             }
             imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), drawableRes))
         }
     }


    private fun initialize() {

        binding.rlNextBtn.setOnClickListener {
            if (binding.viewpager.currentItem < adapters!!.itemCount - 1) {
                // Move to the next item
                binding.viewpager.currentItem += 1
            } else {
                val bundle = Bundle()
                bundle.putString("screen",screen)
                findNavController().navigate(R.id.homeSubscriptionAllPlanFragment,bundle)
            }
        }

        binding.crossImages.setOnClickListener {
            movToScreen()
        }

        binding.imageBackIcon.setOnClickListener {
            movToScreen()
        }

        datalist.clear()

        // List of onboarding items
        datalist.add(OnSubscriptionModel(R.drawable.image_1, true))

        datalist.add(OnSubscriptionModel(R.drawable.image_2, false))

        datalist.add(OnSubscriptionModel(R.drawable.image_3, false))

        datalist.add(OnSubscriptionModel(R.drawable.image_4, false))

        datalist.add(OnSubscriptionModel(R.drawable.image_5, false))

        adapters = AdapterOnBoardingSubscriptionItem(requireContext(),datalist,sessionManagement.getProviderName(),sessionManagement.getProviderImage())
        binding.viewpager.adapter = adapters
        binding.viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        /// set indicator for onboarding
        setUpOnBoardingIndicator()
        /// set current indicator position
        currentOnBoardingIndicator(currentIndex)

        viewPagerFunctionImpl()

        binding.textSeeAllButton.setOnClickListener {
            binding.textSeeAllButton.visibility = View.GONE

            binding.rlBottom.visibility = View.VISIBLE

            slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_anim)
            slideUp.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    binding.llSubPlans.clearAnimation()
                    binding.llSubPlans.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            binding.llSubPlans.startAnimation(slideUp)
        }

        binding.tvRestorePurchase.setOnClickListener {
            if (isOnline(requireContext())) {
                planPurchases()
            } else {
                alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun movToScreen(){
        if (screen.equals("login",true)){
            findNavController().navigate(R.id.homeSubscriptionAllPlanFragment)
        }else{
            findNavController().navigateUp()
        }
    }

   /* override fun onResume() {
        super.onResume()
        currentOnBoardingIndicator(4) // keep track of this index globally
    }*/

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
                                    .setProductId(lastPlan)
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

    private fun viewPagerFunctionImpl() {

        /// set view pager position and value
        binding.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.tvHeadingTitle.text = "Save Time, Save Money,\n Eat Better"
                        binding.tvDescriptions.text = "Kai plans your meals, Compares store prices, And creates your cart so you don’t have to."
                        binding.tvDescriptions2.text = ""
                        binding.relDescriptions2.visibility = View.INVISIBLE
                    }
                    1 -> {
                        binding.tvHeadingTitle.text = "Endless Meals to Explore"
                        binding.tvDescriptions.text = "Kai gives you access to over 80,000 recipes"
                        binding.tvDescriptions2.text = ""
                        binding.relDescriptions2.visibility = View.INVISIBLE
                    }
                    2 -> {
                        binding.relDescriptions2.visibility = View.VISIBLE
                        binding.tvHeadingTitle.text = "Eat Smart, Every Day"
                        binding.tvDescriptions.text =
                            "Kai helps you plan your week with recipes tailored to your preferences"
                        binding.tvDescriptions2.text =
                            "Stay on top of your nutrition with Kai’s daily nutrition tracker"

                    }
                    3 -> {
                        binding.relDescriptions2.visibility = View.VISIBLE
                        binding.tvHeadingTitle.text = "Maximum Savings, Zero Hassle"
                        binding.tvDescriptions.text =
                            "One tap, and all your weekly ingredients are in your cart"
                        binding.tvDescriptions2.text =
                            "Compare grocery prices at nearby stores & have them delivered  right to your door"
                    }
                    else -> {
                        binding.relDescriptions2.visibility = View.VISIBLE
                        binding.tvHeadingTitle.text = "Show Me the Money!"
                        binding.tvDescriptions.text =
                            "Users save an average of \$64 a week that’s an amazing \$256* a month!"
                        binding.tvDescriptions2.text =
                            "With Kai, smart choices aren't just smart. They’re money in the bank"
                    }
                }

                currentOnBoardingIndicator(position)
            }
        })

    }


    override fun onStart() {
        super.onStart()
        apiStatus()
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

    private fun handleApiResponse(result: NetworkResult<String>, type:String) {
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
                    binding.tvRestorePurchase.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(),apiModel.message,Toast.LENGTH_SHORT).show()
                }else{
                    if (apiModel.data?.last_plan.equals("",true)){
                        binding.tvRestorePurchase.visibility = View.INVISIBLE
                    }else{
                        lastPlan=apiModel.data?.last_plan.toString()
                        binding.tvRestorePurchase.visibility = View.VISIBLE
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

}