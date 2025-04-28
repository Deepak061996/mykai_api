package com.mykaimeal.planner.fragment.mainfragment.profilesetting

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentNutritionGoalBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.SettingViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.ProfileRootResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import kotlinx.coroutines.launch

class NutritionGoalFragment : Fragment() {

    private lateinit var binding: FragmentNutritionGoalBinding
    private lateinit var viewModel: SettingViewModel
    private lateinit var sessionManagement: SessionManagement
    var calories=0
    var fat=0
    var protien=0
    var carbs=0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNutritionGoalBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SettingViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())

        setupBackPressHandler()
        initializeUI()
        loadProfileData()

        return binding.root
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun loadProfileData() {
        val profileData = viewModel.getProfileData()
        if (profileData != null) {
            updateUI(profileData)
        } else if (BaseApplication.isOnline(requireActivity())) {
            fetchUserProfileData()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun fetchUserProfileData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.userProfileData { result ->
                BaseApplication.dismissMe()
                handleApiResponse(result)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> parseAndHandleSuccess(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun parseAndHandleSuccess(data: String) {
        try {
            val response = Gson().fromJson(data, ProfileRootResponse::class.java)
            if (response.code == 200 && response.success) {
                response.data?.let {
                    viewModel.setProfileData(it)
                    updateUI(it)
                }
            } else {
                showAlert(response.message, response.code == ErrorMessage.code)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showAlert(message: String?, isError: Boolean) {
        BaseApplication.alertError(requireContext(), message, isError)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(data: Data) {

        if (data.height_protein!=null && !data.height_protein.equals("null",true)){
            binding.spinnerHighProtein.text = data.height_protein
        }

        if (data.calories!=null){
            calories=data.calories?.toInt() ?: 0
            binding.seekbarcalories.progress = data.calories?.toInt() ?: 0
            binding.textCalorisTotal.text=""+data.calories?.toInt()+"/10000"
        }else{
            calories= 0
            binding.seekbarcalories.progress =  0
            binding.textCalorisTotal.text="0/10000"
        }

        if (data.fat!=null){
            fat=data.fat?.toInt() ?: 0
            binding.seekbarFats.progress = data.fat?.toInt() ?: 0
            binding.textFatTotal.text=""+data.fat?.toInt()+"/300"
        }else{
            fat= 0
            binding.seekbarFats.progress =  0
            binding.textFatTotal.text="0/300"
        }

        if (data.protien!=null){
            protien=data.protien?.toInt() ?: 0
            binding.seekbarProtein.progress = data.protien?.toInt() ?: 0
            binding.textProtienTotal.text=""+data.protien?.toInt()+"/500"
        }else{
            protien=0
            binding.seekbarProtein.progress =  0
            binding.textProtienTotal.text="0/500"
        }

        if (data.carbs!=null){
            carbs=data.carbs?.toInt() ?: 0
            binding.seekbarCarbs.progress = data.carbs?.toInt() ?: 0
            binding.textCarbsTotal.text=""+data.carbs?.toInt()+"/1200"
        }else{
            carbs=0
            binding.seekbarCarbs.progress =  0
            binding.textCarbsTotal.text="0/1200"
        }


    }

    @SuppressLint("SetTextI18n")
    private fun initializeUI() {

        if (sessionManagement.getUserName() != null) {
            binding.tvName.text = sessionManagement.getUserName()+"'s Nutrition Goals"
        }

        binding.spinnerHighProtein.setIsFocusable(true)


        binding.imageBackNutrition.setOnClickListener {
            findNavController().navigateUp()
        }

        if (sessionManagement.getUserName()!=null && sessionManagement.getUserName().equals("null")){
            binding.tvName.text=sessionManagement.getUserName()+"’s Nutrition Goals"
        }

        setSeekBarValue()
        setupSpinner()
        setupUpdateButton()
    }

    private fun setSeekBarValue() {


        binding.seekbarcalories.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update TextView with SeekBar's current value
                binding.textCalorisTotal.text = "$progress/10000"
                // Enforce the minimum value constraint
                if (progress <= calories) {
                    seekBar?.progress = calories
                }else{
                    seekBar?.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch stops
            }
        })

        binding.seekbarFats.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update TextView with SeekBar's current value
                binding.textFatTotal.text = "$progress/300"
                // Enforce the minimum value constraint
                if (progress <= fat) {
                    seekBar?.progress = fat
                }else{
                    seekBar?.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch stops
            }
        })
        binding.seekbarCarbs.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update TextView with SeekBar's current value
                binding.textCarbsTotal.text = "$progress/1200"
                // Enforce the minimum value constraint
                if (progress <= carbs) {
                    seekBar?.progress = carbs
                }else{
                    seekBar?.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch stops
            }
        })
        binding.seekbarProtein.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update TextView with SeekBar's current value
                binding.textProtienTotal.text = "$progress/500"
                // Enforce the minimum value constraint
                if (progress <= protien) {
                    seekBar?.progress = protien
                }else{
                    seekBar?.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch stops
            }
        })


    }

    private fun setupSpinner() {
        binding.spinnerHighProtein.setItems(
            listOf("Low fat", "Kito", "High Protein", "Low Carb", "Balanced")
        )

    }

    private fun setupUpdateButton() {

        binding.rlUpdateButton.setOnClickListener {
            viewModel.getProfileData()?.let { data ->
                data.apply {
                    height_protein=binding.spinnerHighProtein.text.toString()
                    fat = binding.seekbarFats.progress
                    carbs = binding.seekbarCarbs.progress
                    calories = binding.seekbarcalories.progress
                    protien = binding.seekbarProtein.progress
                }
                viewModel.setProfileData(data)
                findNavController().navigateUp()
            }
        }
    }
}
