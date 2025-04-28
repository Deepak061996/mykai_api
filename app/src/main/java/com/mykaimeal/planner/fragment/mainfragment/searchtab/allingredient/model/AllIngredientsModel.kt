package com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model

data class AllIngredientsModel(
    val code: Int,
    val `data`: AllIngredientsModelData,
    val message: String,
    val success: Boolean
)

data class AllIngredientsModelData(
    val categories: MutableList<String>?,
    val ingredients: MutableList<IngredientList>?
)


data class IngredientList(
    val category: String?,
    val created_at: Any,
    val deleted_at: Any,
    val id: Int?,
    val name: String?,
    var status: Boolean?=false,
    val updated_at: Any
)