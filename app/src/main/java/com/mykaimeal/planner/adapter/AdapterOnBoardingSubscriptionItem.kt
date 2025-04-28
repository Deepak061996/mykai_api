package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.adapter.AdapterOnBoardingSubscriptionItem.OnboardingViewHolder
import com.mykaimeal.planner.databinding.AdapterOnboardingSubscriptionItemBinding
import com.mykaimeal.planner.model.OnSubscriptionModel
import com.mykaimeal.planner.model.OnboardingItem
import com.mykaimeal.planner.model.SubscriptionModel

class AdapterOnBoardingSubscriptionItem(private val onboardingItems: List<OnSubscriptionModel>) : RecyclerView.Adapter<OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = AdapterOnboardingSubscriptionItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int {
        return onboardingItems.size
    }

    class OnboardingViewHolder(private val binding: AdapterOnboardingSubscriptionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnSubscriptionModel) {
            binding.imageView.setBackgroundResource(item.image)
            if (item.status){
                binding.layData.visibility=View.VISIBLE
                binding.ImageMainLogo.visibility=View.VISIBLE
            }else{
                binding.layData.visibility=View.GONE
                binding.ImageMainLogo.visibility=View.GONE
            }
        }
    }
}