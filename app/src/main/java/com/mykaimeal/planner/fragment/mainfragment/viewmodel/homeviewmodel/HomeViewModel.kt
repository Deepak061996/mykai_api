package com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.DataModel
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var _data: DataModel? = null
    val data: DataModel? get() = _data

    suspend fun homeDetailsRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.homeDetailsRequestApi{ successCallback(it) }
    }

    suspend fun superMarketSaveRequest(successCallback: (response: NetworkResult<String>) -> Unit,uuid:String?,storeName:String?){
        repository.superMarketSaveRequest({ successCallback(it) },uuid,storeName)
    }

    suspend fun likeUnlikeRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                  uri: String,likeType: String,type:String){
        repository.likeUnlikeRequestApi({ successCallback(it) },uri,likeType,type)
    }

    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookBookRequestApi { successCallback(it) }
    }

    suspend fun getSuperMarket(successCallback: (response: NetworkResult<String>) -> Unit,
                               latitude: String?,longitude: String?){
        repository.getSuperMarket({ successCallback(it) },latitude,longitude)
    }

    // set Home Data
    fun setData(dataItem:DataModel?) {
        _data = dataItem
    }


}