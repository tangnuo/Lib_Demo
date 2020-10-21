package com.caowj.lib_widgets.banner

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ViewAnimator
import com.caowj.lib_widgets.R

/**
 * 轮播广告横幅组件的自动播放部分。
 *
 * 在可以在其中添加自定义的组件
 */
internal class BannerSwitcher : ViewAnimator {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : super(context, attributes) {
        Log.i("AutoBanner", "BannerSwitcher constructor method")
        // set default animation if anim attributes not set
        inAnimation = inAnimation ?: AnimationUtils.loadAnimation(context, R.anim.slide_fade_in)
        outAnimation = outAnimation ?: AnimationUtils.loadAnimation(context, R.anim.slide_fade_out)
        animateFirstView = false
    }

    /**
     * 一次添加多个子view。会在原有的子view后边追加新的子View。
     *
     * @param children 可变参数，可以传入需要添加的View
     */
    fun addViews(children: Array<out View>) {
        val count = childCount
        children.forEachIndexed { index, view ->
            addView(view, count + index, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        }
    }

}