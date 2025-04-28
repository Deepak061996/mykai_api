package com.mykaimeal.planner.fragment.mainfragment.hometab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.databinding.FragmentRateYourMealBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RateYourMealFragment : Fragment() {

    private lateinit var binding: FragmentRateYourMealBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentRateYourMealBinding.inflate(layoutInflater, container, false)
        
        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }
        

        backButton()

        binding.imgBackRateMeal.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.relPublishReview.setOnClickListener{
            findNavController().navigate(R.id.homeFragment)
        }

        return binding.root
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }


}