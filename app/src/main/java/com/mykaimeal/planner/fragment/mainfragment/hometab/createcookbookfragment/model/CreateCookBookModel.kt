package com.mykaimeal.planner.fragment.mainfragment.hometab.createcookbookfragment.model

data class CreateCookBookModel(
    val code: Int,
    val message: String,
    val success: Boolean,
    val data: CookBookDataModel
)