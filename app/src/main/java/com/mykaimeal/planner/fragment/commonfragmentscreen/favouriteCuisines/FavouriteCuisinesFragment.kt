package com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterFavouriteCuisinesItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentFavouriteCuisinesBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines.model.FavouriteCuisinesModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines.model.FavouriteCuisinesModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.favouriteCuisines.viewmodel.FavouriteCuisineViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavouriteCuisinesFragment : Fragment(), OnItemClickedListener {

    private lateinit var binding: FragmentFavouriteCuisinesBinding
    private var adapterFavouriteCuisinesItem: AdapterFavouriteCuisinesItem? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue: Int = 0
    private var status: String? = ""
    private var favouriteSelectId = mutableListOf<String>()
    private lateinit var favouriteCuisineViewModel: FavouriteCuisineViewModel
    private var favouriteCuiModelData: MutableList<FavouriteCuisinesModelData> = mutableListOf()
    private var isExpanded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        binding = FragmentFavouriteCuisinesBinding.inflate(inflater, container, false)
        favouriteCuisineViewModel = ViewModelProvider(requireActivity())[FavouriteCuisineViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())

        val cookingFor = sessionManagement.getCookingFor()
        val progressValue: Int
        val maxProgress: Int
        val restrictionText: String

        if (cookingFor.equals("Myself")) {
            restrictionText = "What cuisines do you enjoy most?"
            maxProgress = 10
            progressValue=3
        } else if (cookingFor.equals("MyPartner")) {
            restrictionText = "What cuisines do you and your partner enjoy most?"
            maxProgress = 11
            progressValue=6
        } else {
            restrictionText = "What cuisines do you and your family enjoy most?"
            maxProgress = 11
            progressValue=6
        }

        binding.tvCuisinesEnjoy.text = restrictionText
        binding.progressBar3.max = maxProgress
        totalProgressValue = maxProgress
        updateProgress(progressValue)

        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile",true)
        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateFavCuisine.visibility = if (isProfileScreen) View.VISIBLE else View.GONE

        if (BaseApplication.isOnline(requireContext())) {
            if (isProfileScreen) {
                favouriteCuisineSelectApi()
            } else {
                favouriteCuisineViewModel.getFavouriteCuiData()?.let {
                    showDataInUi(it)
                } ?: favouriteCuisineApi()
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

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar3.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        binding.imbBackFavouriteCuisines.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener {
            if (status == "2") {
                favouriteSelectId.clear()
                favouriteCuiModelData.forEach {
                    if (it.selected){
                        favouriteSelectId.add(it.id.toString())
                    }
                }
                favouriteCuisineViewModel.setFavouriteCuiData(favouriteCuiModelData!!)
                sessionManagement.setFavouriteCuisineList(favouriteSelectId)
                if (sessionManagement.getCookingFor().equals("Myself")) {
                    findNavController().navigate(R.id.ingredientDislikesFragment)
                } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                    findNavController().navigate(R.id.mealRoutineFragment)
                } else {
                    findNavController().navigate(R.id.mealRoutineFragment)
                }
            }
        }

        binding.rlUpdateFavCuisine.setOnClickListener {
            if (status=="2"){
                if (BaseApplication.isOnline(requireContext())) {
                    favouriteSelectId.clear()
                    favouriteCuiModelData.forEach {
                        if (it.selected){
                            favouriteSelectId.add(it.id.toString())
                        }
                    }
                    updateFavCuisineApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        binding.relMoreButton.setOnClickListener {
            isExpanded = true
            adapterFavouriteCuisinesItem!!.setExpanded(true)
            binding.relMoreButton.visibility = View.GONE // Hide button after expanding
        }


    }

    private fun updateFavCuisineApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            favouriteCuisineViewModel.updateFavouriteApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val updateModel =
                                gson.fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
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
                            Log.d("FavouriteCuisines@@@@", "message" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, favouriteSelectId)
        }
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
            dialogStillSkip.dismiss()
            sessionManagement.setFavouriteCuisineList(null)
            if (sessionManagement.getCookingFor().equals("Myself")) {
                findNavController().navigate(R.id.ingredientDislikesFragment)
            } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                findNavController().navigate(R.id.mealRoutineFragment)
            } else {
                findNavController().navigate(R.id.mealRoutineFragment)
            }
        }
    }

    private fun favouriteCuisineSelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            favouriteCuisineViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                favouriteCuiModelData.clear()
                                bodyModel.data.favouritcuisine?.let {localData->
                                    favouriteCuiModelData.addAll(localData)
                                }
                                showDataInUi(favouriteCuiModelData)
                            } else {
                                handleError(bodyModel.code,bodyModel.message)
                            }
                        } catch (e: Exception) {
                            Log.d("FavouriteCuisines@@@", "message" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        } else {
            showAlertFunction(message, false)
        }
    }


    private fun favouriteCuisineApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            favouriteCuisineViewModel.getFavouriteCuisines {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val dietaryModel = gson.fromJson(it.data, FavouriteCuisinesModel::class.java)
                            if (dietaryModel.code == 200 && dietaryModel.success) {
                                showDataInFirstUi(dietaryModel.data)
                            } else {
                                handleError(dietaryModel.code,dietaryModel.message)
                            }
                        } catch (e: Exception) {
                            Log.d("FavouriteCuisines", "message" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }
        }
    }

    private fun showDataInUi(favouriteModelData: MutableList<FavouriteCuisinesModelData>) {
        try {
            if (favouriteModelData.size>0) {
                if (favouriteModelData.size > 5) {
                    binding.relMoreButton.visibility = View.VISIBLE
                }
                hideShow()
                adapterFavouriteCuisinesItem = AdapterFavouriteCuisinesItem(favouriteModelData, requireActivity(), this)
                binding.rcyFavCuisines.adapter = adapterFavouriteCuisinesItem
            }
        } catch (e: Exception) {
            Log.d("FavouriteCuisines", "message" + e.message)
        }

    }

    private fun showDataInFirstUi(favouriteModelData: MutableList<FavouriteCuisinesModelData>) {
        try {
            favouriteCuiModelData.clear()
            favouriteModelData.let {
                favouriteCuiModelData.addAll(it)
            }
            if (favouriteCuiModelData.size>0) {
                if (favouriteCuiModelData.size > 5) {
                    binding.relMoreButton.visibility = View.VISIBLE
                }
                hideShow()
                adapterFavouriteCuisinesItem = AdapterFavouriteCuisinesItem(favouriteCuiModelData, requireActivity(), this)
                binding.rcyFavCuisines.adapter = adapterFavouriteCuisinesItem
            }
        } catch (e: Exception) {
            Log.d("FavouriteCuisines", "message" + e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    override fun itemClicked(position: Int?, list: MutableList<String>?, status1: String?, type: String?) {
        hideShow()
    }


    private fun hideShow() {
        val count = favouriteCuiModelData.count { it.selected }
        if (count == 0) {
            status = ""
            binding.tvNextBtn.isClickable = false
            binding.rlUpdateFavCuisine.isClickable = false
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateFavCuisine.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        } else {
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.rlUpdateFavCuisine.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateFavCuisine.setBackgroundResource(R.drawable.green_fill_corner_bg)
        }
    }

}