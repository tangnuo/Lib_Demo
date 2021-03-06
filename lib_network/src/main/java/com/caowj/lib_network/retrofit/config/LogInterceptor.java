package com.caowj.lib_network.retrofit.config;


import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * @Dec ：Log拦截器
 * @Author : Caowj
 * @Date : 2018/6/14 14:57
 */
public class LogInterceptor implements Interceptor {
    @Override
    public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Log.d("caowj", "LogInterceptor_request:" + request.toString());
        okhttp3.Response response = chain.proceed(chain.request());
        okhttp3.MediaType mediaType = response.body().contentType();
        String content = response.body().string();

//            Log.d("caowj","response body:" + content);
        if (response.body() != null) {
            ResponseBody body = ResponseBody.create(mediaType, content);
            return response.newBuilder().body(body).build();
        } else {
            return response;
        }
    }
}