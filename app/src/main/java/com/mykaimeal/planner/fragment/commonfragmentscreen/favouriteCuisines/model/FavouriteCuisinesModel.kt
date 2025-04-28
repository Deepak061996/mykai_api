package com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines.model

data class FavouriteCuisinesModel(
    val code: Int,
    val `data`: MutableList<FavouriteCuisinesModelData>,
    val message: String,
    val success: Boolean
)

data class FavouriteCuisinesModelData(
    val id: Int,
    var selected:Boolean=false,
    val name: String,
)