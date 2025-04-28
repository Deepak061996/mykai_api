package com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen.model

data class GetCardMealMeModel(
    val code: Int?,
    val `data`: MutableList<GetCardMealMeModelData>?,
    val message: String?,
    val success: Boolean?
)

data class GetCardMealMeModelData(
    val card_num: Int?,
    val created_at: String?,
    val deleted_at: Any,
    val id: Int?,
    val payment_id: String?,
    val updated_at: String?,
    val status: Int?,
    val user_id: Int?
)