package org.magicalwater.mgkotlin.mgviewskt.view

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by magicalwater on 2018/2/1.
 * 可自定義速度的TextView
 * 參考: https://www.jianshu.com/p/bb458f3cf783
 *
 * 要點: 不能設置 android:marqueeRepeatLimit="marquee_forever" 屬性
 * 否則一切自定義速度跑馬燈失效
 * */
open class MGMarqueeTextView: AppCompatTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private var currentScrollX = 0      // 當前x軸滾動位置
    private var firstScrollX = 0        // 初始位置
    private var isStop = false          // 開始停止的標記
    private var textWidth = 0           // 文本寬度
    private var mWidth = 0              // 元件寬度
    private var speed = 2               // 默認是兩個點
    private var delayed: Long = 1000    // 默認1秒
    private var endX = 0                // 滾動到哪個位置
    private var isFirstDraw = true      // 當首次或文本改變時重置
    private var SCROLL_DELAYED: Long = 4 * 1000


    /***讓外部呼叫回調***/
    //滾動速度
//    fun setSpeed(speed: Int) {
//        this.speed = speed
//    }
//
//    //滾動時間間隔
//    fun setDelayed(delayed: Long) {
//        this.delayed = delayed
//    }
//
//    //開始滾動
//    fun startScroll() {
//        isStop = false
//        this.removeCallbacks(this)  // 清空隊列
//        postDelayed(this, SCROLL_DELAYED)  // 4秒之後滾動到指定位置
//    }
//
//    //停止滾動
//    fun stopScroll() {
//        isStop = true
//    }
//
//    //從頭開始滑動
//    fun startFor() {
//        currentScrollX = 0  // 將當前位置設置為0
//        startScroll()
//    }
    /***讓外部呼叫回調***/
    
    
    
    //一律回傳 true, 讓即使現在沒有取得focused也能滾動
    override fun isFocused(): Boolean {
        return true
    }


    //實現runnable接口, 不斷執行滾動的操作
//    override fun run() {
//        currentScrollX += speed  // 滾動每次加幾個點
//        scrollTo(currentScrollX, 0) // 滾動到指定位置
//        if (isStop) { return }
//        if (currentScrollX >= endX) {   // 如果滾動的位置大於最大限度, 則滾動到初始位置
//            scrollTo(firstScrollX, 0)
//            currentScrollX = firstScrollX // 初始化滾動速度
//            postDelayed(this, SCROLL_DELAYED)  // SCROLL_DELAYED毫秒之後重新滾動
//        } else {
//            postDelayed(this, delayed)  // delayed毫秒之後在滾動到指定至為
//        }
//    }
//
//
//    //當文本改變時, 滾動屬性和參數需要初始化
//    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
//        super.onTextChanged(text, start, lengthBefore, lengthAfter)
//        isStop = true // 停止滾動
//        this.removeCallbacks(this)   // 清空隊列
//        currentScrollX = firstScrollX  // 滾動到初始位置
//        this.scrollTo(currentScrollX, 0)
//        super.onTextChanged(text, start, lengthBefore, lengthAfter)
//
//        // 需要重新設置參數
//        isFirstDraw = true
//        isStop = false
//        postDelayed(this, SCROLL_DELAYED)
//    }
//
//
//    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
//        if (isFirstDraw) {
//            textWidth = getTextWidth()
//            firstScrollX = scrollX // 獲取第一次滑動的x軸距離
//            currentScrollX = firstScrollX
//            mWidth = this.width  // 獲取文本寬度, 如果文本寬度大於螢幕寬度, 則為螢幕寬度, 否則為文本寬度
//            endX = firstScrollX + textWidth - mWidth/2  // 滾動的最大距離, 可根據需求來定
//            isFirstDraw = false
//        }
//    }
//
//
//    private fun getTextWidth(): Int {
//        var iRet = 0
//        if (text != null && text.length > 0) {
//            val len = text.length
//            val widths = FloatArray(len)
//            paint.getTextWidths(text as String?, widths)
//            for (j in 0 until len) {
//                iRet += Math.ceil(widths[j].toDouble()).toInt()
//            }
//        }
//        return iRet
//    }
}