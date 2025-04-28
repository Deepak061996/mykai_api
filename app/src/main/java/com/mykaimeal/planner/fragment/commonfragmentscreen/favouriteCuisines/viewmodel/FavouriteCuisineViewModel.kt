package com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines.model.FavouriteCuisinesModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class FavouriteCuisineViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    private var favouriteCuiLocalData: MutableList<FavouriteCuisinesModelData>?=null

        suspend fun getFavouriteCuisines(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getFavouriteCuisines { successCallback(it) }
    }

    fun setFavouriteCuiData(data: MutableList<FavouriteCuisinesModelData>) {
        favouriteCuiLocalData=data
    }

    // Method to clear data
    fun clearData() {
        favouriteCuiLocalData = null // Clear LiveData
        // Reset other variables
    }

    fun getFavouriteCuiData(): MutableList<FavouriteCuisinesModelData>? {
        return favouriteCuiLocalData
    }

    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userPreferencesApi{ successCallback(it) }
    }

    suspend fun updateFavouriteApi(successCallback: (response: NetworkResult<String>) -> Unit,favouriteId: List<String>?){
        repository.updateFavouriteApi ({ successCallback(it) },favouriteId)
    }

}