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

    @SuppressLint("NotifyDataSetChanged")
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
            } else {
                dietaryId.remove(dislikeIngredientsData[position].id.toString())
            }

            // Handle item click logic
            relMainLayout.setOnClickListener {
                if (dislikeIngredientsData[position].name.equals("None",true)){
                    // In your click listener or wherever you're handling item selection
                    dislikeIngredientsData.forEachIndexed { index, item ->
                        if (index==0){
                            item.selected = !item.selected
                        }else{
                            item.selected = false
                        }
                    }
                }else{
                    // In your click listener or wherever you're handling item selection
                    dislikeIngredientsData.forEachIndexed { index, item ->
                        if (index==0){
                            item.selected = false
                        }
                    }
                    val data=dislikeIngredientsData[position]
                    data.selected = !dislikeIngredientsData[position].selected
                    notifyItemChanged(position,data)
                }
                notifyDataSetChanged()
                onItemClickedListener.itemClicked(position, dietaryId, "2", "false")

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