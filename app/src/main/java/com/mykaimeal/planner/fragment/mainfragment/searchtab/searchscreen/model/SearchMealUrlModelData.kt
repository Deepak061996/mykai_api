package com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model

import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.ImagesModel

data class SearchMealUrlModelData(
    val calories: Double?,
    val cautions: MutableList<String>?,
    val cuisineType: MutableList<String>?,
    val dietLabels: MutableList<String>?,
    val digest: MutableList<Digest>?,
    val dishType: MutableList<String>?,
    val healthLabels: MutableList<String>?,
    val image: String?,
    val images: ImagesModel,
    val ingredientLines: MutableList<String>?,
    val ingredients: MutableList<Ingredient>?,
    val instructionLines: MutableList<String>?,
    val label: String?,
    val mealType: MutableList<String>?,
    val shareAs: String?,
    val source: String?,
    val totalDaily: Any?,
    val totalNutrients: Any?,
    val totalTime: Int?,
    val totalWeight: Double?,
    val uri: String?,
    val url: String?,
    val yield: Int?
)