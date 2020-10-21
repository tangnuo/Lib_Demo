package com.caowj.lib_logs.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;


import com.caowj.lib_logs.R;
import com.caowj.lib_logs.helper.LogRecord;
import com.caowj.lib_logs.helper.LogTypeEnum;
import com.caowj.lib_logs.helper.LogcatRecord;
import com.caowj.lib_logs.helper.OnPrintLogListener;
import com.caowj.lib_logs.ui.adapter.LogWatchAdapter;
import com.caowj.lib_utils.SystemUtil;


public class LogWatchWindow {

    Context mContext;
    View mFloatView;
    int mCurrentX;
    int mCurrentY;
    WindowManager mWindowManager;
    WindowManager.LayoutParams mLayoutParams;
    private static int mFloatViewWidth = 50;
    private static int mFloatViewHeight = 500;
    LogWatchAdapter mLogWatchAdapter;
    RecyclerView mRecyclerView;
    boolean mIsWatching = true;
    LogTypeEnum mWatchType = null;

    String[] logTypes = new String[]{"Logcat日志","LegoLog全部日志","LegoLog业务日志","LegoLog网络日志"};
    LogTypeEnum[] logTypeEnums = new  LogTypeEnum[]{LogTypeEnum.Logcat,null,LogTypeEnum.Bussiness,LogTypeEnum.Net};

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                mLogWatchAdapter.addData((String)msg.obj);
                mRecyclerView.smoothScrollToPosition(mLogWatchAdapter.getItemCount());
            }
        }
    };

    public LogWatchWindow(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        mFloatViewWidth = SystemUtil.getScreenPixels()[0];
    }

    public void createView() {
        // TODO Auto-generated method stub
        //加载布局文件
        mFloatView = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.window_watch_log, null);
        //为View设置监听，以便处理用户的点击和拖动
        mFloatView.setOnTouchListener(new OnFloatViewTouchListener());
        /*为View设置参数*/
        mLayoutParams = new WindowManager.LayoutParams();
        //设置View默认的摆放位置
//        mLayoutParams.gravity = Gravity.RIGHT | Gravity.CENTER;
        //设置window type

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0+
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置背景为透明
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //注意该属性的设置很重要，FLAG_NOT_FOCUSABLE使浮动窗口不获取焦点,若不设置该属性，屏幕的其它位置点击无效，应为它们无法获取焦点
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置视图的显示位置，通过WindowManager更新视图的位置其实就是改变(x,y)的值
        mCurrentX = mLayoutParams.x = 0;
        mCurrentY = mLayoutParams.y = 0;
        //设置视图的宽、高
        mLayoutParams.width = mFloatViewWidth;
        mLayoutParams.height = mFloatViewHeight;
        //将视图添加到Window中
        mWindowManager.addView(mFloatView, mLayoutParams);
        initView();

    }

    private void initView() {
        mFloatView.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogRecord.getINSTANCE().setOnPrintLogListener(null);
                mWindowManager.removeView(mFloatView);
            }
        });
        mRecyclerView = mFloatView.findViewById(R.id.list_log);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mFloatView.getContext()));
        mLogWatchAdapter = new LogWatchAdapter(mFloatView.getContext());
        mRecyclerView.setAdapter(mLogWatchAdapter);

        LogcatRecord.setOnPrintLogListener(mOnPrintLogListener);

        Spinner spinner = mFloatView.findViewById(R.id.spinner);

        //创建ArrayAdapter对象
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(mFloatView.getContext(),R.layout.spinner_list_item_1,logTypes);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mWatchType = logTypeEnums[position];
                if(mWatchType == LogTypeEnum.Logcat){
                    LogcatRecord.setOnPrintLogListener(mOnPrintLogListener);
                    LogRecord.getINSTANCE().setOnPrintLogListener(null);
                }else{
                    LogcatRecord.setOnPrintLogListener(null);
                    LogRecord.getINSTANCE().setOnPrintLogListener(mOnPrintLogListener);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ((CheckBox)mFloatView.findViewById(R.id.cb_watch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsWatching = isChecked;
                if(mIsWatching){
                    buttonView.setText("实时监听");
                }else{
                    buttonView.setText("暂停监听");
                }
            }
        });

        mFloatView.findViewById(R.id.iv_oper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDisplay){
                    mFloatView.findViewById(R.id.ll_conent).setVisibility(View.INVISIBLE);
                    ((ImageView)v).setImageResource(R.mipmap.lib_log_back);
                }else{
                    mFloatView.findViewById(R.id.ll_conent).setVisibility(View.VISIBLE);
                    ((ImageView)v).setImageResource(R.mipmap.lib_log_right);
                }
                isDisplay = !isDisplay;
            }
        });
    }
    boolean isDisplay = true;
    OnPrintLogListener mOnPrintLogListener = new OnPrintLogListener() {
        @Override
        public void onPrintLog(LogTypeEnum logType, String logMessage){
            if(mIsWatching){
                if(mWatchType!=null){
                    if(logType != mWatchType){
                        return;
                    }
                }
                Message message = mHandler.obtainMessage(1);
                message.obj = logMessage;
                message.sendToTarget();
            }
        }

    };

    private class OnFloatViewTouchListener implements View.OnTouchListener {
        int lastX;
        int lastY;
        int paramX, paramY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
//            Log.i("wangq", "mCurrentX: " + mCurrentX + ",mCurrentY: "
//                    + mCurrentY + ",mFloatViewWidth: " + mFloatViewWidth
//                    + ",mFloatViewHeight: " + mFloatViewHeight);
            /*
             * getRawX(),getRawY()这两个方法很重要。通常情况下，我们使用的是getX(),getY()来获得事件的触发点坐标，
             * 但getX(),getY()获得的是事件触发点相对与视图左上角的坐标；而getRawX(),getRawY()获得的是事件触发点
             * 相对与屏幕左上角的坐标。由于LayoutParams中的x,y是相对与屏幕的，所以需要使用getRawX(),getRawY()。
             */
            mCurrentX = (int) event.getRawX() - mFloatViewWidth;
            mCurrentY = (int) event.getRawY() - mFloatViewHeight;

            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    paramX = mLayoutParams.x;
                    paramY = mLayoutParams.y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;
                    mLayoutParams.x = paramX + dx;
                    mLayoutParams.y = paramY + dy;
                    // 更新悬浮窗位置
                    mWindowManager.updateViewLayout(mFloatView, mLayoutParams);
//                    updateFloatView();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        }
    }

    private void updateFloatView() {
        mLayoutParams.x = mCurrentX;
        mLayoutParams.y = mCurrentY;
        mWindowManager.updateViewLayout(mFloatView, mLayoutParams);
    }
}
