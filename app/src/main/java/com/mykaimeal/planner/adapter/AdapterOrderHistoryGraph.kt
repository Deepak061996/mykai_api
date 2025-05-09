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
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterOrderHistoryGraphBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModelData

class AdapterOrderHistoryGraph(private var datalist: MutableList<OrderHistoryModelData>,
                               private var requireActivity: FragmentActivity,
                               private var onItemClickedListener: OnItemClickedListener): RecyclerView.Adapter<AdapterOrderHistoryGraph.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterOrderHistoryGraphBinding = AdapterOrderHistoryGraphBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= datalist[position]

        data.let { it ->
            it.store_logo?.let { local->
                Glide.with(requireActivity)
                    .load(local)
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

            }?:run {
                holder.binding.layProgess.root.visibility = View.GONE
            }


            it.order?.order_id?.let {
                holder.binding.tvOrder.text= "Order #$it"
            }

            it.order?.final_quote?.items?.let { count->
                holder.binding.tvCount.text= "View ${count.size} items"
            }

            it.order?.final_quote?.total_with_tip?.let { value->
                val formattedPrice = String.format("%.2f", value)
                holder.binding.tvPrice.text= "Total $${formattedPrice}"
            }

        }


        holder.binding.tvCount.setOnClickListener{
            onItemClickedListener.itemClicked(position,null,"","View")
        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(dataList: MutableList<OrderHistoryModelData>) {
        datalist=dataList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterOrderHistoryGraphBinding) : RecyclerView.ViewHolder(binding.root){

    }

}