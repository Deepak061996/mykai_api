package com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model

data class BodyGoalModel(
    val code: Int,
    val `data`: MutableList<BodyGoalModelData>,
    val message: String,
    val success: Boolean
)

data class BodyGoalModelData(
    val id: Int,
    val name: String?,
    val descripttion: String?,
    var selected:Boolean=false
)


