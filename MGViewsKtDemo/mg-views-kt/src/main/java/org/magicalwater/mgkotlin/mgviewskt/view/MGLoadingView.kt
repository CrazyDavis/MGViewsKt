package org.magicalwater.mgkotlin.mgviewskt.view

import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

open class MGLoadingView: AppCompatImageView {

    private lateinit var va: ValueAnimator

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        va = ValueAnimator.ofFloat(0f, 360f).setDuration(800)
        va.repeatMode = ValueAnimator.RESTART
        va.repeatCount = ValueAnimator.INFINITE
        va.interpolator = LinearInterpolator()
        va.addUpdateListener { animation ->
            rotation = animation.animatedValue as Float
        }
        va.start()
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) {
            startRotate()
        } else {
            endRotate()
        }
        super.setVisibility(visibility)
    }

    fun startRotate() {
        va.start()
    }

    fun endRotate() {
        va.end()
    }
}