package com.mykaimeal.planner.fragment.commonfragmentscreen.familyinfoscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.FamilyDetail
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FamilyMemberInfoViewModel@Inject constructor(private val repository: MainRepository) : ViewModel() {

    private var familyLocalData: FamilyDetail?=null

    fun setFamilyData(data: FamilyDetail) {
        familyLocalData=data
    }

    // Method to clear data
    fun clearData() {
        familyLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getFamilyData(): FamilyDetail? {
        return familyLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.userPreferencesApi { successCallback(it) }
    }

    suspend fun updatePartnerInfoApi(successCallback: (response: NetworkResult<String>) -> Unit, familyName: String?, familyAge: String?, familyStatus:String?){
        repository.updateFamilyInfoApi ({ successCallback(it) },familyName,familyAge,familyStatus)
    }

}