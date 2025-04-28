package com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apisubscription

data class SubscriptionModel(
    val code: Int,
    val `data`: Data?,
    val message: String,
    val success: Boolean
)