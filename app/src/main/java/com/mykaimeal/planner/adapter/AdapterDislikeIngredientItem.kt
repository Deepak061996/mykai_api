package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBodyGoalsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModelData

class AdapterDislikeIngredientItem(private var dislikeIngredientsData: List<DislikedIngredientsModelData>,
                                   private var requireActivity: FragmentActivity,
                                   private var onItemClickedListener: OnItemClickedListener
):
    RecyclerView.Adapter<AdapterDislikeIngredientItem.ViewHolder>() {

    private val selectedPositions = mutableSetOf<Int>() // Track selected positions
    private val dietaryId = mutableListOf<String>() // Track selected dietary IDs
    private var isExpanded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBodyGoalsBinding = AdapterBodyGoalsBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text=dislikeIngredientsData[position].name

        holder.binding.apply {
            // Update UI based on the item's 'selected' property
            imageRightTick.visibility = if (dislikeIngredientsData[position].selected) View.VISIBLE else View.GONE
            relMainLayout.setBackgroundResource(
                if (dislikeIngredientsData[position].selected) R.drawable.orange_box_bg else R.drawable.gray_box_border_bg
            )

            // Update dietaryId based on the 'selected' state
            if (dislikeIngredientsData[position].selected) {
                if (!dietaryId.contains(dislikeIngredientsData[position].id.toString())) {
                    dietaryId.add(dislikeIngredientsData[position].id.toString())
                }
                onItemClickedListener.itemClicked(position, dietaryId, "-1", "")
            } else {
                dietaryId.remove(dislikeIngredientsData[position].id.toString())
            }




            // Handle item click logic
            relMainLayout.setOnClickListener {
                when (position) {
                    0 -> { // Handle "Select All" or "None"
                        if (dislikeIngredientsData[position].selected) {
                            // Deselect "None" or "Select All"
                            dislikeIngredientsData[position].selected = false
                            selectedPositions.clear()
                            dietaryId.clear()
                            onItemClickedListener.itemClicked(position, dietaryId, "2", "false")
                        } else {
                            // Select "None" or "Select All" and clear all other selections
                            selectedPositions.clear()
                            dislikeIngredientsData.forEach { it.selected = false }
                            dietaryId.clear()

                            dislikeIngredientsData[position].selected = true
                            selectedPositions.add(0)
                            dietaryId.add(dislikeIngredientsData[position].id.toString())
                            onItemClickedListener.itemClicked(position, dietaryId, "2", "true")
                        }
                    } else -> { // Handle individual item selection
                        // Deselect "Select All" if any other item is clicked
                      /*  if (selectedPositions.contains(0)) {
                            selectedPositions.remove(0)*/
                            dislikeIngredientsData[0].selected = false
                            dietaryId.clear()
                      /*  }*/

                        // Toggle the current item's selection state
                        dislikeIngredientsData[position].selected = !dislikeIngredientsData[position].selected
                        if (dislikeIngredientsData[position].selected) {
                            selectedPositions.add(position)
                            dietaryId.add(dislikeIngredientsData[position].id.toString())
                            onItemClickedListener.itemClicked(position, dietaryId, "2", "true")
                        } else {
                            selectedPositions.remove(position)
                            dietaryId.remove(dislikeIngredientsData[position].id.toString())
                            onItemClickedListener.itemClicked(position, dietaryId, "2", "false")
                        }
                    }
                }

                notifyDataSetChanged() // Refresh the UI
            }




        }
    }

    override fun getItemCount(): Int {
        return dislikeIngredientsData.size
//        return if (isExpanded) dislikeIngredientsData.size else Math.min(3, dislikeIngredientsData.size)
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        notifyDataSetChanged()
    }

    fun filterList(filteredList: MutableList<DislikedIngredientsModelData>) {
        this.dislikeIngredientsData = filteredList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterBodyGoalsBinding) : RecyclerView.ViewHolder(binding.root){
    }

}