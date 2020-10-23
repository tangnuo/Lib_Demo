package com.caowj.lib_network.retrofit.parser;

import okhttp3.HttpUrl;

/**
 * @author : yuanbingbing
 * @since : 2018/9/5 16:45
 */
public interface HttpUrlParser {

    HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl sourceUrl);

}
