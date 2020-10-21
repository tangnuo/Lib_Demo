package com.caowj.lib_logs.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.caowj.lib_logs.R;
import com.caowj.lib_logs.helper.Constance;
import com.caowj.lib_logs.helper.LogTypeEnum;
import com.caowj.lib_utils.FileUtil;
import com.caowj.lib_logs.ui.adapter.LogFileListAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class LogFileListActivity extends AppCompatActivity {
    String mFolderPath;
    LogFileListAdapter mLogFileListAdapter;
    HashSet<String> mSelectedList = new HashSet();
    LogTypeEnum mLogType;
    boolean mSelectAll = true;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_file_list);
        int type = getIntent().getIntExtra("type", LogTypeEnum.Crash.toValue());
        mLogType = LogTypeEnum.valueOf(type);

        mFolderPath = Constance.GLOBAL_PATH
                + File.separator
                + Constance.LOG_FOLDER_NAME_MAP.get(mLogType);
        initRecyclerView(mLogType);
        ((TextView) findViewById(R.id.tv_title)).setText(mLogType.getName());

    }

    private void initRecyclerView(LogTypeEnum logType) {
        RecyclerView recyclerView = findViewById(R.id.list_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLogFileListAdapter = new LogFileListAdapter(this);
        recyclerView.setAdapter(mLogFileListAdapter);
        mLogFileListAdapter.setData(getData(logType));
        mLogFileListAdapter.setOnCheckedChangeListener(new LogFileListAdapter.OnRecyclerViewCheckedChangeListener() {
            @Override
            public void onItemClick(@NotNull View view, @NotNull LogFileInfo data, boolean isChecked) {
                data.isSelected = isChecked;
                if (isChecked) {
                    mSelectedList.add(data.fileName);
                } else {
                    mSelectedList.remove(data.fileName);
                }

            }
        });
        mLogFileListAdapter.setOnItemClickListener(new LogFileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(@NotNull View view, @NotNull LogFileInfo data) {
                Intent intent = new Intent(LogFileListActivity.this, LogContentActivity.class);
                intent.putExtra("path", mFolderPath + File.separator + data.fileName);
                startActivity(intent);
            }
        });
    }


    private List<LogFileInfo> getData(LogTypeEnum logType) {

        File folder = new File(mFolderPath);
        File[] files = folder.listFiles();
        if (files == null) {
            return null;
        }
        List<LogFileInfo> list = new ArrayList<>(files.length);
        LogFileInfo fileInfo;
        for (File file : files) {
            fileInfo = new LogFileInfo(file.getName());
            fileInfo.displaySize = FileUtil.byteCountToDisplaySize(file.length());

            list.add(fileInfo);
        }
        java.util.Collections.sort(list, new Comparator<LogFileInfo>() {
            @Override
            public int compare(LogFileInfo o1, LogFileInfo o2) {
                return -o1.fileName.compareTo(o2.fileName);
            }
        });

        return list;
    }

    public void del(View view) {
        showConfrimDialog();
    }

    private void showConfrimDialog() {

        new AlertDialog.Builder(this).setTitle("提示")
                .setMessage("确认删除？").setPositiveButton("确定",

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        delFiles();

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        }).create().show();

    }

    private void delFiles() {
        File file;
        for (String fileName : mSelectedList) {
            file = new File(mFolderPath + File.separator + fileName);
            file.delete();
        }
        mLogFileListAdapter.setData(getData(mLogType));
    }



    public void selectAll(View view) {
        if (mSelectAll) {
            List<LogFileInfo> data = mLogFileListAdapter.getData();
            for (LogFileInfo fileInfo : data) {
                fileInfo.isSelected = true;
                mSelectedList.add(fileInfo.fileName);
            }
            mLogFileListAdapter.notifyDataSetChanged();
            ((TextView) view).setText("不选");
        } else {
            List<LogFileInfo> data = mLogFileListAdapter.getData();
            for (LogFileInfo fileInfo : data) {
                fileInfo.isSelected = false;
            }
            mSelectedList.clear();
            mLogFileListAdapter.notifyDataSetChanged();
            ((TextView) view).setText("全选");

        }
        mSelectAll = !mSelectAll;
//        mSelectedList.add

    }

    public void back(View view) {
        finish();
    }

}
