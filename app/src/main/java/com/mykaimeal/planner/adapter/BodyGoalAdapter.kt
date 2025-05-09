package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBodyGoalsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData


class BodyGoalAdapter(private var datalist: List<BodyGoalModelData>, private var requireActivity: FragmentActivity, private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<BodyGoalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBodyGoalsBinding = AdapterBodyGoalsBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data=datalist[position]

        data.let {
            if (it.name.equals("Add other")){
                val typeface = ResourcesCompat.getFont(requireActivity, R.font.poppins_semibold)
                holder.binding.tvTitleName.typeface = typeface
            }else{
                val typeface = ResourcesCompat.getFont(requireActivity, R.font.poppins)
                holder.binding.tvTitleName.typeface = typeface
            }
            holder.binding.tvTitleName.text = it.name
        }

        // Bind UI based on whether the item is selected or matches the last checked position
        if (datalist[position].selected) {
            holder.binding.imageRightTick.visibility = View.VISIBLE
            holder.binding.relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
            onItemClickListener.itemClick(data.id,"-1",position.toString())
        } else {
            holder.binding.imageRightTick.visibility = View.GONE
            holder.binding.relMainLayout.setBackgroundResource(R.drawable.gray_box_border_bg)
        }

        // Handle click event for the item
        holder.binding.relMainLayout.setOnClickListener {
            // Check if the clicked item is already selected
            if (data.selected) {
                // Deselect all items
                datalist.forEach { it.selected = false }
                onItemClickListener.itemClick(data.id,"false" , position.toString())
            } else {
                // Deselect all items
                datalist.forEach { it.selected = false }
                onItemClickListener.itemClick(data.id, "true",position.toString())
                // Select the new item
                datalist[position].selected = true
            }

            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    class ViewHolder(var binding: AdapterBodyGoalsBinding) : RecyclerView.ViewHolder(binding.root){

    }

}