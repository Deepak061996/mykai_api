package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
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

    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            if (datalist!![position].card_num!=null){
             holder.binding.tvCardNumber.text="**** **** **** "+datalist!![position].card_num.toString()
            }

            // ✅ Correctly update the background based on selection
            if (selectedPosition == position) {
                holder.binding.imageCheckRadio.setBackgroundResource(R.drawable.radio_green_icon)
            } else {
                holder.binding.imageCheckRadio.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            // ✅ Click event for selection
            holder.binding.imageCheckRadio.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position // ✅ Use `position` instead of `holder.adapterPosition`
                // Refresh the UI for both previously selected and newly selected item
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // ✅ Notify selection change
                onItemSelectListener.itemSelect(position, "", "CardDetails")
            }

        }catch (e:Exception){
            Log.d("@Error ","*****"+e.message)
        }

    }

    override fun getItemCount(): Int {
        return datalist!!.size
    }


    class ViewHolder(var binding: AdapterCardPrefferedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}