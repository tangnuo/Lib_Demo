package com.caowj.lib_utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * @author wangqian
 * @date 2020/11/30
 * 获取当前网络状态
 */
@SuppressLint("MissingPermission")
public class NetworkUtil {

    public enum NetWorkState {
        /**
         * 网络不可用
         */
        NETWORK_STATE_UNAVAILABLE(-1),
        /**
         * 网络可用
         */
        NETWORK_STATE_AVAILABLE(0),
        /**
         * 网络可用，且是移动数据
         */
        NETWORK_STATE_MOBILE(1),
        /**
         * 网络可用，且是wifi
         */
        NETWORK_STATE_AVAILABLE_WIFI(2),
        /**
         * 网络可用，且是Ethernet
         */
        NETWORK_STATE_AVAILABLE_ETHERNET(3);

        private int value;//自定义属性

        NetWorkState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

//    public enum WifiState {
//
//        WIFE_STATE_OPEN(1),
//        WIFE_STATE_CLOSE(0);
//        private int value;//自定义属性
//
//        WifiState(int value) {
//            this.value = value;
//        }
//
//        public int getValue() {
//            return value;
//        }
//    }

    public static NetWorkState getNetworkState(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkNetworkStateAbove23(context);
        }
        return checkNetworkStateBelow23(context);
    }

    //检测当前的网络状态
    public static NetWorkState checkNetworkStateBelow23(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo.isConnected()) {
            return NetWorkState.NETWORK_STATE_AVAILABLE_WIFI;
        }
        //获取移动数据连接的信息
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfo.isConnected()) {
            return NetWorkState.NETWORK_STATE_MOBILE;
        }

        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

        if (networkInfo.isConnected()) {
            return NetWorkState.NETWORK_STATE_AVAILABLE_ETHERNET;
        }

        NetworkInfo info = connMgr.getActiveNetworkInfo();
        if (null != info && info.isConnected()) {
            return NetWorkState.NETWORK_STATE_AVAILABLE;
        }

        return NetWorkState.NETWORK_STATE_UNAVAILABLE;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    public static NetWorkState checkNetworkStateAbove23(Context context) {
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Network activeNetwork = connMgr.getActiveNetwork();
        if (activeNetwork == null) {
            return NetWorkState.NETWORK_STATE_UNAVAILABLE;
        }
        NetworkCapabilities networkCapabilities = connMgr.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return NetWorkState.NETWORK_STATE_AVAILABLE_WIFI;

            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return NetWorkState.NETWORK_STATE_MOBILE;

            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return NetWorkState.NETWORK_STATE_AVAILABLE_ETHERNET;
            }

            return NetWorkState.NETWORK_STATE_AVAILABLE;
        }
        return NetWorkState.NETWORK_STATE_UNAVAILABLE;
    }

    public static NetConnectivityChangeReceiver registerNetConnectionChangeBroadcast(Context context,
                                                                                                      NetConnectivityChangeReceiver.OnConnectivityChangeListener onConnectivityChangeListener) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        NetConnectivityChangeReceiver connectionChangeReceiver = new NetConnectivityChangeReceiver(onConnectivityChangeListener);
        context.registerReceiver(connectionChangeReceiver, intentFilter);
        return connectionChangeReceiver;

    }

    public static void unRegisterNetConnectionChangeBroadcast(Context context,
                                                           NetConnectivityChangeReceiver netConnectivityChangeReceiver) {
        context.unregisterReceiver(netConnectivityChangeReceiver);
        netConnectivityChangeReceiver.unRegister();
    }
}
