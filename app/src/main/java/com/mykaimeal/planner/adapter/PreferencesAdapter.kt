package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.model.DataPreferencesModel
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.databinding.AdapterPreferencesItemBinding

class PreferencesAdapter(var datalist: List<DataPreferencesModel>, private var requireActivity: FragmentActivity, private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<PreferencesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterPreferencesItemBinding =
            AdapterPreferencesItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvPreferencesName.text = datalist[position].title

        holder.binding.relMainLayout.setOnClickListener {
            onItemClickListener.itemClick(position,datalist[position].type,datalist[position].title)
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: AdapterPreferencesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}
