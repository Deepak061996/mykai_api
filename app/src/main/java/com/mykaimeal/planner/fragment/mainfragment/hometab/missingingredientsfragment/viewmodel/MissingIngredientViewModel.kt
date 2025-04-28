package com.mykaimeal.planner.fragment.mainfragment.hometab.missingingredientsfragment.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.hometab.missingingredientsfragment.model.MissingIngredientModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MissingIngredientViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getMissingIngredientsApi(successCallback: (response: NetworkResult<String>) -> Unit, uri: String?,schId:String?){
        repository.getMissingIngredientsApi ({ successCallback(it) },uri, schId)
    }

    suspend fun addToCartUrlApi(successCallback: (response: NetworkResult<String>) -> Unit,foodIds: MutableList<String>?, schId:String?,
                                foodName:MutableList<String>?,status:MutableList<String>?){
        repository.addToCartUrlApi ({ successCallback(it) },foodIds,schId,foodName,status)
    }

}