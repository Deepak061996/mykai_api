package com.mykaimeal.planner.basedata

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mykaimeal.planner.fragment.authfragment.login.model.RememberMe

class SessionManagement(var context: Context) {
    var dialog: Dialog? = null
    private var editor: SharedPreferences.Editor? = null
    private var editor2: SharedPreferences.Editor? = null
    private var editorFirstTime: SharedPreferences.Editor? = null
    private var pref: SharedPreferences? = null
    private var pref2: SharedPreferences? = null
    private var prefFirstTime: SharedPreferences? = null
    private val gson = Gson()


    init {
        pref = context.getSharedPreferences(AppConstant.LOGIN_SESSION, Context.MODE_PRIVATE)
        editor = pref?.edit()

        pref2 = context.getSharedPreferences(AppConstant.RememberMe, Context.MODE_PRIVATE)
        editor2 = pref2?.edit()


        prefFirstTime = context.getSharedPreferences(AppConstant.FirstTime, Context.MODE_PRIVATE)
        editorFirstTime = prefFirstTime?.edit()
    }


    fun getPreferences(): Boolean {
        return pref!!.getBoolean(AppConstant.Preferences, false)
    }

    fun getSubscriptionId(): String? {
        return pref!!.getString(AppConstant.SubscriptionId, "")
    }

    fun getPurchaseToken(): String? {
        return pref!!.getString(AppConstant.PurchaseToken, "")
    }

    fun getPlanType(): String? {
        return pref!!.getString(AppConstant.planType, "")
    }


    fun setPreferences(session: Boolean?) {
        editor!!.putBoolean(AppConstant.Preferences, session!!)
        editor!!.commit()
    }

    fun setSubscriptionId(session: String?) {
        editor!!.putString(AppConstant.SubscriptionId, session!!)
        editor!!.commit()
    }

    fun setPurchaseToken(session: String?) {
        editor!!.putString(AppConstant.PurchaseToken, session!!)
        editor!!.commit()
    }

    fun setPlanType(session: String?) {
        editor!!.putString(AppConstant.planType, session!!)
        editor!!.commit()
    }


    fun setRememberMe(value: RememberMe) {
        editor2!!.putString(AppConstant.rememberMe, Gson().toJson(value))
        editor2!!.apply()
    }


    fun getRememberMe(): String? {
        return pref2!!.getString(AppConstant.rememberMe, "")
    }

    fun getLoginSession(): Boolean {
        return pref!!.getBoolean(AppConstant.loginSession, false)
    }


    fun setFirstTime(status: Boolean) {
        editorFirstTime!!.putBoolean(AppConstant.SessionFirstTime, status)
        editorFirstTime!!.commit()
    }

    fun getFirstTime(): Boolean {
        return prefFirstTime!!.getBoolean(AppConstant.SessionFirstTime, true)
    }

    fun setLatitude(status: String) {
        editor!!.putString(AppConstant.Latitude, status)
        editor!!.commit()
    }

    fun getLatitude(): String? {
        return pref?.getString(AppConstant.Latitude, "")
    }

    fun setLongitude(status: String) {
        editor!!.putString(AppConstant.Longitude, status)
        editor!!.commit()
    }

    fun getLongitude(): String? {
        return pref!!.getString(AppConstant.Longitude, "")
    }


    fun setLoginSession(session: Boolean?) {
        editor!!.putBoolean(AppConstant.loginSession, session!!)
        editor!!.commit()
    }

    fun setUserName(name: String) {
        editor!!.putString(AppConstant.NAME, name)
        editor!!.commit()
    }

    fun setAddress(address: String) {
        editor!!.putString(AppConstant.Address, address)
        editor!!.commit()
    }

    fun setCookBookName(name: String) {
        editor!!.putString(AppConstant.CookBookName, name)
        editor!!.commit()
    }

    fun getProviderName(): String? {
        return pref?.getString(AppConstant.ProviderName, "")
    }


    fun setProviderName(name: String) {
        editor!!.putString(AppConstant.ProviderName, name)
        editor!!.commit()
    }

    fun getProviderImage(): String? {
        return pref?.getString(AppConstant.ProviderName, "")
    }


    fun setProviderImage(name: String) {
        editor!!.putString(AppConstant.ProviderImage, name)
        editor!!.commit()
    }

