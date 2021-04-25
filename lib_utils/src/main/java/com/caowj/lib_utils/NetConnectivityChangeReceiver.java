package com.caowj.lib_utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * @author wangqian
 * @date 2020/12/1
 */
public class NetConnectivityChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "caowj";
    OnConnectivityChangeListener nOnConnectivityChangeListener;

    public NetConnectivityChangeReceiver(OnConnectivityChangeListener onConnectivityChangeListener) {
        super();
        this.nOnConnectivityChangeListener = onConnectivityChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "网络状态改变");
        if (intent == null) {
            return;
        }
       if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (nOnConnectivityChangeListener != null) {
                nOnConnectivityChangeListener.onConnectivityChange(NetworkUtil.getNetworkState(context));
            }
        }
    }

    public void unRegister() {
        this.nOnConnectivityChangeListener = null;
    }

    public interface OnConnectivityChangeListener {
        void onConnectivityChange(NetworkUtil.NetWorkState netWorkState);
    }

}
