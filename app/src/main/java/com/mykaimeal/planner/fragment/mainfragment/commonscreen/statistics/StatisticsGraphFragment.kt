package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLinkResult
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.customview.SpendingChartView
import com.mykaimeal.planner.databinding.FragmentStatisticsGraphBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsGraphModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsGraphModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel.StatisticsViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class StatisticsGraphFragment : Fragment() {

    private lateinit var binding: FragmentStatisticsGraphBinding
    private lateinit var sessionManagement: SessionManagement
    private lateinit var statisticsViewModel: StatisticsViewModel
    private var referLink: String =""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStatisticsGraphBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        statisticsViewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]
        sessionManagement = SessionManagement(requireActivity())

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })


        // Create chart data
        val data = listOf(
            SpendingChartView.BarData(300f, "01", "Jun", Color.parseColor("#FFA500")),
            SpendingChartView.BarData(600f, "07", "Jun", Color.parseColor("#32CD32")),
            SpendingChartView.BarData(600f, "14", "Jun", Color.parseColor("#32CD32")),
            SpendingChartView.BarData(600f, "21", "Jun", Color.parseColor("#32CD32")),
            SpendingChartView.BarData(200f, "03", "Jun", Color.parseColor("#FF4040")))

        binding.spendingChart.setData(data, 1566f, 60f)

        initialize()

        return binding.root
    }

    private fun initialize() {
        Log.d("AppsFlyer22", "Deep link found, store")

        AppsFlyerLib.getInstance().subscribeForDeepLink { deepLinkResult ->
            when (deepLinkResult.status) {
                DeepLinkResult.Status.FOUND -> {
                    Log.d("AppsFlyer22", "Deep link found, store")

                    val deepLinkValue = deepLinkResult.deepLink?.getStringValue("deep_link_value")
                    if (deepLinkValue == "profile_screen") {
                        // Navigate to Profile Screen
                        Log.d("AppsFlyer22", "profile")

                    } else {
                        Log.d("AppsFlyer22", "Deep link statistics")

                        // Handle other deep link values
                    }
                }

                DeepLinkResult.Status.NOT_FOUND -> {
                    Log.d("AppsFlyer22", "Deep link not found, redirecting to Play Store")
                    redirectToPlayStore()
                }

                DeepLinkResult.Status.ERROR -> {
                    Log.d("AppsFlyer22", "Error in deep link: ${deepLinkResult.error}")
                }
            }
        }

        binding.imgBackStats.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.relInvite.setOnClickListener {
            findNavController().navigate(R.id.invitationsScreenFragment)
        }



        binding.textInviteFriends.setOnClickListener {

            shareImageWithText("Hey! I put together this cookbook in My" +
                    "Kai, and I think you’ll love it! It’s packed with delicious meals, check it out and let me know what you think!" +
                    "\nclick on the link below:\n\n",
                referLink)

//            dynamicValueUpdate()
            /* copyShareInviteLink()*/
        }

        binding.barChart.setOnClickListener {
            findNavController().navigate(R.id.statisticsWeekYearFragment)
        }

        copyShareInviteLink()

    /*    if (BaseApplication.isOnline(requireContext())) {
            getGraphList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }*/
    }

    private fun getGraphList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            statisticsViewModel.getGraphScreenUrl({
                BaseApplication.dismissMe()
                handleApiGraphResponse(it)
            },"4")
        }
    }

    private fun handleApiGraphResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessGraphResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessGraphResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, StatisticsGraphModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null) {
                    if (apiModel.data.graph_data!=null){
                        showSpendingChart(apiModel.data)
                    }
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

        @SuppressLint("SetTextI18n")
        private fun showSpendingChart(response: StatisticsGraphModelData) {



            // Create chart data
            val data = listOf(
                SpendingChartView.BarData(300f, "01", "Jun", Color.parseColor("#FFA500")),
                SpendingChartView.BarData(600f, "07", "Jun", Color.parseColor("#32CD32")),
                SpendingChartView.BarData(600f, "14", "Jun", Color.parseColor("#32CD32")),
                SpendingChartView.BarData(600f, "21", "Jun", Color.parseColor("#32CD32")),
                SpendingChartView.BarData(200f, "03", "Jun", Color.parseColor("#FF4040")))

            binding.spendingChart.setData(data, 1566f, 60f)

 /*           if (response.total_spent!=null){
                binding.textSpent.text="Total spent $"+response.total_spent.toString().trim()
            }

            if (response.saving!=null){
                binding.textSpent.text="Your savings are $"+response.saving.toString().trim()
            }

            val weekLabels = listOf("01", "07", "14", "21")
            val fullLabels = weekLabels.map { "$it ${response.month}" }

            val weeklyAmounts = listOf(
                response.graph_data.week_1,
                response.graph_data.week_2,
                response.graph_data.week_3,
                response.graph_data.week_4
            )

            val colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.orange),
                ContextCompat.getColor(requireContext(), R.color.light_green),
                ContextCompat.getColor(requireContext(), R.color.red),
                ContextCompat.getColor(requireContext(), R.color.orange)
            )

            val barData = fullLabels.mapIndexed { index, label ->
                val parts = label.split(" ")
                val date = parts.getOrNull(0) ?: ""
                val month = parts.getOrNull(1) ?: ""
                SpendingChartView.BarData(
                    value = weeklyAmounts[index],
                    date = date,
                    month = month,
                    color = colors[index]
                )
            }

            binding.spendingChart.setData(barData, response.total_spent, response.saving)*/
        }


    private fun redirectToPlayStore() {
        val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.mykaimeal.planner")
        }
        startActivity(playStoreIntent)
    }

    private fun shareImageWithText(description: String,  link: String) {
        // Download image using Glide
        Glide.with(requireContext())
            .asBitmap() // Request a Bitmap image
            .load(R.mipmap.app_icon) // Provide the URL to load the image from
            .into(object : CustomTarget<Bitmap>() { override fun onResourceReady(resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    try {
                        // Save the image to a file in the app's external storage
                        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "shared_image.png"
                        )
                        val fos = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        fos.close()

                        // Create URI for the file using FileProvider
                        val uri: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().packageName + ".provider", // Make sure this matches your manifest provider
                            file
                        )

                        // Format the message with line breaks
                        val formattedText = """$description$link""".trimIndent()

                        // Create an intent to share the image and text
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, formattedText)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        // Launch the share dialog
                        requireContext().startActivity(Intent.createChooser(shareIntent, "Share Image"))

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("ImageShareError", "onResourceReady: ${e.message}")
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Optional: Handle if the image load is cleared or cancelled
                }
            })
    }

    @SuppressLint("RestrictedApi")
    private fun copyShareInviteLink() {

        val afUserId = sessionManagement.getId()?.toString().orEmpty()
        val referrerCode = sessionManagement.getReferralCode()?.toString().orEmpty()
        val providerName = sessionManagement.getUserName()?.toString().orEmpty()
        val providerImage = sessionManagement.getImage()?.toString().orEmpty()

        val baseUrl = "https://mykaimealplanner.onelink.me/mPqu/"

        val fullUrl = Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("af_user_id", afUserId)
            .appendQueryParameter("Referrer", referrerCode)
            .appendQueryParameter("providerName", providerName)
            .appendQueryParameter("providerImage", providerImage)
            .build()
            .toString()

        referLink = fullUrl
        Log.d("AF_TEST", "Custom Raw Link: $referLink")


        /*    val currentCampaign = "user_invite"
            val currentChannel = "mobile_share"

            val afUserId = sessionManagement.getId()?.toString().orEmpty()
            val referrerCode = sessionManagement.getReferralCode()?.toString().orEmpty()
            val providerName = sessionManagement.getUserName()?.toString().orEmpty()
            val providerImage = sessionManagement.getImage()?.toString().orEmpty()

            val linkGenerator = ShareInviteHelper.generateInviteUrl(requireActivity())
                .setBaseDeeplink("https://mykaimealplanner.onelink.me/mPqu")
                .setCampaign(currentCampaign)
                .setChannel(currentChannel)
                .addParameter("af_user_id", afUserId)
                .addParameter("referrer", referrerCode)
                .addParameter("providerName", providerName)
                .addParameter("providerImage", providerImage)

            Log.d("AF_TEST", "Params: ${linkGenerator.userParams}")

            linkGenerator.generateLink(requireActivity(), object : LinkGenerator.ResponseListener {
                override fun onResponse(s: String) {
                    referLink = s
                    Log.d("AF_TEST", "Generated Link: $s")
                }

                override fun onResponseError(s: String) {
                    Log.e("AF_TEST", "Error Generating Link: $s")
                }
            })*/

    }
}