    fun setCookBookId(name: String) {
        editor!!.putString(AppConstant.CookBookId, name)
        editor!!.commit()
    }
    fun setOpenCookBookUsingShare(name: String) {
        editor!!.putString(AppConstant.CookBookUsingShare, name)
        editor!!.commit()
    }


    fun setCookBookImage(name: String) {
        editor!!.putString(AppConstant.CookBookImage, name)
        editor!!.commit()
    }

    fun setCookBookType(name: String) {
        editor!!.putString(AppConstant.CookBookType, name)
        editor!!.commit()
    }

    fun getCookBookName(): String? {
        return pref?.getString(AppConstant.CookBookName, "")
    }

    fun getCookBookShare():String?{
        return  pref?.getString(AppConstant.CookBookUsingShare,"")
    }

    fun getCookBookId(): String? {
        return pref?.getString(AppConstant.CookBookId, "")
    }

    fun getCookBookImage(): String? {
        return pref?.getString(AppConstant.CookBookImage, "")
    }

    fun getCookBookType(): String? {
        return pref?.getString(AppConstant.CookBookType, "")
    }

    fun getUserName(): String? {
        return pref?.getString(AppConstant.NAME, "")
    }

    fun getUserAddress(): String? {
        return pref?.getString(AppConstant.Address, "")
    }


    fun setGender(gender: String) {
        editor!!.putString(AppConstant.Gender, gender)
        editor!!.commit()
    }

    fun getGender(): String? {
        return pref?.getString(AppConstant.Gender, "")
    }

    fun setBodyGoal(bogyGoal: String) {
        editor!!.putString(AppConstant.BodyGoal, bogyGoal)
        editor!!.commit()
    }

    fun getBodyGoal(): String? {
        return pref?.getString(AppConstant.BodyGoal, "")
    }

    fun setPartnerName(partnerName: String) {
        editor!!.putString(AppConstant.PartnerName, partnerName)
        editor!!.commit()
    }

    fun getPartnerName(): String? {
        return pref?.getString(AppConstant.PartnerName, "")
    }


    fun setReferralCode(referralCode: String) {
        editor!!.putString(AppConstant.ReferralCode, referralCode)
        editor!!.commit()
    }

    fun getReferralCode(): String? {
        return pref?.getString(AppConstant.ReferralCode, "")
    }

    fun setPartnerAge(partnerAge: String) {
        editor!!.putString(AppConstant.PartnerAge, partnerAge)
        editor!!.commit()
    }

    fun getPartnerAge(): String? {
        return pref?.getString(AppConstant.PartnerAge, "")
    }

    fun setPartnerGender(partnerGender: String) {
        editor!!.putString(AppConstant.PartnerGender, partnerGender)
        editor!!.commit()
    }

    fun getPartnerGender(): String? {
        return pref?.getString(AppConstant.PartnerGender, "")
    }

    fun setFamilyMemName(familyMemName: String) {
        editor!!.putString(AppConstant.FamilyMemName, familyMemName)
        editor!!.commit()
    }

    fun getFamilyMemName(): String? {
        return pref?.getString(AppConstant.FamilyMemName, "")
    }

    fun setFamilyMemAge(familyMemAge: String) {
        editor!!.putString(AppConstant.FamilyMemAge, familyMemAge)
        editor!!.commit()
    }

    fun getFamilyMemAge(): String? {
        return pref?.getString(AppConstant.FamilyMemAge, "")
    }

    fun setFamilyStatus(familyStatus: String) {
        editor!!.putString(AppConstant.FamilyStatus, familyStatus)
        editor!!.commit()
    }

    fun getFamilyStatus(): String? {
        return pref?.getString(AppConstant.FamilyStatus, "")
    }

    fun setDietaryRestrictionList(dietaryList: MutableList<String>?) {
        val json = gson.toJson(dietaryList)
        editor!!.putString(AppConstant.DietaryRestriction, json)
        editor!!.commit()
    }

