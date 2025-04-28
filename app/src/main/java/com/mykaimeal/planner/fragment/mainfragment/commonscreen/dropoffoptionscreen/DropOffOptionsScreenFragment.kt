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
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.viewmodel.CheckoutScreenViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.model.DropOffOptionsModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.model.GetDropOffOptionsModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.model.GetDropOffOptionsModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.dropoffoptionscreen.viewmodel.DropOffOptionsScreenViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DropOffOptionsScreenFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentDropOffOptionsScreenBinding
    private lateinit var dropOffOptionsScreenViewModel: CheckoutScreenViewModel
    private var clickedstatus: String? = "Meet at my door"
    private lateinit var commonWorkUtils: CommonWorkUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDropOffOptionsScreenBinding.inflate(layoutInflater, container, false)

        dropOffOptionsScreenViewModel = ViewModelProvider(requireActivity())[CheckoutScreenViewModel::class.java]

        commonWorkUtils = CommonWorkUtils(requireActivity())

        backButton()

        if (BaseApplication.isOnline(requireActivity())) {
            getNotesList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

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

                apiModel.description?.let {
                    binding.edtInstructions.setText(it)
                }

                apiModel.data?.let {
                    if (it.size>0){
                        showDataNotesUI(it)
                    }
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun showDataNotesUI(data: MutableList<GetDropOffOptionsModelData>?) {

        val options = data?.let { listOf(
            Triple(data[0], binding.tvMeetAtDoor, binding.imgMeetDoor),
            Triple(data[1], binding.tvMeetOutside, binding.imgMeetOutside),
            Triple(data[2], binding.tvMeetReception, binding.imgMeetReception),
            Triple(data[3], binding.tvLeaveAtDoor, binding.imgLeaveAtDoor),
            Triple(data[4], binding.tvLeaveReception, binding.imgLeaveReception)
        ) }


        val statusTexts = listOf(
            "Meet at my door",
            "Meet outside",
            "Meet at reception",
            "Leave at my door",
            "Leave at reception"
        )

        options?.forEachIndexed { index, (item, textView, imageView) ->
            item.name?.let {
                textView.text = it
            }
            if (item.status == 1) {
                clickedstatus = statusTexts[index]
                textView.setTextColor(Color.parseColor("#000000"))
                textView.setTypeface(textView.typeface, Typeface.BOLD)
                imageView.setImageResource(R.drawable.radio_green_icon)

                if (index < 3) {
                    // First three options
                    binding.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                    binding.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                } else {
                    // Last two options
                    binding.relMeetHandMe.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                    binding.relLeaveDoorOpt.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                }
            }
        }

    }


    private fun initialize() {

        binding.relMeetDoor.setOnClickListener(this)
        binding.relMeetOutSide.setOnClickListener(this)
        binding.relMeetReception.setOnClickListener(this)

        binding.relLeaveDoor.setOnClickListener(this)
        binding.relLeaveReception.setOnClickListener(this)

        binding.relBack.setOnClickListener(this)
        binding.rlUpdate.setOnClickListener(this)


    }

    override fun onClick(item: View?) {
        when (item!!.id) {

            R.id.relBack -> {
                findNavController().navigateUp()
            }

            R.id.rlUpdate -> {
                    if (BaseApplication.isOnline(requireActivity())) {
                        addNotesUrl()
                    } else {
                        BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                    }
            }

            R.id.relMeetDoor -> {
                clickedstatus = "Meet at my door"
                binding.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                binding.tvMeetAtDoor.setTextColor(Color.parseColor("#000000"))
                binding.tvMeetAtDoor.setTypeface(binding.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding.imgMeetDoor.setImageResource(R.drawable.radio_green_icon)

                binding.tvMeetOutside.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetOutside.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgMeetOutside.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvMeetReception.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgMeetReception.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvLeaveAtDoor.setTextColor(Color.parseColor("#848484"))
                binding.tvLeaveAtDoor.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgLeaveAtDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvLeaveReception.setTextColor(Color.parseColor("#848484"))
                binding.tvLeaveReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgLeaveReception.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            R.id.relMeetOutSide -> {
                clickedstatus = "Meet outside"
                binding.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)

                binding.tvMeetAtDoor.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetAtDoor.setTypeface(binding.tvMeetAtDoor.typeface, Typeface.NORMAL)
                binding.imgMeetDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvMeetOutside.setTextColor(Color.parseColor("#000000"))
                binding.tvMeetOutside.setTypeface(binding.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding.imgMeetOutside.setImageResource(R.drawable.radio_green_icon)

                binding.tvMeetReception.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgMeetReception.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvLeaveAtDoor.setTextColor(Color.parseColor("#848484"))
                binding.tvLeaveAtDoor.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgLeaveAtDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvLeaveReception.setTextColor(Color.parseColor("#848484"))
                binding.tvLeaveReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgLeaveReception.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            R.id.relMeetReception -> {
                clickedstatus = "Meet at reception"
                binding.relMeetHandMe.setBackgroundResource(R.drawable.outline_green_border_white_bg)
                binding.relLeaveDoorOpt.setBackgroundResource(R.drawable.rectangular_shape_green_bg)

                binding.tvMeetAtDoor.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetAtDoor.setTypeface(binding.tvMeetAtDoor.typeface, Typeface.NORMAL)
                binding.imgMeetDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvMeetOutside.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetOutside.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgMeetOutside.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvMeetReception.setTextColor(Color.parseColor("#000000"))
                binding.tvMeetReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.BOLD
                )
                binding.imgMeetReception.setImageResource(R.drawable.radio_green_icon)

                binding.tvLeaveAtDoor.setTextColor(Color.parseColor("#848484"))
                binding.tvLeaveAtDoor.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgLeaveAtDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvLeaveReception.setTextColor(Color.parseColor("#848484"))
                binding.tvLeaveReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgLeaveReception.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            R.id.relLeaveDoor -> {
                clickedstatus = "Leave at my door"
                binding.relMeetHandMe.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                binding.relLeaveDoorOpt.setBackgroundResource(R.drawable.outline_green_border_white_bg)

                binding.tvMeetAtDoor.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetAtDoor.setTypeface(binding.tvMeetAtDoor.typeface, Typeface.NORMAL)
                binding.imgMeetDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvMeetOutside.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetOutside.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgMeetOutside.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvMeetReception.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgMeetReception.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvLeaveAtDoor.setTextColor(Color.parseColor("#000000"))
                binding.tvLeaveAtDoor.setTypeface(binding.tvMeetAtDoor.typeface, Typeface.BOLD)
                binding.imgLeaveAtDoor.setImageResource(R.drawable.radio_green_icon)

                binding.tvLeaveReception.setTextColor(Color.parseColor("#848484"))
                binding.tvLeaveReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgLeaveReception.setImageResource(R.drawable.radio_uncheck_gray_icon)
            }

            R.id.relLeaveReception -> {
                clickedstatus = "Leave at reception"

                binding.relMeetHandMe.setBackgroundResource(R.drawable.rectangular_shape_green_bg)
                binding.relLeaveDoorOpt.setBackgroundResource(R.drawable.outline_green_border_white_bg)

                binding.tvMeetAtDoor.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetAtDoor.setTypeface(binding.tvMeetAtDoor.typeface, Typeface.NORMAL)
                binding.imgMeetDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvMeetOutside.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetOutside.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgMeetOutside.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvMeetReception.setTextColor(Color.parseColor("#848484"))
                binding.tvMeetReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgMeetReception.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvLeaveAtDoor.setTextColor(Color.parseColor("#848484"))
                binding.tvLeaveAtDoor.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.NORMAL
                )
                binding.imgLeaveAtDoor.setImageResource(R.drawable.radio_uncheck_gray_icon)

                binding.tvLeaveReception.setTextColor(Color.parseColor("#000000"))
                binding.tvLeaveReception.setTypeface(
                    binding.tvMeetAtDoor.typeface,
                    Typeface.BOLD
                )
                binding.imgLeaveReception.setImageResource(R.drawable.radio_green_icon)
            }

        }
    }

    private fun addNotesUrl() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            dropOffOptionsScreenViewModel.addNotesUrl({
                BaseApplication.dismissMe()
                handleApiAddNotesResponse(it)
            }, clickedstatus, binding.edtInstructions.text.toString().trim())
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
                dropOffOptionsScreenViewModel.dataCheckOut?.note?.pickup=clickedstatus
                dropOffOptionsScreenViewModel.dataCheckOut?.note?.description=binding.edtInstructions.text.toString().trim()
                dropOffOptionsScreenViewModel.setCheckOutData(dropOffOptionsScreenViewModel.dataCheckOut)
                findNavController().navigateUp()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


}