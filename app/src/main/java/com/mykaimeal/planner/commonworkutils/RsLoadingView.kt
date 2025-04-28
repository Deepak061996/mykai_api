package com.mykaimeal.planner.commonworkutils

/*import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class RSLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN // Customize color
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private var scaleFactor = 0.5f
    private var alphaValue = 255

    private val animator = ValueAnimator.ofFloat(0.5f, 1.2f).apply {
        duration = 800
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = LinearInterpolator()
        addUpdateListener {
            scaleFactor = it.animatedValue as Float
            alphaValue = (255 * (1.2f - scaleFactor)).toInt() // Decreasing opacity
            invalidate()
        }
        start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = width.coerceAtMost(height) / 2.5f * scaleFactor

        circlePaint.alpha = alphaValue
        canvas.drawCircle(centerX, centerY, maxRadius, circlePaint)
    }

    fun stopAnimation() {
        animator.cancel()
    }
}*/

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

class RSLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    private val dotsCount = 12
    private val dotsRadius = 6f
    private var rotationAngle = 0f
    private var animator: ValueAnimator? = null

    init {
        startAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 3f

        for (i in 0 until dotsCount) {
            val angle = Math.toRadians((i * (360 / dotsCount) + rotationAngle).toDouble())
            val x = (centerX + radius * cos(angle)).toFloat()
            val y = (centerY + radius * sin(angle)).toFloat()

            dotPaint.alpha = ((255 * (i.toFloat() / dotsCount))).toInt()
            canvas.drawCircle(x, y, dotsRadius, dotPaint)
        }
    }

    private fun startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1200
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                rotationAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        animator?.cancel()
    }
}


