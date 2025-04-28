package com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.dietaryRestrictions.model.DietaryRestrictionsModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DietaryRestrictionsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var dietaryRestLocalData: MutableList<DietaryRestrictionsModelData>?=null

    suspend fun getDietaryRestrictions(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getDietaryRestrictions { successCallback(it) }
    }

    fun setDietaryResData(data: MutableList<DietaryRestrictionsModelData>) {
        dietaryRestLocalData=data
    }

    // Method to clear data
    fun clearData() {
        dietaryRestLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getDietaryResData(): MutableList<DietaryRestrictionsModelData>? {
        return dietaryRestLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }

    suspend fun updateDietaryApi(successCallback: (response: NetworkResult<String>) -> Unit,dietaryId: List<String>?){
        repository.updateDietaryApi ({ successCallback(it) },dietaryId)
    }

}