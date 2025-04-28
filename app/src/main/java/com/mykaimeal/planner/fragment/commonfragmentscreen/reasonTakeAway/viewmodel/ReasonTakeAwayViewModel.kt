package com.mykaimeal.planner.fragment.commonfragmentscreen.reasonTakeAway.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class ReasonTakeAwayViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var reasonTakeLocalData: MutableList<BodyGoalModelData>?=null

    suspend fun getTakeAwayReason(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getTakeAwayReason { successCallback(it) }
    }

    fun setReasonTakeData(data: MutableList<BodyGoalModelData>) {
        reasonTakeLocalData=data
    }

    // Method to clear data
    fun clearData() {
        reasonTakeLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getReasonTakeData(): MutableList<BodyGoalModelData>? {
        return reasonTakeLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }

    suspend fun updateReasonTakeAwayApi(successCallback: (response: NetworkResult<String>) -> Unit,reasonTakeAway: String?,take_way_name:String?) {
        repository.updateReasonTakeAwayApi({ successCallback(it) }, reasonTakeAway, take_way_name)
    }

    suspend fun updatePreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit,userName: String?,cookingForType:String?,userGender:String?,bodygoal:String?,partner_name:String?,partner_age:String?,partner_gender:String?,family_member_name:String?,
                                        family_member_age:String?,child_friendly_meals:String?,dietaryId: MutableList<String>?,favouriteId: MutableList<String>?,
                                        dislikeIngId: MutableList<String>?,allergensId: MutableList<String>?,mealRoutineId: MutableList<String>?,cookingFrequency: String?,
                                        spendingAmount: String?,duration: String?,eatingOut: String?,takeWay: String?,takeWayName: String?){
        repository.updatePreferencesApi ({ successCallback(it) },userName,cookingForType,userGender,bodygoal,partner_name,partner_age,partner_gender,family_member_name,
            family_member_age,child_friendly_meals,dietaryId,favouriteId,dislikeIngId, allergensId, mealRoutineId,
            cookingFrequency, spendingAmount, duration, eatingOut, takeWay,takeWayName)
    }

}