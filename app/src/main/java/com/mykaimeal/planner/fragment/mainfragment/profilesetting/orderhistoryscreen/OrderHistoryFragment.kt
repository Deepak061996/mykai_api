package com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterOrderHistoryItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentOrderHistoryBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModel
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModelData
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.viewmodel.OrderHistoryViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderHistoryFragment : Fragment(), OnItemClickedListener {

    private lateinit var binding: FragmentOrderHistoryBinding
    private var adapterOrderHistoryItem: AdapterOrderHistoryItem? = null
    private lateinit var orderHistoryViewModel: OrderHistoryViewModel
    private var screen: String = ""
    private var orderHistoryModel: MutableList<OrderHistoryModelData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        orderHistoryViewModel = ViewModelProvider(requireActivity())[OrderHistoryViewModel::class.java]

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        screen = arguments?.getString("screen", "") ?: ""


        adapterOrderHistoryItem = AdapterOrderHistoryItem(orderHistoryModel, requireActivity(), this)
        binding.rcyOrderHistory.adapter = adapterOrderHistoryItem

        setupBackNavigation()

        initialize()

        return binding.root
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    moveToScreen()
                }
            })
    }

    private fun initialize() {

        binding.imgBackOrderHistory.setOnClickListener {
            moveToScreen()
        }

//        binding.rlStartOrder.setOnClickListener {
//            binding.relNoOrders.visibility = View.GONE
//            binding.rcyOrderHistory.visibility = View.VISIBLE
//        }


        if (orderHistoryViewModel.dataOrderHistory!=null){
            showDataInUI(orderHistoryViewModel.dataOrderHistory!!)
        }else{
            loadApi()
        }

    }

    private fun loadApi(){
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
                apiModel.data?.let {
                    orderHistoryModel.clear()
                    orderHistoryModel.addAll(it)
                    showDataInUI(it)
                }?.run {
                    binding.relNoOrders.visibility = View.GONE
                    binding.rcyOrderHistory.visibility = View.VISIBLE
                }
            } else {
                binding.relNoOrders.visibility = View.GONE
                binding.rcyOrderHistory.visibility = View.VISIBLE
                handleError(apiModel.code, apiModel.message)
            }

        } catch (e: Exception) {
            binding.relNoOrders.visibility = View.GONE
            binding.rcyOrderHistory.visibility = View.VISIBLE
            showAlert(e.message, false)
        }
    }

    private fun showDataInUI(data: MutableList<OrderHistoryModelData>) {
        try {
            orderHistoryViewModel.setOrderHistoryData(data)
            if (data.size > 0) {
                binding.relNoOrders.visibility = View.GONE
                binding.rcyOrderHistory.visibility = View.VISIBLE
                adapterOrderHistoryItem?.updateList(data)
            }else{
                binding.relNoOrders.visibility=View.VISIBLE
                binding.rcyOrderHistory.visibility = View.GONE
            }
        }catch (e:Exception){
            binding.relNoOrders.visibility=View.VISIBLE
            binding.rcyOrderHistory.visibility = View.GONE
            Log.d("Error","******"+e.message)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }


    private fun moveToScreen() {
        if (screen.equals("yes", true)) {
            findNavController().navigate(R.id.homeFragment)
        } else {
            findNavController().navigateUp()
        }
    }


    override fun itemClicked(position: Int?, list: MutableList<String>?, status: String?, type: String?) {
        if (type.equals("View",true)) {
            val selectedItem = orderHistoryModel[position!!]
            val gson = Gson()
            val jsonString = gson.toJson(selectedItem)
            val bundle = Bundle().apply {
                putString("order_item_json", jsonString)
            }
            findNavController().navigate(R.id.orderDetailsScreenFragment,bundle)
        } else {
            val selectedItem = orderHistoryModel[position!!]
            if (selectedItem.order?.tracking_link!=null){
                val bundle = Bundle().apply {
                    putString("tracking", selectedItem.order.tracking_link)
                }
                findNavController().navigate(R.id.trackOrderScreenFragment, bundle)
            }
        }

    }

}