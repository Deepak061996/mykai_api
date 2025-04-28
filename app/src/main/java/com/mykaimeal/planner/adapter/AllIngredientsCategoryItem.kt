package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterAllIngredientsCategoryBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.CategoryModel

class AllIngredientsCategoryItem(
    private var datalist: MutableList<CategoryModel>,
    private var requireActivity: FragmentActivity,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AllIngredientsCategoryItem.ViewHolder>() {

    private var selected: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterAllIngredientsCategoryBinding =
            AdapterAllIngredientsCategoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= datalist[position]


        holder.binding.textFruits.text = data.name

        if (data.status) {
            holder.binding.textFruits.setBackgroundResource(R.drawable.select_bg)
            holder.binding.textFruits.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            holder.binding.textFruits.setBackgroundResource(R.drawable.unselect_bg)
            holder.binding.textFruits.setTextColor(Color.parseColor("#000000"))
        }


        holder.binding.root.setOnClickListener {
            // Loop through list and update statuses
            datalist.forEachIndexed { index, item ->
                item.status = index == position
            }
            onItemClickListener.itemClick(position,data.name,"filter")
            notifyDataSetChanged()
        }


    }

    override fun getItemCount(): Int {
         return datalist.size
    }


    fun filterList(filteredList: MutableList<CategoryModel>) {
        this.datalist = filteredList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterAllIngredientsCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}