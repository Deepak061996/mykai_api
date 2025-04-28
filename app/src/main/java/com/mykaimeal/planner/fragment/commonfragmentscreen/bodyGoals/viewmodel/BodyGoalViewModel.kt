package com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BodyGoalViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var bodyGoalLocalData: MutableList<BodyGoalModelData>?=null

    suspend fun getBodyGoal(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.bogyGoal { successCallback(it) }
    }

    fun setBodyGoalData(data: MutableList<BodyGoalModelData>) {
        bodyGoalLocalData=data
    }

    // Method to clear data
    fun clearData() {
        bodyGoalLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getBodyGoalData(): MutableList<BodyGoalModelData>? {
        return bodyGoalLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }

    suspend fun updateBodyGoalApi(successCallback: (response: NetworkResult<String>) -> Unit,bodyGoal: String){
        repository.updateBodyGoalApi ({ successCallback(it) },bodyGoal)
    }

}