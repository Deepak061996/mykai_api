package com.mykaimeal.planner.fragment.authfragment.notificationmodel.model

data class NotificationModel(
    val code: Int,
    val `data`: NotificationModelData,
    val message: String,
    val success: Boolean
)

data class NotificationModelData(
    val activity_level: Any,
    val bio: Any,
    val bodygoal: Int,
    val calories: Any,
    val carbs: Any,
    val conversions: Any,
    val cooking_for_type: Int,
    val cooking_frequency: Int,
    val country: Any,
    val created_at: String,
    val deleted_at: Any,
    val device_type: String,
    val eating_out: Any,
    val email: String,
    val email_verified_at: Any,
    val fat: Any,
    val fcm_token: Any,
    val gender: String,
    val height: Any,
    val height_protein: Any,
    val height_type: Any,
    val id: Int,
    val isNewuser: Int,
    val location_on_off: Int,
    val name: String,
    val notification_status: String,
    val otp: Int,
    val otp_verify: Int,
    val phone_number: Any,
    val profile_img: Any,
    val protien: Any,
    val referral_code: Any,
    val referrals: Any,
    val social_id: Any,
    val social_type: Any,
    val status: Int,
    val take_way: Int,
    val updated_at: String,
    val user_type: Int
)