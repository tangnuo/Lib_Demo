package com.caowj.lib_widgets.textView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.caowj.lib_utils.AndroidUtils;
import com.caowj.lib_widgets.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 倒计时控件
 *
 * @author huhao on 2019/12/19
 */
public class TimeDownTextView extends AppCompatTextView {
    private int leftSecond;   // 剩余秒数
    private int width;
    private int height;
    private int borderWidth;
    private Paint bgPaint;
    private Paint borderPaint;

    public TimeDownTextView(Context context) {
        this(context, null);
    }

    public TimeDownTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeDownTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    private void init(Context context) {
        borderWidth = AndroidUtils.dp2px(context, 1.5f);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(ContextCompat.getColor(context, R.color.bg_light_blue));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(ContextCompat.getColor(context, R.color.bg_light_blue2));

        leftSecond = 3;
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                leftSecond--;
                if (onTimeUpListener != null) {
                    onTimeUpListener.onTimeDown(leftSecond);
                }
                if (leftSecond == 0) {
                    timer.cancel();
                    if (onTimeUpListener != null) {
                        onTimeUpListener.onTimeUp();
                    }
                }
            }
        }, 1000, 1000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(width / 2, height / 2, width / 2, borderPaint);
        canvas.drawCircle(width / 2, height / 2, width / 2 - borderWidth, bgPaint);
        super.onDraw(canvas);
    }

    private OnTimeUpListener onTimeUpListener;

    public void setOnTimeUpListener(OnTimeUpListener onTimeUpListener) {
        this.onTimeUpListener = onTimeUpListener;
    }

    public interface OnTimeUpListener {
        void onTimeDown(int leftSecond);

        void onTimeUp();
    }
}
