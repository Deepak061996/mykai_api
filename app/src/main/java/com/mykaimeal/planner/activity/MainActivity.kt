package com.mykaimeal.planner.activity

import PlanApiResponse
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.wallet.PaymentData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterUrlIngredientItem
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.adapter.ImageViewPagerAdapter
import com.mykaimeal.planner.adapter.IndicatorAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.AppsFlyerConstants
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.ActivityMainBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.viewmodel.MealRoutineViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.viewmodel.BasketScreenViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.viewmodel.CheckoutScreenViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel.StatisticsViewModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SearchMealUrlModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SearchMealUrlModelData
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.HomeViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.PlanViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.BreakfastModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.RecipesModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apisubscription.SubscriptionModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import com.mykaimeal.planner.model.DateModel
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnClickListener, OnItemClickListener{

    lateinit var  binding: ActivityMainBinding
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var mealRoutineViewModel: MealRoutineViewModel
    private var recipesModel: RecipesModel? = null
    private lateinit var layOnBoardingIndicator: RecyclerView
    val dataList = ArrayList<DataModel>()
    private lateinit var adapter: ImageViewPagerAdapter
    private var tvWeekRange: TextView? = null
    private var viewPager: ViewPager2? = null
    private var dialog: Dialog? = null
    private var laybuttonplabasket: LinearLayout? = null
    private var currentDate = Date() // Current date

    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private var mealType:String="Breakfast"
    private var rcyChooseDaySch: RecyclerView? = null
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    private  val LAST_SHOWN_KEY = "lastShownDailyInspirations"
    private  val INTERVAL_MS: Long = 60 * 1000L // 60 seconds
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isRunning = false

    private lateinit var indicatorAdapter: IndicatorAdapter

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var rcyIngredients: RecyclerView? = null
    private var tvTitleName: TextView? = null
    private var tvTitleDesc: TextView? = null
    private var layMainProgress: View? = null
    private var imgRecipeLike: ImageView? = null
    private var imageData: ShapeableImageView? = null
    private var lay_progess: View ? = null
    private var adapterUrlIngredients: AdapterUrlIngredientItem? = null
    private var loadDataStatus: Boolean = false
    private var uri: String = ""
    private var openScreen: String = "Home"
    var Subscription_status: Int?=1
    var addmeal: Int?=0
    var favorite: Int?=0
    var imageSearch: Int?=0
    var urlSearch: Int?=0
    var alertStatus:Boolean=false
    private var status:String?="RecipeSearch"
    private var dialogAddRecipe:Dialog ?=null
    lateinit var navController : NavController
    var isFlashlightOn = false
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private lateinit var sessionManagement: SessionManagement


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        mealRoutineViewModel = ViewModelProvider(this@MainActivity)[MealRoutineViewModel::class.java]
        commonWorkUtils = CommonWorkUtils(this)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.frameContainerMain) as NavHostFragment
        navController = navHostFragment.navController
        sessionManagement = SessionManagement(this)



        getFcmToken()

        setEvent()

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }


        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.first { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }


        // Register for result
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val submittedResult = data?.getStringExtra("submitted_result")
                binding.cardViewAddRecipe.visibility = View.VISIBLE
                if (!submittedResult.equals("close")){
                    searchBottomDialog(submittedResult)
                }
            }
        }


        // Start API polling
        startRepeatingApiCall()

        // using function for find destination graph
        startDestination()

        startTimer(this@MainActivity)

    }

     fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        cameraManager.setTorchMode(cameraId, isFlashlightOn)
    }

    private fun startRepeatingApiCall() {
        lifecycleScope.launch {
            while (isActive) {
                withContext(Dispatchers.IO) {
                    if (BaseApplication.isOnline(this@MainActivity)) {
                        mealRoutineViewModel.userSubscriptionCountApi {0
                            handleApiSubscriptionResponse(it)
                        }
                    }
                }
                delay(3000)
            }
        }
    }

    private fun searchBottomDialog(submittedResult: String?) {
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetDialog?.setContentView(R.layout.bottom_import_recipe_url)
        rcyIngredients = bottomSheetDialog?.findViewById(R.id.rcyIngredients)
        tvTitleName = bottomSheetDialog?.findViewById(R.id.tvTitleName)
        tvTitleDesc = bottomSheetDialog?.findViewById(R.id.tvTitleDesc)
        layMainProgress = bottomSheetDialog?.findViewById(R.id.layMainProgress)
        imgRecipeLike = bottomSheetDialog?.findViewById(R.id.imgRecipeLike)
        imageData = bottomSheetDialog?.findViewById(R.id.imageData)
        lay_progess = bottomSheetDialog?.findViewById(R.id.lay_progess)
        bottomSheetDialog?.show()

        imgRecipeLike!!.setOnClickListener{
            if (loadDataStatus){
                addFavTypeDialogUrl(submittedResult)
            }
        }

        searchMealUrlApi(submittedResult)

    }

    private fun addFavTypeDialogUrl(submittedResult: String?) {
        val dialogAddRecipe = Dialog(this)
        dialogAddRecipe.setContentView(R.layout.alert_dialog_add_recipe)
        dialogAddRecipe.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogAddRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogAddRecipe.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        spinnerActivityLevel = dialogAddRecipe.findViewById(R.id.spinnerActivityLevel)
        val relCreateNewCookBook = dialogAddRecipe.findViewById<RelativeLayout>(R.id.relCreateNewCookBook)
        val imgCheckBoxOrange = dialogAddRecipe.findViewById<ImageView>(R.id.imgCheckBoxOrange)
        cookbookList.clear()
        val data = com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("", "", 0, "", "Favorites", 0, "", 0)
        cookbookList.add(0, data)
        spinnerActivityLevel.setItems(cookbookList.map { it.name })
        dialogAddRecipe.show()
        dialogAddRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        getCookBookList()

        relCreateNewCookBook.setOnClickListener {
            relCreateNewCookBook.setBackgroundResource(R.drawable.light_green_rectangular_bg)
            imgCheckBoxOrange.setImageResource(R.drawable.orange_uncheck_box_images)
            dialog?.dismiss()
            dialogAddRecipe.dismiss()
            bottomSheetDialog?.dismiss()
            val bundle = Bundle()
            bundle.putString("value", "New")
            bundle.putString("uri", submittedResult)
            findNavController(R.id.frameContainerMain).navigate(R.id.createCookBookFragment, bundle)
        }

        rlDoneBtn.setOnClickListener {
            if (spinnerActivityLevel.text.toString().equals("", true)) {
                BaseApplication.alertError(this, ErrorMessage.selectCookBookError, false)
            } else {
                val cookbooktype = cookbookList[spinnerActivityLevel.selectedIndex].id
                recipeLikeAndUnlikeDataUrl(submittedResult.toString(), "0",cookbooktype.toString(),dialogAddRecipe)
            }
        }

    }

    private fun recipeLikeAndUnlikeDataUrl(submittedResult: String, likeType: String, cookbooktype: String, dialogAddRecipe: Dialog?) {
        BaseApplication.showMe(this)
        lifecycleScope.launch {
            mealRoutineViewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponseUrl(it,dialogAddRecipe)
            }, submittedResult, likeType, cookbooktype)
        }
    }

    private fun searchMealUrlApi(submittedResult: String?) {
        if (BaseApplication.isOnline(this)) {
            layMainProgress!!.visibility=View.VISIBLE
            lifecycleScope.launch {
                mealRoutineViewModel.getMealByUrl({
                    layMainProgress!!.visibility=View.GONE
                    when (it) {
                        is NetworkResult.Success -> handleSuccessMealResponse(it.data.toString())
                        is NetworkResult.Error -> showAlert(it.message, false)
                        else -> showAlert(it.message, false)
                    }
                },submittedResult)
            }
        } else {
            BaseApplication.alertError(this, ErrorMessage.networkError, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessMealResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SearchMealUrlModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success==true) {
                if (apiModel.data!=null){
                    showURlData(apiModel.data)
                }else{
                    Toast.makeText(this,apiModel.message,Toast.LENGTH_LONG).show()
                }

            } else {
                apiModel.code?.let { apiModel.message?.let { it1 -> handleError(it, it1) } }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showURlData(data: SearchMealUrlModelData?) {
        try {
            if (data!!.label!=null){
                tvTitleName!!.text=data.label.toString()
            }

            if (data.uri!=null){
                uri=data.uri.toString()
            }


            if (data.image !=null){
                Glide.with(this)
                    .load(data.image)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            lay_progess?.visibility= View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            lay_progess?.visibility= View.GONE
                            return false
                        }
                    })
                    .into(imageData!!)
            }else{
                lay_progess?.visibility= View.GONE
            }

            if (data.source!=null){
                tvTitleDesc!!.text="By "+data.source.toString()
            }

            if (data.ingredients!=null && data.ingredients.size>0){
                adapterUrlIngredients = AdapterUrlIngredientItem(data.ingredients, this)
                rcyIngredients?.adapter = adapterUrlIngredients
                rcyIngredients?.visibility=View.VISIBLE
            }else{
                rcyIngredients?.visibility=View.GONE
            }
            loadDataStatus=true


        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }

    private fun setEvent(){
        binding.llHome.setOnClickListener(this)
        binding.llSearch.setOnClickListener(this)
        binding.llAddRecipe.setOnClickListener(this)
        binding.llPlan.setOnClickListener(this)
        binding.llCooked.setOnClickListener(this)
        binding.relAddRecipeWeb.setOnClickListener(this)
        binding.relCreateNewRecipe.setOnClickListener(this)
        binding.relRecipeImage.setOnClickListener(this)


        binding.cardViewAddRecipe.setOnClickListener {
            binding.cardViewAddRecipe.visibility = View.GONE
            val bundle = Bundle().apply {
                putString("ClickedUrl","")
            }
            findNavController(R.id.frameContainerMain).navigate(R.id.searchFragment,bundle)
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun handleDeepLink() {
        val navGraph = navController.navInflater.inflate(R.navigation.main_graph)
        navGraph.setStartDestination(R.id.homeFragment)
        navController.graph = navGraph
        // Get the intent that started this activity
        val intent = intent
        // Check if the intent contains a URI (deep link)
        if (intent?.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
                Log.d("DeepLink", "Received URI: $uri")
            if (data != null && data.scheme == "mykai" && data.host == "property") {
                val screenName = data.getQueryParameter("ScreenName")
               // val affiliateName = data.getQueryParameter("providerName")
//                val affiliateImage = data.getQueryParameter("ItemName")
                val cookbooksId = data.getQueryParameter("CookbooksID")
               // val referralCode = data.getQueryParameter("Referrer")
                val itemName = data.getQueryParameter("ItemName")

                Log.d("***********","$screenName  & $cookbooksId")

                if (screenName.equals("CookBooksType") && cookbooksId != null) {
                    sessionManagement.setCookBookId(cookbooksId.toString())
                    sessionManagement.setCookBookName(itemName.toString())
                    val bundle= Bundle()
                    bundle.putString("Screen","share")
                    navController.navigate(R.id.christmasCollectionFragment,bundle)
                }
            }
        }
    }

    private fun startDestination() {
        val navGraph = navController.navInflater.inflate(R.navigation.main_graph)
        navGraph.setStartDestination(R.id.homeFragment)
        navController.graph = navGraph
        if (sessionManagement.getCookBookShare().toString().equals("CookBooksType",true)){
            val bundle= Bundle()
            bundle.putString("Screen","share")
            navController.navigate(R.id.christmasCollectionFragment,bundle)
        }
    }

    fun changeBottom(status: String) {
        val selectedColor = ContextCompat.getColor(this, R.color.light_green)
        val defaultColor = ContextCompat.getColor(this, R.color.light_grays)
        val textDefaultColor = ContextCompat.getColor(this, R.color.black)
        val views = listOf("home", "search", "addRecipe", "plan", "cooked")
        views.forEach { view ->
            val isSelected = status.equals(view, true)
            val color = if (isSelected) selectedColor else defaultColor
            val textColor = if (isSelected) selectedColor else textDefaultColor
            val visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

            when (view) {
                "home" -> {
                    binding.imgHome.setColorFilter(color)
                    binding.tvHome.setTextColor(textColor)
                    binding.llHomeIndicator.visibility = visibility
                }
                "search" -> {
                    binding.imgSearch.setColorFilter(color)
                    binding.tvSearch.setTextColor(textColor)
                    binding.llSearchIndicator.visibility = visibility
                }
                "addRecipe" -> {
                    binding.imgAddRecipe.setColorFilter(color)
                    binding.tvAddRecipe.setTextColor(textColor)
                    binding.llAddRecipeIndicator.visibility = visibility
                }
                "plan" -> {
                    binding.imgPlan.setColorFilter(color)
                    binding.tvPlan.setTextColor(textColor)
                    binding.llPlanIndicator.visibility = visibility
                }
                "cooked" -> {
                    binding.imgCooked.setColorFilter(color)
                    binding.tvCooked.setTextColor(textColor)
                    binding.llCookedIndicator.visibility = visibility
                }
            }
        }
        binding.cardViewAddRecipe.visibility = if (status.equals("addRecipe", true)) View.VISIBLE else View.GONE
    }

    /// add recipe screen
    private fun addRecipeFromWeb() {
        val dialogWeb = Dialog(this)
        dialogWeb.setContentView(R.layout.alert_dialog_add_recipe_form_web)
        dialogWeb.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogWeb.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val etPasteURl = dialogWeb.findViewById<EditText>(R.id.etPasteURl)
        val rlSearchRecipe = dialogWeb.findViewById<RelativeLayout>(R.id.rlSearchRecipe)
        val imageCrossWeb = dialogWeb.findViewById<ImageView>(R.id.imageCrossWeb)
        dialogWeb.show()
        dialogWeb.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        imageCrossWeb.setOnClickListener{
            dialogWeb.dismiss()
        }

        rlSearchRecipe.setOnClickListener {
            if (etPasteURl.text.toString().isEmpty()) {
                commonWorkUtils.alertDialog(this, ErrorMessage.pasteUrl, false)
            }else {
                dialogWeb.dismiss()
                dialogAddRecipe?.dismiss()
                val intent = Intent(this, WebViewByUrlActivity::class.java)
                intent.putExtra("url", etPasteURl.text.toString().trim())
                resultLauncher.launch(intent)
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun dialogDailyInspiration() {
        dialog = Dialog(this@MainActivity, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.alert_dialog_daily_inspiration)
            window?.attributes = WindowManager.LayoutParams().apply { copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            layOnBoardingIndicator = findViewById(R.id.layonboarding_indicator)

            viewPager = findViewById(R.id.viewPager)
            val tvnodata = findViewById<TextView>(R.id.tvnodata)

            // Top
            laybuttonplabasket = findViewById(R.id.laybuttonplabasket)
            val llBreakfast = findViewById<LinearLayout>(R.id.llBreakfast)
            val llLunch = findViewById<LinearLayout>(R.id.llLunch)
            val llDinner = findViewById<LinearLayout>(R.id.llDinner)
            val llSnaks = findViewById<LinearLayout>(R.id.llSnaks)
            val llBrunch = findViewById<LinearLayout>(R.id.llBrunch)
            val layRoot = findViewById<RelativeLayout>(R.id.layRoot)

            // Text
            val textBreakfast = findViewById<TextView>(R.id.textBreakfast)
            val textLunch = findViewById<TextView>(R.id.textLunch)
            val textDinner = findViewById<TextView>(R.id.textDinner)
            val textSnaks = findViewById<TextView>(R.id.textSnaks)
            val textBrunch = findViewById<TextView>(R.id.textBrunch)

            // Bottom view
            val viewBreakfast = findViewById<View>(R.id.viewBreakfast)
            val viewLunch = findViewById<View>(R.id.viewLunch)
            val viewDinner = findViewById<View>(R.id.viewDinner)
            val viewSnaks = findViewById<View>(R.id.viewSnaks)
            val viewBrunch = findViewById<View>(R.id.viewBrunch)

            val rlAddPlanButton = findViewById<RelativeLayout>(R.id.rlAddPlanButton)
            val rlAddCartButton = findViewById<RelativeLayout>(R.id.rlAddCartButton)

            fun setMealClickListener(mealLayout: View, mealView: View, mealText: TextView, mealName: String) {
                mealLayout.setOnClickListener {
                    listOf(viewBreakfast, viewLunch, viewDinner, viewSnaks, viewBrunch).forEach { it.visibility = View.INVISIBLE }
                    mealView.visibility = View.VISIBLE

                    listOf(textBreakfast, textLunch, textDinner, textSnaks, textBrunch).forEach {
                        it.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.grey))
                    }
                    mealText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.orange))
                    updateList(mealName,viewPager,tvnodata)
                }
            }

            setMealClickListener(llBreakfast, viewBreakfast, textBreakfast, ErrorMessage.Breakfast)
            setMealClickListener(llLunch, viewLunch, textLunch, ErrorMessage.Lunch)
            setMealClickListener(llDinner, viewDinner, textDinner, ErrorMessage.Dinner)
            setMealClickListener(llSnaks, viewSnaks, textSnaks, ErrorMessage.Snacks)
            setMealClickListener(llBrunch, viewBrunch, textBrunch, ErrorMessage.Brunch)


            layRoot.setOnClickListener {
                dismiss()
            }

            recipesModel?.let { model ->
                model.Breakfast?.let { breakfast ->
                    adapter = ImageViewPagerAdapter(this@MainActivity, breakfast,this@MainActivity)
                    viewPager?.adapter = adapter
                    viewPager?.visibility = View.VISIBLE
                    laybuttonplabasket?.visibility = View.VISIBLE
                    layOnBoardingIndicator.visibility = View.VISIBLE
                    tvnodata.visibility = View.GONE
                    indicatorAdapter = IndicatorAdapter(breakfast.size)
                    layOnBoardingIndicator.adapter = indicatorAdapter
                } ?: run {
                    viewPager?.visibility = View.GONE
                    laybuttonplabasket?.visibility = View.GONE
                    layOnBoardingIndicator.visibility = View.GONE
                    tvnodata.visibility = View.VISIBLE
                }
            }

            viewPager?.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    indicatorAdapter.updateSelectedPosition(position)
                    smoothScrollIndicator(position)
                }
            })

            rlAddPlanButton.setOnClickListener {
                if (Subscription_status==1){
                    if (addmeal!! < 1){
                        chooseDayDialog()
                    }else{
                        subscriptionAlertError(this@MainActivity)
                    }
                }else{
                    chooseDayDialog()
                }
            }

            rlAddCartButton.setOnClickListener {
                if (BaseApplication.isOnline(this@MainActivity)) {
                    Log.d("mealType", "********$mealType")
                    val recipesMap = mapOf(
                        ErrorMessage.Breakfast to recipesModel?.Breakfast,
                        ErrorMessage.Lunch to recipesModel?.Lunch,
                        ErrorMessage.Dinner to recipesModel?.Dinner,
                        ErrorMessage.Snacks to recipesModel?.Snack,
                        ErrorMessage.Brunch to recipesModel?.Teatime
                    )
                    addBasketData(viewPager?.currentItem?.let { it1 -> recipesMap[mealType]?.get(it1)?.recipe?.uri })
                } else {
                    BaseApplication.alertError(this@MainActivity, ErrorMessage.networkError, false)
                }
            }
            show()
        }
    }

    private fun smoothScrollIndicator(position: Int) {
        val layoutManager = layOnBoardingIndicator.layoutManager as LinearLayoutManager
        val visibleRange = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition()

        // Optional: center when there are more than 5
        if (visibleRange >= 4) {
            layOnBoardingIndicator.smoothScrollToPosition(position)
        }
    }


    private fun addBasketData(uri:String?){
        BaseApplication.showMe(this)
        lifecycleScope.launch {
            mealRoutineViewModel.addBasketRequest({
                BaseApplication.dismissMe()
                handleBasketApiResponse(it)
            }, uri.toString(), "",mealType)
        }
    }

    private fun handleBasketApiResponse(
        result: NetworkResult<String>
    ) {
        when (result) {
            is NetworkResult.Success -> handleBasketSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(this, message, status)
    }


    @SuppressLint("SetTextI18n")
    private fun handleBasketSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialog?.dismiss()
                Toast.makeText(this, apiModel.message, Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun updateList(type: String, viewPager: ViewPager2?, tvnodata: TextView){
        mealType=type
        val recipesMap = mapOf(
            ErrorMessage.Breakfast to recipesModel?.Breakfast,
            ErrorMessage.Lunch to recipesModel?.Lunch,
            ErrorMessage.Dinner to recipesModel?.Dinner,
            ErrorMessage.Snacks to recipesModel?.Snack,
            ErrorMessage.Brunch to recipesModel?.Teatime
        )

        recipesMap[type]?.let { breakfast ->
            adapter = ImageViewPagerAdapter(this@MainActivity, breakfast,this@MainActivity)
            viewPager?.adapter = adapter
            viewPager?.visibility = View.VISIBLE
            laybuttonplabasket?.visibility = View.VISIBLE
            layOnBoardingIndicator.visibility = View.VISIBLE
            tvnodata.visibility = View.GONE
            indicatorAdapter = IndicatorAdapter(breakfast.size)
            layOnBoardingIndicator.adapter = indicatorAdapter
        } ?: run {
            viewPager?.visibility = View.GONE
            laybuttonplabasket?.visibility = View.GONE
            layOnBoardingIndicator.visibility = View.GONE
            tvnodata.visibility = View.VISIBLE
        }

        viewPager?.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                indicatorAdapter.updateSelectedPosition(position)
                smoothScrollIndicator(position)
            }
        })



    }

    private fun fetchDataOnLoad() {
        if (BaseApplication.isOnline(this@MainActivity)) {
            fetchRecipeDetailsData()
        } else {
            BaseApplication.alertError(this@MainActivity, ErrorMessage.networkError, false)
        }
    }

    private fun fetchRecipeDetailsData() {
        lifecycleScope.launch {
            mealRoutineViewModel.planRequest({
                handleApiResponse(it)
            }, "q")
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlertFunction(result.message, false)
            else -> showAlertFunction(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, PlanApiResponse::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null) {
                    showData(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlertFunction(e.message, false)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        } else {
            Log.d("@Error", "****$message")
        }
    }

    private fun showData(data: Data) {
        recipesModel = data.recipes
        dialogDailyInspiration()
    }

    @SuppressLint("SetTextI18n")
    private fun chooseDayDialog() {
        val dialogChooseDay = Dialog(this)
        dialogChooseDay.setContentView(R.layout.alert_dialog_choose_day)
        dialogChooseDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogChooseDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        rcyChooseDaySch = dialogChooseDay.findViewById(R.id.rcyChooseDaySch)
        tvWeekRange = dialogChooseDay.findViewById(R.id.tvWeekRange)
        val rlDoneBtn = dialogChooseDay.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        val btnPrevious = dialogChooseDay.findViewById<ImageView>(R.id.btnPrevious)
        val btnNext = dialogChooseDay.findViewById<ImageView>(R.id.btnNext)
        dialogChooseDay.show()
        dialogChooseDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dataList.clear()
        val daysOfWeek =
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        for (day in daysOfWeek) {
            val data = DataModel().apply {
                title = day
                isOpen = false
                type = "CookingSchedule"
                date = ""
            }
            dataList.add(data)
        }

        showWeekDates()

        rlDoneBtn.setOnClickListener {
            var status = false
            for (it in dataList) {
                if (it.isOpen) {
                    status = true
                    break // Exit the loop early
                }
            }
            if (status) {
                chooseDayMealTypeDialog()
                dialogChooseDay.dismiss()
            } else {
                BaseApplication.alertError(this, ErrorMessage.weekNameError, false)
            }
        }

        btnPrevious.setOnClickListener {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedCurrentDate = dateFormat.format(currentDate)
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
            val currentDate1 = calendar.time
            val (startDate, endDate) = getWeekDates(currentDate1)
            println("Week Start Date: ${formatDate(startDate)}")
            println("Week End Date: ${formatDate(endDate)}")
            // Get all dates between startDate and endDate
            val daysBetween = getDaysBetween(startDate, endDate)
            // Mark the current date as selected in the list
            val updatedDaysBetween1 = daysBetween.map { dateModel ->
                dateModel.apply {
                    status = (date == formattedCurrentDate) // Compare formatted strings
                }
            }
            var status=false
            updatedDaysBetween1.forEach {
                status = it.date >= BaseApplication.currentDateFormat().toString()
            }
            if (status){
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
                currentDate = calendar.time
                // Display next week dates
                println("\nAfter clicking 'Next':")
                showWeekDates()
            }else{
                Toast.makeText(this,ErrorMessage.slideError,Toast.LENGTH_LONG).show()
            }
        }

        btnNext.setOnClickListener {
            // Simulate clicking the "Next" button to move to the next week
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, 1) // Move to next week
            currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDates()
        }





    }

    private fun chooseDayMealTypeDialog() {
        val dialogChooseMealDay = Dialog(this)
        dialogChooseMealDay.setContentView(R.layout.alert_dialog_choose_day_meal_type)
        dialogChooseMealDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogChooseMealDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogChooseMealDay.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        // button event listener
        val tvBreakfast = dialogChooseMealDay.findViewById<TextView>(R.id.tvBreakfast)
        val tvLunch = dialogChooseMealDay.findViewById<TextView>(R.id.tvLunch)
        val tvDinner = dialogChooseMealDay.findViewById<TextView>(R.id.tvDinner)
        val tvSnacks = dialogChooseMealDay.findViewById<TextView>(R.id.tvSnacks)
        val tvTeatime = dialogChooseMealDay.findViewById<TextView>(R.id.tvTeatime)
        dialogChooseMealDay.show()
        dialogChooseMealDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        var type = ""

        fun updateSelection(
            selectedType: String,
            selectedView: TextView,
            allViews: List<TextView>
        ) {
            type = selectedType
            allViews.forEach { view ->
                val drawable =
                    if (view == selectedView) R.drawable.radio_select_icon else R.drawable.radio_unselect_icon
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
            }
        }

        val allViews = listOf(tvBreakfast, tvLunch, tvDinner, tvSnacks, tvTeatime)

        tvBreakfast.setOnClickListener {
            updateSelection(ErrorMessage.Breakfast, tvBreakfast, allViews)
        }

        tvLunch.setOnClickListener {
            updateSelection(ErrorMessage.Lunch, tvLunch, allViews)
        }

        tvDinner.setOnClickListener {
            updateSelection(ErrorMessage.Dinner, tvDinner, allViews)
        }

        tvSnacks.setOnClickListener {
            updateSelection(ErrorMessage.Snacks, tvSnacks, allViews)
        }

        tvTeatime.setOnClickListener {
            updateSelection(ErrorMessage.Brunch, tvTeatime, allViews)
        }


        rlDoneBtn.setOnClickListener {
            if (BaseApplication.isOnline(this)) {
                if (type.equals("", true)) {
                    BaseApplication.alertError(this, ErrorMessage.mealTypeError, false)
                } else {
                    addToPlan(dialogChooseMealDay, type)
                }
            } else {
                BaseApplication.alertError(this, ErrorMessage.networkError, false)
            }
        }

    }

    private fun addToPlan(dialogChooseMealDay: Dialog, selectType: String) {
        Log.d("mealType", "********$mealType")
        val recipesMap = mapOf(
            ErrorMessage.Breakfast to recipesModel?.Breakfast,
            ErrorMessage.Lunch to recipesModel?.Lunch,
            ErrorMessage.Dinner to recipesModel?.Dinner,
            ErrorMessage.Snacks to recipesModel?.Snack,
            ErrorMessage.Brunch to recipesModel?.Teatime
        )
        // Create a JsonObject for the main JSON structure
        val jsonObject = JsonObject()

        // Safely get the item and position
        val item = viewPager?.let { recipesMap[mealType]?.get(it.currentItem) }
        if (item != null) {
            if (item.recipe?.uri != null) {
                jsonObject.addProperty("type", selectType)
                jsonObject.addProperty("uri", item.recipe.uri)
                // Create a JsonArray for ingredients
                val jsonArray = JsonArray()
                val latestList = getDaysBetween(startDate, endDate)
                for (i in dataList.indices) {
                    val data = DataModel()
                    data.isOpen = dataList[i].isOpen
                    data.title = dataList[i].title
                    data.date = latestList[i].date
                    dataList[i] = data
                }
                // Iterate through the ingredients and add them to the array if status is true
                dataList.forEach { data ->
                    if (data.isOpen) {
                        // Create a JsonObject for each ingredient
                        val ingredientObject = JsonObject()
                        ingredientObject.addProperty("date", data.date)

                        ingredientObject.addProperty("day", data.title)
                        // Add the ingredient object to the array
                        jsonArray.add(ingredientObject)
                    }
                }

                // Add the ingredients array to the main JSON object
                jsonObject.add("slot", jsonArray)
            }
        }

        BaseApplication.showMe(this)
        lifecycleScope.launch {
            mealRoutineViewModel.recipeAddToPlanRequest({
                BaseApplication.dismissMe()
                handleApiAddToPlanResponse(it, dialogChooseMealDay)
            }, jsonObject)
        }
    }

    private fun handleApiAddToPlanResponse(
        result: NetworkResult<String>,
        dialogChooseMealDay: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddToPlanResponse(result.data.toString(), dialogChooseMealDay)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddToPlanResponse(data: String, dialogChooseMealDay: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dataList.clear()
                dialog?.dismiss()
                dialogChooseMealDay.dismiss()
                Toast.makeText(this, apiModel.message, Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    fun showWeekDates() {
        Log.d("currentDate :- ", "******$currentDate")
        val (startDate, endDate) = getWeekDates(currentDate)
        this.startDate = startDate
        this.endDate = endDate

        println("Week Start Date: ${formatDate(startDate)}")
        println("Week End Date: ${formatDate(endDate)}")

        // Get all dates between startDate and endDate
        val daysBetween = getDaysBetween(startDate, endDate)
        // Mark the current date as selected in the list
        daysBetween.zip(dataList).forEach { (dateModel, dataModel) ->
            dataModel.date = dateModel.date
            dataModel.isOpen = false
        }

        rcyChooseDaySch?.adapter = ChooseDayAdapter(dataList, this)


        // Print the dates
        println("Days between $startDate and ${endDate}:")
        daysBetween.forEach { println(it) }
        tvWeekRange?.text = "" + formatDate(startDate) + "-" + formatDate(endDate)

    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getDaysBetween(startDate: Date, endDate: Date): MutableList<DateModel> {
        val dateList = mutableListOf<DateModel>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Format for the date
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault()) // Format for the day name (e.g., Monday)

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (!calendar.time.after(endDate)) {
            val date = dateFormat.format(calendar.time)  // Get the formatted date (yyyy-MM-dd)
            val dayName = dayFormat.format(calendar.time)  // Get the day name (Monday, Tuesday, etc.)

            val localDate= DateModel()
            localDate.day=dayName
            localDate.date=date
            // Combine both the day name and the date
//            dateList.add("$dayName, $date")
            dateList.add(localDate)


            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateList
    }

    private fun getWeekDates(currentDate: Date): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        // Set the calendar to the start of the week (Monday)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = calendar.time

        // Set the calendar to the end of the week (Saturday)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time
        return Pair(startOfWeek, endOfWeek)
    }

    /// use switch case to redirection or handle click event
    override fun onClick(v:  View?) {
        when (v!!.id) {
            R.id.llHome -> {
                binding.cardViewAddRecipe.visibility = View.GONE
                findNavController(R.id.frameContainerMain).navigate(R.id.homeFragment)
            }

            R.id.llSearch -> {
                if (alertStatus){
                    searchRecipeDialog()
                }else{
                    binding.cardViewAddRecipe.visibility = View.GONE
                    val bundle = Bundle().apply {
                        putString("ClickedUrl","")
                    }
                    findNavController(R.id.frameContainerMain).navigate(R.id.searchFragment,bundle)
                }
            }

            R.id.llAddRecipe -> {
                addRecipeAlert(this@MainActivity)
//                findNavController(R.id.frameContainerMain).navigate(R.id.searchFragmentDummy)
//                binding.cardViewAddRecipe.visibility = View.VISIBLE
            }

            R.id.llPlan -> {
                binding.cardViewAddRecipe.visibility = View.GONE
                findNavController(R.id.frameContainerMain).navigate(R.id.planFragment)
            }

            R.id.llCooked -> {
                binding.cardViewAddRecipe.visibility = View.GONE
                findNavController(R.id.frameContainerMain).navigate(R.id.cookedFragment)
            }

            R.id.relAddRecipeWeb -> {
                if (Subscription_status==1){
                    if (urlSearch!! <=2){
                        addRecipeFromWeb()
                    }else{
                        subscriptionAlertError(this)
                    }
                }else{
                    addRecipeFromWeb()
                }
            }

            R.id.relCreateNewRecipe -> {
                binding.cardViewAddRecipe.visibility = View.GONE
                val bundle = Bundle().apply {
                    putString("name","")
                }
                findNavController(R.id.frameContainerMain).navigate(R.id.createRecipeFragment,bundle)
            }

            R.id.relRecipeImage->{
                if (Subscription_status==1){
                    if (imageSearch!! <=2){
                        binding.cardViewAddRecipe.visibility = View.GONE
                        findNavController(R.id.frameContainerMain).navigate(R.id.createRecipeImageFragment)
                    }else{
                        subscriptionAlertError(this)
                    }

                }else{
                    binding.cardViewAddRecipe.visibility = View.GONE
                    findNavController(R.id.frameContainerMain).navigate(R.id.createRecipeImageFragment)
                }
            }
        }
    }

    fun mealRoutineSelectApi(onResult: (MutableList<MealRoutineModelData>) -> Unit) {
        // Show the loading indicator
        BaseApplication.showMe(this)

        // Launch the coroutine to perform the API call
        lifecycleScope.launch {
            mealRoutineViewModel.userPreferencesApi { networkResult ->
                // Dismiss the loading indicator
                BaseApplication.dismissMe()

                val mealRoutineList = mutableListOf<MealRoutineModelData>()

                when (networkResult) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(networkResult.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                mealRoutineList.addAll(bodyModel.data.mealroutine)
                            } else {
                                // Handle specific error cases
                                handleError(bodyModel.code,bodyModel.message)
                            }
                        } catch (e: Exception) {
                            Log.d("MealRoutine@@", "message:---" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(networkResult.message, false)
                    }

                    else -> {
                        showAlertFunction(networkResult.message, false)
                    }
                }

                // Return the result through the callback
                onResult(mealRoutineList)
            }
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(this, message, status)
    }

    private fun getFcmToken() {
        lifecycleScope.launch {
            Log.d("Token ","******"+BaseApplication.fetchFcmToken())
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        when (status) {
            "4" -> {
                if (Subscription_status==1){
                    if (favorite!! <= 2){
                    if (BaseApplication.isOnline(this)) {
                        toggleIsLike()
                    } else {
                        BaseApplication.alertError(this, ErrorMessage.networkError, false)
                    }
                    }else{
                        subscriptionAlertError(this)
                   }

            }else{
                if (BaseApplication.isOnline(this)) {
                    toggleIsLike()
                } else {
                    BaseApplication.alertError(this, ErrorMessage.networkError, false)
                }
            }
            }
        }
    }


    private fun toggleIsLike() {
        // Map the type to the corresponding list and adapter
        Log.d("mealType", "********$mealType")
        val recipesMap = mapOf(
            ErrorMessage.Breakfast to recipesModel?.Breakfast,
            ErrorMessage.Lunch to recipesModel?.Lunch,
            ErrorMessage.Dinner to recipesModel?.Dinner,
            ErrorMessage.Snacks to recipesModel?.Snack,
            ErrorMessage.Brunch to recipesModel?.Teatime
        )
        // Safely get the item and position
        val item = viewPager?.let { recipesMap[mealType]?.get(it.currentItem) }
        if (item != null) {
            if (item.recipe?.uri != null) {
                val newLikeStatus = if (item.is_like == 0) "1" else "0"
                if (newLikeStatus.equals("0", true)) {
                    recipeLikeAndUnlikeData(item, newLikeStatus,"",null,recipesMap[mealType])
                } else {
                    addFavTypeDialog(item, recipesMap[mealType], newLikeStatus)
                }
            }
        }
    }

    private fun addFavTypeDialog(
        item: BreakfastModel,
        breakfastModels: MutableList<BreakfastModel>?,
        newLikeStatus: String
    ) {
        val dialogAddRecipe = Dialog(this)
        dialogAddRecipe.setContentView(R.layout.alert_dialog_add_recipe)
        dialogAddRecipe.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogAddRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogAddRecipe.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        spinnerActivityLevel = dialogAddRecipe.findViewById(R.id.spinnerActivityLevel)
        val relCreateNewCookBook =
            dialogAddRecipe.findViewById<RelativeLayout>(R.id.relCreateNewCookBook)
        val imgCheckBoxOrange = dialogAddRecipe.findViewById<ImageView>(R.id.imgCheckBoxOrange)
        cookbookList.clear()
        val data = com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data(
            "",
            "",
            0,
            "",
            "Favorites",
            0,
            "",
            0
        )
        cookbookList.add(0, data)
        spinnerActivityLevel.setItems(cookbookList.map { it.name })

        dialogAddRecipe.show()
        dialogAddRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        getCookBookList()

        relCreateNewCookBook.setOnClickListener {
            relCreateNewCookBook.setBackgroundResource(R.drawable.light_green_rectangular_bg)
            imgCheckBoxOrange.setImageResource(R.drawable.orange_uncheck_box_images)
            dialog?.dismiss()
            dialogAddRecipe.dismiss()
            val bundle = Bundle()
            bundle.putString("value", "New")
            bundle.putString("uri", item.recipe?.uri)
            findNavController(R.id.frameContainerMain).navigate(R.id.createCookBookFragment, bundle)
        }


        rlDoneBtn.setOnClickListener {
            if (spinnerActivityLevel.text.toString().equals("", true)) {
                BaseApplication.alertError(
                    this,
                    ErrorMessage.selectCookBookError,
                    false
                )
            } else {
                val cookbooktype = cookbookList[spinnerActivityLevel.selectedIndex].id
                recipeLikeAndUnlikeData(item, newLikeStatus,cookbooktype.toString(),dialogAddRecipe,breakfastModels)
            }
        }

    }

    private fun getCookBookList() {
        BaseApplication.showMe(this)
        lifecycleScope.launch {
            mealRoutineViewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }
        }
    }


    private fun handleApiCookBookResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    private fun handleApiSubscriptionResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleApiSubscriptionResponse(result.data.toString())
            is NetworkResult.Error -> Log.d("@Error","****"+result.message)
            else -> Log.d("@Error","****"+result.message)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
                    cookbookList.retainAll { it == cookbookList[0] }
                    cookbookList.addAll(apiModel.data)
                    // OR directly modify the original list
                    spinnerActivityLevel.setItems(cookbookList.map { it.name })
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleApiSubscriptionResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SubscriptionModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Log.d("subscription data","**********")
                apiModel.data?.let {
                    apiModel.data.Subscription_status?.let {
                        Subscription_status=it
                    }
                    apiModel.data.addmeal?.let {
                        addmeal=it
                    }
                    apiModel.data.favorite?.let {
                        favorite=it
                    }
                    apiModel.data.imageSearch?.let {
                        imageSearch=it
                    }
                    apiModel.data.urlSearch?.let {
                        urlSearch=it
                    }
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlertFunction(apiModel.message, true)
                }
            }
        } catch (e: Exception) {
            Log.d("@Error","****"+e.message)
        }
    }

    private fun recipeLikeAndUnlikeData(
        item: BreakfastModel,
        likeType: String,
        cookbooktype: String,
        dialogAddRecipe: Dialog?,
        breakfastModels: MutableList<BreakfastModel>?
    ) {
        BaseApplication.showMe(this)
        lifecycleScope.launch {
            mealRoutineViewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it, item,dialogAddRecipe,breakfastModels)
            }, item.recipe?.uri.toString(), likeType, cookbooktype)
        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        item: BreakfastModel,
        dialogAddRecipe: Dialog?,
        breakfastModels: MutableList<BreakfastModel>?
    ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(result.data.toString(), item, dialogAddRecipe,breakfastModels)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleLikeAndUnlikeApiResponseUrl(result: NetworkResult<String>, dialogAddRecipe: Dialog?, ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponseUrl(result.data.toString(), dialogAddRecipe)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponseUrl(data: String,dialogAddRecipe: Dialog?) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogAddRecipe?.dismiss()
                bottomSheetDialog?.dismiss()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(
        data: String,
        item: BreakfastModel,
        dialogAddRecipe: Dialog?,
        breakfastModels: MutableList<BreakfastModel>?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogAddRecipe?.dismiss()
                // Toggle the is_like value
                item.is_like = if (item.is_like == 0) 1 else 0
                viewPager?.currentItem?.let { breakfastModels?.set(it, item) }
                adapter.updateItem(breakfastModels!!)
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun startTimer(context: Context) {
        if (isRunning) return
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                Log.d("timer working","*****")
                checkAndShowDailyInspirations(context)
                handler?.postDelayed(this, INTERVAL_MS)
            }
        }
        handler?.post(runnable!!)
        isRunning = true
    }

    private fun stopTimer() {
        handler?.removeCallbacks(runnable!!)
        handler = null
        runnable = null
        isRunning = false
    }

    private fun checkAndShowDailyInspirations(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lastShownMillis = prefs.getLong(LAST_SHOWN_KEY, 0)
        val currentMillis = System.currentTimeMillis()
        Log.d("timer working","***** every time ")
        if (lastShownMillis != 0L) {
            val hoursPassed = (currentMillis - lastShownMillis).toDouble() / (1000 * 60 * 60)
            if (hoursPassed < 24) return
        }
        Log.d("timer working","***** 24 hours passed! Calling API now.")
        prefs.edit().putLong(LAST_SHOWN_KEY, currentMillis).apply()
        fetchDataOnLoad()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }


    @SuppressLint("SetTextI18n")
    fun subscriptionAlertError(context: Context){
        val dialog= Dialog(context, R.style.BottomSheetDialog)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.alert_dialog_subscription_error)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = layoutParams
        val tvTitle: TextView =dialog.findViewById(R.id.tv_text)
        val btnOk: RelativeLayout =dialog.findViewById(R.id.btn_okay)
        val btnCancel: ImageView =dialog.findViewById(R.id.crossImages)
        val layroot: RelativeLayout =dialog.findViewById(R.id.layroot)

        layroot.setOnClickListener {
            dialog.dismiss()
        }
        tvTitle.text=ErrorMessage.subscriptionError
        btnOk.setOnClickListener {
            dialog.dismiss()
            dialogAddRecipe?.dismiss()
            binding.cardViewAddRecipe.visibility=View.GONE
            val bundle = Bundle()
            bundle.putString("screen","main")
            findNavController(R.id.frameContainerMain).navigate(R.id.homeSubscriptionAllPlanFragment,bundle)
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



    private fun addRecipeAlert(context: Context){
        dialogAddRecipe= Dialog(context, R.style.BottomSheetDialog)
        dialogAddRecipe?.setCancelable(true)
        dialogAddRecipe?.setCanceledOnTouchOutside(true)
        dialogAddRecipe?.setContentView(R.layout.add_recipe_alert)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialogAddRecipe?.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialogAddRecipe?.window!!.attributes = layoutParams
        val relAddRecipeWeb: RelativeLayout? =dialogAddRecipe?.findViewById(R.id.relAddRecipeWeb)
        val relCreateNewRecipe: RelativeLayout? =dialogAddRecipe?.findViewById(R.id.relCreateNewRecipe)
        val relRecipeImage: RelativeLayout? =dialogAddRecipe?.findViewById(R.id.relRecipeImage)
        val layRoot: RelativeLayout? =dialogAddRecipe?.findViewById(R.id.layroot)

        layRoot?.setOnClickListener {
            dialogAddRecipe?.dismiss()
        }

        relRecipeImage?.setOnClickListener {
            if (Subscription_status==1){
                if (imageSearch!! <=2){
                    dialogAddRecipe?.dismiss()
                    findNavController(R.id.frameContainerMain).navigate(R.id.createRecipeImageFragment)
                }else{
                    subscriptionAlertError(this)
                }
            }else{
                dialogAddRecipe?.dismiss()
                findNavController(R.id.frameContainerMain).navigate(R.id.createRecipeImageFragment)
            }
        }


        relCreateNewRecipe?.setOnClickListener {
            dialogAddRecipe?.dismiss()
            val bundle = Bundle().apply {
                putString("name","")
            }
            findNavController(R.id.frameContainerMain).navigate(R.id.createRecipeFragment,bundle)
        }

        relAddRecipeWeb?.setOnClickListener {
            if (Subscription_status==1){
                if (urlSearch!! <=2){
                    addRecipeFromWeb()
                }else{
                    subscriptionAlertError(this)
                }
            }else{
                addRecipeFromWeb()
            }
        }
        dialogAddRecipe?.show()

    }


    fun upDateHomeData(){
        ViewModelProvider(this)[HomeViewModel::class.java].setData(null)
    }

    fun upBasket(){
        ViewModelProvider(this)[BasketScreenViewModel::class.java].setBasketData(null)
    }

    fun upBasketCheckOut(){
        ViewModelProvider(this)[CheckoutScreenViewModel::class.java].setCheckOutData(null)
    }

    fun upDatePlan(){
        ViewModelProvider(this)[PlanViewModel::class.java].setData(null)
        ViewModelProvider(this)[PlanViewModel::class.java].setPlanDate(null)
    }


    fun upDateGraph(){
        ViewModelProvider(this)[StatisticsViewModel::class.java].setGraphData(null,null,null,null,null)
        ViewModelProvider(this)[StatisticsViewModel::class.java].setGraphDataList(null)
    }

    private fun searchRecipeDialog() {
        val dialogSearchDialog =Dialog(this)
        dialogSearchDialog.setContentView(R.layout.alert_dialog_search_recipe)
        dialogSearchDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogSearchDialog.setCancelable(true)
        dialogSearchDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val relRecipeSearch = dialogSearchDialog.findViewById<RelativeLayout>(R.id.relRecipeSearch)
        val relFavouritesRecipes = dialogSearchDialog.findViewById<RelativeLayout>(R.id.relFavouritesRecipes)
        val relFromWeb = dialogSearchDialog.findViewById<RelativeLayout>(R.id.relFromWeb)
        val relAddYourOwnRecipe = dialogSearchDialog.findViewById<RelativeLayout>(R.id.relAddYourOwnRecipe)
        val relTakingAPicture = dialogSearchDialog.findViewById<RelativeLayout>(R.id.relTakingAPicture)

        val tvRecipeSearch = dialogSearchDialog.findViewById<TextView>(R.id.tvRecipeSearch)
        val tvFavouritesRecipes = dialogSearchDialog.findViewById<TextView>(R.id.tvFavouritesRecipes)
        val tvFromWeb = dialogSearchDialog.findViewById<TextView>(R.id.tvFromWeb)
        val tvAddYourOwnRecipe = dialogSearchDialog.findViewById<TextView>(R.id.tvAddYourOwnRecipe)
        val tvTakingAPicture = dialogSearchDialog.findViewById<TextView>(R.id.tvTakingAPicture)

        val rlSearch = dialogSearchDialog.findViewById<RelativeLayout>(R.id.rlSearch)
        val imgCrossSearch = dialogSearchDialog.findViewById<ImageView>(R.id.imgCrossSearch)

        dialogSearchDialog.show()
        dialogSearchDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        rlSearch.setOnClickListener {
            when (status) {
                "RecipeSearch" -> {
                    findNavController(R.id.frameContainerMain).navigate(R.id.searchFragment)
                }
                "FavouritesRecipes" -> {
                    findNavController(R.id.frameContainerMain).navigate(R.id.cookBookFragment)
                }
                "Web" -> {
                    addRecipeFromWeb()
                }
                "AddRecipe" -> {
                    val bundle = Bundle().apply {
                        putString("name","")
                    }
                    findNavController(R.id.frameContainerMain).navigate(R.id.createRecipeFragment,bundle)
                }
                else -> {
                    findNavController(R.id.frameContainerMain).navigate(R.id.createRecipeImageFragment)
                }
            }
            dialogSearchDialog.dismiss()
        }

        imgCrossSearch.setOnClickListener{
            dialogSearchDialog.dismiss()
        }

        fun updateSelection(selectedView: View, tvTakingAPicture: TextView) {
            val allViews = listOf(relRecipeSearch, relFavouritesRecipes, relFromWeb, relAddYourOwnRecipe, relTakingAPicture)
            val textViews = listOf(tvRecipeSearch, tvFavouritesRecipes, tvFromWeb, tvAddYourOwnRecipe, tvTakingAPicture)
            val drawableLeft = ContextCompat.getDrawable(this, R.drawable.orange_tick_icon) // Replace with your drawable
            allViews.forEach { it.setBackgroundResource(R.drawable.gray_box_border_bg) }
            textViews.forEach { it.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null) }
            selectedView.setBackgroundResource(R.drawable.orange_box_bg)
            tvTakingAPicture.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableLeft, null)
        }

        relRecipeSearch.setOnClickListener {
            status = "RecipeSearch"
            updateSelection(relRecipeSearch,tvRecipeSearch)
        }

        relFavouritesRecipes.setOnClickListener {
            status = "FavouritesRecipes"
            updateSelection(relFavouritesRecipes,tvFavouritesRecipes)
        }

        relFromWeb.setOnClickListener {
            if (Subscription_status==1){
                if (urlSearch!! <=2){
                    status = "Web"
                    updateSelection(relFromWeb,tvFromWeb)
                }else{
                    subscriptionAlertError(this)
                }

            }else{
                status = "Web"
                updateSelection(relFromWeb,tvFromWeb)
            }
        }

        relAddYourOwnRecipe.setOnClickListener {
            status = "AddRecipe"
            updateSelection(relAddYourOwnRecipe,tvAddYourOwnRecipe)
        }

        relTakingAPicture.setOnClickListener {
            if (Subscription_status==1){
                if (urlSearch!! <=2){
                    status = "TakingPicture"
                    updateSelection(relTakingAPicture,tvTakingAPicture)
                }else{
                    subscriptionAlertError(this)
                }
            }else{
                status = "TakingPicture"
                updateSelection(relTakingAPicture,tvTakingAPicture)
            }
        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 991) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show()
        }
    }



}
