package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.model

import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.DataX

data class BasketYourRecipeModel(
    val code: Int?,
    val `data`: BasketYourRecipeModelData?,
    val message: String?,
    val success: Boolean?
)

data class BasketYourRecipeModelData(
    val Breakfast: MutableList<Dinner>?,
    val Dinner: MutableList<Dinner>?,
    val Lunch: MutableList<Dinner>?,
    val Snacks: MutableList<Dinner>?,
    val Brunch: MutableList<Dinner>?
)

data class Dinner(
    val basket_id: Int?,
    val `data`: DataX?,
    val uri: String?,
    var serving:String?
)
