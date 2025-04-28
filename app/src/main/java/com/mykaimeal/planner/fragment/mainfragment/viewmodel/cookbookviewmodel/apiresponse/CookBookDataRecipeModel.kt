package com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse

import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.RecipeModel

data class CookBookDataRecipeModel(
    val recipe: RecipeModel?,
    var is_like: Int?
)