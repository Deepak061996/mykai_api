package com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse

data class Address(
    val city: String?,
    val country: String?,
    val lat: Double?,
    val lon: Double?,
    val state: String?,
    val street_addr: String?,
    val zipcode: String?
)