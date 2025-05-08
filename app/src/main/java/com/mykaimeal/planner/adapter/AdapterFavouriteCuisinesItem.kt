package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBodyGoalsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines.model.FavouriteCuisinesModelData

class AdapterFavouriteCuisinesItem(private var favouriteCuisineModelData: List<FavouriteCuisinesModelData>,
                                   private var requireActivity: FragmentActivity,
                                   private var onItemClickedListener: OnItemClickedListener):
    RecyclerView.Adapter<AdapterFavouriteCuisinesItem.ViewHolder>() {
    private var isNoneSelected :Boolean = true
    private val dietaryId = mutableListOf<String>() // Track selected dietary IDs
    private var isExpanded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBodyGoalsBinding = AdapterBodyGoalsBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text = favouriteCuisineModelData[position].name

        holder.binding.apply {
            // Update UI based on the 'selected' property
            if (favouriteCuisineModelData[position].selected) {
                imageRightTick.visibility = View.VISIBLE
                relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
                if (!dietaryId.contains(favouriteCuisineModelData[position].id.toString())) {
                    dietaryId.add(favouriteCuisineModelData[position].id.toString())
                }
//                onItemClickedListener.itemClicked(position, dietaryId, "-1", "")
            } else {
                imageRightTick.visibility = View.GONE
                relMainLayout.setBackgroundResource(R.drawable.gray_box_border_bg)
                dietaryId.remove(favouriteCuisineModelData[position].id.toString())
            }

            // Handle item click
            relMainLayout.setOnClickListener {

                    val data=favouriteCuisineModelData[position]
                    data.selected = !favouriteCuisineModelData[position].selected
                    notifyItemChanged(position,data)
                    onItemClickedListener.itemClicked(position, dietaryId, "2", "true")


            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        isNoneSelected = true
        return if (isExpanded) favouriteCuisineModelData.size else 5.coerceAtMost(
            favouriteCuisineModelData.size
        )
    }

    class ViewHolder(var binding: AdapterBodyGoalsBinding) : RecyclerView.ViewHolder(binding.root){
    }

}