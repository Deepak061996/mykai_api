package com.mykaimeal.planner.model

data class FoodItem(
    val name: String,
    val imageRes: Int,
    val clockIconRes: Int,
    val appleIconRes: Int,
    val wishlistIconRes: Int,
    val timeAgo: String,
    val serves: String
)

