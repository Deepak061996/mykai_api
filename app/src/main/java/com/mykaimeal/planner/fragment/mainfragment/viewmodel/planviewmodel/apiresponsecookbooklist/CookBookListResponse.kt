package com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist

data class CookBookListResponse(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String,
    val success: Boolean
)