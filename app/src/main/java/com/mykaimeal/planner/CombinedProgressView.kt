package com.mykaimeal.planner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import kotlin.math.max
import kotlin.math.min

class CombinedProgressView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr)
    {

        private val thumbPaint = Paint().apply {
            color = ContextCompat.getColor(context, android.R.color.holo_blue_light)
            isAntiAlias = true
        }

        private val progressPaint = Paint().apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_dark)
            isAntiAlias = true
            strokeWidth = 10f
        }

        private val trackPaint = Paint().apply {
            color = ContextCompat.getColor(context, android.R.color.darker_gray)
            isAntiAlias = true
            strokeWidth = 5f
        }

        private val backgroundBar = View(context) // Full background bar
        private val progressBar = View(context)   // Completed progress bar
        private val markerView = AppCompatImageView(context) // Movable marker
        private val tickViews = mutableListOf<View>() // Array to store tick views
        private val labelViews = mutableListOf<TextView>() // Array to store labels
        private val topTickViews = mutableListOf<View>() // Array to store top tick views
        // private val topLabelViews = mutableListOf<TextView>() // Array to store top labels
        private val totalTicks = 6 // Total number of ticks (0 to 6)
        private var progress: Float = 0.5f // Starting progress at 50%
        private var maxWidth: Int = 0 // Width for progress bar calculations
        init {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressView)
            progress = typedArray.getFloat(R.styleable.ProgressView_progressValue, 0.5f)
            typedArray.recycle()
            setupUI()
        }

        private fun setupUI() {
            setupBars()
            setupTicks()
            setupMarker()
        }

        private fun setupBars() {
            // Background bar
            val backgroundBarParams = LayoutParams(LayoutParams.MATCH_PARENT, 12).apply {
                topMargin = 70 // Add margin to move the progress bar down
                bottomMargin = 70
                marginStart = 10
                marginEnd = 10
            }
            backgroundBar.setBackgroundColor(Color.LTGRAY)
            backgroundBar.layoutParams = backgroundBarParams
            addView(backgroundBar)

            // Progress bar
            val progressBarParams = LayoutParams(0, 12).apply {
                topMargin = 70 // Ensure the progress bar aligns with the background bar
                bottomMargin = 70
                // marginStart = 7
                // marginEnd = 7
            }
            progressBar.setBackgroundColor(Color.parseColor("#FFA500")) // Orange color
            progressBar.layoutParams = progressBarParams
            addView(progressBar)
        }
        private fun setupTicks() {
            tickViews.clear()
            labelViews.clear()
            topTickViews.clear()
            // topLabelViews.clear()

            for (i in 0..totalTicks) {
                // Bottom Ticks
                val bottomTick = View(context).apply {
                    setBackgroundColor(Color.parseColor("#FFA500")) // Default orange
                    layoutParams = LayoutParams(13, 25)
                }
                addView(bottomTick)
                tickViews.add(bottomTick)

                val bottomLabel = TextView(context).apply {
                    text = i.toString()
                    textSize = 15f
                    setTextColor(Color.DKGRAY)
                    textAlignment = TEXT_ALIGNMENT_CENTER
                }
                addView(bottomLabel)
                labelViews.add(bottomLabel)

                // Top Ticks
                val topTick = View(context).apply {
                    setBackgroundColor(Color.parseColor("#FFA500")) // Default orange
                    layoutParams = LayoutParams(13, 25)
                }
                addView(topTick)
                topTickViews.add(topTick)
            }
        }

        private fun setupMarker() {
            markerView.setImageResource(R.drawable.icon_miles_marker)
            markerView.setColorFilter(Color.parseColor("#FFA500")) // Orange tint
            markerView.layoutParams = LayoutParams(60, 60).apply {
                bottomMargin = 20
            }
            addView(markerView)

            markerView.setOnTouchListener { _, event ->
                handlePan(event)
                true
            }
        }

        private fun handlePan(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val x = event.rawX - backgroundBar.left
                    val newProgress = x / maxWidth
                    setProgress(newProgress)
                }
            }
        }

        private fun setProgress(newProgress: Float) {
            progress = max(0f, min(1f, newProgress)) // Clamp progress between 0 and 1
            updateProgress()
        }

        private fun updateProgress() {
            // Update progress bar width
            progressBar.layoutParams.width = (progress * maxWidth).toInt()
            progressBar.requestLayout()

            val markerY = backgroundBar.top - 70 // Move marker above progress bar
            ViewCompat.setTranslationX(markerView, progress * maxWidth - markerView.width / 2)
            ViewCompat.setTranslationY(markerView, markerY.toFloat() -40)

            // Update marker position
            //  ViewCompat.setTranslationX(markerView, progress * maxWidth - markerView.width / 2)

            // Update tick colors
            tickViews.forEachIndexed { index, tick ->
                val positionRatio = index.toFloat() / totalTicks
                tick.setBackgroundColor(if (positionRatio <= progress) Color.parseColor("#FFA500") else Color.LTGRAY)
            }
            // Update tick colors (top ticks)
            topTickViews.forEachIndexed { index, tick ->
                val positionRatio = index.toFloat() / totalTicks
                tick.setBackgroundColor(if (positionRatio <= progress) Color.parseColor("#FFA500") else Color.LTGRAY)
            }
        }


        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            maxWidth = backgroundBar.width

            // Position ticks and labels (both top and bottom)
            for (i in 0..totalTicks) {
                val positionX = backgroundBar.left + i * (maxWidth / totalTicks)

                // Bottom Ticks
                val bottomTickView = tickViews[i]
                val bottomLabelView = labelViews[i]

                bottomTickView.layout(
                    positionX - bottomTickView.width / 2, backgroundBar.bottom + 0,
                    positionX + bottomTickView.width / 2, backgroundBar.bottom + 32
                )

                bottomLabelView.layout(
                    positionX - bottomLabelView.width / 2, bottomTickView.bottom + 3,
                    positionX + bottomLabelView.width / 2, bottomTickView.bottom + 53
                )

                // Top Ticks
                val topTickView = topTickViews[i]
                //  val topLabelView = topLabelViews[i]

                topTickView.layout(
                    positionX - topTickView.width / 2, backgroundBar.top - 32,
                    positionX + topTickView.width / 2, backgroundBar.top - 0
                )
            }
            updateProgress()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Draw the track
            canvas.drawLine(
                0f,
                height / 2f,
                width.toFloat(),
                height / 2f,
                trackPaint
            )

            // Draw the progress
            val progressX = width * progress
            canvas.drawLine(
                0f,
                height / 2f,
                progressX,
                height / 2f,
                progressPaint
            )

            // Draw the thumb
            canvas.drawCircle(
                progressX,
                height / 2f,
                20f,
                thumbPaint
            )
        }
    }
