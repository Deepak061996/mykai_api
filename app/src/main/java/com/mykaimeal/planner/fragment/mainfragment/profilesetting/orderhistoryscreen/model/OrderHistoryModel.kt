package com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model

import java.io.Serializable

data class OrderHistoryModel(
    val code: Int,
    val `data`: MutableList<OrderHistoryModelData>?,
    val message: String,
    val success: Boolean
)

data class OrderHistoryModelData(
    val address: String?,
    val status: Int?,
    val date: String?,
    val order: Order?,
    val store_logo: String?
) : Serializable

data class Order(
    val final_quote: FinalQuote?,
    val is_sandbox: Boolean?,
    val order_id: String?,
    val order_placed: Boolean?,
    val tracking_link: String?
)

data class FinalQuote(
    val added_fees: AddedFees?,
    val items: MutableList<Item>?,
    val misc_fees: MutableList<Any>,
    val quote: Quote?,
    val quote_id: String?,
    val store: String?,
    val store_address: String?,
    val store_id: String?,
    val tip: Int,
    val total_with_tip: Double
)

data class AddedFees(
    val flat_fee_cents: Int,
    val is_fee_taxable: Boolean,
    val percent_fee: Int,
    val sales_tax_cents: Int,
    val total_fee_cents: Int
)

data class Item(
    val base_price: Int?,
    val customizations: MutableList<Any>,
    val image: String?,
    val name: String?,
    val notes: String?,
    val product_id: String?,
    val quantity: Int?
)

data class Quote(
    val delivery_fee_cents: Int?,
    val delivery_time_max: Int?,
    val delivery_time_min: Int?,
    val expected_time_of_arrival: String?,
    val sales_tax_cents: Int,
    val scheduled: MutableList<Any>,
    val service_fee_cents: Int?,
    val small_order_fee_cents: Int?,
    val subtotal: Int?,
    val total_without_tips: Int?
)






