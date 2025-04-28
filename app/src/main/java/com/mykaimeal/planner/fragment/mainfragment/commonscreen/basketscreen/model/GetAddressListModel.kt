package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model

data class GetAddressListModel(
    val code: Int,
    val `data`: MutableList<GetAddressListModelData>?,
    val message: String,
    val success: Boolean
)

data class GetAddressListModelData(
    val apart_num: String?,
    val city: String?,
    val country: String?,
    val created_at: String?,
    val deleted_at: Any?,
    val id: Int?,
    val latitude: String?,
    val longitude: String?,
    var primary: Int?,
    val state: String?,
    val street_name: String?,
    val street_num: String?,
    val type: String?,
    val updated_at: String?,
    val user_id: Int?,
    val zipcode: String?
)



data class AddressPrimaryResponse(
    val code: Int,
    val `data`: AddressPrimaryResponseData,
    val message: String,
    val success: Boolean
)

data class AddressPrimaryResponseData(
    val apart_num: String,
    val city: String,
    val country: String,
    val created_at: String,
    val deleted_at: Any,
    val id: Int,
    val latitude: String,
    val longitude: String,
    val primary: Int,
    val state: String,
    val street_name: String,
    val street_num: String,
    val type: String,
    val updated_at: String,
    val user_id: Int,
    val zipcode: String
)


