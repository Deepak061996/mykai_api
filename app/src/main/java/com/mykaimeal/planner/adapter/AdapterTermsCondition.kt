package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.AdapterTermsConditionBinding
import com.mykaimeal.planner.model.DataModel

class AdapterTermsCondition(private var datalist: List<DataModel>, private var requireActivity: FragmentActivity): RecyclerView.Adapter<AdapterTermsCondition.ViewHolder>() {

    private var lastCheckedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterTermsConditionBinding = AdapterTermsConditionBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitle.text=datalist[position].title
        holder.binding.tvHeading.text=datalist[position].description

    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: AdapterTermsConditionBinding) : RecyclerView.ViewHolder(binding.root){

    }


}