package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBodyGoalsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.model.AllergensIngredientModelData

class AdapterAllergensIngItem(
    private var allergensIngredientsData: List<AllergensIngredientModelData>,
    private var requireActivity: FragmentActivity,
    private var onItemClickedListener: OnItemClickedListener
) :
    RecyclerView.Adapter<AdapterAllergensIngItem.ViewHolder>() {

    private val selectedPositions = mutableSetOf<Int>() // Track selected positions
    private val dietaryId = mutableListOf<String>() // Track selected dietary IDs
    private var isExpanded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBodyGoalsBinding =
            AdapterBodyGoalsBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text = allergensIngredientsData[position].name

        holder.binding.apply {
            // Update UI based on the item's 'selected' property
            imageRightTick.visibility =
                if (allergensIngredientsData[position].selected) View.VISIBLE else View.GONE
            relMainLayout.setBackgroundResource(
                if (allergensIngredientsData[position].selected) R.drawable.orange_box_bg else R.drawable.gray_box_border_bg
            )

            // Update dietaryId based on the 'selected' state
            if (allergensIngredientsData[position].selected) {
                if (!dietaryId.contains(allergensIngredientsData[position].id.toString())) {
                    dietaryId.add(allergensIngredientsData[position].id.toString())
                }
                onItemClickedListener.itemClicked(position, dietaryId, "-1", "")
            } else {
                dietaryId.remove(allergensIngredientsData[position].id.toString())
            }

            // Handle item click logic
            relMainLayout.setOnClickListener {
                when (position) {
                    0 -> { // Handle "Select All" or "None"
                        if (allergensIngredientsData[position].selected) {
                            // Deselect "None" or "Select All"
                            allergensIngredientsData[position].selected = false
                            selectedPositions.clear()
                            dietaryId.clear()
                            onItemClickedListener.itemClicked(position, dietaryId, "2", "false")
                        } else {
                            // Select "None" or "Select All" and clear all other selections
                            selectedPositions.clear()
                            allergensIngredientsData.forEach { it.selected = false }
                            dietaryId.clear()

                            allergensIngredientsData[position].selected = true
                            selectedPositions.add(0)
                            dietaryId.add(allergensIngredientsData[position].id.toString())
                            onItemClickedListener.itemClicked(position, dietaryId, "2", "true")
                        }
                    }

                    else -> { // Handle individual item selection
                        // Deselect "Select All" if any other item is clicked
                        /*   if (selectedPositions.contains(0)) {
                               selectedPositions.remove(0)*/
                        allergensIngredientsData[0].selected = false
                        dietaryId.clear()
                        /*    }*/

                        // Toggle the current item's selection state
                        allergensIngredientsData[position].selected =
                            !allergensIngredientsData[position].selected
                        if (allergensIngredientsData[position].selected) {
                            selectedPositions.add(position)
                            dietaryId.add(allergensIngredientsData[position].id.toString())
                            onItemClickedListener.itemClicked(position, dietaryId, "2", "true")
                        } else {
                            selectedPositions.remove(position)
                            dietaryId.remove(allergensIngredientsData[position].id.toString())
                            onItemClickedListener.itemClicked(position, dietaryId, "2", "false")
                        }
                    }
                }

                notifyDataSetChanged() // Refresh the UI
            }
        }
    }

    override fun getItemCount(): Int {
        return allergensIngredientsData.size
//        return if (isExpanded) allergensIngredientsData.size else Math.min(3, allergensIngredientsData.size)
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        notifyDataSetChanged()
    }

    fun filterList(filteredList: MutableList<AllergensIngredientModelData>) {
        this.allergensIngredientsData = filteredList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterBodyGoalsBinding) : RecyclerView.ViewHolder(binding.root) {
    }

}