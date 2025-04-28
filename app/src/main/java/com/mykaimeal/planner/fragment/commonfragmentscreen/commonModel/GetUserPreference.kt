package com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel

import com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.model.AllergensIngredientModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.model.DietaryRestrictionsModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines.model.FavouriteCuisinesModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData

data class GetUserPreference(
    val code: Int,
    val `data`: GetUserPreferenceData,
    val message: String,
    val success: Boolean
)

data class GetUserPreferenceData(
    val allergesingredient: MutableList<AllergensIngredientModelData>,
    val bodygoal: MutableList<BodyGoalModelData>,
    val cookingfrequency: MutableList<BodyGoalModelData>,
    val cookingschedule: MutableList<Cookingschedule>,
    val dietaryrestriction: MutableList<DietaryRestrictionsModelData>,
    val eatingout: MutableList<BodyGoalModelData>,
    val familyDetail: FamilyDetail?,
    val favouritcuisine: MutableList<FavouriteCuisinesModelData>,
    val grocereisExpenses: GrocereisExpenses?,
    val ingredientdislike: MutableList<DislikedIngredientsModelData>,
    val mealroutine: MutableList<MealRoutineModelData>,
    val partnerDetail: PartnerDetail,
    val takeawayreason: MutableList<BodyGoalModelData>
)


/*
data class GetUserPreferenceData(
    val allergesingredient: List<Allergesingredient>,
    val bodygoal: List<BodyGoalModelData>,
    val cookingfrequency: List<Cookingfrequency>,
    val cookingschedule: List<Cookingschedule>,
    val dietaryrestriction: List<DietaryRestrictionsModelData>,
    val eatingout: List<Eatingout>,
    val familyDetail: Any,
    val favouritcuisine: List<Favouritcuisine>,
    val grocereisExpenses: GrocereisExpenses,
    val ingredientdislike: List<Ingredientdislike>,
    val mealroutine: List<Mealroutine>,
    val partnerDetail: PartnerDetail,
    val takeawayreason: List<Takeawayreason>
)*/
