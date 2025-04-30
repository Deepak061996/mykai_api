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
    private var selectedTipPercent: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {

        binding.tvDescriptions.text = "100% of your tip goes to your courier. Tips are based on your order total of $$totalPrices before any discounts or promotions."

        binding.relBack.setOnClickListener {
            findNavController().navigateUp()
        }

        if (BaseApplication.isOnline(requireActivity())) {
            getTipUrl()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

        binding.etSignEmailPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editText: Editable) {
                if (editText.isNotEmpty()) {
                    status = "1"
                    val allViews = listOf(binding.linearNotNow, binding.llTenPerc, binding.llFifteenPerc, binding.llTwentyPerc, binding.lltwentyFivePerc)
                    allViews.forEach { it.setBackgroundResource(R.drawable.edittext_bg) }
                }else{
                    status = ""
                }
                searchable()
            }
        })

        setupListeners()

        binding.rlProceedAndPay.setOnClickListener {

            // (Optional) Log selected tip amount
            val selectedTipAmount = when (selectedTipPercent) {
                10 -> binding.tvDollarSeven.text.toString()
                15 -> binding.tvDollarNine.text.toString()
                20 -> binding.tvDollarTwelve.text.toString()
                25 -> binding.tvDollarFifteen.text.toString()
                else -> "$0"
            }

            Log.d("SelectedTip", "Tip $selectedTipPercent% -> $selectedTipAmount")

            if (status.isNotEmpty()) {
                if (BaseApplication.isOnline(requireContext())) {
                    paymentCreditDebitApi(selectedTipAmount)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun getTipUrl() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            addTipScreenViewModel.getTipUrl({
                BaseApplication.dismissMe()
                handleApiTipResponse(it)
            }, totalPrices)
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

        if (data.tip10 != null) {
            val roundedTipTen = data.tip10.roundToInt()
            binding.tvDollarSeven.text = "$$roundedTipTen"
        }

        if (data.tip15!=null){
            val roundedTipFifteen = data.tip15.roundToInt()
            binding.tvDollarNine.text = "$$roundedTipFifteen"
        }

        if (data.tip20!=null){
            val roundedTipTwenty = data.tip20.roundToInt()
            binding.tvDollarTwelve.text="$$roundedTipTwenty"
        }

        if (data.tip25!=null){
            val roundedTipTwentyFive = data.tip25.roundToInt()
            binding.tvDollarFifteen.text="$$roundedTipTwentyFive"
        }
    }

    private fun setupListeners() {
        binding.etSignEmailPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editText: Editable) {
                if (editText.isNotEmpty()) {
                    status = "1"
                    val allViews = listOf(
                        binding.linearNotNow,
                        binding.llTenPerc,
                        binding.llFifteenPerc,
                        binding.llTwentyPerc,
                        binding.lltwentyFivePerc
                    )
                    allViews.forEach { it.setBackgroundResource(R.drawable.edittext_bg) }
                } else {
                    status = ""
                }
                searchable()
            }
        })

        binding.linearNotNow.setOnClickListener { updateSelection(binding.linearNotNow) }
        binding.llTenPerc.setOnClickListener { updateSelection(binding.llTenPerc) }
        binding.llFifteenPerc.setOnClickListener { updateSelection(binding.llFifteenPerc) }
        binding.llTwentyPerc.setOnClickListener { updateSelection(binding.llTwentyPerc) }
        binding.lltwentyFivePerc.setOnClickListener { updateSelection(binding.lltwentyFivePerc) }
    }

    private fun updateSelection(selectedView: View) {
        status = "1"
        searchable()

        val allViews = listOf(
            binding.linearNotNow,
            binding.llTenPerc,
            binding.llFifteenPerc,
            binding.llTwentyPerc,
            binding.lltwentyFivePerc
        )
        allViews.forEach { it.setBackgroundResource(R.drawable.edittext_bg) }
        selectedView.setBackgroundResource(R.drawable.outline_green_border_bg)

        selectedTipPercent = when (selectedView) {
            binding.linearNotNow -> 0
            binding.llTenPerc -> 10
            binding.llFifteenPerc -> 15
            binding.llTwentyPerc -> 20
            binding.lltwentyFivePerc -> 25
            else -> null
        }

        // Clear and hide keyboard
        binding.etSignEmailPhone.text.clear()
        binding.etSignEmailPhone.clearFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSignEmailPhone.windowToken, 0)

    }


/*
    private fun updateSelection(selectedView: View) {
        status = "1"
        searchable()
        val allViews = listOf(binding.linearNotNow, binding.llTenPerc, binding.llFifteenPerc, binding.llTwentyPerc, binding.lltwentyFivePerc)
        allViews.forEach { it.setBackgroundResource(R.drawable.edittext_bg) }
        selectedView.setBackgroundResource(R.drawable.outline_green_border_bg)

        // Set selected tip value
        selectedTipPercent = when (selectedView) {
            binding.linearNotNow -> 0
            binding.llTenPerc -> 10
            binding.llFifteenPerc -> 15
            binding.llTwentyPerc -> 20
            binding.lltwentyFivePerc -> 25
            else -> null
        }

        binding.etSignEmailPhone.text.clear()
        binding.etSignEmailPhone.clearFocus()

        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSignEmailPhone.windowToken, 0)
    }*/

    private fun searchable() {
        if (status != "") {
            status = "1"
            binding.rlProceedAndPay.setBackgroundResource(R.drawable.green_fill_corner_bg)
        } else {
            status = ""
            binding.rlProceedAndPay.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }

    private fun paymentCreditDebitApi(selectedTipAmount: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            addTipScreenViewModel.getOrderProductUrl({
                BaseApplication.dismissMe()
                handleApiOrderResponse(it)
            },selectedTipAmount,cardId)
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