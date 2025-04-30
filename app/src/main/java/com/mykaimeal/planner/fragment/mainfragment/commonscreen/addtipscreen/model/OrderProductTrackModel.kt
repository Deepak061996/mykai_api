package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen.model

data class OrderProductTrackModel(
    val code: Int?,
    val message: String?,
    val error: String?,
    val response: Response?,
    val success: Boolean?
)

data class Response(
    val final_quote: FinalQuote?,
    val is_sandbox: Boolean?,
    val order_id: String?,
    val error: String?,
    val order_placed: Boolean?,
    val tracking_link: String?
)


data class FinalQuote(
    val added_fees: AddedFees,
    val items: MutableList<Item>?,
    val misc_fees: List<Any>?,
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

data class Item(
    val base_price: Int?,
    val customizations: MutableList<Any>?,
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
    val sales_tax_cents: Int?,
    val scheduled: MutableList<Any>?,
    val service_fee_cents: Int?,
    val small_order_fee_cents: Int?,
    val subtotal: Int?,
    val total_without_tips: Int?
)


data class GetTipModel(
    val code: Int,
    val `data`: GetTipModelData,
    val message: String,
    val success: Boolean
)

data class GetTipModelData(
    val tip10: Double?,
    val tip15: Double?,
    val tip20: Double?,
    val tip25: Double?
)