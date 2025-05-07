package com.mykaimeal.planner.fragment.mainfragment.profilesetting

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentWalletBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.WalletViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsetransfer.TransferModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalletFragment : Fragment() {

    
    private lateinit var binding: FragmentWalletBinding

    private lateinit var viewModel: WalletViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWalletBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[WalletViewModel::class.java]

        (activity as MainActivity).binding.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        setupBackNavigation()

        setupUI()

        // When screen load then api call
        fetchWalletLoad()


        return binding.root
    }

    private fun fetchWalletLoad() {
        if (BaseApplication.isOnline(requireActivity())) {
            fetchWalletData()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun fetchWalletData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.getWalletRequest { result ->
                BaseApplication.dismissMe()
                handleApiResponse(result)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> processSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun processSuccessResponse(response: String) {
        try {
            val apiModel = Gson().fromJson(response, TransferModel::class.java)
            Log.d("@@@ Response wallet ", "message :- $response")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data!=null){

                    binding.textCurrentBalance.text="$ "+apiModel.data.walletbalance

                    if (apiModel.data.name!=null){
                        binding.textWalletHolderName.text=apiModel.data.name
                    }

                    if (apiModel.data.date!=null){
                        binding.textOnDateMonthYear.text="On "+apiModel.data.date
                    }
                }
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


    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun setupUI() {


        binding.imgWallet.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.rlWithdrawAmountButton.setOnClickListener {
            if (binding.textCurrentBalance.text.toString().equals("$ 0",true)){
                BaseApplication.alertError(requireContext(), ErrorMessage.amountNoError, false)
            }else{
                val bundle=Bundle()
                bundle.putString("amount",binding.textCurrentBalance.text.toString())
                findNavController().navigate(R.id.paymentMethodFragment,bundle)
            }
        }

    }

}
