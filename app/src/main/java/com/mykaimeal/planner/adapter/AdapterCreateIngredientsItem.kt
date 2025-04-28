package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterCreateIngredientsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefromimage.model.RecyclerViewItemModel

class AdapterCreateIngredientsItem(var datalist: MutableList<RecyclerViewItemModel>,
                                   var requireActivity: FragmentActivity,
                                   var uploadImage: UploadImage,
                                   private val onExerciseUpdated: (Int, RecyclerViewItemModel) -> Unit
                                   ) : RecyclerView.Adapter<AdapterCreateIngredientsItem.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterCreateIngredientsItemBinding = AdapterCreateIngredientsItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged", "DefaultLocale", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = datalist[position]


        holder.binding.relImages.setOnClickListener{
            uploadImage.uploadImage(position)
        }


        holder.binding.spinnerQntType.setIsFocusable(true)

        holder.binding.spinnerQntType.setItems(
            listOf("tsp", "tbsp", "cup", "ml", "liter", "floz","unit", "Pint", "quart", "gallon", "gram", "kg", "mg", "ounce", "pound",
                "pinch", "dash", "drop", "handful", "slice", "stick", "piece", "can", "bottle", "jar", "packet", "bunch", "sprig", "inch", "cm", "feet")
        )


        if (item.status) {
            holder.binding.llLayouts.setBackgroundResource(R.drawable.create_select_bg) // Change this drawable
        } else {
            holder.binding.llLayouts.setBackgroundResource(R.drawable.create_unselect_bg)  // Default background
        }

        item.measurement?.let {
            if (!it.equals("<unit>",true)){
                 if (!it.equals("null",true)){
                     holder.binding.spinnerQntType.text = it
                     holder.binding.spinnerQntType.isClickable=true
                 }else{
                     holder.binding.spinnerQntType.text = " "
                     holder.binding.spinnerQntType.isClickable=false
                 }
            }else{
                holder.binding.spinnerQntType.text = " "
                holder.binding.spinnerQntType.isClickable=false
            }
        }


        var previousSelectedItem: String? = null

        holder.binding.spinnerQntType.setIsFocusable(true)
        holder.binding.spinnerQntType.setOnSpinnerItemSelectedListener<String> { _, _, newPos, selectedItem ->
            // Check if the selected item is different from the previous one
            if (previousSelectedItem != selectedItem) {
                val data = datalist[position]
                data.status = datalist[position].ingredientName!!.isNotBlank() && datalist[position].quantity!!.isNotBlank()
                data.measurement = selectedItem
                datalist[position] = data
                // Notify only the changed item to avoid cursor issues
                notifyItemChanged(position)
                // Update the previousSelectedItem
                previousSelectedItem = selectedItem
            }
        }

        if (item.uri!=null){
            Glide.with(requireActivity)
                .load(item.uri)
                .error(R.drawable.upload_ing_icon)
                .placeholder(R.drawable.upload_ing_icon)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility= View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility= View.GONE
                        return false
                    }
                })
                .into(holder.binding.imageData)
        }else{
            holder.binding.layProgess.root.visibility= View.GONE
        }


        holder.ingredientsWatcher?.let { holder.binding.etAddIngredients.removeTextChangedListener(it) }
        holder.quantityWatcher?.let { holder.binding.etAddIngQuantity.removeTextChangedListener(it) }

        if (holder.binding.etAddIngredients.text.toString() != (item.ingredientName ?: "")) {
            holder.binding.etAddIngredients.setText(item.ingredientName ?: "")
            holder.binding.etAddIngredients.setSelection(holder.binding.etAddIngredients.text.length)
        }


        val quantityStr = item.quantity?.let { quantity ->
            quantity.toDoubleOrNull()?.let { quantityDouble ->
                if (quantityDouble % 1 == 0.0) {
                    quantityDouble.toInt().toString()
                } else {
                    String.format("%.1f", quantityDouble)
                }
            } ?: quantity
        } ?: ""

        val quantityEditText = holder.binding.etAddIngQuantity
        if (quantityEditText.text.toString() != quantityStr) {
            quantityEditText.setText(quantityStr)
            quantityEditText.setSelection(quantityStr.length)
        }


        val ingredientsWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                datalist[holder.adapterPosition].ingredientName = s.toString()
                onExerciseUpdated(holder.adapterPosition, datalist[holder.adapterPosition])
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        val quantityWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                datalist[holder.adapterPosition].quantity = s.toString()
                onExerciseUpdated(holder.adapterPosition, datalist[holder.adapterPosition])
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        holder.binding.etAddIngredients.addTextChangedListener(ingredientsWatcher)

        holder.binding.etAddIngQuantity.addTextChangedListener(quantityWatcher)

        holder.ingredientsWatcher = ingredientsWatcher
        holder.quantityWatcher = quantityWatcher


    }

    private fun updateBackground(llLayouts: LinearLayout, text: String) {
        if (text.isNotEmpty()) {
            llLayouts.setBackgroundResource(R.drawable.create_select_bg) // Change this drawable
        } else {
            llLayouts.setBackgroundResource(R.drawable.create_unselect_bg)  // Default background
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun update(toMutableList: MutableList<RecyclerViewItemModel>) {
        this.datalist=toMutableList
        notifyDataSetChanged()

    }

    class ViewHolder(var binding: AdapterCreateIngredientsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var ingredientsWatcher: TextWatcher? = null
        var quantityWatcher: TextWatcher? = null
    }

    interface UploadImage {
        // all are the abstract methods.
        fun uploadImage(pos:Int)
    }

}