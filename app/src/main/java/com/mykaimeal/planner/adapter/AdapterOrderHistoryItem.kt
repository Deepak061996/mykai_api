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
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.AdapterOrderHistoryItemBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModelData

class AdapterOrderHistoryItem(private var datalist: MutableList<OrderHistoryModelData>,
                              private var requireActivity: FragmentActivity,
                              private var onItemClickedListener: OnItemClickedListener): RecyclerView.Adapter<AdapterOrderHistoryItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterOrderHistoryItemBinding = AdapterOrderHistoryItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= datalist[position]

        data.let {

            if (it.store_logo!=null){
                Glide.with(requireActivity)
                    .load(it.store_logo)
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
                    .into(holder.binding.imageData)
            }

            val date = it.date
            val quote = it.order?.final_quote
            val itemsSize = quote?.items?.size ?: 0
            val totalWithTip = quote?.total_with_tip?.div(100.0) // use 100.0 for Double division

            val formattedTotalWithTip = totalWithTip?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else String.format("%.2f", it)
            }

            if (date != null && totalWithTip != null && itemsSize > 0) {
                holder.binding.tvDate.text = "${BaseApplication.formatDateMonth(date)} • $$formattedTotalWithTip • $itemsSize items"
            }

            if (it.address!=null){
                holder.binding.tvDelivery.text="Delivered -"+it.address.toString()
            }

            if (it.status==1){
                holder.binding.tvTrackOrder.visibility=View.GONE
                holder.binding.tvViewOrder.visibility=View.VISIBLE
            }else{
                holder.binding.tvTrackOrder.visibility=View.VISIBLE
                holder.binding.tvViewOrder.visibility=View.GONE
            }
        }

        holder.binding.tvViewOrder.setOnClickListener{
            onItemClickedListener.itemClicked(position,null,"","View")
        }

        holder.binding.tvTrackOrder.setOnClickListener{
            onItemClickedListener.itemClicked(position,null,"","Track")
        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    fun updateList(dataList: MutableList<OrderHistoryModelData>) {
        datalist=dataList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterOrderHistoryItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

}