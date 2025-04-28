package com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse

data class Data(
    var activity_level: String?,
    var bio: String?="",
    var calories: Int?=0,
    var carbs: Int?=0,
    val email: String,
    var fat: Int?=0,
    val gender: String?,
    var height: String?,
    var weight: String?,
    var dob: String?,
    var height_protein: String?,
    var height_type: String?,
    var weight_type: String?,
    var name: String?,
    val profile_img: String?,
    var protien: Int?=0
)