package com.mykaimeal.planner.fragment.mainfragment.commonscreen.supermarktesnearbyscreen.model

data class SuperMarketsNearByModel(
    val code: Int?,
    val `data`: MutableList<SuperMarketsNearByModelData>?,
    val message: String?,
    val success: Boolean?
)

data class SuperMarketsNearByModelData(
    val address: Address?,
    val distance: Any?,
    val image: String?,
    val operational_hours: Any?,
    val store_name: String?,
    val store_uuid: String?
)

data class Address(
    val city: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
    val state: String?,
    val street_addr: String?,
    val street_addr_2: String?,
    val zipcode: String?
)