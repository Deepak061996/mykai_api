package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemLongClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterAddressItemBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.GetAddressListModelData

class AdapterGetAddressItem(private var addressList: MutableList<GetAddressListModelData>?,
                            private var requireActivity: FragmentActivity,
                            private var onItemClickedListener: OnItemLongClickListener
):
    RecyclerView.Adapter<AdapterGetAddressItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterAddressItemBinding = AdapterAddressItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemList= addressList?.get(position)

        if (itemList?.type!=null){
            holder.binding.tvSetName.text=itemList.type.toString()
        }

        if (itemList?.type.equals("Home",true)){
            holder.binding.imageHome.setImageResource(R.drawable.home_icon)
            holder.binding.imageHome.setColorFilter(ContextCompat.getColor(requireActivity, R.color.light_orange), PorterDuff.Mode.SRC_IN)

        }else{
            holder.binding.imageHome.setImageResource(R.drawable.work_icon)
            holder.binding.imageHome.setColorFilter(ContextCompat.getColor(requireActivity, R.color.light_grays), PorterDuff.Mode.SRC_IN)

        }

        if (itemList?.primary!=null){
            if (itemList.primary==1){
                holder.binding.relHomeLayout.setBackgroundResource(R.drawable.outline_address_green_border_bg)
                onItemClickedListener.itemLongClick(itemList.id, itemList.latitude.toString(), itemList.longitude.toString(),"SelectPrimary")
            }else{
                holder.binding.relHomeLayout.setBackgroundResource(R.drawable.height_type_bg)
            }

        }

        val addressParts = listOf(
            itemList?.apart_num,
            itemList?.street_num,
            itemList?.street_name,
            itemList?.city,
            itemList?.state,
            itemList?.country,
            itemList?.zipcode
        )

        val isAddressComplete = addressParts.all { !it.isNullOrBlank() }
        val latitude = itemList?.latitude
        val longitude = itemList?.longitude

        if (isAddressComplete) {
            val fullAddress = addressParts.joinToString(" ")
            holder.binding.tvFullAddress.text = fullAddress

            if (latitude != null && longitude != null && fullAddress.isNotBlank()) {
                holder.binding.imagePencilIcon.setOnClickListener {
                    onItemClickedListener.itemLongClick(position, latitude.toString(), fullAddress, "Edit")
                }
            }
        }

        holder.binding.relSelectHome.setOnClickListener{
            if (itemList?.id!=null && itemList.latitude !=null && itemList.longitude!=null){
                updateSelection(position)
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSelection(selectedPosition: Int) {
        addressList?.forEachIndexed { index, stores ->
            stores.primary = if (index == selectedPosition) 1 else 0
            notifyItemChanged(index,stores)
        }
    }

    override fun getItemCount(): Int {
        return addressList!!.size
    }

    class ViewHolder(var binding: AdapterAddressItemBinding) : RecyclerView.ViewHolder(binding.root){
    }

}