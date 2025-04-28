package com.mykaimeal.planner.fragment.authfragment.forgotpassword.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun forgotPassword(successCallback: (response: NetworkResult<String>) -> Unit, emailOrPhone: String){
        repository.forgotPassword({ successCallback(it) }, emailOrPhone)
    }

}