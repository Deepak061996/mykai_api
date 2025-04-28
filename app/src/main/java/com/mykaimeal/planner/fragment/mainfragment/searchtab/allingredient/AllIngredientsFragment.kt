package com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterAllIngredientsItem
import com.mykaimeal.planner.adapter.AllIngredientsCategoryItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentAllIngredientsBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.AllIngredientsModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.AllIngredientsModelData
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.CategoryModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.IngredientList
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.viewmodel.AllIngredientsViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class AllIngredientsFragment : Fragment(),View.OnClickListener,OnItemClickListener {

    private var _binding: FragmentAllIngredientsBinding?=null
    private val binding get() = _binding!!
    private var adapterAllIngItem: AdapterAllIngredientsItem? = null
    private var adapterAllIngCategoryItem: AllIngredientsCategoryItem? = null
    private lateinit var allIngredientsModelData:AllIngredientsViewModel
    private var ingredients: MutableList<IngredientList> = mutableListOf()
    private var categoryModel:MutableList<CategoryModel> = mutableListOf()
    private var lastSelected="Fruit"
    private lateinit var textListener: TextWatcher
    private var textChangedJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding= FragmentAllIngredientsBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.VISIBLE
            it.llBottomNavigation.visibility = View.VISIBLE
        }

        adapterAllIngCategoryItem = AllIngredientsCategoryItem(categoryModel, requireActivity(),this)
        binding.rcyIngredientCategory.adapter = adapterAllIngCategoryItem

        allIngredientsModelData = ViewModelProvider(requireActivity())[AllIngredientsViewModel::class.java]


        showCount(0)

        adapterAllIngItem = AdapterAllIngredientsItem(ingredients, requireActivity(),this)
        binding.rcyAllIngredients.adapter = adapterAllIngItem

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        initialize()

        return binding.root
    }

    private fun initialize() {

        binding.imageBackIcon.setOnClickListener(this)


        textListener = object : TextWatcher {
            private var searchFor = "" // Or view.editText.text.toString()

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (searchText.isNotEmpty()){
                    if (!searchText.equals(searchFor,true)) {
                        lastSelected=""
                        searchFor = searchText
                        textChangedJob?.cancel()
                        // Launch a new coroutine in the lifecycle scope
                        textChangedJob = lifecycleScope.launch {
                            delay(1000)  // Debounce time
                            if (searchText.equals(searchFor,true)) {
                                searchRecipeApi(searchText)
                            }
                        }
                    }else{
                        lastSelected="Fruit"
                    }
                }else{
                    lastSelected="Fruit"
                }
            }
        }


        if (allIngredientsModelData.dataIngredients!=null && allIngredientsModelData.dataCategories!=null){
            ingredients = allIngredientsModelData.dataIngredients!!
            categoryModel = allIngredientsModelData.dataCategories!!
            upDateUI()
        }else{
            searchRecipeApi("")
        }


        binding.relApplyBtn.setOnClickListener {
            if (binding.relApplyBtn.isClickable){
                if (BaseApplication.isOnline(requireActivity())) {
                    val mealType = ingredients
                        .filter { it.status == true }.joinToString(", ") { it.name.toString() }
                    val bundle = Bundle().apply {
                        putString("recipeName",mealType)
                        putString("screenType","Ingredients")
                    }
                    findNavController().navigate(R.id.searchedRecipeBreakfastFragment,bundle)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun searchRecipeApi(type:String){
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                allIngredientsModelData.getAllIngredientsUrl({
                    BaseApplication.dismissMe()
                    when (it) {
                        is NetworkResult.Success -> handleSuccessResponse(it.data.toString())
                        is NetworkResult.Error -> showAlert(it.message, false)
                        else -> showAlert(it.message, false)
                    }
                },lastSelected,type,"")
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
            val apiModel = Gson().fromJson(data, AllIngredientsModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null) {
                    showDataInUi(apiModel.data)
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun showDataInUi(data: AllIngredientsModelData) {
        try {

            categoryModel.clear()
            ingredients.clear()

            data.ingredients?.let {
                ingredients.addAll(it)
            }

            data.categories?.forEach {
                categoryModel.add(CategoryModel(it, it.equals(lastSelected,true)))
            }


            allIngredientsModelData.setIngredients(ingredients)
            allIngredientsModelData.setCategories(categoryModel)

            upDateUI()
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun upDateUI(){
        if (categoryModel.size>0){
            adapterAllIngCategoryItem?.filterList(categoryModel)
        }
        if (ingredients.size>0){
            val count = ingredients.count { it.status == true }
            showCount(count)
            adapterAllIngItem?.filterList(ingredients)
            binding.rcyAllIngredients.visibility = View.VISIBLE
            binding.tvNodata.visibility = View.GONE
        } else {
            binding.rcyAllIngredients.visibility = View.GONE
            binding.tvNodata.visibility = View.VISIBLE
        }
    }

    override fun onClick(item: View?) {
        when(item!!.id){
            R.id.imageBackIcon->{
                findNavController().navigateUp()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.etIngRecipes.addTextChangedListener(textListener)
    }

    override fun onPause() {
        binding.etIngRecipes.removeTextChangedListener(textListener)
        super.onPause()
    }

    @SuppressLint("SetTextI18n")
    override fun itemClick(position: Int?, status: String?, type: String?) {
        if (type.equals("filter",true)){
            lastSelected=status.toString()
            // This Api call when the screen in loaded
            searchRecipeApi("")
        }else{
            val count = ingredients.count { it.status == true }
            showCount(count)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCount(count:Int){
        if (count==0){
            binding.tvCount.text= "Search Recipes"
            binding.relApplyBtn.isClickable=false
            binding.relApplyBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }else{
            binding.tvCount.text= "Search Recipes ($count)"
            binding.relApplyBtn.isClickable=true
            binding.relApplyBtn.setBackgroundResource(R.drawable.green_btn_background)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        allIngredientsModelData.setIngredients(null)
        allIngredientsModelData.setCategories(null)
    }
}