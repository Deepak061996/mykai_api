package com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterOrderHistoryDetailsItem
import com.mykaimeal.planner.databinding.FragmentOrderDetailsScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModelData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDetailsScreenFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsScreenBinding
    private var adapterOrderHistoryDetailsItem: AdapterOrderHistoryDetailsItem? = null
    private lateinit var orderHistoryModelData: OrderHistoryModelData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderDetailsScreenBinding.inflate(inflater, container, false)

        orderHistoryModelData =
            (arguments?.getSerializable("viewDetails") as? OrderHistoryModelData)!!

        Log.d("dfddff", "dfdggd$orderHistoryModelData")

        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        adapterOrderHistoryDetailsItem = AdapterOrderHistoryDetailsItem(
            orderHistoryModelData.order?.final_quote?.items,
            requireActivity()
        )
        binding.rcyOrderHistoryDetails.adapter = adapterOrderHistoryDetailsItem

        setupBackNavigation()
        initialize()
        setDataInUI()

        return binding.root
    }

    private fun setDataInUI() {
        val styledText = HtmlCompat.fromHtml(
            "<font color='#000000'>Delivery</font> <font color='#9E9E9E'>+Tip</font>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.textServices.text = styledText

        if (orderHistoryModelData.order?.order_id != null) {
            binding.tvOrderId.text = "Order #" + orderHistoryModelData.order?.order_id.toString()
        }

        if (orderHistoryModelData.order?.final_quote?.items!!.size > 0) {
            adapterOrderHistoryDetailsItem?.update(orderHistoryModelData.order?.final_quote?.items!!)
        }

        if (orderHistoryModelData.store_logo != null) {
            Glide.with(requireActivity())
                .load(orderHistoryModelData.store_logo)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.layProgess.root.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.layProgess.root.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.imageWelmart)
        }

        val quote = orderHistoryModelData.order?.final_quote?.quote

        binding.textSubTotalPrices.text = "$"+quote?.subtotal.formatCents()
        binding.textServiceFees.text = "$"+quote?.service_fee_cents.formatCents()

        val deliveryFee = orderHistoryModelData.order?.final_quote?.quote?.delivery_fee_cents ?: 0
        val tip = orderHistoryModelData.order?.final_quote?.tip ?: 0
        val totalWithTip = orderHistoryModelData.order?.final_quote?.total_with_tip ?: 0
        val totalCents = deliveryFee + tip

        binding.textDeliveryTipPrice.text = "$"+totalCents.formatCents()

        binding.textTotalAmounts.text = "$"+totalWithTip.formatCents()

        binding.textCardAmounts.text = "$"+totalWithTip.formatCents()

    }

    private fun Int?.formatCents(): String? {
        return this?.div(100.0)?.let {
            if (it % 1.0 == 0.0) it.toInt().toString() else String.format("%.2f", it)
        }
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