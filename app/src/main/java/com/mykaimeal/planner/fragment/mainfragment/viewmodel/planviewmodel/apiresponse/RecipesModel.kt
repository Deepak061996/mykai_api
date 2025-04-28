package com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse

import com.google.gson.annotations.SerializedName

data class RecipesModel(
    val Breakfast: MutableList<BreakfastModel>?,
    val Dinner: MutableList<BreakfastModel>?,
    val Lunch: MutableList<BreakfastModel>?,
    @SerializedName("Snacks")
    val Snack: MutableList<BreakfastModel>?,
    @SerializedName("Brunch")
    val Teatime: MutableList<BreakfastModel>?

)