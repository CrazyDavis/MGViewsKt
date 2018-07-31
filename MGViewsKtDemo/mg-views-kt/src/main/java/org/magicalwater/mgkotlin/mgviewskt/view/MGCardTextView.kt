package org.magicalwater.mgkotlin.mgviewskt.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.widget.TextView
import org.magicalwater.mgkotlin.mgextensionkt.getColor
import org.magicalwater.mgkotlin.mgviewskt.R
import org.magicalwater.mgkotlin.mgextensionkt.px

/**
 * Created by magicalwater on 2018/1/16.
 */
open class MGCardTextView: AppCompatTextView {

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    var mStrokeColor: Int = Color.BLACK
    var mStrokeWidth: Int = 1.px
    var mStartColor: Int = Color.BLUE
    var mEndColor: Int = Color.BLUE
    var mGlideColor: Int
        get() = mStartColor
        set(value) {
            mStartColor = value
            mEndColor = value
        }
    var mRadius: Int = 2.px
    var mTouchAlpha: Boolean = false

    //在此還不能能操作其餘屬性, 直接使用 typeArray 賦予值
    //底下的view要操作屬性必須在 init {} 裡
    private fun initView(mContext: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            var styleArray = mContext.obtainStyledAttributes(attrs, R.styleable.MGCardTextView)
            mTouchAlpha = styleArray.getBoolean(R.styleable.MGBaseView_bv_touch_alpha, false)
            mStrokeColor = styleArray.getColor(R.styleable.MGCardTextView_ctw_stroke_clr, mStrokeColor)
            mStartColor = styleArray.getColor(R.styleable.MGCardTextView_ctw_start_clr, mStartColor)
            mEndColor = styleArray.getColor(R.styleable.MGCardTextView_ctw_end_clr, mEndColor)
            mStrokeWidth = styleArray.getDimensionPixelSize(R.styleable.MGCardTextView_ctw_stroke_width, mStrokeWidth)
            val glideColor = styleArray.getColor(R.styleable.MGCardTextView_ctw_glide_clr)
            if (glideColor != null) {
                mGlideColor = glideColor
            }
            mRadius = styleArray.getDimensionPixelSize(R.styleable.MGCardTextView_ctw_radius, mRadius)
        }
        settingBackground()
    }

    private fun settingBackground() {
        val drawable = resources.getDrawable(R.drawable.bg_card).constantState.newDrawable().mutate()
        background = drawable
        (background as GradientDrawable).setStroke(mStrokeWidth, mStrokeColor)
        (background as GradientDrawable).colors = intArrayOf(mStartColor, mEndColor)
        (background as GradientDrawable).cornerRadius = mRadius.toFloat()
    }

}