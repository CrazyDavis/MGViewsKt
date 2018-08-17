package org.magicalwater.mgkotlin.mgviewskt.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import org.magicalwater.mgkotlin.mgviewskt.R
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import org.magicalwater.mgkotlin.mgextensionkt.getColor
import org.magicalwater.mgkotlin.mgextensionkt.getResourceId
import org.magicalwater.mgkotlin.mgutilskt.util.MGImgLoadUtils
import kotlin.properties.Delegates


/**
 * Created by 志朋 on 2017/12/13.
 */
open class MGMultiIconView : MGBaseView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    override fun styleableView(): IntArray? = R.styleable.MGMultiIconView

    //二隻畫筆, 一隻換一般圖片, 一隻畫高亮圖
    private val mHltPaint: Paint = Paint()
    private val mNorPaint: Paint = Paint()

    //顯示圖片
    var mImgIdHlt: Int? = null
        set(value) {
            field = value
            when (value) {
                null -> mHltImg = null
                else -> MGImgLoadUtils.loadBitmap(context, value, handler = ::setHltBmp)
            }
        }
    var mImgIdNor: Int? = null
        set(value) {
            field = value
            when (value) {
                null -> mNorImg = null
                else -> MGImgLoadUtils.loadBitmap(context, value, handler = ::setNorBmp)
            }
        }

    private var bmpScaleMatrix = Matrix()
    private val bmpNorClrMatrix = ColorMatrix()
    private val bmpHltClrMatrix = ColorMatrix()

    var mHltImg: Bitmap? = null
        set(value) { field = value; calculateMatrixAttrs(); syncNorClrMatrix(); invalidate() }
    var mNorImg: Bitmap? = null
        set(value) { field = value; calculateMatrixAttrs(); syncNorClrMatrix(); invalidate() }

    var mNorClr: Int? = null
        set(value) { field = value; syncNorClrMatrix(); invalidate() }
    var mHltClr: Int? = null
        set(value) { field = value; syncHltClrMatrix(); invalidate() }

    var highlight: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) invalidate()
    }

    override fun setupView(style: TypedArray?) {
        if (style != null) {
            mImgIdNor = style.getResourceId(R.styleable.MGMultiIconView_miv_img_nor)
            mImgIdHlt = style.getResourceId(R.styleable.MGMultiIconView_miv_img_hlt)
            mHltClr = style.getColor(R.styleable.MGMultiIconView_miv_clr_hlt)
            mNorClr = style.getColor(R.styleable.MGMultiIconView_miv_clr_nor)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        //需要重新繪製
        if (mNorImg != null || mHltImg != null) {
            calculateMatrixAttrs()
        }
    }

    init {
        setupView(styleArray)
        initPaint()
        syncNorClrMatrix()
        syncHltClrMatrix()
    }

    private fun initPaint() {
        mNorPaint.isAntiAlias = true
        mHltPaint.isAntiAlias = true
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

//        println("離上方: $topPadding, 離下方: $bottomPadding, 上方偏移: $topPaddingOffset, 下方偏移: $bottomPaddingOffset")
//        val paint = Paint()
//        paint.color = Color.RED
//        var rt = Rect(mRealX.toInt(), mRealY.toInt(), mRealX.toInt() + mRealWidth.toInt(), mRealY.toInt() + mRealHeight.toInt())
//        canvas?.drawRect(rt, paint)
        if (mNorImg != null && !highlight) {
            canvas?.drawBitmap(mNorImg, bmpScaleMatrix, mNorPaint)
        }

        if (mHltImg != null && highlight) {
            canvas?.drawBitmap(mHltImg, bmpScaleMatrix, mHltPaint)
        }
    }

    //設置一般圖
    fun setNorBmp(bmp: Bitmap) { mNorImg = bmp }

    //設置高亮圖
    fun setHltBmp(bmp: Bitmap) { mHltImg = bmp }


    //同步圖像顯示單色問題
    private fun syncNorClrMatrix() {
        if (mNorClr != null) {
            val src = floatArrayOf(
                    0f, 0f, 0f, 0f, Color.red(mNorClr!!).toFloat(),
                    0f, 0f, 0f, 0f, Color.green(mNorClr!!).toFloat(),
                    0f, 0f, 0f, 0f, Color.blue(mNorClr!!).toFloat(),
                    0f, 0f, 0f, 1f, 0f)
            bmpNorClrMatrix.set(src)
            mNorPaint.colorFilter = ColorMatrixColorFilter(bmpNorClrMatrix)
        } else {
            bmpNorClrMatrix.reset()
            mNorPaint.colorFilter = null
            return
        }
    }

    private fun syncHltClrMatrix() {
        if (mHltClr != null) {
            val src = floatArrayOf(
                    0f, 0f, 0f, 0f, Color.red(mHltClr!!).toFloat(),
                    0f, 0f, 0f, 0f, Color.green(mHltClr!!).toFloat(),
                    0f, 0f, 0f, 0f, Color.blue(mHltClr!!).toFloat(),
                    0f, 0f, 0f, 1f, 0f)
            bmpHltClrMatrix.set(src)
            mHltPaint.colorFilter = ColorMatrixColorFilter(bmpHltClrMatrix)
        } else {
            bmpHltClrMatrix.reset()
            mHltPaint.colorFilter = null
            return
        }
    }

    //使用 matrix 放大縮小圖片到合適的範圍
    private fun calculateMatrixAttrs() {

        //代表現在是不可見的狀態, 不繪製也不計算
        if (width == 0 && height == 0) {
            return
        }

        val bW = mNorImg?.width ?: 10000
        val bH = mNorImg?.height ?: 10000

        val sw = (realWidth) / bW.toFloat()
        val sh = (realHeight) / bH.toFloat()

//        if (sw < 0) {
//            println("資料有錯, 印出所有屬性 $width,$height,$left,$right,$leftPadding,$rightPadding")
//        }

        val sl = Math.min(sw, sh)

        val afterScaleWidth = bW.toFloat() * sl
        val afterScaleHeight = bH.toFloat() * sl

        val translateX = realCenterX - afterScaleWidth / 2f
        val translateY = realCenterY - afterScaleHeight / 2f

        bmpScaleMatrix.reset()
        bmpScaleMatrix.setScale(sl, sl)
        bmpScaleMatrix.postTranslate(translateX, translateY)
    }
}