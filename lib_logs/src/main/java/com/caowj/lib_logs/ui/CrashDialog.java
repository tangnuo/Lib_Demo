package com.caowj.lib_logs.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.caowj.lib_logs.R;


/**
 * @author wangqian
 * @date 2020/7/1
 */
public class CrashDialog extends AlertDialog {
    private Context nContext;
    private TextView nBtnCheckLog, nBtClose;
    private TextView nMsgView;
    String nLog;
    String highlightString = "com.kedacom";
    int nShowTime = 60;
    public CrashDialog(Context context,String log) {
        super(context);
        this.nContext = context;
        nLog = log;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(nContext).inflate(R.layout.dialog_crash, null);
        setContentView(view);
        initView(view);
        initListener();
    }

    private void initView(View view) {
        nBtClose = (TextView) view.findViewById(R.id.btn_close);
        nBtnCheckLog = (TextView) view.findViewById(R.id.btn_checkLog);
        nMsgView = (TextView) view.findViewById(R.id.tv_msg);
    }

    private void initListener() {
        nBtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrashDialog.this.dismiss();
            }
        });
        nBtnCheckLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightString();
                nMsgView.setGravity(Gravity.LEFT);
                nBtnCheckLog.setVisibility(View.GONE);
                widthFullScreen();
            }
        });

    }

    public void show(){
        super.show();
        nBtClose.setText("关闭（"+nShowTime+"s）");
        nHandler.sendMessageDelayed(nHandler.obtainMessage(1),1000);
    }

    Handler nHandler = new Handler(){
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                if(nShowTime == 0){
                    dismiss();
                }else{
                    nShowTime -= 1;

                    nBtClose.setText("关闭（"+nShowTime+"s）");
                    nHandler.sendMessageDelayed(nHandler.obtainMessage(1),1000);
                }

            }
        }
    };

    private void highlightString() {

        int length = highlightString.length();
        SpannableStringBuilder sb=new SpannableStringBuilder();
        sb.append(nLog);
        String[] lines = nLog.split("\\n");
        int startIndex = 0;
        // 包含highlightString的行都变成蓝色
        for(String line:lines){
            if(line.contains(highlightString)){
//                int index = nLog.indexOf(line);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#1677ff"));
                sb.setSpan(colorSpan,startIndex,startIndex+line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //\n 是一个字符
            startIndex+=line.length()+1;
        }

//        Matcher matcher = Pattern.compile(highlightString).matcher(nLog);
//
//        matcher.reset();
//        boolean result = matcher.find();
//        if (result) {
//            do {
//                int index = matcher.start();
//                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#1677ff"));
//                sb.setSpan(colorSpan,index,index+length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                result = matcher.find();
//            } while (result);
//
//        }


        nMsgView.setText(sb);
        nMsgView.setTextSize(TypedValue.COMPLEX_UNIT_SP,8);
    }

    private void widthFullScreen(){
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;//宽高可设置具体大小
        // 此行必不可少
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

}
