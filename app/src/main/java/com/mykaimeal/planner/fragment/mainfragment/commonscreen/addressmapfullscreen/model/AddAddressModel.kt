package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addressmapfullscreen.model

data class AddAddressModel(
    val code: Int?,
    val `data`: AddAddressModelData?,
    val message: String?,
    val success: Boolean?
)

data class AddAddressModelData(
    val apart_num: String?,
    val city: String?,
    val country: String?,
    val created_at: String?,
    val id: Int?,
    val latitude: String?,
    val longitude: String?,
    val primary: String?,
    val state: String?,
    val street_name: String?,
    val street_num: String?,
    val type: String?,
    val updated_at: String?,
    val user_id: Int?,
    val zipcode: String?
)