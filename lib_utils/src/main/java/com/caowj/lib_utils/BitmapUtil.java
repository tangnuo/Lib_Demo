package com.caowj.lib_utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.view.View.MeasureSpec.UNSPECIFIED;

/**
 * Created by wangqian on 2018/6/21.
 */

public class BitmapUtil {
    /**
     * 通过资源ID获取Drawable实例
     * @param context
     * @param resId
     * @return
     */
    public static Drawable resToDrawable(Context context, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(resId);
        }
        return context.getResources().getDrawable(resId);
    }

    /**
     * Bitmap 转为 byte 数组
     * @param b
     * @return
     */
    public static byte[] bitmapToByte(Bitmap b) {
        if (b == null) {
            return null;
        }
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }
    /**
     * byte数据转为bitmap
     * @param b
     * @return
     */
    public static Bitmap byteToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    /**
     * Drawable转为Bitmap
     * @param d
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable d) {
        return d == null ? null : ((BitmapDrawable) d).getBitmap();
    }
    /**
     * Bitmap转为Drawable
     * @param context
     * @param b
     * @return
     */
    public static Drawable bitmapToDrawable(Context context, Bitmap b) {
        return b == null ? null : new BitmapDrawable(context.getResources(),b);
    }


    /**
     * byte数组转为Drawable
     * @param b
     * @return
     */
    public static Drawable byteToDrawable(Context context, byte[] b) {
        return bitmapToDrawable(context,byteToBitmap(b));
    }
    /**
     * view转为指定宽度和高度的Bitmap
     * @param view
     * @param width
     * @param height
     * @return
     */
    public static Bitmap convertViewToBitmap(View view, int width, int height) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, (width == UNSPECIFIED) ? UNSPECIFIED :
                        View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, (height == UNSPECIFIED) ? UNSPECIFIED :
                        View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    /**
     *  view转为默认宽度和高度的Bitmap
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        return convertViewToBitmap(view, UNSPECIFIED, UNSPECIFIED);
    }

    /**
     * 压缩Bitmap到kBSize
     * @param bmp
     * @param kBSize 压缩到的尺寸，单位是KB
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bmp, int kBSize) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > kBSize) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        closeSafe(baos);
        closeSafe(isBm);
        return bitmap;
    }

    /**
     * 保存Bitmap到指定的uri
     * @param context
     * @param bitmap
     * @param uri
     * @param compressFormat
     * @param compressQuality
     * @throws FileNotFoundException
     */
    public static void saveBitmapToUri(Context context, Bitmap bitmap, Uri uri,
                                            Bitmap.CompressFormat compressFormat,
                                            int compressQuality)
            throws FileNotFoundException {
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(compressFormat, compressQuality, outputStream);
        } finally {
            closeSafe(outputStream);
        }
    }

    /**
     *  保存Bitmap到指定的absolutePath
     * @param bitmap
     * @param absolutePath 要保存的文件的绝对路径
     * @param compressFormat
     * @param compressQuality Hint to the compressor, 0-100. 0 meaning compress for
                          small size, 100 meaning compress for max quality. Some
                      formats, like PNG which is lossless, will ignore the
                    quality setting
     * @throws FileNotFoundException
     */
    public static void saveBitmap(Bitmap bitmap, String absolutePath,
                                       Bitmap.CompressFormat compressFormat,
                                       int compressQuality)
            throws FileNotFoundException {
        File file = new File(absolutePath);
        if(file.exists()){
          file.delete();
        }

        OutputStream outputStream = null;
        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            bitmap.compress(compressFormat, compressQuality, outputStream);
        } catch (IOException e){
            Log.e("BitmapUtil",e.getMessage());
        } finally{
            closeSafe(outputStream);
        }
    }

    private static void closeSafe(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }


}
