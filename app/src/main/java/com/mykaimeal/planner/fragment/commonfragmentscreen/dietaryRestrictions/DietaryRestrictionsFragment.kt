package com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions

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
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.DietaryRestrictionsAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentDietaryRestrictionsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.model.DietaryRestrictionsModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.model.DietaryRestrictionsModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.viewmodel.DietaryRestrictionsViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DietaryRestrictionsFragment : Fragment(), OnItemClickedListener {

    private var _binding: FragmentDietaryRestrictionsBinding? = null
    private val binding get() = _binding!!
    private var dietaryRestrictionsAdapter: DietaryRestrictionsAdapter? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue: Int = 0
    private var status: String? = null
    private var dietarySelectedId = mutableListOf<String>()
    private lateinit var dietaryRestrictionsViewModel: DietaryRestrictionsViewModel
    private var dietaryModelsData: MutableList<DietaryRestrictionsModelData> = mutableListOf()
    private var isExpanded = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDietaryRestrictionsBinding.inflate(inflater, container, false)
        dietaryRestrictionsViewModel = ViewModelProvider(this)[DietaryRestrictionsViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())

        val cookingFor = sessionManagement.getCookingFor()
        val progressValue: Int
        val maxProgress: Int
        val restrictionText: String
        when (cookingFor) {
            "Myself" -> {
                restrictionText = "Do you have any dietary restrictions?"
                maxProgress = 10
                progressValue = 2
            }

            "MyPartner" -> {
                restrictionText = "Do you or your partner have any dietary restrictions?"
                maxProgress = 11
                progressValue = 3
            }

            else -> {
                restrictionText =
                    "Do you or any of your family members have any dietary restrictions?"
                maxProgress = 11
                progressValue = 3
            }
        }

        binding.tvRestrictions.text = restrictionText
        binding.progressBar2.max = maxProgress
        totalProgressValue = maxProgress

        updateProgress(progressValue)

        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile", true)
        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateDietRest.visibility = if (isProfileScreen) View.VISIBLE else View.GONE

        if (BaseApplication.isOnline(requireContext())) {
            if (isProfileScreen) {
                dietaryRestrictionSelectApi()
            } else {
                dietaryRestrictionsViewModel.getDietaryResData()?.let {
                    showDataInUi(it)
                } ?: dietaryRestrictionApi()
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

        backButton()

        initialize()

        return binding.root
    }

    private fun backButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun dietaryRestrictionSelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dietaryRestrictionsViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                dietaryModelsData.clear()
                                dietaryModelsData.add(0, DietaryRestrictionsModelData(id = -1, selected = false, name = "None"))
                                bodyModel.data.dietaryrestriction.let { local->
                                    dietaryModelsData.addAll(local)
                                }
                                showDataInUi(dietaryModelsData)
                            } else {
                                handleError(bodyModel.code,bodyModel.message)
                            }
                        } catch (e: Exception) {
                            Log.d("DietaryRestriction", "message" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        } else {
            showAlertFunction(message, false)
        }
    }

    private fun dietaryRestrictionApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dietaryRestrictionsViewModel.getDietaryRestrictions {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val dietaryModel =
                                gson.fromJson(it.data, DietaryRestrictionsModel::class.java)
                            if (dietaryModel.code == 200 && dietaryModel.success) {
                                dietaryModelsData.clear()
                                dietaryModelsData.add(0, DietaryRestrictionsModelData(id = -1, selected = false, name = "None"))
                                showDataFirstUi(dietaryModel.data)
                            } else {
                                handleError(dietaryModel.code,dietaryModel.message)
                            }
                        } catch (e: Exception) {
                            Log.d("DietaryRestriction@@", "message" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }
        }
    }

    private fun showDataInUi(dietaryModelData: MutableList<DietaryRestrictionsModelData>) {
        try {
            if (dietaryModelData.size>0) {
                hideShow()
                binding.relMoreButton.visibility = if (dietaryModelData.size > 5) View.VISIBLE else View.GONE
                dietaryRestrictionsAdapter = DietaryRestrictionsAdapter(dietaryModelData, requireActivity(), this)
                binding.rcyDietaryRestrictions.adapter = dietaryRestrictionsAdapter
            }
        } catch (e: Exception) {
            Log.d("DietaryRestriction", "message" + e.message)
        }
    }


    private fun showDataFirstUi(dietaryModelData: MutableList<DietaryRestrictionsModelData>) {
        try {

            dietaryModelData.let {
                dietaryModelsData.addAll(it)
            }

            if (dietaryModelsData.size>0) {

                hideShow()
                // Show "Show More" button only if there are more than 3 items
                if (dietaryModelsData.size > 3) {
                    binding.relMoreButton.visibility = View.VISIBLE
                }
                dietaryRestrictionsAdapter = DietaryRestrictionsAdapter(dietaryModelsData, requireActivity(), this)
                binding.rcyDietaryRestrictions.adapter = dietaryRestrictionsAdapter
            }
        } catch (e: Exception) {
            Log.d("Dietary Restrictions", "message" + e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar2.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        binding.imageBackDietaryRestrictions.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener {
            if (status == "2") {
                dietarySelectedId.clear()
                dietaryModelsData.forEach {
                    if (it.selected){
                        dietarySelectedId.add(it.id.toString())
                    }
                }
                dietaryRestrictionsViewModel.setDietaryResData(dietaryModelsData)
                sessionManagement.setDietaryRestrictionList(dietarySelectedId)
                if (sessionManagement.getCookingFor().equals("Myself")) {
                    findNavController().navigate(R.id.favouriteCuisinesFragment)
                } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                    findNavController().navigate(R.id.ingredientDislikesFragment)
                } else {
                    findNavController().navigate(R.id.ingredientDislikesFragment)
                }
            }
        }

        binding.rlUpdateDietRest.setOnClickListener {
            if (status=="2"){
                if (BaseApplication.isOnline(requireContext())) {
                    dietarySelectedId.clear()
                    dietaryModelsData.forEach {
                        if (it.selected){
                            dietarySelectedId.add(it.id.toString())
                        }
                    }
                    updateDietaryRestApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        binding.relMoreButton.setOnClickListener {
            isExpanded = true
            dietaryRestrictionsAdapter!!.setExpanded(true)
            binding.relMoreButton.visibility = View.GONE // Hide button after expanding
        }

    }

    private fun updateDietaryRestApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dietaryRestrictionsViewModel.updateDietaryApi({
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
                        } catch (e: Exception) {
                            Log.d("Dietary Restrictions@@@", "message" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, dietarySelectedId)
        }
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
            sessionManagement.setDietaryRestrictionList(null)
            if (sessionManagement.getCookingFor().equals("Myself")) {
                findNavController().navigate(R.id.favouriteCuisinesFragment)
            } else {
                findNavController().navigate(R.id.ingredientDislikesFragment)
            }
        }
    }

    override fun itemClicked(
        position: Int?,
        list: MutableList<String>?,
        status1: String?,
        type: String?
    ) {

        hideShow()

//        if (status1.equals("-1")) {
//            if (position == 0) {
//                dietarySelectedId = mutableListOf()
//            } else {
//                dietarySelectedId = list!!
//            }
//            status = "2"
//            binding.tvNextBtn.isClickable = true
//            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
//            binding.rlUpdateDietRest.isClickable = true
//            binding.rlUpdateDietRest.setBackgroundResource(R.drawable.green_fill_corner_bg)
//            return
//        }
//
//        if (type.equals("true")) {
//            status = "2"
//            binding.tvNextBtn.isClickable = true
//            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
//            binding.rlUpdateDietRest.isClickable = true
//            binding.rlUpdateDietRest.setBackgroundResource(R.drawable.green_fill_corner_bg)
//            dietarySelectedId = list!!
//        } else {
//            status = ""
//            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
//            binding.rlUpdateDietRest.setBackgroundResource(R.drawable.gray_btn_unselect_background)
//        }
    }

    private fun hideShow() {
        val count = dietaryModelsData.count { it.selected }
        if (count == 0) {
            status = ""
            binding.tvNextBtn.isClickable = false
            binding.rlUpdateDietRest.isClickable = false
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateDietRest.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        } else {
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.rlUpdateDietRest.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateDietRest.setBackgroundResource(R.drawable.green_fill_corner_bg)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}