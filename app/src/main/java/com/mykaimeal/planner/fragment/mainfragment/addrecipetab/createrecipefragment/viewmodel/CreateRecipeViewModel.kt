package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateRecipeViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {
    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookBookRequestApi { successCallback(it) }
    }

    suspend fun recipeSearchApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch: String?){
        repository.createRecipeUrlApi ({ successCallback(it) },itemSearch)
    }

    suspend fun createRecipeRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.createRecipeRequestApi({ successCallback(it) },jsonObject)
    }




}