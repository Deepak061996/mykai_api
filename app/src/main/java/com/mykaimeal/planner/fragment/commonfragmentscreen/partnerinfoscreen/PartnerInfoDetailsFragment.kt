package com.mykaimeal.planner.fragment.commonfragmentscreen.partnerinfoscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentPartnerInfoDetailsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.PartnerDetail
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.partnerinfoscreen.viewmodel.PartnerInfoViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PartnerInfoDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPartnerInfoDetailsBinding
    private var statusCheck: Boolean = true
    private var status: String = ""
    private lateinit var sessionManagement: SessionManagement
    private lateinit var partnerInfoViewModel: PartnerInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPartnerInfoDetailsBinding.inflate(inflater, container, false)

        sessionManagement = SessionManagement(requireContext())

        partnerInfoViewModel = ViewModelProvider(this)[PartnerInfoViewModel::class.java]

        val isProfileScreen = sessionManagement.getCookingScreen() == "Profile"

        binding.apply {
            llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
            rlUpdatePartInfo.visibility = if (isProfileScreen) View.VISIBLE else View.GONE
        }

        if (isProfileScreen) {
            if (BaseApplication.isOnline(requireContext())) {
                partnerInfoApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        } else {
            partnerInfoViewModel.getPartnerData()?.let { showDataInUi(it) }
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
                    if (sessionManagement.getCookingScreen().equals("Profile",true)){
                        findNavController().navigateUp()
                    }else{
                        requireActivity().finish()
                    }
                }
            })
    }


    private fun partnerInfoApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            partnerInfoViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                bodyModel.data.partnerDetail.let {
                                    showDataInUi(it)
                                }
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                }else{
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("PartnerDetail","message:--"+e.message)
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

    private fun showDataInUi(partnerModelData: PartnerDetail?) {
        try {
            if (partnerModelData!=null){
                if (partnerModelData.name!=null){
                    binding.etPartnerName.setText(partnerModelData.name.toString())
                }
                if (partnerModelData.age!=null){
                    binding.etPartnerAge.setText(partnerModelData.age.toString())
                }

                if (partnerModelData.gender!=null){
                    binding.tvChooseGender.text=partnerModelData.gender.toString()
                }
            }
        }catch (e:Exception){
            Log.d("PartnerDetail","message:--"+e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                searchable()
            }
        }

        binding.etPartnerName.addTextChangedListener(textWatcher)
        binding.etPartnerAge.addTextChangedListener(textWatcher)

        binding.imgBackPartnerInfo.setOnClickListener {
            if (sessionManagement.getCookingScreen().equals("Profile",true)){
                findNavController().navigateUp()
            }else{
                requireActivity().finish()
            }
        }

        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener {
            if (status=="2"){
                val partnerLocalData = PartnerDetail(
                    age = "",
                    created_at = "",  // Provide an appropriate value or default
                    deleted_at = null,  // This can remain null if it's optional
                    gender = "",
                    id = 0,  // Default value or appropriate ID
                    name = "",
                    updated_at = "",
                    user_id = 0  // Default value or appropriate user ID
                )
                partnerLocalData.name=binding.etPartnerName.text.toString().trim()
                partnerLocalData.age=binding.etPartnerAge.text.toString().trim()
                partnerLocalData.gender=binding.tvChooseGender.text.toString().trim()
                partnerInfoViewModel.setPartnerData(partnerLocalData)

                sessionManagement.setPartnerName(binding.etPartnerName.text.toString().trim())
                sessionManagement.setPartnerAge(binding.etPartnerAge.text.toString().trim())
                sessionManagement.setPartnerGender(binding.tvChooseGender.text.toString().trim())
                findNavController().navigate(R.id.bodyGoalsFragment)
            }
        }

        binding.rlSelectGender.setOnClickListener {
            if (statusCheck) {
                statusCheck = false
                val drawableEnd =
                    ContextCompat.getDrawable(requireContext(), R.drawable.drop_up_icon)
                drawableEnd!!.setBounds(
                    0,
                    0,
                    drawableEnd.intrinsicWidth,
                    drawableEnd.intrinsicHeight
                )
                binding.tvChooseGender.setCompoundDrawables(null, null, drawableEnd, null)
                binding.relSelectedGender.visibility = View.VISIBLE
            } else {
                statusCheck = true
                val drawableEnd =
                    ContextCompat.getDrawable(requireContext(), R.drawable.drop_down_icon)
                drawableEnd!!.setBounds(
                    0,
                    0,
                    drawableEnd.intrinsicWidth,
                    drawableEnd.intrinsicHeight
                )
                binding.tvChooseGender.setCompoundDrawables(null, null, drawableEnd, null)
                binding.relSelectedGender.visibility = View.GONE
            }
        }

        binding.rlSelectMale.setOnClickListener {
            binding.tvChooseGender.text = "Male"
            binding.relSelectedGender.visibility = View.GONE
            val drawableEnd = ContextCompat.getDrawable(requireContext(), R.drawable.drop_down_icon)
            drawableEnd!!.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
            binding.tvChooseGender.setCompoundDrawables(null, null, drawableEnd, null)
            statusCheck = true
            searchable()
        }

        binding.rlSelectFemale.setOnClickListener {
            binding.tvChooseGender.text = "Female"
            binding.relSelectedGender.visibility = View.GONE
            val drawableEnd = ContextCompat.getDrawable(requireContext(), R.drawable.drop_down_icon)
            drawableEnd!!.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
            binding.tvChooseGender.setCompoundDrawables(null, null, drawableEnd, null)
            statusCheck = true
            searchable()
        }

        binding.rlUpdatePartInfo.setOnClickListener{
            if (status=="2"){
                if (BaseApplication.isOnline(requireContext())) {
                    updatePartnerInfoApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

    }

    private fun updatePartnerInfoApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            partnerInfoViewModel.updatePartnerInfoApi({
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
                            Log.d("PartnerDetail@@@@","message:--"+e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, binding.etPartnerName.text.toString().trim(),binding.etPartnerAge.text.toString().trim(),binding.tvChooseGender.text.toString().trim())
        }
    }

    private fun searchable() {
        if (binding.etPartnerName.text.isNotEmpty()) {
            if (binding.etPartnerAge.text.isNotEmpty()) {
                if (binding.tvChooseGender.text.toString().isNotEmpty()) {
                    status = "2"
                    binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
                    binding.rlUpdatePartInfo.setBackgroundResource(R.drawable.green_fill_corner_bg)
                } else {
                    status = "1"
                    binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                    binding.rlUpdatePartInfo.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                }
            } else {
                status = "1"
                binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                binding.rlUpdatePartInfo.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            }
        } else {
            status = "1"
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdatePartInfo.setBackgroundResource(R.drawable.gray_btn_unselect_background)
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
            sessionManagement.setPartnerName("")
            sessionManagement.setPartnerAge("")
            sessionManagement.setPartnerGender("")
            dialogStillSkip.dismiss()
            findNavController().navigate(R.id.bodyGoalsFragment)
        }
    }



}