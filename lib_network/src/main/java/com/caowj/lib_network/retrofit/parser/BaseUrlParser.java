package com.caowj.lib_network.retrofit.parser;

import android.text.TextUtils;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;

/**
 * @author : yuanbingbing
 * @since : 2018/9/6 09:30
 */
public class BaseUrlParser implements HttpUrlParser {

    private LruCache<String, String> nCache;
    private HttpUrl nBaseUrl;
    private int nPathSize;


    public BaseUrlParser(HttpUrl baseUrl) {
        this.nCache = new LruCache<>(100);
        nBaseUrl = baseUrl;
        nPathSize = baseUrl.pathSize();
        List<String> baseUrlPathSegments = baseUrl.pathSegments();
        if ("".equals(baseUrlPathSegments.get(baseUrlPathSegments.size() - 1))) {
            this.nPathSize -= 1;
        }
    }

    @Override
    public HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl sourceUrl) {
        if (null == domainUrl) return sourceUrl;

        HttpUrl.Builder builder = sourceUrl.newBuilder();

        if (TextUtils.isEmpty(nCache.get(getKey(domainUrl, sourceUrl)))) {
            //清空原始URL中的Path路径
            for (int i = 0; i < sourceUrl.pathSize(); i++) {
                //当删除了上一个 index, PathSegment 的 item 会自动前进一位, 所以 remove(0) 就好
                builder.removePathSegment(0);
            }

            //添加将要替换的DomainUrl的Path
            List<String> newPathSegments = new ArrayList<>();
            newPathSegments.addAll(domainUrl.encodedPathSegments());

            //获取BaseURL路径之后的Path
            if (sourceUrl.pathSize() > nPathSize) {
                List<String> encodedPathSegments = sourceUrl.encodedPathSegments();
                for (int i = nPathSize; i < encodedPathSegments.size(); i++) {
                    newPathSegments.add(encodedPathSegments.get(i));
                }
            } else if (sourceUrl.pathSize() < nPathSize) {
                throw new IllegalArgumentException(String.format("Your final path is %s, but the baseUrl is %s",
                        sourceUrl.scheme() + "://" + sourceUrl.host() + sourceUrl.encodedPath(),
                        nBaseUrl.scheme() + "://" + nBaseUrl.host() + nBaseUrl.encodedPath()));
            }

            //将SourceUrl中除了BaseUrl的Path,添加目标路径中
            for (String PathSegment : newPathSegments) {
                builder.addEncodedPathSegment(PathSegment);
            }
        } else {
            builder.encodedPath(nCache.get(getKey(domainUrl, sourceUrl)));
        }

        HttpUrl httpUrl = builder
                .scheme(domainUrl.scheme())
                .host(domainUrl.host())
                .port(domainUrl.port())
                .build();

        //缓存路径
        if (TextUtils.isEmpty(nCache.get(getKey(domainUrl, sourceUrl)))) {
            nCache.put(getKey(domainUrl, sourceUrl), httpUrl.encodedPath());
        }
        return httpUrl;
    }

    private String getKey(HttpUrl domainUrl, HttpUrl sourceUrl) {
        return domainUrl.encodedPath() + sourceUrl.encodedPath() + nPathSize;
    }
}
