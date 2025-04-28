package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterSearchFilterItemBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.Diet

class AdapterFilterDietItem(
    private var datalist: MutableList<Diet>,
    private var requireActivity: FragmentActivity,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdapterFilterDietItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterSearchFilterItemBinding =
            AdapterSearchFilterItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list=datalist[position]
        holder.binding.tvItem.text = list.name
        if (list.selected==true){
            holder.binding.tvItem.setTextColor(android.graphics.Color.parseColor("#06C169"))
            if (list.name.equals("More")){
                holder.binding.relMainLayouts.background=null
            }else{
                holder.binding.relMainLayouts.setBackgroundResource(R.drawable.month_year_bg)
            }
        }else{
            holder.binding.tvItem.setTextColor(android.graphics.Color.parseColor("#000000"))
            holder.binding.relMainLayouts.setBackgroundResource(R.drawable.month_year_unselected_bg)
        }
        holder.binding.relMainLayouts.setOnClickListener {
            if (list.name.equals("More")){
                onItemClickListener.itemClick(position, datalist[position].name,"Diet")
            }else{
                val data=datalist[position]
                data.selected = list.selected != true
                datalist[position] = data
                notifyDataSetChanged()
                onItemClickListener.itemClick(position, datalist[position].name,"check")
            }
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    fun updateList(newList: MutableList<Diet>) {
        datalist=newList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterSearchFilterItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}