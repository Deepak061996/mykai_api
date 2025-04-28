package com.mykaimeal.planner.fragment.commonfragmentscreen.spendingOnGroceries.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GrocereisExpenses
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpendingGroceriesViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    private var groceriesLocalData: GrocereisExpenses?=null

    fun setGroceriesData(data: GrocereisExpenses) {
        groceriesLocalData=data
    }

    // Method to clear data
    fun clearData() {
        groceriesLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getGroceriesData(): GrocereisExpenses? {
        return groceriesLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.userPreferencesApi { successCallback(it) }
    }

    suspend fun updateSpendingGroceriesApi(successCallback: (response: NetworkResult<String>) -> Unit,spendingAmount: String?,duration: String?){
        repository.updateSpendingGroceriesApi ({ successCallback(it) },spendingAmount,duration)
    }

}
