package com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.BasketScreenModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model.CheckoutScreenModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CheckoutScreenViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getCheckoutScreenUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCheckoutScreenUrl{ successCallback(it) }
    }

    suspend fun getAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getAddressUrl{ successCallback(it) }
    }

    suspend fun addAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit, latitude: String?, longitude: String?,
                              streetName:String?,streetNum:String?,apartNum:String?,city:String?,state:String?,country:String?,
                              zipcode:String?,primary:String?,id:String?,type:String?) {
        repository.addAddressUrl({ successCallback(it) }, latitude, longitude, streetName, streetNum, apartNum, city,state, country, zipcode, primary, id, type)
    }

    suspend fun makeAddressPrimaryUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?) {
        repository.makeAddressPrimaryUrl({ successCallback(it) },id)
    }

    suspend fun sendOtpUrl(
        successCallback: (response: NetworkResult<String>) -> Unit, phone: String?) {
        repository.sendOtpUrl({ successCallback(it) }, phone)
    }


    suspend fun addPhoneUrl(successCallback: (response: NetworkResult<String>) -> Unit, phone: String?, otp: String?,countryCode:String?) {
        repository.addPhoneUrl({ successCallback(it) }, phone, otp,countryCode)
    }


    suspend fun addNotesUrl(successCallback: (response: NetworkResult<String>) -> Unit,pickup:String?,description:String?){
        repository.addNotesUrl({ successCallback(it) },pickup,description)
    }

    suspend fun getNotesUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getNotesUrl{ successCallback(it) }
    }



    // card add
    suspend fun getCardMealMeUrl(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.getCardMealMeUrl { successCallback(it) }
    }

    suspend fun deleteCardMealMeUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        id: String?
    ) {
        repository.deleteCardMealMeUrl({ successCallback(it) }, id)
    }

    suspend fun setPreferredCardMealMeUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        id: String?
    ) {
        repository.setPreferredCardMealMeUrl({ successCallback(it) }, id)
    }


    suspend fun addCardMealMeUrl(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cardNumber: String?,
        expMonth: String?,
        expYear: String?,
        cvv: String?,
        status: String?,
        type: String?
    ) {
        repository.addCardMealMeUrl(
            { successCallback(it) },
            cardNumber,
            expMonth,
            expYear,
            cvv,
            status,
            type
        )
    }


    // hold data in view model


    private var _dataCheckOut: CheckoutScreenModelData? = null
    val dataCheckOut: CheckoutScreenModelData? get() = _dataCheckOut

    fun setCheckOutData(data: CheckoutScreenModelData?){
        _dataCheckOut=data
    }
}