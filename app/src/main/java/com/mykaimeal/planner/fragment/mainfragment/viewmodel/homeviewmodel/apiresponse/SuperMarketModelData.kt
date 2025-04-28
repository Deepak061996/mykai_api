package com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse

data class SuperMarketModelData(
    val address: Address?,
    val distance: Double?,
    val operational_hours: OperationalHours?,
    val store_name: String?,
    val store_uuid: String?
)