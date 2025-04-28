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
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData

class MealRoutineAdapter(
    private var mealRoutineModelData: List<MealRoutineModelData>,
    var requireActivity: FragmentActivity,
    private var onItemClickedListener: OnItemClickedListener
) : RecyclerView.Adapter<MealRoutineAdapter.ViewHolder>() {

    private var selectedIds = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBodyGoalsBinding = AdapterBodyGoalsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitleName.text = mealRoutineModelData[position].name

         holder.binding.apply {
                 // Set tick visibility
                 imageRightTick.visibility = if (mealRoutineModelData[position].selected) View.VISIBLE else View.GONE

                 // Handle background and tick visibility based on the 'selected' property
                 if (mealRoutineModelData[position].selected) {
                     imageRightTick.visibility = View.VISIBLE
                     relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
                     if (mealRoutineModelData[0].selected){
                         relMainLayout.setBackgroundResource(R.drawable.green_box_bg)
                     }else{
                         relMainLayout.setBackgroundResource(R.drawable.orange_box_bg)
                     }
                     if (!selectedIds.contains(mealRoutineModelData[position].id.toString())) {
                         selectedIds.add(mealRoutineModelData[position].id.toString())
                     }
                     imageRightTick.setImageResource(if (position == 0) R.drawable.green_tick_icon else R.drawable.orange_tick_icon)
                     onItemClickedListener.itemClicked(position, selectedIds, "-1", "")
                 } else {
                     imageRightTick.visibility = View.GONE
                     relMainLayout.setBackgroundResource(R.drawable.gray_box_border_bg)
                 }

                // Set background color based on selection
                 relMainLayout.setBackgroundResource(
                     when {
                         position == 0 && mealRoutineModelData[position].selected -> R.drawable.green_box_bg // "Select All" selected
                         position != 0 && mealRoutineModelData[position].selected -> R.drawable.orange_box_bg // Other selected items
                         else -> R.drawable.gray_box_border_bg // Default state
                     }
                 )

                 // Handle click events
                 relMainLayout.setOnClickListener {
                     if (position == 0) { // "Select All" logic
                         if (mealRoutineModelData[position].selected) {
                             // Deselect "Select All" and all other items
                             mealRoutineModelData.forEach { it.selected = false }
                             onItemClickedListener.itemClicked(position, null, "2", "false")
                         } else {
                             // Select "Select All" and all other items
                             mealRoutineModelData.forEach { it.selected = true }
                             selectedIds = mealRoutineModelData.map { it.id.toString() }.toMutableList()
                             onItemClickedListener.itemClicked(position, selectedIds, "2", "true")
                         }
                     } else {
                         // Toggle selection for individual items
                         mealRoutineModelData[position].selected = !mealRoutineModelData[position].selected

                         // Deselect "Select All" if any individual item is deselected
                         if (!mealRoutineModelData[position].selected) {
                             mealRoutineModelData[0].selected = false
                         }

                         // Select "Select All" if all items (except the "Select All" button) are selected
                         val areAllItemsSelected = mealRoutineModelData.drop(1).all { it.selected }
                         mealRoutineModelData[0].selected = areAllItemsSelected

                         // Pass the updated selected IDs to the listener
                         selectedIds = mealRoutineModelData.filter { it.selected }.map { it.id.toString() }.toMutableList()
                         onItemClickedListener.itemClicked(position, selectedIds, "", if (mealRoutineModelData[position].selected) "true" else "false"
                         )
                     }

                     notifyDataSetChanged() // Refresh the UI
                 }
             }

    }

    override fun getItemCount(): Int {
        return mealRoutineModelData.size
    }


    class ViewHolder(var binding: AdapterBodyGoalsBinding) : RecyclerView.ViewHolder(binding.root) {

    }

}