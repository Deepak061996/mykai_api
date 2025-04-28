package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model

data class Recipe(
    val calories: Double?,
    val cautions: List<String>?,
    val cuisineType: List<String>?,
    val dietLabels: List<String>?,
    val digest: List<Any>?,
    val dishType: List<String>?,
    val healthLabels: List<String>?,
    val image: String?,
    val images: Images?,
    val ingredientLines: List<String>?,
    val ingredients: List<Ingredient>?,
    val instructionLines: List<String>?,
    val label: String?,
    val mealType: List<String>?,
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