package com.mykaimeal.planner.fragment.mainfragment.commonscreen.missingingredientbasket.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MissingIngredientBasketViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    suspend fun getMissingIngBasketUrl(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.getMissingIngBasketUrl { successCallback(it) }
    }

    suspend fun addToCartUrlApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        foodIds: MutableList<String>?,
        schId: String?,
        foodName: MutableList<String>?,
        status: MutableList<String>?
    ) {
        repository.addToCartUrlApi({ successCallback(it) }, foodIds, schId, foodName, status)
    }

}
