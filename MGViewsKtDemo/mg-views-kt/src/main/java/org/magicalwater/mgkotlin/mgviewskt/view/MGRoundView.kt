package org.magicalwater.mgkotlin.mgviewskt.view

import android.content.Context
import android.util.AttributeSet
import android.content.res.TypedArray
import org.magicalwater.mgkotlin.mgviewskt.R
import android.graphics.*
import org.magicalwater.mgkotlin.mgextensionkt.getColor
import org.magicalwater.mgkotlin.mgextensionkt.getResourceId
import org.magicalwater.mgkotlin.mgutilskt.util.MGImgLoadUtils
import kotlin.properties.Delegates


/**
 * Created by 志朋 on 2017/12/12.
 */
open class MGRoundView: MGBaseView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    override fun styleableView(): IntArray? = R.styleable.MGRoundView

    private val mPaint: Paint = Paint()
    private val mStrokePaint: Paint = Paint()
    private val mOuterPaint: Paint = Paint()
    private val mTextPaint: Paint = Paint()
    private val mContentPath = Path() //用來繪製填充形狀
    private val mStokePath = Path() //用來繪製填充形狀, 之所以另開個 path 是為了 stoke 不要超出邊界
    private val mOuterRegion = Region() //代替background, 與background不同的地方在於中間的content path並不會繪製

    //外框相關
    //外框顏色, 外框寬度
    var mStrokeClr: Int by Delegates.observable(Color.TRANSPARENT) { _, _, _ ->
        mStrokePaint.color = mStrokeClr; invalidate()
    }
    var mStokeWidth: Float by Delegates.observable(5f) { _, _, _ ->
        mStrokePaint.strokeWidth = mStokeWidth; invalidate()
    }
    //內部填滿的顏色
    var mContentClrs: MutableList<Int> = mutableListOf()
        set(value) { field = value; syncGradient(); invalidate() }
    //支持在xml直接設定起始跟結束顏色
    var mStartClr: Int by Delegates.observable(Color.TRANSPARENT) { _, _, _ ->
        initGradientClrs(); syncGradient(); invalidate()
    }
    var mCenterClr: Int? = null
        set(value) { field = value; initGradientClrs(); syncGradient(); invalidate() }
    var mEndClr: Int by Delegates.observable(Color.TRANSPARENT) { _, _, _ ->
        initGradientClrs(); syncGradient(); invalidate()
    }

    var mGlideColor: Int
        get() = mStartClr
        set(value) {
            mStartClr = value
            mEndClr = value
            mCenterClr = value
        }

    //外部的 clr
    var mOuterClr: Int by Delegates.observable(Color.TRANSPARENT) { _, _, _ ->
        initGradientClrs(); syncGradient(); invalidate()
    }

    //三角形的連接點相關, 以字串組成, 以逗號隔開
    var trianglePoint: String by Delegates.observable("1,8,3") { _, _, _ ->
        invalidate()
    }

    //顯示文字一律置中
    //顯示文字
    var mText: String by Delegates.observable("") { _, _, _ ->
        invalidate()
    }

    //文字大小
    var mTextSize: Float by Delegates.observable(12f) { _, _, _ ->
        mTextPaint.textSize = mTextSize; invalidate()
    }

    //文字顏色
    var mTextClr: Int by Delegates.observable(Color.BLACK) { _, _, _ ->
        mTextPaint.color = mTextClr; invalidate()
    }

    //將內容以圖片的方式代替
    var mContentImgId: Int? = null
    set(value) {
        field = value
        when (value) {
            null -> mContentImg = null
            else -> MGImgLoadUtils.load(context, value, null, null, ::setContent)
        }
    }

    //圖片縮放狀態
    var mImgScaleType: Int = TypeImage.ASPECT_FIT

    private var bmpScaleMatrix = Matrix()

    var mContentImg: Bitmap? = null
        set(value) { field = value; calculateMatrixAttrs(); invalidate() }

    fun setContent(bmp: Bitmap) {
        mContentImg = bmp
    }

    //漸變點
    private var mContentPos: MutableList<Float> = mutableListOf()
        set(value) { field = value; invalidate() }

    //漸變元件
    private val mGradient: LinearGradient
        get() {
            val x0: Float
            val y0: Float
            when (angle) {
                0 -> { x0 = 0f; y0 = measuredHeight/2f}
                45 -> { x0 = 0f; y0 = measuredHeight.toFloat() }
                90 -> { x0 = measuredWidth/2f; y0 = measuredHeight.toFloat() }
                135 -> { x0 = measuredWidth.toFloat(); y0 = measuredHeight.toFloat() }
                180 -> { x0 = measuredWidth.toFloat(); y0 = measuredHeight/2f }
                -45 -> { x0 = 0f; y0 = 0f}
                -90 -> { x0 = measuredWidth/2f; y0 = 0f }
                -135 -> { x0 = measuredWidth.toFloat(); y0 = 0f }

                else -> { x0 = 0f; y0 = 0f }
            }
            return LinearGradient(
                    x0,
                    y0,
                    measuredWidth - x0,
                    measuredHeight - y0,
                    mContentClrs.toIntArray(),
                    if (mContentPos.size != mContentClrs.size) null else mContentPos.toFloatArray(),
                    Shader.TileMode.CLAMP
            )
        }

    //圓角半徑
    var radius: Float = 50f
        set(value) { field = value; invalidate() }
    var leftTop: Boolean = true
        set(value) { field = value; invalidate() }
    var rightTop: Boolean = true
        set(value) { field = value; invalidate() }
    var rightBottom: Boolean = true
        set(value) { field = value; invalidate() }
    var leftBottom: Boolean = true
        set(value) { field = value; invalidate() }

    private var radiusArray: FloatArray = FloatArray(8, { 0f })

    //角度
    var angle: Int = 0
        set(value) { field = value; syncGradient(); invalidate()}

    //形狀: 之後加入的功能, 先暫時全先暫時全都長方形
    private var type: Int = Type.TYPE_RECT

    //外形
    class Type {
        companion object {
            val TYPE_RECT = 0   //長方形
            val TYPE_CIRCLE = 1 //圓形
            val TYPE_TRIANGLE = 2 //三角形
        }
    }

    //圖片顯示類型
    class TypeImage {
        companion object {
            val ASPECT_FIT = 0   //依照比例較大邊填滿
            val ASPECT_FILL = 1 //依照比例較小邊填滿
            val SCALE_FILL = 2 //拉伸填滿
        }
    }

    //初始化相關設定
    override fun setupView(style: TypedArray?) {
        if (style != null) {
            mStartClr = style.getColor(R.styleable.MGRoundView_rv_start_clr, mStartClr)
            mCenterClr = style.getColor(R.styleable.MGRoundView_rv_center_clr)
            mEndClr = style.getColor(R.styleable.MGRoundView_rv_end_clr, mEndClr)
            mStokeWidth = style.getDimensionPixelSize(R.styleable.MGRoundView_rv_stroke_width, mStokeWidth.toInt()).toFloat()
            mStrokeClr = style.getColor(R.styleable.MGRoundView_rv_stroke_clr, mStrokeClr)
            angle = style.getInt(R.styleable.MGRoundView_rv_angle, angle)
            type = style.getInt(R.styleable.MGRoundView_rv_type, Type.TYPE_RECT)
            mImgScaleType = style.getInt(R.styleable.MGRoundView_rv_src_type, TypeImage.ASPECT_FIT)
            mContentImgId = style.getResourceId(R.styleable.MGRoundView_rv_src)
            leftTop = style.getBoolean(R.styleable.MGRoundView_rv_corner_top_left, true)
            leftBottom = style.getBoolean(R.styleable.MGRoundView_rv_corner_bottom_left, true)
            rightTop = style.getBoolean(R.styleable.MGRoundView_rv_corner_top_right, true)
            rightBottom = style.getBoolean(R.styleable.MGRoundView_rv_corner_bottom_right, true)
            radius = style.getDimensionPixelSize(R.styleable.MGRoundView_rv_corner_radius, 50).toFloat()
            mOuterClr = style.getColor(R.styleable.MGRoundView_rv_outer_clr, mOuterClr)
            mText = style.getString(R.styleable.MGRoundView_rv_text) ?: mText
            mTextSize = style.getDimension(R.styleable.MGRoundView_rv_text_size, mTextSize)
            mTextClr = style.getColor(R.styleable.MGRoundView_rv_text_clr, mTextClr)
            val glideColor = style.getColor(R.styleable.MGRoundView_rv_glide_clr)
            if (glideColor != null) {
                mGlideColor = glideColor
            }
        }
    }

    init {
        setupView(styleArray)
        initPaint()
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        calculateMatrixAttrs()
        when (type) {
            Type.TYPE_RECT -> syncRectPath()
            Type.TYPE_CIRCLE -> syncCirclePath()
            Type.TYPE_TRIANGLE -> syncTrianglePath()
        }
        syncGradient()
    }

    private fun initPaint() {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL_AND_STROKE

        mStrokePaint.isAntiAlias = true
        mStrokePaint.style = Paint.Style.STROKE

        mOuterPaint.isAntiAlias = true
        mOuterPaint.style = Paint.Style.FILL

        mOuterPaint.color = mOuterClr

        mTextPaint.isAntiAlias = true
        mTextPaint.color = mTextClr
        mTextPaint.textSize = mTextSize
        mTextPaint.textAlign = Paint.Align.CENTER

        initGradientClrs()
        syncGradient()
    }

    //初始化漸變的 顏色 array
    private fun initGradientClrs() {
        mContentClrs.clear()
        mContentClrs.add(mStartClr)
        if (mCenterClr != null) mContentClrs.add(mCenterClr!!)
        mContentClrs.add(mEndClr)
    }

    //根據形狀描述出path
    private fun syncRectPath() {
        mContentPath.reset()
        mStokePath.reset()
        syncRoundArray()
        val rect = RectF(realX,
                realY,
                realX + realWidth,
                realY + realHeight)

        mContentPath.addRoundRect(rect, radiusArray, Path.Direction.CW)

        val strokeRect = RectF(
                realX + mStokeWidth / 2,
                realY + mStokeWidth / 2,
                realX + realWidth - mStokeWidth / 2,
                realY + realHeight - mStokeWidth / 2
        )
        mStokePath.addRoundRect(strokeRect, radiusArray, Path.Direction.CW)

        syncOuterRegion()
    }

    private fun syncCirclePath() {
//        println("類型: 圓形")
        mContentPath.reset()
        mStokePath.reset()
        mContentPath.addCircle(realCenterX, realCenterY, minOf(realWidth,realHeight)/2f, Path.Direction.CW)
        mStokePath.addCircle(realCenterX, realCenterY, minOf(realWidth,realHeight)/2f - mStokeWidth/2f, Path.Direction.CW)

        syncOuterRegion()
    }


    private fun syncTrianglePath() {
        mContentPath.reset()
        mStokePath.reset()

        //將點轉為座標
        fun convertToXY(point: Int): PointF {
            var p: PointF = when (point) {
                1 -> PointF(realX          , realY)
                2 -> PointF(realCenterX     , realY)
                3 -> PointF(realEndX        , realY)
                4 -> PointF(realX          , realCenterY)
                5 -> PointF(realCenterX     , realCenterY)
                6 -> PointF(realEndX        , realCenterY)
                7 -> PointF(realX          , realEndY)
                8 -> PointF(realCenterX     , realEndY)
                9 -> PointF(realEndX        , realEndY)
                else -> PointF(realX        , realY)
            }
            return p
        }
        //解析三角形的點字串, 並轉為點
        val points = trianglePoint.split(",").map { i -> i.toInt() }
        val firstP = convertToXY(points[0])
        mContentPath.moveTo(firstP.x, firstP.y)
        (1 until points.size)
                .map { convertToXY(points[it]) }
                .forEach { mContentPath.lineTo(it.x, it.y) }
        mStokePath.addPath(mContentPath)

        syncOuterRegion()
    }


    //同步外部的region繪製範圍
    private fun syncOuterRegion() {
        //假如外部繪製顏色是透明, 就不用去繪製
        if (isOuterTransparent()) return

        val outerRect = Rect(
                realX.toInt(),
                realY.toInt(),
                realX.toInt() + realWidth.toInt(),
                realY.toInt() + realHeight.toInt()
        )
        val clipRegion = Region()
        clipRegion.setPath(mContentPath, Region(outerRect))
        mOuterRegion.set(outerRect)
        mOuterRegion.op(clipRegion, Region.Op.DIFFERENCE)
    }

    //同步圓角array
    private fun syncRoundArray() {
        radiusArray[0] = if (leftTop) radius else 0f
        radiusArray[1] = if (leftTop) radius else 0f
        radiusArray[2] = if (rightTop) radius else 0f
        radiusArray[3] = if (rightTop) radius else 0f
        radiusArray[4] = if (rightBottom) radius else 0f
        radiusArray[5] = if (rightBottom) radius else 0f
        radiusArray[6] = if (leftBottom) radius else 0f
        radiusArray[7] = if (leftBottom) radius else 0f
    }


    private fun syncGradient() {
        //先檢查 content color 是否為透明, 如果皆為透明不用設定, 因為也不會繪製
        if (!isContentTransparent()) mPaint.shader = mGradient
    }

    //檢查內容是否為透明
    private fun isContentTransparent(): Boolean = mContentClrs.any { it == Color.TRANSPARENT }

    //檢查外框是否為透明
    private fun isStrokeTransparent(): Boolean = mStrokeClr == Color.TRANSPARENT

    //檢查外部繪製區域是否為透明
    private fun isOuterTransparent(): Boolean = mOuterClr == Color.TRANSPARENT

    //檢查文字是否為空, 顏色是否為透明
    private fun isTextNeedDraw(): Boolean = mTextClr != Color.TRANSPARENT && !mText.isBlank()

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        if (canvas == null) return

        if (mContentImg != null) {
            canvas.save()
            canvas.clipPath(mContentPath)
            canvas.drawBitmap(mContentImg, bmpScaleMatrix, mPaint)
            canvas.restore()
        } else if (!isContentTransparent()){
            canvas.drawPath(mContentPath, mPaint)
        }

        /**
         * 這邊畫邊邊時, 當app縮小後再開啟會出現整個變空白的情況
         * 此bug之後修, 暫時不會用到
         */
        if (!isStrokeTransparent()) canvas.drawPath(mStokePath, mStrokePaint)


        //若外部顏色為透明, 則不需繪製
        if (!isOuterTransparent()) drawRegion(canvas, mOuterRegion, mOuterPaint)


        //最後開始畫字
        if (isTextNeedDraw()) {
            val point = getTextDrawCenterPoint(canvas)
            canvas.drawText(mText, point.first, point.second, mTextPaint)
        }
    }

    //繪製外部區域
    private fun drawRegion(canvas: Canvas, rgn: Region, paint: Paint) {
        val iter = RegionIterator(rgn)
        val r = Rect()

        while (iter.next(r)) {
            canvas.drawRect(r, paint)
        }
    }


    //得到繪製字體中間的點
    private fun getTextDrawCenterPoint(canvas: Canvas): Pair<Float,Float> {
        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2f - (mTextPaint.descent() + mTextPaint.ascent()) / 2f)
        return Pair(xPos,yPos)
    }


    //使用 matrix 放大縮小圖片到合適的範圍
    private fun calculateMatrixAttrs() {

        val bW = mContentImg?.width ?: 100
        val bH = mContentImg?.height ?: 100

        var sw = (realWidth) / bW.toFloat()
        var sh = (realHeight) / bH.toFloat()

        //依照類型不同縮放
        when (mImgScaleType) {
            TypeImage.ASPECT_FIT -> {
                sw = Math.min(sw, sh)
                sh = sw
            }
            TypeImage.ASPECT_FILL -> {
                sw = Math.max(sw, sh)
                sh = sw
            }
            TypeImage.SCALE_FILL -> {}
        }

        val afterScaleWidth = bW * sw
        val afterScaleHeight = bH * sh

        val translateX = realCenterX - afterScaleWidth / 2f
        val translateY = realCenterY - afterScaleHeight / 2f

        bmpScaleMatrix.setScale(sw, sh)
        bmpScaleMatrix.postTranslate(translateX, translateY)
    }
}


