package com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model

data class CheckoutScreenModel(
    val code: Int?,
    val `data`: CheckoutScreenModelData?,
    val message: String?,
    val success: Boolean?
)

data class CheckoutScreenModelData(
    val Store: String?,
    val address: Addres?,
    val card: Card?,
    val country_code: String?,
    val delivery: Int?,
    val ingredient: List<IngredientList>?,
    val ingredient_count: Int?,
    val net_total: Double?,
    val note: Note?,
    val phone: Any,
    val processing: Double?,
    val recipes: Int?,
    val store_image: String?,
    val tax: Double?,
    val total: Double?
)


data class IngredientList(
    val created_at: String?,
    val deleted_at: Any,
    val food_id: String?,
    val id: Int,
    val market_id: Any,
    val name: String?,
    val price: Int?,
    val pro_id: String?,
    val pro_img: String?,
    val pro_name: String?,
    val pro_price: String?,
    val product_id: String?,
    val quantity: Any,
    val sch_id: Int,
    val status: Int?,
    val updated_at: String?,
    val user_id: Int?
)


data class Card(
    val card_num: Int?,
    val created_at: String,
    val deleted_at: Any,
    val id: Int,
    val payment_id: String,
    val updated_at: String,
    val user_id: Int,
    val status: Int
)

data class Addres(
    val apart_num: String?,
    val city: String?,
    val country: String?,
    val created_at: String?,
    val deleted_at: Any,
    val id: Int?,
    val latitude: String?,
    val longitude: String?,
    val primary: Int?,
    val state: String?,
    val street_name: String?,
    val street_num: String?,
    val type: String?,
    val updated_at: String?,
    val user_id: Int?,
    val zipcode: String?
)

data class Note(
    val description: String?,
    val id: Int?,
    val pickup: String?,
    val user_id: Int?
)