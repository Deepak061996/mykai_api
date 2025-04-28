package com.mykaimeal.planner.fragment.mainfragment.commonscreen

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
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterCookBookDetailsItem
import com.mykaimeal.planner.adapter.AdapterCookBookItem
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentCookBookBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.CookBookViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse.CookBookDataModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse.CookBookListApiResponse
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
class CookBookFragment : Fragment(), OnItemClickListener, OnItemSelectListener {

    private lateinit var binding: FragmentCookBookBinding
    private var adapterCookBookItem: AdapterCookBookItem? = null
    private var adapterCookBookDetailsItem: AdapterCookBookDetailsItem? = null
    private var tvWeekRange: TextView? = null
    private var rcyChooseDaySch: RecyclerView? = null
    private lateinit var viewModel: CookBookViewModel
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    private var cookbookListLocal: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    private var localData: MutableList<CookBookDataModel> = mutableListOf()
    private var currentDate = Date() // Current date
    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    val dataList = arrayListOf<DataModel>()
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    private lateinit var sessionManagement: SessionManagement

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentCookBookBinding.inflate(layoutInflater, container, false)
        sessionManagement = SessionManagement(requireContext())
         
        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.VISIBLE
            it.llBottomNavigation.visibility = View.VISIBLE
        }
        
        
        viewModel = ViewModelProvider(requireActivity())[CookBookViewModel::class.java]

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        cookbookList.clear()

        val data1= com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("","",0,"000","Add",0,"",R.drawable.add_more_cookbook_icon)
        val data2= com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("","",0,"001","Favorites",0,"",R.drawable.favourites_cookbook_image)
        cookbookList.add(0,data1)
        cookbookList.add(1,data2)

        initialize()



