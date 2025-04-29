package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.AdapterOrderHistoryDetailsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModelData

class AdapterOrderHistoryDetailsItem(private var datalist: MutableList<OrderHistoryModelData>,
                                     private var requireActivity: FragmentActivity
): RecyclerView.Adapter<AdapterOrderHistoryDetailsItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterOrderHistoryDetailsItemBinding = AdapterOrderHistoryDetailsItemBinding.inflate(inflater, parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= datalist[position]

    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    class ViewHolder(var binding: AdapterOrderHistoryDetailsItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

}