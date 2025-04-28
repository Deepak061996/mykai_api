package com.mykaimeal.planner.fragment.authfragment.verification.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    suspend fun signUpOtpVerify(
        successCallback: (response: NetworkResult<String>) -> Unit,
        userid: String?,
        otp: String?,
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
        dietaryid: List<String>?,favourite:List<String>?, allergies:List<String>?,
        dislikeIngredients: List<String>?,
        deviceType: String?,
        fcmToken: String?,
        referralFrom: String?
    ) {
        repository.otpVerify(
            { successCallback(it) },
            userid,
            otp,
            userName,
            userGender,
            bodyGoal,
            cookingFrequency,
            eatingOut,
            takeAway,takeWayName,
            cookingForType,
            partnerName,
            partnerAge,
            partnerGender,
            familyMemberName,
            familyMemberAge,
            childFriendlyMeals,
            mealRoutineId,
            spendingAmount,
            duration,
            dietaryid,favourite,
            allergies,
            dislikeIngredients,
            deviceType,
            fcmToken,
            referralFrom
        )
    }

    suspend fun forgotOtpVerify(
        successCallback: (response: NetworkResult<String>) -> Unit, emailOrPhone: String, otp: String) {
        repository.forgotOtpVerify({ successCallback(it) }, emailOrPhone, otp)
    }

    suspend fun forgotPassword(successCallback: (response: NetworkResult<String>) -> Unit, emailOrPhone: String){
        repository.forgotPassword({ successCallback(it) }, emailOrPhone)
    }

    suspend fun resendSignUpModel(successCallback: (response: NetworkResult<String>) -> Unit, emailOrPhone: String){
        repository.resendSignUpModel({ successCallback(it) }, emailOrPhone)
    }


}