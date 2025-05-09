package com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterFoodListItem
import com.mykaimeal.planner.adapter.CalendarDayDateAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentCookedBinding
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.Breakfast
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.CookedTabModel
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.CookedTabModelData
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.viewmodel.CookedTabViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DateModel
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CookedFragment : Fragment(), OnItemClickListener {

    private lateinit var binding: FragmentCookedBinding
    private lateinit var cookedTabViewModel: CookedTabViewModel
    private var foodListBreakFastAdapter: AdapterFoodListItem? = null
    private var foodListLunchAdapter: AdapterFoodListItem? = null
    private var foodListDinnerAdapter: AdapterFoodListItem? = null
    private var foodListSnacksAdapter: AdapterFoodListItem? = null
    private var foodListTeaTimeAdapter: AdapterFoodListItem? = null
    private var planType: String = "1"
    private var currentDate = Date() // Current date
    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private var currentDateSelected: String = ""
    private var calendarAdapter: CalendarDayDateAdapter? = null
    private var recipesModel: CookedTabModelData? = null
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()

    var updatedDaysBetween: List<DateModel> = emptyList()
    private var lastDateSelected: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        binding = FragmentCookedBinding.inflate(inflater, container, false)
        (activity as MainActivity?)?.changeBottom("cooked")
        (activity as MainActivity?)?.alertStatus=false

        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.VISIBLE
            it.llBottomNavigation.visibility = View.VISIBLE
        }

        currentDateSelected = BaseApplication.currentDateFormat().toString()
        lastDateSelected=currentDateSelected
        cookedTabViewModel = ViewModelProvider(requireActivity())[CookedTabViewModel::class.java]
        cookbookList.clear()

        val data= com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("","",0,"","Favorites",0,"",0)
        cookbookList.add(0,data)
         
        backButton()

        initialize()

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {

        binding.textFridge.setOnClickListener { updateUI(true) }

        binding.textFreezer.setOnClickListener { updateUI(false) }

        // Display current week dates
        showWeekDates()

        binding.imageBreakFastCreate.setOnClickListener {
            findNavController().navigate(R.id.addMealCookedFragment)
        }

        binding.imageLunchCreate.setOnClickListener {
            findNavController().navigate(R.id.addMealCookedFragment)
        }

        binding.imageDinnerCreate.setOnClickListener {
            findNavController().navigate(R.id.addMealCookedFragment)
        }

        binding.imageSnacksCreate.setOnClickListener {
            findNavController().navigate(R.id.addMealCookedFragment)
        }

        binding.imageTeaTimeCreate.setOnClickListener {
            findNavController().navigate(R.id.addMealCookedFragment)
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

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.relCalendarYear.setOnClickListener {
            if (binding.llCalendarViewEvents.visibility == View.VISIBLE) {
                binding.llCalendarViewEvents.visibility = View.GONE
                binding.dropdown.setBackgroundResource(R.drawable.drop_down_small_icon)
            } else {
                binding.llCalendarViewEvents.visibility = View.VISIBLE
                binding.dropdown.setBackgroundResource(R.drawable.drop_up_small_icon)
            }
        }

        binding.textAddMeals.setOnClickListener {
            findNavController().navigate(R.id.addMealCookedFragment)
        }


//        if (cookedTabViewModel.data!=null){
//            showDataInUi(cookedTabViewModel.data)
//        }else{
//            updateUI(true)
//        }

        updateUI(true)

    }


    private fun loadApi(){
        if (BaseApplication.isOnline(requireActivity())) {
            cookedTabApi(currentDateSelected)
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hidPastDate(){
        if (updatedDaysBetween.isNotEmpty()){
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
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun showWeekDates() {
        val (startDate, endDate) = getWeekDates(currentDate)
        this.startDate = startDate
        this.endDate = endDate
        // Get all dates between startDate and endDate
        val daysBetween = getDaysBetween(startDate, endDate)
        daysBetween.forEach { println(it) }
        binding.textMonthAndYear.text = BaseApplication.formatonlyMonthYear(startDate)
        binding.textWeekRange.text = "" + formatDate(startDate) + "-" + formatDate(endDate)
        // Update the RecyclerView
        updatedDaysBetween = daysBetween.map { dateModel ->
            dateModel.apply {
                status = (date == lastDateSelected) // Compare formatted strings
            }
        }

        calendarAdapter = CalendarDayDateAdapter(updatedDaysBetween.toMutableList()) {
            // Handle item click if needed
            val dateList = getDaysBetween(startDate, endDate)
            // Update the status of the item at the target position
            dateList.forEachIndexed { index, dateModel ->
                dateModel.status = index == it

            }
            lastDateSelected=dateList[it].date
            currentDateSelected = dateList[it].date
            Log.d("Date ", "*****$dateList")
            // Notify the adapter to refresh the changed position
            calendarAdapter!!.updateList(dateList)
            loadApi()
        }
        // Update the RecyclerView
        binding.recyclerViewWeekDays.adapter = calendarAdapter

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
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault()) // Format for the day name (e.g., Monday)

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
            dateList.add(localDate)
            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dateList
    }

    private fun cookedTabApi(date: String) {
        BaseApplication.showMe(requireActivity())
        lifecycleScope.launch {
            cookedTabViewModel.cookedDateRequest({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val cookedModel = gson.fromJson(it.data, CookedTabModel::class.java)
                            if (cookedModel.code == 200 && cookedModel.success) {
                                showDataInUi(cookedModel.data)
                            } else {
                                handleError(cookedModel.code,cookedModel.message)
                            }
                        }catch (e:Exception){
                            showAlertFunction(e.message, false)
                        }

                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, date, planType)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        } else {
            showAlertFunction(message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDataInUi(cookedTabModelData: CookedTabModelData?) {
        try {
            if (cookedTabModelData != null) {
                recipesModel = cookedTabModelData
                if (cookedTabModelData.fridge != null && cookedTabModelData.freezer != null) {
                    fun updateFridgeVisibility(condition: Boolean) {
                        if (condition) {
                            binding.llEmptyFridge.visibility = View.VISIBLE
                            binding.llFilledFridge.visibility = View.GONE
                        } else {
                            binding.llEmptyFridge.visibility = View.GONE
                            binding.llFilledFridge.visibility = View.VISIBLE
                        }
                    }

                    if (planType.equals("1", true)) {
                        updateFridgeVisibility(cookedTabModelData.fridge == 0)
                    } else {
                        updateFridgeVisibility(cookedTabModelData.freezer == 0)
                    }

                    binding.textFreezer.text = "Freezer (" + cookedTabModelData.freezer.toString() + ")"
                    binding.textFridge.text = "Fridge (" + cookedTabModelData.fridge.toString() + ")"

                }else{
                    binding.llEmptyFridge.visibility = View.VISIBLE
                    binding.llFilledFridge.visibility = View.GONE
                }

                fun setupMealAdapter(mealRecipes: MutableList<Breakfast>?, recyclerView: RecyclerView, type: String): AdapterFoodListItem? {
                    return if (mealRecipes != null && mealRecipes.isNotEmpty()) {
                        val adapter = AdapterFoodListItem(mealRecipes,type, requireActivity(), this)
                        recyclerView.adapter = adapter
                        adapter
                    } else {
                        null
                    }
                }

                // Breakfast
                if (cookedTabModelData.Breakfast != null && cookedTabModelData.Breakfast.size >0) {
                    foodListBreakFastAdapter = setupMealAdapter(cookedTabModelData.Breakfast, binding.rcvBreakfast, ErrorMessage.Breakfast)
                    binding.rlBreakfast.visibility = View.VISIBLE
                } else {
                    binding.rlBreakfast.visibility = View.GONE
                }

                // Lunch
                if (cookedTabModelData.Lunch != null && cookedTabModelData.Lunch.size >0) {
                    foodListLunchAdapter = setupMealAdapter(cookedTabModelData.Lunch, binding.rcvLunch, ErrorMessage.Lunch)
                    binding.rlLunch.visibility = View.VISIBLE
                } else {
                    binding.rlLunch.visibility = View.GONE
                }

                // Dinner
                if (cookedTabModelData.Dinner != null && cookedTabModelData.Dinner.size >0) {
                    foodListDinnerAdapter = setupMealAdapter(cookedTabModelData.Dinner, binding.rcvDinner, ErrorMessage.Dinner)
                    binding.relDinner.visibility = View.VISIBLE
                } else {
                    binding.relDinner.visibility = View.GONE
                }

                // Snacks
                if (cookedTabModelData.Snacks != null && cookedTabModelData.Snacks.size >0) {
                    foodListSnacksAdapter = setupMealAdapter(cookedTabModelData.Snacks, binding.rcvSnacks, ErrorMessage.Snacks)
                    binding.relSnacks.visibility = View.VISIBLE
                } else {
                    binding.relSnacks.visibility = View.GONE
                }

                // Teatime
                if (cookedTabModelData.Teatime != null && cookedTabModelData.Teatime.size >0) {
                    foodListTeaTimeAdapter = setupMealAdapter(cookedTabModelData.Teatime, binding.rcvTeaTime, ErrorMessage.Brunch)
                    binding.relTeaTime.visibility = View.VISIBLE
                } else {
                    binding.relTeaTime.visibility = View.GONE
                }

            }
        }catch (e:Exception){
            Log.d("CookedScreen","message:--"+e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun updateUI(isFridgeSelected: Boolean) {
        binding.textFridge.setBackgroundResource(if (isFridgeSelected) R.drawable.selected_button_bg else R.drawable.unselected_button_bg)
        binding.textFreezer.setBackgroundResource(if (isFridgeSelected) R.drawable.unselected_button_bg else R.drawable.selected_button_bg)
        binding.textFridge.setTextColor(if (isFridgeSelected) Color.WHITE else Color.BLACK)
        binding.textFreezer.setTextColor(if (isFridgeSelected) Color.BLACK else Color.WHITE)
        binding.llFilledFridge.visibility = if (isFridgeSelected) View.VISIBLE else View.GONE
        binding.llEmptyFridge.visibility = View.GONE
        planType = if (isFridgeSelected) "1" else "2"
//        cookedTabViewModel.type=planType
        loadApi()
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        when (status) {
            "1" -> {
                if ((activity as? MainActivity)?.Subscription_status==1){
                    if ((activity as? MainActivity)?.favorite!! <= 2){
                        if (BaseApplication.isOnline(requireActivity())) {
                            removeAddServing(type ?: "", position, "like")
                        } else {
                            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                        }
                    }else{
                        (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                    }

                }else{
                    if (BaseApplication.isOnline(requireActivity())) {
                        removeAddServing(type ?: "", position, "like")
                    } else {
                        BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                    }
                }
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
                    removeAddServing(type ?: "", position, "remove")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
            "4" -> {
                if (BaseApplication.isOnline(requireActivity())) {
                    removeAddServing(type ?: "", position, "minus")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun removeAddServing(type: String, position: Int?, apiType: String) {
        // Map the type to the corresponding list and adapter
        val (mealList, adapter) = when (type) {
            ErrorMessage.Breakfast -> recipesModel?.Breakfast to foodListBreakFastAdapter
            ErrorMessage.Lunch -> recipesModel?.Lunch to foodListLunchAdapter
            ErrorMessage.Dinner -> recipesModel?.Dinner to foodListDinnerAdapter
            ErrorMessage.Snacks -> recipesModel?.Snacks to foodListSnacksAdapter
            ErrorMessage.Brunch -> recipesModel?.Teatime to foodListTeaTimeAdapter
            else -> null to null
        }

        val item = mealList?.get(position!!)
        if (item != null) {
            if (item.recipe?.uri !=null){
                if (apiType.equals("like",true)){
                    val newLikeStatus = if (item.is_like == 0) "1" else "0"
                    if (newLikeStatus.equals("0",true)){
                        recipeLikeAndUnlikeData(item, adapter, type, mealList, position, newLikeStatus,"",null)
                    }else{
                        addFavTypeDialog(item, adapter, type, mealList, position, newLikeStatus)
                    }
                }
                if (apiType.equals("remove",true)){
                    removeMealDialog(item, adapter, type, mealList, position)
                }

                if (apiType.equals("add",true) || apiType.equals("minus",true)) {
                    var count = item.servings
                    count = when (apiType.lowercase()) {
                        "add" -> count!! + 1
                        "minus" -> count!! - 1
                        else -> count // No change if `apiType` doesn't match
                    }
                    // Create a JsonObject for the main JSON structure
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("type", type)
                    jsonObject.addProperty("plan_type", planType)
                    jsonObject.addProperty("uri", item.recipe.uri)
                    jsonObject.addProperty("date", item.date)
                    jsonObject.addProperty("servings", String.format("%02d", count))

                    Log.d("json object ", "******$jsonObject")

                    recipeServingCountData(item, adapter, type, mealList, position, count.toString(),jsonObject)
                }
            }
        }
    }

    private fun recipeLikeAndUnlikeData(
        item: Breakfast?,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        likeType: String,
        cookbooktype: String,
        dialogAddRecipe: Dialog?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookedTabViewModel?.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it,item,adapter,type,mealList,position,dialogAddRecipe)
            }, item?.recipe?.uri!!,likeType,cookbooktype)
        }
    }


    private fun recipeServingCountData(
        item: Breakfast?,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        count: String,
        jsonObject: JsonObject
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookedTabViewModel?.recipeServingCountRequest({
                BaseApplication.dismissMe()
                handleCountApiResponse(it,item,adapter,type,mealList,position,count)
            }, jsonObject)
        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        item: Breakfast?,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(result.data.toString(),item,adapter,type,mealList,position,dialogAddRecipe)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleCountApiResponse(
        result: NetworkResult<String>,
        item: Breakfast?,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        count: String,
        ) {
        when (result) {
            is NetworkResult.Success -> handleCountSuccessResponse(result.data.toString(),item,adapter,type,mealList,position,count)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(
        data: String,
        item: Breakfast?,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogAddRecipe?.dismiss()
                // Toggle the is_like value
                item?.is_like = if (item?.is_like == 0) 1 else 0
                if (item != null) {
                    mealList?.set(position!!, item)
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

    @SuppressLint("SetTextI18n")
    private fun handleCountSuccessResponse(
        data: String,
        item: Breakfast?,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        count: String,
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ count ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                item?.servings = count.toInt()
                if (item != null) {
                    mealList?.set(position!!, item)
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

    private fun addFavTypeDialog(
        item: Breakfast?,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        likeType: String) {
        val dialogAddRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogAddRecipe.setContentView(R.layout.alert_dialog_add_recipe)
        dialogAddRecipe.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogAddRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogAddRecipe.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        spinnerActivityLevel = dialogAddRecipe.findViewById(R.id.spinnerActivityLevel)
        val relCreateNewCookBook = dialogAddRecipe.findViewById<RelativeLayout>(R.id.relCreateNewCookBook)
        val imgCheckBoxOrange = dialogAddRecipe.findViewById<ImageView>(R.id.imgCheckBoxOrange)

        spinnerActivityLevel.setItems(cookbookList.map { it.name })

        dialogAddRecipe.show()
        dialogAddRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        getCookBookList()


        relCreateNewCookBook.setOnClickListener{
            relCreateNewCookBook.setBackgroundResource(R.drawable.light_green_rectangular_bg)
            imgCheckBoxOrange.setImageResource(R.drawable.orange_uncheck_box_images)
            dialogAddRecipe.dismiss()
            val bundle=Bundle()
            bundle.putString("value","New")
            bundle.putString("uri",item?.recipe?.uri)
            findNavController().navigate(R.id.createCookBookFragment,bundle)
        }


        rlDoneBtn.setOnClickListener{
            if (spinnerActivityLevel.text.toString().equals("",true)){
                BaseApplication.alertError(requireContext(), ErrorMessage.selectCookBookError, false)
            }else {
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

    private fun getCookBookList(){
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookedTabViewModel?.getCookBookRequest {
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
                if (apiModel.data!=null && apiModel.data.size>0){
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

    private fun removeMealDialog(
        item: Breakfast,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>,
        position: Int?
    ) {
        val dialogRemoveDay: Dialog = context?.let { Dialog(it) }!!
        dialogRemoveDay.setContentView(R.layout.alert_dialog_remove_cooked_meals)
        dialogRemoveDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogRemoveDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogNoBtn = dialogRemoveDay.findViewById<TextView>(R.id.tvDialogNoBtn)
        val tvDialogYesBtn = dialogRemoveDay.findViewById<TextView>(R.id.tvDialogYesBtn)
        dialogRemoveDay.show()
        dialogRemoveDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogNoBtn.setOnClickListener {
            dialogRemoveDay.dismiss()
        }

        tvDialogYesBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                removeCookBookApi(item.id.toString(), dialogRemoveDay, adapter, type,mealList,position)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun removeCookBookApi(
        cookedId: String,
        dialogRemoveDay: Dialog,
        adapter: AdapterFoodListItem?,
        type: String,
        mealList: MutableList<Breakfast>,
        position: Int?
    ) {
        BaseApplication.showMe(requireActivity())
        lifecycleScope.launch {
            cookedTabViewModel!!.removeMealApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        val gson = Gson()
                        val cookedModel = gson.fromJson(it.data, CookedTabModel::class.java)
                        if (cookedModel.code == 200 && cookedModel.success) {
                            try {
                                // Remove item from the list
                                mealList.removeAt(position!!)
                                val count = listOf(
                                    recipesModel?.Breakfast?.size ?: 0,
                                    recipesModel?.Lunch?.size ?: 0,
                                    recipesModel?.Dinner?.size ?: 0,
                                    recipesModel?.Snacks?.size ?: 0,
                                    recipesModel?.Teatime?.size ?: 0
                                ).sum().let { total ->
                                    if (total == 0) 0 else total
                                }


                                // Define meal types and corresponding UI elements
                                val mealVisibilityMap = mapOf(
                                    ErrorMessage.Breakfast to binding.rlBreakfast,
                                    ErrorMessage.Lunch to binding.rlLunch,
                                    ErrorMessage.Dinner to binding.relDinner,
                                    ErrorMessage.Snacks to binding.relSnacks,
                                    ErrorMessage.Brunch to binding.relTeaTime
                                )

                                // Update adapter and visibility
                                mealVisibilityMap[type]?.let { view ->
                                    if (mealList.isNotEmpty()) {
                                        adapter?.updateList(mealList, type)
                                        view.visibility = View.VISIBLE
                                    } else {
                                        view.visibility = View.GONE
                                    }
                                }

                                // Dismiss the dialog
                                dialogRemoveDay.dismiss()

                                if (planType.equals("1",true)){
                                    binding.textFridge.text = "Freezer ($count)"
                                }else{
                                    binding.textFreezer.text = "Freezer ($count)"
                                }

                            }catch (e:Exception){
                                Log.d("@@@@@@","Error response "+e.message)
                            }

                        } else {
                            handleError(cookedModel.code,cookedModel.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, cookedId)
        }
    }

}