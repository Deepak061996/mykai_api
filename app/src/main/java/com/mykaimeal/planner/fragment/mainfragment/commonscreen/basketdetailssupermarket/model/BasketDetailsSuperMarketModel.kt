package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.model

import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient

data class BasketDetailsSuperMarketModel(
    val code: Int?,
    val `data`: BasketDetailsSuperMarketModelData?,
    val message: String?,
    val success: Boolean?
)

data class BasketDetailsSuperMarketModelData(
    val product: MutableList<Ingredient>?,
    val store: Store?,
    val total: String?
)

data class Product(
    val created_at: String?,
    val deleted_at: Any,
    val food_id: String?,
    val id: Int?,
    val market_id: Any,
    val name: String?,
    val price: Any,
    val pro_id: String?,
    val pro_img: String?,
    val pro_name: String?,
    val pro_price: String?,
    val product_id: String,
    val quantity: Int,
    var sch_id: Int?,
    val status: Int?,
    val updated_at: String?,
    val user_id: Int?
)

data class Store(
    val address: Address?,
    val distance: Any,
    val image: String?,
    val operational_hours: Any,
    val store_name: String?,
    val store_uuid: String?

)