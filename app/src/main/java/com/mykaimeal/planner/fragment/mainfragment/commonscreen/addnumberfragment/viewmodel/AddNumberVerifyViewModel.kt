package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addnumberfragment.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddNumberVerifyViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    suspend fun sendOtpUrl(
        successCallback: (response: NetworkResult<String>) -> Unit, phone: String?) {
        repository.sendOtpUrl({ successCallback(it) }, phone)
    }


    suspend fun addPhoneUrl(successCallback: (response: NetworkResult<String>) -> Unit, phone: String?, otp: String?,countryCode:String?) {
        repository.addPhoneUrl({ successCallback(it) }, phone, otp,countryCode)
    }

}