package com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel

data class CookBook(
    val created_at: String,
    val deleted_at: Any,
    val id: Int?,
    val image: String?,
    val name: String?,
    val shared: Int?,
    val status: Int?,
    val updated_at: String,
    val user_id: Int?
)