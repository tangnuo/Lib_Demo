package com.caowj.lib_network.retrofit.log;

import okhttp3.MediaType;

/**
 * @author : yuanbingbing
 * @since : 2018/9/11 16:37
 */
public class HttpContentTypeUtil {

    /**
     * 是否可以解析
     *
     * @param mediaType {@link okhttp3.MediaType}
     * @return {@code true} 为可以解析
     */
    public static boolean isParseable(MediaType mediaType) {
        return isText(mediaType) || isPlain(mediaType)
                || isJson(mediaType) || isForm(mediaType)
                || isHtml(mediaType) || isXml(mediaType);
    }

    public static boolean isText(MediaType mediaType) {
        if (mediaType == null || mediaType.type() == null) return false;
        return mediaType.type().equals("text");
    }

    public static boolean isPlain(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) return false;
        return mediaType.subtype().toLowerCase().contains("plain");
    }

    public static boolean isJson(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) return false;
        return mediaType.subtype().toLowerCase().contains("json");
    }

    public static boolean isXml(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) return false;
        return mediaType.subtype().toLowerCase().contains("xml");
    }

    public static boolean isHtml(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) return false;
        return mediaType.subtype().toLowerCase().contains("html");
    }

    public static boolean isForm(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) return false;
        return mediaType.subtype().toLowerCase().contains("x-www-form-urlencoded");
    }
}
