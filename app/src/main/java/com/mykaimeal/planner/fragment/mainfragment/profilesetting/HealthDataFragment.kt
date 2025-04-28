package com.mykaimeal.planner.fragment.mainfragment.profilesetting

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentHealthDataBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.ApiModelBMR
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.SettingViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.ProfileRootResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class HealthDataFragment : Fragment() {

    private var _binding: FragmentHealthDataBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingViewModel
    private var genderType: String = ""
    private lateinit var textListener: TextWatcher
    private lateinit var textListener1: TextWatcher
    private var textChangedJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthDataBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SettingViewModel::class.java]

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }



        setupBackNavigation()

        if (viewModel.getProfileData() != null) {
            showDataInUi(viewModel.getProfileData()!!)
        } else {
            // This condition is true when network condition is enable and call the api if condition is true
            if (BaseApplication.isOnline(requireActivity())) {
                getUserProfileData()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }



        setupUi()

        return binding.root
    }

    private fun setupUi() {

        binding.spinnerActivityLevel.setItems(listOf("Sedentary", "Lightly active", "Moderately active", "Very active", "Super active"))

        binding.spinnerHeight.setItems(listOf("Inch", "Centimeter", "Feet"))

        binding.spinnerweight.setItems(listOf("Kilograms","Pounds","Stones"))


        binding.spinnerHeight.setIsFocusable(true)

        binding.spinnerweight.setIsFocusable(true)

        binding.spinnerActivityLevel.setIsFocusable(true)


        fun setupSpinnerListener(spinner: PowerSpinnerView) {
            spinner.setOnSpinnerItemSelectedListener<String> { _, _, _, _ ->
                if (binding.rlAddMoreGoals.visibility == View.GONE) {
                    logicBMR("2")
                }
            }


        }


       // Apply the listener to all relevant spinners
        setupSpinnerListener(binding.spinnerHeight)
        setupSpinnerListener(binding.spinnerweight)
        setupSpinnerListener(binding.spinnerActivityLevel)



        binding.imgBackHealthData.setOnClickListener {
            viewModel.clearData()
            findNavController().navigateUp()
        }

        binding.rlAddMoreGoals.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (isValidation()) {
                    logicBMR("1")
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }

        }

        binding.imageEditTargets.setOnClickListener {
            findNavController().navigate(R.id.nutritionGoalFragment)
        }

        binding.textMale.setOnClickListener { selectGender(true) }

        binding.textFemale.setOnClickListener { selectGender(false) }

        binding.etDateOfBirth.setOnClickListener {
            openCalendarBox()
        }

        binding.layBottom.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (isValidation()) {
                    upDateProfile()
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        textListener = object : TextWatcher {
            private var searchFor = "" // Or view.editText.text.toString()
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.rlAddMoreGoals.visibility == View.GONE) {
                    val searchText = s.toString().trim()
                    if (searchText.isNotEmpty()){
                        if (!searchText.trim().equals(searchFor,true)) {
                            searchFor = searchText
                            textChangedJob?.cancel()
                            // Launch a new coroutine in the lifecycle scope
                            textChangedJob = lifecycleScope.launch {
                                delay(1000)  // Debounce time
                                if (searchText.equals(searchFor,true)) {
                                    logicBMR("2")
                                }
                            }
                        }
                    }

                }
            }
        }

        textListener1 = object : TextWatcher {
            private var searchFor = "" // Or view.editText.text.toString()
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.rlAddMoreGoals.visibility == View.GONE) {
                    val searchText = s.toString().trim()
                    if (searchText.isNotEmpty()){
                        if (!searchText.trim().equals(searchFor,true)) {
                            searchFor = searchText
                            textChangedJob?.cancel()
                            // Launch a new coroutine in the lifecycle scope
                            textChangedJob = lifecycleScope.launch {
                                delay(1000)  // Debounce time
                                if (searchText.equals(searchFor,true)) {
                                    logicBMR("2")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun logicBMR(apiType: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.updateDietSuggestionUrl({
                    BaseApplication.dismissMe()
                if (apiType.equals("1",true)){
                    handleApiUpdateResponse(it,"BMR")
                }else{
                    handleApiUpdateResponse(it,"BMRUPDATE")
                } },
                genderType,
                binding.etDateOfBirth.text.toString() ,
                binding.etHeight.text.toString(),
                binding.spinnerHeight.text.toString().trim(),
                binding.etweight.text.toString(),
                binding.spinnerweight.text.toString().trim(),
                binding.spinnerActivityLevel.text.toString().trim()
            )
        }
    }

    private fun upDateProfile() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.upDateProfileRequest(
                {
                    BaseApplication.dismissMe()
                    handleApiUpdateResponse(it,"Main")
                },
                viewModel.getProfileData()?.name.toString(),
                viewModel.getProfileData()?.bio.toString(),
                genderType,
                binding.etDateOfBirth.text.toString(),
                binding.etHeight.text.toString(),
                binding.spinnerHeight.text.toString().trim(),
                binding.spinnerActivityLevel.text.toString().trim(),
                viewModel.getProfileData()?.height_protein.toString(),
                viewModel.getProfileData()?.calories.toString(),
                viewModel.getProfileData()?.fat.toString(),
                viewModel.getProfileData()?.carbs.toString(),
                viewModel.getProfileData()?.protien.toString(),
                binding.etweight.text.toString(),
                binding.spinnerweight.text.toString().trim()
            )
        }
    }

    private fun handleApiUpdateResponse(result: NetworkResult<String>,type:String) {
        when (result) {
            is NetworkResult.Success -> handleUpdateSuccessResponse(result.data.toString(),type)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun isValidation(): Boolean {

        if (binding.etDateOfBirth.text.toString().equals("mm/dd/yyyy", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.dobError, false)
            return false
        } else if (binding.etHeight.text.toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.heightError, false)
            return false
        } else if (binding.spinnerHeight.text.toString().equals("Type", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.typeError, false)
            return false
        } else if (binding.spinnerActivityLevel.text.toString().equals("Select Your Activity Level", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.activityTypeError, false)
            return false
        }

        return true
    }


    // This function is use for open the Calendar
    @SuppressLint("SetTextI18n")
    private fun openCalendarBox() {
        // Get the current calendar instance
        val calendar = Calendar.getInstance()

        // Extract the current year, month, and day
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog with the current date and minimum date set to today
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, selectedYear, selectedMonth, selectedDay ->

                // Update the TextView with the selected date
                val date = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                Log.d("******", "" + date)
                binding.etDateOfBirth.text = date
                if (binding.rlAddMoreGoals.visibility ==  View.GONE){
                    logicBMR("2")
                }
            },
            year,
            month,
            day
        )

        // Disable previous dates
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        // Show the date picker dialog
        datePickerDialog.show()
    }


    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.clearData()
                    findNavController().navigateUp()
                }
            }
        )
    }


    // This function is use for get the user profile data when api call
    private fun getUserProfileData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.userProfileData {
                BaseApplication.dismissMe()
                handleApiResponse(it)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleUpdateSuccessResponse(data: String, type: String) {
        try {
            if (type.equals("Main",true)){
                val apiModel = Gson().fromJson(data, ProfileRootResponse::class.java)
                Log.d("@@@ Health profile", "message :- $data")
                if (apiModel.code == 200 && apiModel.success) {
                    findNavController().navigateUp()
                } else {
                     handleError(apiModel.code,apiModel.message)
                }
            }
            if (type.equals("BMR",true) || type.equals("BMRUPDATE",true)){
                val apiModel = Gson().fromJson(data, ApiModelBMR::class.java)
                Log.d("@@@ BMR profile", "message :- $data")
                if (apiModel.code == 200 && apiModel.success) {
                    apiModel.data?.let { dataModel->
                        viewModel.getProfileData()?.let { data ->
                            data.apply {
                                dob = binding.etDateOfBirth.text.toString()
                                height_type = binding.spinnerHeight.text.toString()
                                height = binding.etHeight.text.toString()
                                weight = binding.etweight.text.toString()
                                weight_type = binding.spinnerweight.text.toString()
                                activity_level = binding.spinnerActivityLevel.text.toString()
                                fat = dataModel.fat
                                carbs = dataModel.carbs
                                calories = dataModel.kcal
                                protien = dataModel.protein
                            }
                            viewModel.setProfileData(data)
                            if (type.equals("BMR",true)){
                                findNavController().navigate(R.id.nutritionGoalFragment)
                            }else{
                                showDataInUi(viewModel.getProfileData()!!)
                            }
                        }
                    }
                } else {
                    handleError(apiModel.code,apiModel.message)
                }
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

    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ProfileRootResponse::class.java)
            Log.d("@@@ Health profile", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                viewModel.setProfileData(apiModel.data)
                showDataInUi(apiModel.data)
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun showDataInUi(data: Data) {

        data.gender?.let {
            when (it.lowercase()) {
                "male" -> selectGender(true)
                "female" -> selectGender(false)
                else -> resetGenderSelection()
            }
        }

        if (data.height != null && !data.height.equals("null",true)) {
            binding.etHeight.setText(data.height)
        }

        if (data.weight != null && !data.weight.equals("null",true)) {
            binding.etweight.setText(data.weight)
        }

        if (data.height_type != null && !data.height_type.equals("null",true)) {
            binding.spinnerHeight.text = data.height_type
        }

        if (data.weight_type != null && !data.weight_type.equals("null",true)) {
            binding.spinnerweight.text = data.weight_type
        }

        if (data.dob != null && !data.dob.equals("null",true)) {
            binding.etDateOfBirth.text = data.dob
        }

        if (data.activity_level != null && !data.activity_level.equals("null",true)) {
            binding.spinnerActivityLevel.text = data.activity_level
        }

        if ((data.calories ?: 0) == 0 && (data.carbs ?: 0) == 0 && (data.fat ?: 0) == 0 && (data.protien ?: 0) == 0) {
            // Corrected "protien" to "protein" if needed
            binding.llCalculateBMR.visibility = View.GONE
            binding.rlAddMoreGoals.visibility = View.VISIBLE
            binding.layBottom.visibility = View.GONE
            binding.imageEditTargets.visibility = View.GONE
        } else {
            binding.llCalculateBMR.visibility = View.VISIBLE
            binding.rlAddMoreGoals.visibility = View.GONE
            binding.layBottom.visibility = View.VISIBLE
            binding.imageEditTargets.visibility = View.VISIBLE

            if ((data.calories ?: 0) == 0) {
                binding.tvCalories.text = "" + 0
            } else {
                binding.tvCalories.text = "" + data.calories!!.toInt()
            }

            if ((data.carbs ?: 0) == 0) {
                binding.tvCarbs.text = "" + 0
            } else {
                binding.tvCarbs.text = "" + data.carbs!!.toInt()
            }

            if ((data.fat ?: 0) == 0) {
                binding.tvFat.text = "" + 0
            } else {
                binding.tvFat.text = "" + data.fat!!.toInt()
            }

            if ((data.protien ?: 0) == 0) {
                binding.tvProtein.text = "" + 0
            } else {
                binding.tvProtein.text = "" + data.protien!!.toInt()
            }

        }
    }

    private fun selectGender(isMale: Boolean) {
        val selectedIcon = R.drawable.radio_select_icon
        val unselectedIcon = R.drawable.radio_unselect_icon

        genderType = if (isMale) {
            "male"
        } else {
            "female"
        }

        binding.textMale.setCompoundDrawablesWithIntrinsicBounds(
            if (isMale) selectedIcon else unselectedIcon, 0, 0, 0
        )
        binding.textFemale.setCompoundDrawablesWithIntrinsicBounds(
            if (isMale) unselectedIcon else selectedIcon, 0, 0, 0
        )
    }

    private fun resetGenderSelection() {
        val unselectedIcon = R.drawable.radio_unselect_icon
        binding.textMale.setCompoundDrawablesWithIntrinsicBounds(unselectedIcon, 0, 0, 0)
        binding.textFemale.setCompoundDrawablesWithIntrinsicBounds(unselectedIcon, 0, 0, 0)
    }

    private fun showAlert(message: String?, isError: Boolean) {
        BaseApplication.alertError(requireContext(), message, isError)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        binding.etweight.addTextChangedListener(textListener)
        binding.etHeight.addTextChangedListener(textListener1)
    }

    override fun onPause() {
        binding.etweight.addTextChangedListener(textListener)
        binding.etHeight.addTextChangedListener(textListener1)
        super.onPause()
    }


}
