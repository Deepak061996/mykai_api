package com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse

import com.google.gson.annotations.SerializedName

data class FrezzerModel(
    val Breakfast: Int?,
    val Dinner: Int?,
    val Lunch: Int?,
    val Snacks: Int?,
    @SerializedName("Brunch")
    val Teatime: Int?
)