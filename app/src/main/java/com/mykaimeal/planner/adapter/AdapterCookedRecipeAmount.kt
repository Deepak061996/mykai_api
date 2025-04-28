package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.databinding.AdapterCookedRecipeAmountBinding
import com.mykaimeal.planner.model.DataModel

class AdapterCookedRecipeAmount(private var datalist: List<DataModel>, private var requireActivity: FragmentActivity, private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<AdapterCookedRecipeAmount.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterCookedRecipeAmountBinding = AdapterCookedRecipeAmountBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvAmountType.text=datalist[position].title
        holder.binding.tvAmount.text=datalist[position].price

    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    class ViewHolder(var binding: AdapterCookedRecipeAmountBinding) : RecyclerView.ViewHolder(binding.root){

    }
}