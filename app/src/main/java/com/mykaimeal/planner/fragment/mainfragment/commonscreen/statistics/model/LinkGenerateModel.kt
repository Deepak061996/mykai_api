package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model

data class LinkGenerateModel(
    val code: Int?,
    val `data`: String?,
    val message: String?,
    val success: Boolean?
)

data class StatisticsGraphModel(
    val code: Int,
    val `data`: StatisticsGraphModelData,
    val message: String,
    val success: Boolean
)

data class StatisticsGraphModelData(
    val graph_data: GraphData,
    val month: String,
    val saving: Double,
    val total_spent: Double
)

data class GraphData(
    val week_1: Float,
    val week_2: Float,
    val week_3: Float,
    val week_4: Float
)

data class SpendingChartItem(
    val label: String,    // e.g., "01 April"
    val amount: Int,      // e.g., 300
    val color: Int        // bar color
)