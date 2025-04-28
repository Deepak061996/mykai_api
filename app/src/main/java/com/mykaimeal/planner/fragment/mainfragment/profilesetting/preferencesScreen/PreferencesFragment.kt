package com.mykaimeal.planner.fragment.mainfragment.profilesetting.preferencesScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.model.DataPreferencesModel
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.PreferencesAdapter
import com.mykaimeal.planner.databinding.FragmentPreferencesBinding

class PreferencesFragment : Fragment(), OnItemClickListener {

    private lateinit var binding: FragmentPreferencesBinding
    private lateinit var sessionManagement: SessionManagement
    private var preferenceAdapter: PreferencesAdapter? = null
    private var screenType: String? = null
    private val dataList = ArrayList<DataPreferencesModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreferencesBinding.inflate(inflater, container, false)

        (activity as MainActivity?)?.apply {
            binding.llIndicator.visibility = View.GONE
            binding.llBottomNavigation.visibility = View.GONE
        }

        sessionManagement = SessionManagement(requireActivity())
        screenType = sessionManagement.getCookingFor()

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
        populateDataList(screenType)

        binding.imgBackPreferences.setOnClickListener {
            findNavController().navigateUp()
        }

        preferenceAdapter = PreferencesAdapter(dataList, requireActivity(), this)
        binding.recyPreferences.adapter = preferenceAdapter

    }

    private fun populateDataList(type: String?) {

        val dataMap = when (type) {
            "Myself" -> listOf(
                "Body Goals",
                "Dietary Restrictions",
                "Favorite Cuisines",
                "Disliked Ingredient",
                "Allergies",
                "Meal Routine",
                "Cooking Frequency",
                "Spending on Groceries",
                "Eating Out",
                "Reason Take Away"
            )
            "MyPartner" -> listOf(
                "Partner Info",
                "Body Goals",
                "Dietary Restrictions",
                "Disliked Ingredient",
                "Allergies",
                "Favorite Cuisines",
                "Meal Prep Days",
                "Cooking Frequency",
                "Spending on Groceries",
                "Eating Out",
                "Reason Take Away"
            )
            else -> listOf(
                "Family Members",
                "Body Goals",
                "Dietary Restrictions",
                "Disliked Ingredient",
                "Allergies",
                "Favorite Cuisines",
                "Family Meal Preferences",
                "Cooking Frequency",
                "Spending on Groceries",
                "Eating Out",
                "Reason Take Away"
            )
        }

        dataList.clear()
        dataMap.forEach {
            dataList.add(DataPreferencesModel(it, false, type))
        }
    }

    override fun itemClick(position: Int?, cookingType: String?, tittleName: String?) {
        sessionManagement.setCookingScreen("Profile")
        sessionManagement.setCookingFor(cookingType ?: "")
        val navigationMap = mapOf(
            "Body Goals" to R.id.bodyGoalsFragment,
            "Dietary Restrictions" to R.id.dietaryRestrictionsFragment,
            "Favorite Cuisines" to R.id.favouriteCuisinesFragment,
            "Disliked Ingredient" to R.id.ingredientDislikesFragment,
            "Allergies" to R.id.allergensIngredientsFragment,
            "Meal Routine" to R.id.mealRoutineFragment,
            "Cooking Frequency" to R.id.cookingFrequencyFragment,
            "Spending on Groceries" to R.id.spendingOnGroceriesFragment,
            "Eating Out" to R.id.eatingOutFragment,
            "Reason Take Away" to R.id.reasonsForTakeAwayFragment,
            "Partner Info" to R.id.partnerInfoDetailsFragment,
            "Meal Prep Days" to R.id.mealRoutineFragment,
            "Family Members" to R.id.familyMembersFragment,
            "Family Meal Preferences" to R.id.mealRoutineFragment
        )
        navigationMap[tittleName]?.let {
            findNavController().navigate(it)
        }
    }
}
