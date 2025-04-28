package com.mykaimeal.planner.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.ActivityIntroPageBinding
import com.mykaimeal.planner.databinding.FragmentWebViewByUrlBinding

class WebViewByUrlActivity : AppCompatActivity() {

    private lateinit var binding: FragmentWebViewByUrlBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use `inflate` method directly without creating an extra object
        binding = FragmentWebViewByUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var url = intent.getStringExtra("url") ?: ""


        binding.relBack.setOnClickListener{
            val resultIntent = Intent()
            resultIntent.putExtra("submitted_result", "close")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadsImagesAutomatically = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccess = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Set a WebViewClient to capture URL clicks
        binding.webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, urlNew: String): Boolean {
                url=urlNew
                // Capture the clicked URL
//                Toast.makeText(requireContext(), "Clicked URL: $url", Toast.LENGTH_SHORT).show()
                 Log.d("Clicked URL:", "***$urlNew")
                // Decide whether to load the URL in the WebView
                view.loadUrl(urlNew) // Load the URL in the WebView
                return true // Return true if you handle the URL loading
            }
        }
        Log.d("url", "****$url")
        binding.webView.loadUrl(url)

        binding.rlImportApp.setOnClickListener{
            val resultIntent = Intent()
            resultIntent.putExtra("submitted_result", url)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        val resultIntent = Intent()
        resultIntent.putExtra("submitted_result", "close")
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
