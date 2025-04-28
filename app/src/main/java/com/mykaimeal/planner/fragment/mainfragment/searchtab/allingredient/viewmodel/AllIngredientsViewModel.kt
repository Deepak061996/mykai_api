package com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.viewmodel

import androidx.lifecycle.ViewModel
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.CategoryModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.allingredient.model.IngredientList
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Data
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AllIngredientsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {



    private var _data: MutableList<CategoryModel>? = null

    private var _dataingredients: MutableList<IngredientList>? = null

    val dataCategories: MutableList<CategoryModel>? get() = _data

    val dataIngredients: MutableList<IngredientList>? get() = _dataingredients


    fun setIngredients(data: MutableList<IngredientList>?){
        _dataingredients=data
    }

    fun setCategories(data: MutableList<CategoryModel>?){
        _data=data
    }


    suspend fun getAllIngredientsUrl(successCallback: (response: NetworkResult<String>) -> Unit,category:String?,search:String?,number:String?){
        repository.getAllIngredientsUrl({ successCallback(it) },category, search, number)
    }



}