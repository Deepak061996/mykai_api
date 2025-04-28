package com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse

data class IngredientsModel(
    val text: String?,
    val food: String?,
    val image: String?,
    val foodCategory: String?,
    val measure: String?,
    val quantity: Double?,
    val weight:Double?,
    val foodId:String?,
    var status:Boolean=false
)