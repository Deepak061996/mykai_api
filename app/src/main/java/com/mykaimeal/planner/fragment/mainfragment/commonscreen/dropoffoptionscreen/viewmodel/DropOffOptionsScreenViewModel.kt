package com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DropOffOptionsScreenViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun addNotesUrl(successCallback: (response: NetworkResult<String>) -> Unit,pickup:String?,description:String?){
        repository.addNotesUrl({ successCallback(it) },pickup,description)
    }

    suspend fun getNotesUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getNotesUrl{ successCallback(it) }
    }

}