package com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse

data class DataModel(
    val userData: MutableList<UserDataModel>?,
    val frezzer: FrezzerModel?,
    val fridge: FrezzerModel?,
    val graph_value: Int?,
    var is_supermarket: Int?,
    val Subscription_status: Int?,
    val address: Int?,
    val last_plan: String?,
    val active_plan: String?,
    val monthly_savings: String?,
    val date: String?
)