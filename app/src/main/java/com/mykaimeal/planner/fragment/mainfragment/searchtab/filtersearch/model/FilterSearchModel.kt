package com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model

data class FilterSearchModel(
    val code: Int?,
    val `data`: FilterSearchModelData?,
    val message: String?,
    val success: Boolean?
)

data class FilterSearchModelData(
    val Diet: MutableList<Diet>?,
    val cook_time: MutableList<CookTime>?,
    val mealType: MutableList<MealType>?
)

data class Diet(
    val name: String?,
    var selected:Boolean?=false,
    val value: String?
)

data class CookTime(
    val name: String?,
    val value: String?,
    var selected:Boolean?=false
)

data class MealType(
    val id: Int?,
    val image: String?,
    val name: String?,
    val value: String?,
    var selected:Boolean?=false
)