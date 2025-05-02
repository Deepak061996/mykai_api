package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model

data class StatisticsWeekYearModel(
    val code: Int?,
    val `data`: StatisticsWeekYearModelData?,
    val message: String?,
    val success: Boolean?
)

data class StatisticsWeekYearModelData(
    val month: String?,
    val orders: MutableList<Order>?,
    val recipes: Recipes?,
    val total_price: Double?,
    val user_budget: Int?
)

data class Order(
    val address: String,
    val date: String,
    val order: OrderX,
    val status: Int,
    val store_logo: String
)

data class OrderX(
    val final_quote: FinalQuote?,
    val is_sandbox: Boolean?,
    val order_id: String?,
    val order_placed: Boolean?,
    val tracking_link: String?
)

data class FinalQuote(
    val added_fees: AddedFees?,
    val items: MutableList<Any>?,
    val misc_fees: MutableList<Any>?,
    val quote: Quote?,
    val quote_id: String?,
    val store: String?,
    val store_address: String?,
    val store_id: String?,
    val tip: Int?,
    val total_with_tip: Int?
)

data class AddedFees(
    val flat_fee_cents: Int?,
    val is_fee_taxable: Boolean?,
    val percent_fee: Int?,
    val sales_tax_cents: Int?,
    val total_fee_cents: Int?
)

data class Quote(
    val delivery_fee_cents: Int?,
    val delivery_time_max: Int?,
    val delivery_time_min: Int?,
    val expected_time_of_arrival: String?,
    val sales_tax_cents: Int?,
    val scheduled: MutableList<Any>?,
    val service_fee_cents: Int?,
    val small_order_fee_cents: Int?,
    val subtotal: Int?,
    val total_without_tips: Int?
)