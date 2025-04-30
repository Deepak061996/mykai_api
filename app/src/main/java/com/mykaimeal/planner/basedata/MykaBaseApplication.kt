package com.mykaimeal.planner.basedata

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLinkResult
import com.google.firebase.FirebaseApp
import com.mykaimeal.planner.commonworkutils.AppsFlyerConstants
import dagger.hilt.android.HiltAndroidApp
import java.io.File


@HiltAndroidApp
class MykaBaseApplication : Application() {

    companion object {
        @Volatile
        var instance: MykaBaseApplication? = null
        fun getAppContext(): Context {
            return instance?.applicationContext
                ?: throw IllegalStateException("Application instance is null")
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        registerReceiver(NetworkChangeReceiver(), filter)
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()

        AppsFlyerConstants.afDevKey

        val afDevKey = "M57zyjkFgb7nSQwHWN6isW"

        val conversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                Log.d("AppsFlyerssssssss", "Conversion success: $data")
                // Store in a singleton or SharedPreferences for later use
            }

            override fun onConversionDataFail(error: String?) {
                Log.e("AppsFlyerssssssss", "Conversion error: $error")
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {}
            override fun onAttributionFailure(error: String?) {}
        }

        AppsFlyerLib.getInstance().init(afDevKey, conversionListener, applicationContext)
        AppsFlyerLib.getInstance().start(applicationContext)



/*        val afDevKey: String = AppsFlyerConstants.afDevKey
        Log.d("AppsFlyerSDKVerMYKAI", "Referral:")

        val appsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                if (data != null) {
                    val referrer = data["Referrer"]?.toString()
                    val screenName = data["ScreenName"]?.toString()
                    val cookbooksId = data["CookbooksID"]?.toString()
                    val itemName = data["ItemName"]?.toString()
                    val providerName = data["providerName"]?.toString()
                    val providerImage = data["providerImage"]?.toString()

                    Log.d("AppsFlyerSDKVerMYKAI", "Referral: $referrer, Screen: $screenName")

                    if (!referrer.isNullOrEmpty()) {
      *//*                  sessionManagement.setReferralCode(referrer)
                        sessionManagement.setCookBookId(cookbooksId ?: "")
                        sessionManagement.setCookBookName(itemName ?: "")
                        sessionManagement.setProviderName(providerName ?: "")
                        sessionManagement.setProviderImage(providerImage ?: "")*//*

                        Log.d("AppsFlyerSDKVerMYKAI","******:----"+referrer+"----"+providerName+"----"+"---"+providerImage)

                        // You can navigate or show something here if needed
                    }
                }
            }

            override fun onConversionDataFail(error: String?) {
                Log.e("AppsFlyer", "Error getting conversion data: $error")
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {}
            override fun onAttributionFailure(error: String?) {}
        }

        AppsFlyerLib.getInstance().init(afDevKey, appsFlyerConversionListener, this)
        AppsFlyerLib.getInstance().start(this)*/
    }
}
