package com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.BodyGoalAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentBodyGoalsBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.viewmodel.BodyGoalViewModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BodyGoalsFragment : Fragment(), OnItemClickListener {

    private var _binding: FragmentBodyGoalsBinding? = null
    private val binding get() = _binding!!
    private var bodyGoalAdapter: BodyGoalAdapter? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue: Int = 0
    private var status: String? = null
    private var bodySelect: String? = ""
    private lateinit var bodyGoalViewModel: BodyGoalViewModel
    private var bodyModelData1: MutableList<BodyGoalModelData> = mutableListOf()
    private var bodyGoalsText: String = "What are your body's goals?"
    private var progressValue: Int = 1
    private var showBodyGoals: Boolean = true
    val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentBodyGoalsBinding.inflate(inflater, container, false)
        bodyGoalViewModel = ViewModelProvider(this)[BodyGoalViewModel::class.java]

        sessionManagement = SessionManagement(requireContext())

        // Get the cookingFor value once to avoid multiple method calls
        val cookingFor = sessionManagement.getCookingFor()
        val cookingScreen = sessionManagement.getCookingScreen()

        /// checked session value cooking for
        if (cookingFor.equals("Myself")) {
            bodyGoalsText = "What are your body goals?"
            showBodyGoals=true
            binding.progressBar.max = 10
            totalProgressValue = 10
            progressValue=1
        } else if (cookingFor.equals("MyPartner")) {
            bodyGoalsText = "You and your partner's goals?"
            showBodyGoals=true
            binding.progressBar.max = 11
            totalProgressValue = 11
            progressValue=2
        } else {
            bodyGoalsText = "What are your family's body goals?"
            showBodyGoals=false
            binding.progressBar.max = 11
            totalProgressValue = 11
            progressValue=2
        }

        Log.d("type", "*****$cookingFor")

        binding.tvYourBodyGoals.text = bodyGoalsText
        updateProgress(progressValue)

        if (showBodyGoals){
            binding.textBodyGoals.visibility = View.VISIBLE
            binding.textBodyMembersGoals.visibility = View.GONE
        }else{
            binding.textBodyGoals.visibility = View.GONE
            binding.textBodyMembersGoals.visibility = View.VISIBLE
        }

        if (cookingScreen.equals("Profile")) {
            binding.llBottomBtn.visibility = View.GONE
            binding.rlUpdateBodyGoals.visibility = View.VISIBLE
            loadApi(cookingScreen)
        } else {
            binding.llBottomBtn.visibility = View.VISIBLE
            binding.rlUpdateBodyGoals.visibility = View.GONE
            if (bodyGoalViewModel.getBodyGoalData()!=null){
                showDataInUi(bodyGoalViewModel.getBodyGoalData()!!)
            }else{
                loadApi(cookingScreen)
            }
        }

        ///handle on back pressed
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (sessionManagement.getCookingScreen() != "Profile") {
                        if (sessionManagement.getCookingFor().equals("Myself")) {
                           /* val intent = Intent(requireActivity(), CookingForScreenActivity::class.java)
                            startActivity(intent)*/
                            requireActivity().finish()
                        } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                            findNavController().navigateUp()
                        } else {
                            findNavController().navigateUp()
                        }
                    } else {
                        findNavController().navigateUp()
                    }
                }
            })

        initialize()

        return binding.root
    }

    private fun loadApi(type: String?){
        if (BaseApplication.isOnline(requireActivity())) {
            if (type.equals("Profile")){
                bodyGoalSelectApi()
            }else{
                bodyGoalApi()
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun bodyGoalSelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            bodyGoalViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data.bodygoal)
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                } else {
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("bodyGoal@@","message"+e.message)
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

    private fun bodyGoalApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            bodyGoalViewModel.getBodyGoal {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val bodyModel = gson.fromJson(it.data, BodyGoalModel::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data)
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                } else {
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("bodyGoal@@@@","message"+e.message)

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

    private fun showDataInUi(bodyModelData: MutableList<BodyGoalModelData>) {
        try {
            bodyModelData1.clear()
            bodyModelData1.addAll(bodyModelData)
            if (bodyModelData1.size > 0) {
                bodyGoalAdapter = BodyGoalAdapter(bodyModelData1, requireActivity(), this)
                binding.rcyBodyGoals.adapter = bodyGoalAdapter
            }
        }catch (e:Exception){
            Log.d("bodyGoal","message"+e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        binding.imageBackBodyGoals.setOnClickListener {
            if (sessionManagement.getCookingScreen() != "Profile") {
                if (sessionManagement.getCookingFor().equals("Myself")) {
                    requireActivity().finish()
                } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                    findNavController().navigateUp()
                } else {
                    findNavController().navigateUp()
                }
            } else {
                findNavController().navigateUp()
            }
        }

        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener {
            if (status == "2") {
                bodyGoalViewModel.setBodyGoalData(bodyModelData1.toMutableList())
                sessionManagement.setBodyGoal(bodySelect.toString())
                findNavController().navigate(R.id.dietaryRestrictionsFragment)
            }
        }

        binding.rlUpdateBodyGoals.setOnClickListener {
            if (status=="2"){
                ///checking the device of mobile data in online and offline(show network error message)
                if (BaseApplication.isOnline(requireActivity())) {
                    updateBodyGoalApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun updateBodyGoalApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            bodyGoalViewModel.updateBodyGoalApi({
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
                            Log.d("bodyGoal@@@","message"+e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }

                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, bodySelect.toString())
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
            sessionManagement.setBodyGoal(bodySelect.toString())
            dialogStillSkip.dismiss()
            findNavController().navigate(R.id.dietaryRestrictionsFragment)
        }
    }


    override fun itemClick(position: Int?, status: String?, type: String?) {
        bodySelect = ""
        if (status.equals("-1")) {
            this.status = "2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)

            binding.rlUpdateBodyGoals.isClickable = true
            binding.rlUpdateBodyGoals.setBackgroundResource(R.drawable.green_fill_corner_bg)
            bodySelect = position.toString()
            return
        }

        if (type.equals("true")) {
            this.status = "2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateBodyGoals.isClickable = true
            binding.rlUpdateBodyGoals.setBackgroundResource(R.drawable.green_fill_corner_bg)
            bodySelect = position.toString()
        } else {
            this.status = ""
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateBodyGoals.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}