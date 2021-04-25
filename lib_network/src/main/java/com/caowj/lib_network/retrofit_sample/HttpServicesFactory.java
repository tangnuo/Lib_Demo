package com.caowj.lib_network.retrofit_sample;

import android.support.annotation.NonNull;

import com.caowj.lib_network.retrofit_sample.net.HttpServiceApi;
import com.caowj.lib_network.retrofit.HttpRetrofitFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;


/**
 * 使用封装的RetrofitFactory，内部自定义了各种拦截器；参考【LegoArch】
 * 1、日志拦截器
 * 2、缓存拦截器
 * 3、去重拦截器
 * 4、取消执行
 */
public class HttpServicesFactory {

    private final static String baseUrl = "https://api.apiopen.top/";
    private final static HttpRetrofitFactory httpRetrofitFactory;

    static {
        //init HttpRetrofitFactory
        httpRetrofitFactory = HttpRetrofitFactory.instance(new HttpRetrofitFactory.Interceptor() {
            @Override
            public String baseUrl() {
                // TODO: 2020/10/23 替换成自己的
//                return BuildConfig.HTTP_BASE_URL;
                return baseUrl;
            }

            @Override
            public void okHttpClient(OkHttpClient.Builder okHttpClient) {
                okHttpClient.connectTimeout(5000, TimeUnit.MILLISECONDS)
                        .writeTimeout(60000, TimeUnit.MILLISECONDS)
                        .readTimeout(60000, TimeUnit.MILLISECONDS);
//                        .addInterceptor(chain -> {
//                            Request.Builder builder = chain.request().newBuilder();
//                            AccountBean bean = MainApplication.getInstance().getAccountBean();
//                            builder.addHeader("user_id", bean.getUserCardNum());//身份证号码
//
//                            return chain.proceed(builder.build());
//                        })

            }

            @Override
            public void retrofit(Retrofit.Builder retrofit) {

            }
        });

    }

    public static HttpRetrofitFactory getHttpRetrofitFactory() {
        return httpRetrofitFactory;
    }

    private static <T> T createService(@NonNull final Class<T> clazz) {
        if (httpRetrofitFactory != null) return httpRetrofitFactory.createHttpService(clazz);
        return null;
    }


    public static synchronized HttpServiceApi getHttpServiceApi() {
        return createService(HttpServiceApi.class);
    }

}
