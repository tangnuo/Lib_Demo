package com.caowj.lib_network.retrofit;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LruCache;

import com.caowj.lib_network.retrofit.converter.NullOnEmptyConverterFactory;
import com.caowj.lib_network.retrofit.log.HttpLogInterceptor;
import com.caowj.lib_network.retrofit.parser.BaseUrlParser;
import com.caowj.lib_network.retrofit.parser.HttpUrlParser;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * HttpRetrofitFactory
 *
 * @author : yuanbingbing
 * @since : 2018/7/26 14:04
 */
public class HttpRetrofitFactory {

    protected static final String DOMAIN_NAME = "Lego-Domain";
    private static final String GLOBAL_DOMAIN_NAME = "com.kedacom.lego.DomainName";
    static protected HttpUrl nRetrofitBaseUrl = null;
    static OkHttpClient mOkHttpClient;
    // 去掉重复请求的拦截器
//    static CancelDuplicationInterceptor mCancelDuplicationInterceptor = new CancelDuplicationInterceptor();
    private final Map<String, HttpUrl> nDomainNameHub = new HashMap<>();
    private Retrofit nRetrofit;
    private HttpUrlParser nUrlParser;
    private LruCache<String, Object> nCache = new LruCache<>(20);
    private boolean isPrintLog = true;

    private HttpRetrofitFactory(@NonNull Retrofit retrofit) {
        this.nRetrofit = retrofit;
    }

    private HttpRetrofitFactory() {

    }

