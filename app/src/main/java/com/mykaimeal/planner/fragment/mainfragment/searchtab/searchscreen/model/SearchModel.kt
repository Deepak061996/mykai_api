package com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model

data class SearchModel(
    val code: Int,
    val `data`: SearchModelData?,
    val message: String,
    val success: Boolean
)

data class SearchModelData(
    val recipes: MutableList<Recipe>?,
    val url: String?
)

data class Recipe(
    val _links: Links?,
    var is_like: Int?,
    val recipe: RecipeX?,
    val review_number: Int?=0,
    val review: Double?=0.0,
)

data class Links(
    val self: Self?
)

data class Self(
    val href: String?,
    val title: String?
)

data class RecipeX(
    val calories: Double?,
    val cautions: MutableList<String>?,
    val co2EmissionsClass: String?,
    val cuisineType: MutableList<String>?,
    val dietLabels: MutableList<String>?,
    val digest: MutableList<Digest>?,
    val dishType: MutableList<String>?,
    val glycemicIndex: Double?,
    val healthLabels: MutableList<String>?,
    val image: String?,
    val images: Images?,
    val ingredientLines: MutableList<String>?,
    val ingredients: MutableList<Ingredient>?,
    val label: String?,
    val mealType: MutableList<String>?,
    val shareAs: String?,
    val source: String?,
    val tags: MutableList<String>?,
    val totalDaily: Any?,
    val totalNutrients: Any?,
    val totalTime: Int?,
    val totalWeight: Double?,
    val uri: String?,
    val url: String?,
    val yield: Double?
)

data class Digest(
    val daily: Double?,
    val hasRDI: Boolean?,
    val label: String?,
    val schemaOrgTag: String?,
    val sub: MutableList<Sub>?,
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
