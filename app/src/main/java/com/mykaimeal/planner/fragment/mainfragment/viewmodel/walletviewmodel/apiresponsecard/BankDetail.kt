package com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard

data class BankDetail(
    val address: Address,
    val bank_account: MutableList<BankAccount>,
    val disabled_reason: Any,
    val dob: String,
    val email: String,
    val firstname: String,
    val lastname: String,
    val local_document: LocalDocument,
    val phone: String,
    val stripe_document: StripeDocument,
    val verification_status: Boolean
)