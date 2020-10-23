package com.caowj.lib_network.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * <pre>
 *     作者：Caowj
 *     邮箱：caoweijian@kedacom.com
 *     日期：2020/4/7 16:39
 * </pre>
 */
public class FileUtils {

    /**
     * 将字符串写入到文本文件中
     * https://blog.csdn.net/u012246458/article/details/83063112
     *
     * @param content
     */
    public static void writeTxtToFile(String content) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Network/requestTxt/requestBody.txt";
        // 每次写入时，都换行写
//        String strContent = "请求日期：" + DatetimeUtil.getNow() + "\r\n" + content + "\r\n\r\n";
        String strContent = "请求日期：" + getNow() + "\r\n" + content + "\r\n\r\n";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.w("caowj", e.getMessage());
        }
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    private static String getNow() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(d);
    }
//
//    //读取指定目录下的所有TXT文件的文件内容
//    public static String getFileContent(File file) {
//        String content = "";
//        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
//            if (file.getName().endsWith("txt")) {//文件格式为""文件
//                try {
//                    InputStream instream = new FileInputStream(file);
//                    if (instream != null) {
//                        InputStreamReader inputreader
//                                = new InputStreamReader(instream, "UTF-8");
//                        BufferedReader buffreader = new BufferedReader(inputreader);
//                        String line = "";
//                        //分行读取
//                        while ((line = buffreader.readLine()) != null) {
//                            content += line + "\n";
//                        }
//                        instream.close();//关闭输入流
//                    }
//                } catch (java.io.FileNotFoundException e) {
//                    Log.d("TestFile", "The File doesn't not exist.");
//                } catch (IOException e) {
//                    Log.d("TestFile", e.getMessage());
//                }
//            }
//        }
//        return content;
//    }

    public static String parseParams(RequestBody body) {
        try {
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
                text = text.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                text = URLDecoder.decode(text, convertCharset(charset));
            }

            return TextUtil.jsonFormat(text);
        } catch (IOException e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private static String convertCharset(Charset charset) {
        String s = charset.toString();
        int i = s.indexOf("[");
        if (i == -1)
            return s;
        return s.substring(i + 1, s.length() - 1);
    }

}
