package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketproductsdetailsscreen.model

data class BasketDetailsModel(
    val code: Int,
    val `data`: BasketDetailsModelData?,
    val message: String?,
    val success: Boolean?
)


data class BasketDetailsModelData(
    val formatted_price: String?,
    val image: String?,
    val name: String?,
    val product_id: String?,
    val sch_id: Int?,
    val unit_size: Int?,
    val food_id: String?
)