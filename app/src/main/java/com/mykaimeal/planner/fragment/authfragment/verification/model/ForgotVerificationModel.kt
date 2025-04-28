package com.mykaimeal.planner.fragment.authfragment.verification.model

data class ForgotVerificationModel(
    val code: Int,
    val `data`: ForgotVerificationModelData,
    val message: String,
    val success: Boolean
)

data class ForgotVerificationModelData(
    val email: String,
    val id: Int,
    val name: String,
    val profile_img: Any,
    val token: String
)