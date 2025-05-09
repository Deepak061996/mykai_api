package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model.CheckoutScreenModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsGraphModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsWeekYearModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {


    suspend fun generateLinkUrl(successCallback: (response: NetworkResult<String>) -> Unit, link: RequestBody?, image: MultipartBody.Part?) {
        repository.generateLinkUrl({ successCallback(it) }, link, image)
    }

    suspend fun addBasketRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                 uri: String,quantity: String,type:String){
        repository.addBasketRequestApi({ successCallback(it) },uri,quantity,type)
    }

    suspend fun recipeAddToPlanRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.recipeAddToPlanRequestApi({ successCallback(it) },jsonObject)
    }

    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookBookRequestApi { successCallback(it) }
    }

    suspend fun likeUnlikeRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                  uri: String,likeType: String,type:String){
        repository.likeUnlikeRequestApi({ successCallback(it) },uri,likeType,type)
    }

    suspend fun getGraphScreenUrl(successCallback: (response: NetworkResult<String>) -> Unit, month: String?, year: String?) {
        repository.getGraphScreenUrl({ successCallback(it) }, month,year)
    }

    suspend fun referralUrl(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.referralUrl{ successCallback(it)}
    }

    suspend fun orderWeekUrl(successCallback: (response: NetworkResult<String>) -> Unit, start_date: String?,end_date:String?,year:String?) {
        repository.orderWeekUrl({ successCallback(it) }, start_date,end_date,year)
    }


    // hold data in view model

    private var _dataGraph: StatisticsGraphModelData? = null
    private var _dataCurrentMonth: String? = null
    private var _dataCurrentYear: String? = null
    private var _dataWeekOfMonth: String? = null
    private var _currentDate: Date? = null
    private var _currentDateList: Date? = null
    val dataGraph: StatisticsGraphModelData? get() = _dataGraph
    val dataCurrentMonth: String? get() = _dataCurrentMonth
    val dataCurrentYear: String? get() = _dataCurrentYear
    val dataWeekOfMonth: String? get() = _dataWeekOfMonth
    val currentDate: Date? get() = _currentDate
    val currentDateList: Date? get() = _currentDateList

    fun setGraphData(data: StatisticsGraphModelData?,currentMonth:String?,year:String?,weekOfMonth:String?,currentDate: Date?){
        _dataGraph=data
        _dataCurrentMonth=currentMonth
        _dataCurrentYear=year
        _dataWeekOfMonth=weekOfMonth
        _currentDate=currentDate
    }



    private var _dataGraphDataList: StatisticsWeekYearModelData? = null
    val dataGraphDataList: StatisticsWeekYearModelData? get() = _dataGraphDataList

    fun setGraphDataList(data: StatisticsWeekYearModelData?,currentDate: Date?){
        _dataGraphDataList=data
        _currentDateList=currentDate
    }




}