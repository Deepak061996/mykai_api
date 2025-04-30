package com.mykaimeal.planner.fragment.authfragment.notificationmodel

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
import com.mykaimeal.planner.databinding.FragmentTurnOnNotificationsBinding
import com.mykaimeal.planner.fragment.authfragment.notificationmodel.model.NotificationModel
import com.mykaimeal.planner.fragment.authfragment.notificationmodel.viewmodel.NotificationViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TurnOnNotificationsFragment : Fragment() {

    private var binding: FragmentTurnOnNotificationsBinding? = null
    private lateinit var notificationViewModel: NotificationViewModel
    private var status: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTurnOnNotificationsBinding.inflate(inflater, container, false)

        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        ///handle on back pressed
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        ///main function using all triggered of this screen
        initialize()

        return binding!!.root
    }

    private fun initialize() {

        /// handle on back pressed
        binding!!.imgBackNotifications.setOnClickListener {
            findNavController().navigateUp()
        }

        ///checking the device of mobile data in online and offline(show network error message)
        /// turn on notification permission and implement api
        binding!!.rlTurnOnNotifications.setOnClickListener {
            status = "1"
            if (BaseApplication.isOnline(requireActivity())) {
                notificationApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        /// handle click event notification permission denied in this time
        binding!!.tvNotNow.setOnClickListener {
            status = "0"
            if (BaseApplication.isOnline(requireActivity())) {
                notificationApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    /// implement notification api & redirection
    private fun notificationApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            notificationViewModel.updateNotification(
                {
                    BaseApplication.dismissMe()
                    when (it) {
                        is NetworkResult.Success -> {
                            try {
                                val gson = Gson()
                                val notificationModel = gson.fromJson(it.data, NotificationModel::class.java)
                                if (notificationModel.code == 200 && notificationModel.success) {
                                    val bundle = Bundle()
                                    bundle.putString("screen","login")
                                    findNavController().navigate(R.id.subscriptionPlanOverViewFragment,bundle)
                                } else {
                                  handleError(notificationModel.code,notificationModel.message)
                                }
                            }catch (e:Exception){
                                Log.d("Notifications","message:---"+e.message)
                            }
                        }

                        is NetworkResult.Error -> {
                            showAlertFunction(it.message, false)
                        }

                        else -> {
                            showAlertFunction(it.message, false)
                        }
                    }
                }, status
            )
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        } else {
            showAlertFunction(message, false)
        }
    }

    /// show error message
    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

}