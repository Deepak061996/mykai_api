package com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.model

data class GetDropOffOptionsModel(
    val code: Int?,
    val `data`: MutableList<GetDropOffOptionsModelData>?,
    val message: String?,
    val success: Boolean?,
    val description:String?
)

data class GetDropOffOptionsModelData(
    val id: Int,
    val name: String?,
    val status: Int,
    val type: Int
)