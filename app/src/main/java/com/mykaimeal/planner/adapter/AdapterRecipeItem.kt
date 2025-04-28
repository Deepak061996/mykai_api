package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.AdapterRecipeItemBinding

class AdapterRecipeItem(var datalist: MutableList<String>, var requireActivity: FragmentActivity): RecyclerView.Adapter<AdapterRecipeItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterRecipeItemBinding =
            AdapterRecipeItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text = "Step "+(position+1)
        holder.binding.tvTitleDescriptions.text = datalist[position]

    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: AdapterRecipeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}