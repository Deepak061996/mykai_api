package com.mykaimeal.planner.fragment.mainfragment.commonscreen.missingingredientbasket.model

data class MissingIngBasketModel(
    val code: Int?,
    val `data`: MutableList<MissingIngBasketModelData>?,
    val message: String?,
    val success: Boolean?
)

data class MissingIngBasketModelData(
    val food: String?,
    val foodCategory: String?,
    val foodId: String?,
    val image: String?,
    val is_missing: Int?,
    val measure: String?,
    val quantity: Double?,
    val text: String?,
    val weight: Double?,
    var status:Boolean=false

)