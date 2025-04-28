package com.mykaimeal.planner.fragment.mainfragment.hometab.missingingredientsfragment.model

data class MissingIngredientModel(
    val code: Int?,
    val `data`: MutableList<MissingIngredientModelData>?,
    val message: String?,
    val success: Boolean?
)

data class MissingIngredientModelData(
    val food: String?,
    val foodCategory: String?,
    val foodId: String?,
    val image: String?,
    val is_missing: Int?,
    val measure: String?,
    val quantity: Double?,
    val text: String?,
    val weight: Double?,
    var status: Boolean =false // Automatically set based on is_missing

)