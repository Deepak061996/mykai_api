package com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard

data class CradApiResponse(
    val `data`: Data?,
    val message: String,
    val success: Boolean,
    var code:Int
)