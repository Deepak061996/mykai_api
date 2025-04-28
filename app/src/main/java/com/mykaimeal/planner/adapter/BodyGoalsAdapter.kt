package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBodyGoalsBinding
import com.mykaimeal.planner.model.DataModel


class BodyGoalsAdapter(private var datalist: MutableList<DataModel>, private var requireActivity: FragmentActivity, private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<BodyGoalsAdapter.ViewHolder>() {

    private var lastCheckedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBodyGoalsBinding = AdapterBodyGoalsBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text=datalist[position].title

        if (lastCheckedPosition == position) {
            holder.binding.imageRightTick.visibility= View.VISIBLE
            holder.binding.relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
            onItemClickListener.itemClick(position,"2","")
        } else {
            holder.binding.imageRightTick.visibility= View.GONE
            holder.binding.relMainLayout.setBackgroundResource(R.drawable.gray_box_border_bg)
        }

        holder.binding.relMainLayout.setOnClickListener{
                val copyOfLastCheckedPosition = lastCheckedPosition
                lastCheckedPosition = holder.adapterPosition
                notifyItemChanged(copyOfLastCheckedPosition)
                notifyItemChanged(lastCheckedPosition)

        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: AdapterBodyGoalsBinding) : RecyclerView.ViewHolder(binding.root){

    }


}