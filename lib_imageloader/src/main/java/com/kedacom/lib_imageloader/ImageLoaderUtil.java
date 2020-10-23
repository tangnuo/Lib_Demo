package com.kedacom.lib_imageloader;

import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.kedacom.lib_imageloader.glide.GlideImpl;

/**
 * @Dec ：图片加载工具（依赖第三方库）
 * 工厂模式，自由切换第三方库
 * @Author : Caowj
 * @Date : 2018/6/13 12:39
 */
public class ImageLoaderUtil {

    private static IImageLoader imageLoader;

    private ImageLoaderUtil() {
    }

    private static IImageLoader getInstance() {
        if (imageLoader == null) {
            synchronized (ImageLoaderUtil.class) {
                if (imageLoader == null) {
                    imageLoader = new GlideImpl();
//                    imageLoader = new PicassoImpl();
//                    imageLoader = new FrescoImpl();
                }
            }
        }
        return imageLoader;
    }


    /********************************************************/


    public static void loadCenterCrop(String url, ImageView view, int defaultResId) {
        imageLoader.load(view, url, defaultResId, defaultResId);
    }

    /**
     * 带加载异常图片
     */
    public static void loadCenterCrop(String url, ImageView view, int defaultResId, int errorResId) {
        imageLoader.load(view, url, defaultResId, defaultResId);
    }

    /**
     * 带监听处理
     */
    public static void loadCenterCrop(String url, ImageView view, RequestListener listener) {
        imageLoader.load(view, url, listener);
    }

    public static void loadNormal(String url, ImageView view) {
        imageLoader.load(view, url);
    }

    public static void loadResourceId(int url, ImageView view) {
        imageLoader.loadResourceId(view, url);
    }


}
