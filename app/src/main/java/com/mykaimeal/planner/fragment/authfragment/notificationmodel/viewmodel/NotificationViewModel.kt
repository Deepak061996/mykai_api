package com.mykaimeal.planner.fragment.authfragment.notificationmodel.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(private val repository: MainRepository) :
    ViewModel() {

    suspend fun updateNotification(
        successCallback: (response: NetworkResult<String>) -> Unit,
        notificationStatus: String
    ) {
        repository.updateNotification({ successCallback(it) }, notificationStatus)
    }
}