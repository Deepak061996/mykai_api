package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterSearchFilterItemBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.CookTime

class AdapterFilterCookTimeItem(
    private var datalist: MutableList<CookTime>,
    private var requireActivity: FragmentActivity,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdapterFilterCookTimeItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterSearchFilterItemBinding =
            AdapterSearchFilterItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item=datalist[position]

        holder.binding.tvItem.text = item.name

        if (item.selected==true){
            holder.binding.tvItem.setTextColor(android.graphics.Color.parseColor("#06C169"))
            if (item.name.equals("More")){
                holder.binding.relMainLayouts.background=null
            }else{
                holder.binding.relMainLayouts.setBackgroundResource(R.drawable.month_year_bg)
            }
        }else{
            holder.binding.tvItem.setTextColor(android.graphics.Color.parseColor("#000000"))
            holder.binding.relMainLayouts.setBackgroundResource(R.drawable.month_year_unselected_bg)
        }

        holder.binding.relMainLayouts.setOnClickListener {
            if (item.name.equals("More")){
                onItemClickListener.itemClick(position, datalist[position].name,"CookTime")
            }else{
                val data=datalist[position]
                data.selected = item.selected != true
                datalist[position] = data
                notifyDataSetChanged()
                onItemClickListener.itemClick(position, datalist[position].name,"check")
            }
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: MutableList<CookTime>) {
        datalist=newList
        notifyDataSetChanged()
    }


    class ViewHolder(var binding: AdapterSearchFilterItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}