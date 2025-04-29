package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model

import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.Recipe

data class BasketScreenModel(
    val code: Int?,
    val `data`: BasketScreenModelData?,
    val message: String?,
    val success: Boolean?
)


data class BasketScreenModelData(
    val ingredient: MutableList<Ingredient>?,
    val recipe: MutableList<Recipes>?,
    val stores: MutableList<Store>?,
    val billing:BillingAddress?
)

data class Ingredient(
    val created_at: String?,
    val deleted_at: String?,
    val food_id: String?,
    val id: Int?,
    val market_id: Any?,
    val name: String?,
    val price: Any?,
    val pro_id: String?,
    val pro_img: String?,
    val pro_name: String?,
    val pro_price: String?,
    val product_id: String?,
    val quantity: Any,
    var sch_id: Int?,
    val status: Int?,
    val newStatus: Boolean?=false,
    val updated_at: String?,
    val user_id: Int?,
    val unit_of_measurement: String?
)

data class Store(
    val address: Address?,
    val distance: Double?,
    val operational_hours: OperationalHours?,
    val store_name: String?,
    val store_uuid: String?,
    val image:String?,
    val all_items:Int?,
    var is_slected:Int?,
    val missing:String?,
    val total:Double?
)

data class Address(
    val city: String?,
    val country: String?,
    val latitude: Double?=0.0,
    val longitude: Double?=0.0,
    val state: String?,
    val street_addr: String?,
    val zipcode: String?
)

data class BillingAddress(
    val recipes:Int?,
    val net_total:Double?,
    val tax:Double?,
    val delivery:Double?,
    val processing:Double?,
    val total:Double?
)


data class OperationalHours(
    val Friday: String?,
    val Monday: String?,
    val Saturday: String?,
    val Sunday: String?,
    val Thursday: String?,
    val Tuesday: String?,
    val Wednesday: String?
)

data class Recipes(
    val created_at: String?,
    val `data`: DataX?,
    val deleted_at: Any?,
    val id: Int?,
    var serving: String?,
    val type: String?,
    val updated_at: String?,
    val uri: String?,
    val user_id: Int?
)

data class DataX(
    val _links: Links?,
    val recipe: Recipe?
)

data class Links(
    val self: Self?
)

data class Self(
    val href: String?,
    val title: String?
)



