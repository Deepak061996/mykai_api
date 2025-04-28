 package com.mykaimeal.planner.commonworkutils

import com.appsflyer.share.ShareInviteHelper
import com.appsflyer.share.LinkGenerator
import com.appsflyer.AppsFlyerLib
import android.content.Intent
import android.util.Log
import android.content.Context


fun shareApp(context: Context) {

    val appsFlyerUID = AppsFlyerLib.getInstance().getAppsFlyerUID(context)

    // Create a LinkGenerator for generating OneLink URL
    val linkGenerator = ShareInviteHelper.generateInviteUrl(context)

    // Configure the link with required parameters
    linkGenerator.channel = "social_media" // E.g., WhatsApp, SMS
    linkGenerator.campaign = "user_invite_campaign"
    linkGenerator.setReferrerUID(appsFlyerUID)
    linkGenerator.addParameter("deep_link_value", "example_screen") // Add custom deep link data

    linkGenerator.generateLink(context, object : LinkGenerator.ResponseListener {
        override fun onResponse(response: String) {
            Log.d("AppsFlyer", "Generated Link: $response")

            // Share the generated link via an intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out this app: $response")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share App"))
        }

        override fun onResponseError(error: String?) {
            Log.e("AppsFlyer", "Failed to generate the link. Error: $error")
        }

    })
}



