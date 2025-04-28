package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.AdapterCookwareItemBinding
import com.mykaimeal.planner.model.DataModel

class CookWareAdapter(private var datalist: List<DataModel>, private var requireActivity: FragmentActivity): RecyclerView.Adapter<CookWareAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterCookwareItemBinding =
            AdapterCookwareItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text = datalist[position].title
        holder.binding.imgIngRecipe.setImageResource(datalist[position].image)
    }


    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: AdapterCookwareItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}