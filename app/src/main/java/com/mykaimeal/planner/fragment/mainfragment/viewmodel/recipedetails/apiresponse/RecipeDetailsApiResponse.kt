package com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse

data class RecipeDetailsApiResponse(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String,
    val success: Boolean
)