package com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsebydate.DataPlayByDate
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {



    private var _data: Data? = null
    private var _dataPlan: DataPlayByDate? = null
    private var _date: String? = null


    val data: Data? get() = _data
    val dataPlan: DataPlayByDate? get() = _dataPlan
    val date: String? get() = _date



    suspend fun planRequest(successCallback: (response: NetworkResult<String>) -> Unit, q: String){
        repository.planRequestApi({ successCallback(it) },q)
    }

    suspend fun planDateRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                            date: String,planType:String){
        repository.planDateRequestApi({ successCallback(it) },date,planType)
    }

    suspend fun recipeSwapUrl(successCallback: (response: NetworkResult<String>) -> Unit,
                            id: String?,uri:String?){
        repository.recipeSwapUrl({ successCallback(it) },id,uri)
    }


    suspend fun likeUnlikeRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                    uri: String,likeType: String,type:String){
        repository.likeUnlikeRequestApi({ successCallback(it) },uri,likeType,type)
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


    suspend fun updateMealRoutineApi(successCallback: (response: NetworkResult<String>) -> Unit,mealRoutineId: List<String>?){
        repository.updateMealRoutineApi ({ successCallback(it) },mealRoutineId)
    }

    suspend fun recipeServingCountRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.recipeAddToPlanRequestApi({ successCallback(it) },jsonObject)
    }


    suspend fun addDayToBasketAllUrl(successCallback: (response: NetworkResult<String>) -> Unit, date:String?
    ){
        repository.addToBasketAllUrl({ successCallback(it) },date)
    }



    fun setData(data: Data?){
        _data=data
    }


    fun setPlanDate(data: DataPlayByDate?){
        _dataPlan=data
    }


    fun setDate(data: String?){
        _date=data
    }



}