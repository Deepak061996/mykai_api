package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterCookedRecipeAmount
import com.mykaimeal.planner.adapter.AdapterStatisticsWeekItem
import com.mykaimeal.planner.adapter.CalendarDayAdapter
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.databinding.FragmentStatisticsWeekYearBinding
import com.mykaimeal.planner.model.CalendarDataModel
import com.mykaimeal.planner.model.DataModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@AndroidEntryPoint
class StatisticsWeekYearFragment : Fragment(),OnItemClickListener {

    private var binding: FragmentStatisticsWeekYearBinding?=null
    private var dataList1: MutableList<DataModel> = mutableListOf()
    private var dataList4: MutableList<DataModel> = mutableListOf()
    private var dataList2: MutableList<DataModel> = mutableListOf()
    private var dataList3: MutableList<DataModel> = mutableListOf()
    private var rcyChooseDaySch: RecyclerView? = null
    private var tvWeekRange: TextView? = null
    private var adapterStatisticsWeekItem: AdapterStatisticsWeekItem? = null
    private var adapterCookedRecipeAmount: AdapterCookedRecipeAmount? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private var chooseDayAdapter: ChooseDayAdapter? = null
    private var calendarDayAdapter: CalendarDayAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentStatisticsWeekYearBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        recipeCookedModel()
        statsBreakFastModel()
        statsLunchModel()
        statsDinnerModel()

        initialize()

        return binding!!.root
    }

    private fun initialize() {

        binding!!.imgBackChristmas.setOnClickListener {
            findNavController().navigateUp()
        }

        updateWeek()
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

    @SuppressLint("SetTextI18n")
    private fun updateWeek() {
        val startOfWeek = calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }.time

        val endOfWeek = calendar.apply {
            add(Calendar.DAY_OF_WEEK, 6)
        }.time

        binding!!.textWeekRange.text =
            "${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}"
        binding!!.recyclerViewWeekDays.adapter = calendarDayAdapter
        binding!!.recyclerViewWeekDays.adapter = CalendarDayAdapter(getDaysOfWeek()) {
        }
    }

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


    private fun statsBreakFastModel() {
        val data1 = DataModel()
        val data2 = DataModel()
        val data3 = DataModel()

        data1.title = "Bread"
        data1.isOpen = false
        data1.type = "RecipeCookedBreakFast"
        data1.image = R.drawable.bread_breakfast_image

        data2.title = "Juice"
        data2.isOpen = false
        data2.type = "RecipeCookedBreakFast"
        data2.image = R.drawable.fresh_juice_glass_image

        data3.title = "Bar-B-Q"
        data3.isOpen = false
        data3.type = "RecipeCookedBreakFast"
        data3.image = R.drawable.bar_b_q_breakfast_image

        dataList1.add(data1)
        dataList1.add(data2)
        dataList1.add(data3)

        adapterStatisticsWeekItem = AdapterStatisticsWeekItem(dataList1, requireActivity(),this)
        binding!!.rcyBreakFast.adapter = adapterStatisticsWeekItem
    }

    private fun recipeCookedModel() {
        val data1 = DataModel()
        val data2 = DataModel()
        val data3 = DataModel()

        data1.title = "Amount Spent on Breakfast"
        data1.isOpen = false
        data1.price="$289"
        data1.type = "RecipeCooked"
        data1.image = R.drawable.bread_breakfast_image

        data2.title = "Amount Spent this week"
        data2.isOpen = false
        data2.price="$87"
        data2.type = "RecipeCooked"
        data2.image = R.drawable.fresh_juice_glass_image

        data3.title = "Savings this week"
        data3.isOpen = false
        data3.price="$25"
        data3.type = "RecipeCooked"
        data3.image = R.drawable.bar_b_q_breakfast_image

        data3.title = "Time Spend"
        data3.isOpen = false
        data3.price="2h 22m"
        data3.type = "RecipeCooked"
        data3.image = R.drawable.bar_b_q_breakfast_image

        dataList4.add(data1)
        dataList4.add(data2)
        dataList4.add(data3)

        adapterCookedRecipeAmount = AdapterCookedRecipeAmount(dataList4, requireActivity(),this)
        binding!!.rcyWeekAmountType.adapter = adapterCookedRecipeAmount
        binding!!.rcyWeekAmountType1.adapter = adapterCookedRecipeAmount
        binding!!.rcyWeekAmountType2.adapter = adapterCookedRecipeAmount
    }

    private fun statsLunchModel() {
        val data1 = DataModel()
        val data2 = DataModel()
        val data3 = DataModel()

        data1.title = "Bread"
        data1.isOpen = false
        data1.type = "RecipeCookedLunch"
        data1.image = R.drawable.bread_lunch_image

        data2.title = "Juice"
        data2.isOpen = false
        data2.type = "RecipeCookedLunch"
        data2.image = R.drawable.bar_b_q_breakfast_image

        data3.title = "Bar-B-Q"
        data3.isOpen = false
        data3.type = "RecipeCookedLunch"
        data3.image = R.drawable.bar_b_q_breakfast_image

        dataList2.add(data1)
        dataList2.add(data2)
        dataList2.add(data3)

        adapterStatisticsWeekItem = AdapterStatisticsWeekItem(dataList2, requireActivity(),this)
        binding!!.rcyLunch.adapter = adapterStatisticsWeekItem
    }

    private fun statsDinnerModel() {
        val data1 = DataModel()
        val data2 = DataModel()
        val data3 = DataModel()

        data1.title = "Bread"
        data1.isOpen = false
        data1.type = "RecipeCookedDinner"
        data1.image = R.drawable.bread_dinner_image

        data2.title = "Juice"
        data2.isOpen = false
        data2.type = "RecipeCookedDinner"
        data2.image = R.drawable.fresh_juice_glass_image

        data3.title = "Bar-B-Q"
        data3.isOpen = false
        data3.type = "RecipeCookedDinner"
        data3.image = R.drawable.bar_b_q_breakfast_image

        dataList3.add(data1)
        dataList3.add(data2)
        dataList3.add(data3)

        adapterStatisticsWeekItem = AdapterStatisticsWeekItem(dataList3, requireActivity(),this)
        binding!!.rcyDinner.adapter = adapterStatisticsWeekItem
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        if (status=="1"){
            chooseDayDialog()
        }else{
            findNavController().navigate(R.id.basketScreenFragment)
        }
    }


}