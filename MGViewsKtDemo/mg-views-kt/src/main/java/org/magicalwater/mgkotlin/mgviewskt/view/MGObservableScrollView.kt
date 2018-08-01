package org.magicalwater.mgkotlin.mgviewskt.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/**
 * Created by magicalwater on 2018/1/4.
 * 可監聽, 1. 滑動距離 2. 滑動到最底部
 */
open class MGObservableScrollView: ScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    //現在是否在最頂端
    private var mIsScrolledToTop: Boolean = true

    //是否在
    private var mIsScrolledToBottom: Boolean = false

    //當滑動距離超出此數值, 則觸發監聽
    private var mDistanceDetect: Int = 0

    //紀錄上次滑動到的y軸為置, 方便監聽超出數值
    private var mLastScrollY: Int = 0

    //當前滑動距離
    private var mNowDistance: Int = 0

    var mNowPos: ScrollPos = ScrollPos.TOP
    var scrollDelegate: ObservableScrollDelegate? = null


    /**
     * @param scrollX - 距離原點的 x 軸距離
     * @param scrollY - 距離原點的 y 軸距離
     * @param clampedX - 滑動到左側邊界時為 true
     * @param clampedY - 滑動道下方邊界時為 true
     * */
    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        mDistanceDetect += scrollY - mLastScrollY
        if (scrollY == 0) {
            mIsScrolledToTop = clampedY
            mIsScrolledToBottom = false
        } else {
            mIsScrolledToTop = false
            mIsScrolledToBottom = clampedY
        }
        notifyScrollChangedListeners()
    }

    /**
     * 注意:
     *     getScrollY()得到的不是絕對正確的, 有可能會超出邊界, 接著會自行逐漸恢復到正確的值，可能會導致判斷失敗
     * @param l - 變化後 x 軸的位置
     * @param t - 變化後 y 軸的位置
     * @param oldl - 原先 x 軸的位置
     * @param oldt - 原先 y 軸的位置
     * */
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (android.os.Build.VERSION.SDK_INT < 9) {  // API 9以上走onOverScrolled
            mNowDistance += t - oldt
            when {
                scrollY == 0 -> {
                    //注意: 这里不能是 scrollY <= 0
                    mIsScrolledToTop = true
                    mIsScrolledToBottom = false
                }
                scrollY + height - paddingTop - paddingBottom == getChildAt(0).height -> {
                    // 注意: 这里不能是 >=
                    // 注意：要加入上下的padding
                    mIsScrolledToBottom = true
                    mIsScrolledToTop = false
                }
                else -> {
                    mIsScrolledToTop = false
                    mIsScrolledToBottom = false
                }
            }
            notifyScrollChangedListeners()
        }
    }


    private fun notifyScrollChangedListeners() {
        val pos: ScrollPos = when {
            mIsScrolledToTop -> ScrollPos.TOP
            mIsScrolledToBottom -> ScrollPos.BOTTOM
            else -> ScrollPos.BODY
        }

        scrollDelegate?.onScrolling(scrollX, scrollY)

        if (mNowPos != pos) {
            mNowPos = pos
            scrollDelegate?.onScrollPos(mNowPos)
        }

        if (Math.abs(mNowDistance) >= mDistanceDetect) {
            scrollDelegate?.onScrollDistanceDetect(
                    if (mNowDistance >= 0) ScrollDirection.DOWN
                    else ScrollDirection.UP
            )
            mNowDistance = 0
        }
    }

    //監聽滑動事件: 最底部, 滑動距離
    interface ObservableScrollDelegate {
        fun onScrollPos(pos: ScrollPos) {}
        fun onScrolling(scrollX: Int, scrollY: Int) {}
        fun onScrollDistanceDetect(direction: ScrollDirection) {}
    }

    //只對滑動到頂部, 身體, 底部坐回傳
    enum class ScrollPos {
        TOP, BODY, BOTTOM
    }

    enum class ScrollDirection {
        UP, DOWN
    }
}