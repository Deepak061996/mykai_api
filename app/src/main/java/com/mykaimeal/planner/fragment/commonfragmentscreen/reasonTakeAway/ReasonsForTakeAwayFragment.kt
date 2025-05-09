package com.mykaimeal.planner.fragment.commonfragmentscreen.reasonTakeAway

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
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
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.AuthActivity
import com.mykaimeal.planner.adapter.BodyGoalAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.alertError
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentReasonsForTakeAwayBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.reasonTakeAway.viewmodel.ReasonTakeAwayViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ReasonsForTakeAwayFragment : Fragment(), OnItemClickListener {

    private var _binding: FragmentReasonsForTakeAwayBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue: Int = 0
    private var status: String? = ""
    private var reasonSelect: String? = ""
    private lateinit var reasonTakeAwayViewModel: ReasonTakeAwayViewModel
    private var bodyGoalAdapter: BodyGoalAdapter? = null
    private var reasonTakeModelData: List<BodyGoalModelData>?=null

    private var userName: String? = ""
    private var cookingFor: String? = ""
    private var userGender: String? = ""
    private var partnerName: String? = ""
    private var partnerAge: String? = ""
    private var partnerGender: String? = ""
    private var familyMemName: String? = ""
    private var familyMemAge: String? = ""
    private var familyMemStatus: String? = ""
    private var bodyGoals: String? = ""
    private var dietarySelectedId = mutableListOf<String>()
    private var favouriteSelectedId = mutableListOf<String>()
    private var dislikeSelectedId = mutableListOf<String>()
    private var allergenSelectedId = mutableListOf<String>()
    private var mealRoutineSelectedId = mutableListOf<String>()
    private var cookingFrequency: String? = ""
    private var spendingAmount: String? = ""
    private var spendingDuration: String? = ""
    private var eatingOut: String? = ""
    private var reasonTakeAway: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentReasonsForTakeAwayBinding.inflate(inflater, container, false)

        reasonTakeAwayViewModel = ViewModelProvider(this)[ReasonTakeAwayViewModel::class.java]

        sessionManagement = SessionManagement(requireContext())


        val progressValue = if (sessionManagement.getCookingFor().equals("Myself")) 10 else 11
        binding.progressBar11.max = progressValue
        totalProgressValue = progressValue
        updateProgress(progressValue)


        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile")
        val isOnline = isOnline(requireContext())

        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateReasonTakeAway.visibility = if (isProfileScreen) View.VISIBLE else View.GONE

        if (isOnline) {
            if (isProfileScreen) {
                reasonTakeAwaySelectApi()
            } else {
                reasonTakeAwayApi()
            }
        } else {
            alertError(requireContext(), ErrorMessage.networkError, false)
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

    private fun reasonTakeAwaySelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            reasonTakeAwayViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data.takeawayreason)
                            } else {
                                handleError(bodyModel.code,bodyModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("ReasonTakeAway@@@","message:--"+e.message)
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

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar11.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        /// value get for social login
        cookingFor = if (sessionManagement.getCookingFor().equals("Myself",true)) {
            "1"
        } else if (sessionManagement.getCookingFor().equals("MyPartner",true)) {
            "2"
        } else {
            "3"
        }


        if (sessionManagement.getUserName() != "") {
            userName = sessionManagement.getUserName()
        }

        if (sessionManagement.getGender() != "") {
            userGender = sessionManagement.getGender()
        }

        if (sessionManagement.getPartnerName() != "") {
            partnerName = sessionManagement.getPartnerName()
        }

        if (sessionManagement.getPartnerAge() != "") {
            partnerAge = sessionManagement.getPartnerAge()
        }

        if (sessionManagement.getPartnerGender() != "") {
            partnerGender = sessionManagement.getPartnerGender()
        }

        if (sessionManagement.getFamilyMemName() != "") {
            familyMemName = sessionManagement.getFamilyMemName()
        }

        if (sessionManagement.getFamilyMemAge() != "") {
            familyMemAge = sessionManagement.getFamilyMemAge()
        }

        if (sessionManagement.getFamilyStatus() != "") {
            familyMemStatus = sessionManagement.getFamilyStatus()
        }

        if (sessionManagement.getBodyGoal() != "") {
            bodyGoals = sessionManagement.getBodyGoal()
        }

        if (sessionManagement.getDietaryRestrictionList() != null) {
            dietarySelectedId = sessionManagement.getDietaryRestrictionList()!!
        }

        if (sessionManagement.getFavouriteCuisineList() != null) {
            favouriteSelectedId = sessionManagement.getFavouriteCuisineList()!!
        }

        if (sessionManagement.getDislikeIngredientList() != null) {
            dislikeSelectedId = sessionManagement.getDislikeIngredientList()!!
        }

        if (sessionManagement.getAllergenIngredientList() != null) {
            allergenSelectedId = sessionManagement.getAllergenIngredientList()!!
        }

        if (sessionManagement.getMealRoutineList() != null) {
            mealRoutineSelectedId = sessionManagement.getMealRoutineList()!!
        }

        if (sessionManagement.getCookingFrequency() != "") {
            cookingFrequency = sessionManagement.getCookingFrequency()
        }

        if (sessionManagement.getSpendingAmount() != "") {
            spendingAmount = sessionManagement.getSpendingAmount()
        }

        if (sessionManagement.getSpendingDuration() != "") {
            spendingDuration = sessionManagement.getSpendingDuration()
        }

        if (sessionManagement.getEatingOut() != "") {
            eatingOut = sessionManagement.getEatingOut()
        }

        if (sessionManagement.getReasonTakeAway() != "") {
            reasonSelect = sessionManagement.getReasonTakeAway()
        }

        if (sessionManagement.getReasonTakeAwayDesc() != "") {
            reasonTakeAway = sessionManagement.getReasonTakeAwayDesc()
        }


        binding.imbBackTakeAway.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }


        binding.edtext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty()){
                    reasonTakeAway = s.toString()
                    status="2"
                    binding.tvNextBtn.isClickable = true
                    binding.rlUpdateReasonTakeAway.isClickable = true
                    binding.rlUpdateReasonTakeAway.setBackgroundResource(R.drawable.gray_btn_select_background)
                    binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_select_background)
                }else{
                    status=""
                    reasonTakeAway = ""
                    binding.tvNextBtn.isClickable = false
                    binding.rlUpdateReasonTakeAway.isClickable = false
                    binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                    binding.rlUpdateReasonTakeAway.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                }
            }

            override fun afterTextChanged(data: Editable?) {

            }
        })

        binding.tvNextBtn.setOnClickListener {
            if (isOnline(requireContext())) {
                if (status.equals("2")) {
                    if (sessionManagement.getPreferences()){
                        updatePreferencesApi()
                    }else{
                        reasonTakeAwayViewModel.setReasonTakeData(reasonTakeModelData!!.toMutableList())
                        sessionManagement.setReasonTakeAway(reasonSelect.toString())
                        sessionManagement.setReasonTakeAwayDesc(reasonTakeAway.toString())
                        if (sessionManagement.getFirstTime()){
                            navigateToAuthActivity("login")
                        }else{
                            navigateToAuthActivity("signup")
                        }
                    }
                }
            } else {
                alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        binding.rlUpdateReasonTakeAway.setOnClickListener{
            if (status=="2"){
                if (isOnline(requireContext())) {
                    updateReasonTakeAwayApi()
                } else {
                    alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun updatePreferencesApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            reasonTakeAwayViewModel.updatePreferencesApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val updateModel = gson.fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
                            if (updateModel.code == 200 && updateModel.success) {
                                sessionManagement.setLoginSession(true)
                                findNavController().navigate(R.id.turnOnLocationFragment)
                            } else {
                               handleError(updateModel.code,updateModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("ReasonTakeAway@@@@@","message:--"+e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            },userName,cookingFor,userGender,bodyGoals,partnerName,partnerAge,partnerGender,familyMemName,
                familyMemAge, familyMemStatus,dietarySelectedId,favouriteSelectedId,dislikeSelectedId,allergenSelectedId,mealRoutineSelectedId,
                cookingFrequency,spendingAmount,spendingDuration,eatingOut, reasonSelect,reasonTakeAway)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        }else{
            showAlertFunction(message, false)
        }
    }

    private fun navigateToAuthActivity(type: String) {
        val intent = Intent(requireActivity(), AuthActivity::class.java).apply {
            putExtra("type", type)
            putExtra("backType", "yes")
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }

    private fun updateReasonTakeAwayApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            reasonTakeAwayViewModel.updateReasonTakeAwayApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val updateModel = gson.fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
                            if (updateModel.code == 200 && updateModel.success) {
                                findNavController().navigateUp()
                            } else {
                                if (updateModel.code == ErrorMessage.code) {
                                    showAlertFunction(updateModel.message, true)
                                }else{
                                    showAlertFunction(updateModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("ReasonTakeAway@@@@@","message:--"+e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            },reasonSelect.toString(),reasonTakeAway)
        }
    }


    private fun reasonTakeAwayApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            reasonTakeAwayViewModel.getTakeAwayReason {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, BodyGoalModel::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data)
                            } else {
                                handleError(bodyModel.code,bodyModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("ReasonTakeAway@@@@","message:--"+e.message)
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

    private fun showDataInUi(bodyModelData: List<BodyGoalModelData>) {
        try {
            if (bodyModelData!=null && bodyModelData.size>0){
                reasonTakeModelData=bodyModelData
                bodyGoalAdapter = BodyGoalAdapter(bodyModelData, requireActivity(), this)
                binding.rcyTakeAway.adapter = bodyGoalAdapter
            }
        }catch (e:Exception){
            Log.d("ReasonTakeAway","message:--"+e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        alertError(requireContext(), message, status)
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
            sessionManagement.setReasonTakeAway("")
            sessionManagement.setReasonTakeAwayDesc("")
            dialogStillSkip.dismiss()
            navigateToAuthActivity("login")
        }
    }

    override fun itemClick(selectItem: Int?, status1: String?, type: String?) {
        reasonSelect=""
        reasonTakeAway = ""
        if (status1.equals("-1")) {
            if (reasonTakeModelData?.get(type!!.toInt())!!.name.toString().equals("Add other",true)){
                reasonSelect = reasonTakeModelData?.get(type!!.toInt())!!.id.toString()
                binding.relMainLayout.visibility=View.VISIBLE
                if (reasonTakeModelData?.get(type!!.toInt())!!.descripttion!=null){
                    reasonTakeAway = reasonTakeModelData?.get(type!!.toInt())!!.descripttion.toString()
                    binding.edtext.setText(reasonTakeModelData?.get(type!!.toInt())!!.descripttion.toString())
                    status = "2"
                    binding.tvNextBtn.isClickable = true
                    binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
                    binding.rlUpdateReasonTakeAway.isClickable = true
                    binding.rlUpdateReasonTakeAway.setBackgroundResource(R.drawable.green_fill_corner_bg)
                }else{
                    binding.edtext.text.clear()
                    status = ""
                    binding.tvNextBtn.isClickable = false
                    binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                    binding.rlUpdateReasonTakeAway.isClickable = false
                    binding.rlUpdateReasonTakeAway.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                }
            }else{
                reasonSelect = reasonTakeModelData?.get(type!!.toInt())!!.id.toString()
                reasonTakeAway = ""
                binding.relMainLayout.visibility=View.GONE
                status = "2"
                binding.tvNextBtn.isClickable = true
                binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
                binding.rlUpdateReasonTakeAway.isClickable = true
                binding.rlUpdateReasonTakeAway.setBackgroundResource(R.drawable.green_fill_corner_bg)
            }
            return
        }

        if (status1.equals("true")) {
            if (reasonTakeModelData?.get(type!!.toInt())!!.name.toString().equals("Add other",true)){
                status = ""
                binding.tvNextBtn.isClickable = false
                binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                binding.rlUpdateReasonTakeAway.isClickable = false
                binding.rlUpdateReasonTakeAway.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                binding.relMainLayout.visibility=View.VISIBLE
                binding.edtext.text.clear()
            }else{
                status = "2"
                binding.tvNextBtn.isClickable = true
                binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
                binding.rlUpdateReasonTakeAway.isClickable = true
                binding.rlUpdateReasonTakeAway.setBackgroundResource(R.drawable.green_fill_corner_bg)
                reasonSelect = reasonTakeModelData?.get(type!!.toInt())!!.id.toString()
                reasonTakeAway = selectItem.toString()
                reasonTakeAway = ""
                binding.relMainLayout.visibility=View.GONE
            }

        } else {
            if (reasonTakeModelData?.get(type!!.toInt())!!.name.toString().equals("Add other",true)){
                binding.tvNextBtn.isClickable = false
                binding.rlUpdateReasonTakeAway.isClickable = false
                binding.relMainLayout.visibility=View.GONE
                binding.edtext.text.clear()
            }
            status = ""
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateReasonTakeAway.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}