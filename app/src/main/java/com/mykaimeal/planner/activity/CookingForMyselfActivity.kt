package com.mykaimeal.planner.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.ActivityCookingMyselfBinding
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CookingForMyselfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCookingMyselfBinding
    private lateinit var sessionManagement: SessionManagement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCookingMyselfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManagement = SessionManagement(this)
        Log.d("sessionManagement.getCookingFor() ","****"+sessionManagement.getCookingFor())

        initialize()

    }

    private fun initialize() {
        setStartDestination()
    }

    private fun setStartDestination() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.frameLayoutAuth) as? NavHostFragment
        if (navHostFragment == null) {
            Toast.makeText(this, ErrorMessage.navigationError, Toast.LENGTH_SHORT).show()
            return
        }
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.main_graph)

        val cookingFor = sessionManagement.getCookingFor() ?: ""
        val startDestination = when (cookingFor) {
            "Myself" -> R.id.bodyGoalsFragment
            "MyPartner" -> R.id.partnerInfoDetailsFragment
            else -> R.id.familyMembersFragment
        }

        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
    }
}
