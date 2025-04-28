package com.mykaimeal.planner.fragment.mainfragment.hometab.createcookbookfragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
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
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentCreateCookBookBinding
import com.mykaimeal.planner.fragment.mainfragment.hometab.createcookbookfragment.model.CreateCookBookModel
import com.mykaimeal.planner.fragment.mainfragment.hometab.createcookbookfragment.viewmodel.CreateCookBookViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class CreateCookBookFragment : Fragment() {
    private lateinit var binding: FragmentCreateCookBookBinding
    private var isOpened: Boolean? = false
    private var checkType: String? = null
    private var uri: String? = null
    private var file: File? = null
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var createCookBookViewModel: CreateCookBookViewModel
    private val selectedButton = arrayOf<RadioButton?>(null)
    private var status:String?="0"
    private var id:String?=""
    private var name:String?=""
    private var image:String?=""
    private lateinit var sessionManagement: SessionManagement

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentCreateCookBookBinding.inflate(layoutInflater, container, false)
        sessionManagement = SessionManagement(requireContext())
        createCookBookViewModel = ViewModelProvider(this)[CreateCookBookViewModel::class.java]

        commonWorkUtils = CommonWorkUtils(requireActivity())

        checkType = arguments?.getString("value", "")?:""
        uri = arguments?.getString("uri", "")?:""

        if (checkType!="New"){
            id=sessionManagement.getCookBookId()
            name=sessionManagement.getCookBookName()
            image=sessionManagement.getCookBookImage()
            status=sessionManagement.getCookBookType()
        }


        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        backButton()

        initialize()

        return binding.root
    }
    
    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }


    private val pickImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                binding.imageCookBook.visibility=View.VISIBLE
                binding.llAddImages.visibility=View.GONE
                binding.llAddImage.background=null
                file = commonWorkUtils.getPath(requireContext(), uri)?.let { File(it) }
                image=file.toString()
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.mask_group_icon)
                    .error(R.drawable.mask_group_icon)
                    .into(binding.imageCookBook)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {

        if (checkType == "New") {
            binding.tvToolbar.text = "Create Cookbook"
            binding.textDone.text = "Done"
        } else {
            binding.tvToolbar.text = "Edit Cookbook"
            binding.textDone.text = "Update"
        }

        if (!name.equals("",true)){
            binding.etEnterYourNewCookbook.setText(name)
        }

        if (!image.equals("")){
            binding.imageCookBook.visibility=View.VISIBLE
            binding.llAddImages.visibility=View.GONE
            Glide.with(requireActivity())
                .load(BaseUrl.imageBaseUrl+image)
                .placeholder(R.drawable.mask_group_icon)
                .error(R.drawable.mask_group_icon)
                .into(binding.imageCookBook)
        }

        if (!status.equals("",true)){
             if (status.equals("0",true)){
                 // Select the RadioButton and update the selected reference
                 binding.radioPrivate.isChecked = true
                 binding.radioPublic.isChecked = false
                 selectedButton[0] = binding.radioPrivate
                 status="0"
             }else{
                 // Select the RadioButton and update the selected reference
                 binding.radioPublic.isChecked = true
                 binding.radioPrivate.isChecked = false
                 selectedButton[0] = binding.radioPublic
                 status="1"
             }
        }

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imageInfo.setOnClickListener {
            if (isOpened == true) {
                isOpened = false
                binding.cvInfoMessage.visibility = View.GONE
            } else {
                isOpened = true
                binding.cvInfoMessage.visibility = View.VISIBLE
            }
        }


        binding.textDone.setOnClickListener {
            if (validate()) {
                if (BaseApplication.isOnline(requireActivity())) {
                    createCookBookApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }

        }

         // Set listeners for each RadioButton
        binding.radioPrivate.setOnClickListener {
            if (selectedButton[0] === binding.radioPrivate) {
                // If already selected, deselect it
                binding.radioPrivate.isChecked = false
                selectedButton[0] = null
                status=""
            } else {
                // Select the RadioButton and update the selected reference
                binding.radioPrivate.isChecked = true
                binding.radioPublic.isChecked = false
                selectedButton[0] = binding.radioPrivate
                status="0"
            }
        }

        binding.radioPublic.setOnClickListener {
            if (selectedButton[0] === binding.radioPublic) {
                // If already selected, deselect it
                binding.radioPublic.isChecked = false
                selectedButton[0] = null
                status=""
            } else {
                // Select the RadioButton and update the selected reference
                binding.radioPublic.isChecked = true
                binding.radioPrivate.isChecked = false
                selectedButton[0] = binding.radioPublic
                status="1"
            }
        }

        binding.llAddImage.setOnClickListener {
            ImagePicker.with(this)
                .crop() // Crop image (Optional)
                .compress(1024 * 5) // Compress the image to less than 5 MB
                .maxResultSize(250, 250) // Set max resolution
                .createIntent { intent -> pickImageLauncher.launch(intent) }
        }

    }

    /// add validation based on valid email or phone
    private fun validate(): Boolean {
        if (image.equals("")) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.cookbookUpload, false)
            return false
        } else if (binding.etEnterYourNewCookbook.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.cookbookName, false)
            return false
        } else if (status=="") {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.selectPrivatePublic, false)
            return false
        }
        return true
    }

    private fun createCookBookApi() {
        BaseApplication.showMe(requireActivity())
        lifecycleScope.launch {
            val filePart: MultipartBody.Part? = if (file != null) {
                val requestBody = file?.asRequestBody(file!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", file?.name, requestBody!!)
            } else {
                null
            }
            val cookBookName = binding.etEnterYourNewCookbook.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val cookBookStatus = status?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val idStatus = id?.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            createCookBookViewModel.createCookBookApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        val createCookBookModel = Gson().fromJson(it.data, CreateCookBookModel::class.java)
                        if (createCookBookModel.code == 200 && createCookBookModel.success) {
                            if (uri.equals("",true)){
                                Toast.makeText(requireContext(),createCookBookModel.message,Toast.LENGTH_LONG).show()
                                sessionManagement.setCookBookId(createCookBookModel.data.id.toString())
                                sessionManagement.setCookBookName(binding.etEnterYourNewCookbook.text.toString())
                                sessionManagement.setCookBookType(status!!)
                                findNavController().navigateUp()
                            }else{
                                recipeLikeAndUnlikeData(createCookBookModel.data.id.toString(),createCookBookModel.message)
                            }
                        } else {
                            if (createCookBookModel.code == ErrorMessage.code) {
                                showAlertFunction(createCookBookModel.message, true)
                            } else {
                                showAlertFunction(createCookBookModel.message, false)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, cookBookName, filePart, cookBookStatus,idStatus)
        }
    }

    private fun recipeLikeAndUnlikeData(cookbooktype: String, message: String) {
        lifecycleScope.launch {
            createCookBookViewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it,message)
            }, uri!!,"1",cookbooktype)
        }
    }

    private fun handleLikeAndUnlikeApiResponse(result: NetworkResult<String>, message: String) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(result.data.toString(),message)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(data: String, message: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
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

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


}