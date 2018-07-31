package org.magicalwater.mgkotlin.mgviewskt.view

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Size
import android.widget.ImageView
import org.magicalwater.mgkotlin.mgviewskt.R
import org.magicalwater.mgkotlin.mgextensionkt.getDimensionPixelSize
import org.magicalwater.mgkotlin.mgextensionkt.getInteger
import org.magicalwater.mgkotlin.mgutilskt.util.MGProportionUtils

/**
 * Created by magicalwater on 2018/1/24.
 * 依照比例設置大小的ImageView
 */
open class MGProportionImageView: AppCompatImageView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initStyleArray(context, attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initStyleArray(context, attrs)
    }

    //寬高比例
    private var widthProportion: Int? = null
    private var heightProportion: Int? = null

    //當measure的寬高沒有任何限制時, 最大的寬/高度, null代表隨著圖片, 若是連圖片也沒有則採用defaultSize
    private var maxWidth: Double? = null
    private var maxHeight: Double? = null

    //這個預設值是以寬度為主
    private var defaultSize: Double = 100.0

    private fun initStyleArray(context: Context?, attrs: AttributeSet?) {
        val style = context?.obtainStyledAttributes(attrs, R.styleable.MGProportionImageView)
        if (style != null) {
            widthProportion = style.getInteger(R.styleable.MGProportionImageView_piv_width_proport)
            heightProportion = style.getInteger(R.styleable.MGProportionImageView_piv_height_proport)
            maxWidth = style.getDimensionPixelSize(R.styleable.MGProportionImageView_piv_width_max)?.toDouble()
            maxHeight = style.getDimensionPixelSize(R.styleable.MGProportionImageView_piv_height_max)?.toDouble()
            defaultSize = style.getDimensionPixelSize(R.styleable.MGProportionImageView_piv_height_max, defaultSize.toInt()).toDouble()
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        //假如有設定寬高比, 就接管外誆設置
        if (widthProportion != null && heightProportion != null) {
            val pairSize = measureProportion(widthMeasureSpec, heightMeasureSpec)
            if (pairSize != null) setMeasuredDimension(pairSize.first,pairSize.second)
            else super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

    }


    //計算整個view的大小
    private fun measureProportion(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int,Int>? {
        val widthProportion = widthProportion
        val heightProportion = heightProportion
        if (widthProportion == null || heightProportion == null) return null

        //如果寬跟高都有限制, 那麼以寬為主
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)//得到模式
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)//得到大小

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)//得到模式
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)//得到大小

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            //當兩個模式都有精確的數字時, 直接設定長寬, 比例失效
            setMeasuredDimension(widthSize, heightSize)
            return Pair(widthSize,heightSize)

        } else if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //寬度有精確(最大)數值, 以寬度為主
            //先取得應有的高度
            var w = widthSize.toDouble()
            var h = MGProportionUtils.getHeight(
                    widthProportion.toDouble(), heightProportion.toDouble(), w
            )

            //如果應有的高度超過最大高度, 那麼便以最大高度為主
            if (heightMode == MeasureSpec.AT_MOST && h > heightSize) {
                h = heightSize.toDouble()
                w = MGProportionUtils.getWidth(
                        widthProportion.toDouble(), heightProportion.toDouble(), h
                )
            }
            return Pair(w.toInt(),h.toInt())

        } else if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //高度有精確(最大)數值, 以高度為主
            //先取得應有的高度
            var h = heightSize.toDouble()
            var w = MGProportionUtils.getWidth(
                    widthProportion.toDouble(), heightProportion.toDouble(), h
            )

            //如果應有的寬度超過最大寬度, 那麼便以最大寬度為主
            if (widthMode == MeasureSpec.AT_MOST && w > widthSize) {
                w = widthSize.toDouble()
                h = MGProportionUtils.getHeight(
                        widthProportion.toDouble(), heightProportion.toDouble(), w
                )
            }
            return Pair(w.toInt(),h.toInt())

        } else {
            //當measure的寬高沒有任何限制時, 最大的寬/高度, null代表隨著原本的size
            val maxWidth = maxWidth
            val maxHeight = maxHeight
            if (maxWidth != null && maxHeight != null) {
                var w = maxWidth
                var h = MGProportionUtils.getHeight(
                        widthProportion.toDouble(), heightProportion.toDouble(), w
                )

                //如果高度超過最大高度, 那麼便以最大高度為主
                if (h > maxHeight) {
                    h = maxHeight
                    w = MGProportionUtils.getWidth(
                            widthProportion.toDouble(), heightProportion.toDouble(), h
                    )
                }
                return Pair(w.toInt(),h.toInt())

            } else {
                return null
            }
        }
    }
}