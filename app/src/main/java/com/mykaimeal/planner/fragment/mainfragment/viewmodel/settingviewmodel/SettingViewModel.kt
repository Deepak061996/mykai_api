package com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.Data
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
@HiltViewModel
class SettingViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {


    private var localData: Data?=null


    suspend fun userProfileData(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userProfileDataApi { successCallback(it) }
    }
    suspend fun userLogOutData(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userLogOutDataApi { successCallback(it) }
    }

    suspend fun userDeleteData(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.userDeleteDataApi { successCallback(it) }
    }



    fun setProfileData(data: Data) {
        localData=data
    }

    // Method to clear data
    fun clearData() {
        localData = null // Clear LiveData
        // Reset other variables
    }

    fun getProfileData(): Data? {
        return localData
    }


    suspend fun upDateProfileRequest(
        successCallback: (response: NetworkResult<String>) -> Unit,
        name: String
        , bio: String
        , genderType: String
        , dob: String
        , height: String
        , heightType: String,
        activityLevel: String
        , heightProtein: String
        , calories: String
        , fat: String
        , carbs: String
        , protien: String
        , weight: String
        , weightType: String
    ){
        repository.upDateProfileRequestApi({ successCallback(it) },  name,bio,genderType,dob,height,heightType,activityLevel,heightProtein,calories,fat,carbs,protien,weight,weightType)
    }


    suspend fun userProfileUpdateBioApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
         bio: String

    ){
        repository.userProfileUpdateBioApi({ successCallback(it) }, bio)
    }

    suspend fun upDateImageNameRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                      Image: MultipartBody.Part?,name: RequestBody){
        repository.upDateImageNameRequestApi({ successCallback(it) },  Image,name)
    }

    suspend fun updatePostCodeApi(successCallback: (response: NetworkResult<String>) -> Unit,
                                      postCode: String?,longitude:String?,latitude: String?){
        repository.updatePostCodeApi({ successCallback(it) },  postCode,longitude,latitude)
    }

    suspend fun updateDietSuggestionUrl(successCallback: (response: NetworkResult<String>) -> Unit,
                                        gender: String?, dob: String?, height: String?,
                                        heightType: String?, weight: String?, weightType: String?, activityLevel: String?){
        repository.updateDietSuggestionUrl({ successCallback(it) },gender,dob, height, heightType, weight, weightType, activityLevel)
    }

}