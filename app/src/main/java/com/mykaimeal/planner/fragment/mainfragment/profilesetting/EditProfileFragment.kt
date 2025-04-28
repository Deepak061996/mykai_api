package com.mykaimeal.planner.fragment.mainfragment.profilesetting

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.apiInterface.ApiInterface
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentEditProfileBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.SettingViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.ProfileRootResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.repository.Feature
import com.mykaimeal.planner.repository.Image
import com.mykaimeal.planner.repository.Request
import com.mykaimeal.planner.repository.VisionRequest
import com.mykaimeal.planner.repository.VisionResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingViewModel
    private var file: File? = null
    private lateinit var sessionManagement: SessionManagement

    private val pickImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->

                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                file = getPath(requireContext(), uri)?.let { File(it) }

                /*processImage(bitmap)*/
               // Now you can send the image URI to Vision API for processing
                // Convert image to Base64
               /* val base64Image = convertImageToBase64(uri)

                // Call Vision API to recognize the image
                recognizeImage(base64Image)*/

                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.mask_group_icon)
                    .error(R.drawable.mask_group_icon)
                    .into(binding.imageEditProfile)

            }
        }
    }

    fun recognizeImage(base64Image: String) {

        BaseApplication.showMe(requireContext())
        val visionApiService = createRetrofit().create(ApiInterface::class.java)

// Request setup for WEB_DETECTION instead of LABEL_DETECTION
        val features = listOf(Feature("WEB_DETECTION", 1))  // Adjusting to use WEB_DETECTION
        val image = Image(base64Image)
        val request = Request(image, features)
        val visionRequest = VisionRequest(listOf(request))

// Pass your API key (this is just for demonstration, don't hardcode your API key)
        val apiKey = "AIzaSyB1WtrB2oHQmyIX1ZpaXzbI9kOA2FlkCXk"

        val call = visionApiService.annotateImage(apiKey, visionRequest)
        call.enqueue(object : retrofit2.Callback<VisionResponse> {
            override fun onResponse(call: Call<VisionResponse>, response: retrofit2.Response<VisionResponse>) {
                BaseApplication.dismissMe()
                if (response.isSuccessful) {
                    val webDetection = response.body()?.responses?.get(0)?.webDetection
                   if (webDetection!=null){
                       if (webDetection.webEntities.size>0){
                           Toast.makeText(requireContext(),"Name :-"+webDetection?.webEntities!![0].description.toString(),Toast.LENGTH_LONG).show()
                           Log.d("Google Vision", "Similar Image: "+ webDetection?.webEntities!![0].description.toString())
                       }
                   }
                }else{
                    Toast.makeText(requireContext(),"Name :-"+response.message(),Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<VisionResponse>, t: Throwable) {
                Log.e("Google Vision", "API call failed", t)
                Toast.makeText(requireContext(),"Name :-"+t.message,Toast.LENGTH_LONG).show()
            }
        })
    }

    // Retrofit setup
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://vision.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    fun convertImageToBase64(uri: Uri): String {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }


    private fun getPath(context: Context, uri: Uri): String? {
        var uri = uri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                when (split[0]) {
                    "image" -> {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor =
                    context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (_: Exception) {
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SettingViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())


        setupUI()
        setupListeners()
        handleBackPress()

        return binding.root
    }

    private fun setupUI() {
        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }


        if (viewModel.getProfileData()!=null){

            if (viewModel.getProfileData()?.name!=null){
                binding.etName.setText(viewModel.getProfileData()?.name)
            }

            if (viewModel.getProfileData()?.profile_img!=null){
                Glide.with(this)
                    .load(BaseUrl.imageBaseUrl+viewModel.getProfileData()?.profile_img)
                    .placeholder(R.drawable.image_not)
                    .error(R.drawable.image_not)
                    .into(binding.imageEditProfile)
            }

        }


    }

    private fun setupListeners() {
        binding.imgBackEditProfile.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.relSaveChanges.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (binding.etName.text.toString().trim().isEmpty()){
                    BaseApplication.alertError(requireContext(), ErrorMessage.bioError, false)
                }else{
                    upDateImageNameProfile()
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        binding.textChangeImage.setOnClickListener {
            ImagePicker.with(this)
                .crop() // Crop image (Optional)
                .compress(1024 * 5) // Compress the image to less than 5 MB
                .maxResultSize(250, 250) // Set max resolution
                .createIntent { intent -> pickImageLauncher.launch(intent) }
        }
    }

    private fun upDateImageNameProfile() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            val filePart: MultipartBody.Part? = if (file != null) {
                val requestBody = file?.asRequestBody(file!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profile_img", file?.name, requestBody!!)
            } else {
                null
            }
            val nameBody = binding.etName.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            viewModel.upDateImageNameRequest({
                BaseApplication.dismissMe()
                handleApiUpdateResponse(it)
            }, filePart,nameBody)
        }
    }

    private fun handleApiUpdateResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleUpdateSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleUpdateSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ProfileRootResponse::class.java)
            Log.d("@@@ Health profile", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                apiModel.data.profile_img?.let { sessionManagement.setImage(it) }
                apiModel.data.name?.let { sessionManagement.setUserName(it) }
                 findNavController().navigateUp()
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


    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
