package com.mykaimeal.planner.fragment.mainfragment.profilesetting.terms_condition.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TermsConditionViewModel @Inject constructor(private val repository: MainRepository) :
    ViewModel() {

    suspend fun getTermCondition(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.termCondition { successCallback(it) }
    }

}