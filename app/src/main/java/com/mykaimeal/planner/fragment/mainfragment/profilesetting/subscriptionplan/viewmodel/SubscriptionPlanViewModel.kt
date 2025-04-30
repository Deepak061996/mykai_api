package com.mykaimeal.planner.fragment.mainfragment.profilesetting.subscriptionplan.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SubscriptionPlanViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    suspend fun subscriptionGoogle(successCallback: (response: NetworkResult<String>) -> Unit,type: String?, purchaseToken: String?, subscriptionId:String?) {
        repository.subscriptionGoogle ({ successCallback(it) },type,purchaseToken,subscriptionId)
    }
    suspend fun updateCookBookApi(successCallback: (response: NetworkResult<String>) -> Unit,cookBookID: String?){
        repository.updateCookBookApi ({ successCallback(it) },cookBookID)
    }



    suspend fun subscriptionPurchaseType(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.subscriptionPurchaseType { successCallback(it) }
    }






}