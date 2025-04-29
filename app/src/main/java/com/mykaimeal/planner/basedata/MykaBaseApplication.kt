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

        val afDevKey: String = AppsFlyerConstants.afDevKey

        val conversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: Map<String, Any>) {
                Log.d("AF_CONVERSION", "Conversion data: $data")
            }

            override fun onConversionDataFail(error: String) {
                Log.e("AF_CONVERSION", "Error: $error")
            }

            override fun onAppOpenAttribution(data: Map<String, String>) {
                Log.d("AF_ATTRIBUTION", "App Open Attribution: $data")
                // Handle direct deep link click data if needed
            }

            override fun onAttributionFailure(error: String) {
                Log.e("AF_ATTRIBUTION", "Attribution failure: $error")
            }
        }

        AppsFlyerLib.getInstance().init(afDevKey, conversionListener, applicationContext)
        AppsFlyerLib.getInstance().start(this)
    }
}
