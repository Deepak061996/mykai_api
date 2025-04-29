package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefromimage

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.apiInterface.ApiInterface
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.FragmentCreateRecipeImageBinding
import com.mykaimeal.planner.repository.Feature
import com.mykaimeal.planner.repository.Image
import com.mykaimeal.planner.repository.Request
import com.mykaimeal.planner.repository.VisionRequest
import com.mykaimeal.planner.repository.VisionResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CreateRecipeImageFragment : Fragment() {

    private lateinit var binding: FragmentCreateRecipeImageBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isFlashOn = false
    private var capturedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding= FragmentCreateRecipeImageBinding.inflate(layoutInflater,container,false)


        requireActivity().onBackPressedDispatcher.addCallback(
           viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
        
        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.GONE
            it.llBottomNavigation.visibility = View.GONE
        }



        initialize()

        return binding.root
    }

    private fun initialize() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }



        clickListener()

        cameraExecutor = Executors.newSingleThreadExecutor()

    }



    private fun convertImageToBase64(uri: Uri): String {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }



            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder().setFlashMode(if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun requestPermissions() {
        requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }


    private fun clickListener() {
        binding.galleryBtn.setOnClickListener {
            galleryIntent()
        }

        binding.camera.setOnClickListener {
            takePhoto()
        }

        binding.flashIcon.setOnClickListener {
            flashWorking()
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        // Tick button
        binding.okBtn.setOnClickListener {
            recognizeImage(convertImageToBase64(capturedImageUri!!))
        }

        // Cancel button
        binding.noBtn.setOnClickListener {
            restoreCameraView()
        }



    }

    private fun recognizeImage(base64Image: String) {

        BaseApplication.showMe(requireContext())
        val visionApiService = createRetrofit().create(ApiInterface::class.java)

       // Request setup for WEB_DETECTION instead of LABEL_DETECTION
        val features = listOf(Feature("WEB_DETECTION", 1))  // Adjusting to use WEB_DETECTION
        val image = Image(base64Image)
        val request = Request(image, features)
        val visionRequest = VisionRequest(listOf(request))

        /////new api key
        val apiKey = "AIzaSyCjOLbQCG6foFlN05JOFKBpjNqV8DE9vi8"
        val call = visionApiService.annotateImage(apiKey, visionRequest)
        call.enqueue(object : retrofit2.Callback<VisionResponse> {
            override fun onResponse(call: Call<VisionResponse>, response: retrofit2.Response<VisionResponse>) {
                BaseApplication.dismissMe()
                if (response.isSuccessful) {
                    val webDetection = response.body()?.responses?.get(0)?.webDetection
                    if (webDetection!=null){
                        if (webDetection.webEntities.isNotEmpty()){
                            val bundle = Bundle().apply {
                                putString("name", webDetection.webEntities[0].description.toString())
                            }
                            findNavController().navigate(R.id.createRecipeFragment, bundle)

                            Log.d("Google Vision", "Similar Image: "+ webDetection.webEntities[0].description.toString())
                        }
                    }
                    restoreCameraView()
                }else{
                    restoreCameraView()
                    Toast.makeText(requireContext(),"Failed :-"+response.message(),Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<VisionResponse>, t: Throwable) {
                Log.e("Google Vision", "API call failed", t)
                Toast.makeText(requireContext(),"Error :-"+t.message,Toast.LENGTH_LONG).show()
                restoreCameraView()
            }
        })
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://vision.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun showCapturedPreview() {
        binding.previewView.visibility = View.GONE
        binding.galleryBtn.visibility = View.GONE
        binding.camera.visibility = View.GONE
        binding.flashIcon.visibility = View.GONE
        binding.backBtn.visibility = View.GONE

        binding.ImageView.visibility = View.VISIBLE
        binding.ImageView.setImageURI(capturedImageUri)

        binding.okBtn.visibility = View.VISIBLE
        binding.noBtn.visibility = View.VISIBLE
    }

    private fun restoreCameraView() {
        binding.previewView.visibility = View.VISIBLE
        binding.galleryBtn.visibility = View.VISIBLE
        binding.camera.visibility = View.VISIBLE
        binding.flashIcon.visibility = View.VISIBLE
        binding.backBtn.visibility = View.VISIBLE

        binding.ImageView.visibility = View.GONE
        binding.okBtn.visibility = View.GONE
        binding.noBtn.visibility = View.GONE
        capturedImageUri = null
    }


    private fun flashWorking() {
        isFlashOn = !isFlashOn
        imageCapture?.flashMode = if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MY-KAI-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(requireActivity().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImageUri = output.savedUri
                    showCapturedPreview()
                }
            }
        )
    }

    @SuppressLint("IntentReset")
    private fun galleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 201)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 201 && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                capturedImageUri=selectedImageUri
                Log.d(TAG, "Selected image URI: $selectedImageUri")
                // Handle the image URI (e.g., display it in an ImageView)
/*
                showImagePreview(selectedImageUri)
*/
                showCapturedPreview()
            } else {
                Log.e(TAG, "No image selected")
            }
        }
    }



    companion object {
        private const val TAG = "Camera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PERMISSION_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

}