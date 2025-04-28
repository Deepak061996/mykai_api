package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.databinding.AdapterBodyGoalsBinding
import com.mykaimeal.planner.databinding.AdapterOrderHistoryItemBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.model.AllergensIngredientModelData
import com.mykaimeal.planner.model.DataModel

class AdapterOrderHistoryItem(private var datalist: List<DataModel>,
                              private var requireActivity: FragmentActivity,
                              private var onItemClickedListener: OnItemClickedListener): RecyclerView.Adapter<AdapterOrderHistoryItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterOrderHistoryItemBinding = AdapterOrderHistoryItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= datalist[position]

        if (data.isOpen){
            holder.binding.tvTrackOrder.visibility=View.VISIBLE
            holder.binding.tvViewOrder.visibility=View.GONE
        }else{
            holder.binding.tvTrackOrder.visibility=View.GONE
            holder.binding.tvViewOrder.visibility=View.VISIBLE
        }

        holder.binding.tvDate.text=data.title+"• "+data.price+"• "+data.quantity
        holder.binding.tvDelivery.text=data.distance
        holder.binding.imageData.setImageResource(data.image)

        holder.binding.tvViewOrder.setOnClickListener{
            onItemClickedListener.itemClicked(position,null,"","View")
        }

        holder.binding.tvTrackOrder.setOnClickListener{
            onItemClickedListener.itemClicked(position,null,"","Track")
        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    class ViewHolder(var binding: AdapterOrderHistoryItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

}