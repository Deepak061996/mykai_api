package com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterDislikeIngredientItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentIngredientDislikesBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.viewmodel.DislikeIngredientsViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IngredientDislikesFragment : Fragment(), OnItemClickedListener {

    private lateinit var binding: FragmentIngredientDislikesBinding
    private var dislikeIngredientModelData = mutableListOf<DislikedIngredientsModelData>()
    private var dislikeIngredientsAdapter: AdapterDislikeIngredientItem? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue: Int = 0
    private var status: String? = ""
    private var itemCount:String = "5"  // Default count
    private var dislikeSelectedId = mutableListOf<String>()
    private lateinit var dislikeIngredientsViewModel: DislikeIngredientsViewModel
    private lateinit var textListener: TextWatcher
    private var textChangedJob: Job? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentIngredientDislikesBinding.inflate(inflater, container, false)

        dislikeIngredientsViewModel = ViewModelProvider(this)[DislikeIngredientsViewModel::class.java]

        sessionManagement = SessionManagement(requireContext())

        dislikeIngredientsAdapter = AdapterDislikeIngredientItem(dislikeIngredientModelData, requireActivity(), this)
        binding.rcyIngDislikes.adapter = dislikeIngredientsAdapter

        val cookingFor = sessionManagement.getCookingFor()
        val progressValue: Int
        val maxProgress: Int
        val restrictionText: String

        if (cookingFor.equals("Myself")) {
            restrictionText = "Pick or search the ingredients you dislike"
            maxProgress = 10
            progressValue=4
        } else if (cookingFor.equals("MyPartner")) {
            restrictionText = "Select ingredients that you and your partner dislike"
            maxProgress = 11
            progressValue=4
        } else {
            restrictionText = "Ingredients that you dislike"
            maxProgress = 11
            progressValue=4
        }

        binding.tvIngDislikes.text = restrictionText
        binding.progressBar4.max = maxProgress
        totalProgressValue = maxProgress
        updateProgress(progressValue)

        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile",true)
        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateIngDislike.visibility = if (isProfileScreen) View.VISIBLE else View.GONE

        if (BaseApplication.isOnline(requireContext())) {
            if (isProfileScreen) {
                ingredientDislikeSelectApi()
            } else {
                dislikeIngredientsViewModel.getDislikeIngData()?.let {
                    showDataInUi(it)
                } ?: ingredientDislikeApi()
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

        backButton()

        initialize()

        return binding.root
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun ingredientDislikeSelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dislikeIngredientsViewModel.userPreferencesDislikeApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
//                                showDataInUi(bodyModel.data.ingredientdislike)
                                showDataFirstUi(bodyModel.data.ingredientdislike,"count")
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                } else {
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("IngredientDislike@", "message:--" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            },"",itemCount)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar4.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        binding.imbBackIngDislikes.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener {
            if (status == "2") {
                dislikeIngredientsViewModel.setDislikeIngData(dislikeIngredientModelData,binding.etIngDislikesSearchBar.text.toString())
                sessionManagement.setDislikeIngredientList(dislikeSelectedId)
                findNavController().navigate(R.id.allergensIngredientsFragment)
            }
        }

//        binding.etIngDislikesSearchBar.addTextChangedListener(object :
//            TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(editable: Editable) {
//                searchable(editable.toString())
//            }
//        })


        textListener = object : TextWatcher {
            private var searchFor = ""

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()

                textChangedJob?.cancel() // Cancel any pending jobs

                if (searchText.isNotEmpty()) {
                    if (searchText != searchFor) {
                        searchFor = searchText
                        textChangedJob = lifecycleScope.launch {
                            delay(1000)  // Debounce time
                            if (searchText == searchFor) {
                                if (BaseApplication.isOnline(requireActivity())) {
                                    searchable(searchText)
                                } else {
                                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                                }
                            }
                        }
                    }
                } else {
                    Log.d("not data", "Text field is empty")
                    if (sessionManagement.getCookingScreen().equals("Profile",true)){
                        if (BaseApplication.isOnline(requireContext())) {
                            ingredientDislikeSelectApi()
                        } else {
                            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                        }
                    }else{
                        ///checking the device of mobile data in online and offline(show network error message)
                        if (BaseApplication.isOnline(requireContext())) {
                            ingredientDislikeApi()
                        } else {
                            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                        }
                    }
                }
            }
        }



        /*  textListener = object : TextWatcher {
              private var searchFor = "" // Or view.editText.text.toString()

              override fun afterTextChanged(s: Editable?) {}

              override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

              override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                  val searchText = s.toString().trim()
                  if (searchText.length!=0){
                      if (searchText.trim() != searchFor) {
                          searchFor = searchText
                          textChangedJob?.cancel()
                          // Launch a new coroutine in the lifecycle scope
                          textChangedJob = lifecycleScope.launch {
                              delay(1000)  // Debounce time
                              if (searchText == searchFor) {
                                  if (BaseApplication.isOnline(requireActivity())) {
                                      searchable(searchText)
                                  } else {
                                      BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                                  }
                              }
                          }
                      }
                  }else{
                      Log.d("not data","vyvdsvd")
                      if (BaseApplication.isOnline(requireContext())) {
                          ingredientDislikeSelectApi()
                      } else {
                          BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                      }
                  }

              }
          }*/

        binding.rlUpdateIngDislike.setOnClickListener {
            if (status=="2"){
                if (BaseApplication.isOnline(requireContext())) {
                    updateIngDislikeApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        binding.relMoreButton.setOnClickListener {
            itemCount = (itemCount.toInt() + 10).toString()  // Convert to Int, add 10, convert back to String
            if (sessionManagement.getCookingScreen().equals("Profile")) {
                if (BaseApplication.isOnline(requireContext())) {
                    ingredientDislikeSelectApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            } else {
                ///checking the device of mobile data in online and offline(show network error message)
                if (BaseApplication.isOnline(requireContext())) {
                    ingredientDislikeApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun updateIngDislikeApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dislikeIngredientsViewModel.updateDislikedIngredientsApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val updateModel = gson.fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
                            if (updateModel.code == 200 && updateModel.success) {
                                findNavController().navigateUp()
                            } else {
                                if (updateModel.code == ErrorMessage.code) {
                                    showAlertFunction(updateModel.message, true)
                                } else {
                                    showAlertFunction(updateModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("IngredientDislike@@@@", "message:--" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, dislikeSelectedId)
        }
    }

    private fun ingredientDislikeApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dislikeIngredientsViewModel.getDislikeIngredients({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val dietaryModel = gson.fromJson(it.data, DislikedIngredientsModel::class.java)
                            if (dietaryModel.code == 200 && dietaryModel.success) {
                                showDataFirstUi(dietaryModel.data,"count")
                            } else {
                                if (dietaryModel.code == ErrorMessage.code) {
                                    showAlertFunction(dietaryModel.message, true)
                                } else {
                                    showAlertFunction(dietaryModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("IngredientDislike@@@@", "message:--" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            },itemCount)
        }
    }

    private fun showDataInUi(dislikeIngModelData: MutableList<DislikedIngredientsModelData>) {
        try {
            if (dislikeIngModelData != null && dislikeIngModelData.isNotEmpty()) {
                if (dislikeIngredientsViewModel.getDislikeIngData() == null) {
                    dislikeIngModelData.add(0, DislikedIngredientsModelData(id = -1, selected = false, "None")) // ID set to -1 as an indicator
                }
                var selected = false
                dislikeIngModelData.forEach { if (it.selected) selected = true }
                if (!selected) {
                    dislikeIngModelData[0] = DislikedIngredientsModelData(id = -1, selected = true, "None")
                }
                dislikeIngredientModelData = dislikeIngModelData.toMutableList()
                dislikeIngredientsAdapter?.filterList(dislikeIngredientModelData)
                /*dislikeIngredientsAdapter = AdapterDislikeIngredientItem(dislikeIngModelData, requireActivity(), this)
                binding.rcyIngDislikes.adapter = dislikeIngredientsAdapter*/
                if (dislikeIngredientsViewModel.getEditStatus().equals("")){
                    binding.relMoreButton.visibility=View.VISIBLE
                }else{
                    binding.relMoreButton.visibility=View.GONE
                }

            }
        } catch (e: Exception) {
            Log.d("IngredientDislike", "message:--" + e.message)
        }
    }

    private fun showDataFirstUi(dislikeIngModelData: MutableList<DislikedIngredientsModelData>?,type:String) {
        try {
            if (dislikeIngModelData != null && dislikeIngModelData.size>0) {
                dislikeIngredientModelData.clear()
                dislikeIngModelData.add(0, DislikedIngredientsModelData(id = -1, selected = false, "None")) // ID set to -1 as an indicator
                dislikeIngredientModelData.addAll(dislikeIngModelData.toMutableList())
                dislikeIngredientsAdapter?.filterList(dislikeIngredientModelData)
                if (type.equals("search",true)){
                    binding.relMoreButton.visibility=View.GONE
                }else{
                    binding.relMoreButton.visibility=View.VISIBLE
                }
                binding.rcyIngDislikes.visibility=View.VISIBLE
            }else{
                binding.rcyIngDislikes.visibility=View.GONE
            }


        } catch (e: Exception) {
            Log.d("IngredientDislike", "message:--" + e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun searchable(editText: String) {
        binding.layProgess.visibility=View.VISIBLE
        lifecycleScope.launch {
            dislikeIngredientsViewModel.getDislikeSearchIngredients({
                binding.layProgess.visibility=View.GONE
                when (it) {
                    is NetworkResult.Success -> {
                        val gson = Gson()
                        try {
                            if (sessionManagement.getCookingScreen().toString().equals("Profile", ignoreCase = true)){
                                val dietaryModel = gson.fromJson(it.data, GetUserPreference::class.java)
                                if (dietaryModel.code == 200 && dietaryModel.success) {
                                    showDataFirstUi(dietaryModel.data.ingredientdislike,"search")
                                } else {
                                    if (dietaryModel.code == ErrorMessage.code) {
                                        showAlertFunction(dietaryModel.message, true)
                                    } else {
                                        showAlertFunction(dietaryModel.message, false)
                                    }
                                }
                            }else{
                                val dietaryModel = gson.fromJson(it.data, DislikedIngredientsModel::class.java)
                                if (dietaryModel.code == 200 && dietaryModel.success) {
                                    showDataFirstUi(dietaryModel.data,"search")
                                } else {
                                    if (dietaryModel.code == ErrorMessage.code) {
                                        showAlertFunction(dietaryModel.message, true)
                                    } else {
                                        showAlertFunction(dietaryModel.message, false)
                                    }
                                }
                            }
                        }catch (e:Exception){
                            Log.d("IngredientDislike@@@@", "message:--" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            },editText,sessionManagement.getCookingScreen().toString())

        }

    /*    if (editText!=""){
            binding.relMoreButton.visibility=View.GONE
            val filteredList: MutableList<DislikedIngredientsModelData> =
                java.util.ArrayList<DislikedIngredientsModelData>()
            for (item in dislikeIngredientModelData) {
                if (item.name.toLowerCase().contains(editText.lowercase(Locale.getDefault()))) {
                    filteredList.add(item)
                }
            }
            if (filteredList.size > 0) {
                dislikeIngredientsAdapter!!.filterList(filteredList)
                binding.rcyIngDislikes.visibility = View.VISIBLE
            } else {
                binding.rcyIngDislikes.visibility = View.GONE
            }
        }else{
            binding.relMoreButton.visibility=View.VISIBLE
        }*/
    }

    private fun stillSkipDialog() {
        val dialogStillSkip: Dialog = context?.let { Dialog(it) }!!
        dialogStillSkip.setContentView(R.layout.alert_dialog_still_skip)
        dialogStillSkip.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogCancelBtn = dialogStillSkip.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogSkipBtn = dialogStillSkip.findViewById<TextView>(R.id.tvDialogSkipBtn)
        dialogStillSkip.show()
        dialogStillSkip.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogStillSkip.dismiss()
        }

        tvDialogSkipBtn.setOnClickListener {
            sessionManagement.setDislikeIngredientList(null)

            dialogStillSkip.dismiss()
            findNavController().navigate(R.id.allergensIngredientsFragment)
        }
    }

    override fun itemClicked(
        position: Int?,
        list: MutableList<String>?,
        status1: String?,
        type: String?
    ) {
        if (status1.equals("-1")) {
            if (position == 0) {
                dislikeSelectedId = mutableListOf()
            } else {
                dislikeSelectedId = list!!
            }
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateIngDislike.isClickable = true
            binding.rlUpdateIngDislike.setBackgroundResource(R.drawable.green_fill_corner_bg)
            return
        }

        if (type.equals("true")) {
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateIngDislike.isClickable = true
            binding.rlUpdateIngDislike.setBackgroundResource(R.drawable.green_fill_corner_bg)
            dislikeSelectedId = list!!
        } else {
            status = ""
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateIngDislike.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }


    override fun onResume() {
        super.onResume()
        binding.etIngDislikesSearchBar.addTextChangedListener(textListener)
    }

    override fun onPause() {
        binding.etIngDislikesSearchBar.removeTextChangedListener(textListener)
        super.onPause()
    }

}