package com.caowj.lib_network.retrofit.converter;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by wangqian on 2019/8/22.
 */
public class NullOnEmptyConverterFactory extends Converter.Factory {

    private static final String TAG = "NullOnEmptyConverterFactory";

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
        return new Converter<ResponseBody, Object>() {
            @Override
            public Object convert(@NonNull ResponseBody body) throws IOException {
                if (body.contentLength() == 0) {
                    return null;
                }
                // 打log结果打出一个bug，这里使用body.string()打印结果，导致数据被拦截，无法往后传递
//                 LogUtils.d(TAG + "--responseBodyConverter--convert  data=" + body.string());
                return delegate.convert(body);
            }
        };
    }
}
