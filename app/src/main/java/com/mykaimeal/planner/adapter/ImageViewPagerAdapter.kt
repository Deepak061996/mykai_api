package com.mykaimeal.planner.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.ImageSliderItemBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.BreakfastModel

class ImageViewPagerAdapter(private val context: Context, private var imageList: MutableList<BreakfastModel>?,var OnItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<ImageViewPagerAdapter.ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageSliderItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        val data= imageList?.get(position)


        if (data?.recipe?.label!=null){
            holder.binding.tvname.text = data.recipe.label
        }

        if (data?.recipe?.totalTime!=null){
            holder.binding.tvTime.text = ""+ data.recipe.totalTime +" min "
        }

        if (data?.review!=null){
            holder.binding.textMin.text = ""+ data.review +"("+ data.review_number +")"
        }

        if (data?.is_like!=null){
            if (data.is_like ==0 ){
                holder.binding.imgHeartRed.setImageResource(R.drawable.heart_white_icon)
            }else{
                holder.binding.imgHeartRed.setImageResource(R.drawable.heart_red_icon)
            }
        }

        if (data?.recipe?.images?.SMALL?.url != null) {
            Glide.with(context)
                .load(data.recipe.images.SMALL.url)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        holder.binding.layProgess.root.visibility = View.GONE
                        return false
                    }
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        holder.binding.layProgess.root.visibility = View.GONE
                        return false
                    }
                })
                .into(holder.binding.imageData)
        } else {
            holder.binding.layProgess.root.visibility = View.GONE
        }



        holder.binding.imgHeartRed.setOnClickListener {
            OnItemClickListener.itemClick(position,"4", "")
        }





    }

    override fun getItemCount(): Int {
        return imageList!!.size
    }

    class ImageViewHolder(var binding : ImageSliderItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    fun updateItem(list : MutableList<BreakfastModel>){
        this.imageList = list
        notifyDataSetChanged()

    }
}
