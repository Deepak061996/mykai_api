package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketproductsdetailsscreen.model

data class BasketProductsDetailsModel(
    val code: Int,
    val `data`: MutableList<BasketProductsDetailsModelData>?,
    val message: String,
    val success: Boolean
)

data class BasketProductsDetailsModelData(
    val formatted_price: String?,
    val image: String?,
    val name: String?,
    val product_id: String?,
    var sch_id: Int?,
    val food_id: String?,
    val unit_of_measurement: String?,
    val name_query: String?
)

