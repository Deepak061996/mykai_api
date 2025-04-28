package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketproductsdetailsscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BasketProductsDetailsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    suspend fun getProductsUrl(successCallback: (response: NetworkResult<String>) -> Unit,query:String?,foodId:String?,schId:String?){
        repository.getProductsUrl({ successCallback(it) },query,foodId,schId)
    }


    suspend fun getProductsDetailsUrl(successCallback: (response: NetworkResult<String>) -> Unit,proId:String?,query:String?,foodId:String?,schId:String?){
        repository.getProductsDetailsUrl({ successCallback(it) },proId,query,foodId,schId)
    }

    suspend fun getSelectProductsUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?,productId:String?,schId:String?){
        repository.getSelectProductsUrl({ successCallback(it) },id, productId,schId)
    }

    suspend fun basketIngIncDescUrl(successCallback: (response: NetworkResult<String>) -> Unit,
                                    foodId: String?,quantity:String?){
        repository.basketIngIncDescUrl({ successCallback(it) },foodId, quantity)
    }

}