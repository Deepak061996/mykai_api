package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model

import com.google.gson.annotations.SerializedName

data class Recipes(
    val Breakfast: MutableList<Breakfast>?,
    val Breakfast_price: Double?,
    val Dinner: MutableList<Breakfast>?,
    val Dinner_price: Double?,
    val Lunch: MutableList<Breakfast>?,
    val Lunch_price: Double?,
    @SerializedName("Snacks")
    val Snacks:MutableList<Breakfast>?,
    val Snacks_price:Double?,
    @SerializedName("Brunch")
    val Brunch:MutableList<Breakfast>?,
    val Brunch_price:Double?,

)

data class Breakfast(
    val created_at: String,
    val date: String?,
    val day: String?,
    val deleted_at: Any,
    val id: Int?,
    val plan_type: Int?,
    val recipe: Recipe?,
    val servings: Int?,
    val status: Int?,
    val type: String?,
    val updated_at: String?,
    val uri: String?,
    val user_id: Int?
)


data class Recipe(
    val `xservices`: X0?,
    val price: Double?
)

data class X0(
    val _links: Links?,
    val recipe: RecipeX?
)

data class Links(
    val self: Self
)

data class Self(
    val href: String,
    val title: String
)

data class RecipeX(
    val calories: Double,
    val cautions: MutableList<String>?,
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
    val url: String?,
    val yield: Int?
)

data class Digest(
    val daily: Double?,
    val hasRDI: Boolean,
    val label: String?,
    val schemaOrgTag: String?,
    val sub: MutableList<Any>?,
    val tag: String,
    val total: Double,
    val unit: String
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
    val LARGE: LARGE,
    val REGULAR: REGULAR,
    val SMALL: SMALL,
    val THUMBNAIL: THUMBNAIL
)

data class LARGE(
    val height: Int,
    val url: String,
    val width: Int
)

data class REGULAR(
    val height: Int,
    val url: String,
    val width: Int
)

data class SMALL(
    val height: Int,
    val url: String,
    val width: Int
)

data class THUMBNAIL(
    val height: Int,
    val url: String,
    val width: Int
)