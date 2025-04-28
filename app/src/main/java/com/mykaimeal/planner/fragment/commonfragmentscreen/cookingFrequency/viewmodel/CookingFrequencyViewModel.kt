package com.mykaimeal.planner.fragment.commonfragmentscreen.cookingFrequency.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CookingFrequencyViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var cookingFreqLocalData: MutableList<BodyGoalModelData>?=null

    suspend fun getCookingFrequency(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookingFrequency { successCallback(it) }
    }

    fun setCookingFreqData(data: MutableList<BodyGoalModelData>) {
        cookingFreqLocalData=data
    }

    // Method to clear data
    fun clearData() {
        cookingFreqLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getCookingFreqData(): MutableList<BodyGoalModelData>? {
        return cookingFreqLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }


    suspend fun updateCookingFrequencyApi(successCallback: (response: NetworkResult<String>) -> Unit,bodyGoal: String){
        repository.updateCookingFrequencyApi ({ successCallback(it) },bodyGoal)
    }

}