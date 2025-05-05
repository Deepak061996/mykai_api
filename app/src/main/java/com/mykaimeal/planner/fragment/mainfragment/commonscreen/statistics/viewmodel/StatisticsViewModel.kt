package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {


    suspend fun generateLinkUrl(successCallback: (response: NetworkResult<String>) -> Unit, link: RequestBody?, image: MultipartBody.Part?) {
        repository.generateLinkUrl({ successCallback(it) }, link, image)
    }

    suspend fun getGraphScreenUrl(successCallback: (response: NetworkResult<String>) -> Unit, month: String?) {
        repository.getGraphScreenUrl({ successCallback(it) }, month)
    }

    suspend fun referralUrl(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.referralUrl{ successCallback(it)}
    }

    suspend fun orderWeekUrl(successCallback: (response: NetworkResult<String>) -> Unit, week: String?,month:String?) {
        repository.orderWeekUrl({ successCallback(it) }, week,month)
    }

}