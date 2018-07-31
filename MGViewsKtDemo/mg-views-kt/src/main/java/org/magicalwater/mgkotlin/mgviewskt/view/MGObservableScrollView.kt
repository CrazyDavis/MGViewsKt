package org.magicalwater.mgkotlin.mgviewskt.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ScrollView

/**
 * Created by magicalwater on 2018/1/4.
 * 可監聽, 1. 滑動距離 2. 滑動到最底部
 */
open class MGObservableScrollView: ScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    private var isScrolledToTop: Boolean = true // 初始化的时候设置一下值
    private var isScrolledToBottom: Boolean = false

    var nowPos: ScrollPos = ScrollPos.TOP

    var scrollDelegate: ObserableScrollDelegate? = null


    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        if (scrollY == 0) {
            isScrolledToTop = clampedY
            isScrolledToBottom = false
        } else {
            isScrolledToTop = false
            isScrolledToBottom = clampedY
        }
        notifyScrollChangedListeners()
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (android.os.Build.VERSION.SDK_INT < 9) {  // API 9及之后走onOverScrolled方法监听
            when {
                scrollY == 0 -> {    // 小心踩坑1: 这里不能是getScrollY() <= 0
                    isScrolledToTop = true
                    isScrolledToBottom = false
                }
                scrollY + height - paddingTop - paddingBottom == getChildAt(0).height -> {
                    // 小心踩坑2: 这里不能是 >=
                    // 小心踩坑3（可能忽视的细节2）：这里最容易忽视的就是ScrollView上下的padding　
                    isScrolledToBottom = true
                    isScrolledToTop = false
                }
                else -> {
                    isScrolledToTop = false
                    isScrolledToBottom = false
                }
            }
            notifyScrollChangedListeners()
        }
        // 有时候写代码习惯了，为了兼容一些边界奇葩情况，上面的代码就会写成<=,>=的情况，结果就出bug了
        // 我写的时候写成这样：getScrollY() + getHeight() >= getChildAt(0).getHeight()
        // 结果发现快滑动到底部但是还没到时，会发现上面的条件成立了，导致判断错误
        // 原因：getScrollY()值不是绝对靠谱的，它会超过边界值，但是它自己会恢复正确，导致上面的计算条件不成立
        // 仔细想想也感觉想得通，系统的ScrollView在处理滚动的时候动态计算那个scrollY的时候也会出现超过边界再修正的情况
    }


    private fun notifyScrollChangedListeners() {
        val pos: ScrollPos = when {
            isScrolledToTop -> ScrollPos.TOP
            isScrolledToBottom -> ScrollPos.BOTTOM
            else -> ScrollPos.BODY
        }

        scrollDelegate?.onScrolling(scrollX, scrollY)

        if (nowPos != pos) {
            nowPos = pos
            scrollDelegate?.onScrollPos(nowPos)
        }
    }


    //監聽滑動事件: 最底部, 滑動距離
    interface ObserableScrollDelegate {
        fun onScrollPos(pos: ScrollPos)
        fun onScrolling(scrollX: Int, scrollY: Int)
    }


    //只對滑動到頂部, 身體, 底部坐回傳
    enum class ScrollPos {
        TOP, BODY, BOTTOM
    }
}