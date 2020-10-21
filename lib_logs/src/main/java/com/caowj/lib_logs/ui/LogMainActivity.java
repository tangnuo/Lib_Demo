package com.caowj.lib_logs.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.caowj.lib_logs.R;
import com.caowj.lib_logs.helper.Constance;
import com.caowj.lib_logs.helper.LogTypeEnum;

import java.io.File;

public class LogMainActivity extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                //没有悬浮窗权限,跳转申请
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                permissionIntent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(permissionIntent, 100);

            }
        }


    }

    public void onResume() {
        super.onResume();
        ((TextView) findViewById(R.id.tv_crash_num)).setText("个数：" + getFileNum(LogTypeEnum.Crash));
        ((TextView) findViewById(R.id.tv_business_num)).setText("个数：" + getFileNum(LogTypeEnum.Bussiness));
        ((TextView) findViewById(R.id.tv_net_num)).setText("个数：" + getFileNum(LogTypeEnum.Net));
        ((TextView) findViewById(R.id.tv_logcat_num)).setText("个数：" + getFileNum(LogTypeEnum.Logcat));
    }
    public void back(View view){
        finish();
    }
    public void showRealTimeWindow(View view) {
        new LogWatchWindow(this).createView();
    }

    public void checkCrashLog(View view) {
        gotoLogListActivity(LogTypeEnum.Crash);
    }

    public void checkNetLog(View view) {
        gotoLogListActivity(LogTypeEnum.Net);
    }

    public void checkBussinessLog(View view) {
        gotoLogListActivity(LogTypeEnum.Bussiness);
    }

    public void checkLogcatLog(View view) {
        gotoLogListActivity(LogTypeEnum.Logcat);
    }

    public void gotoLogListActivity(LogTypeEnum logType) {
        Intent intent = new Intent(this, LogFileListActivity.class);
        intent.putExtra("type", logType.toValue());
        startActivity(intent);
    }


    private String getFileNum(LogTypeEnum logTypeEnum) {
        String folderPath = Constance.GLOBAL_PATH
                + File.separator
                + Constance.LOG_FOLDER_NAME_MAP.get(logTypeEnum);
        File folder = new File(folderPath);
        String[] list = folder.list();
        return list == null ? "0" : list.length + "";
    }





}
