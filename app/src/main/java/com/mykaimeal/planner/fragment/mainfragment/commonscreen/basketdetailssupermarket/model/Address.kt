package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.model

data class Address(
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val state: String,
    val street_addr: String,
    val street_addr_2: String,
    val zipcode: String
)