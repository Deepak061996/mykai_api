package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.AdapterRecipeItemBinding
import com.mykaimeal.planner.databinding.RowMonthBinding

class MonthYearsCardAdapter(var datalist: MutableList<String>, var requireActivity: Context,private val onDaySelected: (String) -> Unit): RecyclerView.Adapter<MonthYearsCardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: RowMonthBinding = RowMonthBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data=datalist[position]
        holder.binding.tvTitle.text = data

        holder.binding.root.setOnClickListener {
            onDaySelected(data)
        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: RowMonthBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}