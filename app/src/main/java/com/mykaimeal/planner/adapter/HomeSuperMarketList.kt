package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterLayoutSupermarketBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.SuperMarketModelsData
import java.math.BigDecimal
import java.math.RoundingMode

class HomeSuperMarketList(
    private var storesData: MutableList<SuperMarketModelsData>?,
    private var requireActivity: FragmentActivity,
    private var onItemSelectListener: OnItemSelectListener,
    pos: Int
) : RecyclerView.Adapter<HomeSuperMarketList.ViewHolder>() {

    private var selectedPosition = pos // Default no selection

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterLayoutSupermarketBinding =
            AdapterLayoutSupermarketBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val data = storesData?.get(position)

        // ✅ Correctly update the background based on selection
        if (selectedPosition == position) {
            // ✅ Notify selection change
            onItemSelectListener.itemSelect(position, data!!.store_uuid.toString(), "SuperMarket")
            holder.binding.relativeLayoutMain.setBackgroundResource(R.drawable.supermarket_selection_bg) // Default
        } else {
            holder.binding.relativeLayoutMain.background = null // Selected
        }

        data?.let {
            if (it.store_name != null) {
                holder.binding.tvSuperMarketItems.text = it.store_name
            }

            // ✅ Load image with Glide
            Glide.with(requireActivity)
                .load(it.image)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility = View.GONE
                        return false
                    }
                })
                .into(holder.binding.imageSuperMarket)
        } ?: run {
            holder.binding.layProgess.root.visibility = View.GONE
        }


        // ✅ Click event for selection
        holder.binding.relativeLayoutMain.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position // ✅ Use `position` instead of `holder.adapterPosition`
            // Refresh the UI for both previously selected and newly selected item
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }


    fun updateList(list: MutableList<SuperMarketModelsData>){
        storesData=list
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return storesData?.size ?: 0
    }

    class ViewHolder(var binding: AdapterLayoutSupermarketBinding) :
        RecyclerView.ViewHolder(binding.root)
}
