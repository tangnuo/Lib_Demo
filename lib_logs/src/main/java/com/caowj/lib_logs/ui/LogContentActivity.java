package com.caowj.lib_logs.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.caowj.lib_logs.R;
import com.caowj.lib_logs.ui.adapter.PageData;
import com.caowj.lib_logs.ui.adapter.LogContentAdapter;

import java.util.List;

public class LogContentActivity extends AppCompatActivity implements PageData.OnPageDataListener  {
    String mPath;
    int mfirstPostion = 0;
    LogContentAdapter mLogContentAdapter;
    PageData mPageData;
    RecyclerView mRecyclerView;
    Handler mHandler = new Handler();
    private AlertDialog mLoadingDialog;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_content);
        mPath = getIntent().getStringExtra("path");
        mRecyclerView = findViewById(R.id.list_log);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPageData = new PageData(mPath,this);


        mLogContentAdapter = new LogContentAdapter(this,mPageData);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //当前屏幕 首个 可见的 Item 的position
                int firstPosition = ((LinearLayoutManager)recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();
                int lastPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                if(mfirstPostion!=firstPosition){
                    mfirstPostion =  firstPosition;
                    mPageData.onVisiblePositionChange(firstPosition,lastPosition,dy<0);
                }

            }
        });

        mPageData.firstRead(null);
        showLoadingDialog();
//        if(data!=null){
//            if(data.size()>0){
//                if(mLogContentAdapter.getItemCount()==0){
//                    mLogContentAdapter.setItemCount(Integer.MAX_VALUE);
//                }
//                mLogContentAdapter.notifyDataSetChanged();
//            }
//        }
//        mLogContentAdapter.setData( readFile(0));
        mRecyclerView.setAdapter(mLogContentAdapter);

        ((EditText)findViewById(R.id.et_search)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(null);
                }

                return false;
            }
        });
    }


    public void search(View view) {
        mLogContentAdapter.setItemCount(0);
        mLogContentAdapter.notifyDataSetChanged();
        if(view == null){
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mRecyclerView.getWindowToken(), 0);
        }
        String keyword = ((EditText)findViewById(R.id.et_search)).getText().toString().trim();
        mPageData.firstRead(keyword);

        showLoadingDialog();


    }
    public void clearKeywords(View view) {
        ((EditText)findViewById(R.id.et_search)).setText("");
    }

    public void back(View view) {
        finish();
    }
    int mReqeustLoadPageNum;

    @Override
    public void requestLoadNewPage(int pageNum){
        mReqeustLoadPageNum = pageNum;
        mRecyclerView.stopScroll();
        showLoadingDialog();
    }

    @Override
    public void onFinishLoadNewPage(int pageNum){
        if(mReqeustLoadPageNum == pageNum){
            hideLoadingDialog();
            mLogContentAdapter.notifyItemChanged(pageNum*PageData.PAGE_SIZE);
        }
    }
    @Override
    public void onReadFileEnd(final int maxLines){
//        Log.e("wangq","onReadFileEnd:maxLines="+maxLines);
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                mLogContentAdapter.setItemCount(maxLines);
                mLogContentAdapter.notifyItemRangeRemoved(maxLines,1);
//                Log.e("wangq","onReadFileEndpost:maxLines="+maxLines);
            }
        });

    }
    @Override
    public void onReadFirstPage(List<String> data){
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }

        if(data==null||data.size()==0){
            Toast.makeText(this.getApplicationContext(),"未检索到内容",Toast.LENGTH_SHORT).show();
            mLogContentAdapter.setItemCount(0);
            mLogContentAdapter.notifyDataSetChanged();
        }else{
            if(mLogContentAdapter.getItemCount()==0){
                mLogContentAdapter.setItemCount(Integer.MAX_VALUE);
            }
            mRecyclerView.scrollToPosition(0);
            mLogContentAdapter.notifyDataSetChanged();
        }
    }

    public void hideLoadingDialog() {
        if(mLoadingDialog.isShowing()){
            mLoadingDialog.dismiss();
        }
    }
    public void showLoadingDialog() {
        if(mLoadingDialog == null){
            View view = getLayoutInflater().inflate(R.layout.lib_log_loading_alert,mRecyclerView,false);
            mLoadingDialog = new AlertDialog.Builder(this)
                    .setView(view).create();
            mLoadingDialog.setCancelable(false);
            mLoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
                        return true;
                    return false;
                }
            });
//            mLoadingDialog.setContentView(R.layout.lib_log_loading_alert);
            mLoadingDialog.setCanceledOnTouchOutside(false);
        }

        mLoadingDialog.show();

    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        mPageData.release();
    }


}
