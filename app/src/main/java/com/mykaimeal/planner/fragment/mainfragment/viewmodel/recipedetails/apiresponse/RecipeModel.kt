package com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse

data class RecipeModel(
    val label: String?,
    val source: String?,
    val url: String?,
    val uri: String?,
    val type: String?,
    val mealType: MutableList<String>?,
    val images: ImagesModel?,
    val totalNutrients: TotalNutrientsModel?,
    val calories: Double?,
    val totalTime: Int?,
    val yield: Double?,
    val statusInGredients: Boolean=false,
    val ingredients: MutableList<IngredientsModel>?,
    val instructionLines: MutableList<String>?

)