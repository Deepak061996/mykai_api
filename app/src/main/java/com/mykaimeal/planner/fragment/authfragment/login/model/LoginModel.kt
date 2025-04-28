package com.mykaimeal.planner.fragment.authfragment.login.model

data class LoginModel(
    val code: Int,
    val `data`: LoginModelData?,
    val message: String,
    val success: Boolean
)

data class LoginModelData(
    val device_type: Any,
    val email: String?,
    val fcm_token: String?,
    val id: Int?,
    val cooking_for_type: Int?,
    val is_cooking_complete: Int?,
    val name: String?,
    val referral_code: String?,
    val profile_img: String?,
    val token: String?,
    val updated_at: String
)
data class SocialLoginModel(
    val code: Int,
    val `data`: SocialLoginModelData,
    val message: String,
    val success: Boolean
)

data class SocialLoginModelData(
    val cooking_for_type: Int?,
    val is_cooking_complete: Int?,
    val created_at: String?,
    val name: String?,
    val email: String?,
    val id: Int?,
    val isNewuser: Int?,
    val otp_verify: Int?,
    val social_id: String?,
    val profile_img: String?,
    val referral_code: String?,
    val token: String?,
    val updated_at: String?
)

