package com.mykaimeal.planner.fragment.authfragment.locationfragment.model

data class LocationModel(
    val code: Int,
    val `data`: LocationModelData,
    val message: String,
    val success: Boolean
)


data class LocationModelData(
    val activity_level: String,
    val bio: String,
    val bodygoal: Int,
    val calories: Int,
    val carbs: Int,
    val conversions: Any,
    val cooking_for_type: Int,
    val cooking_frequency: Int,
    val country: Any,
    val created_at: String,
    val deleted_at: Any,
    val device_type: Any,
    val eating_out: Any,
    val email: String,
    val email_verified_at: Any,
    val fat: Int,
    val fcm_token: Any,
    val gender: String,
    val height: String,
    val height_protein: String,
    val height_type: String,
    val id: Int,
    val isNewuser: Int,
    val location_on_off: String,
    val name: String,
    val notification_status: Int,
    val otp: Int,
    val otp_verify: Int,
    val phone_number: Any,
    val profile_img: String,
    val protien: Int,
    val referral_code: Any,
    val referrals: Any,
    val social_id: Any,
    val social_type: Any,
    val status: Int,
    val take_way: Int,
    val updated_at: String,
    val user_type: Int
)