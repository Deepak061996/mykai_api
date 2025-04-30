package com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class MealRoutineViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var mealRoutineLocalData: MutableList<MealRoutineModelData>?=null

    suspend fun getMealRoutine(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getMealRoutine { successCallback(it) }
    }


    fun setMealRoutineData(data: MutableList<MealRoutineModelData>) {
        mealRoutineLocalData=data
    }

    // Method to clear data
    fun clearData() {
        mealRoutineLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getMealRoutineData(): MutableList<MealRoutineModelData>? {
        return mealRoutineLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }


    suspend fun userSubscriptionCountApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userSubscriptionCountApi{ successCallback(it) }
    }

    suspend fun updateMealRoutineApi(successCallback: (response: NetworkResult<String>) -> Unit,mealRoutineId: List<String>?){
        repository.updateMealRoutineApi ({ successCallback(it) },mealRoutineId)
    }
  suspend fun updateCookBookApi(successCallback: (response: NetworkResult<String>) -> Unit,cookBookID: String?){
        repository.updateCookBookApi ({ successCallback(it) },cookBookID)
    }

    suspend fun planRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                            q: String){
        repository.planRequestApi({ successCallback(it) },q)
    }

    suspend fun addBasketRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                 uri: String,quantity: String,type: String){
        repository.addBasketRequestApi({ successCallback(it) },uri,quantity,type)
    }

    suspend fun recipeAddToPlanRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.recipeAddToPlanRequestApi({ successCallback(it) },jsonObject)
    }

    suspend fun likeUnlikeRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                  uri: String,likeType: String,type:String){
        repository.likeUnlikeRequestApi({ successCallback(it) },uri,likeType,type)
    }

    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookBookRequestApi { successCallback(it) }
    }

    suspend fun getMealByUrl(successCallback: (response: NetworkResult<String>) -> Unit,url:String?){
        repository.getMealByUrl({successCallback(it)},url)
    }

}