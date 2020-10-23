package com.caowj.lib_network;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;

import com.caowj.lib_network.net.HttpServiceApi;
import com.caowj.lib_network.retrofit.config.HttpClient;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 使用默认的Retrofit
 */
public class HttpServicesFactory2 {
    private static final String BASE_URL = "https://api.apiopen.top/";
    private static Retrofit mRetrofit;
    private volatile static HttpServicesFactory2 servicesFactory2;
    private LruCache<String, Object> nCache = new LruCache<>(20);

    private HttpServicesFactory2() {
        getRetrofit(BASE_URL);
    }

    public static HttpServicesFactory2 getInstance() {
        if (servicesFactory2 == null) {
            synchronized (HttpServicesFactory2.class) {
                if (servicesFactory2 == null) {
                    servicesFactory2 = new HttpServicesFactory2();
                }
            }
        }
        return servicesFactory2;
    }

    private static Retrofit newRetrofit(String baseUrl) {
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .retryOnConnectionFailure(true)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
////                .addInterceptor(new ReceivedCookiesInterceptor(MainApplication.getContext()))
////                .addInterceptor(new AddCookiesInterceptor(MainApplication.getContext()))
////                .addInterceptor(new UserAgnetInterceptor())
//                .build();

        mRetrofit = new Retrofit.Builder()
//                .client(okHttpClient)//方法1
                .client(HttpClient.getHttpClient())//方法2
                .addConverterFactory(GsonConverterFactory.create())
//                .addConverterFactory(QipaiGsonConverterFactory.create())//自定义转换器
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
        return mRetrofit;
    }

    /**
     * 获取Retrofit实例
     *
     * @param baseUrl
     * @return
     */
    private Retrofit getRetrofit(String baseUrl) {
        synchronized (HttpServicesFactory2.class) {
            if (mRetrofit == null) {
                mRetrofit = newRetrofit(baseUrl);
            } else {
                String oldBaseUrl = mRetrofit.baseUrl().toString().toLowerCase();
                if (!baseUrl.toLowerCase().equals(oldBaseUrl)) {
                    mRetrofit = newRetrofit(baseUrl);
                } else {
                    Log.d("caowj", "retrofit已经存在。oldBaseUrl：" + oldBaseUrl + "\nbaseUrl:" + baseUrl);
                }
            }
        }

        return mRetrofit;
    }

    public synchronized HttpServiceApi getHttpServiceApi() {
        return createService(HttpServiceApi.class);
    }

    public <T> T createService(@NonNull final Class<T> clazz) {
        Object o = nCache.get(clazz.getCanonicalName());
        if (o != null) return (T) o;
        if (mRetrofit != null) {
            T t = mRetrofit.create(clazz);
            nCache.put(clazz.getCanonicalName(), t);
            // 添加需要去掉重复请求的url
//            mCancelDuplicationInterceptor.addCancelDuplicationUrls(AnnotationHandler.handler(clazz));
            return t;
        }
        return null;
    }
}
