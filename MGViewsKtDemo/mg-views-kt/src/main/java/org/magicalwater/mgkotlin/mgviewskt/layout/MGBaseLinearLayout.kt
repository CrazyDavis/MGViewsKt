package org.magicalwater.mgkotlin.mgviewskt.layout

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import org.magicalwater.mgkotlin.mgviewskt.R

/**
 * Created by 志朋 on 2017/12/13.
 * 最基礎的封裝元件, 省的打一些重複的code
 * 繼承此元件請直接複製以下的建構式
constructor(context: Context) : super(context)
constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
 */

open class MGBaseLinearLayout : LinearLayout {

    //是否開啟觸摸時稍微透明
    var mTouchAlpha: Boolean = false
    var mStyleArray: TypedArray? = null

    var mRealX: Float = 0f
        get() = 0f + paddingLeft
    var mRealY: Float = 0f
        get() = 0f + paddingLeft
    var mRealWidth: Float = 0f
        get() = width.toFloat() - (paddingLeft + paddingRight)
    var mRealHeight: Float = 0f
        get() = height.toFloat() - (paddingTop + paddingBottom)

    constructor(context: Context) : super(context) {
        inflaterWidget(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        inflaterWidget(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        inflaterWidget(context, attrs)
    }

    private fun inflaterWidget(mContext: Context, attrs: AttributeSet?) {
        val layoutId = contentLayout()
        if (layoutId != null)
            View.inflate(context, layoutId, this)
        if (attrs != null && styleableWidget() != null) {
            mStyleArray = mContext.obtainStyledAttributes(attrs, styleableWidget())
            mTouchAlpha = mStyleArray!!.getBoolean(R.styleable.BaseWidget_bw_touch_alpha, false)
        }
//        setupWidget(mStyleArray)
    }

    //設定元件要套用的物件
    open fun contentLayout(): Int? = null

    //得到元件是否擁有自訂風格屬性
    open fun styleableWidget(): IntArray? = null

    //因為初始化問題, setupwidget請在繼承的class或者當下類別初始化init裡呼叫
    open fun setupWidget(style: TypedArray?) {}

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || !mTouchAlpha) {
            return super.onTouchEvent(event)
        }
        super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> this.alpha = 0.3f
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                this.alpha = 1f
            }
        }
        return true
    }
}