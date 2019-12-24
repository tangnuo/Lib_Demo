package com.caowj.lib_widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * <pre>
 *     作者：Caowj
 *     邮箱：caoweijian@kedacom.com
 *     日期：2019/12/13 13:44
 * </pre>
 */

public class CircleImage extends AppCompatImageView {

    private int width;
    private int height;

    private int defaultRadius = 0;

    private int radius;

    private int leftTopRadius;

    private int rightTopRadius;

    private int rightBottomRadius;

    private int leftBottomRadius;

    public CircleImage(Context context) {
        this(context, null);
    }

    public CircleImage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView);
        radius = array.getDimensionPixelOffset(R.styleable.CircleImageView_radius, defaultRadius);
        leftTopRadius = array.getDimensionPixelOffset(R.styleable.CircleImageView_left_top_radius, defaultRadius);
        rightTopRadius = array.getDimensionPixelOffset(R.styleable.CircleImageView_right_top_radius, defaultRadius);
        rightBottomRadius = array.getDimensionPixelOffset(R.styleable.CircleImageView_right_bottom_radius, defaultRadius);
        leftBottomRadius = array.getDimensionPixelOffset(R.styleable.CircleImageView_left_bottom_radius, defaultRadius);

        if (leftTopRadius == defaultRadius) {
            leftTopRadius = radius;
        }
        if (rightTopRadius == defaultRadius) {
            rightTopRadius = radius;
        }
        if (rightBottomRadius == defaultRadius) {
            rightBottomRadius = radius;
        }
        if (leftBottomRadius == defaultRadius) {
            leftBottomRadius = radius;
        }
        array.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int maxLeft = Math.max(leftTopRadius, leftBottomRadius);
        int maxRight = Math.max(rightTopRadius, rightBottomRadius);
        int minWidth = maxLeft + maxRight;
        int maxTop = Math.max(leftTopRadius, rightTopRadius);
        int maxBottom = Math.max(leftBottomRadius, rightBottomRadius);
        int minHeight = maxTop + maxBottom;
        if (width >= minWidth && height > minHeight) {
            Path path = new Path();
            path.moveTo(leftTopRadius, 0);
            path.lineTo(width - rightTopRadius, 0);
            path.quadTo(width, 0, width, rightTopRadius);

            path.lineTo(width, height - rightBottomRadius);
            path.quadTo(width, height, width - rightBottomRadius, height);

            path.lineTo(leftBottomRadius, height);
            path.quadTo(0, height, 0, height - leftBottomRadius);

            path.lineTo(0, leftTopRadius);
            path.quadTo(0, 0, leftTopRadius, 0);

            canvas.clipPath(path);
        }

        super.onDraw(canvas);
    }
}
