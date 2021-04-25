package com.caowj.lib_network.interfaces;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.caowj.lib_network.retrofit_sample.bean.HttpBaseResult;
import com.caowj.lib_network.retrofit_sample.bean.ResponseResult;
import com.caowj.lib_network.util.FileUtils;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <pre>
 *     BaseViewModel中的内部类：
 *     1、封装了常规的数据解析
 *     2、保存报错接口的请求参数
 *     3、针对常见的status（业务status），翻译提示语
 *     作者：Caowj
 *     邮箱：caoweijian@kedacom.com
 *     日期：2020/10/23 11:11
 * </pre>
 */
public class HttpRequestCallback<T> implements Callback<HttpBaseResult<T>> {
    MutableLiveData<T> nLiveData;
    private boolean showFailedToast = true;//失败的情况，默认弹窗Toast

    public HttpRequestCallback() {
        showLoading();
    }

    public HttpRequestCallback(boolean showLoading) {
        if (showLoading) {
            showLoading();
        }
    }

    public HttpRequestCallback(boolean showLoading, boolean showToast) {
        if (showLoading) {
            showLoading();
        }
        showFailedToast = showToast;
    }

    public HttpRequestCallback(MutableLiveData<T> liveData) {
        showLoading();
        nLiveData = liveData;
    }

    @Override
    public final void onResponse(Call<HttpBaseResult<T>> call, Response<HttpBaseResult<T>> response) {
        onMyComplete();
        HttpBaseResult<T> baseBean = response.body();
        if (baseBean == null) {
            FileUtils.writeTxtToFile("接口异常响应：" + response.toString());
            onMyFailure(HttpBaseResult.STATUS_FAILURE, "Http Body Is NULL");
            return;
        }
        if (baseBean.isSuccess()) {
            ResponseResult<T> result = baseBean.getResult();
            if (result != null) {
                onMySuccess(result.getData());
                onMySuccess(result.getData(), result.getTotal());
            } else {
                onMySuccess((T) result);
            }
        } else {
            onMyFailure(baseBean.getStatus(), baseBean.getMessage());
            // 接口异常，保存接口请求参数，便于调试
            saveErrorRequest(call, baseBean.getStatus() + ",," + baseBean.getMessage());
        }
    }

    @Override
    public final void onFailure(Call<HttpBaseResult<T>> call, Throwable t) {
        onMyComplete();
        saveErrorRequest(call, t.getMessage());
        if (t instanceof ConnectException) {
            onMyFailure(HttpBaseResult.STATUS_NETWORK_UNCONNECTED, "网络连接失败");
        } else if (t instanceof SocketTimeoutException) {
            onMyFailure(HttpBaseResult.STATUS_NETWORK_READTIME_OUT, "网络连接超时");
        } else if (t instanceof UnknownHostException) {
            onMyFailure(HttpBaseResult.STATUS_UnknownHostException, "无法连接到服务器，请检查网络是否可用");
        } else if (t instanceof NoRouteToHostException) {
            onMyFailure(HttpBaseResult.STATUS_NoRouteToHostException, "连接服务器失败");
        } else {
            onMyFailure(HttpBaseResult.STATUS_EXCEPTION, t.getMessage());
        }
    }

    public void onMySuccess(T result) {
        if (nLiveData != null) {
            nLiveData.postValue(result);
        }
    }

    public void onMySuccess(T result, int totalPage) {
        if (nLiveData != null) {
            nLiveData.postValue(result);
        }
    }

    public void onMyFailure(int status, String message) {
        Log.w("caowj", "接口请求失败：status:" + status + ",,message:" + message);
//        if (status == MyConstants.NETWORK_RESPONSE_STATUS_TOKEN_EXPIRE || "Invalid token".equals(message)) {
//            //token过期toast提示用户重新登录
//            ToastUtil.showFilteredToast(AppUtil.getApp(), "您的登录状态已过期，请重新登录", TOAST_WARNING);
//            LegoEventBus.use("showReLoginDialog", Integer.class).postValue(1);
//        } else if (status == 0) {
//            //服务停止了
//            if (showFailedToast) {
//                ToastUtil.showFilteredToast(AppUtil.getApp(), "服务器异常", TOAST_ERROR);
//            }
//        } else {
//            if (showFailedToast) {
//                ToastUtil.showFilteredToast(AppUtil.getApp(), message, TOAST_ERROR);
//            }
//        }
    }

    public void onMyComplete() {
        hideLoading();
    }

    /**
     * 接口异常时，保存接口请求参数，便于调试。
     *
     * @param call 请求参数
     * @param <T>  类型
     */
    private <T> void saveErrorRequest(Call<HttpBaseResult<T>> call, String message) {
        if (call != null && call.request() != null) {
            Request request = call.request();
            RequestBody body = request.body();

            String logBuffer = "URL：" + request.url().toString() + "\r\n" +
                    "Method：" + request.method() + "\r\n" +
                    "Body：" + FileUtils.parseParams(body) + "\r\n" +
                    "Message：" + message;
            FileUtils.writeTxtToFile(logBuffer);
        }
    }

    private void hideLoading() {
        //通过改变ViewModel中的MutableLiveData变量值，通知BaseActivity隐藏弹窗
    }

    private void showLoading() {
        //通过改变ViewModel中的MutableLiveData变量值，通知BaseActivity显示弹窗
    }
}
