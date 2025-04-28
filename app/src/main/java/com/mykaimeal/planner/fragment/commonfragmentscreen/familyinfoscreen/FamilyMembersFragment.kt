package com.mykaimeal.planner.fragment.commonfragmentscreen.familyinfoscreen

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
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.alertError
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentFamilyMembersBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.FamilyDetail
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.familyinfoscreen.viewmodel.FamilyMemberInfoViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FamilyMembersFragment : Fragment() {
    private lateinit var binding: FragmentFamilyMembersBinding
    private var isChecked: Boolean? = null
    private var status: String? = ""
    private var childFriendlyStatus: String? = ""
    private lateinit var sessionManagement: SessionManagement
    private lateinit var familyMemberInfoViewModel: FamilyMemberInfoViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentFamilyMembersBinding.inflate(inflater, container, false)
        sessionManagement = SessionManagement(requireActivity())

        familyMemberInfoViewModel = ViewModelProvider(this)[FamilyMemberInfoViewModel::class.java]


        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile",true)
        val isOnline = isOnline(requireContext())

        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateFamMem.visibility = if (isProfileScreen) View.VISIBLE else View.GONE

        if (isProfileScreen) {
            if (isOnline) {
                familyMemApi()
            } else {
                alertError(requireContext(), ErrorMessage.networkError, false)
            }
        } else {
            familyMemberInfoViewModel.getFamilyData()?.let {
                showDataInUi(it)
            }
        }


        backButton()

        initialize()

        return binding.root
    }
    
    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (sessionManagement.getCookingScreen()=="Profile"){
                        findNavController().navigateUp()
                    }else{
                        requireActivity().finish()
                    }

                }
            })
    }

    private fun familyMemApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            familyMemberInfoViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                bodyModel.data.familyDetail?.let { it1 -> showDataInUi(it1) }
                            } else {
                                binding.tvNextBtn.isClickable=false
                                binding.rlUpdateFamMem.isClickable=false
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                }else{
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            binding.tvNextBtn.isClickable=false
                            binding.rlUpdateFamMem.isClickable=false
                            Log.d("FamilyMembers@@","message"+e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        binding.tvNextBtn.isClickable=false
                        binding.rlUpdateFamMem.isClickable=false
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        binding.tvNextBtn.isClickable=false
                        binding.rlUpdateFamMem.isClickable=false
                        showAlertFunction(it.message, false)
                    }
                }
            }
        }
    }

    private fun showDataInUi(familyModelData: FamilyDetail) {
        try {
                familyModelData.name?.let {
                    binding.etMembersName.setText(it)
                }

                familyModelData.age?.let {
                    binding.etMemberAge.setText(it)
                }

                familyModelData.child_friendly_meals?.let {
                    if (it.equals("1",true)){
                        isChecked=false
                        childFriendlyStatus = "1"
                        binding.checkBoxImages.setImageResource(R.drawable.tick_ckeckbox_images)
                    }else{
                        isChecked=true
                        childFriendlyStatus = "0"
                        binding.checkBoxImages.setImageResource(R.drawable.uncheck_box_images)
                    }
                }?: run {
                    isChecked=true
                    // Handle the case where familyModelData.status is null
                    childFriendlyStatus = "0"
                    binding.checkBoxImages.setImageResource(R.drawable.uncheck_box_images)
                }

            searchable()

        }catch (e:Exception){
            Log.d("FamilyMembers","message"+e.message)
        }

    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        alertError(requireContext(), message, status)
    }

    private fun initialize() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                searchable()
            }
        }
        binding.etMembersName.addTextChangedListener(textWatcher)
        binding.etMemberAge.addTextChangedListener(textWatcher)
        binding.imgBackFamilyMember.setOnClickListener {
            if (sessionManagement.getCookingScreen()=="Profile"){
                findNavController().navigateUp()
            }else{
                requireActivity().finish()
            }
        }
        binding.relRememberForgot.setOnClickListener {
            if (isChecked == true) {
                childFriendlyStatus = "1"
                binding.checkBoxImages.setImageResource(R.drawable.tick_ckeckbox_images)
                isChecked = false
            } else {
                childFriendlyStatus = "0"
                binding.checkBoxImages.setImageResource(R.drawable.uncheck_box_images)
                isChecked = true
            }
            searchable()
        }
        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }
        binding.tvNextBtn.setOnClickListener {
            if (status == "2") {
                val familyLocalData = FamilyDetail(
                    name = "",
                    age = "",
                    child_friendly_meals = "",
                    id = 0,  // Default or appropriate ID
                    created_at = "",
                    updated_at = "",
                    deleted_at = null,  // This can remain null if it's optional
                    user_id = 0  // Default or appropriate user ID
                )
                familyLocalData.name=binding.etMembersName.text.toString().trim()
                familyLocalData.age=binding.etMemberAge.text.toString().trim()
                familyLocalData.child_friendly_meals=childFriendlyStatus.toString()
                familyMemberInfoViewModel.setFamilyData(familyLocalData)

                sessionManagement.setFamilyMemName(binding.etMembersName.text.toString().trim())
                sessionManagement.setFamilyMemAge(binding.etMemberAge.text.toString().trim())
                sessionManagement.setFamilyStatus(childFriendlyStatus.toString())
                findNavController().navigate(R.id.bodyGoalsFragment)
            }
        }
        binding.rlUpdateFamMem.setOnClickListener{
            if (status=="2"){
                if (isOnline(requireContext())) {
                    updateFamilyMemInfoApi()
                } else {
                    alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun updateFamilyMemInfoApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            familyMemberInfoViewModel.updatePartnerInfoApi({
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
                            Log.d("FamilyMembers@@@@","message"+e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, binding.etMembersName.text.toString().trim(),binding.etMemberAge.text.toString().trim(),childFriendlyStatus)
        }
    }

    private fun searchable() {
        if (binding.etMembersName.text.isNotEmpty()) {
            if (binding.etMemberAge.text.isNotEmpty()) {
                if (childFriendlyStatus == "1") {
                    status = "2"
                    binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
                    binding.tvNextBtn.isClickable=true
                    binding.rlUpdateFamMem.setBackgroundResource(R.drawable.green_fill_corner_bg)
                    binding.rlUpdateFamMem.isClickable=true
                } else {
                    status = "1"
                    binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                    binding.tvNextBtn.isClickable=false
                    binding.rlUpdateFamMem.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                    binding.rlUpdateFamMem.isClickable=false
                }
            } else {
                status = "1"
                binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                binding.tvNextBtn.isClickable=false
                binding.rlUpdateFamMem.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                binding.rlUpdateFamMem.isClickable=false
            }
        } else {
            status = "1"
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.tvNextBtn.isClickable=false
            binding.rlUpdateFamMem.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateFamMem.isClickable=false
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
            sessionManagement.setFamilyMemName("")
            sessionManagement.setFamilyMemAge("")
            sessionManagement.setFamilyStatus("")
            dialogStillSkip.dismiss()
            findNavController().navigate(R.id.bodyGoalsFragment)
        }
    }

}