package com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterOrderHistoryDetailsItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentOrderDetailsScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModel
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModelData
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.viewmodel.OrderHistoryViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailsScreenFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsScreenBinding
    private var dataList3: MutableList<DataModel> = mutableListOf()
    private lateinit var orderHistoryViewModel: OrderHistoryViewModel
    private var orderHistoryModel: MutableList<OrderHistoryModelData> = mutableListOf()
    private var adapterOrderHistoryDetailsItem: AdapterOrderHistoryDetailsItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderDetailsScreenBinding.inflate(inflater, container, false)

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        orderHistoryViewModel = ViewModelProvider(this)[OrderHistoryViewModel::class.java]

        adapterOrderHistoryDetailsItem =
            AdapterOrderHistoryDetailsItem(orderHistoryModel, requireActivity())
        binding.rcyOrderHistoryDetails.adapter = adapterOrderHistoryDetailsItem

        setupBackNavigation()

        initialize()

        orderHistoryModel()

        return binding.root
    }

    private fun initialize() {

        binding.imgBackOrderDetails.setOnClickListener {
            findNavController().navigateUp()
        }

        if (BaseApplication.isOnline(requireActivity())) {
            orderHistoryApi()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun orderHistoryApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            orderHistoryViewModel.orderHistoryUrl {
                BaseApplication.dismissMe()
                handleApiResponse(it)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, OrderHistoryModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                showDataInUI(apiModel.data)
            } else {
                handleError(apiModel.code, apiModel.message)
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


    private fun showDataInUI(data: MutableList<OrderHistoryModelData>?) {

        orderHistoryModel.clear()

        data.let {
            orderHistoryModel.addAll(it!!)
        }

      /*  if (orderHistoryModel.size > 0) {
            binding.relNoOrders.visibility = View.GONE
            binding.rcyOrderHistory.visibility = View.VISIBLE
            adapterOrderHistoryItem?.updateList(orderHistoryModel)
        } else {
            binding.relNoOrders.visibility = View.VISIBLE
            binding.rcyOrderHistory.visibility = View.GONE
        }*/
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

    private fun orderHistoryModel() {
        val data1 = DataModel()
        val data2 = DataModel()
        val data3 = DataModel()
        val data4 = DataModel()
        val data5 = DataModel()

        data1.title = "11 jan"
        data1.price = "$48.47"
        data1.distance = "Delivered - 926 Gainsway Street, NY 12603"
        data1.isOpen = false
        data1.quantity = "6 items"
        data1.image = R.drawable.ic_welmart_super_market

        data2.title = "11 jan"
        data2.price = "$48.47"
        data2.distance = "Delivered to Home\n" +
                "17 Sugar Lane South Ozone Park, NY 11420"
        data2.isOpen = false
        data2.quantity = "6 items"
        data2.image = R.drawable.ic_kroger_super_market

        data3.title = "11 jan"
        data3.price = "$48.47"
        data3.distance = "Delivered - 926 Gainsway Street, NY 12603"
        data3.isOpen = false
        data3.quantity = "6 items"
        data3.image = R.drawable.ic_target_super_market

        data4.title = "11 jan"
        data4.price = "$48.47"
        data4.distance = "Delivered - 926 Gainsway Street, NY 12603"
        data4.isOpen = false
        data4.quantity = "6 items"
        data4.image = R.drawable.ic_welmart_super_market

        data5.title = "11 jan"
        data5.price = "$48.47"
        data5.distance = "Delivered - 926 Gainsway Street, NY 12603"
        data5.isOpen = false
        data5.quantity = "6 items"
        data5.image = R.drawable.ic_kroger_super_market

        dataList3.add(data1)
        dataList3.add(data2)
        dataList3.add(data3)
        dataList3.add(data4)
        dataList3.add(data5)


    }


}