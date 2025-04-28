package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BasketYourRecipeViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getYourRecipeUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getYourRecipeUrl{ successCallback(it) }
    }

    suspend fun removeBasketUrlApi(successCallback: (response: NetworkResult<String>) -> Unit,
                                   cookedId: String?){
        repository.removeBasketUrlApi({ successCallback(it) },cookedId)
    }


    suspend fun basketYourRecipeIncDescUrl(successCallback: (response: NetworkResult<String>) -> Unit,
                                           uri: String?,quantity:String?){
        repository.basketYourRecipeIncDescUrl({ successCallback(it) },uri, quantity)
    }

}