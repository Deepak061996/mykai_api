package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterCookIngredientsItem
import com.mykaimeal.planner.adapter.AdapterCreateIngredientsItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.commonworkutils.MediaUtility
import com.mykaimeal.planner.commonworkutils.UriToBase64
import com.mykaimeal.planner.commonworkutils.imageUrlToBase64
import com.mykaimeal.planner.databinding.FragmentCreateRecipeBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.CreateRecipeNameModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.CreateRecipeNameModelData
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.CreateRecipeSuccessModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.Recipe
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.RecyclerViewCookIngModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.viewmodel.CreateRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefromimage.model.RecyclerViewItemModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class CreateRecipeFragment : Fragment(), AdapterCreateIngredientsItem.UploadImage {

    private lateinit var binding: FragmentCreateRecipeBinding
    private var file: File? = null
    private var ingredientList: MutableList<RecyclerViewItemModel> =  mutableListOf()
    private var cookList: MutableList<RecyclerViewCookIngModel> = mutableListOf()
    private var adapter: AdapterCreateIngredientsItem? = null
    private var position: Int = 0
    private var checkBase64Url:Boolean?=false
    private var recipeMainImageUri: String? = null
    private var recipeStatus: String? = "0"
    private var adapterCook: AdapterCookIngredientsItem? = null
    private lateinit var createRecipeViewModel: CreateRecipeViewModel
    private lateinit var commonWorkUtils: CommonWorkUtils
    private var recipeName:String?=""
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> =
        mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentCreateRecipeBinding.inflate(layoutInflater, container, false)
        
        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.GONE
            it.llBottomNavigation.visibility = View.GONE
        }

        commonWorkUtils = CommonWorkUtils(requireActivity())

        createRecipeViewModel = ViewModelProvider(requireActivity())[CreateRecipeViewModel::class.java]

        recipeName = arguments?.getString("name", "")?:""


        backButton()

        initialize()

        return binding.root
    }


    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    addRecipeDiscardDialog()
                }
            })
    }


    private val pickImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                file = MediaUtility.getPath(requireContext(), uri)?.let { File(it) }
                /*processImage(bitmap)*/
                // Now you can send the image URI to Vision API for processing
                // Convert image to Base64
                binding.addImageIcon.visibility = View.GONE
                checkBase64Url=true
                recipeMainImageUri = UriToBase64(requireActivity(), uri)
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(binding.addImages)
            }
        }
    }

    private fun initialize() {

        cookbookList.clear()
        val data = com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("", "", 0, "", "Favorites", 0, "", 0)
        cookbookList.add(0, data)


        // Add the first blank EditText item
        ingredientList.add(RecyclerViewItemModel("", "", false,"",""))

        // Set up RecyclerView and Adapter
        adapter = AdapterCreateIngredientsItem(ingredientList, requireActivity(), this){ updatedPosition, updatedItem ->
            updatedItem.status = updatedItem.ingredientName?.isNotBlank() == true &&
                    updatedItem.quantity?.isNotBlank() == true &&
                    updatedItem.measurement?.isNotBlank() == true
            ingredientList[updatedPosition] = updatedItem
           binding.rcyCreateIngredients.adapter?.notifyItemChanged(updatedPosition)
        }
        binding.rcyCreateIngredients.adapter = adapter

        // Ingredients Handle "+" button click
        binding.imageCrtIngPlus.setOnClickListener {
            var result = true // Default to true, assuming all values are filled

            // Iterate through each item in the ingredientList and check if all values are filled
            ingredientList.forEachIndexed { _, item ->
                if (item.ingredientName?.isBlank() == true ||
                    item.quantity?.isBlank() == true ||
                    item.measurement?.isBlank() == true) {
                    // If any field is blank in the current position, set result to false
                    result = false
                    // Show a toast message indicating which position has missing values
                    Toast.makeText(requireContext(), ErrorMessage.ingredientInstructions, Toast.LENGTH_LONG).show()
                    return@forEachIndexed // Exit early after finding the first invalid item
                }
            }
            // If all values are filled, add a new ingredient; otherwise, do nothing
            if (result) {
                ingredientList.add(RecyclerViewItemModel("", "", false, "", ""))
                adapter?.update(ingredientList)
            }
        }

        // Update the model when quantity changes
        binding.etRecipeName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateBackground(binding.llCreateTitle, s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Initialize with Step-1 by default
        cookList.add(RecyclerViewCookIngModel(1))

        // Setup RecyclerView
        adapterCook = AdapterCookIngredientsItem(cookList,requireActivity()){ updatedPosition, updatedItem ->
            cookList[updatedPosition] = updatedItem
            binding.rcyCookInstructions.adapter?.notifyItemChanged(updatedPosition)
        }
        binding.rcyCookInstructions.adapter=adapterCook

        binding.imageCookIns.setOnClickListener {
            // Check if any item has a blank or null description
            val hasEmptyField = cookList.any { it.description.isNullOrBlank() }
            if (hasEmptyField) {
                Toast.makeText(requireContext(), ErrorMessage.validCookingInstructions, Toast.LENGTH_LONG).show()
            } else {
                // All descriptions are filled, so add a new step
                cookList.add(RecyclerViewCookIngModel(1)) // You can update '1' to meaningful data if needed
                adapterCook?.update(cookList)
            }
        }


        // serving count - and +
        binding.imgMinus.setOnClickListener {
            val currentValue = binding.textValue.text.toString().toInt()
            if (currentValue > 1) {
                updateValue(currentValue - 1)
            }
        }

        binding.imgPlus.setOnClickListener {
            val currentValue = binding.textValue.text.toString().toInt()
            if (currentValue < 99) {
                updateValue(currentValue + 1)
            }
        }

        // backButton handle
        binding.relBacks.setOnClickListener {
            addRecipeDiscardDialog()
        }

        // save button handle
        binding.layBottom.setOnClickListener {
            if (validate()) {
                if (BaseApplication.isOnline(requireActivity())) {
                    createRecipeApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        // Private button handle
        binding.textPrivate.setOnClickListener {
            radioButton(true)
        }

        // Public button handle
        binding.textPublic.setOnClickListener {
            radioButton(false)
        }

        // add Image handle
        binding.addImages.setOnClickListener {
            openCameraGallery(false)
        }

        // cookBookList
        if (BaseApplication.isOnline(requireActivity())) {
            getCookBookList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

    }

    private fun radioButton(type: Boolean) {
        if (type){
            recipeStatus="0"
            binding.textPublic.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_uncheck_gray_icon, 0, 0, 0)
            binding.textPrivate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_check_icon, 0, 0, 0)
        }else{
            binding.textPublic.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_check_icon, 0, 0, 0)
            binding.textPrivate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_uncheck_gray_icon, 0, 0, 0)
            recipeStatus="1"
        }
    }

    private fun validate(): Boolean {

        var result = true // Default to true, assuming all values are filled

        // Iterate through each item in the ingredientList and check if all values are filled
        ingredientList.forEachIndexed { _, item ->
            if (item.ingredientName?.isBlank() == true ||
                item.quantity?.isBlank() == true ||
                item.measurement?.isBlank() == true) {
                // If any field is blank in the current position, set result to false
                result = false
                return@forEachIndexed // Exit early after finding the first invalid item
            }
        }

        val hasEmptyField = cookList.any { it.description.isNullOrBlank() }

        if (binding.etRecipeName.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.recipeName, false)
            return false
        } else if (!result) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.ingredientInstructions, false)
            return false
        } else if (hasEmptyField) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.validCookingInstructions, false)
            return false
        }else if (binding.edtTotalTime.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.validTotalTime, false)
            return false
        }
        return true
    }

    private fun updateBackground(llCreateTitle: LinearLayout, text: String) {
        if (text.isNotEmpty()) {
            llCreateTitle.setBackgroundResource(R.drawable.create_select_bg) // Change this drawable
        } else {
            llCreateTitle.setBackgroundResource(R.drawable.create_unselect_bg)  // Default background
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun createRecipeApi() {
        lifecycleScope.launch {
            try {
                if (!checkBase64Url!!) {
                    checkBase64Url=true
                    recipeMainImageUri = imageUrlToBase64(recipeMainImageUri!!)
                }


                val jsonObject = JsonObject()
                Log.d("fdfdf", "ffd:--0" + ingredientList.size)
                // Create a JsonArray for ingredients
                val ingArray = JsonArray()
                val prepArray = JsonArray()
                // Extract required fields dynamically
                ingredientList.forEach { item ->
                    /* val ingredientString = "${item.ingredientName},${item.quantity},${item.measurement}"*/
                    ingArray.add(item.ingredientName)
                }
                // Prepare prep steps
                cookList.forEach { items ->
                    prepArray.add(items.description)
                }
                var cookBookID = ""
                if (binding.spinnerCookBook.text.toString().isNotEmpty()) {
                    cookBookID = cookbookList[binding.spinnerCookBook.selectedIndex].id.toString()
                }
                // Add data to JSON object
                jsonObject.addProperty("recipe_key", recipeStatus.toString())
                jsonObject.addProperty("cook_book", cookBookID)
                jsonObject.addProperty("title", binding.etRecipeName.text.toString().trim())
                jsonObject.add("ingr", ingArray)
                jsonObject.addProperty("summary", binding.edtSummary.text.toString().trim())
                jsonObject.addProperty("yield", binding.textValue.text.toString().trim())
                jsonObject.addProperty("totalTime", binding.edtTotalTime.text.toString().trim())
                jsonObject.add("prep", prepArray)
                jsonObject.addProperty("img", recipeMainImageUri ?: "")  // Ensure it's not null
//            jsonObject.addProperty("tags", binding.etRecipeName.text.toString().trim())
                Log.d("json object", "******$jsonObject")
                BaseApplication.showMe(requireContext())
                // Call API after everything is ready
                createRecipeViewModel.createRecipeRequestApi({
                    BaseApplication.dismissMe()
                    handleApiCreateRecipeResponse(it)
                }, jsonObject)
            }catch (e:Exception){
                Log.d("@Error ","*********"+e.message)
            }
        }
    }

    private fun handleApiCreateRecipeResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCreateApiResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun getCookBookList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            createRecipeViewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }
        }
    }


    private fun searchRecipeByNameApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            createRecipeViewModel.recipeSearchApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val recipeNameModel = gson.fromJson(it.data, CreateRecipeNameModel::class.java)
                            if (recipeNameModel.code == 200 && recipeNameModel.success == true) {
                                showDataInUi(recipeNameModel.data)
                            } else {
                                if (recipeNameModel.code == ErrorMessage.code) {
                                    showAlert(recipeNameModel.message, true)
                                }else{
                                    showAlert(recipeNameModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("CreateRecipe:","Message:--"+e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlert(it.message, false)
                    }
                    else -> {
                        showAlert(it.message, false)
                    }
                }
            },recipeName)
        }
    }

    private fun showDataInUi(recipeNameModelData: List<CreateRecipeNameModelData>?) {
        try {
            if (recipeNameModelData!=null){
                val recipe : Recipe? =recipeNameModelData[0].recipe
                if (recipe!=null){
                    if (recipe.label !=null){
                        binding.etRecipeName.setText(recipe.label.toString())
                    }
                    if (recipe.images?.SMALL?.url!=null){
                        val imageUrl = recipe.images.SMALL.url
                        recipeMainImageUri = recipe.images.SMALL.url
                        checkBase64Url=false
                        Glide.with(requireActivity())
                            .load(imageUrl)
                            .error(R.drawable.no_image)
                            .placeholder(R.drawable.no_image)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    binding.layProgess.root.visibility= View.GONE
                                    binding.addImageIcon.visibility= View.VISIBLE
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    binding.layProgess.root.visibility= View.GONE
                                    binding.addImageIcon.visibility= View.GONE
                                    return false
                                }
                            })
                            .into(binding.addImages)
                    }else{
                        binding.layProgess.root.visibility= View.GONE
                        binding.addImageIcon.visibility= View.VISIBLE
                    }

                    if (recipe.instructionLines!=null){
                        // Map the instruction lines to RecyclerViewCookIngModel
                        cookList = recipe.instructionLines.mapIndexed { index, instruction ->
                            RecyclerViewCookIngModel(
                                count = index + 1,
                                description = instruction,
                                status = false
                            )
                        }.toMutableList()
                        adapterCook!!.update(cookList)
                    }

                    if (recipe.totalTime!=null && recipe.totalTime!=0){
                        binding.edtTotalTime.setText(recipe.totalTime.toString())
                    }

                    if (recipe.ingredients!=null){
                        // Map the response values to your IngredientModel list
                        ingredientList = recipe.ingredients.map { response ->
                            if (!response.food.toString().equals("",true) && !response.quantity.toString().equals("",true)){
                                RecyclerViewItemModel(uri = response.image, ingredientName = response.food.toString(), quantity = response.quantity.toString(), measurement = response.measure.toString(), status = true)
                            }else{
                                RecyclerViewItemModel(uri = response.image, ingredientName = response.food.toString(), quantity = response.quantity.toString(), measurement = response.measure.toString(), status = false)
                            }
                         }.toMutableList()
                        adapter?.update(ingredientList)
                    }
                }
            }
        }catch (e:Exception){
            Log.d("CreateRecipe:","Message:--"+e.message)
        }
    }

    private fun handleApiCookBookResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }

        if (recipeName!=""){
            if (BaseApplication.isOnline(requireActivity())) {
                searchRecipeByNameApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
              /*      binding.spinnerCookBook.setItems(cookbookList.map { it.name })*/
                    cookbookList.retainAll { it == cookbookList[0] }
                    cookbookList.addAll(apiModel.data)

                }
                // OR directly modify the original list
                binding.spinnerCookBook.setItems(cookbookList.map { it.name })
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

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    private fun handleSuccessCreateApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CreateRecipeSuccessModel::class.java)
                 Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                     addRecipeSuccessDialog()
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

    @SuppressLint("SetTextI18n")
    private fun updateValue(value:Int) {
        binding.textValue.text =""+value
    }

    private fun addRecipeDiscardDialog() {
        val dialogDiscard: Dialog = context?.let { Dialog(it) }!!
        dialogDiscard.setContentView(R.layout.alert_dialog_discard_recipe)
        dialogDiscard.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogDiscard.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogYesBtn = dialogDiscard.findViewById<TextView>(R.id.tvDialogYesBtn)
        val tvDialogNoBtn = dialogDiscard.findViewById<TextView>(R.id.tvDialogNoBtn)
        dialogDiscard.show()
        dialogDiscard.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogYesBtn.setOnClickListener {
            dialogDiscard.dismiss()
            findNavController().navigateUp()
        }

        tvDialogNoBtn.setOnClickListener {
            dialogDiscard.dismiss()
        }
    }

    private fun addRecipeSuccessDialog() {
        val dialogSuccess: Dialog = context?.let { Dialog(it) }!!
        dialogSuccess.setContentView(R.layout.alert_dialog_add_recipe_success)
        dialogSuccess.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogSuccess.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlOkayBtn = dialogSuccess.findViewById<RelativeLayout>(R.id.rlOkayBtn)
        dialogSuccess.show()
        dialogSuccess.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        rlOkayBtn.setOnClickListener {
            dialogSuccess.dismiss()
            findNavController().navigate(R.id.planFragment)
        }
    }

    override fun uploadImage(pos: Int) {
        this.position = pos
        openCameraGallery(true)
    }


    private fun openCameraGallery(type:Boolean){
        if (type){
            ImagePicker.with(requireActivity())
                .crop() // Crop image (Optional)
                .compress(1024 * 5) // Compress the image to less than 5 MB
                .maxResultSize(250, 250) // Set max resolution
                .createIntent { intent -> pickImageLauncher1.launch(intent) }
        }else{
            ImagePicker.with(requireActivity())
                .crop() // Crop image (Optional)
                .compress(1024 * 5) // Compress the image to less than 5 MB
                .maxResultSize(250, 250) // Set max resolution
                .createIntent { intent -> pickImageLauncher.launch(intent) }
        }
    }

    private val pickImageLauncher1: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                result.data?.data?.let { uri ->
                    val localData=ingredientList[position]
                    localData.uri=uri.toString()
                    localData.status=false
                    ingredientList[position] = localData
                    adapter?.update(ingredientList)
                }
            }catch (e:Exception){
                BaseApplication.alertError(requireContext(), e.message, false)
            }
        }
    }


}