package com.mykaimeal.planner.fragment.authfragment.locationfragment.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(private val repository: MainRepository) :
    ViewModel() {

    suspend fun updateLocation(
        successCallback: (response: NetworkResult<String>) -> Unit,
        locationStatus: String
    ) {
        repository.updateLocation({ successCallback(it) }, locationStatus)
    }

    suspend fun addAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit, latitude: String?, longitude: String?,
                              streetName:String?, streetNum:String?, apartNum:String?, city:String?, state:String?, country:String?,
                              zipcode:String?, primary:String?, id:String?, type:String?) {
        repository.addAddressUrl({ successCallback(it) }, latitude, longitude, streetName, streetNum, apartNum, city,state, country, zipcode, primary, id, type)
    }

}