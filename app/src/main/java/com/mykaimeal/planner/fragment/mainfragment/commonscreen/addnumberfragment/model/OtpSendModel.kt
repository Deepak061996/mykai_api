package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addnumberfragment.model

data class OtpSendModel(
    val code: Int,
    val message: String,
    val success: Boolean,
    val data:Int
)