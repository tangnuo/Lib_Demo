package com.caowj.lib_widgets.banner.anim

import android.os.Handler
import android.os.Message
import android.view.animation.Animation

/**
 * 控制Banner动画执行过程，时间间隔。
 */
internal class AnimHandler(private val mAutoBanner: com.caowj.lib_widgets.banner.AutoBanner) : Handler() {
    var timeInterval: Int = ANIMATION_INTERVAL

    companion object {
        /**
         * 默认的时间间隔
         */
        const val ANIMATION_INTERVAL = 3000
    }

    override fun handleMessage(msg: Message?) {
        mAutoBanner.mBannerSwitcher.showNext()
        mAutoBanner.mBannerSwitcher.inAnimation.setAnimationListener(object : SimpleAnimationListener() {
            override fun onAnimationStart(animation: Animation) {
                mAutoBanner.mIndicator.focusNext()
            }
        })

        sendEmptyMessageDelayed(0, timeInterval.toLong())
    }

    fun start() {
        sendEmptyMessageDelayed(0, timeInterval.toLong())
    }

    fun stop() {
        removeMessages(0)
    }
}