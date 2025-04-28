package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterCardPrefferedItemBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen.model.GetCardMealMeModelData

class AdapterCardPreferredItem(var context: Context, private var datalist: MutableList<GetCardMealMeModelData>?,
                               private var onItemSelectListener: OnItemSelectListener,
                               private var pos: Int) : RecyclerView.Adapter<AdapterCardPreferredItem.ViewHolder>() {

    private var selectedPosition = pos // Default no selection

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterCardPrefferedItemBinding =
            AdapterCardPrefferedItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("DiscouragedApi", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = datalist!![position]
        try {
            if (datalist!![position].card_num!=null){
                holder.binding.tvCardNumber.text="*** *** **** "+datalist!![position].card_num.toString()
            }

            // Set visibility and icon based on status
            if (item.status == 1) {
                holder.binding.tvPreferred.visibility = View.VISIBLE
                holder.binding.imageCheckRadio.setImageResource(R.drawable.radio_green_icon)
                onItemSelectListener.itemSelect(position,"","CardDetails")
            } else {
                holder.binding.tvPreferred.visibility = View.GONE
                holder.binding.imageCheckRadio.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            // Click listener to handle selection
            holder.itemView.setOnClickListener {
                // Deselect all items
                datalist!!.forEach { it.status = 0 }
                // Select the clicked item
                item.status = 1
                // Refresh the whole list (or use notifyItemChanged for better performance)
                notifyDataSetChanged()
            }
        }catch (e:Exception){
            Log.d("@Error ","*****"+e.message)
        }


    }

    override fun getItemCount(): Int {
        return datalist!!.size
    }

    fun updateList(cardMealMe: MutableList<GetCardMealMeModelData>) {
        datalist=cardMealMe
        notifyDataSetChanged()
    }



    class ViewHolder(var binding: AdapterCardPrefferedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}