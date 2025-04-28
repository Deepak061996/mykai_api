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

    var deferred_deep_link_processed_flag = false
    var conversionData: Map<String, Any>? = null


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

        // Initialize AppsFlyer
        AppsFlyerLib.getInstance().init(afDevKey, object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(data: Map<String, Any>) {
                    Log.d("AppsFlyer", "Conversion Data: $data")
                }

                override fun onConversionDataFail(error: String) {
                    Log.e("AppsFlyer", "Conversion Data Error: $error")
                }

                override fun onAppOpenAttribution(data: Map<String, String>) {
                    Log.d("AppsFlyer", "App Open Attribution: $data")
                }

                override fun onAttributionFailure(error: String) {
                    Log.e("AppsFlyer", "Attribution Error: $error")
                }
            }, this)

        // Enable debugging during development (disable in production)
        AppsFlyerLib.getInstance().setDebugLog(true)

        // Start AppsFlyer
        AppsFlyerLib.getInstance().start(this)
        AppsFlyerLib.getInstance().setAppInviteOneLink("LQhk")

        // Listen for deep links
        AppsFlyerLib.getInstance().subscribeForDeepLink { deepLinkResult ->
            when (deepLinkResult.status) {
                DeepLinkResult.Status.FOUND -> {
                    val deepLink = deepLinkResult.deepLink
                    Log.d("AppsFlyer", "Deep link: ${deepLink?.deepLinkValue}")
                }

                DeepLinkResult.Status.NOT_FOUND -> {
                    Log.d("AppsFlyer", "Deep link not found.")
                }

                DeepLinkResult.Status.ERROR -> {
                    Log.e("AppsFlyer", "Deep link error: ${deepLinkResult.error}")
                }
            }
        }
    }


    /* val afDevKey: String = AppsFlyerConstants.afDevKey
     // Make sure you remove the following line when building to production
     AppsFlyerLib.getInstance().setDebugLog(true)
     AppsFlyerLib.getInstance().setMinTimeBetweenSessions(0)
     //set the OneLink template id for share invite links
     AppsFlyerLib.getInstance().setAppInviteOneLink("LQhk")



     val afRevenueBuilder: AppsFlyerAdRevenue.Builder = AppsFlyerAdRevenue.Builder(this)
     AppsFlyerAdRevenue.initialize(afRevenueBuilder.build())

     AppsFlyerLib.getInstance().subscribeForDeepLink(DeepLinkListener { deepLinkResult ->
         val dlStatus = deepLinkResult.status
         if (dlStatus == DeepLinkResult.Status.FOUND) {
             Log.d(LOG_TAG,"Deep link found"
             )
         } else if (dlStatus == DeepLinkResult.Status.NOT_FOUND) {
             Log.d(LOG_TAG, "Deep link not found")
             return@DeepLinkListener
         } else {
             // dlStatus == DeepLinkResult.Status.ERROR
             val dlError = deepLinkResult.error
             Log.d(LOG_TAG, "There was an error getting Deep Link data: $dlError")
             return@DeepLinkListener
         }
         val deepLinkObj = deepLinkResult.deepLink
         try {
             Log.d(LOG_TAG, "The DeepLink data is: $deepLinkObj")
         } catch (e: Exception) {
             Log.d(LOG_TAG, "DeepLink data came back null")
             return@DeepLinkListener
         }
         // An example for using is_deferred
         if (deepLinkObj.isDeferred!!) {
             Log.d(LOG_TAG, "This is a deferred deep link")
             if (deferred_deep_link_processed_flag == true) {
                 Log.d(LOG_TAG,
                     "Deferred deep link was already processed by GCD. This iteration can be skipped.")
                 deferred_deep_link_processed_flag = false
                 return@DeepLinkListener
             }
         } else {
             Log.d(LOG_TAG,
                 "This is a direct deep link"
             )
         }
         // An example for getting deep_link_value
         var fruitName: String? = ""
         try {
             fruitName = deepLinkObj.deepLinkValue
             var referrerId: String? = ""
             val dlData = deepLinkObj.clickEvent

             // * Next if statement is optional *
             // Our sample app's user-invite carries the referrerID in deep_link_sub2
             // See the user-invite section in FruitActivity.java
             if (dlData.has("deep_link_sub2")) {
                 referrerId = deepLinkObj.getStringValue("deep_link_sub2")
                 Log.d(LOG_TAG, "The referrerID is: $referrerId"
                 )
             } else {
                 Log.d(LOG_TAG, "deep_link_sub2/Referrer ID not found")
             }
             if (fruitName == null || fruitName == "") {
                 Log.d(LOG_TAG,
                     "deep_link_value returned null"
                 )
                 fruitName = deepLinkObj.getStringValue("fruit_name")
                 if (fruitName == null || fruitName == "") {
                     Log.d(LOG_TAG, "could not find fruit name")
                     return@DeepLinkListener
                 }
                 Log.d(LOG_TAG,
                     "fruit_name is $fruitName. This is an old link"
                 )
             }
             Log.d(LOG_TAG,
                 "The DeepLink will route to: $fruitName"
             )
             // This marks to GCD that UDL already processed this deep link.
             // It is marked to both DL and DDL, but GCD is relevant only for DDL
             deferred_deep_link_processed_flag = true
         } catch (e: Exception) {
             Log.d(LOG_TAG, "There's been an error: $e"
             )
             return@DeepLinkListener
         }
         *//*goToFruit(fruitName, deepLinkObj)*//*
        })

        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionDataMap: MutableMap<String, Any>) {
                for (attrName in conversionDataMap.keys) Log.d(LOG_TAG,
                    "Conversion attribute: " + attrName + " = " + conversionDataMap[attrName]
                )
                val status = Objects.requireNonNull(conversionDataMap["af_status"]).toString()
                if (status == "Non-organic") {
                    if (Objects.requireNonNull<Any?>(conversionDataMap["is_first_launch"])
                            .toString() == "true"
                    ) {
                        Log.d(LOG_TAG,
                            "Conversion: First Launch")
                        //Deferred deep link in case of a legacy link
                        if (deferred_deep_link_processed_flag) {
                            Log.d(LOG_TAG,
                                "Deferred deep link was already processed by UDL. The DDL processing in GCD can be skipped."
                            )
                            deferred_deep_link_processed_flag = false
                        } else {
                            deferred_deep_link_processed_flag = true
                            if (conversionDataMap.containsKey("fruit_name")) {
                                conversionDataMap["deep_link_value"] =
                                    conversionDataMap["fruit_name"]!!
                            }
                            val fruitNameStr = conversionDataMap["deep_link_value"] as String?
                            val deepLinkData: DeepLink = mapToDeepLinkObject(conversionDataMap)!!
                            *//*goToFruit(fruitNameStr, deepLinkData)*//*
                        }
                    } else {
                        Log.d(LOG_TAG,
                            "Conversion: Not First Launch"
                        )
                    }
                } else {
                    Log.d(LOG_TAG,
                        "Conversion: This is an organic install."
                    )
                }
                conversionData = conversionDataMap
            }

            override fun onConversionDataFail(errorMessage: String) {
                Log.d(LOG_TAG,
                    "error getting conversion data: $errorMessage"
                )
            }

            override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                Log.d(LOG_TAG,
                    "onAppOpenAttribution: This is fake call."
                )
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d(LOG_TAG,
                    "error onAttributionFailure : $errorMessage"
                )
            }
        }
        AppsFlyerLib.getInstance().init(afDevKey, conversionListener, this)
        AppsFlyerLib.getInstance().start(this)
*/
}
