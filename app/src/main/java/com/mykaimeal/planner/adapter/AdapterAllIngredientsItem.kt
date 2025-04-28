package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterAllIngredientsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.IngredientList
import com.mykaimeal.planner.model.DataModel

class AdapterAllIngredientsItem(
    private var datalist: MutableList<IngredientList>,
    private var requireActivity: FragmentActivity,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdapterAllIngredientsItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterAllIngredientsItemBinding =
            AdapterAllIngredientsItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data=datalist[position]


        data.name?.let {
            val foodName = it
            val result = foodName.mapIndexed { index, c ->
                if (index == 0 || c.isUpperCase()) c.uppercaseChar() else c
            }.joinToString("")
            holder.binding.textTittles.text = result
        }

        if (data.status==true){
            holder.binding.imgTick.visibility=View.VISIBLE
        }else{
            holder.binding.imgTick.visibility=View.GONE
        }

        holder.binding.root.setOnClickListener {
            val data=datalist[position]
            data.status = data.status != true
            datalist[position] = data
            notifyDataSetChanged()
            onItemClickListener.itemClick(position,data.name,"selected")
        }




    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    fun filterList(filteredList: MutableList<IngredientList>) {
        this.datalist = filteredList
        notifyDataSetChanged()
    }


    class ViewHolder(var binding: AdapterAllIngredientsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}