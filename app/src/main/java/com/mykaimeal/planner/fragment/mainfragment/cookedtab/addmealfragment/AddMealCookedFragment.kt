package com.mykaimeal.planner.fragment.mainfragment.cookedtab.addmealfragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemMealTypeListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.activity.SplashActivity
import com.mykaimeal.planner.adapter.AdapterMealTypeMeal
import com.mykaimeal.planner.adapter.SearchAdapterItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentAddMealCookedBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.addmealfragment.viewmodel.AddMealCookedViewModel
import com.mykaimeal.planner.fragment.mainfragment.plantab.ImagesDeserializer
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.Recipe
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SearchModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SearchModelData
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.ImagesModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AddMealCookedFragment : Fragment(), OnItemClickListener, OnItemMealTypeListener {
    private lateinit var binding: FragmentAddMealCookedBinding
    private lateinit var addMealCookedViewModel: AddMealCookedViewModel
    private var searchAdapterItem: SearchAdapterItem? = null
    private var recipes: List<Recipe>? = null
    private var quantity: Int = 1
    private var lastSelectedDate: Long? = null
    private var selectedDate: String = ""
    private var status: String = ""
    private var clickable: String = ""
    private var mealType: String = ""
    private var recipeUri: String = ""
    private var planType: String = "1"
    private var popupWindow: PopupWindow? = null
    private lateinit var textListener: TextWatcher
    private var textChangedJob: Job? = null
    private var mealRoutineList: MutableList<MealRoutineModelData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddMealCookedBinding.inflate(layoutInflater, container, false)

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        addMealCookedViewModel = ViewModelProvider(this)[AddMealCookedViewModel::class.java]

        backButton()

        initialize()

        return binding.root
    }

    private fun backButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun initialize() {

        binding.relDateCalendar.setOnClickListener {
            openDialog()
        }

        binding.imageBackAddMeal.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvTitleName.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                val mainActivity = requireActivity() as MainActivity
                mainActivity.mealRoutineSelectApi { data ->
                    mealRoutineList.clear()
                    mealRoutineList.addAll(data)
                    if (mealRoutineList.isNotEmpty()) {
                        mealType()
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

        binding.textFridge.setOnClickListener {
            updateButtonState(isFridgeSelected = true)
        }

        binding.textFreezer.setOnClickListener {
            updateButtonState(isFridgeSelected = false)
        }


        binding.imgPlusValue.setOnClickListener {
            if (quantity < 99) {
                quantity++
                updateValue()
            }
        }

        binding.imgMinusValue.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateValue()
            } else {
                Toast.makeText(requireActivity(), ErrorMessage.servingError, Toast.LENGTH_LONG)
                    .show()
            }
        }

        binding.testAddMeals.setOnClickListener {
            if (clickable == "2") {
                if (BaseApplication.isOnline(requireActivity())) {
                    if ((activity as? MainActivity)?.Subscription_status==1){
                        if ((activity as? MainActivity)?.addmeal!! < 1){
                            addMealsApi()
                        }else{
                            (activity as? MainActivity)?.subscriptionAlertError()
                        }
                    }else{
                        addMealsApi()
                    }
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }


        textListener = object : TextWatcher {
            private var searchFor = "" // Or view.editText.text.toString()

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (searchText != searchFor) {
                    searchFor = searchText
                    textChangedJob?.cancel()
                    // Launch a new coroutine in the lifecycle scope
                    textChangedJob = lifecycleScope.launch {
                        delay(1000)  // Debounce time
                        if (searchText == searchFor) {
                            if (BaseApplication.isOnline(requireActivity())) {
                                searchRecipeApi(searchText)
                            } else {
                                BaseApplication.alertError(
                                    requireContext(),
                                    ErrorMessage.networkError,
                                    false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateButtonState(isFridgeSelected: Boolean) {
        // Update backgrounds
        binding.textFridge.setBackgroundResource(if (isFridgeSelected) R.drawable.selected_button_bg else R.drawable.unselected_button_bg)
        binding.textFreezer.setBackgroundResource(if (isFridgeSelected) R.drawable.unselected_button_bg else R.drawable.selected_button_bg)

        // Update text colors
        binding.textFridge.setTextColor(if (isFridgeSelected) Color.WHITE else Color.BLACK)
        binding.textFreezer.setTextColor(if (isFridgeSelected) Color.BLACK else Color.WHITE)

        // Update plan type
        planType = if (isFridgeSelected) "1" else "2"

        // Update text based on status
        if (status == "2") {
            if (isFridgeSelected) {
                binding.textFridge.text = "Fridge (1)"
                binding.textFreezer.text = "Freezer (0)"
            } else {
                binding.textFridge.text = "Fridge (0)"
                binding.textFreezer.text = "Freezer (1)"
            }
        } else {
            binding.textFridge.text = "Fridge (0)"
            binding.textFreezer.text = "Freezer (0)"
        }
    }


    private fun addMealsApi() {
        // Create a JsonObject for the main JSON structure
        val jsonObject = JsonObject()
        if (recipeUri != null) {
            jsonObject.addProperty("type", binding.tvTitleName.text.toString().trim())
            jsonObject.addProperty("plan_type", planType)
            jsonObject.addProperty("uri", recipeUri)
            jsonObject.addProperty("date", selectedDate)
            jsonObject.addProperty("serving", String.format("%02d", quantity))
        }

        Log.d("json object ", "******$jsonObject")

        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            addMealCookedViewModel.recipeAddToPlanRequest({
                BaseApplication.dismissMe()
                handleApiAddToPlanResponse(it)
            }, jsonObject)
        }
    }


    private fun mealType() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_select_layoutdrop, null)
        popupWindow = PopupWindow(popupView, 400, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow?.showAsDropDown(binding.laytype, 0, 0, Gravity.CENTER)

        // Access views inside the inflated layout using findViewById
        val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)
////
        rcyData?.adapter = AdapterMealTypeMeal(mealRoutineList, requireContext(), this)

        binding.tvTitleName.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.drop_up_icon,
            0
        )

        // Set the dismiss listener
        popupWindow?.setOnDismissListener {
            binding.tvTitleName.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.drop_down_icon,
                0
            )
        }

    }

    private fun handleApiAddToPlanResponse(
        result: NetworkResult<String>
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddToPlanResponse(
                result.data.toString()
            )

            is NetworkResult.Error -> showAlertFunction(result.message, false)
            else -> showAlertFunction(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddToPlanResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMeal List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                binding.imageLogo.setImageResource(R.drawable.add_meal_icon_success)

                lifecycleScope.launch {
                    delay(SplashActivity.SPLASH_DELAY)
                    findNavController().navigate(R.id.cookedFragment)
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlertFunction(apiModel.message, true)
                } else {
                    showAlertFunction(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlertFunction(e.message, false)
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateValue() {
        binding.tvServing.text = "serves" + String.format("%02d", quantity)
    }

    private fun openDialog() {

        val dialog = Dialog(requireActivity())
        // Set custom layout
        dialog.setContentView(R.layout.dialog_calendar)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val calendarView = dialog.findViewById<CalendarView>(R.id.calendar)

        dialog.setOnShowListener {
            calendarView?.date = lastSelectedDate ?: Calendar.getInstance().timeInMillis
        }
        // Get today's date
        val today = Calendar.getInstance()
        // Set the minimum date to today
        calendarView?.minDate = today.timeInMillis

        calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Create a Calendar instance
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            calendar.set(year, month, dayOfMonth)
            lastSelectedDate = calendar.timeInMillis // Store the selected date

            // Format the selected date to "YYYY-MM-DD"
            val dateFormatForApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDateApi = dateFormatForApi.format(calendar.time)

            selectedDate = formattedDateApi
            // Format the date to "17 January 2025"
            val dateFormatForShow = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            val formattedDateShow = dateFormatForShow.format(calendar.time)

            binding.tvDateCooked.text = formattedDateShow

            checkable()
            // Dismiss the dialog
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun checkable() {
        if (selectedDate != "") {
            if (status == "2") {
                clickable = "2"
                binding.testAddMeals.setBackgroundResource(R.drawable.green_btn_background)
            } else {
                binding.testAddMeals.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            }
        } else {
            binding.testAddMeals.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }

    private fun searchRecipeApi(searchText: String) {
//        binding.layProgress.visibility=View.VISIBLE
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            addMealCookedViewModel.recipeSearchApi({
                binding.layProgress.visibility = View.GONE
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            /*  val gson = Gson()*/
                            val gson = GsonBuilder()
                                .registerTypeAdapter(ImagesModel::class.java, ImagesDeserializer())
                                .create()

                            val searchModel = gson.fromJson(it.data, SearchModel::class.java)
                            if (searchModel.code == 200 && searchModel.success) {
                                searchModel.data?.let { it1 -> showDataInUi(it1) }
                            } else {
                                popupWindow?.dismiss()
                                if (searchModel.code == ErrorMessage.code) {
                                    showAlertFunction(searchModel.message, true)
                                } else {
                                    if (!searchModel.message.equals("Search query cannot be empty.")) {
                                        showAlertFunction(searchModel.message, false)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            popupWindow?.dismiss()
                            Log.d("AddMeal", "message:--" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        popupWindow?.dismiss()
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        popupWindow?.dismiss()
                        showAlertFunction(it.message, false)
                    }
                }
            }, searchText)
        }
    }

    private fun showDataInUi(searchModelData: SearchModelData) {
        try {
            if (searchModelData != null) {
                if (searchModelData.recipes != null && searchModelData.recipes.size > 0) {
                    recipes = searchModelData.recipes
                    loadSearch()
                } else {
                    popupWindow?.dismiss()
                }
            }
        } catch (e: Exception) {
            popupWindow?.dismiss()
            Log.d("AddMeal", "message:--" + e.message)
        }
    }


    private fun loadSearch() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_select_layoutdrop, null)
        // Allows dismissing the popup when touching outside
        popupWindow?.isOutsideTouchable = true
        popupWindow = PopupWindow(popupView, binding.relCookedMeals.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow?.showAsDropDown(binding.relCookedMeals, 0, 0, Gravity.CENTER)
        val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)
        searchAdapterItem = recipes?.let { SearchAdapterItem(it, requireActivity(), this) }
        rcyData!!.adapter = searchAdapterItem
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    override fun itemClick(position: Int?, uri: String?, type: String?) {

        if (planType == "1") {
            binding.textFridge.text = "Fridge (1)"
            binding.textFreezer.text = "Freezer (0)"
        } else {
            binding.textFridge.text = "Fridge (0)"
            binding.textFreezer.text = "Freezer (1)"
        }

        popupWindow?.dismiss()
        binding.etCookedDishes.text.clear()
        mealType = type.toString()
        recipeUri = uri.toString()
        status = "2"
        binding.cardViewSearchRecipe.visibility = View.GONE
        binding.cardViewRecipe.visibility = View.VISIBLE

        if (recipes!![position!!].recipe != null) {
            if (recipes!![position].recipe?.image != null) {
                if (recipes!![position].recipe?.images?.SMALL?.url != null) {
                    val imageUrl = recipes!![position].recipe?.images?.SMALL?.url
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
                        .into(binding.imgIngRecipe)


                    Glide.with(requireActivity())
                        .load(imageUrl)
                        .error(R.drawable.add_meal_icon)
                        .placeholder(R.drawable.add_meal_icon)
                        .into(binding.imageLogo)
                } else {
                    binding.layProgess.root.visibility = View.GONE
                }
            }
        }

        binding.tvTitleName.text = type.toString()
        binding.tvName.visibility = View.VISIBLE
        binding.tvName.text = recipes!![position].recipe?.label

        checkable()

    }

    override fun onResume() {
        super.onResume()
        binding.etCookedDishes.addTextChangedListener(textListener)
    }

    override fun onPause() {
        binding.etCookedDishes.removeTextChangedListener(textListener)
        super.onPause()
    }

    override fun itemMealTypeSelect(position: Int?, status: String?, type: String?) {
        popupWindow?.dismiss()
        binding.tvTitleName.text = mealRoutineList[position!!].name
    }
}