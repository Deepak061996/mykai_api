package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBodyGoalsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.model.DietaryRestrictionsModelData


class AdapterCookingSchedule(private var dietaryRestrictionsModelData: List<DietaryRestrictionsModelData>, private var requireActivity: FragmentActivity, private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<AdapterCookingSchedule.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBodyGoalsBinding = AdapterBodyGoalsBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text=dietaryRestrictionsModelData[position].name

        if (dietaryRestrictionsModelData[position].selected){
            holder.binding.imageRightTick.visibility= View.VISIBLE
            holder.binding.relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
            onItemClickListener.itemClick(dietaryRestrictionsModelData[position].id,"2","")
        }else{
            holder.binding.imageRightTick.visibility= View.GONE
            holder.binding.relMainLayout.setBackgroundResource(R.drawable.gray_box_border_bg)
        }

        holder.binding.relMainLayout.setOnClickListener{
            if (dietaryRestrictionsModelData[position].selected === false) {
                dietaryRestrictionsModelData[position].selected=true
            } else {
                dietaryRestrictionsModelData[position].selected=false
            }

            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return dietaryRestrictionsModelData.size
    }

    class ViewHolder(var binding: AdapterBodyGoalsBinding) : RecyclerView.ViewHolder(binding.root){
    }

}