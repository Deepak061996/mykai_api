package com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterFilterCookTimeItem
import com.mykaimeal.planner.adapter.AdapterFilterDietItem
import com.mykaimeal.planner.adapter.AdapterFilterMealItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentFilterSearchBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.CookTime
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.Diet
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.FilterSearchModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.FilterSearchModelData
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.MealType
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.viewmodel.FilterSearchViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Locale

@AndroidEntryPoint
class FilterSearchFragment : Fragment(), OnItemClickListener {

    private var _binding: FragmentFilterSearchBinding? = null
    private val binding get() = _binding!!
    private var adapterFilterMealItem: AdapterFilterMealItem? = null
    private var adapterFilterDietItem: AdapterFilterDietItem? = null
    private var adapterFilterCookBookItem: AdapterFilterCookTimeItem? = null
    private lateinit var filterSearchViewModel: FilterSearchViewModel
    private var fullListMealType: MutableList<MealType> = mutableListOf()
    private var originalFullList: MutableList<Diet> = mutableListOf()
    private var fullListCookTime: MutableList<CookTime> = mutableListOf()

    private var showMealType: MutableList<MealType> = mutableListOf()
    private var showFullList: MutableList<Diet> = mutableListOf()
    private var showListCookTime: MutableList<CookTime> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFilterSearchBinding.inflate(inflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        filterSearchViewModel = ViewModelProvider(this)[FilterSearchViewModel::class.java]


        binding.relApplyBtn.isClickable=false

        backButton()

        initialize()


        if (filterSearchViewModel.fullListMealType!=null && filterSearchViewModel.originalFullList!=null&& filterSearchViewModel.fullListCookTime!=null){
            fullListMealType = filterSearchViewModel.fullListMealType!!
            originalFullList = filterSearchViewModel.originalFullList!!
            fullListCookTime = filterSearchViewModel.fullListCookTime!!


            showFullList.clear()
            showMealType.clear()
            showListCookTime.clear()

            upDateUi()
        }else{
            // This Api call when the screen in loaded
            if (BaseApplication.isOnline(requireActivity())) {
                launchApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }



        return binding.root
    }

    private fun backButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun launchApi() {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                filterSearchViewModel.getFilterList {
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

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, FilterSearchModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                if (apiModel.data != null) {
                    showDataInUi(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun handleError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun showDataInUi(data: FilterSearchModelData) {
        try {

            fullListMealType.clear()
            originalFullList.clear()
            fullListCookTime.clear()

            showFullList.clear()
            showMealType.clear()
            showListCookTime.clear()

            data.mealType?.let {
                fullListMealType.addAll(it)
            }
            data.Diet?.let {
                originalFullList.addAll(it)
            }
            data.cook_time?.let {
                fullListCookTime.addAll(it)
            }


            filterSearchViewModel.setOriginalFullList(originalFullList)
            filterSearchViewModel.setFullListCookTime(fullListCookTime)
            filterSearchViewModel.setFullListMealType(fullListMealType)

            upDateUi()

        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun upDateUi(){
        if (fullListMealType.size > 0) {
            fullListMealType.forEachIndexed { index, diet ->
                if (index<5){
                    showMealType.add(diet)
                }
            }
            if (showMealType.size>5){
                showMealType.add(MealType(id = -1, image = "", name = "More", "",selected = true))
            }
            val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
            binding.rcyMealType.layoutManager = flexboxLayoutManager
            adapterFilterMealItem = AdapterFilterMealItem(showMealType, requireActivity(), this)
            binding.rcyMealType.adapter = adapterFilterMealItem
        }
        if (originalFullList.size > 0) {
            originalFullList.forEachIndexed { index, diet ->
                if (index<5){
                    showFullList.add(diet)
                }
            }

            if (showFullList.size>5){
                showFullList.add(Diet(name = "More", selected = true,""))
            }

            val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
            binding.rcyDiet.layoutManager = flexboxLayoutManager
            adapterFilterDietItem = AdapterFilterDietItem(showFullList, requireActivity(), this)
            binding.rcyDiet.adapter = adapterFilterDietItem
        }
        if (fullListCookTime.size > 0) {
            fullListCookTime.forEachIndexed { index, diet ->
                if (index<2){
                    showListCookTime.add(diet)
                }
            }

            if (showListCookTime.size>5){
                showListCookTime.add(CookTime(name = "More", value = "", selected = true))
            }
            val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
            binding.rcyCookTime.layoutManager = flexboxLayoutManager
            adapterFilterCookBookItem = AdapterFilterCookTimeItem(showListCookTime, requireActivity(), this)
            binding.rcyCookTime.adapter = adapterFilterCookBookItem
        }

        buttonActive()

    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun initialize() {

        binding.relBackFiltered.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.etIngRecipeSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isEmpty()) {

                    if (showMealType.size>0){
                        adapterFilterMealItem?.updateList(showMealType)
                        binding.rcyMealType.visibility=View.VISIBLE
                    }else{
                        binding.rcyMealType.visibility=View.GONE
                    }

                    if (showFullList.size>0){
                        adapterFilterDietItem?.updateList(showFullList)
                        binding.rcyDiet.visibility=View.VISIBLE
                    }else{
                        binding.rcyDiet.visibility=View.GONE
                    }

                    if (showListCookTime.size>0){
                        adapterFilterCookBookItem?.updateList(showListCookTime)
                        binding.rcyCookTime.visibility=View.VISIBLE
                    }else{
                        binding.rcyCookTime.visibility=View.GONE
                    }

                } else {
                    filter(editable.toString())
                }
            }
        })

        binding.relApplyBtn.setOnClickListener {
            if (binding.relApplyBtn.isClickable){
                val mealType = showMealType.filter { it.selected == true }.map { it.name.toString() }
                val diet = showFullList.filter { it.selected == true }.map { it.value.toString() }
                val cookTime = showListCookTime.filter { it.selected == true }.map { it.value.toString() }

                val bundle = Bundle().apply {
                    putString("recipeName","")
                    putString("mealJsonArray", JSONArray(mealType).toString())
                    putString("dietJsonArray", JSONArray(diet).toString())
                    putString("cookTimeJsonArray", JSONArray(cookTime).toString())
                    putString("screenType", "filter")
                }

                findNavController().navigate(R.id.searchedRecipeBreakfastFragment, bundle)

            }
        }


    }

    private fun filter(text: String) {
        val list1: MutableList<MealType> = mutableListOf()
        val list2: MutableList<Diet> = mutableListOf()
        val list3: MutableList<CookTime> = mutableListOf()
        try {

            if (showMealType.size > 0) {
                for (item in showMealType) {
                    val category = item.name?.lowercase(Locale.getDefault())
                    if (category != null && category.contains(text.lowercase(Locale.getDefault()))) {
                        list1.add(item)
                    }
                }
                if (list1.size > 0) {
                    adapterFilterMealItem?.updateList(list1)
                    binding.rcyMealType.visibility=View.VISIBLE
                }else{
                    binding.rcyMealType.visibility=View.GONE
                }
            }else{
                binding.rcyMealType.visibility=View.GONE
            }

            if (showFullList.size > 0) {
                for (item in showFullList) {
                    val category = item.name?.lowercase(Locale.getDefault())
                    if (category != null && category.contains(text.lowercase(Locale.getDefault()))) {
                        list2.add(item)
                    }
                }
                if (list2.size > 0) {
                    adapterFilterDietItem?.updateList(list2)
                    binding.rcyDiet.visibility=View.VISIBLE
                }else{
                    binding.rcyDiet.visibility=View.GONE
                }
            }else{
                binding.rcyDiet.visibility=View.GONE
            }
            if (showListCookTime.size > 0) {
                for (item in showListCookTime) {
                    val category = item.name?.lowercase(Locale.getDefault())
                    if (category != null && category.contains(text.lowercase(Locale.getDefault()))) {
                        list3.add(item)
                    }
                }
                if (list3.size > 0) {
                    adapterFilterCookBookItem?.updateList(list3)
                    binding.rcyCookTime.visibility=View.VISIBLE
                }else{
                    binding.rcyCookTime.visibility=View.GONE
                }
            }else{
                binding.rcyCookTime.visibility=View.GONE
            }
        }catch (e:Exception){
            binding.rcyDiet.visibility=View.GONE
            binding.rcyCookTime.visibility=View.GONE
            binding.rcyMealType.visibility=View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    override fun itemClick(position: Int?, status: String?, type: String?) {
        if (type.equals("MealType",true)) {
            showMealType.removeLast()
            for (i in 5 until fullListMealType.size step 5) {
                showMealType.add(fullListMealType[i])
            }
            adapterFilterMealItem?.updateList(showMealType)
        }
        if (type.equals("Diet",true)) {
            showFullList.removeLast()
            for (i in 5 until originalFullList.size step 5) {
                showFullList.add(originalFullList[i])
            }
            adapterFilterDietItem?.updateList(showFullList)
        }
        if (type.equals("CookTime",true)) {
            showListCookTime.removeLast()
            for (i in 2 until fullListCookTime.size step 2) {
                showListCookTime.add(fullListCookTime[i])
            }
            adapterFilterCookBookItem?.updateList(showListCookTime)
        }
        buttonActive()
    }

    private fun buttonActive(){
        val count = showMealType.count { it.selected == true } + showFullList.count { it.selected == true } + showListCookTime.count { it.selected == true }



        Log.d("count", "******$count")

        if (count==0){
            binding.tvCount.text= "Apply"
            binding.relApplyBtn.isClickable=false
            binding.relApplyBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }else{
            binding.tvCount.text= "Apply ($count)"
            binding.relApplyBtn.isClickable=true
            binding.relApplyBtn.setBackgroundResource(R.drawable.green_btn_background)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onDestroy() {
        super.onDestroy()
        filterSearchViewModel.setOriginalFullList(null)
        filterSearchViewModel.setFullListCookTime(null)
        filterSearchViewModel.setFullListMealType(null)
    }


}