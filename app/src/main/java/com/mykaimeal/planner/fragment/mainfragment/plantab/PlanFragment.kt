package com.mykaimeal.planner.fragment.mainfragment.plantab

import PlanApiResponse
import PlanByDateApiResponse
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
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemSelectPlanTypeListener
import com.mykaimeal.planner.adapter.AdapterPlanBreakByDateFast
import com.mykaimeal.planner.adapter.CalendarDayDateAdapter
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsebydate.BreakfastModelPlanByDate
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsebydate.DataPlayByDate
import com.mykaimeal.planner.model.DateModel
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemMealTypeListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterMealType
import com.mykaimeal.planner.adapter.AdapterPlanBreakFast
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.adapter.ImageViewPagerAdapter
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentPlanBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.PlanViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.BreakfastModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.RecipesModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PlanFragment : Fragment(), OnItemClickListener, OnItemSelectPlanTypeListener,
    OnItemMealTypeListener {

    private lateinit var binding: FragmentPlanBinding
    private var tvWeekRange: TextView? = null
    private var rcyChooseDaySch: RecyclerView? = null
    private var clickable: String? = ""
    private lateinit var viewModel: PlanViewModel
    private var recipesModel: RecipesModel? = null
    private var recipesDateModel: DataPlayByDate? = null

    // Separate adapter instances for each RecyclerView
    private var breakfastAdapter: AdapterPlanBreakFast? = null
    private var adapterPlanBreakByDateFast: AdapterPlanBreakByDateFast? = null
    private var lunchAdapter: AdapterPlanBreakFast? = null
    private var adapterLunchByDateFast: AdapterPlanBreakByDateFast? = null
    private var dinnerAdapter: AdapterPlanBreakFast? = null
    private var adapterDinnerByDateFast: AdapterPlanBreakByDateFast? = null
    private var snackesAdapter: AdapterPlanBreakFast? = null
    private var adapterSnacksByDateFast: AdapterPlanBreakByDateFast? = null
    private var teaTimeAdapter: AdapterPlanBreakFast? = null
    private var AdapterteaTimeByDateFast: AdapterPlanBreakByDateFast? = null
    private lateinit var sessionManagement: SessionManagement
    lateinit var adapter: ImageViewPagerAdapter

    private var currentDate = Date() // Current date
    private var currentDateSelected: String = ""
    private var lastDateSelected: String = ""
    private var swapMealType: String = ""
    private var swapId: Int = 0

    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private var calendarAdapter: CalendarDayDateAdapter? = null
    private var mealTypeAdapter: AdapterMealType? = null
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    private var mealRoutineList: MutableList<MealRoutineModelData> = mutableListOf()
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    var updatedDaysBetween: List<DateModel> = emptyList()
    val dataList = arrayListOf<DataModel>()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentPlanBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[PlanViewModel::class.java]


        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        (activity as MainActivity?)?.changeBottom("plan")
        (activity as MainActivity?)?.alertStatus=true
        sessionManagement = SessionManagement(requireContext())

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

        if (sessionManagement.getImage() != null) {
            Glide.with(requireContext())
                .load(BaseUrl.imageBaseUrl + sessionManagement.getImage())
                .placeholder(R.drawable.mask_group_icon)
                .error(R.drawable.mask_group_icon)
                .into(binding.imageProfile)
        }

        currentDateSelected = BaseApplication.currentDateFormat().toString()
        Log.d("currentDateSelected", "******$currentDateSelected")

        if (sessionManagement.getUserName() != null) {
            binding.tvName.text = sessionManagement.getUserName() + "’s week"
        }

        setUpListener()

        if (viewModel.data!=null){
            lastDateSelected=viewModel.date.toString()
            showData(viewModel.data!!)
        }else{
            lastDateSelected = currentDateSelected
            viewModel.setDate(lastDateSelected)
            // When screen load then api call
            fetchDataOnLoad()
        }



        // Display current week dates
        showWeekDates()

        return binding.root
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.homeFragment)
                }
            })
    }

    @SuppressLint("SetTextI18n")
    fun showWeekDates() {
        Log.d("currentDate :- ", "******$currentDate")
        Log.d("lastDateSelected :- ", "******$lastDateSelected")
        viewModel.setDate(lastDateSelected)
        // Define the date format (update to match your `date` string format)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedCurrentDate =
            dateFormat.format(currentDate) // Format currentDate to match the string format
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
                status = (date == lastDateSelected) // Compare formatted strings
            }
        }
        // Print the dates
        println("Days between $startDate and ${endDate}:")
        daysBetween.forEach { println(it) }
        binding.tvDate.text = BaseApplication.formatOnlyMonthYear(startDate)
        binding.textWeekRange.text = "" + formatDate(startDate) + "-" + formatDate(endDate)

        tvWeekRange?.text = "" + formatDate(startDate) + "-" + formatDate(endDate)

        daysBetween.zip(dataList).forEach { (dateModel, dataModel) ->
            dataModel.date = dateModel.date
            dataModel.isOpen = false
        }
        rcyChooseDaySch?.adapter = ChooseDayAdapter(dataList, requireActivity())

        // Initialize the adapter with the updated date list
        calendarAdapter = CalendarDayDateAdapter(updatedDaysBetween.toMutableList()) { selectedPosition ->
                // Update the list to reflect the selected date
                updatedDaysBetween.forEachIndexed { index, dateModel ->
                    dateModel.status = (index == selectedPosition)
                    lastDateSelected = updatedDaysBetween[selectedPosition].date
                    viewModel.setDate(lastDateSelected)
                    Log.d("dateModel Date ", "*****${dateModel.date}")
                }
                Log.d("Date ", "*****$updatedDaysBetween")

                // Notify the adapter to refresh the data
                calendarAdapter?.updateList(updatedDaysBetween.toMutableList())

                // Update the current date selection
                currentDateSelected = updatedDaysBetween[selectedPosition].date
                viewModel.setData(null)
                // When screen load then api call
                fetchDataOnLoad()
//                // Fetch data for the selected date if online
//                if (BaseApplication.isOnline(requireActivity())) {
//                    dataFatchByDate(currentDateSelected,"2")
//                } else {
//                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
//                }
            }
        // Update the RecyclerView
        binding.recyclerViewWeekDays.adapter = calendarAdapter

    }

    private fun dataFatchByDate(date: String,type:String) {
        if (!type.equals("1")){
            BaseApplication.showMe(requireContext())
        }
        lifecycleScope.launch {
            viewModel.planDateRequest({
                BaseApplication.dismissMe()
                handleApiPlanDateResponse(it)
            }, date, "0")
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
            viewModel.planRequest({
                BaseApplication.dismissMe()
                handleApiResponse(it)
            }, "q")
        }
    }

    private fun handleApiPlanDateResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessPlanDateResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
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

    private fun handleApiAddToBasketResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddDayToBasketResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
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
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, PlanApiResponse::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null) {
                    showData(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessPlanDateResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, PlanByDateApiResponse::class.java)
            Log.d("@@@ user click  List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null) {
                    showDataAccordingDate(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddDayToBasketResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                // Fetch data for the selected date if online
                if (BaseApplication.isOnline(requireActivity())) {
                    dataFatchByDate(currentDateSelected,"2")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
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
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
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

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(
        data: String,
        item: BreakfastModel,
        adapter: AdapterPlanBreakFast?,
        type: String,
        mealList: MutableList<BreakfastModel>,
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

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
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

    private fun showData(data: Data) {
        try {
            viewModel.setData(data)
            recipesModel = data.recipes

            if (recipesModel != null) {
                fun setupMealAdapter(
                    mealRecipes: MutableList<BreakfastModel>?, recyclerView: RecyclerView, type: String
                ): AdapterPlanBreakFast? {
                    return if (mealRecipes != null && mealRecipes.isNotEmpty()) {
                        val adapter = AdapterPlanBreakFast(mealRecipes, requireActivity(), this, type)
                        recyclerView.adapter = adapter
                        adapter
                    } else {
                        null
                    }
                }

                binding.llCalculateBmr.visibility = View.GONE

                // Breakfast
                if (recipesModel?.Breakfast != null && recipesModel?.Breakfast?.size!! > 0) {
                    breakfastAdapter =
                        setupMealAdapter(recipesModel?.Breakfast, binding.rcyBreakFast, ErrorMessage.Breakfast)
                    binding.linearBreakfast.visibility = View.VISIBLE
                } else {
                    binding.linearBreakfast.visibility = View.GONE
                }

                // Lunch
                if (recipesModel?.Lunch != null && recipesModel?.Lunch?.size!! > 0) {
                    lunchAdapter = setupMealAdapter(recipesModel?.Lunch, binding.rcyLunch, ErrorMessage.Lunch)
                    binding.linearLunch.visibility = View.VISIBLE
                } else {
                    binding.linearLunch.visibility = View.GONE
                }

                // Dinner
                if (recipesModel?.Dinner != null && recipesModel?.Dinner?.size!! > 0) {
                    dinnerAdapter =
                        setupMealAdapter(recipesModel?.Dinner, binding.rcyDinner, ErrorMessage.Dinner)
                    binding.linearDinner.visibility = View.VISIBLE
                } else {
                    binding.linearDinner.visibility = View.GONE
                }


                // Snacks
                if (recipesModel?.Snack != null && recipesModel?.Snack?.size!! > 0) {
                    snackesAdapter =
                        setupMealAdapter(recipesModel?.Snack, binding.rcySnacks, ErrorMessage.Snacks)
                    binding.linearSnacks.visibility = View.VISIBLE
                } else {
                    binding.linearSnacks.visibility = View.GONE
                }

                // TeaTime
                if (recipesModel?.Teatime != null && recipesModel?.Teatime?.size!! > 0) {
                    teaTimeAdapter =
                        setupMealAdapter(recipesModel?.Teatime, binding.rcyTeatime, ErrorMessage.Brunch)
                    binding.linearTeatime.visibility = View.VISIBLE
                } else {
                    binding.linearTeatime.visibility = View.GONE
                }
            }

            if (viewModel.dataPlan!=null){
                showDataAccordingDate(viewModel.dataPlan)
            }else{
                // Fetch data for the selected date if online
                if (BaseApplication.isOnline(requireActivity())) {
                    dataFatchByDate(currentDateSelected,"1")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }

        }catch (e:Exception){
            Log.d("@Error","******"+e.message)
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun showDataAccordingDate(data: DataPlayByDate?) {

        try {
            viewModel.setPlanDate(data)
            recipesDateModel = null
            recipesDateModel = data

            Log.d("fdfd", "fdfdff" + recipesDateModel)

            if (recipesDateModel != null) {

                if (recipesDateModel!!.show == 0) {
                    binding.imgBmr.visibility = View.VISIBLE
                    binding.llCalculateBmr.visibility = View.VISIBLE
                } else {
                    binding.imgBmr.visibility = View.GONE
                    binding.llCalculateBmr.visibility = View.VISIBLE
                }


                if (recipesDateModel!!.fat != null || recipesDateModel!!.protein != null || recipesDateModel!!.kcal != null || recipesDateModel!!.carbs != null) {
                    if (recipesDateModel!!.fat != 0 || recipesDateModel!!.protein != 0 ||
                        recipesDateModel!!.kcal != 0 || recipesDateModel!!.carbs != 0
                    ) {
                        if (recipesDateModel!!.show == 1) {
                            binding.tvCalories.text = ""+recipesDateModel!!.kcal
                            binding.tvFats.text = ""+recipesDateModel!!.fat
                            binding.tvCarbohydrates.text = ""+recipesDateModel!!.carbs
                            binding.tvProteins.text = ""+recipesDateModel!!.protein

                        }
                    }
                }

                var status = false

                fun setupMealAdapter(
                    mealRecipes: MutableList<BreakfastModelPlanByDate>?,
                    recyclerView: RecyclerView,
                    type: String
                ): AdapterPlanBreakByDateFast? {
                    return if (mealRecipes != null && mealRecipes.isNotEmpty()) {
                        val adapter =
                            AdapterPlanBreakByDateFast(mealRecipes, requireActivity(), this, type)
                        recyclerView.adapter = adapter
                        adapter
                    } else {
                        null
                    }
                }


                fun setupMealTopAdapter(
                    mealRecipes: MutableList<BreakfastModel>?,
                    recyclerView: RecyclerView,
                    type: String
                ): AdapterPlanBreakFast? {
                    return if (mealRecipes != null && mealRecipes.isNotEmpty()) {
                        val adapter = AdapterPlanBreakFast(mealRecipes, requireActivity(), this, type)
                        recyclerView.adapter = adapter
                        adapter
                    } else {
                        null
                    }
                }

                // Breakfast
                if (recipesDateModel?.Breakfast != null && recipesDateModel?.Breakfast?.size!! > 0) {
                    adapterPlanBreakByDateFast = setupMealAdapter(
                        recipesDateModel!!.Breakfast!!,
                        binding.rcyBreakFast,
                        ErrorMessage.Breakfast
                    )
                    binding.linearBreakfast.visibility = View.VISIBLE
                    status = true
                } else {
                    // Breakfast
                    if (recipesModel?.Breakfast != null && recipesModel?.Breakfast?.size!! > 0) {
                        breakfastAdapter = setupMealTopAdapter(
                            recipesModel?.Breakfast,
                            binding.rcyBreakFast,
                            ErrorMessage.Breakfast
                        )
                        binding.linearBreakfast.visibility = View.VISIBLE
                    } else {
                        binding.linearBreakfast.visibility = View.GONE
                    }
                }

                // Lunch
                if (recipesDateModel?.Lunch != null && recipesDateModel?.Lunch?.size!! > 0) {
                    adapterLunchByDateFast = setupMealAdapter(data?.Lunch, binding.rcyLunch, ErrorMessage.Lunch)
                    binding.linearLunch.visibility = View.VISIBLE
                    status = true
                } else {
                    if (recipesModel?.Lunch != null && recipesModel?.Lunch?.size!! > 0) {
                        lunchAdapter =
                            setupMealTopAdapter(recipesModel?.Lunch, binding.rcyLunch, ErrorMessage.Lunch)
                        binding.linearLunch.visibility = View.VISIBLE
                    } else {
                        binding.linearLunch.visibility = View.GONE
                    }
                }

                // Dinner
                if (recipesDateModel?.Dinner != null && recipesDateModel?.Dinner?.size!! > 0) {
                    adapterDinnerByDateFast =
                        setupMealAdapter(data?.Dinner, binding.rcyDinner, ErrorMessage.Dinner)
                    binding.linearDinner.visibility = View.VISIBLE
                    status = true
                } else {
                    // Dinner
                    if (recipesModel?.Dinner != null && recipesModel?.Dinner?.size!! > 0) {
                        dinnerAdapter =
                            setupMealTopAdapter(recipesModel?.Dinner, binding.rcyDinner, ErrorMessage.Dinner)
                        binding.linearDinner.visibility = View.VISIBLE
                    } else {
                        binding.linearDinner.visibility = View.GONE
                    }
                }

                // Snacks
                if (recipesDateModel?.Snack != null && recipesDateModel?.Snack?.size!! > 0) {
                    adapterSnacksByDateFast =
                        setupMealAdapter(data?.Snack, binding.rcySnacks, ErrorMessage.Snacks)
                    binding.linearSnacks.visibility = View.VISIBLE
                    status = true
                } else {
                    // Snacks
                    if (recipesModel?.Snack != null && recipesModel?.Snack?.size!! > 0) {
                        snackesAdapter =
                            setupMealTopAdapter(recipesModel?.Snack, binding.rcySnacks, ErrorMessage.Snacks)
                        binding.linearSnacks.visibility = View.VISIBLE
                    } else {
                        binding.linearSnacks.visibility = View.GONE
                    }
                }

                // TeaTime
                if (recipesDateModel?.Teatime != null && recipesDateModel?.Teatime?.size!! > 0) {
                    AdapterteaTimeByDateFast =
                        setupMealAdapter(data?.Teatime, binding.rcyTeatime, ErrorMessage.Brunch)
                    binding.linearTeatime.visibility = View.VISIBLE
                    status = true
                } else {
                    // TeaTime
                    if (recipesModel?.Teatime != null && recipesModel?.Teatime?.size!! > 0) {
                        teaTimeAdapter =
                            setupMealTopAdapter(recipesModel?.Teatime, binding.rcyTeatime, ErrorMessage.Brunch)
                        binding.linearTeatime.visibility = View.VISIBLE
                    } else {
                        binding.linearTeatime.visibility = View.GONE
                    }
                }

                if (status) {
                    clickable = "1"
                    binding.rlAddDayToBasket.isClickable = true
                    binding.rlAddDayToBasket.setBackgroundResource(R.drawable.gray_btn_select_background)
                } else {
                    clickable = ""
                    binding.rlAddDayToBasket.isClickable = false
                    binding.rlAddDayToBasket.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                }
            }
        }catch (e:Exception){
            Log.d("@Error","******"+e.message)
        }

    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("DefaultLocale")
    private fun setUpListener() {

        binding.tvAddAnotherMealBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                val mainActivity = requireActivity() as MainActivity
                mainActivity.mealRoutineSelectApi { data ->
                    mealRoutineList.clear()
                    mealRoutineList.addAll(data)
                    if (mealRoutineList.isNotEmpty()) {
                        addAnotherMealDialog()
                    } else {
                        // Handle the case where the list is empty
                        BaseApplication.alertError(
                            requireContext(),
                            "No meal routines available.",
                            false
                        )
                    }
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        binding.imageProfile.setOnClickListener {
            findNavController().navigate(R.id.settingProfileFragment)
        }

        binding.imgHearRedIcons.setOnClickListener {
            findNavController().navigate(R.id.cookBookFragment)
        }

        binding.imgBasketIcon.setOnClickListener {
            (activity as MainActivity?)?.upBasket()
            findNavController().navigate(R.id.basketScreenFragment)
        }

        binding.rlAddDayToBasket.setOnClickListener {
            if (!clickable.equals("", true)) {
                if (BaseApplication.isOnline(requireActivity())) {
                    addDayToBasket()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        binding.imgAddBreakFast.setOnClickListener {
            val bundle = Bundle().apply {
                putString("ClickedUrl", "")
            }
            findNavController().navigate(R.id.searchFragment, bundle)
        }

        binding.imgAddLunch.setOnClickListener {
            val bundle = Bundle().apply {
                putString("ClickedUrl", "")
            }
            findNavController().navigate(R.id.searchFragment, bundle)
        }

        binding.imgAddDinner.setOnClickListener {
            val bundle = Bundle().apply {
                putString("ClickedUrl", "")
            }
            findNavController().navigate(R.id.searchFragment, bundle)
        }

        binding.imgAddSnacks.setOnClickListener {
            val bundle = Bundle().apply {
                putString("ClickedUrl", "")
            }
            findNavController().navigate(R.id.searchFragment, bundle)
        }

        binding.imgAddTeaTime1.setOnClickListener {
            val bundle = Bundle().apply {
                putString("ClickedUrl", "")
            }
            findNavController().navigate(R.id.searchFragment, bundle)
        }


        binding.imgBmr.setOnClickListener {
            if (recipesDateModel != null) {
                if (recipesDateModel!!.show == 1) {
                    if (recipesDateModel!!.fat != null || recipesDateModel!!.protein != null || recipesDateModel!!.kcal != null || recipesDateModel!!.carbs != null) {
                        if (recipesDateModel!!.fat?.toInt() != 0 || recipesDateModel!!.protein?.toInt() != 0 ||
                            recipesDateModel!!.kcal?.toInt() != 0 || recipesDateModel!!.carbs?.toInt() != 0
                        ) {
                            binding.imgBmr.visibility = View.GONE
                            binding.tvCalories.text =
                                String.format("%.2f", recipesDateModel!!.kcal)
                            binding.tvFats.text = String.format("%.2f", recipesDateModel!!.fat)
                            binding.tvCarbohydrates.text =
                                String.format("%.2f", recipesDateModel!!.carbs)
                            binding.tvProteins.text =
                                String.format("%.2f", recipesDateModel!!.protein)
                        }
                    } else {
                        findNavController().navigate(R.id.healthDataFragment)
                    }
                } else {
                    findNavController().navigate(R.id.healthDataFragment)
                }

            }
        }


        binding.relMonthYear.setOnClickListener {
            openDialog()
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

    private fun addDayToBasket() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.addDayToBasketAllUrl({
                BaseApplication.dismissMe()
                handleApiAddToBasketResponse(it)
            }, currentDateSelected)
        }
    }


    private fun hidPastDate() {
        if (updatedDaysBetween.isNotEmpty()) {
            // Define the date format (update to match your `date` string format)
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

    private fun openDialog() {
        val dialog = Dialog(requireActivity())
        // Set custom layout
        dialog.setContentView(R.layout.dialog_calendar)

        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val calendarView = dialog.findViewById<CalendarView>(R.id.calendar)
        // Disable previous dates
        calendarView.minDate = System.currentTimeMillis()

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
            showWeekDates()
            dialog.dismiss()
        }
        dialog.show()
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
            hidPastDate()
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
        val (mealList, adapter) = when (typeAdapter) {
            ErrorMessage.Breakfast -> recipesModel?.Breakfast to breakfastAdapter
            ErrorMessage.Lunch -> recipesModel?.Lunch to lunchAdapter
            ErrorMessage.Dinner -> recipesModel?.Dinner to dinnerAdapter
            ErrorMessage.Snacks -> recipesModel?.Snack to snackesAdapter
            ErrorMessage.Brunch -> recipesModel?.Teatime to teaTimeAdapter
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

    private fun addAnotherMealDialog() {
        val dialogAddItem: Dialog = context?.let { Dialog(it) }!!
        dialogAddItem.setContentView(R.layout.alert_dialog_add_another_meal)
        dialogAddItem.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddItem.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val rlAddToPlan = dialogAddItem.findViewById<RelativeLayout>(R.id.rlAddToPlan)
        val rcyMeal = dialogAddItem.findViewById<RecyclerView>(R.id.rcy_meal)
        dialogAddItem.show()
        dialogAddItem.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        if (mealRoutineList.size > 0) {
            mealTypeAdapter = AdapterMealType(mealRoutineList, requireActivity(), this)
            rcyMeal.adapter = mealTypeAdapter
        }

        rlAddToPlan.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                val selectId: MutableList<String> = mutableListOf()
                selectId.clear()
                var status = false
                mealRoutineList.forEach {
                    if (it.selected) {
                        selectId.add(it.id.toString())
                        status = true
                    }
                }
                if (status) {
                    updateMealRoutineApi(selectId, dialogAddItem)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.mealTypetError, false)
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

    }

    private fun updateMealRoutineApi(selectId: MutableList<String>, dialogAddItem: Dialog) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.updateMealRoutineApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val updateModel =
                                gson.fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
                            if (updateModel.code == 200 && updateModel.success) {
                                dialogAddItem.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    updateModel.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                viewModel.setData(null)
                                // When screen load then api call
                                fetchDataOnLoad()
                            } else {
                                handleError(updateModel.code,updateModel.message)
                            }
                        } catch (e: Exception) {
                            Log.d("MealRoutine@@@", "message:---" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlert(it.message, false)
                    }
                    else -> {
                        showAlert(it.message, false)
                    }
                }
            }, selectId)
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        when (status) {
            "1" -> {
                if (type == swapMealType) {
                    swapApiImplement(position, type)
                } else {
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

    private fun swapApiImplement(position: Int?, type: String) {
        // Map the type to the corresponding list and adapter
        val (mealList, adapter) = when (type) {
            ErrorMessage.Breakfast -> recipesModel?.Breakfast to breakfastAdapter
            ErrorMessage.Lunch -> recipesModel?.Lunch to lunchAdapter
            ErrorMessage.Dinner -> recipesModel?.Dinner to dinnerAdapter
            ErrorMessage.Snacks -> recipesModel?.Snack to snackesAdapter
            ErrorMessage.Brunch -> recipesModel?.Teatime to teaTimeAdapter
            else -> null to null
        }

        // Safely get the item and position
        val item = mealList?.get(position!!)
        if (item != null) {
            if (item.recipe?.uri != null) {
                swapApi(item.recipe.uri)
            }
        }
    }

    private fun swapApi(uri: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.recipeSwapUrl({
                BaseApplication.dismissMe()
                handleApiSwapResponse(it)
            }, swapId.toString(), uri)
        }

    }


    private fun handleApiSwapResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessSwapResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleSuccessSwapResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ user click  List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), "" + apiModel.message, Toast.LENGTH_LONG).show()

                // Fetch data for the selected date if online
                if (BaseApplication.isOnline(requireActivity())) {
                    dataFatchByDate(currentDateSelected,"2")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    private fun toggleIsLike(type: String, position: Int?, apiType: String) {
        // Map the type to the corresponding list and adapter
        val (mealList, adapter) = when (type) {
            ErrorMessage.Breakfast -> recipesModel?.Breakfast to breakfastAdapter
            ErrorMessage.Lunch -> recipesModel?.Lunch to lunchAdapter
            ErrorMessage.Dinner -> recipesModel?.Dinner to dinnerAdapter
            ErrorMessage.Snacks -> recipesModel?.Snack to snackesAdapter
            ErrorMessage.Brunch -> recipesModel?.Teatime to teaTimeAdapter
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


    private fun addFavTypeDialog(
        item: BreakfastModel, adapter: AdapterPlanBreakFast?, type: String,
        mealList: MutableList<BreakfastModel>, position: Int?, likeType: String
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

    private fun getCookBookList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }
        }
    }


    private fun recipeLikeAndUnlikeData(
        item: BreakfastModel,
        adapter: AdapterPlanBreakFast?,
        type: String,
        mealList: MutableList<BreakfastModel>,
        position: Int?,
        likeType: String,
        cookbooktype: String,
        dialogAddRecipe: Dialog?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.likeUnlikeRequest({
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

    private fun addBasketData(uri: String, type: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.addBasketRequest({
                BaseApplication.dismissMe()
                handleBasketApiResponse(it)
            }, uri, "",type)
        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        item: BreakfastModel,
        adapter: AdapterPlanBreakFast?,
        type: String,
        mealList: MutableList<BreakfastModel>,
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

    private fun handleBasketApiResponse(
        result: NetworkResult<String>
    ) {
        when (result) {
            is NetworkResult.Success -> handleBasketSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    override fun itemSelectPlayByDate(position: Int?, status: String?, type: String?) {
        when (status) {
            "1" -> {
                swap(type!!, position, status)
            }

            "2" -> {
                if (BaseApplication.isOnline(requireActivity())) {
                    removeAddServing(type ?: "", position, "add")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }

            "3" -> {
                if (BaseApplication.isOnline(requireActivity())) {
                    removeAddServing(type ?: "", position, "minus")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale", "SuspiciousIndentation")
    private fun removeAddServing(type: String, position: Int?, apiType: String) {
        // Map the type to the corresponding list and adapter
        val (mealList, adapter) = when (type) {
            ErrorMessage.Breakfast -> recipesDateModel?.Breakfast to adapterPlanBreakByDateFast
            ErrorMessage.Lunch -> recipesDateModel?.Lunch to adapterLunchByDateFast
            ErrorMessage.Dinner -> recipesDateModel?.Dinner to adapterDinnerByDateFast
            ErrorMessage.Snacks -> recipesDateModel?.Dinner to adapterSnacksByDateFast
            ErrorMessage.Brunch -> recipesDateModel?.Dinner to AdapterteaTimeByDateFast
            else -> null to null
        }

        val item = mealList?.get(position!!)
        if (item != null) {
            if (item.recipe?.uri != null) {
                if (apiType.equals("add", true) || apiType.equals("minus", true)) {
                    var count = item.servings
                    if (count != null) {
                        count = when (apiType.lowercase()) {
                            "add" -> count + 1
                            "minus" -> count - 1
                            else -> count // No change if `apiType` doesn't match
                        }
                    }

                    // Create a JsonObject for the main JSON structure
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("type", type)
                    jsonObject.addProperty("uri", item.recipe.uri)
                    jsonObject.addProperty("servings", count.toString())
                    // Create a JsonArray for ingredients
                    val jsonArray = JsonArray()

                    // Iterate through the ingredients and add them to the array if status is true
                    mealList.forEach { data ->
                        // Create a JsonObject for each ingredient
                        val ingredientObject = JsonObject()
                        ingredientObject.addProperty("date", data.date)

                        ingredientObject.addProperty("day", data.day)
                        // Add the ingredient object to the array
                        jsonArray.add(ingredientObject)

                    }
                    // Add the ingredients array to the main JSON object
                    jsonObject.add("slot", jsonArray)
                    recipeServingCountData(
                        item,
                        adapter,
                        type,
                        mealList,
                        position,
                        count.toString(),
                        jsonObject
                    )

                }
            }
        }
    }

    private fun recipeServingCountData(
        item: BreakfastModelPlanByDate,
        adapter: AdapterPlanBreakByDateFast?,
        type: String,
        mealList: MutableList<BreakfastModelPlanByDate>,
        position: Int?,
        count: String,
        jsonObject: JsonObject
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.recipeServingCountRequest({
                BaseApplication.dismissMe()
                handleCountApiResponse(it, item, adapter, type, mealList, position, count)
            }, jsonObject)
        }
    }

    private fun handleCountApiResponse(
        result: NetworkResult<String>,
        item: BreakfastModelPlanByDate,
        adapter: AdapterPlanBreakByDateFast?,
        type: String,
        mealList: MutableList<BreakfastModelPlanByDate>,
        position: Int?,
        count: String
    ) {
        when (result) {
            is NetworkResult.Success -> handleCountSuccessResponse(
                result.data.toString(),
                item,
                adapter,
                type,
                mealList,
                position,
                count
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleCountSuccessResponse(
        data: String,
        item: BreakfastModelPlanByDate,
        adapter: AdapterPlanBreakByDateFast?,
        type: String,
        mealList: MutableList<BreakfastModelPlanByDate>,
        position: Int?,
        count: String,
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ count ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                item.servings = count.toInt()
                if (item != null) {
                    mealList[position!!] = item
                }
                // Update the adapter
                if (mealList != null) {
                    adapter?.updateList(mealList, type)
                }

            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun swap(type: String, position: Int?, apiType: String) {
        // Map the type to the corresponding list and adapter
        val (mealList, adapter) = when (type) {
            ErrorMessage.Breakfast -> recipesModel?.Breakfast to breakfastAdapter
            ErrorMessage.Lunch -> recipesModel?.Lunch to lunchAdapter
            ErrorMessage.Dinner -> recipesModel?.Dinner to dinnerAdapter
            ErrorMessage.Snacks -> recipesModel?.Snack to snackesAdapter
            ErrorMessage.Brunch -> recipesModel?.Teatime to teaTimeAdapter
            else -> null to null
        }

        if (position != null) {
            swapId = position
        }

        swapMealType = type

        fun setupMealTopAdapter(
            mealRecipes: MutableList<BreakfastModel>?,
            recyclerView: RecyclerView,
            type: String
        ): AdapterPlanBreakFast? {
            return if (!mealRecipes.isNullOrEmpty()) {
                val adapter = AdapterPlanBreakFast(mealRecipes, requireActivity(), this, type)
                recyclerView.adapter = adapter
                adapter
            } else {
                null
            }
        }

        fun updateMealSection(
            mealList: MutableList<BreakfastModel>?,
            recyclerView: RecyclerView,
            linearLayout: View,
            type: String
        ): AdapterPlanBreakFast? {
            return if (!mealList.isNullOrEmpty()) {
                linearLayout.visibility = View.VISIBLE
                setupMealTopAdapter(mealList, recyclerView, type)
            } else {
                linearLayout.visibility = View.GONE
                null
            }
        }

        // Optimize visibility and adapter assignment logic
        when (type) {
            ErrorMessage.Breakfast -> {
                breakfastAdapter = updateMealSection(
                    mealList,
                    binding.rcyBreakFast,
                    binding.linearBreakfast,
                    type
                )
            }

            ErrorMessage.Lunch -> {
                lunchAdapter = updateMealSection(
                    mealList,
                    binding.rcyLunch,
                    binding.linearLunch,
                    type
                )
            }

            ErrorMessage.Dinner -> {
                dinnerAdapter = updateMealSection(
                    mealList,
                    binding.rcyDinner,
                    binding.linearDinner,
                    type
                )
            }

            ErrorMessage.Snacks -> {
                snackesAdapter = updateMealSection(
                    mealList,
                    binding.rcySnacks,
                    binding.linearSnacks,
                    type
                )
            }

            ErrorMessage.Brunch -> {
                teaTimeAdapter = updateMealSection(
                    mealList,
                    binding.rcyTeatime,
                    binding.linearTeatime,
                    type
                )
            }
        }
    }

    override fun itemMealTypeSelect(position: Int?, status: String?, type: String?) {
        mealRoutineList.forEachIndexed { index, mealRoutineModelData ->
            if (index == position) {
                mealRoutineModelData.selected = !mealRoutineModelData.selected // Toggle selection
            }
        }
        mealTypeAdapter?.updateList(mealRoutineList)

    }

}