package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.databinding.FragmentTescoCartItemFragmentBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TescoCartItemFragmentFragment : Fragment() {
    private lateinit var binding: FragmentTescoCartItemFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentTescoCartItemFragmentBinding.inflate(layoutInflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        initialize()

        return binding.root
    }

    private fun initialize() {
        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }
    }

}