package com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard

data class CardData(
    val brand: String?,
    val card_id: String?,
    val exp_month: Int?,
    val exp_year: Int?,
    val last4: String?,
    val name: String?,
    val customer_id: String?
)