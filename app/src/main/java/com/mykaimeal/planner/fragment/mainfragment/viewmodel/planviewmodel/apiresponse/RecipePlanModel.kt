package com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse

import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.ImagesModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.TotalNutrientsModel

data class RecipePlanModel(
    val label: String?,
    val uri: String?,
    val images: ImagesModel?,
    val totalNutrients: TotalNutrientsModel?,
    val calories: Double?,
    val totalTime: Int?

    /*  val label: String?,
      val uri: String?,
      val images: Any?,
      val totalNutrients: TotalNutrientsModel?,
      val calories: Double?,
      val totalTime: Int?*/

)