package com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class DislikeIngredientsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var dislikeIngLocalData: MutableList<DislikedIngredientsModelData>?=null
    private var value: String=""

    suspend fun getDislikeIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?){
        repository.getDislikeIngredients({ successCallback(it) },itemCount)
    }

    suspend fun getDislikeSearchIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?,type:String){
        repository.getDislikeSearchIngredients({ successCallback(it) },itemCount,type)
    }

    fun setDislikeIngData(data: MutableList<DislikedIngredientsModelData>,value:String) {
        dislikeIngLocalData=data
        this.value=value
    }

    // Method to clear data
    fun clearData() {
        dislikeIngLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getDislikeIngData(): MutableList<DislikedIngredientsModelData>? {
        return dislikeIngLocalData
    }

    fun getEditStatus(): String? {
        return value
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }

    suspend fun userPreferencesDislikeApi(successCallback: (response: NetworkResult<String>) -> Unit,dislikeSearch:String?, dislikeum:String?){
        repository.userPreferencesDislikeApi({ successCallback(it) },dislikeSearch,dislikeum)
    }

    suspend fun updateDislikedIngredientsApi(successCallback: (response: NetworkResult<String>) -> Unit,dislikeId: List<String>?){
        repository.updateDislikedIngredientsApi ({ successCallback(it) },dislikeId)
    }

}