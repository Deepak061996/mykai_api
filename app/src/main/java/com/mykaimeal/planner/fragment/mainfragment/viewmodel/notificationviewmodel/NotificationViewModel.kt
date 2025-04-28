package com.mykaimeal.planner.fragment.mainfragment.viewmodel.notificationviewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class NotificationViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {


    suspend fun notificationRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                    pushNotification: String,recipeRecommendations: String,productUpdates: String,promotionalUpdates: String){
        repository.notificationRequestApi({ successCallback(it) },pushNotification,recipeRecommendations,productUpdates,promotionalUpdates)
    }



}