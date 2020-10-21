package com.caowj.lib_widgets.banner.anim

import android.view.animation.Animation

/**
 * 简化的AnimationListener根据需要进行方法的override。
 */
internal abstract class SimpleAnimationListener : Animation.AnimationListener {
    override fun onAnimationRepeat(animation: Animation) {
    }

    override fun onAnimationEnd(animation: Animation) {
    }

    override fun onAnimationStart(animation: Animation) {
    }
}