    /**
     * 实例化 HttpRetrofitFactory
     *
     * @param interceptor 构造拦截器
     * @return HttpServerFactory实例
     */
    public static synchronized HttpRetrofitFactory instance(@NonNull Interceptor interceptor) {
        HttpRetrofitFactory result = new HttpRetrofitFactory();

        //获取用户配置
        String globalUrl = interceptor.baseUrl();
        if (!TextUtils.isEmpty(globalUrl)) {
//            result.setGlobalDomain(globalUrl);
        } else {
            throw new IllegalArgumentException("please initialize retrofit baseurl");
        }
        Map<String, String> domainUrl = interceptor.domainUrl();
        if (domainUrl != null && domainUrl.size() != 0) {
            for (Map.Entry<String, String> entry : domainUrl.entrySet()) {
                result.putDomain(entry.getKey(), entry.getValue());
            }
        }

        OkHttpClient.Builder okHttp = new OkHttpClient.Builder()
//                .cache(new Cache(context.getExternalCacheDir(), 10 * 1024 * 1024))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                // 这两个Interceptor的顺序不可调换
//                .addInterceptor(mCancelDuplicationInterceptor)
                .addInterceptor(result.new HttpDomainInterceptor());

//        if (interceptor.cancelDuplicationFullUrls() != null) {
//            mCancelDuplicationInterceptor.addCancelDuplicationStrUrls(interceptor.cancelDuplicationFullUrls());
//        }


        //用户配置OKHttpClient
        interceptor.okHttpClient(okHttp);

        result.isPrintLog = interceptor.printLog();
        if (result.isPrintLog) {
            okHttp.addInterceptor(new HttpLogInterceptor(true));
        }

        mOkHttpClient = okHttp.build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(globalUrl)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()))
                // .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mOkHttpClient);

        //用户配置Retrofit
        interceptor.retrofit(builder);


        result.nRetrofit = builder.build();
        nRetrofitBaseUrl = result.nRetrofit.baseUrl();
        return result;
    }

    static HttpUrl checkUrl(String url) {
        HttpUrl parseUrl = HttpUrl.parse(url);
        if (null == parseUrl) {
            throw new IllegalArgumentException(url);
        } else {
            return parseUrl;
        }
    }

    private <T> T checkNotNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    /**
     * 实例化Http Service
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T createHttpService(@NonNull final Class<T> clazz) {
        Object o = nCache.get(clazz.getCanonicalName());
        if (o != null) return (T) o;
        if (nRetrofit != null) {
            T t = nRetrofit.create(clazz);
            nCache.put(clazz.getCanonicalName(), t);
            // 添加需要去掉重复请求的url
//            mCancelDuplicationInterceptor.addCancelDuplicationUrls(AnnotationHandler.handler(clazz));
            return t;
        }
        return null;
    }

    protected void putDomain(String domainName, String domainUrl) {
        checkNotNull(domainName, "domainName cannot be null");
        checkNotNull(domainUrl, "domainUrl cannot be null");
        synchronized (nDomainNameHub) {
            nDomainNameHub.put(domainName, checkUrl(domainUrl));
        }
    }

    public synchronized HttpUrl getDomain(String domainName) {
        checkNotNull(domainName, "domainName cannot be null");
        return nDomainNameHub.get(domainName);
    }

    protected void removeDomain(String domainName) {
        checkNotNull(domainName, "domainName cannot be null");
        synchronized (nDomainNameHub) {
            nDomainNameHub.remove(domainName);
        }
    }

    /**
     * 获取全局 BaseUrl
     */
    protected synchronized HttpUrl getGlobalDomain() {
        return getDomain(GLOBAL_DOMAIN_NAME);
    }

    /**
     * 设置全局 BaseUrl
     *
     * @param globalDomain
     */
    public void setGlobalDomain(String globalDomain) {
        checkNotNull(globalDomain, "globalDomain cannot be null");
        synchronized (nDomainNameHub) {
            HttpUrl httpUrl = checkUrl(globalDomain);
            nDomainNameHub.put(GLOBAL_DOMAIN_NAME, httpUrl);
            nUrlParser = new BaseUrlParser(httpUrl);
        }
    }

    /**
     * 移除全局 BaseUrl
     */
    protected void removeGlobalDomain() {
        removeDomain(GLOBAL_DOMAIN_NAME);
    }

    private String parseDomainNameFromHeaders(Request request) {
        List<String> headers = request.headers(DOMAIN_NAME);
        if (headers == null || headers.size() == 0)
            return null;
        if (headers.size() > 1)
            throw new IllegalArgumentException(String.format("Only one %s in the headers", DOMAIN_NAME));
        return request.header(DOMAIN_NAME);
    }

    /**
     * 实例构建拦截器
     */
    public static abstract class Interceptor {

        /**
         * Retrofit Http 服务跟路径
         *
         * @return
         */
        public abstract String baseUrl();

        /**
         * 是否打印HTTP的日志
         * <p>
         * <p>
         *
         * @return
         */
        @Deprecated
        public boolean printLog() {
            return true;
        }

        /**
         * 获取 DomainName 与 URL 的映射关系
         *
         * @return DomainName 与 URL 映射集合
         */
        public Map<String, String> domainUrl() {
            return null;
        }

        /**
         * OkHttpClient 构造自定义参数设置
         *
         * @param okHttpClient OkHttpClient.Builder
         */
        public void okHttpClient(OkHttpClient.Builder okHttpClient) {

        }


        /**
         * Retrofit 构造自定义参数设置
         *
         * @param retrofit Retrofit.Builder
         */
        public void retrofit(Retrofit.Builder retrofit) {

        }

        /**
         * 设置取消重复请求的全路径url
         *
         * @return
         */
        public List<String> cancelDuplicationFullUrls() {
            return null;
        }

    }

    protected class HttpDomainInterceptor implements okhttp3.Interceptor {
        private static final String TAG = "HttpDomainInterceptor";

        @Override
        public Response intercept(Chain chain) throws IOException {
            return chain.proceed(processRequest(chain.request()));
        }

        public Request processRequest(Request request) {
            if (request == null) return request;
            HttpUrl httpUrl = getGlobalDomain();
            if (httpUrl == null) {
                return request;
            }
            String baseUrl = nRetrofitBaseUrl.toString();
            String requestUrl = request.url().toString();
            if (requestUrl.startsWith(baseUrl)) {//说明API使用的是相对路径，则可替换host
                Request.Builder newBuilder = request.newBuilder();

                if (null != httpUrl && nUrlParser != null) {
                    HttpUrl newUrl = nUrlParser.parseUrl(httpUrl, request.url());

          /*      if (isPrintLog)
                    Log.d(TAG, "The new url is { " + newUrl.toString() + " }, old url is { " + request.url().toString() + " }");*/

                    return newBuilder
                            .url(newUrl)
                            .build();
                }

                return newBuilder.build();
            } else {
                return request;
            }


        }


    }
}
