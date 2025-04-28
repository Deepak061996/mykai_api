package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
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
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Store
import java.math.BigDecimal
import java.math.RoundingMode

class SuperMarketListAdapter(
    private var storesData: MutableList<Store>?,
    private var requireActivity: FragmentActivity,
    private var onItemSelectListener: OnItemSelectListener
) : RecyclerView.Adapter<SuperMarketListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterLayoutSupermarketBinding =
            AdapterLayoutSupermarketBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val data = storesData?.get(position)

        if (data?.is_slected != null) {
            if (data.is_slected == 1) {
                holder.binding.relativeLayoutMain.setBackgroundResource(R.drawable.supermarket_selection_bg) // Default
            } else {
                holder.binding.relativeLayoutMain.background = null // Selected
            }
        }



        data?.let {
            if (it.missing !=null) {
                if (it.missing != "0") {
                    holder.binding.tvSuperMarketItems.setTextColor(android.graphics.Color.parseColor("#FF3232"))
                    holder.binding.tvSuperMarketItems.text = it.missing.toString() + " ITEMS MISSING"
                } else {
                    holder.binding.tvSuperMarketItems.setTextColor(android.graphics.Color.parseColor("#06C169"))
                    holder.binding.tvSuperMarketItems.text = "ALL ITEMS"
                }
            }

            if (it.total != null) {
                val roundedNetTotal = it.total.let {
                    BigDecimal(it).setScale(2, RoundingMode.HALF_UP).toDouble()
                }
                holder.binding.tvSuperMarketRupees.text = "$$roundedNetTotal"
            }
            /*
                        holder.binding.tvSuperMarketItems.text = it.store_name ?: ""*/
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

        holder.binding.relativeLayoutMain.setOnClickListener {
            updateSelection(position)
            onItemSelectListener.itemSelect(position, storesData!![position].store_uuid.toString(), "SuperMarket")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSelection(selectedPosition: Int) {
        storesData?.forEachIndexed { index, stores ->
            stores.is_slected = if (index == selectedPosition) 1 else 0
        }
        notifyDataSetChanged()
    }


    fun updateList(list: MutableList<Store>){
        storesData=list
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return storesData?.size ?: 0
    }

    class ViewHolder(var binding: AdapterLayoutSupermarketBinding) :
        RecyclerView.ViewHolder(binding.root)
}

