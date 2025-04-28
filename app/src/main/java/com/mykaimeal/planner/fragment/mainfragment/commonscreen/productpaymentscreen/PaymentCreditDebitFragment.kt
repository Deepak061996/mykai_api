package com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterPaymentCreditDebitItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentPaymentCreditDebitBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen.model.AddCardMealMeModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen.model.GetCardMealMeModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen.model.GetCardMealMeModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen.viewmodel.PaymentCreditDebitViewModel
import com.mykaimeal.planner.listener.CardBankListener
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class PaymentCreditDebitFragment : Fragment(), CardBankListener {

    private lateinit var binding: FragmentPaymentCreditDebitBinding
    private var checkUnchecked: Boolean? = false
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var adapterPaymentCreditDebitItem: AdapterPaymentCreditDebitItem
    private var month: Int = 0
    private var year: Int = 0
    private var checkStatus: String = "0"
    private lateinit var paymentCreditDebitViewModel: PaymentCreditDebitViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPaymentCreditDebitBinding.inflate(layoutInflater, container, false)

        commonWorkUtils = CommonWorkUtils(requireActivity())
        paymentCreditDebitViewModel =
            ViewModelProvider(requireActivity())[PaymentCreditDebitViewModel::class.java]

        setupBackNavigation()
        initialize()

        return binding.root

    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun initialize() {

        if (BaseApplication.isOnline(requireActivity())) {
            getCardMealMe()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

        binding.imgCreditDebit.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etMonth.setOnClickListener {
            openMonthPickerBox()
        }

        binding.etYear.setOnClickListener {
            openYearPickerBox()
        }

        binding.imgCheckUncheck.setOnClickListener {
            checkUnchecked = !checkUnchecked!!
            if (checkUnchecked as Boolean) {
                checkStatus = "1"
                binding.imgCheckUncheck.setImageResource(R.drawable.tick_ckeckbox_images)
            } else {
                checkStatus = "0"
                binding.imgCheckUncheck.setImageResource(R.drawable.uncheck_box_images)
            }
        }

        binding.tvSaveCreditDebitCard.setOnClickListener {
            if (isValidationCard()) {
                if (BaseApplication.isOnline(requireActivity())) {
                    cardSaveApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        binding.textAddCardNumber.setOnClickListener {
            binding.cvDebitCard3.visibility = View.VISIBLE
            binding.textAddCardNumber.visibility = View.GONE
        }
    }

    private fun getCardMealMe() {
        if (BaseApplication.isOnline(requireActivity())) {
            fetchUserCardData()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun fetchUserCardData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            paymentCreditDebitViewModel.getCardMealMeUrl { result ->
                BaseApplication.dismissMe()
                handleGetCardApiResponse(result)
            }
        }
    }

    private fun handleGetCardApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> processGetCardSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun processGetCardSuccessResponse(response: String) {
        try {
            val apiModel = Gson().fromJson(response, GetCardMealMeModel::class.java)
            Log.d("@@@ Response cardBank ", "message :- $response")
            if (apiModel.code == 200 && apiModel.success == true) {
                showDataInUi(apiModel.data)
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataInUi(data: MutableList<GetCardMealMeModelData>?) {

        if (data != null && data.size > 0) {
            binding.cvDebitCard3.visibility = View.GONE
            binding.textAddCardNumber.visibility = View.VISIBLE
            binding.relSavedCards.visibility = View.VISIBLE
            adapterPaymentCreditDebitItem = AdapterPaymentCreditDebitItem(requireContext(), data, this)
            binding.rcvCardNumber.adapter = adapterPaymentCreditDebitItem
        } else {
            binding.cvDebitCard3.visibility = View.VISIBLE
            binding.textAddCardNumber.visibility = View.GONE
            binding.relSavedCards.visibility = View.GONE
        }
    }

    private fun cardSaveApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            paymentCreditDebitViewModel.addCardMealMeUrl(
                {
                    BaseApplication.dismissMe()
                    handleApiAddCardResponse(it)
                }, binding.etCardNumber.text.toString().trim(),
                binding.etMonth.text.toString().trim(),
                binding.etYear.text.toString().trim(),
                binding.etCVVNumber.text.toString().trim(), checkStatus
            )
        }
    }

    private fun handleApiAddCardResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleUpdateAddCardResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleUpdateAddCardResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, AddCardMealMeModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                month = 0
                year = 0
                binding.etCardName.text.clear()
                binding.etCardNumber.text.clear()
                binding.etCVVNumber.text.clear()
                binding.etMonth.text = "Month"
                binding.etYear.text = "Year"
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()

                // When screen load then api call
                getCardMealMe()

            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun openMonthPickerBox() {
        // Get the current calendar instance
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)

        // Array of month names
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        // Create a dialog
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_picker, null)

        // Get references to the NumberPicker
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.Picker)

        // Configure the month picker
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = monthNames
        monthPicker.value = currentMonth

        // Set the custom view in the dialog
        dialog.setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedMonth = monthPicker.value + 1 // Months are 0-based, so add 1
                val formattedMonth = String.format("%02d", selectedMonth) // Format to two digits

                // Update the TextView with the formatted month number
                binding.etMonth.text = formattedMonth

//                Toast.makeText(requireContext(), "Selected Month: $formattedMonth", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)

        // Show the dialog
        dialog.create().show()
    }


    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun openYearPickerBox() {
        // Get the current calendar instance
        val calendar = Calendar.getInstance()
        // Extract the current year and month
        val currentYear = calendar.get(Calendar.YEAR)
        // Create a dialog
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_picker, null)
        // Get references to the NumberPickers in the custom dialog layout
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.Picker)
        // Configure the year picker
        monthPicker.minValue = currentYear
        monthPicker.maxValue = currentYear + 50 // Limit to 50 years ahead
        monthPicker.value = currentYear
        // Set the custom view in the dialog
        dialog.setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                // Get selected month and year
                val selectedMonth = monthPicker.value
                year = monthPicker.value
                // Update the TextView with the selected month name and year
                binding.etYear.text = "" + selectedMonth
            }
            .setNegativeButton("Cancel", null)
        // Show the dialog
        dialog.create().show()
    }

    private fun isValidationCard(): Boolean {
        if (binding.etCardName.text.toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cardholderError, false)
            return false
        } else if (binding.etCardNumber.text.toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cardNumberError, false)
            return false
        } else if (binding.etCVVNumber.text.toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cvvError, false)
            return false
        } else if (binding.etCVVNumber.text.toString().length == 1 || binding.etCVVNumber.text.toString().length == 2) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cvvValidError, false)
            return false
        } else if (binding.etMonth.text.toString().equals("Month", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.monthError, false)
            return false
        } else if (binding.etYear.text.toString().equals("Year", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.yearError, false)
            return false
        }

        return true
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {
        if (type!!.equals("delete", true)) {
            if (BaseApplication.isOnline(requireActivity())) {
                deleteApi(status)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }else{
            if (type.equals("preferred", true)) {
                if (BaseApplication.isOnline(requireActivity())) {
                    preferredApi(status)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun preferredApi(status: String?) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            paymentCreditDebitViewModel.setPreferredCardMealMeUrl({
                BaseApplication.dismissMe()
                handleApiPreferredCardResponse(it)
            }, status)
        }
    }

    private fun handleApiPreferredCardResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleUpdatePreferredResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleUpdatePreferredResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, AddCardMealMeModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                // When screen load then api call
                getCardMealMe()

            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    private fun deleteApi(status: String?) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            paymentCreditDebitViewModel.deleteCardMealMeUrl({
                BaseApplication.dismissMe()
                handleApiDeleteCardResponse(it)
            }, status)
        }
    }

    private fun handleApiDeleteCardResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleUpdateDeleteResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleUpdateDeleteResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, AddCardMealMeModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                // When screen load then api call
                getCardMealMe()

            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

}