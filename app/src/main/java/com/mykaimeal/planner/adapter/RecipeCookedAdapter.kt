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
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterIngredientsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.UserDataModel

class RecipeCookedAdapter(var datalist: MutableList<UserDataModel>?, var requireActivity: FragmentActivity, var onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<RecipeCookedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterIngredientsItemBinding =
            AdapterIngredientsItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(data:  MutableList<UserDataModel>?){
        datalist=data
        notifyDataSetChanged()
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= datalist?.get(position)

        holder.binding.imageMinus.visibility=View.GONE

        if (datalist!![position].is_missing==0){
            holder.binding.missingIngredientsImg.visibility=View.VISIBLE
            holder.binding.checkBoxImg.visibility=View.GONE
        }else{
            holder.binding.missingIngredientsImg.visibility=View.GONE
            holder.binding.checkBoxImg.visibility=View.VISIBLE
        }

        if (data != null) {
            holder.binding.tvBreakfast.text=data.recipe?.label
            if (data.recipe?.images?.SMALL?.url!=null){
                Glide.with(requireActivity)
                    .load(data.recipe.images.SMALL.url)
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

            if (data.is_like !=null){
                if (data.is_like ==0 ){
                    holder.binding.imgHeartRed.setImageResource(R.drawable.heart_white_icon)
                }else{
                    holder.binding.imgHeartRed.setImageResource(R.drawable.heart_red_icon)
                }
            }

            if (data.recipe?.totalTime!=null){
                holder.binding.tvTime.text=""+ data.recipe.totalTime +" min "
            }
        }


        holder.itemView.setOnClickListener {
            if (data != null) {
                onItemClickListener.itemClick(position, "5", data.recipe?.uri)
            }
        }

        holder.binding.missingIngredientsImg.setOnClickListener{
            onItemClickListener.itemClick(datalist?.get(position)?.recipe?.yield?.toInt(), "1", datalist?.get(position)?.uri)
        }

        holder.binding.imgHeartRed.setOnClickListener {
            if (data != null) {
                onItemClickListener.itemClick(position,"4", data.is_like.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return datalist?.size!!
    }

    class ViewHolder(var binding: AdapterIngredientsItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }


}