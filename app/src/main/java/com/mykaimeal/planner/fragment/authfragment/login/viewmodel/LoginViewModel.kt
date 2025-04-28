package com.mykaimeal.planner.fragment.authfragment.login.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun userLogin(successCallback: (response: NetworkResult<String>) -> Unit, emailOrPhone: String,password:String,deviceType:String,fcmToken:String){
        repository.userLogin({ successCallback(it) }, emailOrPhone,password,deviceType, fcmToken)
    }

    suspend fun socialLogin(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String?,
        socialId: String?,
        userName: String?,
        userGender: String?,
        bodyGoal: String?,
        cookingFrequency: String?,
        eatingOut: String?,
        takeAway: String?,
        takeWayName: String?,
        cookingForType: String?,
        partnerName: String?,
        partnerAge: String?,
        partnerGender: String?,
        familyMemberName: String?,
        familyMemberAge: String?,
        childFriendlyMeals: String?,
        mealRoutineId: List<String>?,
        spendingAmount: String?,
        duration: String?,
        dietaryId: List<String>?,favourite:List<String>?, allergies:List<String>?,
        dislikeIngredients: List<String>?,
        deviceType: String?,
        fcmToken: String?,
        referralFrom: String?
    ) {
        repository.socialLogin({ successCallback(it) }, emailOrPhone, socialId, userName, userGender, bodyGoal, cookingFrequency,eatingOut, takeAway,takeWayName,
            cookingForType, partnerName,partnerAge, partnerGender, familyMemberName, familyMemberAge, childFriendlyMeals, mealRoutineId, spendingAmount,
            duration, dietaryId,favourite, allergies, dislikeIngredients, deviceType, fcmToken,referralFrom
        )
    }

}