package com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard

data class BankAccount(
    val account: String,
    val account_holder_name: String,
    val account_holder_type: String,
    val account_type: Any,
    val available_payout_methods: List<String>,
    val bank_name: String,
    val country: String,
    val currency: String,
    val default_for_currency: Boolean,
    val fingerprint: String,
    val future_requirements: FutureRequirements,
    val id: String,
    val last4: String,
    val metadata: List<Any>,
    val `object`: String,
    val requirements: Requirements,
    val routing_number: String,
    val status: String
)