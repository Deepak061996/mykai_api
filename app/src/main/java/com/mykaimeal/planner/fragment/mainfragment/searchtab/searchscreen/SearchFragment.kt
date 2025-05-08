package com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.SearchMealAdapter
import com.mykaimeal.planner.adapter.SearchMealCatAdapter
import com.mykaimeal.planner.adapter.SearchRecipeAdapter
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentSearchBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Category
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.MealType
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.SearchApiResponse
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.viewmodel.SearchRecipeViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class SearchFragment : Fragment(),View.OnClickListener, OnItemClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var searchRecipeAdapter: SearchRecipeAdapter? = null
    private var searchMealAdapter: SearchMealAdapter? = null
    private var searchMealCatAdapter: SearchMealCatAdapter? = null
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var sessionManagement: SessionManagement
    private lateinit var searchRecipeViewModel:SearchRecipeViewModel
    private var clickedUrl: String = ""
    private var ingredient: MutableList<Ingredient>?= mutableListOf()
    private var mealType: MutableList<MealType>?= mutableListOf()
    private var category: MutableList<Category>?= mutableListOf()
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        (activity as MainActivity?)?.changeBottom("search")
        (activity as MainActivity?)?.alertStatus=false

        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.VISIBLE
            it.llBottomNavigation.visibility = View.VISIBLE
        }

        binding.cardViewAddRecipe.visibility=View.GONE

        clickedUrl = arguments?.getString("ClickedUrl", "").toString()

        searchRecipeViewModel = ViewModelProvider(requireActivity())[SearchRecipeViewModel::class.java]

        cookbookList.clear()

        val data= com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("","",0,"","Favourites",0,"",0)
        cookbookList.add(0,data)

        commonWorkUtils = CommonWorkUtils(requireActivity())
        sessionManagement = SessionManagement(requireContext())


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.homeFragment)
                }
            })

        initialize()

        if (searchRecipeViewModel.data!=null){
            showData(searchRecipeViewModel.data)
        }else{
            // This Api call when the screen in loaded
            lunchApi()
        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Set up touch listener for non-EditText views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard(view)
                false
            }
        }

        // If a layout container, iterate over children
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }


    }

    private fun hideKeyboard(view: View) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun lunchApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            searchRecipeViewModel.recipeforSearchApi {
                BaseApplication.dismissMe()
                handleApiSearchResponse(it)
            }
        }
    }

    private fun handleApiSearchResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessSearchPreferences(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessSearchPreferences(data: String) {
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

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
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

        binding.relViewAll.setOnClickListener(this)
        binding.imgHeartRed.setOnClickListener(this)
        binding.imageProfile.setOnClickListener(this)
        binding.imgBasketIcon.setOnClickListener(this)
        binding.imgFilterIcon.setOnClickListener(this)
        binding.imgPreferences.setOnClickListener(this)

        binding.etIngRecipeSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val query = editable.toString().trim()
                if (query.isNotEmpty()) {
                    if (binding.rcySearchRecipe.visibility == View.VISIBLE) {
                        filterIngredients(query)
                    }
                    if (binding.rcySearchMeal.visibility == View.VISIBLE) {
                        filterMealType(query)
                    }
                    if (binding.rcyPopularCat.visibility == View.VISIBLE) {
                        filterPopular(query)
                    }
                } else {
                    // If the query is empty, reset the lists to show all original data
                    resetLists()
                }
            }
        })
    }

    private fun resetLists() {
        if (binding.rcySearchRecipe.visibility == View.VISIBLE) {
            ingredient?.let { searchRecipeAdapter!!.submitList(it) } // Reset recipe list
        }

        if (binding.rcySearchMeal.visibility == View.VISIBLE) {
            searchMealAdapter?.submitList(mealType) // Reset meal type list
        }

        if (binding.rcyPopularCat.visibility == View.VISIBLE) {
            searchMealCatAdapter?.submitList(category) // Reset popular category list
        }
    }

    private fun filterPopular(editText: String) {
        val filteredList: MutableList<Category> =
            java.util.ArrayList<Category>()
        for (item in category!!) {
            if (item.name!!.lowercase().contains(editText.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        if (filteredList.size > 0) {
            searchMealCatAdapter!!.filterList(filteredList)
            binding.llPopularCat.visibility = View.VISIBLE
        } else {
            binding.llPopularCat.visibility = View.GONE
        }
    }

    private fun filterMealType(editText: String) {
        val filteredList: MutableList<MealType> =
            java.util.ArrayList<MealType>()
        for (item in mealType!!) {
            if (item.name!!.lowercase().contains(editText.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        if (filteredList.size > 0) {
            searchMealAdapter!!.filterList(filteredList)
            binding.llSearchByMeal.visibility = View.VISIBLE
        } else {
            binding.llSearchByMeal.visibility = View.GONE
        }
    }

    private fun filterIngredients(editText: String) {
        val filteredList: MutableList<Ingredient> =
            java.util.ArrayList<Ingredient>()
        for (item in ingredient!!) {
            if (item.name!!.lowercase().contains(editText.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        if (filteredList.size > 0) {
            searchRecipeAdapter!!.filterList(filteredList)
            binding.llSearchRecipientIng.visibility = View.VISIBLE
        } else {
            binding.llSearchRecipientIng.visibility = View.GONE
        }
    }

    override fun onClick(item: View?) {
        when(item!!.id){
            R.id.relViewAll->{
                findNavController().navigate(R.id.allIngredientsFragment)
            }

            R.id.imageProfile->{
                findNavController().navigate(R.id.settingProfileFragment)
            }

            R.id.imgHeartRed->{
                findNavController().navigate(R.id.cookBookFragment)
            }

            R.id.imgBasketIcon->{
                (activity as MainActivity?)?.upBasket()
                findNavController().navigate(R.id.basketScreenFragment)
            }

            R.id.imgFilterIcon->{
                findNavController().navigate(R.id.filterSearchFragment)
            }
            R.id.imgPreferences->{
                if (BaseApplication.isOnline(requireActivity())){
                    upDatePreferences()
                }else{
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun upDatePreferences() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            searchRecipeViewModel.recipePreferencesApi{
                BaseApplication.dismissMe()
                handleApiResponse(it)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessPreferences(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessPreferences(data: String) {
        try {
            val updateModel = Gson().fromJson(data, UpdatePreferenceSuccessfully::class.java)
            if (updateModel.code == 200 && updateModel.success) {
                (activity as MainActivity?)?.upDatePlan()
                lunchApi()
            } else {
                handleError(updateModel.code,updateModel.message)
            }
        }catch (e:Exception){
            Log.d("bodyGoal@@@","message"+e.message)
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
            val bundle = Bundle().apply {
                putString("recipeName",status)
                putString("screenType","Search")
            }
            findNavController().navigate(R.id.searchedRecipeBreakfastFragment,bundle)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}