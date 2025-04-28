package com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse

data class HomeApiResponse(
    val code: Int,
    val `data`: DataModel?,
    val message: String,
    val success: Boolean
)