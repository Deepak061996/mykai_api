package com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen.model

import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Recipes

data class ShoppingListModel(
    val code: Int,
    val `data`: ShoppingListModelData?,
    val message: String,
    val success: Boolean
)

data class ShoppingListModelData(
    val ingredient: MutableList<Ingredient>?,
    val recipe: MutableList<Recipes>?
)
