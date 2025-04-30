package com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse

data class CookBookListApiResponse(
    val code: Int,
    val `data`: MutableList<CookBookDataModel>?,
    val message: String,
    val shared: Int?,
    val success: Boolean
)