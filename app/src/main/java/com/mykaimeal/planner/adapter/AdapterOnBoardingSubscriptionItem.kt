package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterOnBoardingSubscriptionItem.OnboardingViewHolder
import com.mykaimeal.planner.databinding.AdapterOnboardingSubscriptionItemBinding
import com.mykaimeal.planner.model.OnSubscriptionModel

class AdapterOnBoardingSubscriptionItem(
    var onboardingItems1: Context,
    private val onboardingItems: List<OnSubscriptionModel>,
    var providerName: String?,
    var providerImage: String?
) : RecyclerView.Adapter<OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = AdapterOnboardingSubscriptionItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position],providerName,providerImage,onboardingItems1)
    }

    override fun getItemCount(): Int {
        return onboardingItems.size
    }

    class OnboardingViewHolder(private val binding: AdapterOnboardingSubscriptionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(
            item: OnSubscriptionModel,
            providerName: String?,
            providerImage: String?,
            onboardingItems1: Context
        ) {
            binding.imageView.setBackgroundResource(item.image)
            if (item.status){
                binding.layData.visibility=View.VISIBLE
                binding.ImageMainLogo.visibility=View.VISIBLE

                if (!providerName.equals("",true)){
                    if (!providerName.equals("null",true)){
                        binding.tvTextNames.text = "You’ve got a gift from $providerName"
                        binding.tvSecretCookBook.text = "$providerName’s secret cookbook"
                    }
                }
""
                if (!providerImage.equals("",true)){
                    Glide.with(onboardingItems1)
                        .load(providerImage)
                        .placeholder(R.drawable.mask_group_icon)
                        .error(R.drawable.mask_group_icon)
                        .into(binding.imageProfile)
                }

            }else{
                binding.layData.visibility=View.GONE
                binding.ImageMainLogo.visibility=View.GONE
            }
        }
    }
}