package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemMealTypeListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.MealTypeBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData

class AdapterMealType(var datalist: MutableList<MealRoutineModelData>, var requireActivity: FragmentActivity,var OnItemMealTypeListener: OnItemMealTypeListener): RecyclerView.Adapter<AdapterMealType.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: MealTypeBinding = MealTypeBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data=datalist[position]

        if (data.selected){
            holder.binding.relRecipeSearch.setBackgroundResource(R.drawable.circular_white_outline_gray_bg_active)
            holder.binding.tvRecipeSearch.setCompoundDrawablesWithIntrinsicBounds(0,  0,R.drawable.orange_tick_icon, 0)
        }else{
            holder.binding.relRecipeSearch.setBackgroundResource(R.drawable.circular_white_outline_gray_bg)
            holder.binding.tvRecipeSearch.setCompoundDrawablesWithIntrinsicBounds(0,  0,0, 0)
        }

        if (data.name!=null){
            holder.binding.tvRecipeSearch.text = data.name
        }

        holder.itemView.setOnClickListener {
            OnItemMealTypeListener.itemMealTypeSelect(position,"","")
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(data: MutableList<MealRoutineModelData>){
        datalist=data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    class ViewHolder(var binding: MealTypeBinding) : RecyclerView.ViewHolder(binding.root){

    }
}