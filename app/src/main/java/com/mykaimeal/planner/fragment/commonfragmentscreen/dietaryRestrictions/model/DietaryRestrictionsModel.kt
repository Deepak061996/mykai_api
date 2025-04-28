package com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.model

data class DietaryRestrictionsModel(
    val code: Int,
    val `data`: MutableList<DietaryRestrictionsModelData>,
    val message: String,
    val success: Boolean
)

data class DietaryRestrictionsModelData(
    val id: Int,
    var selected:Boolean=false,
    val name: String,
)