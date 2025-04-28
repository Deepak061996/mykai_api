package com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsetransfer

data class TransferModel(
    val code: Int,
    val `data`: Data?,
    val message: String,
    val success: Boolean
)