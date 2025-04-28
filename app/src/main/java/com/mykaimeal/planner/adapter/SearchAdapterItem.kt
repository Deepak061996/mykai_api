package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.databinding.AdapterSearchItemBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.Recipe

class SearchAdapterItem(
    private var recipes: List<Recipe>,
    private var requireActivity: FragmentActivity,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SearchAdapterItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterSearchItemBinding =
            AdapterSearchItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (recipes[position].recipe?.label!=null){
            holder.binding.tvRecipeName.text=recipes[position].recipe?.label.toString()
        }

        holder.binding.tvRecipeName.setOnClickListener{
            var mealType = ""
            if (recipes[position].recipe?.mealType != null && recipes[position].recipe?.mealType?.isNotEmpty() == true) {
                mealType = recipes[position].recipe?.mealType!![0]
                mealType = if (mealType.contains("/")) {
                    mealType.split("/")[0] // Get the first part before the slash
                } else {
                    mealType // Return as is if no slash is present
                }
            }

            onItemClickListener.itemClick(position,recipes[position].recipe?.uri.toString(),mealType)
        }
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    class ViewHolder(var binding: AdapterSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}