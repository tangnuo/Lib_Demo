package com.caowj.lib_widgets.banner

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.AnimRes
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.caowj.lib_widgets.R
import com.caowj.lib_widgets.banner.Indicator.Companion.COLOR_INDICATOR
import com.caowj.lib_widgets.banner.Indicator.Companion.COLOR_INDICATOR_FOCUSED
import com.caowj.lib_widgets.banner.anim.AnimHandler

/**
 * 轮播Banner组件。
 */
class AutoBanner : FrameLayout {

    internal val mBannerSwitcher: com.caowj.lib_widgets.banner.BannerSwitcher by lazy { com.caowj.lib_widgets.banner.BannerSwitcher(context) }
    internal val mIndicator: com.caowj.lib_widgets.banner.Indicator by lazy { com.caowj.lib_widgets.banner.Indicator(context) }
    private val mAnimHandler: AnimHandler by lazy { AnimHandler(this) }

    var timeInterval: Int = AnimHandler.ANIMATION_INTERVAL
        set(value) {
            field = value
            mAnimHandler.timeInterval = value
        }
    var indicatorColor: Int = COLOR_INDICATOR
        set(value) {
            field = value
            mIndicator.indicatorColor = value
        }
    var indicatorFocusedColor: Int = COLOR_INDICATOR_FOCUSED
        set(value) {
            field = value
            mIndicator.indicatorFocusedColor = value
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoBanner)
        val inAnimResId = ta.getResourceId(R.styleable.AutoBanner_inAnim, 0)
        if (inAnimResId > 0) {
            mBannerSwitcher.setInAnimation(context, inAnimResId)
        }

        val outAnimResId = ta.getResourceId(R.styleable.AutoBanner_outAnim, 0)
        if (outAnimResId > 0) {
            mBannerSwitcher.setOutAnimation(context, outAnimResId)
        }

        timeInterval = ta.getInt(R.styleable.AutoBanner_time_interval, AnimHandler.ANIMATION_INTERVAL)
        indicatorColor = ta.getColor(R.styleable.AutoBanner_indicator_color, COLOR_INDICATOR)
        indicatorFocusedColor = ta.getColor(R.styleable.AutoBanner_indicator_focus_color, COLOR_INDICATOR_FOCUSED)

        ta.recycle()

        addView(mBannerSwitcher)  // 添加自动滚动各部分

        val indicatorLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        indicatorLayoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        indicatorLayoutParams.bottomMargin = 10
        addView(mIndicator, indicatorLayoutParams)
    }

    /**
     * 添加多个子View到Banner组件中。
     *
     * @param children 用于切换的子组件
     */
    fun addBannerViews(vararg children: View) {
        mBannerSwitcher.addViews(children)
        mIndicator.addDots(children.size)
    }

    /**
     * 单个添加子组件。
     *
     * @param child 用于切换的子view
     */
    fun addBannerView(child: View) {
        mBannerSwitcher.addView(child)
        mIndicator.addDots(1)
    }

    /**
     * 在index位置添加单个子组件。
     *
     * @param child 轮播的组件
     * @param index 位置索引
     */
    fun addBannerView(child: View, index: Int) {
        mBannerSwitcher.addView(child, index)
        mIndicator.addDot(index)
    }

    /**
     * 开始动画播放
     */
    fun start() {
        mAnimHandler.start()
    }

    /**
     * 结束动画播放
     */
    fun stop() {
        mAnimHandler.stop()
    }

    /**
     * 设置组件进入动画
     *
     * @param context 上下文对象
     * @param  resourceID 进入动画资源id
     */
    fun setInAnimation(context: Context, @AnimRes resourceID: Int) {
        mBannerSwitcher.setInAnimation(context, resourceID)
    }

    /**
     * 设置组件消失动画
     *
     * @param context 上下文对象
     * @param  resourceID 消失动画资源id
     */
    fun setOutAnimation(context: Context, @AnimRes resourceID: Int) {
        mBannerSwitcher.setOutAnimation(context, resourceID)
    }
}