package com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsebydate

import com.google.gson.annotations.SerializedName

data class DataPlayByDate(
    val Breakfast: MutableList<BreakfastModelPlanByDate>?,
    val Dinner: MutableList<BreakfastModelPlanByDate>?,
    val Lunch: MutableList<BreakfastModelPlanByDate>?,
    @SerializedName("Snacks")
    val Snack: MutableList<BreakfastModelPlanByDate>?,
    @SerializedName("Brunch")
    val Teatime: MutableList<BreakfastModelPlanByDate>?,
    val fridge:Int?,
    val freezer:Int?,
    val fat:Int?,
    val protein:Int?,
    val kcal:Int?,
    val carbs:Int?,
    var show:Int
)