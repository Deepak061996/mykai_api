package com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterAllergensIngItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentAllergensIngredientsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.model.AllergensIngredientModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.model.AllergensIngredientModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.viewmodel.AllergenIngredientViewModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllergensIngredientsFragment : Fragment(), OnItemClickedListener {

    private var _binding: FragmentAllergensIngredientsBinding? = null
    private val binding get() = _binding!!
    private var allergenIngAdapter: AdapterAllergensIngItem? = null
    private lateinit var sessionManagement: SessionManagement
    private var allergenIngModelData = mutableListOf<AllergensIngredientModelData>()
    private var totalProgressValue: Int = 0
    private var status: String? = null
    private var allergensSelectedId = mutableListOf<String>()
    private lateinit var allergenIngredientViewModel: AllergenIngredientViewModel
    private var itemCount:String = "5"  // Default count
    private lateinit var textListener: TextWatcher
    private var textChangedJob: Job? = null

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAllergensIngredientsBinding.inflate(inflater, container, false)

        allergenIngredientViewModel = ViewModelProvider(this)[AllergenIngredientViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())
        allergenIngAdapter = AdapterAllergensIngItem(allergenIngModelData, requireActivity(), this)
        binding.rcyAllergensDesc.adapter = allergenIngAdapter

        val cookingFor = sessionManagement.getCookingFor()
        val progressValue: Int
        val maxProgress: Int
        val restrictionText: String

        /// checked session value cooking for
        if (cookingFor.equals("Myself")) {
            restrictionText = getString(R.string.allergens_ingredients_desc)
            maxProgress = 10
            progressValue = 5
        } else if (cookingFor.equals("MyPartner")) {
            restrictionText= "Pick ingredients you and your partner are allergic to"
            maxProgress = 11
            progressValue = 5
        } else {
            restrictionText = "Which ingredients are you and your family allergic to?"
            maxProgress = 11
            progressValue=5
        }

        binding.tvAllergensDesc.text = restrictionText
        binding.progressBar5.max = maxProgress
        totalProgressValue = maxProgress
        updateProgress(progressValue)

        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile",true)
        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateAllergens.visibility = if (isProfileScreen) View.VISIBLE else View.GONE


        if (BaseApplication.isOnline(requireContext())) {
            if (isProfileScreen) {
                searchable("","count")
            } else {
                allergenIngredientViewModel.getAllergensData()?.let {
                    showDataInUi(it)
                } ?: searchable("","count")
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

        backButton()

        ///main function using all triggered of this screen
        initialize()

        return binding.root
    }

    private fun backButton(){
        //// handle on back pressed
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    /// update progressbar value and progress
    private fun updateProgress(progress: Int) {
        binding.progressBar5.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        /// handle on back pressed
        binding.imgBackAllergensIng.setOnClickListener {
            findNavController().navigateUp()
        }

        /// handle click event for skip this screen
        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }


        /// handle click event for redirect next part
        binding.tvNextBtn.setOnClickListener {
            if (status == "2") {
                allergenIngredientViewModel.setAllergensData(allergenIngModelData,binding.etAllergensIngSearchBar.text.toString())
                sessionManagement.setAllergenIngredientList(allergensSelectedId)
                if (sessionManagement.getCookingFor().equals("Myself")) {
                    findNavController().navigate(R.id.mealRoutineFragment)
                } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                    findNavController().navigate(R.id.favouriteCuisinesFragment)
                } else {
                    findNavController().navigate(R.id.favouriteCuisinesFragment)
                }
            }
        }

        binding.rlUpdateAllergens.setOnClickListener {
            if (status == "2") {
                ///checking the device of mobile data in online and offline(show network error message)
                if (BaseApplication.isOnline(requireActivity())) {
                    updateAllergensApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        binding.relMoreButton.setOnClickListener { v ->
            binding.relMoreButton.visibility=View.VISIBLE

            itemCount = (itemCount.toInt() + 10).toString()  // Convert to Int, add 10, convert back to String
            ///checking the device of mobile data in online and offline(show network error message)
            if (BaseApplication.isOnline(requireContext())) {
                searchable("","count")
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        textListener = object : TextWatcher {
            private var searchFor = ""

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()

                textChangedJob?.cancel() // Cancel any pending jobs

                if (searchText.isNotEmpty()) {
                    if (searchText != searchFor) {
                        searchFor = searchText
                        textChangedJob = lifecycleScope.launch {
                            delay(1000)  // Debounce time
                            if (searchText == searchFor) {
                                if (BaseApplication.isOnline(requireActivity())) {
                                    searchable(searchText,"search")
                                } else {
                                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                                }
                            }
                        }
                    }
                } else {
                    Log.d("not data", "Text field is empty")
                    if (BaseApplication.isOnline(requireContext())) {
//                            ingredientDislikeSelectApi()
                        searchable("","count")
                    } else {
                        BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                    }
                }
            }
        }

    }

    private fun updateAllergensApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            allergenIngredientViewModel.updateAllergiesApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val updateModel =
                                gson.fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
                            if (updateModel.code == 200 && updateModel.success) {
                                findNavController().navigateUp()
                            } else {
                                if (updateModel.code == ErrorMessage.code) {
                                    showAlertFunction(updateModel.message, true)
                                } else {
                                    showAlertFunction(updateModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("allergens@@","message:---"+e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, allergensSelectedId)
        }
    }

    private fun searchable(editText: String,countStatus:String) {
        binding.layProgess.visibility=View.VISIBLE
        val count = if (countStatus.equals("search", true)) "100" else itemCount
        lifecycleScope.launch {
            allergenIngredientViewModel.getAllergensSearchIngredients({
                binding.layProgess.visibility=View.GONE
                when (it) {
                    is NetworkResult.Success -> {
                        val gson = Gson()
                        try {
                            if (sessionManagement.getCookingScreen().toString().equals("Profile", ignoreCase = true)){
                                val dietaryModel = gson.fromJson(it.data, GetUserPreference::class.java)
                                if (dietaryModel.code == 200 && dietaryModel.success) {
                                    showDataFirstUi(dietaryModel.data.allergesingredient,countStatus)
                                } else {
                                    if (dietaryModel.code == ErrorMessage.code) {
                                        showAlertFunction(dietaryModel.message, true)
                                    } else {
                                        showAlertFunction(dietaryModel.message, false)
                                    }
                                }
                            }else{
                                val dietaryModel = gson.fromJson(it.data, AllergensIngredientModel::class.java)
                                if (dietaryModel.code == 200 && dietaryModel.success) {
                                    showDataFirstUi(dietaryModel.data,countStatus)
                                } else {
                                    if (dietaryModel.code == ErrorMessage.code) {
                                        showAlertFunction(dietaryModel.message, true)
                                    } else {
                                        showAlertFunction(dietaryModel.message, false)
                                    }
                                }
                            }
                        }catch (e:Exception){
                            Log.d("IngredientDislike@@@@", "message:--" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            },editText,count,sessionManagement.getCookingScreen().toString())
        }


    }

    private fun showDataFirstUi(allergensModelData: MutableList<AllergensIngredientModelData>,type:String) {
        try {
            if (allergensModelData != null && allergensModelData.size>0) {
                allergenIngModelData.clear()
                allergenIngModelData.add(0, AllergensIngredientModelData(id = -1, selected = false, "None")) // ID set to -1 as an indicator
                allergenIngModelData.addAll(allergensModelData)
                allergenIngAdapter?.filterList(allergenIngModelData)
                if (type.equals("search",true)){
                    binding.relMoreButton.visibility=View.GONE
                }else{
                    binding.relMoreButton.visibility=View.VISIBLE
                }
                binding.rcyAllergensDesc.visibility=View.VISIBLE
            }else{
                binding.rcyAllergensDesc.visibility=View.GONE
            }
        }catch (e:Exception){
            Log.d("allergens","message:--"+e.message)
        }
    }

    private fun showDataInUi(allergensModelData: MutableList<AllergensIngredientModelData>) {
        try {
            if (allergensModelData != null && allergensModelData.isNotEmpty()) {
                if (allergenIngredientViewModel.getAllergensData() == null) {
                    allergensModelData.add(0, AllergensIngredientModelData(id = -1, selected = false, "None")
                    ) // ID set to -1 as an indicator
                }
                var selected = false
                allergensModelData.forEach {
                    if(it.selected) selected = true
                }
                if(!selected){
                    allergensModelData.set(0, AllergensIngredientModelData(id = -1, selected = true, "None")
                    )
                }

             /*   // Show "Show More" button only if there are more than 3 items
                if (allergensModelData.size > 3) {
                    binding.relMoreButton.visibility = View.VISIBLE
                }*/
                allergenIngModelData = allergensModelData.toMutableList()
                allergenIngModelData = allergensModelData.toMutableList()
                allergenIngAdapter?.filterList(allergenIngModelData)
                if (allergenIngredientViewModel.getEditStatus().equals("")){
                    binding.relMoreButton.visibility=View.VISIBLE
                }else{
                    binding.relMoreButton.visibility=View.GONE
                }

//                allergenIngAdapter = AdapterAllergensIngItem(allergensModelData, requireActivity(), this)
//                binding.rcyAllergensDesc.adapter = allergenIngAdapter
            }
        }catch (e:Exception){
            Log.d("allergens","message:---"+e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun stillSkipDialog() {
        val dialogStillSkip: Dialog = context?.let { Dialog(it) }!!
        dialogStillSkip.setContentView(R.layout.alert_dialog_still_skip)
        dialogStillSkip.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogCancelBtn = dialogStillSkip.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogSkipBtn = dialogStillSkip.findViewById<TextView>(R.id.tvDialogSkipBtn)
        dialogStillSkip.show()
        dialogStillSkip.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogStillSkip.dismiss()
        }

        tvDialogSkipBtn.setOnClickListener {
            dialogStillSkip.dismiss()
            sessionManagement.setAllergenIngredientList(null)
            if (sessionManagement.getCookingFor().equals("Myself")) {
                findNavController().navigate(R.id.mealRoutineFragment)
            } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                findNavController().navigate(R.id.favouriteCuisinesFragment)
            } else {
                findNavController().navigate(R.id.favouriteCuisinesFragment)
            }
        }
    }

    override fun itemClicked(position: Int?, list: MutableList<String>?, status1: String?, type: String?) {
            if (status1.equals("-1")) {
                if (position==0){
                    allergensSelectedId = mutableListOf()

                }else{
                    allergensSelectedId = list!!
                }
                status = "2"
                binding.tvNextBtn.isClickable = true
                binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
                binding.rlUpdateAllergens.isClickable = true
                binding.rlUpdateAllergens.setBackgroundResource(R.drawable.green_fill_corner_bg)
                return
            }

            if (type.equals("true")) {
                status = "2"
                binding.tvNextBtn.isClickable = true
                binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)

                binding.rlUpdateAllergens.isClickable = true
                binding.rlUpdateAllergens.setBackgroundResource(R.drawable.green_fill_corner_bg)
                allergensSelectedId = list!!
            } else {
                status = ""
                binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                binding.rlUpdateAllergens.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            }
    }

    override fun onResume() {
        super.onResume()
        binding.etAllergensIngSearchBar.addTextChangedListener(textListener)
    }

    override fun onPause() {
        binding.etAllergensIngSearchBar.removeTextChangedListener(textListener)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}