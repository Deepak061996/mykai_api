package com.mykaimeal.planner.repository

import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface MainRepository {

    suspend fun bogyGoal(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getDietaryRestrictions(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getFavouriteCuisines(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getDislikeIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?)
    suspend fun getDislikeSearchIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?,type: String)
    suspend fun getAllergensSearchIngredients(successCallback: (response: NetworkResult<String>) -> Unit,data:String,itemCount:String?,type: String)
    suspend fun getAllergensIngredients(successCallback: (response: NetworkResult<String>) -> Unit,itemCount:String?)
    suspend fun getMealRoutine(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getCookingFrequency(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getEatingOut(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getTakeAwayReason(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun signUpModel(successCallback: (response: NetworkResult<String>) -> Unit,emailOrPhone: String, password: String)

    suspend fun otpVerify(successCallback: (response: NetworkResult<String>) -> Unit,userid: String?, otp: String?,userName:String?,userGender:String?,
                          bodyGoal:String?,cookingFrequency:String?,eatingOut: String?, takeAway:String?,takeWayName:String?,cookingForType:String?,
                          partnerName:String?,partnerAge:String?,partnerGender:String?,familyMemberName:String?, familyMemberAge:String?,
                          childFriendlyMeals:String?,mealRoutineId:List<String>?,spendingAmount:String?,duration:String?, dietaryid:List<String>?,
                          favourite:List<String>?, allergies:List<String>?,dislikeIngredients:List<String>?,deviceType:String?,fcmToken:String?,
                          referralFrom:String?)

    suspend fun forgotPassword(successCallback: (response: NetworkResult<String>) -> Unit,emailOrPhone: String)
    suspend fun resendSignUpModel(successCallback: (response: NetworkResult<String>) -> Unit,emailOrPhone: String)

    suspend fun forgotOtpVerify(successCallback: (response: NetworkResult<String>) -> Unit,emailOrPhone: String,otp:String)

    suspend fun resendOtp(successCallback: (response: NetworkResult<String>) -> Unit,emailOrPhone: String)

    suspend fun resetPassword(successCallback: (response: NetworkResult<String>) -> Unit,emailOrPhone:String,password: String,confirmPassword:String)

    suspend fun userLogin(successCallback: (response: NetworkResult<String>) -> Unit,emailOrPhone: String,password: String,deviceType:String,fcmToken:String)

    suspend fun socialLogin(successCallback: (response: NetworkResult<String>) -> Unit,  emailOrPhone: String?, socialID: String?,userName:String?,
                            userGender:String?,bodyGoal:String?,cookingFrequency:String?,eatingOut:String?,takeAway:String?,takeWayName:String?,cookingForType:String?,
                            partnerName:String?,partnerAge:String?,partnerGender:String?,familyMemberName:String?, familyMemberAge:String?,
                            childFriendlyMeals:String?,mealRoutineId:List<String>?,spendingAmount:String?,duration:String?, dietaryid:List<String>?,
                            favourite:List<String>?, allergies:List<String>?,dislikeIngredients:List<String>?,deviceType:String?,fcmToken:String?,referralFrom:String?)
    suspend fun updateLocation(successCallback: (response: NetworkResult<String>) -> Unit,locationStatus: String)

    suspend fun updateNotification(successCallback: (response: NetworkResult<String>) -> Unit,notificationStatus: String)

    suspend fun privacyPolicy(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun termCondition(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun saveFeedback(successCallback: (response: NetworkResult<String>) -> Unit,email: String,message: String)


    suspend fun userProfileDataApi(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun userLogOutDataApi(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun userDeleteDataApi(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun upDateProfileRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        name: String,
        bio: String,
        genderType: String,
        dob: String,
        height: String
        ,
        heightType: String,
        activityLevel: String,
        heightProtein: String,
        calories: String,
        fat: String,
        carbs: String,
        protien: String,
        weight: String,
        weightType: String
    )

    suspend fun userProfileUpdateBioApi(
        successCallback: (response: NetworkResult<String>) -> Unit, bio: String)

 suspend fun upDateImageNameRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, Image: MultipartBody.Part?, name: RequestBody)

 suspend fun addCardRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, token: String)


 suspend fun notificationRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, pushNotification: String,recipeRecommendations: String,productUpdates: String,promotionalUpdates: String)

 suspend fun recipeDetailsRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, url: String)

 suspend fun recipeReviewRequestApi(
     successCallback: (response: NetworkResult<String>) -> Unit,
     url: String,
     msg: String,
     ratingBarcount: String
 )

 suspend fun homeDetailsRequestApi(successCallback: (response: NetworkResult<String>) -> Unit)

 suspend fun recipeAddBasketRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,jsonObject: JsonObject)

 suspend fun recipeAddToPlanRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,jsonObject: JsonObject)
 suspend fun addToBasketAllUrl(successCallback: (response: NetworkResult<String>) -> Unit,date: String?)
 suspend fun addMealTypeApiUrl(successCallback: (response: NetworkResult<String>) -> Unit,uri: String?,planType: String?,mealType:String?)
 suspend fun createRecipeRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,jsonObject: JsonObject)
 suspend fun updateMealUrl(successCallback: (response: NetworkResult<String>) -> Unit,jsonObject: JsonObject)

 suspend fun getCookBookRequestApi(successCallback: (response: NetworkResult<String>) -> Unit)

 suspend fun getCookBookTypeRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,id:String?)

 suspend fun planRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, q: String)

 suspend fun planDateRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, date: String,planType:String)
 suspend fun getScheduleApi(successCallback: (response: NetworkResult<String>) -> Unit, date: String,planType:String)
 suspend fun createCookBookApi(
     successCallback: (response: NetworkResult<String>) -> Unit, name: RequestBody?, image: MultipartBody.Part?, status: RequestBody?, id: RequestBody?)

 suspend fun likeUnlikeRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,  uri: String,likeType: String,type: String)
 suspend fun superMarketSaveRequest(successCallback: (response: NetworkResult<String>) -> Unit,  uuid: String?,storeName:String?)

 suspend fun moveRecipeRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, id: String, cook_book: String)

 suspend fun deleteCookBookRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, id: String)

 suspend fun addBasketRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,  uri: String,quantity: String,type: String)

 suspend fun getCardAndBankRequestApi(successCallback: (response: NetworkResult<String>) -> Unit)

 suspend fun getWalletRequestApi(successCallback: (response: NetworkResult<String>) -> Unit)

 suspend fun deleteCardRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,cardId: String,customerId: String)

 suspend fun deleteBankRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,stripeAccountId: String)

 suspend fun countryRequestApi(successCallback: (response: NetworkResult<String>) -> Unit,url: String)

 suspend fun transferAmountRequest(successCallback: (response: NetworkResult<String>) -> Unit,amount: String,destination: String)

 suspend fun addBankRequestApi(
     successCallback: (response: NetworkResult<String>) -> Unit,
     filePartFront: MultipartBody.Part?,
     filePartBack: MultipartBody.Part?,
     filePart: MultipartBody.Part?,
     firstNameBody: RequestBody,
     lastNameBody: RequestBody,
     emailBody: RequestBody,
     phoneBody: RequestBody,
     dobBody: RequestBody,
     personalIdentificationNobody: RequestBody,
     idTypeBody: RequestBody,
     ssnBody: RequestBody,
     addressBody: RequestBody,
     countryBody: RequestBody,
     shortStateNameBody: RequestBody,
     cityBody: RequestBody,
     postalCodeBody: RequestBody,
     bankDocumentTypeBody: RequestBody,
     deviceTypeBody: RequestBody,
     tokenTypeBody: RequestBody,
     stripeTokenBody: RequestBody,
     saveCardBody: RequestBody,
     amountBody: RequestBody,
     paymentTypeBody: RequestBody,
     bankIdBody: RequestBody,)


    suspend fun userPreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit)


    suspend fun userSubscriptionCountApi(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun updateAllergiesApi(successCallback: (response: NetworkResult<String>) -> Unit, allergies:List<String>?)
    suspend fun userPreferencesAllergiesApi(successCallback: (response: NetworkResult<String>) -> Unit,allergicSearch:String?,allergicNum:String?)
    suspend fun addToCartUrlApi(successCallback: (response: NetworkResult<String>) -> Unit,foodIds: MutableList<String>?,
                                schId:String?, foodName:MutableList<String>?,status: MutableList<String>?)

    suspend fun addShoppingCartUrlApi(successCallback: (response: NetworkResult<String>) -> Unit,foodIds: MutableList<String>?,
                                schId:MutableList<String>?, foodName:MutableList<String>?,status: MutableList<String>?)

    suspend fun updateBodyGoalApi(successCallback: (response: NetworkResult<String>) -> Unit,bodyGoal: String?)

    suspend fun updateCookingFrequencyApi(successCallback: (response: NetworkResult<String>) -> Unit,cookingFrequency: String?)


    suspend fun updateReasonTakeAwayApi(successCallback: (response: NetworkResult<String>) -> Unit,takeAway: String?,take_way_name: String?)


    suspend fun updateEatingOutApi(successCallback: (response: NetworkResult<String>) -> Unit,eatingOut: String?)

    suspend fun updatePartnerInfoApi(successCallback: (response: NetworkResult<String>) -> Unit,partnerName: String?,
                                     partnerAge: String?,partnerGender: String?)
    suspend fun updateFamilyInfoApi(successCallback: (response: NetworkResult<String>) -> Unit,familyName: String?,
                                    familyAge: String?,status: String?)

    suspend fun updateSpendingGroceriesApi(successCallback: (response: NetworkResult<String>) -> Unit,spendingAmount: String?,
                                           duration: String?)

    suspend fun updateMealRoutineApi(successCallback: (response: NetworkResult<String>) -> Unit, mealRoutineId:List<String>?)
    suspend fun updateCookBookApi(successCallback: (response: NetworkResult<String>) -> Unit, cookBookId:String?)

    suspend fun updateDietaryApi(successCallback: (response: NetworkResult<String>) -> Unit, dietaryId:List<String>?)

    suspend fun updateFavouriteApi(successCallback: (response: NetworkResult<String>) -> Unit, favouriteId:List<String>?)

    suspend fun updateDislikedIngredientsApi(successCallback: (response: NetworkResult<String>) -> Unit, dislikedId:List<String>?)

    suspend fun userPreferencesDislikeApi(successCallback: (response: NetworkResult<String>) -> Unit,dislikeSearch:String?, dislikeum:String?)

    suspend fun updatePostCodeApi(successCallback: (response: NetworkResult<String>) -> Unit, postCode:String?,longitude:String?, latitude:String?)

    suspend fun recipeSearchApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch:String?)
    suspend fun recipeFilterSearchApi(successCallback: (response: NetworkResult<String>) -> Unit, mealType: MutableList<String>?,health: MutableList<String>?,time: MutableList<String>?)
    suspend fun getMissingIngredientsApi(successCallback: (response: NetworkResult<String>) -> Unit, uri:String?,schId:String?)

    suspend fun createRecipeUrlApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch:String?)

    suspend fun recipeforSearchApi(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun recipePreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun removeMealApi(successCallback: (response: NetworkResult<String>) -> Unit, cookedId:String?)
    suspend fun removeBasketUrlApi(successCallback: (response: NetworkResult<String>) -> Unit, recipeId:String?)
    suspend fun basketYourRecipeIncDescUrl(successCallback: (response: NetworkResult<String>) -> Unit, uri:String?,quantity:String?)
    suspend fun basketIngIncDescUrl(successCallback: (response: NetworkResult<String>) -> Unit, foodId:String?,quantity:String?)
    suspend fun makeAddressPrimaryUrl(successCallback: (response: NetworkResult<String>) -> Unit, id:String?)
    suspend fun getMealByUrl(successCallback: (response: NetworkResult<String>) -> Unit, q:String?)
    suspend fun updatePreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit, userName: String?,cookingForType: String?,
                                     userGender:String?,bodyGoal:String?,partnerName:String?,partnerAge:String?,partnerGender:String?,familyMemberName:String?,
                             familyMemberAge:String?,childFriendlyMeals:String?,dietaryId: MutableList<String>?,favouriteId: MutableList<String>?,
                             dislikeIngId: MutableList<String>?,allergensId: MutableList<String>?,mealRoutineId: MutableList<String>?,cookingFrequency: String?,
                             spendingAmount: String?,duration: String?,eatingOut: String?,takeWay: String?,takeWayName: String?)

    suspend fun getFilterList(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun getSuperMarket(successCallback: (response: NetworkResult<String>) -> Unit, latitude: String?, longitude:String?)

    suspend fun getSuperMarketWithPage(successCallback: (response: NetworkResult<String>) -> Unit, latitude: String?, longitude:String?, pageCount:String?)


    suspend fun subscriptionGoogle(successCallback: (response: NetworkResult<String>) -> Unit, type: String?, purchaseToken: String?, subscriptionId:String?)

    suspend fun subscriptionPurchaseType(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun getBasketUrl(successCallback: (response: NetworkResult<String>) -> Unit,storeId:String?,latitude:String?,longitude:String?)
    suspend fun getAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getCheckoutScreenUrl(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getYourRecipeUrl(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun addAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit,latitude: String?, longitude: String?,streetName:String?,
                              streetNum:String?,apartNum:String?,city:String?,state:String?,country:String?,
                              zipcode:String?,primary:String?,id:String?,type:String?)

    suspend fun sendOtpUrl(successCallback: (response: NetworkResult<String>) -> Unit,phone: String?)
    suspend fun addPhoneUrl(successCallback: (response: NetworkResult<String>) -> Unit,phone: String?,otp:String?,countryCode:String?)
    suspend fun getShoppingList(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getNotesUrl(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun addNotesUrl(successCallback: (response: NetworkResult<String>) -> Unit,pickup:String?,description:String?)
    suspend fun getOrderProductUrl(successCallback: (response: NetworkResult<String>) -> Unit,tip:String?,cardId:String?)
    suspend fun getTipUrl(successCallback: (response: NetworkResult<String>) -> Unit, tip:String?)
    suspend fun getStoreProductUrl(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getCardMealMeUrl(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun getAllIngredientsUrl(successCallback: (response: NetworkResult<String>) -> Unit,category:String?,search:String?,number:String?)
    suspend fun addCardMealMeUrl(successCallback: (response: NetworkResult<String>) -> Unit,cardNumber:String?,
                                 expMonth:String?,expYear:String?,cvv:String?,status:String?,type:String?)


    suspend fun deleteCardMealMeUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?)
    suspend fun getMissingIngBasketUrl(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun setPreferredCardMealMeUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?)

    suspend fun getProductsUrl(successCallback: (response: NetworkResult<String>) -> Unit,query:String?,foodId:String?,schId:String?)
    suspend fun getProductsDetailsUrl(successCallback: (response: NetworkResult<String>) -> Unit,proId:String?,query:String?,foodId:String?,schId:String?)
    suspend fun updateDietSuggestionUrl(successCallback: (response: NetworkResult<String>) -> Unit,gender: String?, dob: String?, height: String?,
                                        heightType: String?, weight: String?, weightType: String?, activityLevel: String?)
    suspend fun getSelectProductsUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?,productId:String?,schId:String?)
    suspend fun recipeSwapUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?,uri:String?)

    suspend fun generateLinkUrl(successCallback: (response: NetworkResult<String>) -> Unit, link: RequestBody?, image: MultipartBody.Part?)
    suspend fun selectStoreProductUrl(successCallback: (response: NetworkResult<String>) -> Unit, storeName: String?, storeId: String?)
    suspend fun getGraphScreenUrl(successCallback: (response: NetworkResult<String>) -> Unit, month:String?, year:String?)
    suspend fun orderWeekUrl(successCallback: (response: NetworkResult<String>) -> Unit, start_date:String?,end_date:String?,year:String?)
    suspend fun orderHistoryUrl(successCallback: (response: NetworkResult<String>) -> Unit)
    suspend fun referralUrl(successCallback: (response: NetworkResult<String>) -> Unit)

}