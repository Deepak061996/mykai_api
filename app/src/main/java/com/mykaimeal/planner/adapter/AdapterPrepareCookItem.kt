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
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterPrepareCookItemBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.IngredientsModel
import java.math.BigDecimal
import java.math.RoundingMode


class AdapterPrepareCookItem(private var datalist: MutableList<IngredientsModel>?, private var requireActivity: FragmentActivity): RecyclerView.Adapter<AdapterPrepareCookItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterPrepareCookItemBinding =
            AdapterPrepareCookItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data=datalist!![position]

        if (data.image!=null){
            Glide.with(requireActivity)
                .load(data.image)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
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

        if (data.food!=null){
            val foodName = data.food
            val formattedFoodName = foodName.let {
                // Capitalize the first letter and make the rest lowercase
                it.replaceFirstChar { char -> char.uppercase() } + it.drop(1).lowercase()
            }
            holder.binding.tvTitleName.text =formattedFoodName
        }

        if (data.quantity!=null){
            val roundedQuantity = data.quantity.let {
                BigDecimal(it).setScale(2, RoundingMode.HALF_UP).toDouble()
            }
            if (!data.measure.equals("<unit>")){
                holder.binding.tvTitleDesc.text =""+roundedQuantity+" "+data.measure
            }else{
                holder.binding.tvTitleDesc.text =""+roundedQuantity
            }
        }

    }

    override fun getItemCount(): Int {
        return datalist?.size!!
    }


    class ViewHolder(var binding: AdapterPrepareCookItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}