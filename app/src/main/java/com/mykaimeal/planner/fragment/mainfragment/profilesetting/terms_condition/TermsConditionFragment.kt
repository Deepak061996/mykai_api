package com.mykaimeal.planner.fragment.mainfragment.profilesetting.terms_condition

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentTermsConditionBinding
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.terms_condition.model.TermsConditionModel
import com.mykaimeal.planner.fragment.mainfragment.profilesetting.terms_condition.viewmodel.TermsConditionViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TermsConditionFragment : Fragment() {

    private lateinit var binding: FragmentTermsConditionBinding
    private lateinit var termsConditionViewModel: TermsConditionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTermsConditionBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        termsConditionViewModel = ViewModelProvider(this)[TermsConditionViewModel::class.java]

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()

                }
            })

        binding.imgBackTermsAndCondition.setOnClickListener {
            findNavController().navigateUp()
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

    private fun initialize() {

        if (BaseApplication.isOnline(requireActivity())) {
            termsConditionApi()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }


    /// Terms Condition Api
    private fun termsConditionApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            termsConditionViewModel.getTermCondition {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val termConditionModel = gson.fromJson(it.data, TermsConditionModel::class.java)
                            if (termConditionModel.code == 200 && termConditionModel.success) {
                                termConditionModel.data.description?.let {
                                    binding.descText.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY)
                                    } else {
                                        Html.fromHtml(it)
                                    }
                                }

                            } else {
                                if (termConditionModel.code == ErrorMessage.code) {
                                    showAlertFunction(termConditionModel.message, true)
                                } else {
                                    showAlertFunction(termConditionModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("TermsCondition","message:---"+e.message)
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

    /// show error message
    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


}