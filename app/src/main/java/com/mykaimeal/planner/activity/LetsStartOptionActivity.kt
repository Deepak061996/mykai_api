package com.mykaimeal.planner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mykaimeal.planner.databinding.ActivityLetsStartOptionBinding

class LetsStartOptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLetsStartOptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLetsStartOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    private fun initialize() {
        // Set an OnClickListener for the "Login" TextView
        binding.tvLogin.setOnClickListener {
            navigateToAuthActivity("login")
        }

        // Set an OnClickListener for the "Lets Start cooking" Button
        binding.rlLetsCooking.setOnClickListener {
           /* navigateToAuthActivity("signup")*/
            val intent = Intent(this@LetsStartOptionActivity, IntroPageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToAuthActivity(type: String) {
        val intent = Intent(this, AuthActivity::class.java).apply {
            putExtra("type", type)
            putExtra("backType", "yes")
        }
        startActivity(intent)
    }

}
