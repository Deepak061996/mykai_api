package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model

data class Ingredient(
    val food: String?,
    val foodCategory: String?,
    val foodId: String,
    val image: String?,
    val measure: String?,
    val quantity: Double,
    val text: String?,
    val weight: Double
)