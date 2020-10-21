package com.caowj.lib_widgets.banner

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.support.v7.widget.LinearLayoutCompat
import android.util.AttributeSet
import android.view.View
import com.caowj.lib_widgets.R
import com.caowj.lib_utils.SystemUtil

/**
 * 自动滚动提示器View。
 */
internal class Indicator @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    companion object {
        @JvmStatic
        val COLOR_INDICATOR = Color.parseColor("#E8E8E8")

        @JvmStatic
        val COLOR_INDICATOR_FOCUSED = Color.parseColor("#1E90FF")
    }

    init {
        orientation = HORIZONTAL
        val dividerResId = R.drawable.indicator_shape_divider
        @Suppress("DEPRECATION")
        dividerDrawable = if (SDK_INT >= 21)
            context.getDrawable(dividerResId)
        else
            context.resources.getDrawable(dividerResId)
        showDividers = SHOW_DIVIDER_MIDDLE
    }

    /**
     * 当前显示的dot索引
     */
    private var mWhichChild = 0

    /**
     * 非当前圆点背景
     */
    var indicatorColor = COLOR_INDICATOR

    /**
     * 当前banner对应圆点背景
     */
    var indicatorFocusedColor = COLOR_INDICATOR_FOCUSED

    /**
     * 非当前圆点背景
     */
    private val mIndicatorDotDrawable: Drawable by lazy {
        newIndicatorDotDrawable(false)
    }

    /**
     * 当前圆点
     */
    private val mIndicatorDotFocusedDrawable: Drawable by lazy {
        newIndicatorDotDrawable(true)
    }

    /**
     * 创建indicator的圆点。
     *
     * @return 圆点View
     */
    private fun createIndicatorDot(): View {
        val viewDivider = View(context)
        if (SDK_INT >= Build.VERSION_CODES.M) {
            viewDivider.foreground = mIndicatorDotDrawable
        } else {
            viewDivider.background = mIndicatorDotDrawable
        }
        viewDivider.layoutParams = LayoutParams(SystemUtil.dp2px(5f), SystemUtil.dp2px(5.0f))
        return viewDivider
    }

    /**
     * 创建indicator圆点指示器
     *
     * @param number banner的个数
     */
    fun addDots(number: Int) {
        for (i in 0 until number) {
            addView(createIndicatorDot())
        }

        setDisplayedChild(mWhichChild)
    }

    /**
     * 添加单个View，需要相应添加dot，由于添加位置可能添加当前的banner中的view前
     * 或者后，因此需要根据对应的index调整当前focused dot位置。
     *
     * @param index BannerSwitcher中添加的view的索引
     */
    fun addDot(index: Int) {
        addView(createIndicatorDot())
        val focusedIndex = when {
            index < 0 -> 0
            index >= childCount -> childCount - 1
            else -> index
        }
        setDisplayedChild(focusedIndex)
    }

    /**
     * 创建indicator背景Drawable
     *
     * @param focused 是否当前选中焦点
     *
     * @return indicator指示器的背景
     */
    private fun newIndicatorDotDrawable(focused: Boolean): ShapeDrawable {
        val size = SystemUtil.dp2px(5f)
        val shapeCircle = OvalShape()
        shapeCircle.resize(size * 1.0f, size * 1.0f)
        val shapeDrawable = ShapeDrawable(shapeCircle)
        shapeDrawable.intrinsicHeight = size
        shapeDrawable.intrinsicWidth = size
        shapeDrawable.paint.isAntiAlias = true
        shapeDrawable.paint.color = if (focused) indicatorFocusedColor else indicatorColor
        return shapeDrawable
    }

    /**
     * 显示当前的圆点
     */
    private fun setDisplayedChild(whichChild: Int) {
        // 移除原先的focused圆点
        val viewDot = getChildAt(mWhichChild)
        if (SDK_INT >= Build.VERSION_CODES.M) {
            viewDot.foreground = mIndicatorDotDrawable
        } else {
            viewDot.background = mIndicatorDotDrawable
        }

        // 计算下一个focused圆点
        mWhichChild = when {
            whichChild >= childCount -> 0
            whichChild < 0 -> childCount - 1
            else -> whichChild
        }

        // 将focused圆点进行颜色绘制
        val currViewDot = getChildAt(mWhichChild)
        if (SDK_INT >= Build.VERSION_CODES.M) {
            currViewDot.foreground = mIndicatorDotFocusedDrawable
        } else {
            currViewDot.background = mIndicatorDotFocusedDrawable
        }
    }

    /**
     * 显示下一个焦点圆点
     */
    fun focusNext() {
        setDisplayedChild(mWhichChild + 1)
    }

    /**
     * 显示前一个Dot
     */
    fun focusPrevious() {
        setDisplayedChild(mWhichChild - 1)
    }

    override fun removeAllViews() {
        super.removeAllViews()
        mWhichChild = 0
    }

    override fun removeView(view: View) {
        val index = indexOfChild(view)
        if (index >= 0) {
            removeViewAt(index)
        }
    }

    override fun removeViewAt(index: Int) {
        super.removeViewAt(index)
        val childCount = childCount
        when {
            childCount == 0 -> {
                mWhichChild = 0
            }
            mWhichChild >= childCount -> {
                // Displayed is above child count, so float down to top of stack
                setDisplayedChild(childCount - 1)
            }
            mWhichChild == index -> {
                // Displayed was removed, so show the new child living in its place
                setDisplayedChild(mWhichChild)
            }
        }
    }

    override fun removeViewInLayout(view: View) {
        removeView(view)
    }

    override fun removeViews(start: Int, count: Int) {
        super.removeViews(start, count)
        if (childCount == 0) {
            mWhichChild = 0
        } else if (mWhichChild >= start && mWhichChild < start + count) {
            // Try showing new displayed child, wrapping if needed
            setDisplayedChild(mWhichChild)
        }
    }

    override fun removeViewsInLayout(start: Int, count: Int) {
        removeViews(start, count)
    }

}