package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addnumberfragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentAddNumberVerifyBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addnumberfragment.model.OtpSendModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addnumberfragment.viewmodel.AddNumberVerifyViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.viewmodel.CheckoutScreenViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import `in`.aabhasjindal.otptextview.OTPListener
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class AddNumberVerifyFragment : Fragment() {

    private lateinit var binding: FragmentAddNumberVerifyBinding
    private lateinit var addNumberVerifyViewModel: CheckoutScreenViewModel
    private var lastNumber: String = ""
    private var countryCode: String = "+1"
    private lateinit var commonWorkUtils: CommonWorkUtils
    private val START_TIME_IN_MILLIS: Long = 120000
    private var mTimeLeftInMillis = START_TIME_IN_MILLIS
    private var countDownTimer: CountDownTimer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentAddNumberVerifyBinding.inflate(layoutInflater, container, false)

        commonWorkUtils = CommonWorkUtils(requireActivity())
        addNumberVerifyViewModel = ViewModelProvider(requireActivity())[CheckoutScreenViewModel::class.java]
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        initialize()

        return binding.root
    }

    private fun initialize() {

        binding.rlVerificationVerify.isEnabled=false

        binding.relBacks.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.countryCodePicker.setDefaultCountryUsingNameCode("US")

        binding.countryCodePicker.resetToDefaultCountry()

        binding.etRegPhone.addTextChangedListener(object : TextWatcher {
            private var isFormatting: Boolean = false
            private var prevLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                prevLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().filter { it.isDigit() }
                val formatted = formatPhone(digits)
                binding.etRegPhone.setText(formatted)
                binding.etRegPhone.setSelection(formatted.length.coerceAtMost(binding.etRegPhone.text.length))
                if (formatted.isNotEmpty()){
                    // Enable button and change color if 10 digits
                    if (lastNumber.equals(formatted,true)){
                        val digitsOnly = binding.etRegPhone.text.toString().filter { it.isDigit() }
                        val lastFour = digitsOnly.takeLast(3)
                        binding.tvCodeSent.text= "we have sent the code to *******$lastFour"
                        binding.tvVerify.isEnabled = false
                        binding.rlVerificationVerify.isEnabled = true
                        binding.tvVerificationError.visibility = View.GONE
                        binding.rlVerificationVerify.setBackgroundResource(R.drawable.gray_btn_select_background)
                        binding.relPhoneValidation.visibility = View.VISIBLE
                        binding.tvVerify.setTextColor(Color.parseColor("#999999"))
                    }else{
                        if (digits.length == 10) {
                            binding.tvVerify.isEnabled = true
                            binding.rlVerificationVerify.isEnabled = false
                            binding.rlVerificationVerify.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                            binding.relPhoneValidation.visibility = View.GONE
                            binding.tvVerificationError.visibility = View.GONE
                            binding.tvVerify.setTextColor(Color.parseColor("#06C169"))
                        } else {
                            binding.tvVerify.isEnabled = false
                            binding.rlVerificationVerify.isEnabled = false
                            binding.rlVerificationVerify.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                            binding.relPhoneValidation.visibility = View.GONE
                            binding.tvVerificationError.visibility = View.GONE
                            binding.tvVerify.setTextColor(Color.parseColor("#999999"))
                        }
                    }
                }
                isFormatting = false
            }

            private fun formatPhone(digits: String): String {
                return when {
                    digits.length <= 3 -> digits
                    digits.length <= 6 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
                    digits.length <= 10 -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
                    else -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6, 10)}" // limit to 10 digits
                }
            }
        })


        binding.countryCodePicker.setOnCountryChangeListener {
            countryCode = "+" + binding.countryCodePicker.selectedCountryCode
            Log.d("CountryCode", "Selected Country Code: $countryCode")
        }

        // Click Listener
        binding.tvVerify.setOnClickListener {
            getOtpUrl("verify")
        }

        binding.textResend.setOnClickListener {
            getOtpUrl("resend")
        }

        binding.rlVerificationVerify.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (isValidation())
                    addNumberUrl()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // Called when user starts typing
                binding.tvVerificationError.visibility = View.GONE
            }

            override fun onOTPComplete(otp: String) {
                // Called when OTP is fully entered
                binding.tvVerificationError.visibility = View.GONE

            }
        }

    }

    /// add validation based on valid email or phone
    private fun validate(): Boolean {
        // Check if email/phone is empty
        if (binding.etRegPhone.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.phoneNumber, false)
            return false
        }
        // Check if email or phone is valid
        else if (!validNumber()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.validPhone, false)
            return false
        }

        return true
    }

    /// add validation based on valid phone number
    private fun validNumber(): Boolean {
        val input = binding.etRegPhone.text.toString().trim()
        val digitsOnly = input.replace(Regex("[^\\d]"), "") // Remove dashes and other non-digit characters

        // Check if it's exactly 10 digits
        return digitsOnly.length == 10
    }



    private fun isValidation() : Boolean{
        if (binding.otpView.otp?.isEmpty() == true){
            showAlert(ErrorMessage.otpError, false)
            return false
        }else if (binding.otpView.otp?.length != 6){
            showAlert(ErrorMessage.otpValidError, false)
            return false
        }
        return true
    }


    private fun getOtpUrl(type:String) {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            binding.tvVerificationError.visibility = View.GONE
            val plainNumber = binding.etRegPhone.text.filter { it.isDigit() }
            lifecycleScope.launch {
                addNumberVerifyViewModel.sendOtpUrl({
                    BaseApplication.dismissMe()
                    handleApiOtpSendResponse(it,type)
                }, countryCode + plainNumber)
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

    }

    private fun addNumberUrl() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            addNumberVerifyViewModel.addPhoneUrl(
                {
                    BaseApplication.dismissMe()
                    handleApiVerifyResponse(it)
                },
                binding.etRegPhone.text.toString().trim(),
                binding.otpView.otp.toString(),
                countryCode
            )
        }
    }

    private fun handleApiOtpSendResponse(result: NetworkResult<String>,type:String) {
        when (result) {
            is NetworkResult.Success -> handleSuccessOtpResponse(result.data.toString(),type)
            is NetworkResult.Error -> {
                showAlert(result.message, false)
            }
            else -> showAlert(result.message, false)
        }
    }

    private fun handleApiVerifyResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessVerifyResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun handleSuccessOtpResponse(data: String,type:String) {
        binding.relResendVerificationTimer.visibility = View.GONE
        binding.otpView.setOTP("")
        binding.rlVerificationVerify.isEnabled = true
        binding.rlVerificationVerify.setBackgroundResource(R.drawable.green_btn_background)
        val digitsOnly = binding.etRegPhone.text.toString().filter { it.isDigit() }
        val lastFour = digitsOnly.takeLast(3)
        binding.tvCodeSent.text= "we have sent the code to *******$lastFour"
        binding.tvTimer.text="01:59 sec"
        try {
            val apiModel = Gson().fromJson(data, OtpSendModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                binding.relPhoneValidation.visibility = View.VISIBLE
                lastNumber = binding.etRegPhone.text.toString().trim()
                if (type.equals("verify",true)){
                    countDownTimer?.cancel()
                    mTimeLeftInMillis = 120000
                    binding.tvVerify.isEnabled = false
                    binding.tvVerify.setTextColor(Color.parseColor("#999999"))
                    binding.textResend.isEnabled = true
                    binding.textResend.setTextColor(Color.parseColor("#06C169"))
                }else{
                    binding.relResendVerificationTimer.visibility = View.VISIBLE
                    binding.textResend.setTextColor(Color.parseColor("#828282"))
                    binding.textResend.isEnabled = false
                    startTime()
                }
            } else {
                handleError(apiModel.code,apiModel.message,true)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    /// start timer for counting 2 minutes
    private fun startTime() {
        countDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
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
        binding.tvTimer.text = "$timeLeftFormatted sec"
    }



    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun handleSuccessVerifyResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, OtpSendModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {

                binding.tvVerificationError.visibility = View.GONE
                addNumberVerifyViewModel.dataCheckOut?.let {
                    it.phone=binding.etRegPhone.text.toString().trim()
                    it.country_code=countryCode
                }

                findNavController().navigateUp()
            } else {
                binding.tvVerificationError.visibility = View.VISIBLE
                handleError(apiModel.code,apiModel.message,false)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int, message: String,type: Boolean) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            if (type){
                showAlert(message, false)
            }
        }
    }

}