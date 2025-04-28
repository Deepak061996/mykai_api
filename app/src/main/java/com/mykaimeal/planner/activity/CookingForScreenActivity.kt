package com.mykaimeal.planner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.ActivityCookingForScreenBinding

class CookingForScreenActivity : AppCompatActivity() {

    private var binding: ActivityCookingForScreenBinding?=null
    private lateinit var sessionManagement: SessionManagement
    private var clickStatus:String?="Myself"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCookingForScreenBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)

        sessionManagement = SessionManagement(this@CookingForScreenActivity)
        sessionManagement.setCookingFor(clickStatus!!)

        ///main function using all triggered of this screen
        initialize()
    }

    private fun initialize() {

        //handle click event for Myself
        binding!!.relMySelf.setOnClickListener{
            clickStatus="Myself"
            binding!!.relMySelf.setBackgroundResource(R.drawable.orange_box_bg)
            binding!!.relMySelfPartner.setBackgroundResource(R.drawable.gray_box_border_bg)
            binding!!.relMyFamily.setBackgroundResource(R.drawable.gray_box_border_bg)

            binding!!.imageMyselfTick.visibility= View.VISIBLE
            binding!!.imageMyselfMyPartnerTick.visibility= View.GONE
            binding!!.imageMyFamily.visibility= View.GONE
        }

        //handle click event for MyPartner
        binding!!.relMySelfPartner.setOnClickListener{
            clickStatus="MyPartner"
            binding!!.relMySelf.setBackgroundResource(R.drawable.gray_box_border_bg)
            binding!!.relMySelfPartner.setBackgroundResource(R.drawable.orange_box_bg)
            binding!!.relMyFamily.setBackgroundResource(R.drawable.gray_box_border_bg)

            binding!!.imageMyselfTick.visibility= View.GONE
            binding!!.imageMyselfMyPartnerTick.visibility= View.VISIBLE
            binding!!.imageMyFamily.visibility= View.GONE
        }

        //handle click event for MyFamily
        binding!!.relMyFamily.setOnClickListener{
            clickStatus="MyFamily"
            binding!!.relMySelf.setBackgroundResource(R.drawable.gray_box_border_bg)
            binding!!.relMySelfPartner.setBackgroundResource(R.drawable.gray_box_border_bg)
            binding!!.relMyFamily.setBackgroundResource(R.drawable.orange_box_bg)

            binding!!.imageMyselfTick.visibility= View.GONE
            binding!!.imageMyselfMyPartnerTick.visibility= View.GONE
            binding!!.imageMyFamily.visibility= View.VISIBLE
        }

        ///handle click event for next screen MYSelf, MyPartner or MyFamily
        binding!!.rlNextCooking.setOnClickListener{
            sessionManagement.setCookingFor(clickStatus!!)
            sessionManagement.setCookingScreen("")

            Log.d("sessionManagement.getCookingFor() ","****"+sessionManagement.getCookingFor())

            val intent = Intent(this@CookingForScreenActivity, CookingForMyselfActivity::class.java)
            startActivity(intent)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}