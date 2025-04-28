package com.mykaimeal.planner.adapter

import android.content.Context
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R

// Extension function to convert dp to pixels
fun Int.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

class IndicatorAdapter(private val itemCount: Int) : RecyclerView.Adapter<IndicatorAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(val view: ImageView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val imageView = ImageView(context)

        // Initial size and margin (default size)
        val size = 10.dpToPx(context)
        val margin = 2.dpToPx(context)

        val layoutParams = ViewGroup.MarginLayoutParams(size, size)
        layoutParams.setMargins(margin, 0, margin, 0)
        imageView.layoutParams = layoutParams

        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val isSelected = position == selectedPosition

        val size = if (isSelected) 20.dpToPx(context) else 17.dpToPx(context)  // Selected is bigger
        val margin = 2.dpToPx(context)

        val layoutParams = ViewGroup.MarginLayoutParams(size, size)
        layoutParams.setMargins(margin, 0, margin, 0)
        holder.view.layoutParams = layoutParams

        val drawableId = if (isSelected) {
            R.drawable.indicator_active_circle
        } else {
            R.drawable.indicator_inactive
        }

        holder.view.setImageDrawable(ContextCompat.getDrawable(context, drawableId))
    }


    override fun getItemCount(): Int = itemCount

    fun updateSelectedPosition(newPosition: Int) {
        val oldPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(oldPosition)
        notifyItemChanged(newPosition)
    }
}


