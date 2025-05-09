package com.mykaimeal.planner.fragment.authfragment.verification

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.EnterYourNameActivity
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentVerificationBinding
import com.mykaimeal.planner.fragment.authfragment.forgotpassword.model.ForgotPasswordModel
import com.mykaimeal.planner.fragment.authfragment.verification.model.ForgotVerificationModel
import com.mykaimeal.planner.fragment.authfragment.verification.model.ResendSignUpOtpModel
import com.mykaimeal.planner.fragment.authfragment.verification.model.SignUpVerificationModel
import com.mykaimeal.planner.fragment.authfragment.verification.model.SignUpVerificationModelData
import com.mykaimeal.planner.fragment.authfragment.verification.viewmodel.VerificationViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class VerificationFragment : Fragment() {

    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!
    private var screenType: String? = null
    private var chooseType: String? = ""
    private var value: String? = ""
    private lateinit var commonWorkUtils: CommonWorkUtils
    private val START_TIME_IN_MILLIS: Long = 120000
    private var mTimeLeftInMillis = START_TIME_IN_MILLIS
    private lateinit var verificationViewModel: VerificationViewModel
    private lateinit var sessionManagement: SessionManagement
    private var userID: String? = ""
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
    private var reasonTakeAwayDesc: String? = ""
    private var token: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)
        verificationViewModel = ViewModelProvider(this)[VerificationViewModel::class.java]
        arguments?.let {
            screenType = it.getString("screenType", "") ?: ""
            chooseType = it.getString("chooseType", "") ?: ""
            value = it.getString("value", "") ?: ""
        }
        /// handle on back pressed
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),
            object : OnBackPressedCallback(true) { override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        commonWorkUtils = CommonWorkUtils(requireActivity())
        sessionManagement = SessionManagement(requireActivity())

        ///main function using all triggered of this screen
        initialize()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {

       /* /// check value is contains email aur phone
        if (value.toString().contains("@")) {
            binding.tvCodeSent.text = "we have sent the code to $value"
            binding.tvLogInType.text = " email"
        } else {
            binding.tvLogInType.text = " phone"
            binding.tvCodeSent.text = "**********"
        }*/

        val isEmail = value?.contains("@") == true

        val loginType=if (isEmail) " email" else " phone"

        binding.tvLogInType.text = loginType

        binding.tvCodeSent.text = if (isEmail) "We have sent the code to the$loginType below \n$value" else "*******"+value?.takeLast(3)


        /// screen type value for signup screen
        if (screenType.equals("signup",true)) {
            arguments?.let {
                userID = it.getString("userId", "")?:""
            }
            /// value get for social login
            cookingFor = when (sessionManagement.getCookingFor()) {
                "Myself" -> "1"
                "MyPartner" -> "2"
                else -> "3"
            }
            getValueFromSession()
        }

        //// handle on back pressed
        binding.imgBackVerification.setOnClickListener {
            findNavController().navigateUp()
        }

        /// handle click event for resend password timer and api
        binding.textResend.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (screenType.equals("signup",true)) {
                    signUpResendOtp()
                } else {
                    forgotPasswordApi()
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        //// handle click event for verify otp is valid or not
        binding.rlVerificationVerify.setOnClickListener {
            ///checking the device of mobile data in online and offline(show network error message) sign up otp verify api
            if (BaseApplication.isOnline(requireActivity())) {
                if (validate()) {
                    if (screenType.equals("signup",true)) {
                        signUpOtpVerify()
                    } else {
                        forgotOtpVerifyApi()
                    }
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

    }

    private fun getValueFromSession() {

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
            if (mealRoutineSelectedId.contains("-1")){
                mealRoutineSelectedId.remove("-1")
            }
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
            reasonTakeAway = sessionManagement.getReasonTakeAway()
        }

        if (sessionManagement.getReasonTakeAwayDesc() != "") {
            reasonTakeAwayDesc = sessionManagement.getReasonTakeAwayDesc()
        }

    }

    private fun signUpResendOtp() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            verificationViewModel.resendSignUpModel({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val resendSignUpModel = gson.fromJson(it.data, ResendSignUpOtpModel::class.java)
                            if (resendSignUpModel.code == 200 && resendSignUpModel.success) {
                                binding.relResendVerificationTimer.visibility = View.VISIBLE
                                binding.textResend.isEnabled = false
                                startTime()
                            } else {
                                handleCommon(resendSignUpModel.code,resendSignUpModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("verification@@","message"+e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, value.toString())
        }
    }

    private fun forgotPasswordApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            verificationViewModel.forgotPassword({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val forgotModel = gson.fromJson(it.data, ForgotPasswordModel::class.java)
                            if (forgotModel.code == 200 && forgotModel.success) {
                                binding.otpView.setOTP("")
                                binding.relResendVerificationTimer.visibility = View.VISIBLE
                                binding.textResend.isEnabled = false
                                startTime()
                            } else {
                                handleCommon(forgotModel.code,forgotModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("verification@@@","message"+e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, value.toString())
        }
    }

    private fun signUpOtpVerify() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            verificationViewModel.signUpOtpVerify({
                    BaseApplication.dismissMe()
                    when (it) {
                        is NetworkResult.Success -> {
                            try {
                                val gson = Gson()
                                val signUpVerificationModel = gson.fromJson(it.data, SignUpVerificationModel::class.java)
                                if (signUpVerificationModel.code == 200 && signUpVerificationModel.success) {
                                    showDataInSession(signUpVerificationModel.data)
                                } else {
                                    handleCommon(signUpVerificationModel.code,signUpVerificationModel.message)
                                }
                            }catch (e:Exception){
                                Log.d("verification@@@","message"+e.message)
                            }
                        }

                        is NetworkResult.Error -> {
                            showAlertFunction(it.message, false)
                        }

                        else -> {
                            showAlertFunction(it.message, false)
                        }
                    }
                },
                userID,
                binding.otpView.otp.toString(),
                userName,
                userGender,
                bodyGoals,
                cookingFrequency,
                eatingOut,
                reasonTakeAway,reasonTakeAwayDesc,
                cookingFor,
                partnerName,
                partnerAge,
                partnerGender,
                familyMemName,
                familyMemAge,
                familyMemStatus,
                mealRoutineSelectedId,
                spendingAmount,
                spendingDuration,
                dietarySelectedId,
                favouriteSelectedId,
                allergenSelectedId,
                dislikeSelectedId,
                "Android",
                token,sessionManagement.getReferralCode()
            )
        }
    }

    private fun handleCommon(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        } else {
            showAlertFunction(message, false)
        }
    }

    private fun showDataInSession(signUpVerificationModelData: SignUpVerificationModelData) {
        try {
            value?.let {
                if ("@" in it) sessionManagement.setEmail(it)
                else sessionManagement.setPhone(it)
            }
            sessionManagement.setUserName(signUpVerificationModelData.name?:"")
            val cookingFor = when (signUpVerificationModelData.user_type) {
                1 -> "Myself"
                2 -> "MyPartner"
                3 -> "MyFamily"
                else -> null
            }
            cookingFor?.let { sessionManagement.setCookingFor(it) }
            sessionManagement.setImage(signUpVerificationModelData.profile_img?:"")
            sessionManagement.setReferralCode(signUpVerificationModelData.referral_code ?: "")

            sessionManagement.setAuthToken(signUpVerificationModelData.token?:"")
            val id= signUpVerificationModelData.id?:0
            sessionManagement.setId(id.toString())
            successDialog(signUpVerificationModelData.is_cooking_complete)
        }catch (e:Exception){
            Log.d("verification","message"+e.message)
        }
    }

    /// implement forgot password verify api
    private fun forgotOtpVerifyApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            verificationViewModel.forgotOtpVerify(
                {
                    BaseApplication.dismissMe()
                    when (it) {
                        is NetworkResult.Success -> {
                            try {
                                val gson = Gson()
                                val forgotOtpModel = gson.fromJson(it.data, ForgotVerificationModel::class.java)
                                if (forgotOtpModel.code == 200 && forgotOtpModel.success) {
                                    try {
                                        val bundle = Bundle()
                                        bundle.putString("value", value)
                                        findNavController().navigate(R.id.resetPasswordFragment, bundle)
                                    }catch (e:Exception){
                                        Log.d("Verification","message"+e.message)
                                    }
                                } else {
                                    handleCommon(forgotOtpModel.code,forgotOtpModel.message)
                                }
                            }catch (e:Exception){
                                Log.d("Verification@@@","message"+e.message)

                            }
                        }

                        is NetworkResult.Error -> {
                            showAlertFunction(it.message, false)
                        }

                        else -> {
                            showAlertFunction(it.message, false)
                        }
                    }
                }, value.toString(), binding.otpView.otp.toString()
            )
        }
    }

    //// show error message
    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    /// start timer for counting 2 minutes
    private fun startTime() {
        object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                binding.textResend.setTextColor(Color.parseColor("#828282"))
                updateCountDownText()
            }

            override fun onFinish() {
                mTimeLeftInMillis = 120000
                binding.textResend.setTextColor(Color.parseColor("#06C169"))
                binding.relResendVerificationTimer.visibility = View.GONE
                binding.textResend.isEnabled = true
            }
        }.start()
    }

    /// update count timer
    @SuppressLint("SetTextI18n")
    private fun updateCountDownText() {
        val minutes = mTimeLeftInMillis.toInt() / 1000 / 60
        val seconds = mTimeLeftInMillis.toInt() / 1000 % 60
        val timeLeftFormatted = String.format(Locale.getDefault(), " %02d:%02d", minutes, seconds)
        binding.tvTimer.text = timeLeftFormatted + " sec"
    }

    /// validation part
    private fun validate(): Boolean {
        if (binding.otpView.otp.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.otp, false)
            return false
        } else if (binding.otpView.otp!!.length != 6) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.correctOtp, false)
            return false
        }
        return true
    }

    //// this function is used for success password match & redirect location permission screen
    private fun successDialog(isCookingComplete: Int?) {
        val dialogSuccess: Dialog = context?.let { Dialog(it) }!!
        dialogSuccess.setContentView(R.layout.alert_dialog_singup_success)
        dialogSuccess.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogSuccess.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlOkayBtn = dialogSuccess.findViewById<RelativeLayout>(R.id.rlOkayBtn)
        dialogSuccess.show()
        dialogSuccess.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        rlOkayBtn.setOnClickListener {
            if (isCookingComplete==0){
                sessionManagement.setPreferences(true)
                val intent = Intent(requireActivity(), EnterYourNameActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }else{
                sessionManagement.setLoginSession(true)
                findNavController().navigate(R.id.turnOnLocationFragment)
            }
            dialogSuccess.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        getFcmToken()
    }

    private fun getFcmToken() {
        lifecycleScope.launch {
            token = BaseApplication.fetchFcmToken()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}