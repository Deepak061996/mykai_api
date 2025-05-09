package com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class RecipeDetailsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {


    private var localData: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.Data>?=null

    suspend fun recipeDetailsRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                    url: String){
        repository.recipeDetailsRequestApi({ successCallback(it) },url)
    }


    suspend fun recipeAddBasketRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.recipeAddBasketRequestApi({ successCallback(it) },jsonObject)
    }

    suspend fun recipeAddToPlanRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.recipeAddToPlanRequestApi({ successCallback(it) },jsonObject)
    }


    suspend fun addMealTypeApiUrl(successCallback: (response: NetworkResult<String>) -> Unit, uri: String?, planType:String?,mealType:String?
    ){
        repository.addMealTypeApiUrl({ successCallback(it) },uri, planType,mealType)
    }


    fun setRecipeData(data: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.Data>) {
        localData=data
    }


    fun getRecipeData(): MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.Data>? {
        return localData
    }


    suspend fun recipeReviewRequest(successCallback: (response: NetworkResult<String>) -> Unit, url: String, msg: String,ratingBarcount:String){
        repository.recipeReviewRequestApi({ successCallback(it) },url,msg,ratingBarcount)
    }

}