package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

@AndroidEntryPoint
class AddTipScreenFragment : Fragment() {

    private var _binding: FragmentAddTipScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var addTipScreenViewModel: AddTipScreenViewModel
    private var totalPrices = ""
    private var cardId = ""
    private var status = ""
    private var selectedTipPercent: String=""

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
                    if (!selectedTipPercent.equals("",true))
                    paymentCreditDebitApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

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

}