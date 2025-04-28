package com.mykaimeal.planner.fragment.commonfragmentscreen.eatingOut.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EatingOutViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var eatingOutLocalData: MutableList<BodyGoalModelData>?=null

    suspend fun getEatingOut(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getEatingOut { successCallback(it) }
    }

    fun setEatingOutData(data: MutableList<BodyGoalModelData>) {
        eatingOutLocalData=data
    }

    // Method to clear data
    fun clearData() {
        eatingOutLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getEatingOutData(): MutableList<BodyGoalModelData>? {
        return eatingOutLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }

    suspend fun updateEatingOutApi(successCallback: (response: NetworkResult<String>) -> Unit,eatingOut: String?){
        repository.updateEatingOutApi ({ successCallback(it) },eatingOut)
    }

}