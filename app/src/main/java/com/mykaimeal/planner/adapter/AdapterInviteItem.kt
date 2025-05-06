package com.mykaimeal.planner.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterInvitationItemBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.ReferralInvitationModelData

class AdapterInviteItem(
    private var datalist: MutableList<ReferralInvitationModelData>?,
    private var requireActivity: FragmentActivity
) : RecyclerView.Adapter<AdapterInviteItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterInvitationItemBinding =
            AdapterInvitationItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = datalist!![position]

        try {

            item.name?.let {
                holder.binding.tvFriendsName.text=it
            }

            if (item.status!=null){

            }
            item.status?.let {
                if (it!=""){
                    holder.binding.tvStatusBtn.text=it
                    val backgroundRes = when (it.lowercase()) {
                        "trial" -> R.drawable.trial_btn_bg
                        "trial over" -> R.drawable.trial_over_bg
                        else -> R.drawable.redeemed_btn_bg
                    }
                    holder.binding.relTrialBtn.setBackgroundResource(backgroundRes)
                }
            }
        } catch (e: Exception) {
            Log.d("@Error ", "*****" + e.message)
        }
    }

    override fun getItemCount(): Int {
        return datalist!!.size
    }

    fun updateList(referralList: MutableList<ReferralInvitationModelData>) {
        datalist = referralList
        notifyDataSetChanged()

    }

    class ViewHolder(var binding: AdapterInvitationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}