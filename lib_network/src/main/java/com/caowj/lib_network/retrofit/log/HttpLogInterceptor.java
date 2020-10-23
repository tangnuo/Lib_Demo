package com.caowj.lib_network.retrofit.log;

import android.support.annotation.Nullable;

import com.caowj.lib_network.util.TextUtil;
import com.caowj.lib_network.util.ZipUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Http网络请求和响应结果的日志拦截器
 * <p>
 *
 * @author : yuanbingbing
 * @since : 2018/9/11 16:11
 */
public class HttpLogInterceptor implements Interceptor {

    HttpLogPrinter mPrinter = new DefaultHttpLogPrinter();
    boolean IS_DEBUG = true;
    Level printLevel = Level.ALL;

    public HttpLogInterceptor() {
        IS_DEBUG = false;
    }

    public HttpLogInterceptor(boolean is_debug) {
        IS_DEBUG = is_debug;
    }

    public HttpLogInterceptor(HttpLogPrinter printer, boolean is_debug) {
        if (printer != null) mPrinter = printer;
        IS_DEBUG = is_debug;
    }


    /**
     * 解析请求服务器的请求参数
     *
     * @param request {@link okhttp3.Request}
     * @return 解析后的请求信息
     * @throws UnsupportedEncodingException
     */
    public static String parseParams(Request request) throws UnsupportedEncodingException {
        try {
            RequestBody body = request.newBuilder().build().body();
            if (body == null) return "";
            Buffer requestBuffer = new Buffer();
            body.writeTo(requestBuffer);
            Charset charset = StandardCharsets.UTF_8;
            MediaType contentType = body.contentType();

            if (contentType != null) {
                charset = contentType.charset(charset);
            }
            String text = requestBuffer.readString(charset);

            if (contentType != null && !"json".equals(contentType.subtype())) {
                text = URLDecoder.decode(text, convertCharset(charset));
            }

            return TextUtil.jsonFormat(text);
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public static String convertCharset(Charset charset) {
        String s = charset.toString();
        int i = s.indexOf("[");
        if (i == -1)
            return s;
        return s.substring(i + 1, s.length() - 1);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();


        String log = request.header("X-LegoLog");
        boolean isLogging = !"off".equalsIgnoreCase(log);

        boolean logRequest = isLogging && (printLevel == Level.ALL || (printLevel != Level.NONE && printLevel == Level.REQUEST));
        // wangqian 添加  && IS_DEBUG
        //非debug模式，需要判断请求不成功，才打印请求信息
        if (logRequest && IS_DEBUG) {
            //打印请求信息
            printRequest(request);
        }

        boolean logResponse = isLogging && (printLevel == Level.ALL || (printLevel != Level.NONE && printLevel == Level.RESPONSE));

        //链式调用
        long t1 = logResponse ? System.nanoTime() : 0;
        Response originalResponse;
        try {
            originalResponse = chain.proceed(request);
        } catch (Exception e) {
//            LegoLog.w("Http Error: " + e);
            if (!IS_DEBUG && logRequest) {
                printRequest(request);
            }
            mPrinter.printException(e);
            throw e;
        }
        long t2 = logResponse ? System.nanoTime() : 0;

        ResponseBody responseBody = originalResponse.body();

        //打印响应结果
        String bodyString = null;
        if (responseBody != null && HttpContentTypeUtil.isParseable(responseBody.contentType())) {
            bodyString = printResult(request, originalResponse, logResponse);
        }

        if (!IS_DEBUG) {
            if (!originalResponse.isSuccessful()) {
                // release模式下，请求失败，打印request和response
                //打印请求信息
                if (logRequest) {
                    printRequest(request);
                }

            } else {
                logResponse = false;
            }
        }

        if (logResponse) {
            final List<String> segmentList = request.url().encodedPathSegments();
            final String header = originalResponse.headers().toString();
            final int code = originalResponse.code();
            final boolean isSuccessful = originalResponse.isSuccessful();
            final String message = originalResponse.message();
            final String url = originalResponse.request().url().toString();

            if (responseBody != null && HttpContentTypeUtil.isParseable(responseBody.contentType())) {
                mPrinter.printJsonResponse(TimeUnit.NANOSECONDS.toMillis(t2 - t1), isSuccessful,
                        code, header, responseBody.contentType(), bodyString, segmentList, message, url);
            } else {
                mPrinter.printFileResponse(TimeUnit.NANOSECONDS.toMillis(t2 - t1),
                        isSuccessful, code, header, segmentList, message, url);
            }

        }


        return originalResponse;
    }

    private void printRequest(Request request) throws IOException {
        if (request.body() != null && HttpContentTypeUtil.isParseable(request.body().contentType())) {
            mPrinter.printJsonRequest(request, parseParams(request));
        } else {
            mPrinter.printFileRequest(request);
        }
    }

    /**
     * 打印响应结果
     *
     * @param request     {@link okhttp3.Request}
     * @param response    {@link okhttp3.Response}
     * @param logResponse 是否打印响应结果
     * @return 解析后的响应结果
     * @throws IOException
     */
    @Nullable
    private String printResult(Request request, Response response, boolean logResponse) throws IOException {
        try {
            //读取服务器返回的结果
            ResponseBody responseBody = response.newBuilder().build().body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();

            //获取content的压缩类型
            String encoding = response
                    .headers()
                    .get("Content-Encoding");

            Buffer clone = buffer.clone();

            //解析response content
            return parseContent(responseBody, encoding, clone);
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 解析服务器响应的内容
     *
     * @param responseBody {@link okhttp3.ResponseBody}
     * @param encoding     编码类型
     * @param clone        克隆后的服务器响应内容
     * @return 解析后的响应结果
     */
    private String parseContent(ResponseBody responseBody, String encoding, Buffer clone) {
        Charset charset = StandardCharsets.UTF_8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(charset);
        }
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {//content使用gzip压缩
            return ZipUtil.decompressForGzip(clone.readByteArray(), convertCharset(charset));//解压
        } else if (encoding != null && encoding.equalsIgnoreCase("zlib")) {//content使用zlib压缩
            return ZipUtil.decompressToStringForZlib(clone.readByteArray(), convertCharset(charset));//解压
        } else {//content没有被压缩
            return clone.readString(charset);
        }
    }

    public enum Level {
        /**
         * 不打印log
         */
        NONE,
        /**
         * 只打印请求信息
         */
        REQUEST,
        /**
         * 只打印响应信息
         */
        RESPONSE,
        /**
         * 所有数据全部打印
         */
        ALL
    }

}
