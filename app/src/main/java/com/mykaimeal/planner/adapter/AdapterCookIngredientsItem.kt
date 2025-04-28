package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterCookInstructionsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.RecyclerViewCookIngModel

class AdapterCookIngredientsItem(
    private var datalist: MutableList<RecyclerViewCookIngModel>,
    private var requireActivity: FragmentActivity,
    private val onExerciseUpdated: (Int, RecyclerViewCookIngModel) -> Unit
) : RecyclerView.Adapter<AdapterCookIngredientsItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterCookInstructionsItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datalist[position]

        // Set step number
        holder.binding.tvInstructions.text = "Step- "+(position+1)

        // Remove old text watcher if any
        holder.ingredientsWatcher?.let {
            holder.binding.etAddIngredients.removeTextChangedListener(it)
        }

        // Set description text only if different
        val currentText = item.description ?: ""
        if (holder.binding.etAddIngredients.text.toString() != currentText) {
            holder.binding.etAddIngredients.setText(currentText)
            holder.binding.etAddIngredients.setSelection(currentText.length)
        }

        // Background color based on content
        if (currentText.isNotEmpty()) {
            holder.binding.llLayouts.setBackgroundResource(R.drawable.create_select_bg)
        } else {
            holder.binding.llLayouts.setBackgroundResource(R.drawable.create_unselect_bg)
        }

        // Create new text watcher
        val ingredientsWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    datalist[pos].description = s.toString()
                    onExerciseUpdated(pos, datalist[pos])
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // Attach new text watcher
        holder.ingredientsWatcher = ingredientsWatcher
        holder.binding.etAddIngredients.addTextChangedListener(ingredientsWatcher)
    }

    override fun getItemCount(): Int = datalist.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(recyclerViewCookIngModels: MutableList<RecyclerViewCookIngModel>) {
        this.datalist = recyclerViewCookIngModels
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: AdapterCookInstructionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var ingredientsWatcher: TextWatcher? = null
    }

}
