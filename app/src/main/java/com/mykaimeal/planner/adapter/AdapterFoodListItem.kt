package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterLayoutFoodItemsListBinding
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.Breakfast


class AdapterFoodListItem(var itemList: MutableList<Breakfast>,var type:String?,private var requireActivity: FragmentActivity, private var onItemSelectListener: OnItemClickListener) : RecyclerView.Adapter<AdapterFoodListItem.ViewHolder>() {

    private var quantity:Int=1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterLayoutFoodItemsListBinding = AdapterLayoutFoodItemsListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (itemList[position].recipe?.label!=null){
            holder.binding.tvBreakfast.text = itemList[position].recipe?.label
        }


        if (itemList[position].is_like!=null){
            if (itemList[position].is_like ==0 ){
                holder.binding.imgHeartRed.setImageResource(R.drawable.heart_white_icon)
            }else{
                holder.binding.imgHeartRed.setImageResource(R.drawable.heart_red_icon)
            }
        }else{
            holder.binding.imgHeartRed.setImageResource(R.drawable.heart_white_icon)
        }

        if (itemList[position].created_date!=null){
            if (itemList[position].created_date!=""){
                holder.binding.textTimeAgo.text=itemList[position].created_date.toString()
            }
        }


        if (itemList[position].servings!=null){
            holder.binding.tvServes.text="Serves "+itemList[position].servings.toString()
        }

        if (itemList[position].recipe?.images?.SMALL?.url !=null){
            Glide.with(requireActivity)
                .load(itemList[position].recipe?.images?.SMALL?.url)
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


        holder.binding.imageMinusItem.setOnClickListener{
            if (itemList[position].servings.toString().toInt() > 1) {
                onItemSelectListener.itemClick(position, "4", type)
            }else{
                Toast.makeText(requireActivity,"Minimum serving at least value is one", Toast.LENGTH_LONG).show()
            }
        }

        holder.binding.imagePlusItem.setOnClickListener{
            if (itemList[position].servings.toString().toInt()  < 99) {
                onItemSelectListener.itemClick(position, "2", type)
            }
        }

        holder.binding.imgHeartRed.setOnClickListener{
            onItemSelectListener.itemClick(position, "1", type)
        }

        holder.binding.imgAppleRemove.setOnClickListener {
            onItemSelectListener.itemClick(position, "3", type)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(itemListdata: MutableList<Breakfast>, typechange: String){
        itemList=itemListdata
        type=typechange
        notifyDataSetChanged()
    }

    private fun updateValue(tvServes: TextView) {
        tvServes.text ="Serves"+ String.format("%02d", quantity)

    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    fun removeItem(position: Int) {
        itemList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemList.size) // Optional, updates the positions of remaining items
    }

    class ViewHolder(var binding: AdapterLayoutFoodItemsListBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}