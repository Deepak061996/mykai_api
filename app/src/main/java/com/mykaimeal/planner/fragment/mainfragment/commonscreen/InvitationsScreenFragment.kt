package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.appsflyer.share.LinkGenerator
import com.appsflyer.share.ShareInviteHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterInviteItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentInvitationsScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.ReferralInvitationModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.ReferralInvitationModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel.StatisticsViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class InvitationsScreenFragment : Fragment() {

    private lateinit var binding: FragmentInvitationsScreenBinding
    private var adapterInviteItem: AdapterInviteItem? = null
    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var sessionManagement: SessionManagement

    private var referLink: String = ""
    private var referralList: MutableList<ReferralInvitationModelData> =mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentInvitationsScreenBinding.inflate(layoutInflater, container, false)

        statisticsViewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]
        sessionManagement = SessionManagement(requireActivity())

        backButton()

        adapterInviteItem = AdapterInviteItem(referralList, requireActivity())
        binding.rcyFriendsInvite.adapter = adapterInviteItem

        initialize()

        return binding.root
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun initialize() {

        binding.imgBackInvite.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.spinnerFilterType.setIsFocusable(true)

        binding.spinnerFilterType.setItems(
            listOf("All", "Trial", "Trial over", "Myka", "Redeemed")
        )

        binding.spinnerFilterType.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            filterData(newItem) // Call your filter function with the selected value
        }

        if (BaseApplication.isOnline(requireContext())) {
            getInvitationList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }


        binding.textInviteFriends.setOnClickListener{

            shareImageWithText(
                "Hey! I put together this cookbook in My" +
                        "Kai, and I think you’ll love it! It’s packed with delicious meals, check it out and let me know what you think!" +
                        "\nclick on the link below:\n\n",
                referLink
            )
        }

        generateDeepLink()


        /*  binding.textInviteFriends.setOnClickListener {
              val appPackageName: String = requireActivity().packageName
              val myIntent = Intent(Intent.ACTION_SEND)
              myIntent.type = "text/plain"
              val body =
                  "https://play.google.com/store/apps/details?id=$appPackageName"
              val sub = "Your Subject"
              myIntent.putExtra(Intent.EXTRA_SUBJECT, sub)
              myIntent.putExtra(Intent.EXTRA_TEXT, body)
              startActivity(Intent.createChooser(myIntent, "Share Using"))
          }*/
    }

    private fun shareImageWithText(description: String, link: String) {
        // Download image using Glide
        Glide.with(requireContext())
            .asBitmap() // Request a Bitmap image
            .load(R.mipmap.app_icon) // Provide the URL to load the image from
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    try {
                        // Save the image to a file in the app's external storage
                        val file = File(
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "shared_image.png"
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
                        requireContext().startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share Image"
                            )
                        )

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

    private fun generateDeepLink() {

        val afUserId = sessionManagement.getId()?.toString().orEmpty()
        val referrerCode = sessionManagement.getReferralCode()?.toString().orEmpty()
        val providerName = sessionManagement.getUserName()?.toString().orEmpty()
        val providerImage = sessionManagement.getImage()?.toString().orEmpty()

        // Your OneLink base URL and campaign details
        val currentCampaign = "property_share"
        val oneLinkId = "mPqu" // Replace with your OneLink ID
        val brandDomain = "mykaimealplanner.onelink.me" // Your OneLink domain

        // Prepare the deep link values
        val deepLink = "mykai://property?af_user_id=$afUserId&Referrer=$referrerCode&providerName=$providerName&providerImage=$providerImage"

        //  val deepLink = "https://property?propertyId=$propertyId&propertyType=$propertyType&city=$city"
        val webLink = "https://https://admin.getmykai.com/" // Web fallback link
        // Create the link generator
        val linkGenerator = ShareInviteHelper.generateInviteUrl(requireActivity())
            .setBaseDeeplink("https://$brandDomain/$oneLinkId")
            .setCampaign(currentCampaign)
            .addParameter("af_dp", deepLink) // App deep link
            .addParameter("Referrer", referrerCode)
            .addParameter("providerName", providerName)
            .addParameter("providerImage", providerImage)
            .addParameter("af_web_dp", webLink) // Web fallback URL

        // Generate the link
        linkGenerator.generateLink(requireActivity(), object : LinkGenerator.ResponseListener {
            override fun onResponse(s: String) {
                // Successfully generated the link
                Log.d("TAG", s)
                // Example share message with the generated link
                val message = "Check out this property: $s"
                referLink = s
                Log.d("***********", s)
            }
            override fun onResponseError(s: String) {
                // Handle error if link generation fails
                Log.e("***********", "Error Generating Link: $s")
            }
        })
    }

    // Filter data using only `data` list
    private fun filterData(filter: String) {
        val filtered = if (filter.equals("All", ignoreCase = true)) {
            referralList // just show current list
        } else {
            referralList.filter { it.status?.trim().equals(filter, ignoreCase = true) }.toMutableList()
        }
        adapterInviteItem?.updateList(filtered)
    }

    private fun getInvitationList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            statisticsViewModel.referralUrl {
                BaseApplication.dismissMe()
                handleApiReferralResponse(it)
            }
        }
    }


    private fun handleApiReferralResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessReferralResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessReferralResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ReferralInvitationModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                apiModel.data?.let {
                    showInvitationList(it)
                }?:run {
                    invitedValue()
                }
            } else {
                invitedValue()
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }

            }
        } catch (e: Exception) {
            invitedValue()
            showAlert(e.message, false)
        }
    }

    private fun invitedValue(){
        val htmlText = "You have invited 0 friends to use<b> MyKai</b>"
        val formattedText = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvFriendsCountNumber.text = formattedText
    }

    private fun showInvitationList(data: MutableList<ReferralInvitationModelData>) {
        try {
            referralList.clear()
            data.let {
                referralList.addAll(it)
                if (referralList.size > 0) {
                    val invitedCount = referralList.size.toString()
                    val htmlText = "You have invited $invitedCount friends to use<b> MyKai</b>"
                    val formattedText = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    binding.tvFriendsCountNumber.text = formattedText
                    binding.rcyFriendsInvite.visibility = View.VISIBLE
                    adapterInviteItem?.updateList(referralList)
                } else {
                    invitedValue()
                    binding.rcyFriendsInvite.visibility = View.GONE
                }
            }
        }catch (e:Exception){
            invitedValue()
           Log.d("@Error ","*********"+e.message)
        }
    }

}