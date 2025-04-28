package com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecountry

data class CountryResponseModel(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String,
    val success: Boolean
)