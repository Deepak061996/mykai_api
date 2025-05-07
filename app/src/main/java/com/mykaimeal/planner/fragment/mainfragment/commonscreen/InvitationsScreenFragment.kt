package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.adapter.AdapterInviteItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentInvitationsScreenBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.ReferralInvitationModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.ReferralInvitationModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel.StatisticsViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InvitationsScreenFragment : Fragment() {

    private lateinit var binding: FragmentInvitationsScreenBinding
    private var adapterInviteItem: AdapterInviteItem? = null
    private lateinit var statisticsViewModel: StatisticsViewModel
    private var referralList: MutableList<ReferralInvitationModelData> =mutableListOf()
    private var referLink: String = ""
    private lateinit var sessionManagement: SessionManagement

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentInvitationsScreenBinding.inflate(layoutInflater, container, false)

        statisticsViewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]
        sessionManagement = SessionManagement(requireActivity())

        backButton()

        adapterInviteItem = AdapterInviteItem(referralList, requireActivity())
        binding.rcyFriendsInvite.adapter = adapterInviteItem



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

        binding.imgBackInvite.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.spinnerFilterType.setIsFocusable(true)

        binding.spinnerFilterType.setItems(
            listOf("All", "Trial", "Trial over", "Myka", "Redeemed")
        )

        binding.spinnerFilterType.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            filterData(newItem) // Call your filter function with the selected value
        }


        generateDeepLink()


        if (BaseApplication.isOnline(requireContext())) {
            getInvitationList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

      /*  binding.textInviteFriends.setOnClickListener {
            val appPackageName: String = requireActivity().packageName
            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            val body =
                "https://play.google.com/store/apps/details?id=$appPackageName"
            val sub = "Your Subject"
            myIntent.putExtra(Intent.EXTRA_SUBJECT, sub)
            myIntent.putExtra(Intent.EXTRA_TEXT, body)
            startActivity(Intent.createChooser(myIntent, "Share Using"))
        }*/

    }

    // Filter data using only `data` list
    private fun filterData(filter: String) {
        val filtered = if (filter.equals("All", ignoreCase = true)) {
            referralList // just show current list
        } else {
            referralList.filter { it.status?.trim().equals(filter, ignoreCase = true) }.toMutableList()
        }
        adapterInviteItem?.updateList(filtered)
    }

    private fun getInvitationList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            statisticsViewModel.referralUrl {
                BaseApplication.dismissMe()
                handleApiReferralResponse(it)
            }
        }
    }


    private fun handleApiReferralResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessReferralResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessReferralResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ReferralInvitationModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                apiModel.data?.let {
                    showInvitationList(it)
                }?:run {
                    invitedValue()
                }
            } else {
                invitedValue()
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }

            }
        } catch (e: Exception) {
            invitedValue()
            showAlert(e.message, false)
        }
    }

    private fun invitedValue(){
        val htmlText = "You have invited 0 friends to use<b> MyKai</b>"
        val formattedText = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvFriendsCountNumber.text = formattedText
    }

    private fun showInvitationList(data: MutableList<ReferralInvitationModelData>) {
        try {
            referralList.clear()
            data.let {
                referralList.addAll(it)
                if (referralList.size > 0) {
                    val invitedCount = referralList.size.toString()
                    val htmlText = "You have invited $invitedCount friends to use<b> MyKai</b>"
                    val formattedText = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    binding.tvFriendsCountNumber.text = formattedText
                    binding.rcyFriendsInvite.visibility = View.VISIBLE
                    adapterInviteItem?.updateList(referralList)
                } else {
                    invitedValue()
                    binding.rcyFriendsInvite.visibility = View.GONE
                }
            }
        }catch (e:Exception){
            invitedValue()
           Log.d("@Error ","*********"+e.message)
        }
    }

}