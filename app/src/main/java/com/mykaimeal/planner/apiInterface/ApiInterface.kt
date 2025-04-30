package com.mykaimeal.planner.apiInterface

import com.google.gson.JsonObject
import com.mykaimeal.planner.messageclass.ApiEndPoint
import com.mykaimeal.planner.repository.VisionRequest
import com.mykaimeal.planner.repository.VisionResponse
import dagger.Component.Factory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @GET(ApiEndPoint.bodyGoals)
    suspend fun getBogyGoal(): Response<JsonObject>

    @GET(ApiEndPoint.dietaryRestrictions)
    suspend fun getDietaryRestrictions(): Response<JsonObject>

    @GET(ApiEndPoint.favouriteCuisines)
    suspend fun getFavouriteCuisines(): Response<JsonObject>

    /* @GET(ApiEndPoint.dislikeIngredients)
     suspend fun getDislikeIngredients(): Response<JsonObject>*/


    @GET(ApiEndPoint.dislikeIngredients + "/{value}")
    suspend fun getDislikeIngredients(@Path("value") value: String?): Response<JsonObject>

    @GET(ApiEndPoint.dislikeIngredients + "/{value}/{value1}")
    suspend fun getDislikeSearchIngredients(
        @Path("value") item: String?,
        @Path("value1") search: String?
    ): Response<JsonObject>

    @GET(ApiEndPoint.allergensIngredients + "/{value}")
    suspend fun getAllergensIngredients(@Path("value") value: String?): Response<JsonObject>

    @GET(ApiEndPoint.allergensIngredients + "/{value}/{value1}")
    suspend fun getAllergensSearchIngredients(
        @Path("value") country: String?,
        @Path("value1") state: String?
    ): Response<JsonObject>

    @GET(ApiEndPoint.mealRoutine)
    suspend fun getMealRoutine(): Response<JsonObject>

    @GET(ApiEndPoint.cookingFrequency)
    suspend fun getCookingFrequency(): Response<JsonObject>

    @GET(ApiEndPoint.eatingOut)
    suspend fun getEatingOut(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.tipUrl)
    suspend fun getTipUrl(
        @Field("tip") tip: String?
    ): Response<JsonObject>


    @GET(ApiEndPoint.takeAwayReason)
    suspend fun getTakeAwayReason(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.userSignup)
    suspend fun userSignUp(
        @Field("email_or_phone") emailOrPhone: String,
        @Field("password") password: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.socialLogin)
    suspend fun socialLogin(
        @Field("email_or_phone") emailOrPhone: String?,
        @Field("social_id") socialId: String?,
        @Field("username") username: String?,
        @Field("usergender") userGender: String?,
        @Field("bodygoal") bodyGoal: String?,
        @Field("cooking_frequency") cookingFrequency: String?,
        @Field("eating_out") eatingOut: String?,
        @Field("take_way") takeWay: String?,
        @Field("take_way_name") takeWayName: String?,
        @Field("cooking_for_type") cookingForType: String?,
        @Field("partner_name") partnerName: String?,
        @Field("partner_age") partnerAge: String?,
        @Field("partner_gender") partnerGender: String?,
        @Field("family_member_name") familyMemberName: String?,
        @Field("family_member_age") familyMemberAge: String?,
        @Field("child_friendly_meals") childFriendlyMeals: String?,
        @Field("meal_routine_id[]") mealRoutineId: List<String>?,
        @Field("spending_amount") spendingAmount: String?,
        @Field("duration") duration: String?,
        @Field("dietary_id[]") dietaryId: List<String>?,
        @Field("favourite[]") favourite: List<String>?,
        @Field("allergies[]") allergies: List<String>?,
        @Field("dislike_ingredients_id[]") dislikeIngredientsId: List<String>?,
        @Field("device_type") deviceType: String?,
        @Field("fcm_token") fcmToken: String?,
        @Field("referral_from") referralFrom: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.otpVerify)
    suspend fun otpVerify(
        @Field("user_id") userId: String?,
        @Field("otp") otp: String?,
        @Field("username") username: String?,
        @Field("usergender") userGender: String?,
        @Field("bodygoal") bodyGoal: String?,
        @Field("cooking_frequency") cookingFrequency: String?,
        @Field("eating_out") eatingOut: String?,
        @Field("take_way") takeWay: String?,
        @Field("take_way_name") takeWayName: String?,
        @Field("cooking_for_type") cookingForType: String?,
        @Field("partner_name") partnerName: String?,
        @Field("partner_age") partnerAge: String?,
        @Field("partner_gender") partnerGender: String?,
        @Field("family_member_name") familyMemberName: String?,
        @Field("family_member_age") familyMemberAge: String?,
        @Field("child_friendly_meals") childFriendlyMeals: String?,
        @Field("meal_routine_id[]") mealRoutineId: List<String>?,
        @Field("spending_amount") spendingAmount: String?,
        @Field("duration") duration: String?,
        @Field("dietary_id[]") dietaryId: List<String>?,
        @Field("favourite[]") favourite: List<String>?,
        @Field("allergies[]") allergies: List<String>?,
        @Field("dislike_ingredients_id[]") dislikeIngredientsId: List<String>?,
        @Field("device_type") deviceType: String?,
        @Field("fcm_token") fcmToken: String?,
        @Field("referral_from") referralFrom: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.forgotPassword)
    suspend fun forgotPassword(
        @Field("email_or_phone") emailOrPhone: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.forgotOtpVerify)
    suspend fun forgotOtpVerify(
        @Field("email_or_phone") emailOrPhone: String,
        @Field("otp") otp: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.resendOtp)
    suspend fun resendOtp(
        @Field("email_or_phone") emailOrPhone: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updatePassword)
    suspend fun resetPassword(
        @Field("email_or_phone") emailOrPhone: String,
        @Field("password") password: String,
        @Field("conformpassword") conformPassword: String,
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.userLogin)
    suspend fun userLogin(
        @Field("email_or_phone") emailOrPhone: String,
        @Field("password") password: String,
        @Field("device_type") deviceType: String,
        @Field("fcm_token") fcmToken: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateLocation)
    suspend fun updateLocation(
        @Field("location_on_off") locationOnOff: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateNotification)
    suspend fun updateNotification(
        @Field("notification_status") notificationStatus: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.saveFeedback)
    suspend fun saveFeedback(
        @Field("email") email: String,
        @Field("message") message: String,
    ): Response<JsonObject>


    @GET(ApiEndPoint.termsCondition)
    suspend fun getTermsCondition(): Response<JsonObject>

    @GET(ApiEndPoint.privacyPolicy)
    suspend fun getPrivacyPolicy(): Response<JsonObject>


    @POST(ApiEndPoint.userProfileUrl)
    suspend fun userProfileDataApi(): Response<JsonObject>

    @POST(ApiEndPoint.logOutUrl)
    suspend fun userLogOutDataApi(): Response<JsonObject>


    @POST(ApiEndPoint.deleteUrl)
    suspend fun userDeleteDataApi(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.userProfileUpdateUrl)
    suspend fun userProfileUpdateBioApi(
        @Field("bio") bio: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.userProfileUpdateUrl)
    suspend fun userProfileUpdateApi(
        @Field("name") name: String,
        @Field("bio") bio: String,
        @Field("gender") gender: String,
        @Field("dob") dob: String,
        @Field("height") height: String,
        @Field("height_type") heightType: String,
        @Field("activity_level") activityLevel: String,
        @Field("height_protein") heightProtein: String,
        @Field("calories") calories: String,
        @Field("fat") fat: String,
        @Field("carbs") carbs: String,
        @Field("protien") protien: String,
        @Field("weight") weight: String,
        @Field("weight_type") weightType: String
    ): Response<JsonObject>

    @Multipart
    @POST(ApiEndPoint.userImageUpdateUrl)
    suspend fun upDateImageNameRequestApi(
        @Part image: MultipartBody.Part?,
        @Part("name") name: RequestBody?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.addCardUrl)
    suspend fun addCardRequestApi(@Field("stripe_token") stripeToken: String): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateNotificationUrl)
    suspend fun notificationRequestApi(
        @Field("push_notification") pushNotification: String,
        @Field("recipe_recommendations") recipeRecommendations: String,
        @Field("product_updates") productUpdates: String,
        @Field("promotional_updates") promotionalUpdates: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.recipeDetailsUrl)
    suspend fun recipeDetailsRequestApi(@Field("uri") url: String): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.mealReviewUrl)
    suspend fun recipeReviewRequestApi(
        @Field("uri") url: String,
        @Field("comment") msg: String,
        @Field("rating") ratingBarcount: String
    ): Response<JsonObject>

    @POST(ApiEndPoint.homeUrl)
    suspend fun homeDetailsRequestApi(): Response<JsonObject>

    @POST(ApiEndPoint.addBasketeDetailsUrl)
    suspend fun recipeAddBasketRequestApi(@Body jsonObject: JsonObject): Response<JsonObject>

    @POST(ApiEndPoint.addMealUrl)
    suspend fun recipeAddToPlanRequestApi(@Body jsonObject: JsonObject): Response<JsonObject>

    @POST(ApiEndPoint.createMealUrl)
    suspend fun createRecipeRequestApi(@Body jsonObject: JsonObject): Response<JsonObject>

    @POST(ApiEndPoint.getCookBookUrl)
    suspend fun getCookBookRequestApi(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.cookBookTypeListUrl)
    suspend fun getCookBookTypeRequestApi(@Field("type") type: String?): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.addMealTypeUrl)
    suspend fun addMealTypeApiUrl(@Field("uri") uri: String?,@Field("plan_type") planType:String?): Response<JsonObject>


    @Multipart
    @POST(ApiEndPoint.createCookBook)
    suspend fun createCookBook(
        @Part("name") name: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("status") status: RequestBody?,
        @Part("id") id: RequestBody?
    ): Response<JsonObject>


    @POST(ApiEndPoint.getCardBankUrl)
    suspend fun getCardAndBankRequestApi(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.allRecipeUrl)
    suspend fun planRequestApi(@Field("q") q: String): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getMealsUrl)
    suspend fun planDateRequestApi(
        @Field("date") date: String,
        @Field("plan_type") planType: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getSchedule)
    suspend fun getScheduleApi(
        @Field("date") date: String,
        @Field("plan_type") planType: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.likeUnlikeUrl)
    suspend fun likeUnlikeRequestApi(
        @Field("uri") uri: String, @Field("type") type: String, @Field("cook_book") cookbook: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.storeUUIDUrl)
    suspend fun superMarketSaveRequestApi(
        @Field("store") store: String?,
        @Field("store_name") storeName: String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.moveRecipeUrl)
    suspend fun moveRecipeRequestApi(
        @Field("id") id: String,
        @Field("cook_book") cookbook: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.removeCookBookUrl)
    suspend fun deleteCookBookRequestApi(@Field("id") id: String): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.addBasketeUrl)
    suspend fun addBasketRequestApi(
        @Field("uri") uri: String, @Field("quantity") quantity: String,@Field("type") type: String
    ): Response<JsonObject>

    @POST(ApiEndPoint.walletAmountUrl)
    suspend fun getWalletRequestApi(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.deleteCardUrl)
    suspend fun deleteCardRequestApi(
        @Field("card_id") cardId: String,
        @Field("customer_id") customerId: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.deleteBankUrl)
    suspend fun deleteBankRequestApi(@Field("stripe_account_id") stripeAccountId: String): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.countriesUrl)
    suspend fun countryRequestApi(@Field("url") url: String): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.transferToAccountUrl)
    suspend fun transferAmountRequest(
        @Field("amount") amount: String, @Field("account_id") destination: String
    ): Response<JsonObject>

    @Multipart
    @POST(ApiEndPoint.bankAddUrl)
    suspend fun addBankRequestApi(
        @Part image: MultipartBody.Part?,
        @Part filePartBack: MultipartBody.Part?,
        @Part filePart: MultipartBody.Part?,
        @Part("firstname") firstNameBody: RequestBody,
        @Part("lastname") lastNameBody: RequestBody,
        @Part("email") emailBody: RequestBody,
        @Part("phone") phoneBody: RequestBody,
        @Part("dob") dobBody: RequestBody,
        @Part("id_number") personalIdentificationNobody: RequestBody,
        @Part("id_type") idTypeBody: RequestBody,
        @Part("ssn") ssnBody: RequestBody,
        @Part("address") addressBody: RequestBody,
        @Part("country") countryBody: RequestBody,
        @Part("state_code") shortStateNameBody: RequestBody,
        @Part("city") cityBody: RequestBody,
        @Part("postal_code") postalCodeBody: RequestBody,
        @Part("bank_proof_type") bankDocumentTypeBody: RequestBody,
        @Part("device_type") deviceTypeBody: RequestBody,
        @Part("token_type") tokenTypeBody: RequestBody,
        @Part("stripe_token") stripeTokenBody: RequestBody,
        @Part("save_card") saveCardBody: RequestBody,
        @Part("amount") amountBody: RequestBody,
        @Part("payment_type") paymentTypeBody: RequestBody,
        @Part("bank_id") bankIdBody: RequestBody
    ): Response<JsonObject>


    @POST(ApiEndPoint.getUserPreferences)
    suspend fun userPreferencesApi(): Response<JsonObject>


    @POST(ApiEndPoint.getSubscriptionDeltailsUrl)
    suspend fun userSubscriptionCountApi(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.getUserPreferences)
    suspend fun userPreferencesDislikeApi(
        @Field("dislike_search") dislike_search: String?,
        @Field("dislike_num") dislike_num: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getUserPreferences)
    suspend fun userPreferencesAllergiesApi(
        @Field("allergic_search") country: String?,
        @Field("allergic_num") state: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateBodyGoalApi(@Field("bodygoal") bodyGoal: String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateCookBookApi(@Field("cook_book_id") cookBookId : String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateCookingFrequencyApi(@Field("cooking_frequency") cookingFrequency: String?): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)

    suspend fun updateReasonTakeAwayApi(@Field("take_way") takeWay: String?,@Field("take_way_name") take_way_name: String?): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateEatingOutApi(@Field("eating_out") eatingOut: String?): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updatePartnerInfoApi(
        @Field("partner_name") partnerName: String?,
        @Field("partner_age") partnerAge: String?,
        @Field("partner_gender") partnerGender: String?,
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateFamilyInfoApi(
        @Field("family_member_name") familyMemberName: String?,
        @Field("family_member_age") familyMemberAge: String?,
        @Field("child_friendly_meals") childFriendlyMeals: String?,
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateMealRoutineApi(
        @Field("meal_routine_id[]") mealRoutineId: List<String>?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateSpendingGroceriesApi(
        @Field("spending_amount") spendingAmount: String?,
        @Field("duration") duration: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateDietaryApi(
        @Field("dietary_id[]") dietaryId: List<String>?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateFavouriteApi(
        @Field("favourite[]") favouriteId: List<String>?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateAllergiesApi(
        @Field("allergies[]") allergensId: List<String>?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updateDislikedIngredientsApi(
        @Field("dislike_ingredients_id[]") dislikeIngId: List<String>?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updatePreferencesApi(
        @Field("username") username: String?,
        @Field("cooking_for_type") cookingForType: String?,
        @Field("usergender") userGender: String?,
        @Field("bodygoal") bodyGoal: String?,
        @Field("partner_name") partnerName: String?,
        @Field("partner_age") partnerAge: String?,
        @Field("partner_gender") partnerGender: String?,
        @Field("family_member_name") familyMemberName: String?,
        @Field("family_member_age") familyMemberAge: String?,
        @Field("child_friendly_meals") childFriendlyMeals: String?,
        @Field("dietary_id[]") dietaryId: MutableList<String>?,
        @Field("favourite[]") favouriteId: MutableList<String>?,
        @Field("dislike_ingredients_id[]") dislikeIngId: MutableList<String>?,
        @Field("allergies[]") allergensId: MutableList<String>?,
        @Field("meal_routine_id[]") mealRoutineId: MutableList<String>?,
        @Field("cooking_frequency") cookingFrequency: String?,
        @Field("spending_amount") spendingAmount: String?,
        @Field("duration") duration: String?,
        @Field("eating_out") eatingOut: String?,
        @Field("take_way") takeWay: String?,
        @Field("take_way_name") takeWayName: String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.updateUserPreferences)
    suspend fun updatePostCode(
        @Field("postcode") postcode: String?,
        @Field("longitude") longitude: String?,
        @Field("latitude") latitude: String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.recipeSearch)
    suspend fun recipeSearchApi(
        @Field("q") q: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.recipeSearch)
    suspend fun recipeFilterSearchApi(
        @Field("mealType[]") mealType: MutableList<String>?,
        @Field("health[]") health: MutableList<String>?,
        @Field("time[]") time: MutableList<String>?
    ): Response<JsonObject>


    @POST(ApiEndPoint.forSearchUrl)
    suspend fun recipeForSearchApi(): Response<JsonObject>

    @POST(ApiEndPoint.getMissingIngBasketUrl)
    suspend fun getMissingIngBasketUrl(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addToCartUrl)
    suspend fun addToCartUrlApi(
        @Field("food_ids[]") foodIds: MutableList<String>?,
        @Field("sch_id") schId: String?,
        @Field("names[]") foodName: MutableList<String>?,
        @Field("status[]") status: MutableList<String>?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.addToCartUrl)
    suspend fun addShoppingCartUrlApi(
        @Field("food_ids[]") foodIds: MutableList<String>?,
        @Field("sch_id[]") schId: MutableList<String>?,
        @Field("names[]") foodName: MutableList<String>?,
        @Field("status[]") status: MutableList<String>?
    ): Response<JsonObject>

    @POST(ApiEndPoint.forPreferenceUrl)
    suspend fun recipePreferencesApi(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.createRecipeUrl)
    suspend fun createRecipeUrlApi(
        @Field("q") q: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.removeMeal)
    suspend fun removeMealApi(
        @Field("id") id: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.removeBasketUrl)
    suspend fun removeBasketUrlApi(
        @Field("id") id: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getMissingIngUrl)
    suspend fun getMissingIngredientsApi(
        @Field("uri") uri: String?,
        @Field("sch_id") schId: String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.getMealByUrl)
    suspend fun getMealByUrl(
        @Field("q") uri: String?
    ): Response<JsonObject>


    @POST(ApiEndPoint.updateMealUrl)
    suspend fun updateMealUrl(@Body jsonObject: JsonObject): Response<JsonObject>


    @POST(ApiEndPoint.filterSearchUrl)
    suspend fun getFilterList(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.superMarketsUrl)
    suspend fun getSuperMarket(
        @Field("latitude") latitude: String?,
        @Field("longitude") longitude: String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.subscriptionGoogleUrl)
    suspend fun subscriptionGoogle(
        @Field("type") type: String?,
        @Field("purchase_token") purchaseToken: String?,
        @Field("subscription_id") subscriptionId: String?
    ): Response<JsonObject>



    @POST(ApiEndPoint.checkSubscriptionUrl)
    suspend fun subscriptionPurchaseType(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.getBasketListUrl)
    suspend fun getBasketListUrl(@Field("store_id") storeId: String?,@Field("latitude") latitude:String?,@Field("longitude") longitude:String?): Response<JsonObject>

    @POST(ApiEndPoint.getYourRecipeUrl)
    suspend fun getYourRecipeUrl(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getSendSmsUrl)
    suspend fun sendOtpUrl(@Field("phone") phone: String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addPhoneUrl)
    suspend fun addPhoneUrl(
        @Field("phone") phone: String?,
        @Field("otp") otp: String?,
        @Field("country_code") countryCode: String?
    ): Response<JsonObject>


    @POST(ApiEndPoint.getAddressUrl)
    suspend fun getAddressUrl(): Response<JsonObject>

    @POST(ApiEndPoint.getNotesUrl)
    suspend fun getNotesUrl(): Response<JsonObject>

    @POST(ApiEndPoint.getShoppingListUrl)
    suspend fun getShoppingListUrl(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addNotesUrl)
    suspend fun addNotesUrl(
        @Field("pickup") pickup: String?,
        @Field("description") description: String?
    ): Response<JsonObject>

    @POST(ApiEndPoint.getCheckoutUrl)
    suspend fun getCheckoutScreenUrl(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.getOrderProductUrl)
    suspend fun getOrderProductUrl(@Field("tip") tip:String?, @Field("card_id") card_id:String?): Response<JsonObject>

    @POST(ApiEndPoint.getStoreProductsUrl)
    suspend fun getStoreProductUrl(): Response<JsonObject>


    @POST(ApiEndPoint.getCardMealMeUrl)
    suspend fun getCardMealMeUrl(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.getProductsUrl)
    suspend fun getProductsUrl(@Field("query") query:String?,@Field("food_id") foodId:String?,@Field("sch_id") schId:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getProductsDetailsUrl)
    suspend fun getProductsDetailsUrl(@Field("id") id:String?,@Field("query") query:String?,@Field("food_id") foodId:String?,@Field("sch_id ") schId:String?): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.getSelectProductsUrl)
    suspend fun getSelectProductsUrl(@Field("id") id:String?,@Field("product_id") product_id:String?,@Field("sch_id") schId:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getAllIngredientsUrl)
    suspend fun getAllIngredientsUrl(@Field("category") category:String?,@Field("search") search:String?,@Field("number") number:String?): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.recipeSwapUrl)
    suspend fun recipeSwapUrl(@Field("id") id:String?,@Field("uri") uri:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.basketYourRecipeIncDescUrl)
    suspend fun basketYourRecipeIncDescUrl(@Field("uri") uri:String?,@Field("quantity") quantity:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.basketIngIncDescUrl)
    suspend fun basketIngIncDescUrl(@Field("food_id") foodId:String?,@Field("quantity") quantity:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addCardMealMeUrl)
    suspend fun addCardMealMeUrl(
        @Field("card_number") card_number: String?,
        @Field("exp_month") exp_month: String?,
        @Field("exp_year") exp_year: String?,
        @Field("cvv") cvv: String?,
        @Field("status") status:String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.deleteCardMealMeUrl)
    suspend fun deleteCardMealMeUrl(
        @Field("id") id: String?
    ): Response<JsonObject>


  @FormUrlEncoded
    @POST(ApiEndPoint.setPreferredCardMealMeUrl)
    suspend fun setPreferredCardMealMeUrl(
        @Field("id") id: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addToBasketAllUrl)
    suspend fun addToBasketAllUrl(
        @Field("date") date: String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.userDietSuggestionUrl)
    suspend fun updateDietSuggestionUrl(
        @Field("gender") gender: String?,
        @Field("dob") dob: String?,
        @Field("height") height: String?,
        @Field("height_type") heightType: String?,
        @Field("weight") weight: String?,
        @Field("weight_type") weightType: String?,
        @Field("activityLevel") activityLevel: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addAddressUrl)
    suspend fun addAddressUrl(
        @Field("latitude") latitude: String?,
        @Field("longitude") longitude: String?,
        @Field("street_name") streetName: String?,
        @Field("street_num") streetNum: String?,
        @Field("apart_num") apartNum: String?,
        @Field("city") city: String?,
        @Field("state") state: String?,
        @Field("country") country: String?,
        @Field("zipcode") zipcode: String?,
        @Field("primary") primary: String?,
        @Field("id") id: String?,
        @Field("type") type: String?,
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.makeAddressPrimaryUrl)
    suspend fun makeAddressPrimaryUrl(
        @Field("id") id: String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.selectStoreProductUrl)
    suspend fun selectStoreProductUrl(
        @Field("store_name") storeName: String?,
        @Field("store_id") storeId: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.graph)
    suspend fun getGraphScreenUrl(
        @Field("month") month: String?
    ): Response<JsonObject>

    @POST(ApiEndPoint.orderHistoryUrl)
    suspend fun orderHistoryUrl(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.graphWeekUrl)
    suspend fun orderWeekUrl(@Field("week") week:String?): Response<JsonObject>


    @Multipart
    @POST(ApiEndPoint.generateLinkUrl)
    suspend fun generateLinkUrl(
        @Part("link") name: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<JsonObject>

    /*   @POST("v1/images:annotate")
       fun annotateImage(@Body request: VisionRequest): Call<VisionResponse>*/

    @Headers("Content-Type: application/json")
    @POST("v1/images:annotate")
    fun annotateImage(
        @Query("key") apiKey: String,
        @Body visionRequest: VisionRequest
    ): Call<VisionResponse>


}