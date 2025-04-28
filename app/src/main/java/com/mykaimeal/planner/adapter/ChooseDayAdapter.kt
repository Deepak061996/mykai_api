package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.AdapterChooseDayBinding
import com.mykaimeal.planner.model.DataModel


class ChooseDayAdapter(private var datalist: MutableList<DataModel>, private var requireActivity: FragmentActivity): RecyclerView.Adapter<ChooseDayAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterChooseDayBinding = AdapterChooseDayBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text=datalist[position].title

        if (datalist[position].isOpen){
            holder.binding.imageTick.setImageResource(R.drawable.check_gray_box_icon)
            holder.binding.relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
        }else{
            holder.binding.imageTick.setImageResource(R.drawable.uncheck_gray_box_icon)
            holder.binding.relMainLayout.setBackgroundResource(R.drawable.gray_box_border_bg)
        }

        holder.binding.relMainLayout.setOnClickListener{
            if (datalist[position].date >= BaseApplication.currentDateFormat().toString()) {
                val data = datalist[position]
                data.isOpen = !datalist[position].isOpen
                datalist[position] = data
                notifyDataSetChanged()
            }

        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: AdapterChooseDayBinding) : RecyclerView.ViewHolder(binding.root){

    }
}