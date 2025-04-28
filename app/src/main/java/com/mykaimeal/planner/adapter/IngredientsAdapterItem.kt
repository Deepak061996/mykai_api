package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.databinding.AdapterSearchItemBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModelData

class IngredientsAdapterItem(private var recipes: MutableList<DislikedIngredientsModelData>,
    private var requireActivity: FragmentActivity,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<IngredientsAdapterItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterSearchItemBinding =
            AdapterSearchItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (recipes[position].name!=null){
            holder.binding.tvRecipeName.text=recipes[position].name.toString()
        }

        holder.binding.tvRecipeName.setOnClickListener{
            onItemClickListener.itemClick(position,recipes[position].name.toString(),"IngredientsItem")
        }
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    class ViewHolder(var binding: AdapterSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}