//        if ((activity as? MainActivity)?.Subscription_status==1){
//            binding.btnLock.visibility=View.VISIBLE
//
//        }else{
//            binding.btnLock.visibility=View.GONE
//        }

        binding.btnLock.setOnClickListener {
            (activity as? MainActivity)?.subscriptionAlertError(requireContext())
        }

        if (BaseApplication.isOnline(requireActivity())) {
            getCookBookList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

        return binding.root
    }

    private fun getCookBookList(){
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }
        }
    }

    private fun handleApiCookBookListResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookListResponse(result.data.toString())
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
    private fun handleSuccessCookBookListResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListApiResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                localData.clear()
                apiModel.data?.let { localData.addAll(it) }
                if (localData.size>0){
                    adapterCookBookDetailsItem = AdapterCookBookDetailsItem(localData, requireActivity(),this)
                    binding.rcyCookBookDetails.adapter = adapterCookBookDetailsItem
                    binding.rcyCookBookDetails.visibility=View.VISIBLE
                    binding.tvnoData.visibility=View.GONE
                }else{
                    binding.rcyCookBookDetails.visibility=View.GONE
                    binding.tvnoData.visibility=View.VISIBLE
                }
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
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data!=null && apiModel.data.size>0){
                    binding.llCookBookItems.visibility=View.VISIBLE
                    cookbookList.addAll(apiModel.data)
                }else{
                    binding.llCookBookItems.visibility=View.VISIBLE
                }
                // OR directly modify the original list
                adapterCookBookItem = AdapterCookBookItem(cookbookList, requireActivity(), this)
                binding.rcyCookBookAdding.adapter = adapterCookBookItem
                getCookBookTypeList()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun initialize() {

        binding.imgBackCookbook.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.rlAddRecipes.setOnClickListener {
            val bundle = Bundle().apply {
                putString("name","")
            }
            findNavController().navigate(R.id.createRecipeFragment,bundle)
        }
    }

    private fun getCookBookTypeList(){
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.getCookBookTypeRequest( {
                BaseApplication.dismissMe()
                handleApiCookBookListResponse(it)
            },"0")
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        when (status) {
            "1" -> {
                if ((activity as? MainActivity)?.Subscription_status==1){
                    if ((activity as? MainActivity)?.addmeal!! < 1){
                        chooseDayDialog(position)
                    }else{
                        (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                    }

                }else{
                    chooseDayDialog(position)
                }
            }
            "2" -> {
                if (BaseApplication.isOnline(requireActivity())) {
                    addBasketData(localData[position!!].data?.recipe!!.uri!!,type)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
            "4" -> {
                try {
                    val bundle = Bundle().apply {
                        val data= localData[position!!].data?.recipe!!.mealType?.get(0)?.split("/")
                        val formattedFoodName = data?.get(0)!!.replaceFirstChar { it.uppercase() }
                        putString("uri", localData[position].data?.recipe!!.uri!!)
                        putString("mealType", formattedFoodName)
                    }
                    findNavController().navigate(R.id.recipeDetailsFragment, bundle)
                }catch (e:Exception){
                    BaseApplication.alertError(requireContext(),e.message.toString(), false)
                }
            }
            "5" -> {
                moveRecipeDialog(position)
            }else -> {
            if ((activity as? MainActivity)?.Subscription_status==1){
                if ((activity as? MainActivity)?.favorite!! <= 2){
                    if (BaseApplication.isOnline(requireActivity())) {
                        removeRecipeDialog(position)
                    } else {
                        BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                    }
                }else{
                    (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                }

            }else{
                if (BaseApplication.isOnline(requireActivity())) {
                    removeRecipeDialog(position)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            } }
        }
    }

    private fun addBasketData(uri: String, type: String?) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.addBasketRequest({
                BaseApplication.dismissMe()
                handleBasketApiResponse(it)
            }, uri,"", type.toString())
        }
    }

    private fun handleBasketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleBasketSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleBasketSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(),apiModel.message, Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun chooseDayDialog(position: Int?) {
        val dialogChooseDay: Dialog = context?.let { Dialog(it) }!!
        dialogChooseDay.setContentView(R.layout.alert_dialog_choose_day)
        dialogChooseDay.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogChooseDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        rcyChooseDaySch = dialogChooseDay.findViewById<RecyclerView>(R.id.rcyChooseDaySch)
        tvWeekRange = dialogChooseDay.findViewById(R.id.tvWeekRange)
        val rlDoneBtn = dialogChooseDay.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        val btnPrevious = dialogChooseDay.findViewById<ImageView>(R.id.btnPrevious)
        val btnNext = dialogChooseDay.findViewById<ImageView>(R.id.btnNext)
        dialogChooseDay.show()
        dialogChooseDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dataList.clear()
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
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
                chooseDayMealTypeDialog(position)
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

    @SuppressLint("SetTextI18n")
    fun showWeekDates() {
        Log.d("currentDate :- ", "******$currentDate")
        val (startDate, endDate) = getWeekDates(currentDate)
        this.startDate=startDate
        this.endDate=endDate
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
        tvWeekRange?.text = ""+formatDate(startDate)+"-"+formatDate(endDate)

    }

    private fun getDaysBetween(startDate: Date, endDate: Date): MutableList<DateModel> {
        val dateList = mutableListOf<DateModel>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Format for the date
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault()) // Format for the day name (e.g., Monday)
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        while (!calendar.time.after(endDate)) {
            val date = dateFormat.format(calendar.time)  // Get the formatted date (yyyy-MM-dd)
            val dayName = dayFormat.format(calendar.time)  // Get the day name (Monday, Tuesday, etc.)
            val localDate= DateModel()
            localDate.day=dayName
            localDate.date=date
            // Combine both the day name and the date
            dateList.add(localDate)
            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateList
    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
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

        fun updateSelection(selectedType: String, selectedView: TextView, allViews: List<TextView>) {
            type = selectedType
            allViews.forEach { view ->
                val drawable = if (view == selectedView) R.drawable.radio_select_icon else R.drawable.radio_unselect_icon
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
                if (type.equals("",true)){
                    BaseApplication.alertError(requireContext(), ErrorMessage.mealTypeError, false)
                }else{
                    addToPlan(dialogChooseMealDay,type,position)
                }

            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

    }

    private fun addToPlan(dialogChooseMealDay: Dialog, selectType: String, position: Int?) {
        // Create a JsonObject for the main JSON structure
        val jsonObject = JsonObject()
        // Safely get the item and position
        val item = localData[position!!]
        if (item != null) {
            if (item.data?.recipe?.uri!=null){
                jsonObject.addProperty("type", selectType)
                jsonObject.addProperty("uri", item.data.recipe.uri)
                // Create a JsonArray for ingredients
                val jsonArray = JsonArray()
                val latestList=getDaysBetween(startDate, endDate)
                for (i in dataList.indices) {
                    val data=DataModel()
                    data.isOpen=dataList[i].isOpen
                    data.title=dataList[i].title
                    data.date=latestList[i].date
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
                handleApiAddToPlanResponse(it,dialogChooseMealDay)
            }, jsonObject)
        }
    }


    private fun handleApiAddToPlanResponse(result: NetworkResult<String>, dialogChooseMealDay: Dialog) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddToPlanResponse(result.data.toString(),dialogChooseMealDay)
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
                (activity as MainActivity?)?.upDateHomeData()
                Toast.makeText(requireContext(),apiModel.message,Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun removeRecipeDialog(position: Int?) {
        val dialogRemoveRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogRemoveRecipe.setContentView(R.layout.alert_dialog_remove_recipe)
        dialogRemoveRecipe.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogRemoveRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogCancelBtn = dialogRemoveRecipe.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogRemoveBtn = dialogRemoveRecipe.findViewById<TextView>(R.id.tvDialogRemoveBtn)
        dialogRemoveRecipe.show()
        dialogRemoveRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogRemoveRecipe.dismiss()
        }

        tvDialogRemoveBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                recipeLikeAndUnlikeData(position,dialogRemoveRecipe)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun recipeLikeAndUnlikeData(position: Int?, dialogRemoveRecipe: Dialog) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it,position,dialogRemoveRecipe)
            }, localData[position!!].data?.recipe?.uri.toString(),"0","")
        }
    }

    private fun handleLikeAndUnlikeApiResponse(result: NetworkResult<String>, position: Int?, dialogRemoveRecipe: Dialog) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(result.data.toString(),position,dialogRemoveRecipe)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(data: String, position: Int?, dialogRemoveRecipe: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Api Response ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                localData.removeAt(position ?: return) // Safely handle null position, return if null
                if (localData.size>0){
                    adapterCookBookDetailsItem?.updateList(localData)
                    binding.rcyCookBookDetails.visibility=View.VISIBLE
                    binding.tvnoData.visibility=View.GONE
                }else{
                    binding.rcyCookBookDetails.visibility=View.GONE
                    binding.tvnoData.visibility=View.VISIBLE
                }
                (activity as MainActivity?)?.upDateHomeData()
                dialogRemoveRecipe.dismiss()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun moveRecipeDialog(position: Int?) {
        val dialogMoveRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogMoveRecipe.setContentView(R.layout.alert_dialog_move_dialog)
        dialogMoveRecipe.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogMoveRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlMove = dialogMoveRecipe.findViewById<RelativeLayout>(R.id.rlMove)
        val imgCrossDiscardChanges = dialogMoveRecipe.findViewById<ImageView>(R.id.imgCrossDiscardChanges)

        spinnerActivityLevel = dialogMoveRecipe.findViewById(R.id.spinnerActivityLevel)
        cookbookListLocal.clear()
        cookbookListLocal.addAll(cookbookList)
        cookbookListLocal.removeFirst()
        cookbookListLocal.removeIf {
            it.id== 0
        }

        spinnerActivityLevel.setItems(cookbookListLocal.map { it.name })

        dialogMoveRecipe.show()
        dialogMoveRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        rlMove.setOnClickListener {
            if (spinnerActivityLevel.text.toString().equals("",true)){
                BaseApplication.alertError(requireContext(), ErrorMessage.selectCookBookError, false)
            }else {
                if (BaseApplication.isOnline(requireActivity())) {
                    val cookBookType = cookbookListLocal[spinnerActivityLevel.selectedIndex].id
                    recipeMove(position,dialogMoveRecipe,cookBookType)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
        imgCrossDiscardChanges.setOnClickListener {
            dialogMoveRecipe.dismiss()
        }
    }

    private fun recipeMove(position: Int?, dialogRemoveRecipe: Dialog, cookbooktype: Int) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.moveRecipeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it,position,dialogRemoveRecipe)
            }, localData[position!!].id.toString(),cookbooktype.toString())
        }
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {
        if (position == 0) {
            val bundle=Bundle()
            bundle.putString("value","New")
            bundle.putString("uri","")
            findNavController().navigate(R.id.createCookBookFragment,bundle)
        } else if (position != 1) {
            if ((activity as? MainActivity)?.Subscription_status==1){
                (activity as? MainActivity)?.subscriptionAlertError(requireContext())
            }else{
                sessionManagement.setCookBookId(cookbookList[position!!].id.toString())
                sessionManagement.setCookBookName(cookbookList[position].name)
                sessionManagement.setCookBookImage(cookbookList[position].image)
                sessionManagement.setCookBookType(cookbookList[position].status.toString())
                findNavController().navigate(R.id.christmasCollectionFragment)
            }

        }
    }

}