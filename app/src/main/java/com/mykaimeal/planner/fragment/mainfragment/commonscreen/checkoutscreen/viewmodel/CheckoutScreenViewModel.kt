package com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CheckoutScreenViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getCheckoutScreenUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCheckoutScreenUrl{ successCallback(it) }
    }

    suspend fun getAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getAddressUrl{ successCallback(it) }
    }

    suspend fun addAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit, latitude: String?, longitude: String?,
                              streetName:String?,streetNum:String?,apartNum:String?,city:String?,state:String?,country:String?,
                              zipcode:String?,primary:String?,id:String?,type:String?) {
        repository.addAddressUrl({ successCallback(it) }, latitude, longitude, streetName, streetNum, apartNum, city,state, country, zipcode, primary, id, type)
    }

    suspend fun makeAddressPrimaryUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?) {
        repository.makeAddressPrimaryUrl({ successCallback(it) },id)
    }
}