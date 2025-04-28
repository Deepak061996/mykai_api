package com.mykaimeal.planner.adapter

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemLongClickListener
import com.mykaimeal.planner.OnItemSelectUnSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterIngredientsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.Breakfast
import java.util.Collections

class IngredientsBreakFastAdapter(var datalist:MutableList<Breakfast>?, private var requireActivity: FragmentActivity,
                                  private var onItemClickListener: OnItemSelectUnSelectListener,
                                  private var onItemLongClickListener: OnItemLongClickListener, var type:String): RecyclerView.Adapter<IngredientsBreakFastAdapter.ViewHolder>() {

    private var checkStatus:String?=null
    private var checkTypeStatus: String? = null

    private  var isZiggleEnabled = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterIngredientsItemBinding = AdapterIngredientsItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(datalist, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(datalist, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item= datalist?.get(position)

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

        if (item?.recipe!=null){
            if (item.recipe.images?.SMALL?.url != null) {
                Glide.with(requireActivity)
                    .load(item.recipe.images.SMALL.url)
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
        }

        if (isZiggleEnabled) {
            holder.binding.imageMinus.visibility= View.VISIBLE
            holder.binding.relWatchTimer.visibility= View.GONE
            holder.binding.imgHeartRed.visibility= View.GONE
            startZiggleAnimation(holder)
        }else{
            holder.binding.imageMinus.visibility= View.GONE
            holder.binding.relWatchTimer.visibility= View.VISIBLE
            holder.binding.imgHeartRed.visibility= View.VISIBLE
            stopZiggle(holder)
        }

        if (datalist!![position].is_missing==0){
            holder.binding.missingIngredientsImg.visibility=View.VISIBLE
            holder.binding.checkBoxImg.visibility=View.GONE
        }else{
            holder.binding.missingIngredientsImg.visibility=View.GONE
            holder.binding.checkBoxImg.visibility=View.VISIBLE
        }

    /*    if (datalist[position].isOpen){
            holder.binding.missingIngredientsImg.visibility=View.VISIBLE
            holder.binding.checkBoxImg.visibility=View.GONE
        }else{
            holder.binding.missingIngredientsImg.visibility=View.GONE
            holder.binding.checkBoxImg.visibility=View.VISIBLE
        }*/

        holder.binding.missingIngredientsImg.setOnClickListener{
            checkTypeStatus="missingIng"
            onItemClickListener.itemSelectUnSelect(datalist?.get(position)?.id,checkTypeStatus,"BreakFast",position)
            /*onItemClickListener.itemClick(position, checkStatus, checkTypeStatus)*/
        }

        holder.binding.imgHeartRed.setOnClickListener{
            checkTypeStatus="heart"
            onItemClickListener.itemSelectUnSelect(position,checkTypeStatus,"BreakFast",position)
        }

        holder.binding.relMainLayouts.setOnClickListener{
            checkTypeStatus="recipeDetails"
            onItemClickListener.itemSelectUnSelect(position,checkTypeStatus,"BreakFast",position)
        }

        holder.binding.imageMinus.setOnClickListener {
            checkTypeStatus="minus"
           /* if (datalist[position].isOpen) {
                checkStatus = "1"
            } else {
                checkStatus = "0"
            }*/
            onItemClickListener.itemSelectUnSelect(datalist?.get(position)!!.id,checkTypeStatus,"BreakFast",position)
        }

        holder.itemView.setOnLongClickListener{
            val clipData = ClipData(
                item?.recipe?.label, // Use the title as the drag data
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                ClipData.Item(item?.recipe?.label)
            )

            val shadowBuilder = View.DragShadowBuilder(holder.itemView)
            holder.itemView.startDragAndDrop(clipData, shadowBuilder, null, 0)
            onItemLongClickListener.itemLongClick(position,item?.id?.toString(), type,item?.recipe?.uri!!)
            true
            /*onItemLongClickListener.itemLongClick(position, checkStatus, datalist[position].type)
            true*/
        }
    }

    fun setZiggleEnabled(enabled: Boolean) {
        isZiggleEnabled = enabled
        notifyDataSetChanged()  // Notify all items to start/stop the animation
    }

    override fun getItemCount(): Int {
        return datalist!!.size
    }

    class ViewHolder(var binding: AdapterIngredientsItemBinding) : RecyclerView.ViewHolder(binding.root){

        var ziggleAnimation: ObjectAnimator? = null

    }

  /*  private fun startZiggleAnimation(holder: ViewHolder) {
//        val startAngle = -5f // -2 degrees
//        val stopAngle = 5f   // 2 degrees
//
////        holder.ziggleAnimation? = ObjectAnimator.ofFloat(holder.itemView, "rotation", startAngle, stopAngle)
//        holder.ziggleAnimation?.duration = 80
//        holder.ziggleAnimation?.repeatMode = ValueAnimator.REVERSE
//        holder.ziggleAnimation?.repeatCount = ValueAnimator.INFINITE
//        holder.ziggleAnimation?.start()

        holder.ziggleAnimation?.cancel()
        holder.itemView.rotation = 0f
        val startAngle = -5f
        val stopAngle = 5f
        holder.ziggleAnimation = ObjectAnimator.ofFloat(holder.itemView, "rotation", startAngle, stopAngle).apply {
            duration = 80
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }*/


    private fun startZiggleAnimation(holder: ViewHolder) {
        holder.ziggleAnimation?.cancel()
        holder.itemView.rotation = 0f

        val startAngle = -5f
        val stopAngle = 5f

        holder.ziggleAnimation = ObjectAnimator.ofFloat(holder.itemView, "rotation", startAngle, stopAngle).apply {
            duration = 200 // Increase duration to slow down the movement
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator() // Ensures smooth transition
            start()
        }
    }

  /*  private fun stopZiggle(view: View) {
        // Ensure the animation is properly canceled and the object is reset.
        ziggleAnimation?.cancel()
        ziggleAnimation = null

        // Reset the rotation of the view to 0f
        view.rotation = 0f

        // Optionally set the view to be static again if needed (especially if other properties change).
        view.clearAnimation()

        // Ensure the "isZiggleEnabled" flag is updated.
        isZiggleEnabled = false
    }*/

    private fun stopZiggle(holder: ViewHolder) {
       /* ziggleAnimation?.cancel()
        view.animate().cancel()  // Cancel any ongoing animations on the view
        view.rotation = 0f
        ziggleAnimation = null
        isZiggleEnabled = false*/
        /*ziggleAnimation?.cancel()
        ziggleAnimation = null
        view.rotation = 0f
        isZiggleEnabled = false*/

        isZiggleEnabled = false
        holder.ziggleAnimation?.cancel()
        holder.itemView.rotation = 0f



    }

    fun updateList(mealList: MutableList<Breakfast>) {
        this.datalist=mealList
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        datalist?.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, datalist!!.size) // Optional, updates the positions of remaining items
    }

}