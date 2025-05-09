package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics

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
import android.widget.CalendarView
import android.widget.ImageButton
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
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterOrderHistoryGraph
import com.mykaimeal.planner.adapter.AdapterStatisticsWeekItem
import com.mykaimeal.planner.adapter.CalendarDayDateWeekAdapter
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentStatisticsWeekYearBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.Breakfast
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsWeekYearModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsWeekYearModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel.StatisticsViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import com.mykaimeal.planner.model.DateModel
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class StatisticsWeekYearFragment : Fragment(),OnItemClickListener, OnItemClickedListener {

    private lateinit var binding: FragmentStatisticsWeekYearBinding

    private var rcyChooseDaySch: RecyclerView? = null
    private var tvWeekRange: TextView? = null
    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private lateinit var startDatePlan: Date
    private lateinit var endDatePlan: Date
    private lateinit var statisticsViewModel: StatisticsViewModel
    private var updatedDaysBetween: List<DateModel> = emptyList()
    private var calendarAdapter: CalendarDayDateWeekAdapter? = null
    private var currentDate = Date() // Current date

    private var recipesModel: StatisticsWeekYearModelData? = null
    // Separate adapter instances for each RecyclerView
    private var breakfastAdapter: AdapterStatisticsWeekItem? = null
    private var lunchAdapter: AdapterStatisticsWeekItem? = null
    private var dinnerAdapter: AdapterStatisticsWeekItem? = null
    private var snackesAdapter: AdapterStatisticsWeekItem? = null
    private var teaTimeAdapter: AdapterStatisticsWeekItem? = null
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    val dataList = arrayListOf<DataModel>()
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    private var adapterOrderHistoryItem: AdapterOrderHistoryGraph? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding=FragmentStatisticsWeekYearBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]

        backButton()

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


        statisticsViewModel.currentDateList?.let {
            currentDate=it
        }

        showWeekDates("1")

        initialize()

        return binding.root
    }

    private fun loadWeekListApi(){
        if (BaseApplication.isOnline(requireContext())) {
            getStatWeekList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun getStatWeekList() {
        BaseApplication.showMe(requireContext())
        val daysBetween = getDaysBetween(startDate, endDate)
        val firstDay = daysBetween.first()
        val lastDay = daysBetween.last()
        lifecycleScope.launch {
            statisticsViewModel.orderWeekUrl({
                BaseApplication.dismissMe()
                handleApiWeekGraphResponse(it)
            }, firstDay.date,lastDay.date,statisticsViewModel.dataCurrentYear)
        }
    }


    private fun handleApiWeekGraphResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessGraphWeekResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessGraphWeekResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, StatisticsWeekYearModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                apiModel.data?.let {
                    showSpendingWeekYear(it)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun showSpendingWeekYear(data: StatisticsWeekYearModelData) {
        try {
            statisticsViewModel.setGraphDataList(data,currentDate)
            recipesModel=data
            recipesModel?.orders?.let {
                adapterOrderHistoryItem = AdapterOrderHistoryGraph(it, requireActivity(), this)
                binding.rcyOrderStatsWeek.adapter = adapterOrderHistoryItem
            }
            recipesModel?.recipes?.let {
                fun setupMealAdapter(mealRecipes: MutableList<Breakfast>?, recyclerView: RecyclerView, type: String): AdapterStatisticsWeekItem? {
                    return if (!mealRecipes.isNullOrEmpty()) {
                        val adapter = AdapterStatisticsWeekItem(mealRecipes, requireActivity(), this, type)
                        recyclerView.adapter = adapter
                        adapter
                    } else {
                        null
                    }
                }

                it.Breakfast?.let { data->
                    if (data.size>0){
                        breakfastAdapter = setupMealAdapter(data, binding.rcyBreakfast, ErrorMessage.Breakfast)
                        binding.linearBreakfast.visibility = View.VISIBLE
                    }else{
                        binding.linearBreakfast.visibility = View.GONE
                    }
                }?:run {
                    binding.linearBreakfast.visibility = View.GONE
                }

                it.Lunch?.let { data->
                    if (data.size>0){
                        lunchAdapter = setupMealAdapter(data, binding.rcyLunch, ErrorMessage.Lunch)
                        binding.linearLunch.visibility = View.VISIBLE
                    }else{
                        binding.linearLunch.visibility = View.GONE
                    }
                }?:run {
                    binding.linearLunch.visibility = View.GONE
                }

                it.Dinner?.let { data->
                    if (data.size>0){
                        dinnerAdapter = setupMealAdapter(data, binding.rcyDinner, ErrorMessage.Dinner)
                        binding.linearDinner.visibility = View.VISIBLE
                    }else{
                        binding.linearDinner.visibility = View.GONE
                    }
                }?:run {
                    binding.linearDinner.visibility = View.GONE
                }

                it.Snacks?.let { data->
                    if (data.size>0){
                        snackesAdapter = setupMealAdapter(data, binding.rcySnacks, ErrorMessage.Snacks)
                        binding.linearSnacks.visibility = View.VISIBLE
                    }else{
                        binding.linearSnacks.visibility = View.GONE
                    }
                }?:run {
                    binding.linearSnacks.visibility = View.GONE
                }

                it.Brunch?.let { data->
                    if (data.size>0){
                        lunchAdapter = setupMealAdapter(data, binding.rcyBrunch, ErrorMessage.Brunch)
                        binding.linearBrunch.visibility = View.VISIBLE
                    }else{
                        binding.linearBrunch.visibility = View.GONE
                    }
                }?:run {
                    binding.linearBrunch.visibility = View.GONE
                }

                it.Breakfast_price?.let { value->
                    val formattedPrice = String.format("%.2f", value)
                    binding.tvDate.text= "$$formattedPrice"
                }

                it.Brunch_price?.let {value->
                    val formattedPrice = String.format("%.2f", value)
                    binding.tvAmntBrunchSaving.text="$$formattedPrice"
                }
                it.Dinner_price?.let {value->
                    val formattedPrice = String.format("%.2f", value)
                    binding.tvDate2.text="$$formattedPrice"
                }
                it.Lunch_price?.let {value->
                    val formattedPrice = String.format("%.2f", value)
                    binding.tvDate1.text="$$formattedPrice"
                }
                it.Snacks_price?.let {value->
                    val formattedPrice = String.format("%.2f", value)
                    binding.tvDate3.text="$$formattedPrice"
                }

            }

            recipesModel?.let {
                val total = it.total_price?.let { price ->
                    String.format("%.2f", price).toDouble()
                } ?: 0.0

                val totalBudget = it.user_budget?.let { budget ->
                    String.format("%.2f", budget).toDouble()
                } ?: 0.0



                val views = listOf(
                    binding.tvAmntWeek,
                    binding.tvAmntLunchWeek,
                    binding.tvAmntSnacksWeek,
                    binding.tvAmntBrunchWeek,
                    binding.tvAmntDinnerWeek
                )

                views.forEach { view ->
                    view.text = "$" + String.format("%.2f", total)
                }

                val viewsSaving = listOf(
                    binding.tvAmntSaving,
                    binding.tvAmntLunchSaving,
                    binding.tvAmntSnacksSaving,
                    binding.tvAmntBrunchSaving,
                    binding.tvAmntDinnerSaving
                )
                val difference=totalBudget-total
                viewsSaving.forEach { view ->
                    view.text = "$" + String.format("%.2f", difference)
                }
            }
        }catch (e:Exception){
            Log.d("Error ","*******"+e.message)
        }



    }

    private fun initialize() {

        binding.imgBackChristmas.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.imagePrevious.setOnClickListener {
            hidPastDate()
        }

        binding.relCalendarView.setOnClickListener {
            openDialog()
        }


        binding.imageNext.setOnClickListener {
            // Simulate clicking the "Next" button to move to the next week
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, 1) // Move to next week
            currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDates("2")

        }


    }

    private fun openDialog() {
        val dialog = Dialog(requireActivity())
        // Set custom layout
        dialog.setContentView(R.layout.dialog_calendar)

        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val calendarView = dialog.findViewById<CalendarView>(R.id.calendar)


        // Hide navigation arrows
        try {
            val fields = CalendarView::class.java.declaredFields
            for (field in fields) {
                if (field.name == "mNextButton" || field.name == "mPrevButton") {
                    field.isAccessible = true
                    val button = field.get(calendarView) as ImageButton
                    button.isEnabled = false
                    button.visibility = View.INVISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        calendarView.setOnDateChangeListener { _: CalendarView?, year: Int, month: Int, dayOfMonth: Int ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val date = calendar.time  // This is the Date object
            // Format the Date object to the desired string format
            val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault())
            val currentDateString = dateFormat.format(date)  // This is the formatted string
            // To convert the string back to a Date object:
            currentDate = dateFormat.parse(currentDateString)!!  // This is the Date object
            // Display current week dates
            showWeekDates("2")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun hidPastDate() {
        if (updatedDaysBetween.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
            currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDates("2")
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
        rcyChooseDaySch = dialogChooseDay.findViewById(R.id.rcyChooseDaySch)
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
        showWeekDatesPlan(currentDate)

        rlDoneBtn.setOnClickListener {
            var status = false
            for (value in dataList) {
                if (value.isOpen) {
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
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
            val currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDatesPlan(currentDate)
        }

        btnNext.setOnClickListener {
            // Simulate clicking the "Next" button to move to the next week
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, 1) // Move to next week
            val currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDatesPlan(currentDate)
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

    @SuppressLint("SetTextI18n")
    fun showWeekDatesPlan(currentDatePlan:Date) {
        Log.d("currentDate :- ", "******$currentDatePlan")
        // Define the date format (update to match your `date` string format)
        val (startDate, endDate) = getWeekDates(currentDatePlan)
        startDatePlan=startDate
        endDatePlan=endDate
        println("Week Start Date: ${formatDate(startDate)}")
        println("Week End Date: ${formatDate(endDate)}")
        // Get all dates between startDate and endDate
        val daysBetween = getDaysBetween(startDate, endDate)
        daysBetween.zip(dataList).forEach { (dateModel, dataModel) ->
            dataModel.date = dateModel.date
            dataModel.isOpen = false
        }
        // Print the dates
        println("Days between $startDate and ${endDate}:")
        daysBetween.forEach { println(it.date) }
        daysBetween.zip(dataList).forEach { (dateModel, dataModel) ->
            dataModel.date = dateModel.date
            dataModel.isOpen = false
        }
        tvWeekRange?.text = "" + formatDate(startDate) + "-" + formatDate(endDate)
        rcyChooseDaySch?.adapter = ChooseDayAdapter(dataList, requireActivity())

    }


    @SuppressLint("SetTextI18n")
    fun showWeekDates(type:String) {
        Log.d("currentDate :- ", "******$currentDate")
        // Define the date format (update to match your `date` string format)
        val (startDate, endDate) = getWeekDates(currentDate)
        this.startDate = startDate
        this.endDate = endDate
        println("Week Start Date: ${formatDate(startDate)}")
        println("Week End Date: ${formatDate(endDate)}")
        // Get all dates between startDate and endDate
        val daysBetween = getDaysBetween(startDate, endDate)
        // Mark the current date as selected in the list
        updatedDaysBetween = daysBetween.map { dateModel ->
            dateModel.apply {
                status = true
            }
        }
        // Print the dates
        println("Days between $startDate and ${endDate}:")
        daysBetween.forEach { println(it.date) }
        binding.textWeekRange.text = "" + formatDate(startDate) + "-" + formatDate(endDate)
        binding.tvCustomDates.text = "${formatDate(startDate)} - ${formatDate(endDate)}"
        tvWeekRange?.text = "" + formatDate(startDate) + "-" + formatDate(endDate)
        // Initialize the adapter with the updated date list
        calendarAdapter = CalendarDayDateWeekAdapter(updatedDaysBetween as MutableList)
        // Update the RecyclerView
        binding.recyclerViewWeekDays.adapter = calendarAdapter

        if (type.equals("1",true)){
            statisticsViewModel.dataGraphDataList?.let {
                showSpendingWeekYear(it)
            }?:run{
                loadWeekListApi()
            }
        }else{
            loadWeekListApi()
        }


    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
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
                    addToPlan(dialogChooseMealDay, type, position, typeAdapter)
                }

            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

    }

    private fun addToPlan(
        dialogChooseMealDay: Dialog,
        selectType: String,
        position: Int?,
        typeAdapter: String?
    ) {
        // Map the type to the corresponding list and adapter
        val (mealList, _) = when (typeAdapter) {
            ErrorMessage.Breakfast -> recipesModel?.recipes?.Breakfast to breakfastAdapter
            ErrorMessage.Lunch -> recipesModel?.recipes?.Lunch to lunchAdapter
            ErrorMessage.Dinner -> recipesModel?.recipes?.Dinner to dinnerAdapter
            ErrorMessage.Snacks -> recipesModel?.recipes?.Snacks to snackesAdapter
            ErrorMessage.Brunch -> recipesModel?.recipes?.Brunch to teaTimeAdapter
            else -> null to null
        }

        // Create a JsonObject for the main JSON structure
        val jsonObject = JsonObject()

        // Safely get the item and position
        val item = mealList?.get(position!!)
        if (item != null) {
            if (item.recipe?.uri != null) {
                jsonObject.addProperty("type", selectType)
                jsonObject.addProperty("uri", item.recipe.uri)
                // Create a JsonArray for ingredients
                val jsonArray = JsonArray()
                val latestList = getDaysBetween(startDatePlan, endDatePlan)
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
            statisticsViewModel.recipeAddToPlanRequest({
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
                (activity as MainActivity?)?.upDatePlan()
                dialogChooseMealDay.dismiss()
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
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
                            chooseDayDialog(position, type)
                        }else{
                            (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                        }
                    }else{
                        chooseDayDialog(position, type)
                    }
            }
            "2" -> {
                if (BaseApplication.isOnline(requireActivity())) {
                    toggleIsLike(type ?: "", position, "basket")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
            "4" -> {
                if ((activity as? MainActivity)?.Subscription_status==1){
                    if ((activity as? MainActivity)?.favorite!! <= 2){
                        if (BaseApplication.isOnline(requireActivity())) {
                            toggleIsLike(type ?: "", position, "like")
                        } else {
                            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                        }
                    }else{
                        (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                    }

                }else{
                    if (BaseApplication.isOnline(requireActivity())) {
                        toggleIsLike(type ?: "", position, "like")
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

    private fun toggleIsLike(type: String, position: Int?, apiType: String) {
        // Map the type to the corresponding list and adapter
        val (mealList, adapter) = when (type) {
            ErrorMessage.Breakfast -> recipesModel?.recipes?.Breakfast to breakfastAdapter
            ErrorMessage.Lunch -> recipesModel?.recipes?.Lunch to lunchAdapter
            ErrorMessage.Dinner -> recipesModel?.recipes?.Dinner to dinnerAdapter
            ErrorMessage.Snacks -> recipesModel?.recipes?.Snacks to snackesAdapter
            ErrorMessage.Brunch -> recipesModel?.recipes?.Brunch to teaTimeAdapter
            else -> null to null
        }
        // Safely get the item and position
        val item = mealList?.get(position!!)
        if (item != null) {
            if (item.recipe?.uri != null) {
                if (apiType.equals("basket", true)) {
                    addBasketData(item.recipe.uri,type)
                } else {
                    val newLikeStatus = if (item.is_like == 0) "1" else "0"
                    if (newLikeStatus.equals("0", true)) {
                        recipeLikeAndUnlikeData(
                            item,
                            adapter,
                            type,
                            mealList,
                            position,
                            newLikeStatus,
                            "",
                            null
                        )
                    } else {
                        addFavTypeDialog(item, adapter, type, mealList, position, newLikeStatus)
                    }

                }
            }
        }
    }

    private fun addBasketData(uri: String, type: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            statisticsViewModel.addBasketRequest({
                BaseApplication.dismissMe()
                handleBasketApiResponse(it)
            }, uri, "",type)
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
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun addFavTypeDialog(
        item: Breakfast, adapter: AdapterStatisticsWeekItem?, type: String,
        mealList: MutableList<Breakfast>, position: Int?, likeType: String
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
            bundle.putString("uri", item.recipe?.uri)
            findNavController().navigate(R.id.createCookBookFragment, bundle)
        }


        rlDoneBtn.setOnClickListener {
            if (spinnerActivityLevel.text.toString().equals("", true)) {
                BaseApplication.alertError(requireContext(), ErrorMessage.selectCookBookError, false)
            } else {
                val cookbooktype = cookbookList[spinnerActivityLevel.selectedIndex].id
                recipeLikeAndUnlikeData(
                    item,
                    adapter,
                    type,
                    mealList,
                    position,
                    likeType,
                    cookbooktype.toString(),
                    dialogAddRecipe
                )
            }
        }

    }

    private fun recipeLikeAndUnlikeData(
        item: Breakfast,
        adapter: AdapterStatisticsWeekItem?,
        type: String,
        mealList: MutableList<Breakfast>,
        position: Int?,
        likeType: String,
        cookbooktype: String,
        dialogAddRecipe: Dialog?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            statisticsViewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(
                    it,
                    item,
                    adapter,
                    type,
                    mealList,
                    position,
                    dialogAddRecipe
                )
            }, item.recipe?.uri.toString(), likeType, cookbooktype)
        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        item: Breakfast,
        adapter: AdapterStatisticsWeekItem?,
        type: String,
        mealList: MutableList<Breakfast>,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(
                result.data.toString(),
                item,
                adapter,
                type,
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
        item: Breakfast,
        adapter: AdapterStatisticsWeekItem?,
        type: String,
        mealList: MutableList<Breakfast>,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogAddRecipe?.dismiss()
                // Toggle the is_like value
                item.is_like = if (item.is_like == 0) 1 else 0
                mealList[position!!] = item
                // Update the adapter
                adapter?.updateList(mealList, type)
                (activity as MainActivity?)?.upDateHomeData()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun getCookBookList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            statisticsViewModel.getCookBookRequest {
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
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    override fun itemClicked(position: Int?, list: MutableList<String>?, status: String?, type: String?) {
        if (type.equals("View",true)) {
            val selectedItem = recipesModel?.orders?.get(position!!)
            val gson = Gson()
            val jsonString = gson.toJson(selectedItem)
            val bundle = Bundle().apply {
                putString("order_item_json", jsonString)
            }
            findNavController().navigate(R.id.orderDetailsScreenFragment,bundle)
        } else {
            val selectedItem = recipesModel?.orders?.get(position!!)
                val bundle = Bundle().apply {
                    putString("tracking", selectedItem?.order?.tracking_link)
                }
                findNavController().navigate(R.id.trackOrderScreenFragment, bundle)
        }

    }

}