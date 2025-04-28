package com.mykaimeal.planner.fragment.mainfragment.viewmodel.notificationviewmodel.apiresponse

data class NotificationApiResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val success: Boolean
)