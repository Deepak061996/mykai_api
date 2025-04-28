package com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse

data class Data(
    val recipe: RecipeModel?,
    val is_like: Int?,
    val review_number: Int?=0,
    val review: Double?=0.0,
)