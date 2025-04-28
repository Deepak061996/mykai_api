package com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.model


data class AllergensIngredientModel(
    val code: Int,
    val `data`: MutableList<AllergensIngredientModelData>,
    val message: String,
    val success: Boolean
)

data class AllergensIngredientModelData(
    val id: Int,
    var selected:Boolean=false,
    val name: String,
)