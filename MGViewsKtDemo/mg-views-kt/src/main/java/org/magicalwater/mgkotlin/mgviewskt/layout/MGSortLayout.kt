package org.magicalwater.mgkotlin.mgviewskt.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.magicalwater.mgkotlin.mgviewskt.R
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.rightPadding


/**
 * Created by magicalwater on 2017/12/19.
 * 依照自行設定排序的layout
 * 初始化時可以以字串的方式設定陣列, 格式為 順序,順序,順序
 * 之後再以逗號隔開得到相關順序
 */
open class MGSortLayout: ViewGroup {

    //排列設定參數
    private val SORT_NO = 0
    private val SORT_HORIZONTAL = 1
    private val SORT_VERTICAL = 2
    private val SORT_ALL = 3

    var horizontalSpacing: Int = 0
    var verticalSpacing: Int = 0

    //子view的位置垂直增長是否開啟
    var verticalIncrease: Boolean = true

    //子view的位置水平增長是否開啟
    var horizontalIncrease: Boolean = true

    //順位為以下的子view不顯示, 當然也不佔用空間
    private var hideItem: MutableList<Int> = mutableListOf()

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }


    private fun initView(context: Context, attrs: AttributeSet?) {
        val styleArray = context.obtainStyledAttributes(attrs, R.styleable.MGSortLayout)
        if (styleArray != null) {
            horizontalSpacing = styleArray.getDimensionPixelSize(R.styleable.MGSortLayout_sl_base_horizontal_increase, 0)
            verticalSpacing = styleArray.getDimensionPixelSize(R.styleable.MGSortLayout_sl_base_vertical_increase, 0)

            val sort = styleArray.getInt(R.styleable.MGSortLayout_sl_sort_increase, SORT_ALL)
            when (sort) {
                SORT_NO         -> { horizontalIncrease = false; verticalIncrease = false }
                SORT_HORIZONTAL -> { horizontalIncrease = true;  verticalIncrease = false }
                SORT_VERTICAL   -> { horizontalIncrease = false; verticalIncrease = true  }
                SORT_ALL        -> { horizontalIncrease = true;  verticalIncrease = true }
            }

            val hideText = styleArray.getString(R.styleable.MGSortLayout_sl_hide_sort) ?: ""
            syncHideChild(hideText)
        }
    }

    //外部呼叫隱藏哪些 child
    fun setHide(sort: List<Int>) {
        hideItem = sort.toMutableList()
    }

    private fun syncHideChild(text: String) {

        //只有確實有逗號分隔線時才執行切割的動作
        hideItem = text.split(",").map { s ->
            try {
                s.toInt()
            } catch (e: NumberFormatException) {
                -1
            }
        }.toMutableList()
    }

    /**
     * step2 自定義LayoutParams, 該類用於保存每個子view的x，y軸位置
     */
    class LayoutParams: ViewGroup.LayoutParams {
        var x: Int = 0
        var y: Int = 0

        constructor(context: Context, attrs: AttributeSet): super(context, attrs)

        constructor(width: Int, height: Int): super(width, height)

        constructor(source: ViewGroup.LayoutParams, x: Int, y: Int): super(source) {
            this.x = x
            this.y = y
        }

    }


    /**
     * step3 使用自定義 LayoutParams 必須覆寫以下方法
     */
    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }


    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return if (p != null) LayoutParams(p.width, p.height)
               else LayoutParams(0, 0)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        //使用寬和高計算佈局的最終大小以及子view的x, y軸位置
        var width = paddingLeft
        var height = paddingTop

        //儲存子view最大的寬度
        var maxWidth = 0
        var maxHeight = 0

        //獲取每個子view
        val count = childCount
        var isFirstItem = true
        for (i in 0 until count) if (!hideItem.contains(i)) {

            val child = getChildAt(i)

            if (horizontalIncrease && !isFirstItem) width += horizontalSpacing
            if (verticalIncrease && !isFirstItem) height += verticalSpacing

            isFirstItem = false

            //讓子view測量自己
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            //得到子view的 LayoutParams
            val lp = child.layoutParams as LayoutParams

            lp.x = width
            lp.y = height //將寬和高保存到字定義的 LayoutParams中去

            maxWidth = maxOf(child.measuredWidth, maxWidth)
            maxHeight = maxOf(child.measuredHeight, maxHeight)

            if (horizontalIncrease) width += child.measuredWidth
            if (verticalIncrease) height += child.measuredHeight
        }

        if (horizontalIncrease) width += paddingRight else width = maxWidth + leftPadding + rightPadding
        if (verticalIncrease) height += paddingBottom else height = maxHeight + paddingTop + paddingBottom

        //resolveSize的主要作用為根據你提供的大小和MeasureSpec
        //返回你想要的大小值, 這個裡面根據傳入模式的不同作相應的處理
        setMeasuredDimension(View.resolveSize(width, widthMeasureSpec), View.resolveSize(height, heightMeasureSpec))


    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            child.layout(lp.x, lp.y, lp.x + child.measuredWidth, lp.y + child.measuredHeight)
        }
    }
}