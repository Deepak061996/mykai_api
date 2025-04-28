package com.mykaimeal.planner.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.ActivityEnterYourNameBinding
import com.mykaimeal.planner.messageclass.ErrorMessage

class EnterYourNameActivity : AppCompatActivity() {
    private var binding: ActivityEnterYourNameBinding? = null
    private var status: Boolean = true
    private lateinit var commonWorkUtils: CommonWorkUtils
    private var statusCheck: String = ""
    private lateinit var sessionManagement: SessionManagement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterYourNameBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)
        commonWorkUtils = CommonWorkUtils(this@EnterYourNameActivity)
        sessionManagement = SessionManagement(this@EnterYourNameActivity)

        ///main function using all triggered of this screen
        initialize()

    }

    private fun initialize() {

        // click event for Next Choose Cooking for Screen
        binding!!.rlSelectNextBtn.setOnClickListener {
            if (validate()) {
                if (statusCheck == "2") {
                    sessionManagement.setUserName(binding!!.etUserName.text.toString().trim())
                    sessionManagement.setGender(binding!!.tvChooseGender.text.toString().trim())
                    val intent = Intent(this@EnterYourNameActivity, CookingForScreenActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        // Add a TextWatcher to monitor changes in the username EditText field.
        // The searchable() function is triggered after text changes to enable or disable the "Next" button
        // based on the validity of the entered username.
        binding!!.etUserName.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                searchable()
            }
        })

        // handle for open and close dropdown menu gender list
        binding!!.rlSelectGender.setOnClickListener {
            if (status) {
                status = false
                val drawableEnd = ContextCompat.getDrawable(this, R.drawable.drop_up_icon)
                val drawableStart = ContextCompat.getDrawable(this, R.drawable.gender_icon)
                drawableEnd!!.setBounds(
                    0,
                    0,
                    drawableEnd.intrinsicWidth,
                    drawableEnd.intrinsicHeight
                )
                drawableStart!!.setBounds(
                    0,
                    0,
                    drawableStart.intrinsicWidth,
                    drawableStart.intrinsicHeight
                )
                binding!!.tvChooseGender.setCompoundDrawables(
                    drawableStart,
                    null,
                    drawableEnd,
                    null
                )
                binding!!.relSelectedGender.visibility = View.VISIBLE
            } else {
                status = true
                val drawableEnd = ContextCompat.getDrawable(this, R.drawable.drop_down_icon)
                val drawableStart = ContextCompat.getDrawable(this, R.drawable.gender_icon)
                drawableEnd!!.setBounds(
                    0,
                    0,
                    drawableEnd.intrinsicWidth,
                    drawableEnd.intrinsicHeight
                )
                drawableStart!!.setBounds(
                    0,
                    0,
                    drawableStart.intrinsicWidth,
                    drawableStart.intrinsicHeight
                )
                binding!!.tvChooseGender.setCompoundDrawables(
                    drawableStart,
                    null,
                    drawableEnd,
                    null
                )
                binding!!.relSelectedGender.visibility = View.GONE
            }
        }

        ///handle for selection male
        binding!!.rlSelectMale.setOnClickListener {
            binding!!.tvChooseGender.text = "Male"
            val drawableEnd = ContextCompat.getDrawable(this, R.drawable.drop_down_icon)
            val drawableStart = ContextCompat.getDrawable(this, R.drawable.gender_icon)
            drawableEnd!!.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
            drawableStart!!.setBounds(
                0,
                0,
                drawableStart.intrinsicWidth,
                drawableStart.intrinsicHeight
            )
            binding!!.tvChooseGender.setCompoundDrawables(drawableStart, null, drawableEnd, null)
            binding!!.relSelectedGender.visibility = View.GONE
            status = true
            searchable()
        }

        ///handle for selection female
        binding!!.rlSelectFemale.setOnClickListener {
            binding!!.tvChooseGender.text = "Female"
            val drawableEnd = ContextCompat.getDrawable(this, R.drawable.drop_down_icon)
            val drawableStart = ContextCompat.getDrawable(this, R.drawable.gender_icon)
            drawableEnd!!.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
            drawableStart!!.setBounds(
                0,
                0,
                drawableStart.intrinsicWidth,
                drawableStart.intrinsicHeight
            )
            binding!!.tvChooseGender.setCompoundDrawables(drawableStart, null, drawableEnd, null)
            binding!!.relSelectedGender.visibility = View.GONE
            status = true
            searchable()
        }
    }

    // The searchable() function is triggered after text changes to enable or disable the "Next" button
    private fun searchable() {
        if (binding!!.etUserName.text.isNotEmpty()) {
            if (binding!!.tvChooseGender.text.isNotEmpty()) {
                statusCheck = "2"
                binding!!.rlSelectNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            } else {
                statusCheck = "1"
                binding!!.rlSelectNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            }
        } else {
            statusCheck = "1"
            binding!!.rlSelectNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }


    // based on the validation of the entered username and selection gender.
    private fun validate(): Boolean {
        if (binding!!.etUserName.text.toString().isEmpty()) {
            commonWorkUtils.alertDialog(this@EnterYourNameActivity, ErrorMessage.name, false)
            return false
        } else if (binding!!.tvChooseGender.text.toString().isEmpty()) {
            commonWorkUtils.alertDialog(
                this@EnterYourNameActivity,
                ErrorMessage.selectGender,
                false
            )
            return false
        }
        return true
    }
}