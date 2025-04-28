package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.databinding.AdapterStatisticsWeekYearItemBinding
import com.mykaimeal.planner.model.DataModel

class AdapterStatisticsWeekItem(private var datalist: List<DataModel>, private var requireActivity: FragmentActivity, private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<AdapterStatisticsWeekItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterStatisticsWeekYearItemBinding = AdapterStatisticsWeekYearItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvBreakfast.text=datalist[position].title
        holder.binding.relBreakfast.setBackgroundResource(datalist[position].image)

        if (datalist[position].type=="RecipeCookedBreakFast"){
            holder.binding.imgCheck.visibility=View.GONE
        }else{
            holder.binding.imgCheck.visibility=View.VISIBLE
        }

        holder.binding.tvAddToPlan.setOnClickListener{
            onItemClickListener.itemClick(position,"1",datalist[position].type)
        }

        holder.binding.imgBasket.setOnClickListener{
            onItemClickListener.itemClick(position,"2",datalist[position].type)

        }

    }


    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: AdapterStatisticsWeekYearItemBinding) : RecyclerView.ViewHolder(binding.root){

    }
}