package com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel@Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun orderHistoryUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.orderHistoryUrl{ successCallback(it) }
    }

}