    fun getDietaryRestrictionList(): MutableList<String>? {
        val json = pref?.getString(AppConstant.DietaryRestriction, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun setFavouriteCuisineList(favouriteList: MutableList<String>?) {
        val json = gson.toJson(favouriteList)
        editor!!.putString(AppConstant.FavouriteCuisine, json)
        editor!!.commit()
    }

    fun getFavouriteCuisineList(): MutableList<String>? {
        val json = pref?.getString(AppConstant.FavouriteCuisine, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun setDislikeIngredientList(dislikeIngList: MutableList<String>?) {
        val json = gson.toJson(dislikeIngList)
        editor!!.putString(AppConstant.DislikeIngredient, json)
        editor!!.commit()
    }

    fun getDislikeIngredientList(): MutableList<String>? {
        val json = pref?.getString(AppConstant.DislikeIngredient, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun setAllergenIngredientList(allergenIngList: MutableList<String>?) {
        val json = gson.toJson(allergenIngList)
        editor!!.putString(AppConstant.AllergenIngredient, json)
        editor!!.commit()
    }

    fun getAllergenIngredientList(): MutableList<String>? {
        val json = pref?.getString(AppConstant.AllergenIngredient, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun setMealRoutineList(mealRoutineList: MutableList<String>?) {
        val json = gson.toJson(mealRoutineList)
        editor!!.putString(AppConstant.MealRoutine, json)
        editor!!.commit()
    }

    fun getMealRoutineList(): MutableList<String>? {
        val json = pref?.getString(AppConstant.MealRoutine, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }


    fun setCookingFrequency(cookingFrequency: String) {
        editor!!.putString(AppConstant.CookingFrequency, cookingFrequency)
        editor!!.commit()
    }

    fun getCookingFrequency(): String? {
        return pref?.getString(AppConstant.CookingFrequency, "")
    }

    fun setSpendingAmount(spendingAmount: String) {
        editor!!.putString(AppConstant.SpendingAmount, spendingAmount)
        editor!!.commit()
    }

    fun getSpendingAmount(): String? {
        return pref?.getString(AppConstant.SpendingAmount, "")
    }

    fun setSpendingDuration(spendingDuration: String) {
        editor!!.putString(AppConstant.SpendingDuration, spendingDuration)
        editor!!.commit()
    }

    fun getSpendingDuration(): String? {
        return pref?.getString(AppConstant.SpendingDuration, "")
    }

    fun setEatingOut(eatingOut: String) {
        editor!!.putString(AppConstant.EatingOut, eatingOut)
        editor!!.commit()
    }


    fun getEatingOut(): String? {
        return pref?.getString(AppConstant.EatingOut, "")
    }

    fun setReasonTakeAway(reasonAway: String) {
        editor!!.putString(AppConstant.ReasonForTakeAway, reasonAway)
        editor!!.commit()
    }

    fun getReasonTakeAway(): String? {
        return pref?.getString(AppConstant.ReasonForTakeAway, "")
    }

    fun setReasonTakeAwayDesc(reasonAway: String) {
        editor!!.putString(AppConstant.ReasonForTakeAwyDesc, reasonAway)
        editor!!.commit()
    }

    fun getReasonTakeAwayDesc(): String? {
        return pref?.getString(AppConstant.ReasonForTakeAwyDesc, "")
    }


    fun setEmail(email: String) {
        editor!!.putString(AppConstant.EMAIL, email)
        editor!!.commit()
    }

    fun getEmail(): String? {
        return pref?.getString(AppConstant.EMAIL, "")
    }

    fun setPhone(phone: String) {
        editor!!.putString(AppConstant.PHONE, phone)
        editor!!.commit()
    }

    fun getPhone(): String? {
        return pref?.getString(AppConstant.PHONE, "")
    }

    fun setImage(name: String) {
        editor!!.putString(AppConstant.Image, name)
        editor!!.commit()
    }

    fun getImage(): String? {
        return pref?.getString(AppConstant.Image, "")
    }

    fun setId(id: String) {
        editor!!.putString(AppConstant.Id, id)
        editor!!.commit()
    }

    fun getId(): String? {
        return pref?.getString(AppConstant.Id, "")
    }

    fun setAuthToken(name: String) {
        editor!!.putString(AppConstant.authToken, name)
        editor!!.commit()
    }

    fun getAuthToken(): String? {
        return pref?.getString(AppConstant.authToken, "")
    }

    fun setCookingFor(cookingFor: String) {
        editor!!.putString(AppConstant.cookingFor, cookingFor)
        editor!!.commit()
    }

    fun getCookingFor(): String? {
        return pref?.getString(AppConstant.cookingFor, "")
    }

    fun setCookingScreen(cookingFor: String) {
        editor!!.putString(AppConstant.cookingScreen, cookingFor)
        editor!!.commit()
    }

    fun getCookingScreen(): String? {
        return pref?.getString(AppConstant.cookingScreen, "")
    }

    fun sessionClear() {
        editor?.apply()
        editor?.clear()
        editor?.commit()

    }


}