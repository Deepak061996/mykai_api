package com.mykaimeal.planner.adapter

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
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterMealItemBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.MealType

class SearchMealAdapter(var datalist: MutableList<MealType>?, private var requireActivity: FragmentActivity,private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<SearchMealAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterMealItemBinding = AdapterMealItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         val data= datalist?.get(position)

        if (data?.name!=null){
            holder.binding.tvRecipeName.text= data.name
        }

        if (data?.image!=null){
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
                .into(holder.binding.imgRecipe)
        }else{
            holder.binding.layProgess.root.visibility= View.GONE
        }

        holder.binding.relativeBreakFast.setOnClickListener{
            onItemClickListener.itemClick(position,data!!.name,"meal")
        }

    }

    override fun getItemCount(): Int {
        return datalist!!.size
    }

    fun filterList(filteredList: MutableList<MealType>) {
        this.datalist=filteredList
        notifyDataSetChanged()

    }

    fun submitList(mealType: MutableList<MealType>?) {
        this.datalist=mealType
        notifyDataSetChanged()
    }


    class ViewHolder(var binding: AdapterMealItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

}