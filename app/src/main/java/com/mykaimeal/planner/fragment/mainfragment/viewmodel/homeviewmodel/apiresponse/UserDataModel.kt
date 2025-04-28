package com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse

import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.RecipeModel

data class UserDataModel(
    val recipe: RecipeModel?,
    var is_missing: Int?,
    var uri: String?,
    var id: Int?,
    var is_like: Int?
)