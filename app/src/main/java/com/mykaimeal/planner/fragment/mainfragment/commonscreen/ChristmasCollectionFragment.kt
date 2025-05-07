package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.appsflyer.share.LinkGenerator
import com.appsflyer.share.ShareInviteHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterCookBookDetailsItem
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentChristmasCollectionBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.CookBookViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.UserCookBokModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse.CookBookDataModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse.CookBookListApiResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import com.mykaimeal.planner.model.DateModel
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ChristmasCollectionFragment : Fragment(), OnItemClickListener {

    private lateinit var binding: FragmentChristmasCollectionBinding
    private var adapterCookBookDetailsItem: AdapterCookBookDetailsItem? = null
    private var tvWeekRange: TextView? = null
    private var rcyChooseDaySch: RecyclerView? = null
    private var id: String? = ""
    private var name: String? = ""
    private var Screen: String? = ""
    private var image: String? = ""
    private var type: String? = ""
    private lateinit var viewModel: CookBookViewModel
    private var localData: MutableList<CookBookDataModel> = mutableListOf()
    val dataList = arrayListOf<DataModel>()
    private var currentDate = Date() // Current date

    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    private lateinit var sessionManagement: SessionManagement
    private var referLink: String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentChristmasCollectionBinding.inflate(layoutInflater, container, false)
        sessionManagement = SessionManagement(requireContext())
        commonWorkUtils = CommonWorkUtils(requireActivity())

        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.GONE
            it.llBottomNavigation.visibility = View.GONE
        }

        Screen=arguments?.getString("Screen","")?:""

        viewModel = ViewModelProvider(requireActivity())[CookBookViewModel::class.java]
        cookbookList.clear()

        val data =
            com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data(
                "",
                "",
                0,
                "",
                "Favorites",
                0,
                "",
                0
            )
        cookbookList.add(0, data)

        id = sessionManagement.getCookBookId()
        name = sessionManagement.getCookBookName()
        image = sessionManagement.getCookBookImage()
        type = sessionManagement.getCookBookType()

        backButton()



        binding.btnLock.setOnClickListener {
            (activity as? MainActivity)?.subscriptionAlertError(requireContext())
        }

        initialize()

        if (BaseApplication.isOnline(requireActivity())) {
            if (Screen.equals("share",true)){
                updateCookBookId()
            }else{
                getCookBookTypeList()
            }
        }else{
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }


        return binding.root
    }

    private fun updateCookBookId() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.updateCookBookApi({
                BaseApplication.dismissMe()
                handleApiSuccessResponse(it)
            }, id)
        }
    }
    private fun handleApiSuccessResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleApiSuccResponse(result.data.toString())
            is NetworkResult.Error -> {
                showAlert(result.message, false)
            }
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun handleApiSuccResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, UserCookBokModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                apiModel.data?.cook_book?.id?.let {
                    id=it.toString()
                }
                apiModel.data?.cook_book?.name?.let {
                    name=it
                }
                apiModel.data?.cook_book?.image?.let {
                    image= BaseUrl.imageBaseUrl+it
                }
                apiModel.data?.cook_book?.status?.let {
                    type=it.toString()
                }
                getCookBookTypeList()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun backButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    sessionManagement.setCookBookId("")
                    sessionManagement.setCookBookName("")
                    sessionManagement.setCookBookImage("")
                    sessionManagement.setCookBookType("")
                    sessionManagement.setOpenCookBookUsingShare("")
                    findNavController().navigateUp()
                }
            })
    }

    private fun getCookBookTypeList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.getCookBookTypeRequest({
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }, id)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun handleApiCookBookResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookResponse(result.data.toString())
            is NetworkResult.Error -> {
                binding.rcyChristmasCollection.visibility = View.GONE
                binding.tvnoData.visibility = View.VISIBLE
                binding.btnLock.visibility = View.GONE
                showAlert(result.message, false)
            }

            else -> showAlert(result.message, false)
        }
    }

    private fun handleApiCookBookListResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookListResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookListResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
                    cookbookList.retainAll { it == cookbookList[0] }
                    cookbookList.addAll(apiModel.data)
                    cookbookList.removeIf {
                        it.id == id?.toInt()
                    }
                    // OR directly modify the original list
                    spinnerActivityLevel.setItems(cookbookList.map { it.name })
                }
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListApiResponse::class.java)
            Log.d("@@@ addMea List Collection", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if(apiModel.shared==0){
                    binding.imgThreeDotIcon.visibility=View.VISIBLE
                }else{
                    binding.imgThreeDotIcon.visibility=View.GONE
                }
                localData.clear()
                apiModel.data?.let { localData.addAll(it) }
                if (localData.size > 0) {
                    adapterCookBookDetailsItem = AdapterCookBookDetailsItem(localData, requireActivity(), this)
                    binding.rcyChristmasCollection.adapter = adapterCookBookDetailsItem
                    binding.rcyChristmasCollection.visibility = View.VISIBLE
                    binding.tvnoData.visibility = View.GONE
                    if ((activity as? MainActivity)?.Subscription_status == 1) {
                        binding.btnLock.visibility = View.VISIBLE
                    } else {
                        binding.btnLock.visibility = View.GONE
                    }
                } else {
                    binding.btnLock.visibility = View.GONE
                    binding.rcyChristmasCollection.visibility = View.GONE
                    binding.tvnoData.visibility = View.VISIBLE
                }
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            binding.rcyChristmasCollection.visibility = View.GONE
            binding.tvnoData.visibility = View.VISIBLE
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun initialize() {
        if (name != null) {
            binding.tvName.text = name
        }

        binding.imgBackChristmas.setOnClickListener {
            sessionManagement.setCookBookId("")
            sessionManagement.setCookBookName("")
            sessionManagement.setCookBookImage("")
            sessionManagement.setCookBookType("")
            sessionManagement.setOpenCookBookUsingShare("")
            findNavController().navigateUp()
        }

        //copyShareInviteLink()
        generateDeepLink()

        binding.imgThreeDotIcon.setOnClickListener {
            if (binding.cardViewMenuPopUp.visibility == View.VISIBLE) {
                binding.cardViewMenuPopUp.visibility = View.GONE
            } else {
                binding.cardViewMenuPopUp.visibility = View.VISIBLE
            }
        }

        binding.relEditCookBook.setOnClickListener {
            binding.cardViewMenuPopUp.visibility = View.GONE
            val bundle = Bundle()
            bundle.putString("value", "Edit")
            findNavController().navigate(R.id.createCookBookFragment, bundle)
        }

        binding.relShareCookBook.setOnClickListener {
            if (type == "1") {
                shareImageWithText(
                    "Hey! I am inviting you to download My-Kai App!" + "\nclick on the link below:\n\n",
                    referLink
                )
                binding.cardViewMenuPopUp.visibility = View.GONE

            } else {
                commonWorkUtils.alertDialog(
                    requireContext(),
                    ErrorMessage.shareCookBookError,
                    false
                )
            }
        }

        binding.relDeleteCookBook.setOnClickListener {
            binding.cardViewMenuPopUp.visibility = View.GONE
            removeCookBookDialog()
        }

        binding.rlAddRecipes.setOnClickListener {
            findNavController().navigate(R.id.createRecipeFragment)
        }


    }

    @SuppressLint("RestrictedApi")
    private fun copyShareInviteLink() {

        val afUserId = sessionManagement.getId()?.toString().orEmpty()
        val referrerCode = sessionManagement.getReferralCode()?.toString().orEmpty()
        val providerName = sessionManagement.getUserName()?.toString().orEmpty()
        val providerImage = sessionManagement.getImage()?.toString().orEmpty()

        val baseUrl = "https://applinks:mykaimealplanner.onelink.me/mPqu/"

        val fullUrl = Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("af_user_id", afUserId)
            .appendQueryParameter("Referrer", referrerCode)
            .appendQueryParameter("CookbooksID", id)
            .appendQueryParameter("ItemName", name)
            .appendQueryParameter("ScreenName", "CookBooksType")
            .appendQueryParameter("providerName", providerName)
            .appendQueryParameter("providerImage", providerImage)
            .appendQueryParameter("af_web_dp", "https://admin.getmykai.com/") // Web fallback URL
            .build()
            .toString()

        referLink = fullUrl
        Log.d("AF_TEST", "Custom Raw Link: $referLink")


    }


    private fun generateDeepLink() {

        val afUserId = sessionManagement.getId()?.toString().orEmpty()
        val referrerCode = sessionManagement.getReferralCode()?.toString().orEmpty()
        val providerName = sessionManagement.getUserName()?.toString().orEmpty()
        val providerImage = sessionManagement.getImage()?.toString().orEmpty()



        // from deepak
      // Base URL for the OneLink template
        val baseURL = "https://mykaimealplanner.onelink.me/mPqu/" // Replace with your OneLink template

      // Deep link URL for when the app is installed
        val deepLink = "mykai://property?" +
                "af_user_id=$afUserId" +
                "&CookbooksID=$id" +
                "&ItemName=$name" +
                "&ScreenName=CookBooksType"

       // Web fallback URL (e.g., if app is not installed)
        val webLink = "https://www.mykaimealplanner.com" // Replace with your fallback web URL

       // Build OneLink parameters
        val parameters = mapOf(
//            "af_user_id" to afUserId,
//            "providerName" to providerName,
//            "CookbooksID" to id,
//            "ItemName" to name,
            "providerImage" to providerImage,
//            "ScreenName" to "CookBooksType",
            "Referrer" to referrerCode,
            "af_dp" to deepLink, // App deep link
            "af_web_dp" to webLink // Web fallback URL
        )

// Use Uri.Builder to construct the URL with query parameters
        val uriBuilder = Uri.parse(baseURL).buildUpon()
        for ((key, value) in parameters) {
            uriBuilder.appendQueryParameter(key, value)
        }

// Convert the URI to string and call the completion handler
        val fullURL = uriBuilder.toString()
        referLink = fullURL
        Log.d("link ", "Generated OneLink URL: $fullURL")


        // from dhananjay

//        val afUserId = sessionManagement.getId()?.toString().orEmpty()
//        val referrerCode = sessionManagement.getReferralCode()?.toString().orEmpty()
//        val providerName = sessionManagement.getUserName()?.toString().orEmpty()
//        val providerImage = sessionManagement.getImage()?.toString().orEmpty()
//
//        // Base URL for the OneLink template
//        val baseURL = "https://mykaimealplanner.onelink.me/mPqu/" // Replace with your OneLink template
//        val deepLink = "mykai://property?" +
//                "af_user_id=$afUserId" +
//                "&CookbooksID=$id" +
//                "&ItemName=$name" +
//                "&ScreenName=CookBooksType"
//
//        // Build query parameters
//        val parameters = mapOf(
//            "af_user_id" to afUserId,
//            "providerName" to providerName,
//            "CookbooksID" to id,
//            "ItemName" to name,
//            "providerImage" to providerImage,
//            "ScreenName" to "CookBooksType",
//            "Referrer" to referrerCode
//        )
//
//        // Use Uri.Builder to construct the URL with query parameters
//        val uriBuilder = Uri.parse(baseURL).buildUpon()
//        for ((key, value) in parameters) {
//            uriBuilder.appendQueryParameter(key, value)
//        }
//
//        // Convert the URI to string and call the completion handler
//        val fullURL = uriBuilder.toString()
//        referLink= fullURL
//        Log.d("link ", "*****$fullURL")


//        // Your OneLink base config
//        val currentCampaign = "property_share"
//        val oneLinkId = "mPqu" // Replace with your OneLink ID
//        val brandDomain = "mykaimealplanner.onelink.me" // Replace with your OneLink domain
//
//        // Deep link for the app (must match the intent filter)
//        val deepLink = "mykai://property?" +
//                "af_user_id=$afUserId" +
//                "&ScreenName=CookBooksType" +
//                "&CookbooksID=$id" +
//                "&ItemName=$name"
//
//        // Web fallback link (optional, opens if app not installed)
//        val webLink = "https://https://admin.getmykai.com/" // Web fallback link
//
//
//
//        // Create the link generator
//        val linkGenerator = ShareInviteHelper.generateInviteUrl(requireActivity())
//            .setBaseDeeplink("https://$brandDomain/$oneLinkId")
//            .setCampaign(currentCampaign)
//            .addParameter("af_dp", deepLink) // App deep link
//            .addParameter("Referrer", referrerCode)
//            .addParameter("af_web_dp", webLink) // Web fallback URL
//            .addParameter("CookbooksID", id)
//            .addParameter("ItemName", name)
//            .addParameter("ScreenName", "CookBooksType")
//            .addParameter("providerName", providerName)
//            .addParameter("providerImage", providerImage)
//
//
//        // Generate the link
//        linkGenerator.generateLink(requireActivity(), object : LinkGenerator.ResponseListener {
//            override fun onResponse(s: String) {
//                // Successfully generated the link
//                Log.d("TAG", s)
//                // Example share message with the generated link
//                val message = "Check out this property: $s"
//                referLink = s
//                Log.d("***********", s)
//            }
//
//            override fun onResponseError(s: String) {
//                // Handle error if link generation fails
//                Log.e("***********", "Error Generating Link: $s")
//            }
//        })
    }


/*    private fun generateDeepLink() {

        val afUserId = sessionManagement.getId()?.toString().orEmpty()
        val referrerCode = sessionManagement.getReferralCode()?.toString().orEmpty()
        val providerName = sessionManagement.getUserName()?.toString().orEmpty()
        val providerImage = sessionManagement.getImage()?.toString().orEmpty()


        // Your OneLink base URL and campaign details
        val currentCampaign = "property_share"
        val oneLinkId = "mPqu" // Replace with your OneLink ID
        val brandDomain = "mykaimealplanner.onelink.me" // Your OneLink domain

        // Prepare the deep link values
        val deepLink =
            "mykai://property?af_user_id=$afUserId&ScreenName=CookBooksType&CookbooksID=$id&ItemName=$name"

        //  val deepLink = "https://property?propertyId=$propertyId&propertyType=$propertyType&city=$city"

        val webLink = "https://https://admin.getmykai.com/" // Web fallback link

        // Create the link generator
        val linkGenerator = ShareInviteHelper.generateInviteUrl(requireActivity())
            .setBaseDeeplink("https://$brandDomain/$oneLinkId")
            .setCampaign(currentCampaign)
            .addParameter("af_dp", deepLink) // App deep link
            *//*.addParameter("Referrer", referrerCode)
            .addParameter("CookbooksID", id)
            .addParameter("ItemName", name)
            .addParameter("ScreenName", "CookBooksType")
            .addParameter("providerName", providerName)
            .addParameter("providerImage", providerImage)*//*
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
    }*/


    private fun shareImageWithText(description: String, link: String) {
        // Download image using Glide
        Glide.with(requireContext())
            .asBitmap() // Request a Bitmap image
            .load(image) // Provide the URL to load the image from
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

    private fun removeCookBookDialog() {
        val dialogRemoveCookBook: Dialog = context?.let { Dialog(it) }!!
        dialogRemoveCookBook.setContentView(R.layout.alert_dialog_remove_cookbook)
        dialogRemoveCookBook.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogRemoveCookBook.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogCancelBtn = dialogRemoveCookBook.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogRemoveBtn = dialogRemoveCookBook.findViewById<TextView>(R.id.tvDialogRemoveBtn)
        dialogRemoveCookBook.show()
        dialogRemoveCookBook.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogRemoveCookBook.dismiss()
        }

        tvDialogRemoveBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                deleteCookBookMove(dialogRemoveCookBook)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        when (status) {
            "1" -> {
                if ((activity as? MainActivity)?.Subscription_status == 1) {
                    if ((activity as? MainActivity)?.addmeal!! < 1) {
                        chooseDayDialog(position, type)
                    } else {
                        (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                    }
                } else {
                    chooseDayDialog(position, type)
                }
            }

            "2" -> {
                if (BaseApplication.isOnline(requireActivity())) {
                    addBasketData(localData[position!!].data?.recipe!!.uri!!, type.toString())
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }

            "4" -> {
                try {
                    val bundle = Bundle().apply {
                        val data = localData[position!!].data?.recipe!!.mealType?.get(0)?.split("/")
                        val formattedFoodName = data?.get(0)!!.replaceFirstChar { it.uppercase() }
                        putString("uri", localData[position].data?.recipe!!.uri!!)
                        putString("mealType", formattedFoodName)
                    }
                    findNavController().navigate(R.id.recipeDetailsFragment, bundle)
                } catch (e: Exception) {
                    BaseApplication.alertError(requireContext(), e.message.toString(), false)

                }
            }

            "5" -> {
                moveRecipeDialog(position)
            }

            else -> {
                if (BaseApplication.isOnline(requireActivity())) {
                    if ((activity as? MainActivity)?.Subscription_status == 1) {
                        if ((activity as? MainActivity)?.favorite!! <= 2) {
                            removeRecipeDialog(position)
                        } else {
                            (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                        }
                    } else {
                        removeRecipeDialog(position)
                    }
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun removeRecipeDialog(position: Int?) {
        val dialogRemoveRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogRemoveRecipe.setContentView(R.layout.alert_dialog_remove_recipe)
        dialogRemoveRecipe.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogRemoveRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogCancelBtn = dialogRemoveRecipe.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogRemoveBtn = dialogRemoveRecipe.findViewById<TextView>(R.id.tvDialogRemoveBtn)
        dialogRemoveRecipe.show()
        dialogRemoveRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogRemoveRecipe.dismiss()
        }

        tvDialogRemoveBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                recipeLikeAndUnlikeData(position, dialogRemoveRecipe)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun recipeLikeAndUnlikeData(position: Int?, dialogRemoveRecipe: Dialog) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it, position, dialogRemoveRecipe)
            }, localData[position!!].data?.recipe?.uri.toString(), "0", "")
        }
    }

    private fun handleDeleteCookBookApiResponse(
        result: NetworkResult<String>,
        dialogRemoveCookBook: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleDeleteCookBookSuccessResponse(
                result.data.toString(),
                dialogRemoveCookBook
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        position: Int?,
        dialogRemoveRecipe: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(
                result.data.toString(),
                position,
                dialogRemoveRecipe
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(
        data: String,
        position: Int?,
        dialogRemoveRecipe: Dialog
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                localData.removeAt(
                    position ?: return
                ) // Safely handle null position, return if null
                if (localData.size > 0) {
                    adapterCookBookDetailsItem?.updateList(localData)
                    binding.rcyChristmasCollection.visibility = View.VISIBLE
                    binding.tvnoData.visibility = View.GONE
                } else {
                    binding.tvnoData.visibility = View.VISIBLE
                    binding.rcyChristmasCollection.visibility = View.GONE
                }
                (activity as MainActivity?)?.upDateHomeData()
                dialogRemoveRecipe.dismiss()

            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleDeleteCookBookSuccessResponse(data: String, dialogRemoveCookBook: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogRemoveCookBook.dismiss()
                findNavController().navigateUp()
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun moveRecipeDialog(position: Int?) {
        val dialogMoveRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogMoveRecipe.setContentView(R.layout.alert_dialog_move_dialog)
        dialogMoveRecipe.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogMoveRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlMove = dialogMoveRecipe.findViewById<RelativeLayout>(R.id.rlMove)
        val imgCrossDiscardChanges =
            dialogMoveRecipe.findViewById<ImageView>(R.id.imgCrossDiscardChanges)

        spinnerActivityLevel = dialogMoveRecipe.findViewById(R.id.spinnerActivityLevel)

        spinnerActivityLevel.setItems(cookbookList.map { it.name })

        dialogMoveRecipe.show()
        dialogMoveRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        getCookBookList()

        rlMove.setOnClickListener {
            if (spinnerActivityLevel.text.toString().equals("", true)) {
                BaseApplication.alertError(
                    requireContext(),
                    ErrorMessage.selectCookBookError,
                    false
                )
            } else {
                if (BaseApplication.isOnline(requireActivity())) {
                    val cookbooktype = cookbookList[spinnerActivityLevel.selectedIndex].id
                    recipeMove(position, dialogMoveRecipe, cookbooktype)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        imgCrossDiscardChanges.setOnClickListener {
            dialogMoveRecipe.dismiss()
        }

    }

    private fun recipeMove(position: Int?, dialogRemoveRecipe: Dialog, cookbooktype: Int) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.moveRecipeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it, position, dialogRemoveRecipe)
            }, localData[position!!].id.toString(), cookbooktype.toString())
        }
    }

    private fun deleteCookBookMove(dialogRemoveCookBook: Dialog) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.deleteCookBookRequest({
                BaseApplication.dismissMe()
                handleDeleteCookBookApiResponse(it, dialogRemoveCookBook)
            }, id.toString())
        }
    }

    private fun getCookBookList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookListResponse(it)
            }
        }
    }

    private fun addBasketData(uri: String, mealType: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.addBasketRequest({
                BaseApplication.dismissMe()
                handleBasketApiResponse(it)
            }, uri, "", mealType)
        }
    }

    private fun handleBasketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleBasketSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleBasketSuccessResponse(
        data: String
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
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
    private fun chooseDayDialog(position: Int?, typeAdapter: String?) {
        val dialogChooseDay: Dialog = context?.let { Dialog(it) }!!
        dialogChooseDay.setContentView(R.layout.alert_dialog_choose_day)
        dialogChooseDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogChooseDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        rcyChooseDaySch = dialogChooseDay.findViewById<RecyclerView>(R.id.rcyChooseDaySch)
        tvWeekRange = dialogChooseDay.findViewById(R.id.tvWeekRange)
        val rlDoneBtn = dialogChooseDay.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        val btnPrevious = dialogChooseDay.findViewById<ImageView>(R.id.btnPrevious)
        val btnNext = dialogChooseDay.findViewById<ImageView>(R.id.btnNext)
        dialogChooseDay.show()
        dialogChooseDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        dataList.clear()
        val daysOfWeek =
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        for (day in daysOfWeek) {
            val data = DataModel().apply {
                title = day
                isOpen = false
                type = "CookingSchedule"
                date = ""
            }
            dataList.add(data)
        }

        showWeekDates()

        rlDoneBtn.setOnClickListener {
            var status = false
            for (it in dataList) {
                if (it.isOpen) {
                    status = true
                    break // Exit the loop early
                }
            }
            if (status) {
                chooseDayMealTypeDialog(position, typeAdapter)
                dialogChooseDay.dismiss()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.weekNameError, false)
            }
        }

        btnPrevious.setOnClickListener {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedCurrentDate = dateFormat.format(currentDate)
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
            val currentDate1 = calendar.time
            val (startDate, endDate) = getWeekDates(currentDate1)
            println("Week Start Date: ${formatDate(startDate)}")
            println("Week End Date: ${formatDate(endDate)}")
            // Get all dates between startDate and endDate
            val daysBetween = getDaysBetween(startDate, endDate)
            // Mark the current date as selected in the list
            val updatedDaysBetween1 = daysBetween.map { dateModel ->
                dateModel.apply {
                    status = (date == formattedCurrentDate) // Compare formatted strings
                }
            }
            var status = false
            updatedDaysBetween1.forEach {
                status = it.date >= BaseApplication.currentDateFormat().toString()
            }
            if (status) {
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
                currentDate = calendar.time
                // Display next week dates
                println("\nAfter clicking 'Next':")
                showWeekDates()
            } else {
                Toast.makeText(requireContext(), ErrorMessage.slideError, Toast.LENGTH_LONG).show()
            }
        }

        btnNext.setOnClickListener {
            // Simulate clicking the "Next" button to move to the next week
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, 1) // Move to next week
            currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDates()
        }

    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getWeekDates(currentDate: Date): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        // Set the calendar to the start of the week (Monday)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = calendar.time

        // Set the calendar to the end of the week (Saturday)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time
        return Pair(startOfWeek, endOfWeek)
    }

    @SuppressLint("SetTextI18n")
    fun showWeekDates() {
        Log.d("currentDate :- ", "******$currentDate")
        val (startDate, endDate) = getWeekDates(currentDate)
        this.startDate = startDate
        this.endDate = endDate
        println("Week Start Date: ${formatDate(startDate)}")
        println("Week End Date: ${formatDate(endDate)}")
        // Get all dates between startDate and endDate
        val daysBetween = getDaysBetween(startDate, endDate)
        // Mark the current date as selected in the list
        daysBetween.zip(dataList).forEach { (dateModel, dataModel) ->
            dataModel.date = dateModel.date
            dataModel.isOpen = false
        }

        rcyChooseDaySch?.adapter = ChooseDayAdapter(dataList, requireActivity())

        // Print the dates
        println("Days between ${startDate} and ${endDate}:")
        daysBetween.forEach { println(it) }
        tvWeekRange?.text = "" + formatDate(startDate) + "-" + formatDate(endDate)

    }

    private fun getDaysBetween(startDate: Date, endDate: Date): MutableList<DateModel> {
        val dateList = mutableListOf<DateModel>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Format for the date
        val dayFormat =
            SimpleDateFormat("EEEE", Locale.getDefault()) // Format for the day name (e.g., Monday)
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        while (!calendar.time.after(endDate)) {
            val date = dateFormat.format(calendar.time)  // Get the formatted date (yyyy-MM-dd)
            val dayName =
                dayFormat.format(calendar.time)  // Get the day name (Monday, Tuesday, etc.)
            val localDate = DateModel()
            localDate.day = dayName
            localDate.date = date
            // Combine both the day name and the date
            dateList.add(localDate)
            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateList
    }

    private fun chooseDayMealTypeDialog(position: Int?, typeAdapter: String?) {
        val dialogChooseMealDay: Dialog = context?.let { Dialog(it) }!!
        dialogChooseMealDay.setContentView(R.layout.alert_dialog_choose_day_meal_type)
        dialogChooseMealDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogChooseMealDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogChooseMealDay.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        // button event listener
        val tvBreakfast = dialogChooseMealDay.findViewById<TextView>(R.id.tvBreakfast)
        val tvLunch = dialogChooseMealDay.findViewById<TextView>(R.id.tvLunch)
        val tvDinner = dialogChooseMealDay.findViewById<TextView>(R.id.tvDinner)
        val tvSnacks = dialogChooseMealDay.findViewById<TextView>(R.id.tvSnacks)
        val tvTeatime = dialogChooseMealDay.findViewById<TextView>(R.id.tvTeatime)
        dialogChooseMealDay.show()
        dialogChooseMealDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        var type = ""

        fun updateSelection(
            selectedType: String,
            selectedView: TextView,
            allViews: List<TextView>
        ) {
            type = selectedType
            allViews.forEach { view ->
                val drawable =
                    if (view == selectedView) R.drawable.radio_select_icon else R.drawable.radio_unselect_icon
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
            }
        }

        val allViews = listOf(tvBreakfast, tvLunch, tvDinner, tvSnacks, tvTeatime)

        tvBreakfast.setOnClickListener {
            updateSelection("Breakfast", tvBreakfast, allViews)
        }

        tvLunch.setOnClickListener {
            updateSelection("Lunch", tvLunch, allViews)
        }

        tvDinner.setOnClickListener {
            updateSelection("Dinner", tvDinner, allViews)
        }

        tvSnacks.setOnClickListener {
            updateSelection("Snacks", tvSnacks, allViews)
        }

        tvTeatime.setOnClickListener {
            updateSelection("Brunch", tvTeatime, allViews)
        }


        rlDoneBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (type.equals("", true)) {
                    BaseApplication.alertError(requireContext(), ErrorMessage.mealTypeError, false)
                } else {
                    addToPlan(dialogChooseMealDay, type, position)
                }

            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

    }

    private fun addToPlan(dialogChooseMealDay: Dialog, selectType: String, position: Int?) {
        // Create a JsonObject for the main JSON structure
        val jsonObject = JsonObject()
        // Safely get the item and position
        val item = localData[position!!]
        if (item != null) {
            if (item.data?.recipe?.uri != null) {
                jsonObject.addProperty("type", selectType)
                jsonObject.addProperty("uri", item.data.recipe.uri)
                // Create a JsonArray for ingredients
                val jsonArray = JsonArray()
                val latestList = getDaysBetween(startDate, endDate)
                for (i in dataList.indices) {
                    val data = DataModel()
                    data.isOpen = dataList[i].isOpen
                    data.title = dataList[i].title
                    data.date = latestList[i].date
                    dataList[i] = data
                }
                // Iterate through the ingredients and add them to the array if status is true
                dataList.forEach { data ->
                    if (data.isOpen) {
                        // Create a JsonObject for each ingredient
                        val ingredientObject = JsonObject()
                        ingredientObject.addProperty("date", data.date)

                        ingredientObject.addProperty("day", data.title)
                        // Add the ingredient object to the array
                        jsonArray.add(ingredientObject)
                    }
                }

                // Add the ingredients array to the main JSON object
                jsonObject.add("slot", jsonArray)
            }
        }

        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.recipeAddToPlanRequest({
                BaseApplication.dismissMe()
                handleApiAddToPlanResponse(it, dialogChooseMealDay)
            }, jsonObject)
        }
    }

    private fun handleApiAddToPlanResponse(
        result: NetworkResult<String>,
        dialogChooseMealDay: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddToPlanResponse(
                result.data.toString(),
                dialogChooseMealDay
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddToPlanResponse(data: String, dialogChooseMealDay: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dataList.clear()
                (activity as MainActivity?)?.upDateHomeData()
                dialogChooseMealDay.dismiss()
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

}