package com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard

data class Requirements(
    val currently_due: List<Any>,
    val errors: List<Any>,
    val past_due: List<Any>,
    val pending_verification: List<Any>
)