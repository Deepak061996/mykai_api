package com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.IngredientList
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.CookTime
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.Diet
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.MealType
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FilterSearchViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getFilterList(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getFilterList{ successCallback(it) }
    }


    private var _fullListMealType: MutableList<MealType>? = null
    private var _originalFullList: MutableList<Diet>? = null
    private var _fullListCookTime: MutableList<CookTime>? = null

    val fullListMealType: MutableList<MealType>? get() = _fullListMealType

    val originalFullList: MutableList<Diet>? get() = _originalFullList

    val fullListCookTime: MutableList<CookTime>? get() = _fullListCookTime


    fun setFullListMealType(data: MutableList<MealType>?){
        _fullListMealType=data
    }


    fun setOriginalFullList(data: MutableList<Diet>?){
        _originalFullList=data
    }

    fun setFullListCookTime(data: MutableList<CookTime>?){
        _fullListCookTime=data
    }

}