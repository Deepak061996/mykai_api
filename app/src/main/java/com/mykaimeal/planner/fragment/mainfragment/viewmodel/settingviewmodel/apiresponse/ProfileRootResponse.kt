package com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse

data class ProfileRootResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val success: Boolean
)