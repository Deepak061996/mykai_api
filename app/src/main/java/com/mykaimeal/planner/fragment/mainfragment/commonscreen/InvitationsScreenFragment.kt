package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.adapter.AdapterInviteItem
import com.mykaimeal.planner.databinding.FragmentInvitationsScreenBinding
import com.mykaimeal.planner.model.DataModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class InvitationsScreenFragment : Fragment() {

    private lateinit var binding: FragmentInvitationsScreenBinding
    private var dataList: MutableList<DataModel> = mutableListOf()
    private var adapterInviteItem: AdapterInviteItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentInvitationsScreenBinding.inflate(layoutInflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        inviteModel()
        initialize()

        return binding.root
    }



    private fun initialize() {
        binding.imgBackInvite.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.textInviteFriends.setOnClickListener {
            val appPackageName: String = requireActivity().packageName
            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            val body =
                "https://play.google.com/store/apps/details?id=$appPackageName"
            val sub = "Your Subject"
            myIntent.putExtra(Intent.EXTRA_SUBJECT, sub)
            myIntent.putExtra(Intent.EXTRA_TEXT, body)
            startActivity(Intent.createChooser(myIntent, "Share Using"))
        }

    }

    private fun inviteModel() {
        val data1 = DataModel()
        val data2 = DataModel()
        val data3 = DataModel()
        val data4 = DataModel()
        val data5 = DataModel()

        data1.title = "Danny"
        data1.isOpen = false
        data1.type = "Trial"

        data2.title = "Dora"
        data2.isOpen = false
        data2.type = "Trial Over"

        data3.title = "Lizel"
        data3.isOpen = false
        data3.type = "Redeemed"

        data4.title = "Kiara"
        data4.isOpen = false
        data4.type = "Trial"

        data5.title = "Jimy"
        data5.isOpen = false
        data5.type = "Trial"

        dataList.add(data1)
        dataList.add(data2)
        dataList.add(data3)
        dataList.add(data4)
        dataList.add(data5)

        adapterInviteItem = AdapterInviteItem(dataList, requireActivity())
        binding.rcyFriendsInvite.adapter = adapterInviteItem
    }

}