package com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentDropOffOptionsScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.model.DropOffOptionsModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.model.GetDropOffOptionsModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.model.GetDropOffOptionsModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.viewmodel.DropOffOptionsScreenViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DropOffOptionsScreenFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentDropOffOptionsScreenBinding? = null
    private lateinit var dropOffOptionsScreenViewModel: DropOffOptionsScreenViewModel
    private var clickedstatus: String? = "Meet at my door"
    private lateinit var commonWorkUtils: CommonWorkUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDropOffOptionsScreenBinding.inflate(layoutInflater, container, false)

        dropOffOptionsScreenViewModel =
            ViewModelProvider(requireActivity())[DropOffOptionsScreenViewModel::class.java]

        commonWorkUtils = CommonWorkUtils(requireActivity())

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        if (BaseApplication.isOnline(requireActivity())) {
            getNotesList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

        initialize()

        return binding!!.root
    }

    private fun getNotesList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dropOffOptionsScreenViewModel.getNotesUrl {
                BaseApplication.dismissMe()
                handleApiNotesResponse(it)
            }
        }
    }


    private fun handleApiNotesResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessNotesResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessNotesResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, GetDropOffOptionsModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {

                if (apiModel.description != null) {
                    binding!!.edtInstructions.setText(apiModel.description.toString())
                }

                if (apiModel.data != null && apiModel.data.size > 0) {
                    showDataNotesUI(apiModel.data)
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataNotesUI(data: MutableList<GetDropOffOptionsModelData>?) {

        if (data!![0].name != null) {
            binding!!.tvMeetAtDoor.text = data[0].name
        }

        if (data!![0].status != null) {
            if (data[0].status == 1) {
                clickedstatus = "Meet at my door"
                binding!!.tvMeetAtDoor.setTextColor(Color.parseColor("#000000"))
                binding!!.tvMeetAtDoor.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding!!.imgMeetDoor.setImageResource(R.drawable.radio_green_icon)

                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
            }
        }

        if (data!![1].name != null) {
            binding!!.tvMeetOutside.text = data[1].name
        }

        if (data!![1].status != null) {
            if (data[1].status == 1) {
                clickedstatus = "Meet outside"
                binding!!.tvMeetOutside.setTextColor(Color.parseColor("#000000"))
                binding!!.tvMeetOutside.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding!!.imgMeetOutside.setImageResource(R.drawable.radio_green_icon)

                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
            }
        }

        if (data!![2].name != null) {
            binding!!.tvMeetReception.text = data[2].name
        }

        if (data!![2].status != null) {
            if (data[2].status == 1) {
                clickedstatus = "Meet at reception"
                binding!!.tvMeetReception.setTextColor(Color.parseColor("#000000"))
                binding!!.tvMeetReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.BOLD
                )
                binding!!.imgMeetReception.setImageResource(R.drawable.radio_green_icon)

                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
            }
        }

        if (data!![3].name != null) {
            binding!!.tvLeaveAtDoor.text = data[3].name
        }

        if (data!![3].status != null) {
            if (data[3].status == 1) {
                clickedstatus = "Leave at my door"
                binding!!.tvLeaveAtDoor.setTextColor(Color.parseColor("#000000"))
                binding!!.tvLeaveAtDoor.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding!!.imgLeaveAtDoor.setImageResource(R.drawable.radio_green_icon)

                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
            }
        }

        if (data!![4].name != null) {
            binding!!.tvLeaveReception.text = data[4].name
        }

        if (data!![4].status != null) {
            if (data[4].status == 1) {
                clickedstatus = "Leave at reception"
                binding!!.tvLeaveReception.setTextColor(Color.parseColor("#000000"))
                binding!!.tvLeaveReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.BOLD
                )
                binding!!.imgLeaveReception.setImageResource(R.drawable.radio_green_icon)

                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.outline_green_border_white_bg)
            }
        }
    }


    private fun initialize() {

        binding!!.relMeetDoor.setOnClickListener(this)
        binding!!.relMeetOutSide.setOnClickListener(this)
        binding!!.relMeetReception.setOnClickListener(this)

        binding!!.relLeaveDoor.setOnClickListener(this)
        binding!!.relLeaveReception.setOnClickListener(this)

        binding!!.relBack.setOnClickListener(this)
        binding!!.rlUpdate.setOnClickListener(this)
    }

    override fun onClick(item: View?) {
        when (item!!.id) {

            R.id.relBack -> {
                findNavController().navigateUp()
            }

            R.id.rlUpdate -> {
                /*if (validate()) {*/
                    if (BaseApplication.isOnline(requireActivity())) {
                        addNotesUrl()
                    } else {
                        BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                    }
             /*   }*/
            }

            R.id.relMeetDoor -> {
                clickedstatus = "Meet at my door"
                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                binding!!.tvMeetAtDoor.setTextColor(Color.parseColor("#000000"))
                binding!!.tvMeetAtDoor.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding!!.imgMeetDoor.setImageResource(R.drawable.radio_green_icon)

                binding!!.tvMeetOutside.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetOutside.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgMeetOutside.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvMeetReception.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgMeetReception.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvLeaveAtDoor.setTextColor(Color.parseColor("#848484"))
                binding!!.tvLeaveAtDoor.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgLeaveAtDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvLeaveReception.setTextColor(Color.parseColor("#848484"))
                binding!!.tvLeaveReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgLeaveReception.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            R.id.relMeetOutSide -> {
                clickedstatus = "Meet outside"
                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)

                binding!!.tvMeetAtDoor.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetAtDoor.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.NORMAL)
                binding!!.imgMeetDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvMeetOutside.setTextColor(Color.parseColor("#000000"))
                binding!!.tvMeetOutside.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding!!.imgMeetOutside.setImageResource(R.drawable.radio_green_icon)

                binding!!.tvMeetReception.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgMeetReception.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvLeaveAtDoor.setTextColor(Color.parseColor("#848484"))
                binding!!.tvLeaveAtDoor.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgLeaveAtDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvLeaveReception.setTextColor(Color.parseColor("#848484"))
                binding!!.tvLeaveReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgLeaveReception.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            R.id.relMeetReception -> {
                clickedstatus = "Meet at reception"
                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)

                binding!!.tvMeetAtDoor.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetAtDoor.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.NORMAL)
                binding!!.imgMeetDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvMeetOutside.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetOutside.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgMeetOutside.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvMeetReception.setTextColor(Color.parseColor("#000000"))
                binding!!.tvMeetReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.BOLD
                )
                binding!!.imgMeetReception.setImageResource(R.drawable.radio_green_icon)

                binding!!.tvLeaveAtDoor.setTextColor(Color.parseColor("#848484"))
                binding!!.tvLeaveAtDoor.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgLeaveAtDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvLeaveReception.setTextColor(Color.parseColor("#848484"))
                binding!!.tvLeaveReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgLeaveReception.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            R.id.relLeaveDoor -> {
                clickedstatus = "Leave at my door"
                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.outline_green_border_white_bg)

                binding!!.tvMeetAtDoor.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetAtDoor.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.NORMAL)
                binding!!.imgMeetDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvMeetOutside.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetOutside.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgMeetOutside.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvMeetReception.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgMeetReception.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvLeaveAtDoor.setTextColor(Color.parseColor("#000000"))
                binding!!.tvLeaveAtDoor.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding!!.imgLeaveAtDoor.setImageResource(R.drawable.radio_green_icon)

                binding!!.tvLeaveReception.setTextColor(Color.parseColor("#848484"))
                binding!!.tvLeaveReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgLeaveReception.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            R.id.relLeaveReception -> {
                clickedstatus = "Leave at reception"

                binding!!.relMeetHandMe.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                binding!!.relLeaveDoorOpt.setBackgroundResource(R.drawable.outline_green_border_white_bg)

                binding!!.tvMeetAtDoor.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetAtDoor.setTypeface(binding!!.tvMeetAtDoor.typeface, Typeface.NORMAL)
                binding!!.imgMeetDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvMeetOutside.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetOutside.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgMeetOutside.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvMeetReception.setTextColor(Color.parseColor("#848484"))
                binding!!.tvMeetReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgMeetReception.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvLeaveAtDoor.setTextColor(Color.parseColor("#848484"))
                binding!!.tvLeaveAtDoor.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding!!.imgLeaveAtDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding!!.tvLeaveReception.setTextColor(Color.parseColor("#000000"))
                binding!!.tvLeaveReception.setTypeface(
                    binding!!.tvMeetAtDoor.typeface,
                    Typeface.BOLD
                )
                binding!!.imgLeaveReception.setImageResource(R.drawable.radio_green_icon)
            }

        }
    }


/*    /// add validation based on valid email or phone
    private fun validate(): Boolean {
        // Check if email/phone is empty

        if (binding!!.edtInstructions.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.deliveryInstructions, false)
            return false
        }
        return true

    }*/

    private fun addNotesUrl() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dropOffOptionsScreenViewModel.addNotesUrl({
                BaseApplication.dismissMe()
                handleApiAddNotesResponse(it)
            }, clickedstatus, binding!!.edtInstructions.text.toString().trim())
        }
    }

    private fun handleApiAddNotesResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddNotesResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddNotesResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, DropOffOptionsModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                findNavController().navigateUp()
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


}