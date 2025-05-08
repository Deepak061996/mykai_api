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
import com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.model.DietaryRestrictionsModelData


class DietaryRestrictionsAdapter(
    private var datalist: List<DietaryRestrictionsModelData>,
    private var requireActivity: FragmentActivity,
    private var onItemClickListener: OnItemClickedListener
) : RecyclerView.Adapter<DietaryRestrictionsAdapter.ViewHolder>() {

    private val selectedPositions = mutableSetOf<Int>()
    private val dietaryId = mutableListOf<String>()
    private var isExpanded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBodyGoalsBinding =
            AdapterBodyGoalsBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text = datalist[position].name

        // Inside your adapter's onBindViewHolder method
        holder.binding.apply {
            // Update UI based on the data model's 'selected' property
            if (datalist[position].selected) {
                imageRightTick.visibility = View.VISIBLE
                relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
                if (!dietaryId.contains(datalist[position].id.toString())) {
                    dietaryId.add(datalist[position].id.toString())
                }
            } else {
                imageRightTick.visibility = View.GONE
                relMainLayout.setBackgroundResource(R.drawable.gray_box_border_bg)
                dietaryId.remove(datalist[position].id.toString())
            }

            // Handle item click
            relMainLayout.setOnClickListener {
//                if (position == 0) {
//                    // Handle "None" (first item) case
//                    if (datalist[position].selected) {
//                        // Deselect "None"
//                        datalist[position].selected = false
//                        selectedPositions.remove(0)
//                        dietaryId.clear()
//                        onItemClickListener.itemClicked(position, dietaryId, "2", "false")
//                    } else {
//                        // Select "None" and clear all other selections
//                        selectedPositions.clear()
//                        datalist.forEach { it.selected = false }
//                        dietaryId.clear()
//
//                        datalist[position].selected = true
//                        selectedPositions.add(0)
//                        dietaryId.add(datalist[position].id.toString())
//                        onItemClickListener.itemClicked(position, dietaryId, "2", "true")
//                    }
//                } else {
//                    // Deselect "select all" if another item is clicked
//
//                        datalist[0].selected = false
//                        dietaryId.clear()
//
//                    // Toggle the current item's selection state
//                    if (datalist[position].selected) {
//                        // Deselect the item
//                        datalist[position].selected = false
//                        selectedPositions.remove(position)
//                        dietaryId.remove(datalist[position].id.toString())
//                        onItemClickListener.itemClicked(position, dietaryId, "2", "false")
//                    } else {
//                        // Select the item
//                        datalist[position].selected = true
//                        selectedPositions.add(position)
//                        dietaryId.add(datalist[position].id.toString())
//                        onItemClickListener.itemClicked(position, dietaryId, "2", "true")
//                    }
//                }


                if (datalist[position].name.equals("None",true)){
                    // In your click listener or wherever you're handling item selection
                    datalist.forEachIndexed { index, item ->
                        if (index==0){
                            item.selected = !item.selected
                        }else{
                            item.selected = false
                        }
                    }
                }else{
                    // In your click listener or wherever you're handling item selection
                    datalist.forEachIndexed { index, item ->
                        if (index==0){
                            item.selected = false
                        }
                    }
                    val data=datalist[position]
                    data.selected = !datalist[position].selected
                    notifyItemChanged(position,data)
                }
                notifyDataSetChanged()
                onItemClickListener.itemClicked(position, dietaryId, "2", "false")

            }
        }
    }

    override fun getItemCount(): Int {
        return if (isExpanded) datalist.size else 5.coerceAtMost(datalist.size)
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        notifyDataSetChanged()
    }

    fun filterList(filteredList: MutableList<DietaryRestrictionsModelData>) {
        this.datalist = filteredList
        notifyDataSetChanged()
    }


    class ViewHolder(var binding: AdapterBodyGoalsBinding) : RecyclerView.ViewHolder(binding.root) {
    }

}