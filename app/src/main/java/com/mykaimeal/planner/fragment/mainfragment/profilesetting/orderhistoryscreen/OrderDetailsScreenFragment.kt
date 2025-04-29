package com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterOrderHistoryDetailsItem
import com.mykaimeal.planner.databinding.FragmentOrderDetailsScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModelData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDetailsScreenFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsScreenBinding
    private var orderHistoryModel: MutableList<OrderHistoryModelData> = mutableListOf()
    private var adapterOrderHistoryDetailsItem: AdapterOrderHistoryDetailsItem? = null
    private lateinit var viewDetails:OrderHistoryModelData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderDetailsScreenBinding.inflate(inflater, container, false)

        viewDetails = (arguments?.getSerializable("viewDetails") as? OrderHistoryModelData)!!

        Log.d("dfddff","dfdggd"+viewDetails)

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        adapterOrderHistoryDetailsItem = AdapterOrderHistoryDetailsItem(orderHistoryModel, requireActivity())
        binding.rcyOrderHistoryDetails.adapter = adapterOrderHistoryDetailsItem

        setupBackNavigation()

        initialize()


        return binding.root
    }

    private fun initialize() {

        binding.imgBackOrderDetails.setOnClickListener {
            findNavController().navigateUp()
        }

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


}