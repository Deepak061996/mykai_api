package com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse

import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Store

data class SuperMarketModel(
    val code: Int?,
    val `data`: MutableList<Store>?,
    val message: String?,
    val success: Boolean?
)



data class SuperMarketModels(
    val code: Int,
    val `data`: MutableList<SuperMarketModelsData>?,
    val message: String,
    val success: Boolean?
)

data class SuperMarketModelsData(
    val address: SuperMarketModelAddress?,
    val distance: Double?,
    val image: String?,
    val is_open: Boolean?,
    val store_name: String?,
    val store_uuid: String?
)

data class SuperMarketModelAddress(
    val city: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
    val state: String?,
    val street_addr: String?,
    val street_addr_2: String?,
    val zipcode: String?
)