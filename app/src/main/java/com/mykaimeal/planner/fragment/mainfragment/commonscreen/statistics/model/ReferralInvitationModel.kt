package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model

data class ReferralInvitationModel(
    val code: Int?,
    val `data`: MutableList<ReferralInvitationModelData>?,
    val message: String?,
    val success: Boolean?
)

data class ReferralInvitationModelData(
    val created_at: String?,
    val email: String?,
    val name: String?,
    val status: String?
)