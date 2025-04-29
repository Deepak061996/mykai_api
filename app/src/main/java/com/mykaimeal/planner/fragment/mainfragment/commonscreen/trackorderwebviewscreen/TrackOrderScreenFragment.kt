package com.mykaimeal.planner.fragment.mainfragment.commonscreen.trackorderwebviewscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.databinding.FragmentTrackOrderScreenBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackOrderScreenFragment : Fragment() {
    private lateinit var binding: FragmentTrackOrderScreenBinding
    private var trackStatus:String=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTrackOrderScreenBinding.inflate(layoutInflater, container, false)


        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        trackStatus = arguments?.getString("tracking", "")?:""

        setupBackNavigation()
        initialize()

        return binding.root
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bundle = Bundle().apply {
                    putString("screen","yes")
                }
                findNavController().navigate(R.id.orderHistoryFragment,bundle)
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initialize() {

        binding.imgTrackOrder.setOnClickListener{
            val bundle = Bundle().apply {
                putString("screen","yes")
            }
            findNavController().navigate(R.id.orderHistoryFragment,bundle)
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
                Toast.makeText(requireContext(), "Clicked URL: $url", Toast.LENGTH_SHORT).show()
                Log.d("Clicked URL:", "***$url")
                // Decide whether to load the URL in the WebView
                view.loadUrl(url) // Load the URL in the WebView
                return true // Return true if you handle the URL loading
            }
        }
        Log.d("url", "****$trackStatus")
        binding.webView.loadUrl(trackStatus)
    }

}