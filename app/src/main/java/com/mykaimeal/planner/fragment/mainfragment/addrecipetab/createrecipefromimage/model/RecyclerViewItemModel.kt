package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefromimage.model

data class RecyclerViewItemModel(
    var uri: String?,
    var ingredientName: String?,
    var status: Boolean = false,
    var quantity: String?,
    var measurement: String?=""
)