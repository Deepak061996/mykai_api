package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterInvitationItemBinding
import com.mykaimeal.planner.model.DataModel

class AdapterInviteItem(private var datalist: List<DataModel>, private var requireActivity: FragmentActivity): RecyclerView.Adapter<AdapterInviteItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterInvitationItemBinding = AdapterInvitationItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvFriendsName.text=datalist[position].title
        holder.binding.tvStatusBtn.text=datalist[position].type
        if (datalist[position].type=="Trial"){
            holder.binding.relTrialBtn.setBackgroundResource(R.drawable.trial_btn_bg)
        }else if (datalist[position].type=="Trial Over"){
            holder.binding.relTrialBtn.setBackgroundResource(R.drawable.trial_over_bg)
        }else{
            holder.binding.relTrialBtn.setBackgroundResource(R.drawable.redeemed_btn_bg)
        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    class ViewHolder(var binding: AdapterInvitationItemBinding) : RecyclerView.ViewHolder(binding.root){

    }
}