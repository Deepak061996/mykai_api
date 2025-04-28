package com.mykaimeal.planner.fragment.commonfragmentscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AdapterCookingSchedule
import com.mykaimeal.planner.databinding.FragmentCookingScheduleBinding
import com.mykaimeal.planner.model.DataModel

class CookingScheduleFragment : Fragment(),OnItemClickListener {

    private lateinit var binding: FragmentCookingScheduleBinding
    private val dataList = ArrayList<DataModel>()
    private var dietaryRestrictionsAdapter: AdapterCookingSchedule? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue:Int=0
    private var status:String?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCookingScheduleBinding.inflate(inflater, container, false)

        sessionManagement = SessionManagement(requireContext())
        if (sessionManagement.getCookingFor().equals("Myself")){
            binding.textCookingSch.visibility=View.VISIBLE
            binding.textCookingFamilySch.visibility=View.GONE
            binding.tvCookingSchDesc.text="Select the days you usually cook or prep meals"
            binding.progressBar8.max=10
            totalProgressValue=10
            updateProgress(8)
        } else if (sessionManagement.getCookingFor().equals("MyPartner")){
            binding.textCookingSch.visibility=View.VISIBLE
            binding.textCookingFamilySch.visibility=View.GONE
            binding.tvCookingSchDesc.text="Select the days you usually cook or prep meals"
            binding.progressBar8.max=11
            totalProgressValue=11
            updateProgress(7)
        } else {
            binding.textCookingSch.visibility=View.GONE
            binding.textCookingFamilySch.visibility=View.VISIBLE
            binding.tvCookingSchDesc.text="Which days do you normally meal prep or cook for your family?"
            binding.progressBar8.max=11
            totalProgressValue=11
            updateProgress(7)
        }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

//        cookingScheduleModel()
        initialize()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar8.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        binding.imgBackCookingSch.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.tvSkipBtn.setOnClickListener{
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener{
            if (status=="2"){
                if (sessionManagement.getCookingFor().equals("Myself")){
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
                } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                    findNavController().navigate(R.id.mealRoutineFragment)
                } else {
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
                }
            }
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
            if (sessionManagement.getCookingFor().equals("Myself")){
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            } else if (sessionManagement.getCookingFor().equals("MyPartner")) {
                findNavController().navigate(R.id.mealRoutineFragment)
            } else {
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            }
        }
    }

//    private fun cookingScheduleModel() {
//        val data1 = DataModel()
//        val data2 = DataModel()
//        val data3 = DataModel()
//        val data4 = DataModel()
//        val data5 = DataModel()
//        val data6 = DataModel()
//        val data7 = DataModel()
//
//        data1.title = "Sunday"
//        data1.isOpen= false
//        data1.type = "CookingSchedule"
//
//        data2.title = "Monday"
//        data2.isOpen= false
//        data2.type = "CookingSchedule"
//
//        data3.title = "Tuesday"
//        data3.isOpen= false
//        data3.type = "CookingSchedule"
//
//        data4.title = "Wednesday"
//        data4.isOpen= false
//        data4.type = "CookingSchedule"
//
//        data5.title = "Thursday"
//        data5.isOpen= false
//        data5.type = "CookingSchedule"
//
//        data6.title = "Friday"
//        data6.isOpen= false
//        data6.type = "CookingSchedule"
//
//        data7.title = "Saturday"
//        data7.isOpen= false
//        data7.type = "CookingSchedule"
//
//        dataList.add(data1)
//        dataList.add(data2)
//        dataList.add(data3)
//        dataList.add(data4)
//        dataList.add(data5)
//        dataList.add(data6)
//        dataList.add(data7)
//
//        dietaryRestrictionsAdapter = AdapterCookingSchedule(dataList, requireActivity(),this)
//        binding.rcyCookingSch.adapter = dietaryRestrictionsAdapter
//    }

    override fun itemClick(position: Int?, status1: String?, type: String?) {
        if (status1 == "1") {
            status=""
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        } else {
            status="2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)

        }
    }

}