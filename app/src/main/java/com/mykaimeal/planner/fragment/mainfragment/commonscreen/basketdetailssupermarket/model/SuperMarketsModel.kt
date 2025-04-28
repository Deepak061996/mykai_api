package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.model

data class SuperMarketsModel(
    val code: Int?,
    val `data`: MutableList<SuperMarketsModelData>?,
    val message: String?,
    val success: Boolean?
)

data class SuperMarketsModelData(
    val address: Address?,
    val distance: Any?,
    val image: String?,
    val operational_hours: Any?,
    val store_name: String?,
    val store_uuid: String?
)