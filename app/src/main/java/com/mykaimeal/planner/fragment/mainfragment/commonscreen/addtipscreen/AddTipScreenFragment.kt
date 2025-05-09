package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.gson.Gson
import kotlin.math.roundToInt
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentAddTipScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen.model.GetTipModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen.model.GetTipModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen.model.OrderProductTrackModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen.model.Response
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen.viewmodel.AddTipScreenViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class AddTipScreenFragment : Fragment() {

    private var _binding: FragmentAddTipScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var addTipScreenViewModel: AddTipScreenViewModel
    private var totalPrices = ""
    private var cardId = ""
    private var status = ""
    private var selectedTipPercent: String=""

    private lateinit var paymentsClient: PaymentsClient
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddTipScreenBinding.inflate(layoutInflater, container, false)
        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }
        addTipScreenViewModel = ViewModelProvider(requireActivity())[AddTipScreenViewModel::class.java]
        totalPrices = arguments?.getString("totalPrices") ?: ""
        cardId = arguments?.getString("cardId") ?: ""
        setupBackNavigation()
        initialize()
        return binding.root
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {

        binding.rlProceedAndPay.isClickable=false

        binding.tvDescriptions.text = "100% of your tip goes to your courier. Tips are based on your order total of $$totalPrices before any discounts or promotions."
        binding.relBack.setOnClickListener {
            findNavController().navigateUp()
        }
        getTipUrl()
        setupListeners()


        binding.rlProceedAndPay.setOnClickListener {
            if (binding.rlProceedAndPay.isClickable) {
                if (BaseApplication.isOnline(requireContext())) {
                    if (!selectedTipPercent.equals("",true)){
                        if (cardId.equals("gpay",true)){
//                            val paymentDataRequestJson = createPaymentDataRequest()
//                            val paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
//                            AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(paymentDataRequest), requireActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE)
//
//                            val paymentDataRequestJson = createPaymentDataRequest()
//                            val paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
//                            AutoResolveHelper.resolveTask(
//                                paymentsClient.loadPaymentData(paymentDataRequest),
//                                requireActivity(),
//                                LOAD_PAYMENT_DATA_REQUEST_CODE
//                            )

                            val paymentDataRequestJson = createPaymentDataRequest()
                            val paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
                            val task = paymentsClient.loadPaymentData(paymentDataRequest)
                            task.addOnFailureListener { e ->
                                if (e is ResolvableApiException) {
                                    try {
                                        val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                                        loadPaymentDataLauncher.launch(intentSenderRequest)
                                    } catch (sendEx: Exception) {
                                        sendEx.printStackTrace()
                                    }
                                } else {
                                    e.printStackTrace()
                                }
                            }


                        }else{
                            paymentCreditDebitApi()
                        }
                    }
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        gpayPaymentImplement()

    }
    private val loadPaymentDataLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val paymentData = PaymentData.getFromIntent(result.data!!)
            val json = paymentData?.toJson()
            val paymentMethodData = JSONObject(json)
                .getJSONObject("paymentMethodData")
            val token = paymentMethodData
                .getJSONObject("tokenizationData")
                .getString("token")
            Log.d("Token **** token ","****** :- "+token)

            // TODO: Send token to your backend for processing
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            // User canceled the payment
            Log.d("Token ****","****** :- "+"RESULT_CANCELED")
        } else {
            val status = AutoResolveHelper.getStatusFromIntent(result.data)
            // Handle error (e.g., show message to user)
            Log.d("Token **** status","****** :- "+status)
        }
    }

    private fun gpayPaymentImplement() {
        // Initialize PaymentsClient
        paymentsClient = Wallet.getPaymentsClient(
            requireContext(),
            Wallet.WalletOptions.Builder()
//                .setEnvironment(WalletConstants.ENVIRONMENT_TEST) // Use ENVIRONMENT_PRODUCTION for live
                .setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION) // Use ENVIRONMENT_PRODUCTION for live
                .build()
        )
    }

    private fun createPaymentDataRequest(): JSONObject {
        val tokenizationSpec = JSONObject()
            .put("type", "PAYMENT_GATEWAY")
            .put("parameters", JSONObject()
                .put("gateway", "stripe")  // Replace with "stripe"
                .put("stripe:version", "2020-08-27") // Adjust as needed
                .put("stripe:publishableKey", "pk_live_51Qko2KEowij4RlG8jPdIDKTTaX12y4tNGgP2CWL2YAEOy4XMQx7vhEAeAtbmpaohAx7VOBPq0Z7iMBsAiygbJpAM00RcRMGU0W ") // Replace with real key
            )

        val cardPaymentMethod = JSONObject()
            .put("type", "CARD")
            .put("tokenizationSpecification", tokenizationSpec)
            .put("parameters", JSONObject()
                .put("allowedAuthMethods", JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
                .put("allowedCardNetworks", JSONArray().put("VISA").put("MASTERCARD").put("AMEX").put("DISCOVER"))
                .put("billingAddressRequired", true)
                .put("billingAddressParameters", JSONObject().put("format", "FULL"))
            )

        return JSONObject()
            .put("apiVersion", 2)
            .put("apiVersionMinor", 0)
            .put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
            .put("transactionInfo", JSONObject()
                .put("totalPrice", totalPrices) // Adjust dynamically as needed
                .put("totalPriceStatus", "FINAL")
                .put("currencyCode", "USD") // Use USD for US payments
            )
            .put("merchantInfo", JSONObject()
                .put("merchantName", "My Kai Ltd")
                .put("merchantId", "acct_1Qko2KEowij4RlG8")   // ❌ Stripe Account ID — Not Allowed
            )
    }

    private fun getTipUrl() {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                addTipScreenViewModel.getTipUrl({
                    BaseApplication.dismissMe()
                    handleApiTipResponse(it)
                }, totalPrices)
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

    }

    private fun handleApiTipResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessTipResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessTipResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, GetTipModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data!=null){
                    showDataInTipUI(apiModel.data)
                }
            } else {
               handleError(apiModel.code,apiModel.message)
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

    @SuppressLint("SetTextI18n")
    private fun showDataInTipUI(data: GetTipModelData) {
        data.tip10?.let {
            val roundedTipTen = it.roundToInt()
            binding.tvDollarSeven.text = "$$roundedTipTen"
        }
        data.tip15?.let {
            val roundedTipFifteen = it.roundToInt()
            binding.tvDollarNine.text = "$$roundedTipFifteen"
        }
        data.tip20?.let {
            val roundedTipTwenty = it.roundToInt()
            binding.tvDollarTwelve.text="$$roundedTipTwenty"
        }
        data.tip25?.let {
            val roundedTipTwentyFive = it.roundToInt()
            binding.tvDollarFifteen.text="$$roundedTipTwentyFive"
        }

    }

    private fun setupListeners() {
        binding.etSignEmailPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editText: Editable) {
                if (editText.isNotEmpty()) {
                    selectedTipPercent=editText.toString()
                    binding.rlProceedAndPay.isClickable=true
                    binding.rlProceedAndPay.setBackgroundResource(R.drawable.green_fill_corner_bg)
                    val allViews = listOf(binding.linearNotNow, binding.llTenPerc, binding.llFifteenPerc, binding.llTwentyPerc, binding.lltwentyFivePerc)
                    allViews.forEach { it.setBackgroundResource(R.drawable.edittext_bg) }
                } else {
                    selectedTipPercent=""
                    binding.rlProceedAndPay.isClickable=false
                    binding.rlProceedAndPay.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                }
            }
        })

        binding.linearNotNow.setOnClickListener { updateSelection(binding.linearNotNow,"0") }
        binding.llTenPerc.setOnClickListener { updateSelection(binding.llTenPerc,binding.tvDollarSeven.text.toString()) }
        binding.llFifteenPerc.setOnClickListener { updateSelection(binding.llFifteenPerc,binding.tvDollarNine.text.toString()) }
        binding.llTwentyPerc.setOnClickListener { updateSelection(binding.llTwentyPerc,binding.tvDollarTwelve.text.toString()) }
        binding.lltwentyFivePerc.setOnClickListener { updateSelection(binding.lltwentyFivePerc,binding.tvDollarFifteen.text.toString()) }

    }

    private fun updateSelection(selectedView: View,value:String) {
        selectedTipPercent = value
        val allViews = listOf(binding.linearNotNow, binding.llTenPerc, binding.llFifteenPerc, binding.llTwentyPerc, binding.lltwentyFivePerc)
        allViews.forEach { it.setBackgroundResource(R.drawable.edittext_bg) }
        selectedView.setBackgroundResource(R.drawable.outline_green_border_bg)
        // Clear and hide keyboard
        binding.etSignEmailPhone.text.clear()
        binding.etSignEmailPhone.clearFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSignEmailPhone.windowToken, 0)
        if (selectedTipPercent.equals("",true)){
            binding.rlProceedAndPay.isClickable=false
            binding.rlProceedAndPay.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }else{
            binding.rlProceedAndPay.isClickable=true
            binding.rlProceedAndPay.setBackgroundResource(R.drawable.green_fill_corner_bg)
        }
    }


    private fun paymentCreditDebitApi() {
        BaseApplication.showMe(requireContext())
        val tipAmount=selectedTipPercent.replace("$", "")
        lifecycleScope.launch {
            addTipScreenViewModel.getOrderProductUrl({
                BaseApplication.dismissMe()
                handleApiOrderResponse(it)
            },tipAmount,cardId)
        }
    }

    private fun handleApiOrderResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessOrderResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessOrderResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, OrderProductTrackModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code != null) {
                if (apiModel.code == 200 && apiModel.success == true) {
                    if (apiModel.response != null) {
                        showDataInUI(apiModel.response)
                    }
                } else {
                    handleError(apiModel.code,apiModel.message.toString())
                }
            } else {
                if (apiModel.response?.error != null) {
                    showAlert(apiModel.response.error, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataInUI(response: Response) {
        Toast.makeText(requireContext(), "Payment successful", Toast.LENGTH_SHORT).show()
        if (response.tracking_link != null) {
            val bundle = Bundle().apply {
                putString("tracking", response.tracking_link)
            }
            findNavController().navigate(R.id.trackOrderScreenFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val paymentData = data?.let { PaymentData.getFromIntent(it) }
                    val paymentInfo = paymentData?.toJson()?.let { JSONObject(it) }
                    Log.d("GPayImplement", "Payment Success: $paymentInfo")

                    val tokenData = paymentInfo
                        ?.getJSONObject("paymentMethodData")
                        ?.getJSONObject("tokenizationData")
                        ?.getString("token")

                    Log.d("GPayImplement", "Token: $tokenData")
                    Toast.makeText(requireContext(), "Payment Success", Toast.LENGTH_SHORT).show()

                    // TODO: Send token to your backend (Stripe or other processor)
                }

                Activity.RESULT_CANCELED -> {
                    Toast.makeText(requireContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show()
                }

                AutoResolveHelper.RESULT_ERROR -> {
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    Log.e("GPayImplement", "Error: ${status?.statusMessage}")
                    Toast.makeText(requireContext(), "Payment Failed: ${status?.statusMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}