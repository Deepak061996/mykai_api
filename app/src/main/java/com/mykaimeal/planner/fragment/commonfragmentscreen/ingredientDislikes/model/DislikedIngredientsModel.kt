package com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model

data class DislikedIngredientsModel(
    val code: Int,
    val `data`: MutableList<DislikedIngredientsModelData>?,
    val message: String,
    val success: Boolean
)

data class DislikedIngredientsModelData(
    val id: Int,
    var selected:Boolean=false,
    val name: String,
)