package org.magicalwater.mgkotlin.mgviewskt.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import org.magicalwater.mgkotlin.mgviewskt.R
import org.jetbrains.anko.*

/**
 * Created by 志朋 on 2017/12/13.
 * View最基礎的構建, 封裝了一些設定省的每次初始化都在打一次
 * 繼承此view請直接複製以下建構式
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
 */
abstract class MGBaseView: View {

    //是否開啟觸摸時稍微透明
    var touchAlpha: Boolean = false
    protected var styleArray: TypedArray? = null

    var realX: Float = 0f
        get() = 0f + leftPadding
    var realY: Float = 0f
        get() = 0f + topPadding
    var realWidth: Float = 0f
        get() = width.toFloat() - (leftPadding + rightPadding)
    var realHeight: Float = 0f
        get() = height.toFloat() - (topPadding + bottomPadding)

    var realCenterX: Float = 0f
        get() = realX + realWidth / 2
    var realCenterY: Float = 0f
        get() = realY + realHeight / 2

    var realEndX: Float = 0f
        get() = realX + realWidth
    var realEndY: Float = 0f
        get() = realY + realHeight

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    //在此還不能能操作其餘屬性, 直接使用 typeArray 賦予值
    //底下的view要操作屬性必須在 init {} 裡
    private fun initView(mContext: Context, attrs: AttributeSet?) {
        if (attrs != null && styleableView() != null) {
            styleArray = mContext.obtainStyledAttributes(attrs, styleableView())
            touchAlpha = styleArray!!.getBoolean(R.styleable.MGBaseView_bv_touch_alpha, false)
        }
    }

    //得到元件是否擁有自訂風格屬性
    open fun styleableView(): IntArray? = null

    abstract fun setupView(style: TypedArray?)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || !touchAlpha) {
            return super.onTouchEvent(event)
        }
        super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> this.alpha = 0.7f
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                this.alpha = 1f
            }
        }
        return true
    }
}