package com.caowj.lib_network.retrofit.parser;

import android.text.TextUtils;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;

/**
 * @author : yuanbingbing
 * @since : 2018/9/6 09:11
 */
public class DomainUrlParser implements HttpUrlParser {

    private LruCache<String, String> nCache;


    public DomainUrlParser() {
        nCache = new LruCache(100);

    }

    @Override
    public HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl sourceUrl) {
        if (domainUrl == null) return sourceUrl;

        HttpUrl.Builder builder = sourceUrl.newBuilder();
        if (TextUtils.isEmpty(nCache.get(getKey(domainUrl, sourceUrl)))) {
            for (int i = 0; i < sourceUrl.pathSize(); i++) {
                builder.removePathSegment(0);
            }

            List<String> newPathSegments = new ArrayList<>();
            newPathSegments.addAll(domainUrl.encodedPathSegments());
            newPathSegments.addAll(sourceUrl.encodedPathSegments());

            for (String PathSegment : newPathSegments) {
                builder.addEncodedPathSegment(PathSegment);
            }
        } else {
            builder.encodedPath(nCache.get(getKey(domainUrl, sourceUrl)));
        }

        //构建新HttpUrl
        HttpUrl httpUrl = builder
                .scheme(domainUrl.scheme())
                .host(domainUrl.host())
                .port(domainUrl.port())
                .build();

        //缓存H
        if (TextUtils.isEmpty(nCache.get(getKey(domainUrl, sourceUrl)))) {
            nCache.put(getKey(domainUrl, sourceUrl), httpUrl.encodedPath());
        }
        return httpUrl;
    }


    private String getKey(HttpUrl domainUrl, HttpUrl sourceUrl) {
        return domainUrl.encodedPath() + sourceUrl.encodedPath();
    }
}
