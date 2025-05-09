package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
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
import com.mykaimeal.planner.databinding.AdapterMealTypeItemBinding
import com.mykaimeal.planner.databinding.AdapterStatisticsWeekYearItemBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.Breakfast
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.BreakfastModel
import com.mykaimeal.planner.model.DataModel

class AdapterStatisticsWeekItem(private var dataList: MutableList<Breakfast>?, private var requireActivity: FragmentActivity,
                                private var onItemClickListener: OnItemClickListener,private var type:String): RecyclerView.Adapter<AdapterStatisticsWeekItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterMealTypeItemBinding = AdapterMealTypeItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updateList: MutableList<Breakfast>, type:String){
        dataList=updateList
        this.type=type
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item= dataList?.get(position)

            if (item?.recipe?.label!=null){
                holder.binding.tvBreakfast.text = item.recipe.label
            }

            if (item?.recipe?.totalTime!=null){
                holder.binding.tvTime.text = ""+ item.recipe.totalTime +" min "
            }

            if (item?.is_like!=null){
                if (item.is_like ==0 ){
                    holder.binding.imgHeartRed.setImageResource(R.drawable.heart_white_icon)
                }else{
                    holder.binding.imgHeartRed.setImageResource(R.drawable.heart_red_icon)
                }
            }

            if (item?.review!=null){
                holder.binding.tvRatingReviews.text = ""+ item.review +"("+ item.review_number +")"
            }


            if (item?.recipe?.images?.SMALL?.url!=null){
                Glide.with(requireActivity)
                    .load(item.recipe.images.SMALL.url)
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

            holder.binding.tvAddToPlan.setOnClickListener{
                onItemClickListener.itemClick(position,"1",type)
            }

            holder.binding.imgBasket.setOnClickListener{
                onItemClickListener.itemClick(position,"2",type)
            }

            holder.binding.imgHeartRed.setOnClickListener {
                onItemClickListener.itemClick(position,"4", type)
            }

            holder.itemView.setOnClickListener{
                if (item?.recipe?.uri!=null){
                    onItemClickListener.itemClick(position, type, item.recipe.uri)
                }
            }
        }catch (e:Exception){
            Log.d("@@@@ ","Error ****"+e.message.toString())
        }

    }


    override fun getItemCount(): Int {
        return dataList!!.size
    }


    class ViewHolder(var binding: AdapterMealTypeItemBinding) : RecyclerView.ViewHolder(binding.root){

    }
}