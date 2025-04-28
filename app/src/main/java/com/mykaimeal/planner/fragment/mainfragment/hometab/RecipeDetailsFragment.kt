package com.mykaimeal.planner.fragment.mainfragment.hometab

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterRecipeItem
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.adapter.IngredientsRecipeAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentRecipeDetailsBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.RecipeDetailsViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.RecipeDetailsApiResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import com.mykaimeal.planner.model.DateModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class RecipeDetailsFragment : Fragment(), OnItemSelectListener {

    private lateinit var binding: FragmentRecipeDetailsBinding
    private var ingredientsRecipeAdapter: IngredientsRecipeAdapter? = null
    private var adapterRecipeItem: AdapterRecipeItem? = null
    val dataList = ArrayList<DataModel>()
    private var tvWeekRange: TextView? = null
    private var rcyChooseDaySch: RecyclerView? = null
    private var selectAll: Boolean = false
    private var quantity: Int = 1
    private lateinit var viewModel: RecipeDetailsViewModel
    private var uri: String = ""
    private var mealType: String = ""
    private var currentDate = Date() // Current date

    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRecipeDetailsBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[RecipeDetailsViewModel::class.java]

        uri = arguments?.getString("uri", "")?:""
        mealType = arguments?.getString("mealType", "")?:""

        Log.d("@@@@@ ERROR", "uri :- $uri")

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        setupBackNavigation()
        
        initialize()

        // When screen load then api call
        fetchDataOnLoad()

        return binding.root
    }

    private fun fetchDataOnLoad() {
        if (BaseApplication.isOnline(requireActivity())) {
            fetchRecipeDetailsData()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun fetchRecipeDetailsData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.recipeDetailsRequest({
                BaseApplication.dismissMe()
                handleApiResponse(it)
            }, uri)
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleBasketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessBasketResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessBasketResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                (activity as MainActivity?)?.upDateHomeData()
                findNavController().navigateUp()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
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

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, RecipeDetailsApiResponse::class.java)
            Log.d("@@@ Recipe Details", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Log.d("@@@ Recipe Detailsssss", "message :- $apiModel")
                if (apiModel.data != null && apiModel.data.size > 0) {
                    showData(apiModel.data)
                } else {
                    binding.layBottom.visibility = View.GONE
                    binding.webView.visibility = View.GONE
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showData(data: MutableList<Data>) {
        /*localData.clear()
        localData.addAll(data)*/

        viewModel.setRecipeData(data)
        if (viewModel.getRecipeData()?.get(0)!!.recipe?.images?.SMALL?.url != null) {
            Glide.with(requireContext())
                .load(viewModel.getRecipeData()?.get(0)!!.recipe?.images?.SMALL?.url)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.layProgess.root.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.layProgess.root.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.imageData)
        } else {
            binding.layProgess.root.visibility = View.GONE
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.source!=null){
            binding.tvSourcesName.text="By "+ viewModel.getRecipeData()?.get(0)!!.recipe?.source
        }

        if (viewModel.getRecipeData()?.get(0)!!.review!=null){
            binding.tvRating.text = ""+viewModel.getRecipeData()?.get(0)!!.review+"("+viewModel.getRecipeData()?.get(0)!!.review_number+")"
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.label != null) {
            binding.tvTitle.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.label
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.calories != null) {
            binding.tvCalories.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.calories?.toInt()
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.totalNutrients?.FAT?.quantity != null) {
            binding.tvFat.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.totalNutrients?.FAT?.quantity?.toInt()
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.totalNutrients?.PROCNT?.quantity != null) {
            binding.tvProtein.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.totalNutrients?.PROCNT?.quantity?.toInt()
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.totalNutrients?.CHOCDF?.quantity != null) {
            binding.tvCarbs.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.totalNutrients?.CHOCDF?.quantity?.toInt()
        }


        if (viewModel.getRecipeData()?.get(0)!!.recipe?.totalTime != null) {
            binding.tvTotaltime.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.totalTime + " min "
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients != null && viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients!!.size > 0) {
            ingredientsRecipeAdapter = IngredientsRecipeAdapter(viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients, requireActivity(), this)
            binding.rcyIngCookWareRecipe.adapter = ingredientsRecipeAdapter
            binding.layBottom.visibility = View.VISIBLE
        } else {
            binding.layBottom.visibility = View.GONE
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.instructionLines != null && viewModel.getRecipeData()?.get(0)!!.recipe?.instructionLines!!.size > 0) {
            adapterRecipeItem = AdapterRecipeItem(viewModel.getRecipeData()?.get(0)!!.recipe?.instructionLines!!, requireActivity())
            binding.layBottom.visibility = View.VISIBLE
        } else {
            binding.layBottom.visibility = View.GONE
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }


    private fun initialize() {

        binding.imgPlusValue.setOnClickListener {
            if (binding.tvValues.text.toString().toInt() < 99) {
                val data=binding.tvValues.text.toString().toInt()+1
                updateValue(data.toString())
            }
        }

        binding.imgMinusValue.setOnClickListener {
            if (binding.tvValues.text.toString().toInt()  > 1) {
                val data=binding.tvValues.text.toString().toInt()-1
                updateValue(data.toString())
            } else {
                Toast.makeText(requireActivity(), ErrorMessage.servingError, Toast.LENGTH_LONG).show()
            }
        }

        binding.tvAddToPlan.setOnClickListener {
            if ((activity as? MainActivity)?.Subscription_status==1){
                if ((activity as? MainActivity)?.addmeal!! < 1){
                    // Safely get the item and position
                    chooseDayDialog()
                }else{
                    (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                }
            }else{
                chooseDayDialog()
            }
        }

        binding.relBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.llIngredients.setOnClickListener {
            binding.textIngredients.setBackgroundResource(R.drawable.select_bg)
            binding.textCookWare.setBackgroundResource(R.drawable.unselect_bg)
            binding.textRecipe.setBackgroundResource(R.drawable.unselect_bg)

            binding.textIngredients.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textCookWare.setTextColor(Color.parseColor("#3C4541"))
            binding.textRecipe.setTextColor(Color.parseColor("#3C4541"))

            binding.relRecipe.visibility = View.GONE
            binding.textStepInstructions.visibility = View.GONE
            binding.relTittleList.visibility = View.VISIBLE
            binding.relServingsPeople.visibility = View.VISIBLE
            binding.layBottomPlanBasket.visibility = View.VISIBLE
            binding.relIngSelectAll.visibility = View.VISIBLE
            binding.webView.visibility = View.GONE

            if (viewModel.getRecipeData()?.size!! > 0) {
                // Update the drawable based on the selectAll state
                val drawableRes =
                    if (selectAll) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
                binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
                // Notify adapter with updated data
                ingredientsRecipeAdapter?.updateList(viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients!!)
                binding.rcyIngCookWareRecipe.adapter = ingredientsRecipeAdapter
                binding.layBottom.visibility = View.VISIBLE
            }else{
                binding.layBottom.visibility = View.GONE
            }
        }

        binding.llCookWare.setOnClickListener {
            binding.textIngredients.setBackgroundResource(R.drawable.unselect_bg)
            binding.textCookWare.setBackgroundResource(R.drawable.select_bg)
            binding.textRecipe.setBackgroundResource(R.drawable.unselect_bg)
            binding.textIngredients.setTextColor(Color.parseColor("#3C4541"))
            binding.textCookWare.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textRecipe.setTextColor(Color.parseColor("#3C4541"))
            binding.layBottom.visibility = View.GONE
            loadUrl()
        }

        binding.llRecipe.setOnClickListener {
            binding.textIngredients.setBackgroundResource(R.drawable.unselect_bg)
            binding.textCookWare.setBackgroundResource(R.drawable.unselect_bg)
            binding.textRecipe.setBackgroundResource(R.drawable.select_bg)
            binding.textIngredients.setTextColor(Color.parseColor("#3C4541"))
            binding.textCookWare.setTextColor(Color.parseColor("#3C4541"))
            binding.textRecipe.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textStepInstructions.visibility = View.VISIBLE
            binding.relRecipe.visibility = View.VISIBLE
            binding.relTittleList.visibility = View.VISIBLE
            binding.relServingsPeople.visibility = View.GONE
            binding.relIngSelectAll.visibility = View.GONE
            binding.relCookware.visibility = View.GONE
            binding.layBottomPlanBasket.visibility = View.GONE
            if (viewModel.getRecipeData()?.size!!  > 0) {
                binding.rcyIngCookWareRecipe.adapter = adapterRecipeItem
                binding.layBottom.visibility = View.VISIBLE
            }else{
                binding.layBottom.visibility = View.GONE
            }
        }

        binding.textStepInstructions.setOnClickListener {
            if (viewModel.getRecipeData()!=null){
                if (viewModel.getRecipeData()?.get(0)!!.recipe?.instructionLines!!.size > 0) {
                    val bundle=Bundle()
                    bundle.putString("uri",uri)
                    bundle.putString("mealType",mealType)
                    findNavController().navigate(R.id.directionSteps1RecipeDetailsFragment,bundle)
                }
            }
        }

        binding.tvSelectAllBtn.setOnClickListener {
            if (viewModel.getRecipeData()?.size!!>0) {
                selectAll = !selectAll // Toggle the selectAll value
                // Update the drawable based on the selectAll state
                val drawableRes = if (selectAll) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
                binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)

                // Update the status of each ingredient dynamically
                viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.forEach { ingredient ->
                    ingredient.status = selectAll
                }
                // Notify adapter with updated data
                ingredientsRecipeAdapter?.updateList(viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients!!)
            }
        }

        binding.layBasket.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (viewModel.getRecipeData()?.size!!> 0) {
                    try {
                        var status=false
                        // Create a JsonArray for ingredients
                        val jsonArray = JsonArray()
                        // Iterate through the ingredients and add them to the array if status is true
                        viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.forEach { ingredientsModel ->
                            if (ingredientsModel.status) {
                                // Create a JsonObject for each ingredient
                                val ingredientObject = JsonObject()
                                ingredientObject.addProperty("name", ingredientsModel.text)
                                ingredientObject.addProperty("image", ingredientsModel.image)
                                ingredientObject.addProperty("food", ingredientsModel.food)
//                                ingredientObject.addProperty("quantity", ingredientsModel.quantity)
                                ingredientObject.addProperty("quantity", "1")
                                ingredientObject.addProperty("foodCategory", ingredientsModel.foodCategory)
                                ingredientObject.addProperty("measure", ingredientsModel.measure)
                                ingredientObject.addProperty("food_id", ingredientsModel.foodId)
                                ingredientObject.addProperty("status", "0")
                                // Add the ingredient object to the array
                                jsonArray.add(ingredientObject)
                                status=true
                            }
                        }
                        if (status){
                            // Create a JsonObject for the main JSON structure
                            val jsonObject = JsonObject()
                            jsonObject.addProperty("serving", binding.tvValues.text.toString())
                            // Add the ingredients array to the main JSON object
                            jsonObject.add("ingredients", jsonArray)
                            // Log the final JSON data
                            Log.d("final data", "******$jsonObject")
                            addBasketDetailsApi(jsonObject)
                        }else{
                            BaseApplication.alertError(requireContext(), ErrorMessage.ingredientError, false)
                        }

                    } catch (e: Exception) {
                        BaseApplication.alertError(requireContext(), e.message, false)
                    }
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun addBasketDetailsApi(jsonObject: JsonObject) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.recipeAddBasketRequest({
                BaseApplication.dismissMe()
                handleBasketApiResponse(it)
            }, jsonObject)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUrl() {
        binding.webView.visibility = View.VISIBLE
        if (viewModel.getRecipeData()?.size!!  > 0) {
            val webSettings: WebSettings = binding.webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webSettings.loadsImagesAutomatically = true
            webSettings.javaScriptCanOpenWindowsAutomatically = true
            webSettings.allowContentAccess = true
            webSettings.allowFileAccess = true
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            /*// Set WebViewClient to handle page loading within the WebView
            binding.webView.webViewClient = WebViewClient()*/

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

            // Load the URL if it is not null or empty
            val url = viewModel.getRecipeData()?.get(0)!!.recipe?.url?.replace("http:", "https:")
            Log.d("url", "****$url")

            binding.webView.loadUrl("https://www.google.com/")
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateValue(data: String) {
        binding.tvValues.text = data
    }

    @SuppressLint("SetTextI18n")
    private fun chooseDayDialog() {
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
            if (status){
                chooseDayMealTypeDialog()
                dialogChooseDay.dismiss()
            }else{
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
            var status=false
            updatedDaysBetween1.forEach {
                status = it.date >= BaseApplication.currentDateFormat().toString()
            }
            if (status){
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
                currentDate = calendar.time
                // Display next week dates
                println("\nAfter clicking 'Next':")
                showWeekDates()
            }else{
                Toast.makeText(requireContext(),ErrorMessage.slideError,Toast.LENGTH_LONG).show()
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

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
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
//            dateList.add("$dayName, $date")
            dateList.add(localDate)


            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateList
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
        println("Days between $startDate and ${endDate}:")
        daysBetween.forEach { println(it) }
        tvWeekRange?.text = "" + formatDate(startDate) + "-" + formatDate(endDate)

    }


    private fun chooseDayMealTypeDialog() {
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
                if (type.equals("",true)){
                    BaseApplication.alertError(requireContext(), ErrorMessage.mealTypeError, false)
                }else {
                    addToPlan(dialogChooseMealDay, type)
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }


    @SuppressLint("DefaultLocale")
    private fun addToPlan(dialogChooseMealDay: Dialog, selectType: String) {

        // Create a JsonObject for the main JSON structure
        val jsonObject = JsonObject()
        if (uri != null) {
            jsonObject.addProperty("type", selectType)
            jsonObject.addProperty("uri", uri)
            jsonObject.addProperty("serving", binding.tvValues.text.toString())
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
        Log.d("json object ", "******$jsonObject")

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
                dialogChooseMealDay.dismiss()
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {

        viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.forEachIndexed { index, ingredient ->
            if (index == position) {
                ingredient.status = viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.get(position)?.status != true
            }
        }
        // Notify adapter with updated data
        ingredientsRecipeAdapter?.updateList(viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients!!)

        selectAll = viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.all { it.status } == true

        // Update the drawable based on the selectAll state
        val drawableRes = if (selectAll) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
        binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)

    }

}