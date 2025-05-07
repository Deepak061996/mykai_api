package com.mykaimeal.planner.adapter

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

    private val selectedPositions = mutableSetOf<Int>() // Track selected positions
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
                // Toggle selection state
                val data=favouriteCuisineModelData[position]
                data.selected = !favouriteCuisineModelData[position].selected
                notifyItemChanged(position,data)
                onItemClickedListener.itemClicked(position, dietaryId, "2", "true")
                /*if (favouriteCuisineModelData[position].selected) {
                    dietaryId.add(favouriteCuisineModelData[position].id.toString())
                    selectedPositions.add(position)
                    // Callback to notify selection change
                    onItemClickedListener.itemClicked(position, dietaryId, "2", "false")
                } else {
                    dietaryId.remove(favouriteCuisineModelData[position].id.toString())
                    selectedPositions.remove(position)
                    // Callback to notify selection change
                    onItemClickedListener.itemClicked(position, dietaryId, "2", "true")
                }*/
                notifyItemChanged(position) // Efficiently update only the clicked item
            }

        }


        /*   holder.binding.tvTitleName.text=favouriteCuisineModelData[position].name

           // Inside your adapter's onBindViewHolder method
           holder.binding.apply {
               // Update UI based on the data model's 'selected' property
               if (favouriteCuisineModelData[position].selected) {
                   imageRightTick.visibility = View.VISIBLE
                   relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
                   if (!dietaryId.contains(favouriteCuisineModelData[position].id.toString())) {
                       dietaryId.add(favouriteCuisineModelData[position].id.toString())
                   }
                   onItemClickedListener.itemClicked(position, dietaryId, "-1", "")
               } else {
                   imageRightTick.visibility = View.GONE
                   relMainLayout.setBackgroundResource(R.drawable.gray_box_border_bg)
                   dietaryId.remove(favouriteCuisineModelData[position].id.toString())
               }

               // Handle item click
               relMainLayout.setOnClickListener {
                  *//* if (position == 0) {
                    // Handle "None" (first item) case
                    if (favouriteCuisineModelData[position].selected) {
                        // Deselect "None"
                        favouriteCuisineModelData[position].selected = false
                        selectedPositions.remove(0)
                        dietaryId.clear()
                        onItemClickedListener.itemClicked(position, dietaryId, "2", "false")
                    } else {
                        // Select "None" and clear all other selections
                        selectedPositions.clear()
                        favouriteCuisineModelData.forEach { it.selected = false }
                        dietaryId.clear()

                        favouriteCuisineModelData[position].selected = true
                        selectedPositions.add(0)
                        dietaryId.add(favouriteCuisineModelData[position].id.toString())
                        onItemClickedListener.itemClicked(position, dietaryId, "2", "true")
                    }
                } else {*//*
                        favouriteCuisineModelData[0].selected = false
                        dietaryId.clear()

                    // Toggle the current item's selection state
                    if (favouriteCuisineModelData[position].selected) {
                        // Deselect the item
                        favouriteCuisineModelData[position].selected = false
                        selectedPositions.remove(position)
                        dietaryId.remove(favouriteCuisineModelData[position].id.toString())
                        onItemClickedListener.itemClicked(position, dietaryId, "2", "false")
                    } else {
                        // Select the item
                        favouriteCuisineModelData[position].selected = true
                        selectedPositions.add(position)
                        dietaryId.add(favouriteCuisineModelData[position].id.toString())
                        onItemClickedListener.itemClicked(position, dietaryId, "2", "true")
                    }
                *//*}*//*
                notifyDataSetChanged() // Refresh the UI
            }
        }*/
    }

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