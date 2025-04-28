package com.mykaimeal.planner.fragment.commonfragmentscreen.eatingOut

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
import com.mykaimeal.planner.basedata.BaseApplication.alertError
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentEatingOutBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.eatingOut.viewmodel.EatingOutViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class EatingOutFragment : Fragment(),View.OnClickListener,OnItemClickListener {

    private lateinit var binding: FragmentEatingOutBinding
    private var status:String=""
    private var eatingOutSelect: String? = ""
    private var bodyGoalAdapter: BodyGoalAdapter? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue:Int=0
    private lateinit var eatingOutViewModel: EatingOutViewModel
    private var eatingOutModelsData: List<BodyGoalModelData>?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        binding = FragmentEatingOutBinding.inflate(inflater, container, false)

        eatingOutViewModel = ViewModelProvider(this)[EatingOutViewModel::class.java]

        sessionManagement = SessionManagement(requireContext())

        val progressMax = if (sessionManagement.getCookingFor().equals("Myself",true)) 10 else 11
        binding.progressBar10.max = progressMax
        totalProgressValue = progressMax
        updateProgress(progressMax - 1)


        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile",true)
        val isOnline = isOnline(requireContext())
        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateEatingOut.visibility = if (isProfileScreen) View.VISIBLE else View.GONE

        if (isOnline) {
            if (isProfileScreen) {
                eatingOutSelectApi()
            } else {
                eatingOutViewModel.getEatingOutData()?.let {
                    showDataInUi(it)
                }?:eatingOutApi()
            }
        } else {
            alertError(requireContext(), ErrorMessage.networkError, false)
        }

        backButton()

        initialize()

        return binding.root
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun eatingOutSelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            eatingOutViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data.eatingout)
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                }else{
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("EatingOut@@@","message"+e.message)
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

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar10.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {
        binding.imbBackEatingOut.setOnClickListener(this)
        binding.tvSkipBtn.setOnClickListener(this)
        binding.tvNextBtn.setOnClickListener(this)
        binding.rlUpdateEatingOut.setOnClickListener(this)
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
            sessionManagement.setEatingOut(eatingOutSelect.toString())
            dialogStillSkip.dismiss()
            findNavController().navigate(R.id.reasonsForTakeAwayFragment)
        }
    }

    private fun eatingOutApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            eatingOutViewModel.getEatingOut {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, BodyGoalModel::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data)
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                }else{
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("EatingOut@@@","message"+e.message)

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

    private fun showDataInUi(bodyModelData: List<BodyGoalModelData>) {
        try {
            if (bodyModelData!=null && bodyModelData.isNotEmpty()){
                eatingOutModelsData=bodyModelData
                bodyGoalAdapter = BodyGoalAdapter(bodyModelData, requireActivity(), this)
                binding.rcyEatingOut.adapter = bodyGoalAdapter
            }
        }catch (e:Exception){
            Log.d("EatingOut","message"+e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.imbBackEatingOut -> {
                findNavController().navigateUp()
            }

            R.id.tvSkipBtn->{
                stillSkipDialog()
            }

            R.id.tvNextBtn->{
                if (status=="2"){
                    eatingOutViewModel.setEatingOutData(eatingOutModelsData!!.toMutableList())
                    sessionManagement.setEatingOut(eatingOutSelect.toString())
                    findNavController().navigate(R.id.reasonsForTakeAwayFragment)
                }
            }

            R.id.rlUpdateEatingOut->{
                if (status=="2"){
                    if (BaseApplication.isOnline(requireActivity())) {
                        updateEatingOutApi()
                    } else {
                        BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                    }
                }
            }

        }
    }

    private fun updateEatingOutApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            eatingOutViewModel.updateEatingOutApi({
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
                                }else{
                                    showAlertFunction(updateModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("EatingOut@@@","message"+e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, eatingOutSelect)
        }
    }

    private fun status(){
        if (status != "2") {
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        } else {
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)

        }
    }

    override fun itemClick(selectItem: Int?, status1: String?, type: String?) {

        if (status1.equals("-1")) {
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateEatingOut.isClickable = true
            binding.rlUpdateEatingOut.setBackgroundResource(R.drawable.green_fill_corner_bg)
            eatingOutSelect = selectItem.toString()
            return
        }

        if (type.equals("true")) {
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateEatingOut.isClickable = true
            binding.rlUpdateEatingOut.setBackgroundResource(R.drawable.green_fill_corner_bg)
            eatingOutSelect = selectItem.toString()
        } else {
            status = ""
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateEatingOut.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }
}