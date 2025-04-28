package com.mykaimeal.planner.fragment.mainfragment.commonscreen.addtipscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddTipScreenViewModel@Inject constructor(private val repository: MainRepository) : ViewModel()  {

      suspend fun getOrderProductUrl(successCallback: (response: NetworkResult<String>) -> Unit){
          repository.getOrderProductUrl{ successCallback(it) }
      }

}