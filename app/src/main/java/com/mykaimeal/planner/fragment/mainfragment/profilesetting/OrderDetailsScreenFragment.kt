package com.mykaimeal.planner.fragment.mainfragment.profilesetting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterOrderHistoryDetailsItem
import com.mykaimeal.planner.databinding.FragmentOrderDetailsScreenBinding
import com.mykaimeal.planner.model.DataModel

class OrderDetailsScreenFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsScreenBinding
    private var dataList3: MutableList<DataModel> = mutableListOf()
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

        setupBackNavigation()

        initialize()

        orderHistoryModel()

        return binding.root
    }

    private fun initialize() {

        binding.imgBackOrderDetails.setOnClickListener{
            findNavController().navigateUp()
        }
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
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
        data1.price="$48.47"
        data1.distance="Delivered - 926 Gainsway Street, NY 12603"
        data1.isOpen = false
        data1.quantity="6 items"
        data1.image = R.drawable.ic_welmart_super_market

        data2.title = "11 jan"
        data2.price="$48.47"
        data2.distance="Delivered to Home\n" +
                "17 Sugar Lane South Ozone Park, NY 11420"
        data2.isOpen = false
        data2.quantity="6 items"
        data2.image = R.drawable.ic_kroger_super_market

        data3.title = "11 jan"
        data3.price="$48.47"
        data3.distance="Delivered - 926 Gainsway Street, NY 12603"
        data3.isOpen = false
        data3.quantity="6 items"
        data3.image = R.drawable.ic_target_super_market

        data4.title = "11 jan"
        data4.price="$48.47"
        data4.distance="Delivered - 926 Gainsway Street, NY 12603"
        data4.isOpen = false
        data4.quantity="6 items"
        data4.image = R.drawable.ic_welmart_super_market

        data5.title = "11 jan"
        data5.price="$48.47"
        data5.distance="Delivered - 926 Gainsway Street, NY 12603"
        data5.isOpen = false
        data5.quantity="6 items"
        data5.image = R.drawable.ic_kroger_super_market

        dataList3.add(data1)
        dataList3.add(data2)
        dataList3.add(data3)
        dataList3.add(data4)
        dataList3.add(data5)

        adapterOrderHistoryDetailsItem = AdapterOrderHistoryDetailsItem(dataList3, requireActivity())
        binding.rcyOrderHistoryDetails.adapter = adapterOrderHistoryDetailsItem
    }


}