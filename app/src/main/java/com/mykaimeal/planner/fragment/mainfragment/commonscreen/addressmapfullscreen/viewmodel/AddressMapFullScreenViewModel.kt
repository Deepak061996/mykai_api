package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addressmapfullscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddressMapFullScreenViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    suspend fun addAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit, latitude: String?, longitude: String?,
                              streetName:String?,streetNum:String?,apartNum:String?,city:String?,state:String?,country:String?,
                            zipcode:String?,primary:String?,id:String?,type:String?) {
        repository.addAddressUrl({ successCallback(it) }, latitude, longitude, streetName, streetNum, apartNum, city,state, country, zipcode, primary, id, type)
    }

}