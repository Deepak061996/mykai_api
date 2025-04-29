package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.AdapterOrderHistoryDetailsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.Item

class AdapterOrderHistoryDetailsItem(private var datalist: MutableList<Item>?,
                                     private var requireActivity: FragmentActivity
): RecyclerView.Adapter<AdapterOrderHistoryDetailsItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterOrderHistoryDetailsItemBinding = AdapterOrderHistoryDetailsItemBinding.inflate(inflater, parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = datalist?.get(position) ?: return

        with(holder.binding) {
            data.quantity?.let { tvNumber.text = it.toString() }
            data.name?.let { tvIngredientName.text = it }

            data.base_price?.let { price ->
                val formattedPrice = price.div(100.0).let {
                    if (it % 1.0 == 0.0) it.toInt().toString() else String.format("%.2f", it)
                }
                tvOrderPrice.text = formattedPrice
            }
        }


    }

    override fun getItemCount(): Int {
        return datalist!!.size
    }

    fun update(items: MutableList<Item>) {
        datalist=items
        notifyDataSetChanged()

    }

    class ViewHolder(var binding: AdapterOrderHistoryDetailsItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

}