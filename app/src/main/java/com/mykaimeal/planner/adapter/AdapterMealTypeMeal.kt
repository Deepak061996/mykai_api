package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemMealTypeListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.MealType1Binding
import com.mykaimeal.planner.databinding.MealTypeBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData

class AdapterMealTypeMeal(var datalist: MutableList<MealRoutineModelData>, var requireActivity: Context, var OnItemMealTypeListener: OnItemMealTypeListener): RecyclerView.Adapter<AdapterMealTypeMeal.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: MealType1Binding = MealType1Binding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data=datalist[position]


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

    class ViewHolder(var binding: MealType1Binding) : RecyclerView.ViewHolder(binding.root){

    }
}