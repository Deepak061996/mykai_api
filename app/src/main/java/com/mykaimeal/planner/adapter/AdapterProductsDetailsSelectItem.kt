package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterProductDetailsSelectItemBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketproductsdetailsscreen.BasketProductDetailsFragment
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketproductsdetailsscreen.model.BasketProductsDetailsModelData
import com.mykaimeal.planner.messageclass.ErrorMessage

class AdapterProductsDetailsSelectItem(
    private var datalist: MutableList<BasketProductsDetailsModelData>,
    private var requireActivity: FragmentActivity,
    private var onItemSelectListener: OnItemSelectListener
) : RecyclerView.Adapter<AdapterProductsDetailsSelectItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterProductDetailsSelectItemBinding =
            AdapterProductDetailsSelectItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = datalist[position]
        if (data != null) {
            if (data.sch_id != null) {
                holder.binding.textCount.text = data.sch_id.toString()
            }

            if (data.name != null) {
                val foodName = data.name
                val result = foodName.mapIndexed { index, c ->
                    if (index == 0 || c.isUpperCase()) c.uppercaseChar() else c
                }.joinToString("")
                holder.binding.textProductName.text = result
            }

            if (data.formatted_price != null) {
                if (data.formatted_price!= "Not available") {
                    holder.binding.textPrice.text = data.formatted_price.toString()
                }else{
                    holder.binding.textPrice.text="$0"
                }
            }
        }

        data.let {
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
                .into(holder.binding.productImage)
        } ?: run {
            holder.binding.layProgess.root.visibility = View.GONE
        }


        holder.binding.productImage.setOnClickListener {
            onItemSelectListener.itemSelect(data.sch_id, data.product_id, "products")
        }

        holder.binding.textProductName.setOnClickListener {
            onItemSelectListener.itemSelect(data.sch_id, data.product_id, "products")
        }

        holder.binding.productDetails.setOnClickListener {
            onItemSelectListener.itemSelect(position, data.product_id, "swap")
        }

        holder.binding.imageMinusIcon.setOnClickListener {
            if (datalist[position].sch_id.toString().toInt() > 1) {
                onItemSelectListener.itemSelect(position, data.product_id, "Minus")
            } else {
                Toast.makeText(requireActivity, ErrorMessage.servingError, Toast.LENGTH_LONG).show()
            }
        }


        holder.binding.imageAddIcon.setOnClickListener {
            if (datalist[position].sch_id.toString().toInt() < 1000) {
                onItemSelectListener.itemSelect(position, data.product_id, "Plus")
            }
        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    fun submitList(basketProducts: MutableList<BasketProductsDetailsModelData>) {
        this.datalist = basketProducts
        notifyDataSetChanged()

    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredList: MutableList<BasketProductsDetailsModelData>) {
        this.datalist = filteredList
        notifyDataSetChanged()
    }

    fun updateList(basketProductsDetailsModelData: MutableList<BasketProductsDetailsModelData>) {
        this.datalist = basketProductsDetailsModelData
        notifyDataSetChanged()
    }


    class ViewHolder(var binding: AdapterProductDetailsSelectItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}