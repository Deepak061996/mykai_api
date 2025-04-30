package com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.BasketScreenModelData
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model.OrderHistoryModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel@Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun orderHistoryUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.orderHistoryUrl{ successCallback(it) }
    }

    // set Data in view model

    private var _dataOrderHistory: MutableList<OrderHistoryModelData>? = null

    val dataOrderHistory: MutableList<OrderHistoryModelData>? get() = _dataOrderHistory

    fun setOrderHistoryData(data: MutableList<OrderHistoryModelData>?){
        _dataOrderHistory=data
    }
}