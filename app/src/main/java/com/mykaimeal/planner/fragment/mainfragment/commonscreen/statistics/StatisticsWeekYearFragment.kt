package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterCookedRecipeAmount
import com.mykaimeal.planner.adapter.AdapterStatisticsWeekItem
import com.mykaimeal.planner.adapter.CalendarDayAdapter
import com.mykaimeal.planner.adapter.CalendarDayDateWeekAdapter
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.formatDate
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentStatisticsWeekYearBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.Breakfast
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsWeekYearModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsWeekYearModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel.StatisticsViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.CalendarDataModel
import com.mykaimeal.planner.model.DataModel
import com.mykaimeal.planner.model.DateModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class StatisticsWeekYearFragment : Fragment(),OnItemClickListener {

    private lateinit var binding: FragmentStatisticsWeekYearBinding

    private var rcyChooseDaySch: RecyclerView? = null
    private var tvWeekRange: TextView? = null
    private var adapterCookedRecipeAmount: AdapterCookedRecipeAmount? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private var chooseDayAdapter: ChooseDayAdapter? = null
    private var calendarDayAdapter: CalendarDayAdapter? = null
    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private lateinit var statisticsViewModel: StatisticsViewModel
    var updatedDaysBetween: List<DateModel> = emptyList()
    private var calendarAdapter: CalendarDayDateWeekAdapter? = null
    private var currentDate = Date() // Current date



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding=FragmentStatisticsWeekYearBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]

        backButton()




        statisticsViewModel.currentDate?.let {
            currentDate=it
        }

        showWeekDates()

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
        lifecycleScope.launch {
            statisticsViewModel.orderWeekUrl({
                BaseApplication.dismissMe()
                handleApiWeekGraphResponse(it)
            }, statisticsViewModel.dataWeekOfMonth,statisticsViewModel.dataCurrentMonth,statisticsViewModel.dataCurrentYear)
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

    @SuppressLint("SetTextI18n")
    private fun showSpendingWeekYear(data: StatisticsWeekYearModelData) {
        statisticsViewModel.setGraphDataList(data)
        if (data.recipes != null) {
            fun setupMealAdapter(mealRecipes: MutableList<Breakfast>?, recyclerView: RecyclerView, type: String,container: View) {
                if (!mealRecipes.isNullOrEmpty()) {
                    val adapter = AdapterStatisticsWeekItem(mealRecipes, requireActivity(), this, type)
                    recyclerView.adapter = adapter
                    container.visibility = View.VISIBLE
                } else {
                    container.visibility = View.GONE
                }
            }
            setupMealAdapter(data.recipes.Breakfast, binding.rcyBreakfast, ErrorMessage.Breakfast, binding.linearBreakfast)
            setupMealAdapter(data.recipes.Lunch, binding.rcyLunch, ErrorMessage.Lunch, binding.linearLunch)
            setupMealAdapter(data.recipes.Dinner, binding.rcyDinner, ErrorMessage.Dinner, binding.linearDinner)
            setupMealAdapter(data.recipes.Snacks, binding.rcySnacks, ErrorMessage.Snacks, binding.linearSnacks)
            setupMealAdapter(data.recipes.Brunch, binding.rcyBrunch, ErrorMessage.Brunch, binding.linearBrunch)


            data.recipes.Breakfast_price?.let {
                val formattedPrice = String.format("%.2f", it)
                binding.tvDate.text= "$$formattedPrice"
            }

            data.recipes.Brunch_price?.let {
                val formattedPrice = String.format("%.2f", it)
                binding.tvAmntBrunchSaving.text="$$formattedPrice"
            }
            data.recipes.Dinner_price?.let {
                val formattedPrice = String.format("%.2f", it)
                binding.tvDate2.text="$$formattedPrice"
            }
            data.recipes.Lunch_price?.let {
                val formattedPrice = String.format("%.2f", it)
                binding.tvDate1.text="$$formattedPrice"
            }
            data.recipes.Snacks_price?.let {
                val formattedPrice = String.format("%.2f", it)
                binding.tvDate3.text="$$formattedPrice"
            }





        }

    }

    private fun initialize() {

        binding.imgBackChristmas.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.imagePrevious.setOnClickListener {
            hidPastDate()
        }


        binding.imageNext.setOnClickListener {
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

    private fun hidPastDate() {
        if (updatedDaysBetween.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
            currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDates()
        }
    }


    private fun chooseDayDialog() {
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
        updateWeekRange()
        dialogChooseDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        cookingScheduleModel()

        rlDoneBtn.setOnClickListener {
            chooseDayMealTypeDialog()
            dialogChooseDay.dismiss()
        }

        btnPrevious.setOnClickListener {
            changeWeek(-1)
        }

        btnNext.setOnClickListener {
            changeWeek(1)
        }
    }

    private fun changeWeek(weeks: Int) {
        calendar.add(Calendar.WEEK_OF_YEAR, weeks)
        updateWeekRange()
    }

    private fun cookingScheduleModel() {
        val dataList = ArrayList<DataModel>()
        val data1 = DataModel()
        val data2 = DataModel()
        val data3 = DataModel()
        val data4 = DataModel()
        val data5 = DataModel()
        val data6 = DataModel()
        val data7 = DataModel()

        data1.title = "Monday"
        data1.isOpen = false
        data1.type = "CookingSchedule"

        data2.title = "Tuesday"
        data2.isOpen = false
        data2.type = "CookingSchedule"

        data3.title = "Wednesday"
        data3.isOpen = false
        data3.type = "CookingSchedule"

        data4.title = "Thursday"
        data4.isOpen = false
        data4.type = "CookingSchedule"

        data5.title = "Friday"
        data5.isOpen = false
        data5.type = "CookingSchedule"

        data6.title = "Saturday"
        data6.isOpen = false
        data6.type = "CookingSchedule"

        data7.title = "Sunday"
        data7.isOpen = false
        data7.type = "CookingSchedule"

        dataList.add(data1)
        dataList.add(data2)
        dataList.add(data3)
        dataList.add(data4)
        dataList.add(data5)
        dataList.add(data6)
        dataList.add(data7)

        chooseDayAdapter = ChooseDayAdapter(dataList, requireActivity())
        rcyChooseDaySch!!.adapter = chooseDayAdapter
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
        // Define the date format (update to match your `date` string format)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedCurrentDate = dateFormat.format(currentDate) // Format currentDate to match the string format
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

        statisticsViewModel.dataGraphDataList?.let {
            showSpendingWeekYear(it)
        }?:run{
            loadWeekListApi()
        }

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

//    @SuppressLint("SetTextI18n")
//    private fun updateWeek() {
//
//        val startOfWeek = calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }.time
//        val endOfWeek = calendar.apply { add(Calendar.DAY_OF_WEEK, 6) }.time
//
//        binding.textWeekRange.text = "${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}"
//        binding.recyclerViewWeekDays.adapter = calendarDayAdapter
//        binding.recyclerViewWeekDays.adapter = CalendarDayAdapter(getDaysOfWeek()) {
//        }
//
//
//
//
//
//
//    }

    private fun getDaysOfWeek(): List<CalendarDataModel.Day> {
        val days = mutableListOf<CalendarDataModel.Day>()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        for (i in 0..6) {
            days.add(
                CalendarDataModel.Day(
                    dayName = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time),
                    date = calendar.get(Calendar.DAY_OF_MONTH)
                )
            )
            calendar.add(Calendar.DAY_OF_WEEK, 1)
        }

        calendar.add(Calendar.DAY_OF_WEEK, -7) // Reset to start of week
        return days
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeekRange() {
        val startOfWeek = calendar.apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }.time

        val endOfWeek = calendar.apply {
            add(Calendar.DAY_OF_WEEK, 6)
        }.time

        tvWeekRange!!.text = "${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}"
        calendar.add(Calendar.DAY_OF_WEEK, -6) // Reset endOfWeek calculation
    }

    private fun chooseDayMealTypeDialog() {
        val dialogChooseMealDay: Dialog = context?.let { Dialog(it) }!!
        dialogChooseMealDay.setContentView(R.layout.alert_dialog_choose_day_meal_type)
        dialogChooseMealDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogChooseMealDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tvWeekRange = dialogChooseMealDay.findViewById(R.id.tvWeekRange)
        val rlDoneBtn = dialogChooseMealDay.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        val btnPrevious = dialogChooseMealDay.findViewById<ImageView>(R.id.btnPrevious)

        val btnNext = dialogChooseMealDay.findViewById<ImageView>(R.id.btnNext)
        // button event listener
        val tvBreakfast = dialogChooseMealDay.findViewById<TextView>(R.id.tvBreakfast)
        val tvLunch = dialogChooseMealDay.findViewById<TextView>(R.id.tvLunch)
        val tvDinner = dialogChooseMealDay.findViewById<TextView>(R.id.tvDinner)
        val tvSnacks = dialogChooseMealDay.findViewById<TextView>(R.id.tvSnacks)
        val tvTeatime = dialogChooseMealDay.findViewById<TextView>(R.id.tvTeatime)





        dialogChooseMealDay.show()
        updateWeekRange()
        dialogChooseMealDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        var type = ""

        fun updateSelection(selectedType: String, selectedView: TextView, allViews: List<TextView>) {
            type = selectedType
            allViews.forEach { view ->
                val drawable = if (view == selectedView) R.drawable.radio_select_icon else R.drawable.radio_unselect_icon
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
            updateSelection("Teatime", tvTeatime, allViews)
        }

        rlDoneBtn.setOnClickListener {
            dialogChooseMealDay.dismiss()
        }

        btnPrevious.setOnClickListener {
            changeWeek(-1)
        }

        btnNext.setOnClickListener {
            changeWeek(1)
        }
    }


    override fun itemClick(position: Int?, status: String?, type: String?) {

    }


}