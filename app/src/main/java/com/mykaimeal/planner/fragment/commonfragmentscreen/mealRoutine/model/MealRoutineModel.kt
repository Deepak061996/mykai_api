package com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model

data class MealRoutineModel(
    val code: Int,
    val `data`: MutableList<MealRoutineModelData>,
    val message: String,
    val success: Boolean
)

data class MealRoutineModelData(
    val id: Int,
    val name: String?,
    var selected:Boolean

)