package com.mykaimeal.planner.fragment.mainfragment.profilesetting.feedbackscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel

class FeedbackViewModel@Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun saveFeedback(successCallback: (response: NetworkResult<String>) -> Unit, email: String, message:String){
        repository.saveFeedback({ successCallback(it) }, email,message)
    }

}