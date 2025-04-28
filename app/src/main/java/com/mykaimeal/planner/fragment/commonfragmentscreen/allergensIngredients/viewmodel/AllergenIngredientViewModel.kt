package com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.allergensIngredients.model.AllergensIngredientModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AllergenIngredientViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var allergensLocalData: MutableList<AllergensIngredientModelData>?=null
    private var value: String=""


    suspend fun getAllergensIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?){
        repository.getAllergensIngredients({ successCallback(it) },itemCount)
    }

    suspend fun getAllergensSearchIngredients(successCallback: (response: NetworkResult<String>) -> Unit,data:String,itemCount:String?,type:String){
        repository.getAllergensSearchIngredients({ successCallback(it) },data,itemCount,type)
    }

    fun setAllergensData(data: MutableList<AllergensIngredientModelData>,value:String) {
        allergensLocalData=data
        this.value=value
    }

    // Method to clear data
    fun clearData() {
        allergensLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getEditStatus(): String? {
        return value
    }

    fun getAllergensData(): MutableList<AllergensIngredientModelData>? {
        return allergensLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }

    suspend fun userPreferencesAllergiesApi(successCallback: (response: NetworkResult<String>) -> Unit,allergicSearch:String?,allergicNum:String?){
        repository.userPreferencesAllergiesApi({ successCallback(it) },allergicSearch,allergicNum)
    }

    suspend fun updateAllergiesApi(successCallback: (response: NetworkResult<String>) -> Unit,allergies: List<String>?){
        repository.updateAllergiesApi ({ successCallback(it) },allergies)
    }

}