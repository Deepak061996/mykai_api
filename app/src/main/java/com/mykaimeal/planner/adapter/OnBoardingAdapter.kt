package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R
import com.mykaimeal.planner.model.OnboardingItem

class OnboardingAdapter(onboardingItems: List<OnboardingItem>) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {
    private val onboardingItems: List<OnboardingItem>

    init {
        this.onboardingItems = onboardingItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.adapter_onboarding_item, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int {
        return onboardingItems.size
    }

    class OnboardingViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView


        init {
            imageView = itemView.findViewById<ImageView>(R.id.imageView)
        }

        fun bind(item: OnboardingItem) {
            imageView.setImageResource(item.image)

        }
    }
}
