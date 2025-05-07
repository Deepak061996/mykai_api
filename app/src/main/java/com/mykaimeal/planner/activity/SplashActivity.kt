package com.mykaimeal.planner.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLinkResult
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.alertError
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.AppsFlyerConstants
import com.mykaimeal.planner.databinding.ActivitySplashBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.subscriptionplan.viewmodel.SubscriptionPlanViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.HomeApiResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManagement: SessionManagement
    private lateinit var viewModel: SubscriptionPlanViewModel


    companion object {
        public const val SPLASH_DELAY = 3000L // 3 seconds delay
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManagement = SessionManagement(this)
        // Initialize screen actions
        viewModel = ViewModelProvider(this)[SubscriptionPlanViewModel::class.java]

        Log.d("dfdfdfgd","fssss--444")

        if (sessionManagement.getSubscriptionId().toString().equals("",true)){
            initialize()
        }else{
            Log.d("****", "subscription_id ${sessionManagement.getSubscriptionId()}")
            Log.d("**** ", "subscription_PurchaseToken ${sessionManagement.getPurchaseToken()}")
            Log.d("****", "planType $sessionManagement.getPlanType()")

            BaseApplication.showMe(this)
            lifecycleScope.launch {
                viewModel.subscriptionGoogle( {
                    BaseApplication.dismissMe()
                    handleApiResponse(it)
                }, sessionManagement.getPlanType(),sessionManagement.getPurchaseToken(),sessionManagement.getSubscriptionId())
            }
        }
    }



    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, HomeApiResponse::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                sessionManagement.setSubscriptionId("")
                sessionManagement.setPurchaseToken("")
                initialize()
            } else {
                showAlert(apiModel.message, false)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        alertError(this, message, status)
    }

    private fun initialize() {
        handleDeepLink()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun handleDeepLink() {
        // 4. Handle deep link only now
        val data: Uri? = intent?.data
        Log.d("DeepLink", "Received URI: $data")
        if (intent?.action == Intent.ACTION_VIEW && data != null) {
            if (data.scheme == "mykai" || data.host == "property") {
                val screenName = data.getQueryParameter("ScreenName")
                val cookbooksId = data.getQueryParameter("CookbooksID")
                val itemName = data.getQueryParameter("ItemName")
                val referrer = data.getQueryParameter("Referrer")
                val providerName = data.getQueryParameter("providerName")
                val providerImage = data.getQueryParameter("providerImage")
                Log.d("***********MY kai", "$screenName  & $cookbooksId")
                if (screenName.equals("CookBooksType") && cookbooksId != null && referrer!=null) {
                    sessionManagement.setOpenCookBookUsingShare("CookBooksType")
                    sessionManagement.setCookBookId(cookbooksId.toString())
                    sessionManagement.setCookBookName(itemName.toString())
                    providerName?.let {
                        sessionManagement.setProviderName(it)
                    }
                    providerImage?.let {
                        sessionManagement.setProviderImage(it)
                    }
                    Log.d("***********MY kai", "******:----$referrer----$providerName-------$providerImage")
                    sessionManagement.setReferralCode(referrer.toString())
                }
            }
        }

        navigateNext()

    }


    private fun navigateNext() {
        lifecycleScope.launch {
            delay(SPLASH_DELAY)
            // Check login session and navigate accordingly
            if (sessionManagement.getFirstTime()){
                val intent = Intent(this@SplashActivity, IntroPageActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val targetActivity = if (sessionManagement.getLoginSession()) { MainActivity::class.java
                } else {
                    LetsStartOptionActivity::class.java
                }
                val intent = Intent(this@SplashActivity, targetActivity)
                startActivity(intent)
                finish()
            }
        }
    }
}
