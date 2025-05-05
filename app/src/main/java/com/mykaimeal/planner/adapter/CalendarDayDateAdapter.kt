package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.CalendarItemDayBinding
import com.mykaimeal.planner.model.DateModel


class CalendarDayDateAdapter(var days: MutableList<DateModel>,
                             private val onDaySelected: (Int) -> Unit) : RecyclerView.Adapter<CalendarDayDateAdapter.ViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: CalendarItemDayBinding = CalendarItemDayBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val day = days[position]

        holder.binding.tvDayName.text = BaseApplication.getFirstLetterOfDay(day.day)
        holder.binding.tvDayDate.text = BaseApplication.formatOnlyDate(day.date)

        if (day.status){
            holder.binding.llMainLayouts.setBackgroundResource(R.drawable.calendar_select_green_bg)
            holder.binding.tvDayName.setTextColor(Color.parseColor("#ffffff"))
            holder.binding.tvDayDate.setTextColor(Color.parseColor("#ffffff"))
        }else{
            holder.binding.llMainLayouts.setBackgroundResource(R.drawable.calendar_unselect_bg)
            holder.binding.tvDayName.setTextColor(Color.parseColor("#999999"))
            holder.binding.tvDayDate.setTextColor(Color.parseColor("#3C4541"))
        }


        holder.itemView.setOnClickListener {
            // Disable past dates
            Log.d("list date ","*****"+day.date)
            Log.d("current date ","*****"+BaseApplication.currentDateFormat().toString())
            if (day.date >= BaseApplication.currentDateFormat().toString()) {
                // Disable past dates
                notifyDataSetChanged()
                onDaySelected(position)
            }
        }



    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(daysList: MutableList<DateModel>){
        days=daysList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return days.size
    }


    class ViewHolder(var binding: CalendarItemDayBinding) : RecyclerView.ViewHolder(binding.root) {

    }

}