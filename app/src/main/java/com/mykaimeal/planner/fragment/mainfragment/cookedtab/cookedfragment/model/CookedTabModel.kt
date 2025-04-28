package com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model

import com.google.gson.annotations.SerializedName

data class CookedTabModel(
    val code: Int,
    val `data`: CookedTabModelData?,
    val message: String,
    val success: Boolean
)

data class CookedTabModelData(
    val Breakfast: MutableList<Breakfast>?,
    val Dinner: MutableList<Breakfast>?,
    val Lunch: MutableList<Breakfast>?,
    @SerializedName("Snacks")
    val Snacks: MutableList<Breakfast>?,
    @SerializedName("Brunch")
    val Teatime: MutableList<Breakfast>?,
    val fridge:Int?,
    val freezer:Int?
)

data class Breakfast(
    val created_at: String?,
    val date: String?,
    val day: Any?,
    val deleted_at: Any?,
    val id: Int?,
    var servings: Int?,
    var is_like: Int?,
    val created_date: String?,
    val plan_type: Int,
    val recipe: Recipe?,
    val status: Int,
    val type: String,
    val updated_at: String?,
    val uri: String?,
    val is_missing:Int?,
    val user_id: Int?
)

data class Recipe(
    val calories: Double?,
    val cautions: MutableList<Any?>,
    val cuisineType: MutableList<String>?,
    val dietLabels: MutableList<String>?,
    val digest: MutableList<Digest>?,
    val dishType: MutableList<String>?,
    val healthLabels: MutableList<String>?,
    val image: String?,
    val images: Images?,
    val ingredientLines: MutableList<String>?,
    val ingredients: MutableList<Ingredient>?,
    val instructionLines: MutableList<String>?,
    val label: String?,
    val mealType: MutableList<String>?,
    val shareAs: String?,
    val source: String?,
    val totalDaily: Any?,
    val totalNutrients: Any?,
    val totalTime: Int?,
    val totalWeight: Double?,
    val uri: String?,
    val url: String?
)


data class Digest(
    val daily: Double?,
    val hasRDI: Boolean?,
    val label: String?,
    val schemaOrgTag: String?,
    val sub: List<Sub>?,
    val tag: String?,
    val total: Double?,
    val unit: String?
)

data class Sub(
    val daily: Double?,
    val hasRDI: Boolean?,
    val label: String?,
    val schemaOrgTag: String?,
    val tag: String?,
    val total: Double?,
    val unit: String?
)

data class Ingredient(
    val food: String?,
    val foodCategory: String?,
    val foodId: String?,
    val image: String?,
    val measure: String?,
    val quantity: Double?,
    val text: String?,
    val weight: Double?
)

data class Images(
    val LARGE: LARGE?,
    val REGULAR: REGULAR?,
    val SMALL: SMALL?,
    val THUMBNAIL: THUMBNAIL?
)

data class LARGE(
    val height: Int?,
    val url: String?,
    val width: Int?
)

data class REGULAR(
    val height: Int?,
    val url: String?,
    val width: Int?
)

data class SMALL(
    val height: Int?,
    val url: String?,
    val width: Int?
)

data class THUMBNAIL(
    val height: Int?,
    val url: String?,
    val width: Int?
)