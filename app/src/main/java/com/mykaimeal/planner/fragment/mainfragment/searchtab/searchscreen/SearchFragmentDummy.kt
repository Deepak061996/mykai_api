package com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterUrlIngredientItem
import com.mykaimeal.planner.adapter.SearchMealAdapter
import com.mykaimeal.planner.adapter.SearchMealCatAdapter
import com.mykaimeal.planner.adapter.SearchRecipeAdapter
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentSearchBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Category
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.MealType
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.SearchApiResponse
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SearchMealUrlModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SearchMealUrlModelData
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.viewmodel.SearchRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch



@AndroidEntryPoint
class SearchFragmentDummy : Fragment(), OnItemClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var searchRecipeAdapter: SearchRecipeAdapter? = null
    private var searchMealAdapter: SearchMealAdapter? = null
    private var searchMealCatAdapter: SearchMealCatAdapter? = null
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var sessionManagement: SessionManagement
    private lateinit var searchRecipeViewModel:SearchRecipeViewModel
    private var ingredient: MutableList<Ingredient>?= mutableListOf()
    private var mealType: MutableList<MealType>?= mutableListOf()
    private var category: MutableList<Category>?= mutableListOf()
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    private var clickedUrl: String = ""
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var rcyIngredients: RecyclerView? = null
    private var tvTitleName: TextView? = null
    private var tvTitleDesc: TextView? = null
    private var layMainProgress: View? = null
    private var imgRecipeLike: ImageView? = null
    private var adapterUrlIngredients: AdapterUrlIngredientItem? = null
    private var uri: String = ""
    private var loadDataStatus: Boolean = false

    private lateinit var spinnerActivityLevel: PowerSpinnerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        (activity as MainActivity?)?.changeBottom("addRecipe")
        (activity as MainActivity?)?.alertStatus=false
        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.VISIBLE
            it.llBottomNavigation.visibility = View.VISIBLE
        }

        clickedUrl = arguments?.getString("ClickedUrl", "")?:""


        searchRecipeViewModel = ViewModelProvider(requireActivity())[SearchRecipeViewModel::class.java]
        cookbookList.clear()
        val data= com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("","",0,"","Favourites",0,"",0)
        cookbookList.add(0,data)
        commonWorkUtils = CommonWorkUtils(requireActivity())
        sessionManagement = SessionManagement(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })


        binding.cardViewAddRecipe.visibility=View.GONE

        initialize()

        if (searchRecipeViewModel.data!=null){
            showData(searchRecipeViewModel.data)
        }else{
            // This Api call when the screen in loaded
            lunchApi()
        }


        return binding.root
    }

    private fun searchBottomDialog() {
        bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog)
        bottomSheetDialog!!.setContentView(R.layout.bottom_import_recipe_url)
        rcyIngredients = bottomSheetDialog!!.findViewById(R.id.rcyIngredients)
        tvTitleName = bottomSheetDialog!!.findViewById(R.id.tvTitleName)
        tvTitleDesc = bottomSheetDialog!!.findViewById(R.id.tvTitleDesc)
        layMainProgress = bottomSheetDialog!!.findViewById(R.id.layMainProgress)
        imgRecipeLike = bottomSheetDialog!!.findViewById(R.id.imgRecipeLike)
        bottomSheetDialog!!.show()

        imgRecipeLike!!.setOnClickListener{
            if (loadDataStatus){
                addFavTypeDialog()
            }
        }

        searchMealUrlApi()

    }


    private fun addFavTypeDialog() {
        val dialogAddRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogAddRecipe.setContentView(R.layout.alert_dialog_add_recipe)
        dialogAddRecipe.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogAddRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogAddRecipe.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        spinnerActivityLevel = dialogAddRecipe.findViewById(R.id.spinnerActivityLevel)
        val relCreateNewCookBook = dialogAddRecipe.findViewById<RelativeLayout>(R.id.relCreateNewCookBook)
        val imgCheckBoxOrange = dialogAddRecipe.findViewById<ImageView>(R.id.imgCheckBoxOrange)

        val newLikeStatus = 0
        spinnerActivityLevel.setItems(cookbookList.map { it.name })

        dialogAddRecipe.show()
        dialogAddRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        getCookBookList()

        relCreateNewCookBook.setOnClickListener{
            relCreateNewCookBook.setBackgroundResource(R.drawable.light_green_rectangular_bg)
            imgCheckBoxOrange.setImageResource(R.drawable.orange_uncheck_box_images)
            dialogAddRecipe.dismiss()
            val bundle=Bundle()
            bundle.putString("value","New")
            bundle.putString("uri",uri)
            findNavController().navigate(R.id.createCookBookFragment,bundle)
        }


        rlDoneBtn.setOnClickListener{
            if (spinnerActivityLevel.text.toString().equals(ErrorMessage.cookBookSelectError,true)){
                BaseApplication.alertError(requireContext(), ErrorMessage.selectCookBookError, false)
            }else {
                val cookBookType = cookbookList[spinnerActivityLevel.selectedIndex].id
                recipeLikeAndUnlikeData(newLikeStatus.toString(),cookBookType.toString(), dialogAddRecipe)
            }
        }
    }

    private fun recipeLikeAndUnlikeData(
        likeType:String,
        cookBookType: String,
        dialogAddRecipe: Dialog?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            searchRecipeViewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it,dialogAddRecipe)
            }, uri,likeType,cookBookType)
        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        dialogAddRecipe: Dialog?
    ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(result.data.toString(),dialogAddRecipe)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(
        data: String,
        dialogAddRecipe: Dialog?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogAddRecipe?.dismiss()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun getCookBookList(){
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            searchRecipeViewModel.getCookBookRequest {
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

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data!=null && apiModel.data.size>0){
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

    private fun searchMealUrlApi() {
        if (BaseApplication.isOnline(requireActivity())) {
            layMainProgress!!.visibility=View.VISIBLE
            lifecycleScope.launch {
                searchRecipeViewModel.getMealByUrl({
                    layMainProgress!!.visibility=View.GONE
                    when (it) {
                        is NetworkResult.Success -> handleSuccessMealResponse(it.data.toString())
                        is NetworkResult.Error -> showAlert(it.message, false)
                        else -> showAlert(it.message, false)
                    }
                },clickedUrl)
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessMealResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SearchMealUrlModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success==true) {
                showURlData(apiModel.data)
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

            if (data.source!=null){
                tvTitleDesc!!.text="By "+data.source.toString()
            }

            if (data.ingredients!=null && data.ingredients.size>0){
                adapterUrlIngredients = AdapterUrlIngredientItem(data.ingredients, requireActivity())
                rcyIngredients!!.adapter = adapterUrlIngredients
                rcyIngredients!!.visibility=View.VISIBLE
            }else{
                rcyIngredients!!.visibility=View.GONE
            }
            loadDataStatus=true
        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }


    private fun lunchApi() {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                searchRecipeViewModel.recipeforSearchApi {
                    BaseApplication.dismissMe()
                    when (it) {
                        is NetworkResult.Success -> handleSuccessResponse(it.data.toString())
                        is NetworkResult.Error -> showAlert(it.message, false)
                        else -> showAlert(it.message, false)
                    }
                }
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SearchApiResponse::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                showData(apiModel.data)
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }



    private fun showData(data: Data?) {
        try {
            searchRecipeViewModel.setData(data)
            if (data?.ingredient!=null && data.ingredient.size>0){
                ingredient=data.ingredient
                searchRecipeAdapter = SearchRecipeAdapter(data.ingredient, requireActivity())
                binding.rcySearchRecipe.adapter = searchRecipeAdapter
                binding.llSearchRecipientIng.visibility=View.VISIBLE
            }else{
                binding.llSearchRecipientIng.visibility=View.GONE
            }

            if (data?.mealType!=null && data.mealType.size>0){
                mealType=data.mealType
                searchMealAdapter = SearchMealAdapter(data.mealType, requireActivity(),this)
                binding.rcySearchMeal.adapter = searchMealAdapter
                binding.llSearchByMeal.visibility=View.VISIBLE
            }else{
                binding.llSearchByMeal.visibility=View.GONE
            }

            if (data?.category!=null && data.category.size>0){
                category=data.category
                searchMealCatAdapter = SearchMealCatAdapter(data.category, requireActivity(),this)
                binding.rcyPopularCat.adapter = searchMealCatAdapter
                binding.llPopularCat.visibility=View.VISIBLE
            }else{
                binding.llPopularCat.visibility=View.GONE
            }

            if (data?.preference_status!=null){
                if (data.preference_status == 0){
                    binding.imgPreferences.setImageResource(R.drawable.toggle_off_icon)
                }else{
                    binding.imgPreferences.setImageResource(R.drawable.toggle_on_icon)
                }
            }else{
                binding.imgPreferences.setImageResource(R.drawable.toggle_off_icon)
            }

        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }

    private fun initialize() {

        if (sessionManagement.getUserName() != null) {
            val name = BaseApplication.getColoredSpanned(
                "Hello",
                "#06C169"
            ) + BaseApplication.getColoredSpanned(", " + sessionManagement.getUserName(), "#000000")
            binding.tvUserName.text = Html.fromHtml(name)
        }

        if (sessionManagement.getImage()!=null){
            Glide.with(requireContext())
                .load(BaseUrl.imageBaseUrl+sessionManagement.getImage())
                .placeholder(R.drawable.mask_group_icon)
                .error(R.drawable.mask_group_icon)
                .into(binding.imageProfile)
        }

        binding.etIngRecipeSearchBar.isEnabled=false
        binding.scrollview.isNestedScrollingEnabled = false
        binding.rcySearchRecipe.isNestedScrollingEnabled = false
        binding.rcySearchMeal.isNestedScrollingEnabled = false
        binding.rcyPopularCat.isNestedScrollingEnabled = false


        binding.layRoot.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        binding.relAddRecipeWeb1.setOnClickListener {
            addRecipeFromWeb()
        }
        binding.relCreateNewRecipe.setOnClickListener {
            binding.cardViewAddRecipe.visibility=View.GONE
            val bundle = Bundle().apply {
                putString("name","")
            }
            findNavController().navigate(R.id.createRecipeFragment,bundle)
        }
        binding.relRecipeImage.setOnClickListener {
            binding.cardViewAddRecipe.visibility=View.GONE
            findNavController().navigate(R.id.createRecipeImageFragment)
        }

    }

    private fun addRecipeFromWeb() {
        val dialogWeb = Dialog(requireContext())
        dialogWeb.setContentView(R.layout.alert_dialog_add_recipe_form_web)
        dialogWeb.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogWeb.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val etPasteURl = dialogWeb.findViewById<EditText>(R.id.etPasteURl)
        val rlSearchRecipe = dialogWeb.findViewById<RelativeLayout>(R.id.rlSearchRecipe)
        val imageCrossWeb = dialogWeb.findViewById<ImageView>(R.id.imageCrossWeb)

        dialogWeb.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        imageCrossWeb.setOnClickListener{
            dialogWeb.dismiss()
        }

        rlSearchRecipe.setOnClickListener {
            if (etPasteURl.text.toString().isEmpty()) {
                commonWorkUtils.alertDialog(requireContext(), ErrorMessage.pasteUrl, false)
            }/* else if (isValidUrl(etPasteURl.text.toString().trim())){
                commonWorkUtils.alertDialog(this, ErrorMessage.validUrl, false)
            }*/ else {
                binding.cardViewAddRecipe.visibility = View.VISIBLE
                val bundle = Bundle().apply {
                    putString("url",etPasteURl.text.toString().trim())
                }
                findNavController().navigate(R.id.webViewByUrlFragment,bundle)
                dialogWeb.dismiss()
            }
        }

        dialogWeb.show()


    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}