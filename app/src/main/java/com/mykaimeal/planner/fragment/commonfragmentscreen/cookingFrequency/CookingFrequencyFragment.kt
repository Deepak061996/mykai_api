package com.mykaimeal.planner.fragment.commonfragmentscreen.cookingFrequency

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
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.BodyGoalAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentCookingFrequencyBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.cookingFrequency.viewmodel.CookingFrequencyViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CookingFrequencyFragment : Fragment(), OnItemClickListener {

    private var _binding: FragmentCookingFrequencyBinding? = null
    private val binding get() = _binding!!
    private var bodyGoalAdapter: BodyGoalAdapter? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue: Int = 0
    private var status: String? = null
    private var cookingSelect: String? = null
    private lateinit var cookingFrequencyViewModel: CookingFrequencyViewModel
    private var cookingFreqModelData: List<BodyGoalModelData>? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCookingFrequencyBinding.inflate(inflater, container, false)

        cookingFrequencyViewModel = ViewModelProvider(this)[CookingFrequencyViewModel::class.java]

        sessionManagement = SessionManagement(requireContext())
//        if (sessionManagement.getCookingFor().equals("Myself")) {
//            binding.tvCookFreqDesc.text = "How often do you cook meals at home?"
//            binding.progressBar7.max = 10
//            totalProgressValue = 10
//            updateProgress(7)
//        } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
//            binding.tvCookFreqDesc.text = "How often do you cook meals at home?"
//            binding.progressBar7.max = 11
//            totalProgressValue = 11
//            updateProgress(8)
//        } else {
//            binding.tvCookFreqDesc.text = "How often do you cook meals for your family?"
//            binding.progressBar7.max = 11
//            totalProgressValue = 11
//            updateProgress(8)
//        }

        val cookingFor = sessionManagement.getCookingFor()
        var progressValue = 8
        var maxProgress = 11

        if (cookingFor == "Myself") {
            maxProgress = 10
            progressValue = 7
        }

        binding.tvCookFreqDesc.text =
            if (cookingFor == "Myself" || cookingFor == "MyPartner") "How often do you cook meals at home?" else "How often do you cook meals for your family?"

        binding.progressBar7.max = maxProgress
        totalProgressValue = maxProgress
        updateProgress(progressValue)


        if (sessionManagement.getCookingScreen().equals("Profile")) {
            binding.llBottomBtn.visibility = View.GONE
            binding.rlUpdateCookingFrequency.visibility = View.VISIBLE

            if (BaseApplication.isOnline(requireActivity())) {
                cookingFrequencySelectApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        } else {
            binding.llBottomBtn.visibility = View.VISIBLE
            binding.rlUpdateCookingFrequency.visibility = View.GONE

            if (cookingFrequencyViewModel.getCookingFreqData() != null) {
                showDataInUi(cookingFrequencyViewModel.getCookingFreqData()!!)
            } else {
                ///checking the device of mobile data in online and offline(show network error message)
                if (BaseApplication.isOnline(requireActivity())) {
                    cookingFrequencyApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }


        backButton()

        initialize()

        return binding.root
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar7.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        binding.imgBackCookingFreq.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener {
            if (status == "2") {
                cookingFrequencyViewModel.setCookingFreqData(cookingFreqModelData!!.toMutableList())
                sessionManagement.setCookingFrequency(cookingSelect.toString())
                if (sessionManagement.getCookingFor().equals("Myself")) {
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
//                    findNavController().navigate(R.id.cookingScheduleFragment)
                } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
                } else {
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
//                    findNavController().navigate(R.id.cookingScheduleFragment)
                }
            }
        }

        binding.rlUpdateCookingFrequency.setOnClickListener {
            if (status=="2"){
                ///checking the device of mobile data in online and offline(show network error message)
                if (BaseApplication.isOnline(requireActivity())) {
                    updateCookFrequencyApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun updateCookFrequencyApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookingFrequencyViewModel.updateCookingFrequencyApi({
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
                            Log.d("cookingFrequency@@@@", "message" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, cookingSelect.toString())
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
            sessionManagement.setCookingFrequency("")
            if (sessionManagement.getCookingFor().equals("Myself")) {
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            } else {
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            }
        }
    }

    private fun cookingFrequencySelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookingFrequencyViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data.cookingfrequency)
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                } else {
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("cookingFrequency@@@", "message" + e.message)
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

    private fun cookingFrequencyApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookingFrequencyViewModel.getCookingFrequency {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyGoalModel = gson.fromJson(it.data, BodyGoalModel::class.java)
                            if (bodyGoalModel.code == 200 && bodyGoalModel.success) {
                                showDataInUi(bodyGoalModel.data)
                            } else {
                                if (bodyGoalModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyGoalModel.message, true)
                                } else {
                                    showAlertFunction(bodyGoalModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("cookingFrequency@@@@", "message" + e.message)
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

    private fun showDataInUi(bodyGoalModelData: List<BodyGoalModelData>) {
        try {
            if (bodyGoalModelData != null && bodyGoalModelData.size > 0) {
                cookingFreqModelData = bodyGoalModelData
                bodyGoalAdapter = BodyGoalAdapter(bodyGoalModelData, requireActivity(), this)
                binding.rcyCookingFreq.adapter = bodyGoalAdapter
            }
        } catch (e: Exception) {
            Log.d("cookingFrequency@@@@", "message" + e.message)
        }

    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    override fun itemClick(selectItem: Int?, status1: String?, type: String?) {
        if (status1.equals("-1")) {
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateCookingFrequency.isClickable = true
            binding.rlUpdateCookingFrequency.setBackgroundResource(R.drawable.green_fill_corner_bg)
            cookingSelect = selectItem.toString()
            return
        }

        if (type.equals("true")) {
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateCookingFrequency.isClickable = true
            binding.rlUpdateCookingFrequency.setBackgroundResource(R.drawable.green_fill_corner_bg)
            cookingSelect = selectItem.toString()
        } else {
            status = ""
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateCookingFrequency.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}