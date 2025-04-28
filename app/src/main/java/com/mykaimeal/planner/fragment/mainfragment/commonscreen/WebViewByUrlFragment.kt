package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.databinding.FragmentWebViewByUrlBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WebViewByUrlFragment : Fragment() {
    private lateinit var binding: FragmentWebViewByUrlBinding
    private var url: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWebViewByUrlBinding.inflate(inflater, container, false)

        url = arguments?.getString("url", "")?:""

        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.GONE
            it.llBottomNavigation.visibility = View.GONE
        }


        initialize()

        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initialize() {

        binding.relBack.setOnClickListener{
            findNavController().navigateUp()
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
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // Capture the clicked URL
//                Toast.makeText(requireContext(), "Clicked URL: $url", Toast.LENGTH_SHORT).show()
                Log.d("Clicked URL:", "***$url")
                // Decide whether to load the URL in the WebView
                view.loadUrl(url) // Load the URL in the WebView
                return true // Return true if you handle the URL loading
            }
        }
        Log.d("url", "****$url")
        binding.webView.loadUrl(url)

        binding.rlImportApp.setOnClickListener{
            val bundle = Bundle().apply {
                putString("ClickedUrl",url)
            }
            findNavController().navigate(R.id.searchFragmentDummy,bundle)
        }

    }
}