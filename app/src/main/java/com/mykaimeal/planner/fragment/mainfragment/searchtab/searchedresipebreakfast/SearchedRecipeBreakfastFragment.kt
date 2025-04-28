package com.mykaimeal.planner.fragment.mainfragment.searchtab.searchedresipebreakfast

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterSearchedRecipeItem
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentSearchedRecipeBreakfastBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchedresipebreakfast.viewmodel.SearchedRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.Recipe
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SearchModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SearchModelData
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import com.mykaimeal.planner.model.DateModel
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SearchedRecipeBreakfastFragment : Fragment(), OnItemClickListener {

    private var _binding: FragmentSearchedRecipeBreakfastBinding? = null

    private val binding get() = _binding!!
    private var tvWeekRange: TextView? = null
    private var rcyChooseDaySch: RecyclerView? = null
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    private val dataList = arrayListOf<DataModel>()
    private var recipes: MutableList<Recipe> = mutableListOf()
    private var currentDate = Date() // Current date
    private var adapterSearchedRecipeItem: AdapterSearchedRecipeItem? = null
    private var recipeType: String? = null
    private var screenType: String? = null
    private var mealType: String? = null
    private var fullListMealType: MutableList<String> = mutableListOf()
    private var fullListDietType: MutableList<String> = mutableListOf()
    private var fullListCookTime: MutableList<String> = mutableListOf()
    private lateinit var searchedRecipeViewModel: SearchedRecipeViewModel
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> =
        mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentSearchedRecipeBreakfastBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.VISIBLE
            it.llBottomNavigation.visibility = View.VISIBLE
        }

        searchedRecipeViewModel = ViewModelProvider(this)[SearchedRecipeViewModel::class.java]

        screenType = arguments?.getString("screenType", "Search") ?: "Search"
        recipeType = arguments?.getString("recipeName", "") ?: ""
        if (!screenType.equals("Search",true)){
            if (!screenType.equals("Ingredients",true)){
                arguments?.let { bundle ->
                    val mealJson = bundle.getString("mealJsonArray")
                    val dietJson = bundle.getString("dietJsonArray")
                    val cookTimeJson = bundle.getString("cookTimeJsonArray")
                    fullListMealType = jsonArrayToList(mealJson) as MutableList
                    fullListDietType = jsonArrayToList(dietJson)as MutableList
                    fullListCookTime = jsonArrayToList(cookTimeJson)as MutableList
                }
            }

        }

        cookbookList.clear()

        val data = com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data(
            "", "", 0, "", "Favourites", 0, "", 0)
        cookbookList.add(0, data)

        if (!screenType.equals("Ingredients",true)){
            binding.tvSearchedTitle.text = recipeType.toString()
        }

        backHandle()

        initialize()


        if (searchedRecipeViewModel.data!=null){
            showDataInUi(searchedRecipeViewModel.data!!)
        }else{
            // This Api call when the screen in loaded
            launchApi()
        }


        binding.pullToRefresh.setOnRefreshListener {
            // This Api call when the screen in loaded
            launchApi()
        }


        return binding.root
    }


    private fun jsonArrayToList(jsonString: String?): List<String> {
        val list = mutableListOf<String>()
        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        }
        return list
    }

    private fun backHandle() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun launchApi() {
        if (BaseApplication.isOnline(requireContext())){
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                if (screenType.equals("Search",true) || screenType.equals("Ingredients",true)){
                    searchedRecipeViewModel.recipeSearchedApi({
                        binding.pullToRefresh.isRefreshing=false
                        BaseApplication.dismissMe()
                        handleApiSearchResponse(it)
                    }, recipeType)
                }else{
                    searchedRecipeViewModel.recipeFilterSearchApi({
                        BaseApplication.dismissMe()
                        binding.pullToRefresh.isRefreshing=false
                        handleApiSearchResponse(it)
                    }, fullListMealType,fullListDietType,fullListCookTime)
                }

            }
        }else{
            binding.pullToRefresh.isRefreshing=false
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun handleApiSearchResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> {
                handleSearchSuccessResponse(result.data.toString())
            }

            is NetworkResult.Error -> {
                showAlert(result.message, false)
                showNoData()
            }

            else -> {
                showAlert(result.message, false)
                showNoData()
            }
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSearchSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SearchModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                apiModel.data?.let { showDataInUi(it) }
            } else {
                showNoData()
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showNoData()
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

    private fun showDataInUi(searchModelData: SearchModelData) {
        try {
            searchedRecipeViewModel.setData(searchModelData)
            recipes.clear()
            searchModelData.recipes?.let {
                recipes.addAll(it)
            }
            if (recipes.size > 0) {
                binding.rcySearchedItem.visibility = View.VISIBLE
                binding.tvnoData.visibility = View.GONE
                adapterSearchedRecipeItem = AdapterSearchedRecipeItem(recipes, requireActivity(), this)
                binding.rcySearchedItem.adapter = adapterSearchedRecipeItem
            } else {
                showNoData()
            }
        } catch (e: Exception) {
            showNoData()
            Log.d("@@@@SearchFragment", "message:--" + e.message)
        }
    }


    private fun showNoData() {
        binding.rcySearchedItem.visibility = View.GONE
        binding.tvnoData.visibility = View.VISIBLE
    }

    private fun initialize() {

        binding.relBackSearched.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imgHeartRed.setOnClickListener {
            findNavController().navigate(R.id.cookBookFragment)
        }

        binding.imgBasketIcon.setOnClickListener {
            (activity as MainActivity?)?.upBasket()
            findNavController().navigate(R.id.basketScreenFragment)
        }

    }


    private fun chooseDayDialog(position: Int?) {
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
                chooseDayMealTypeDialog(position)
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


    private fun chooseDayMealTypeDialog(position: Int?) {
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
            updateSelection(ErrorMessage.Breakfast, tvBreakfast, allViews)
        }

        tvLunch.setOnClickListener {
            updateSelection(ErrorMessage.Lunch, tvLunch, allViews)
        }

        tvDinner.setOnClickListener {
            updateSelection(ErrorMessage.Dinner, tvDinner, allViews)
        }

        tvSnacks.setOnClickListener {
            updateSelection(ErrorMessage.Snacks, tvSnacks, allViews)
        }

        tvTeatime.setOnClickListener {
            updateSelection(ErrorMessage.Brunch, tvTeatime, allViews)
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


    private fun addToPlan(dialogChooseMealDay: Dialog, selectType: String, position: Int?) {
        // Map the type to the corresponding list and adapter
        // Create a JsonObject for the main JSON structure
        val jsonObject = JsonObject()

        // Safely get the item and position
        val item = recipes[position!!]
        if (item != null) {
            if (item.recipe?.uri != null) {
                jsonObject.addProperty("type", selectType)
                jsonObject.addProperty("uri", item.recipe.uri)
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
            searchedRecipeViewModel.recipeAddToPlanRequest({
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

    override fun itemClick(position: Int?, status: String?, type: String?) {
        when (status) {
            "1" -> {
                if ((activity as? MainActivity)?.Subscription_status==1){
                    if ((activity as? MainActivity)?.addmeal!! < 1){
                        chooseDayDialog(position)
                    }else{
                        (activity as? MainActivity)?.subscriptionAlertError()
                    }
                }else{
                    chooseDayDialog(position)
                }

            }

            "2" -> {
                mealType = if (screenType=="Search"){
                    recipeType
                }else{
                    type
                }
                if (BaseApplication.isOnline(requireActivity())) {
                    toggleIsLike(position, "basket")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }

            "4" -> {
                if ((activity as? MainActivity)?.Subscription_status==1){
                    if ((activity as? MainActivity)?.favorite!! <=2){
                        if (BaseApplication.isOnline(requireActivity())) {
                            toggleIsLike(position, "like")
                        } else {
                            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                        }
                    }else{
                        (activity as? MainActivity)?.subscriptionAlertError()
                    }
                }else{
                    if (BaseApplication.isOnline(requireActivity())) {
                        toggleIsLike(position, "like")
                    } else {
                        BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                    }
                }

            }

            else -> {
                val bundle = Bundle().apply {
                    putString("uri", type)
                    putString("mealType", status)
                }
                findNavController().navigate(R.id.recipeDetailsFragment, bundle)
            }
        }
    }

    private fun toggleIsLike(position: Int?, apiType: String) {
        // Map the type to the corresponding list and adapter
        // Safely get the item and position
        val item = recipes[position!!]
        if (item != null) {
            if (item.recipe?.uri != null) {
                if (apiType.equals("basket", true)) {
                    addBasketData(item.recipe.uri)
                } else {
                    val newLikeStatus = if (item.is_like == 0) "1" else "0"
                    if (newLikeStatus.equals("0", true)) {
                        recipeLikeAndUnlikeData(item, recipes, position, newLikeStatus, "", null)
                    } else {
                        addFavTypeDialog(item, recipes, position, newLikeStatus)
                    }

                }
            }
        }
    }


    private fun addFavTypeDialog(
        item: Recipe?,
        mealList: MutableList<Recipe>?,
        position: Int?,
        likeType: String
    ) {
        val dialogAddRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogAddRecipe.setContentView(R.layout.alert_dialog_add_recipe)
        dialogAddRecipe.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogAddRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogAddRecipe.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        spinnerActivityLevel = dialogAddRecipe.findViewById(R.id.spinnerActivityLevel)
        val relCreateNewCookBook =
            dialogAddRecipe.findViewById<RelativeLayout>(R.id.relCreateNewCookBook)
        val imgCheckBoxOrange = dialogAddRecipe.findViewById<ImageView>(R.id.imgCheckBoxOrange)

        spinnerActivityLevel.setItems(cookbookList.map { it.name })

        dialogAddRecipe.show()
        dialogAddRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        getCookBookList()

        relCreateNewCookBook.setOnClickListener {
            relCreateNewCookBook.setBackgroundResource(R.drawable.light_green_rectangular_bg)
            imgCheckBoxOrange.setImageResource(R.drawable.orange_uncheck_box_images)
            dialogAddRecipe.dismiss()
            val bundle = Bundle()
            bundle.putString("value", "New")
            bundle.putString("uri", item?.recipe?.uri)
            findNavController().navigate(R.id.createCookBookFragment, bundle)
        }


        rlDoneBtn.setOnClickListener {
            if (spinnerActivityLevel.text.toString().equals("", true)) {
                BaseApplication.alertError(requireContext(), ErrorMessage.selectCookBookError, false)
            } else {
                val cookbooktype = cookbookList[spinnerActivityLevel.selectedIndex].id
                recipeLikeAndUnlikeData(
                    item, mealList, position, likeType, cookbooktype.toString(), dialogAddRecipe
                )
            }
        }

    }

    private fun getCookBookList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            searchedRecipeViewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }
        }
    }

    private fun handleApiCookBookResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
                    cookbookList.retainAll { it == cookbookList[0] }
                    cookbookList.addAll(apiModel.data)
                    // OR directly modify the original list
                    spinnerActivityLevel.setItems(cookbookList.map { it.name })
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

    private fun recipeLikeAndUnlikeData(
        item: Recipe?,
        mealList: MutableList<Recipe>?,
        position: Int?,
        likeType: String,
        cookbooktype: String,
        dialogAddRecipe: Dialog?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            searchedRecipeViewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it, item, mealList, position, dialogAddRecipe)
            }, item?.recipe?.uri!!, likeType, cookbooktype)
        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        item: Recipe?,
        mealList: MutableList<Recipe>?,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(
                result.data.toString(),
                item,
                mealList,
                position,
                dialogAddRecipe
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(
        data: String,
        item: Recipe?,
        mealList: MutableList<Recipe>?,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogAddRecipe?.dismiss()
                // Toggle the is_like value
                item!!.is_like = if (item.is_like == 0) 1 else 0
                mealList!![position!!] = item
                // Update the adapter
                adapterSearchedRecipeItem?.updateList(mealList)
                (activity as MainActivity?)?.upDateHomeData()
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun addBasketData(uri: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            searchedRecipeViewModel.addBasketRequest({
                BaseApplication.dismissMe()
                handleBasketApiResponse(it)
            }, uri, "",mealType.toString())
        }
    }

    private fun handleBasketApiResponse(
        result: NetworkResult<String>
    ) {
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
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        searchedRecipeViewModel.setData(null)
    }

}