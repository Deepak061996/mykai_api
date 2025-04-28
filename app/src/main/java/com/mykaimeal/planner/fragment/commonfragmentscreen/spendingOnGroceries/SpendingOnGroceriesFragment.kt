package com.mykaimeal.planner.fragment.commonfragmentscreen.spendingOnGroceries

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentSpendingOnGroceriesBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GrocereisExpenses
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.spendingOnGroceries.viewmodel.SpendingGroceriesViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpendingOnGroceriesFragment : Fragment() {

    private lateinit var binding: FragmentSpendingOnGroceriesBinding
    private var isOpen:Boolean=true
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue:Int=0
    private var status:String=""
    private lateinit var spendingGroceriesViewModel: SpendingGroceriesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentSpendingOnGroceriesBinding.inflate(inflater, container, false)

        spendingGroceriesViewModel = ViewModelProvider(this)[SpendingGroceriesViewModel::class.java]

        sessionManagement = SessionManagement(requireContext())

        val message = "How much do you " + (if (sessionManagement.getCookingFor().equals("Myself",true)) "typically" else "normally") + " spend on groceries per week/month?"
        val maxProgress = if (sessionManagement.getCookingFor().equals("Myself",true)) 10 else 11
        val progressValue = maxProgress - 2

        binding.tvSpendGroceries.text = message
        binding.progressBar9.max = maxProgress
        totalProgressValue = maxProgress
        updateProgress(progressValue)


        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile",true)
        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateSpendingGroc.visibility = if (isProfileScreen) View.VISIBLE else View.GONE

        if (isProfileScreen) {
            if (BaseApplication.isOnline(requireContext())) {
                spendingGroceriesApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        } else {
            spendingGroceriesViewModel.getGroceriesData()?.let {
                showDataInUi(it)
            }
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

    private fun spendingGroceriesApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            spendingGroceriesViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val bodyModel = gson.fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                bodyModel.data.grocereisExpenses?.let { it1 -> showDataInUi(it1) }
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                }else{
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("SpendingGroceries@@@","message:--"+e.message)
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

    private fun showDataInUi(groceriesExercise: GrocereisExpenses) {
        try {
            if (groceriesExercise.amount!=null){
                binding.etSpendingAmount.setText(groceriesExercise.amount.toString())
            }

            if (groceriesExercise.duration!=null){
                binding.tvChooseDuration.text = groceriesExercise.duration.toString()
            }
        }catch (e:Exception){
            Log.d("SpendingGroceries","message:--"+e.message)
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar9.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        binding.imgBackSpendGroceries.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.tvSkipBtn.setOnClickListener{
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener{
            if (status=="2"){
                val groceriesLocalData = GrocereisExpenses(
                    amount = "",
                    created_at = "",
                    deleted_at = null,
                    duration = "",
                    id = 0,         // Default or appropriate ID
                    updated_at = "",
                    user_id = 0  // Default or appropriate user ID
                )
                groceriesLocalData.amount=binding.etSpendingAmount.text.toString().trim()
                groceriesLocalData.duration=binding.tvChooseDuration.text.toString().trim().toLowerCase()
                spendingGroceriesViewModel.setGroceriesData(groceriesLocalData)

                sessionManagement.setSpendingAmount(binding.etSpendingAmount.text.toString().trim())
                sessionManagement.setSpendingDuration(binding.tvChooseDuration.text.toString().trim().toLowerCase())
                findNavController().navigate(R.id.eatingOutFragment)
            }
        }

        binding.etSpendingAmount.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (isEditing) return

                isEditing = true

                val text = s.toString()

                if (text.isNotEmpty()) {
                    if (!text.startsWith("$")) {
                        binding.etSpendingAmount.setText("$$text")
                        binding.etSpendingAmount.setSelection(binding.etSpendingAmount.text.length) // Move cursor to end
                    }
                }

                isEditing = false
            }
            override fun afterTextChanged(editText: Editable) {

                if (editText.length == 1 && editText.toString() == "$") {
                    editText.clear() // Remove the dollar sign if it's the only character left
                }
                searchable()
            }
        })

        binding.rlSelectDuration.setOnClickListener{
            if (isOpen){
                isOpen=false
                val drawableEnd = ContextCompat.getDrawable(requireContext(), R.drawable.drop_up_icon)
                drawableEnd!!.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
                binding.tvChooseDuration.setCompoundDrawables(null, null, drawableEnd, null)
                binding.relSelectWeekMonthly.visibility=View.VISIBLE
            }else{
                isOpen=true
                val drawableEnd = ContextCompat.getDrawable(requireContext(), R.drawable.drop_down_icon)
                drawableEnd!!.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
                binding.tvChooseDuration.setCompoundDrawables(null, null, drawableEnd, null)
                binding.relSelectWeekMonthly.visibility=View.GONE
            }
        }

        binding.rlSelectWeek.setOnClickListener{
            binding.tvChooseDuration.text="Weekly"
            binding.relSelectWeekMonthly.visibility=View.GONE
            val drawableEnd = ContextCompat.getDrawable(requireContext(), R.drawable.drop_down_icon)
            drawableEnd!!.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
            binding.tvChooseDuration.setCompoundDrawables(null, null, drawableEnd, null)
            isOpen=true
            searchable()
        }

        binding.rlSelectMonthly.setOnClickListener{
            binding.tvChooseDuration.text="Monthly"
            binding.relSelectWeekMonthly.visibility=View.GONE
            val drawableEnd = ContextCompat.getDrawable(requireContext(), R.drawable.drop_down_icon)
            drawableEnd!!.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
            binding.tvChooseDuration.setCompoundDrawables(null, null, drawableEnd, null)
            isOpen=true
            searchable()
        }

        binding.rlUpdateSpendingGroc.setOnClickListener{
            if (BaseApplication.isOnline(requireContext())) {
                updateSpendingGrocApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

    }

    private fun updateSpendingGrocApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            spendingGroceriesViewModel.updateSpendingGroceriesApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
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
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            },binding.etSpendingAmount.text.toString().trim(),binding.tvChooseDuration.text.toString().trim())
        }
    }

    private fun searchable() {
        if (binding.etSpendingAmount.text.isNotEmpty()){
            if (binding.tvChooseDuration.text.isNotEmpty()){
                status="2"
                binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            }else{
                status="1"
                binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            }
        }else{
            status="1"
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
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
            sessionManagement.setSpendingAmount("")
            sessionManagement.setSpendingDuration("")
            dialogStillSkip.dismiss()
            findNavController().navigate(R.id.eatingOutFragment)
        }
    }

}