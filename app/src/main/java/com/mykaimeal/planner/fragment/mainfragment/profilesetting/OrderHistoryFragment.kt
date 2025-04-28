package com.mykaimeal.planner.fragment.mainfragment.profilesetting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterOrderHistoryItem
import com.mykaimeal.planner.databinding.FragmentOrderHistoryBinding
import com.mykaimeal.planner.model.DataModel

class OrderHistoryFragment : Fragment(), OnItemClickedListener {

    private lateinit var binding: FragmentOrderHistoryBinding
    private var dataList3: MutableList<DataModel> = mutableListOf()
    private var adapterOrderHistoryItem: AdapterOrderHistoryItem? = null
    private var screen: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)


        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        screen = arguments?.getString("screen", "") ?: ""

        setupBackNavigation()

        initialize()

        orderHistoryModel()

        return binding.root
    }

    private fun setupBackNavigation() {

        if (screen.equals("yes",true)) {
            binding.relNoOrders.visibility = View.GONE
            binding.rcyOrderHistory.visibility = View.VISIBLE
        } else {
            binding.relNoOrders.visibility = View.VISIBLE
            binding.rcyOrderHistory.visibility = View.GONE
        }

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

        binding.rlStartOrder.setOnClickListener {
            binding.relNoOrders.visibility = View.GONE
            binding.rcyOrderHistory.visibility = View.VISIBLE
        }
    }

    private fun moveToScreen(){
        if (screen.equals("yes",true)) {
            findNavController().navigate(R.id.homeFragment)
        } else {
            findNavController().navigateUp()
        }
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
    data1.isOpen = true
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
    data3.isOpen = true
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

    adapterOrderHistoryItem = AdapterOrderHistoryItem(dataList3, requireActivity(), this)
    binding.rcyOrderHistory.adapter = adapterOrderHistoryItem
}

override fun itemClicked(
    position: Int?,
    list: MutableList<String>?,
    status: String?,
    type: String?
) {

    if (type == "View") {
        findNavController().navigate(R.id.orderDetailsScreenFragment)
    } else {
        val bundle = Bundle().apply {
            putString("tracking", "https://tracking.mealme.ai/tracking?id=-OMRihw4FhMILGO035aK")
        }
        findNavController().navigate(R.id.trackOrderScreenFragment, bundle)

    }

}

}