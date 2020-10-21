package com.caowj.lib_utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import android.util.Log

import com.caowj.lib_utils.file.IOUtil
import org.apache.commons.validator.routines.UrlValidator
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset

/**
 * 编解码工具类——Base64等。
 */
object CodecUtil {
    /**
     * base64编码/解码默认标志位
     */
    const val FLAG_BASE64_DEFAULT = Base64.DEFAULT

    /**
     * 编码标志位：移除所有行终止符。
     * 解码后的内容在一长行上。
     */
    const val FLAG_BASE64_NO_WRAP = Base64.NO_WRAP

    /**
     * 编码/解码标志位：在内容中使用 ‘-’ 或 ‘_’ 取代内容中的 '+' 和 '/' 符号。
     */
    const val FLAG_BASE64_URL_SAFE = Base64.URL_SAFE

    /**
     * 编码标志位：制定每行结尾使用CRLF终止符而非只是LF终止符。
     * 若在flags中设置了FLAG_BASE64_NO_WRAP则无效。
     */
    const val FLAG_BASE64_CRLF = Base64.CRLF

    /**
     * 传递给输出流(OutputStream)的的标志位，表明OutputStream在在编码的时候不应该关闭。
     */
    const val FLAG_BASE64_NO_CLOSE = Base64.NO_CLOSE

    /**
     * 编码标志位：移除编码后内容默认的 ‘=’ 号。
     */
    const val FLAG_BASE64_NO_PADDING = Base64.NO_PADDING

    /**
     * 合法的协议头
     */
    @JvmStatic
    private val VALID_SCHEMES = arrayOf("http", "https", "file", "ftp")

    /**
     * 用Base64编码从[InputStream]中读取的内容。
     *
     * @param input 从中读取内容的[InputStream]，不能null
     * @param flags 编码标志位，此值是 [FLAG_BASE64_DEFAULT], [FLAG_BASE64_CRLF],
     * [FLAG_BASE64_NO_CLOSE], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_URL_SAFE]中的值。
     *
     * @return 编码后的字符串
     * @throws IOException 发生IO错误
     */
    @Throws(IOException::class)
    @JvmOverloads
    @JvmStatic
    fun base64Encode(input: InputStream, flags: Int = FLAG_BASE64_DEFAULT): String {
        IOUtil.toBufferedInputStream(input, 1024 * 512).use { bis ->
            val buffer = ByteArray(4 * 1024)
            val baos = ByteArrayOutputStream()
            Base64OutputStream(baos, flags).use { base64Stream ->
                var total = 0
                var count = bis.read(buffer)
                while (count > -1) {
                    total += count
                    base64Stream.write(buffer, 0, count)
                    count = bis.read(buffer)
                }
                base64Stream.write(buffer, 0, 1)
                base64Stream.flush()
                val base64Ret = baos.toByteArray()
                baos.close()
                return String(base64Ret)
            }
        }
    }

    /**
     * Base64编码文件。
     *
     * @param file 使用Base64编码的文件对象，不能null
     *
     * @return 编码后的字符串
     * @throws IOException 发生IO错误
     * @throws FileNotFoundException 文件不存在
     * @throws IllegalArgumentException 文件无法读取
     */
    @Throws(IOException::class, FileNotFoundException::class, IllegalArgumentException::class)
    @JvmStatic
    fun base64Encode(file: File): String {
        if (!file.exists()) {
            throw FileNotFoundException("$file 找不到指定文件！")
        }
        require(file.canRead()) { "文件无法读取！" }
        return base64Encode(FileInputStream(file))
    }

    /**
     * 将String字符串进行Base64编码。
     *
     * @param input 需要编码的字符串内容，不能为null
     * @param flags 标志位，此值是 [FLAG_BASE64_DEFAULT], [FLAG_BASE64_CRLF],
     * [FLAG_BASE64_NO_CLOSE], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_URL_SAFE]中的值。
     *
     * @return 编码后的字符串
     */
    @JvmOverloads
    @JvmStatic
    fun base64Encode(input: String, flags: Int = FLAG_BASE64_DEFAULT): String {
        return String(Base64.encode(input.trim().toByteArray(Charset.defaultCharset()), flags))
    }

    /**
     * 对URL进行Base64编码。
     *
     * @param url 要编码的URL地址，不能为null
     *
     * @return 编码后的URL地址
     * @throws  MalformedURLException URL基本格式不准确
     * @throws IOException 发生IO错误
     */
    @Throws(MalformedURLException::class, IOException::class)
    @JvmStatic
    fun base64Encode(url: URL): String {
        val validator = UrlValidator(VALID_SCHEMES)
        try {
            if (validator.isValid(url.toString())) {
                return base64Encode(url.toString())
            }
        } catch (e: Exception) {
            return ""
        }
        return ""
    }


    /**
     * 将bitmap进行编码。
     *
     * @param bitmap 进行编码的Bitmap，不能是null
     * @param flags 标志位，值是[FLAG_BASE64_DEFAULT], [FLAG_BASE64_CRLF],
     * [FLAG_BASE64_NO_CLOSE], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_URL_SAFE]中一个。是
     *
     * @return 编码后返回的字符串
     * @throws IOException 发生IO错误
     */
    @Throws(IOException::class)
    @JvmOverloads
    @JvmStatic
    fun base64Encode(bitmap: Bitmap, flags: Int = FLAG_BASE64_DEFAULT): String {
        val bitmapOS = ByteArrayOutputStream()
        if (bitmap.compress(Bitmap.CompressFormat.WEBP, 100, bitmapOS)) {
            bitmapOS.use { baos ->
                ByteArrayOutputStream().use { retOS ->
                    Base64OutputStream(retOS, flags).use { base64OS ->
                        IOUtil.writeChunked(baos.toByteArray(), base64OS)
                        val retArray = retOS.toByteArray()
                        return String(retArray)
                    }
                }
            }
        }
        return ""
    }

    /**
     * 将Base64的编码值解码为图片(Bitmap)。
     *
     * @param code 编码内容，不能是null，也不可以是空串
     * @param flags 控制对编码字串输出时的字符串特性，是 [FLAG_BASE64_DEFAULT], [FLAG_BASE64_CRLF],
     * [FLAG_BASE64_NO_CLOSE], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_URL_SAFE]中的值。
     *
     * @return 解码后的图片对象
     * @throws IOException 发生IO错误
     * @throws IllegalArgumentException 编码内容为空字符串
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    @JvmOverloads
    @JvmStatic
    fun base64DecodeBitmap(code: String, flags: Int = FLAG_BASE64_DEFAULT): Bitmap {
        require(!TextUtils.isEmpty(code)) { "编码内容不可以是空字符串" }
        ByteArrayInputStream(code.toByteArray(Charset.defaultCharset())).use { bais ->
            Base64InputStream(bais, flags).use { base64Stream ->
                IOUtil.toBufferedInputStream(base64Stream, 1 * 1024 * 1024).use { bufferedIS ->
                    val buffer = ByteArray(1024 * 512)
                    val byteArray = arrayListOf<Byte>()
                    var count = IOUtil.read(bufferedIS, buffer)
                    while (count > 0) {
                        byteArray.addAll(buffer.toList())
                        count = IOUtil.read(bufferedIS, buffer)
                    }

                    return BitmapFactory.decodeByteArray(byteArray.toByteArray(), 0, byteArray.size)
                }
            }
        }
    }

    /**
     * 将Base64的编码值解码为图片(Bitmap)，并将图片输出到指定图片文件。
     *
     * @param code 编码内容，不能是null，也不可以是空串
     * @param dest 目标图片文件，不能为null
     * @param flags 标志位，此值是 [FLAG_BASE64_DEFAULT], [FLAG_BASE64_CRLF],
     * [FLAG_BASE64_NO_CLOSE], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_URL_SAFE]中的值。是
     *
     * @return 解码后的图片对象
     * @throws IOException 发生IO错误
     * @throws IllegalArgumentException 编码内容为空字符串
     */
    @Throws(IllegalArgumentException::class)
    @JvmOverloads
    @JvmStatic
    fun base64DecodeBitmapToFile(code: String, dest: File, flags: Int = FLAG_BASE64_DEFAULT): File? {
        val parent = File(dest.parent)
        if (!parent.exists() && !parent.mkdirs()) {
            Log.e("CodecUtil", "$parent 目录创建失败！")
            return null
        }

        if (!dest.exists() && !dest.createNewFile()) {
            require(dest.exists()) { "无法创建文件！" }
        }
        require(dest.canWrite()) { "无法写文件！" }
        require(!TextUtils.isEmpty(code)) { "图片编码内容不能为空字符串！" }

        try {
            ByteArrayInputStream(code.toByteArray(Charsets.UTF_8)).use { bais ->
                Base64InputStream(bais, flags).use { base64IS ->
                    IOUtil.toBufferedInputStream(base64IS).use { bufferIS ->
                        FileOutputStream(dest, true).use { fos ->
                            BufferedOutputStream(fos, 512 * 1024).use { bos ->
                                val buffer = ByteArray(1 * 1024 * 1024)
                                var count = IOUtil.read(bufferIS, buffer)
                                while (count > 0) {
                                    bos.write(buffer, 0, count)
                                    count = IOUtil.read(bufferIS, buffer)
                                }
                                bos.flush()
                                return dest
                            }
                        }
                    }
                }
            }
        } catch (err: IOException) {
            return null
        }
    }

    /**
     * 对code内容进行Base64解码操作。
     *
     * @param code Bse64编码内容
     * @param flags 控制输出的解码字符串特性，回车等特殊处理. 是 [FLAG_BASE64_DEFAULT], [FLAG_BASE64_CRLF],
     * [FLAG_BASE64_NO_CLOSE], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_URL_SAFE]中的值。
     *
     * @return 解码后内容
     */
    @JvmOverloads
    @JvmStatic
    fun base64DecodeString(code: String, flags: Int = FLAG_BASE64_DEFAULT): String {
        return String(Base64.decode(code, flags))
    }

    /**
     * 将编码内容解码为URL，若解码后的URL非法，返回null。
     *
     * @param code 编码的内容
     *
     * @return 合法的URL对象，若解码后url非法，返回null
     */
    @Throws(MalformedURLException::class)
    @JvmStatic
    fun base64DecodeURL(code: String): URL? {
        val decoded = base64DecodeString(code)
        return if (UrlValidator(VALID_SCHEMES).isValid(decoded)) URL(decoded) else null
    }

    /**
     * 将code进行解码，并将解码后的内容写入到指定文件。
     *
     * @param code 需要解码的内容，不能是null
     * @param dest 写入解码内容的文件，不能是null
     *
     * @return 准确写入解码内容的文件对象
     * @throws FileNotFoundException  文件创建失败
     * @throws IllegalArgumentException 无法写文件
     * @throws IOException 发生IO错误
     */
    @Throws(FileNotFoundException::class, IllegalArgumentException::class, IOException::class)
    @JvmStatic
    fun base64DecodeFile(code: String, dest: File): File? {
        if (!dest.exists()) {
            var count = 0
            while (!dest.createNewFile() && count < 3) {
                count++
            }
            if (!dest.exists()) {
                throw FileNotFoundException("无法创建指定文件！")
            }
            dest.setWritable(true)
        }
        require(dest.canWrite()) { "文件无法写入！" }
        FileOutputStream(dest).use { fos ->
            OutputStreamWriter(fos).use { osw ->
                val decoded = base64DecodeString(code)
                IOUtil.write(decoded, osw)
                return dest
            }
        }
    }

    /**
     * 读取编码内容的输入流，将内容解码并返回。
     *
     * @param input 从中读取编码的InputStream
     * @param flags 标志位，是 [FLAG_BASE64_DEFAULT], [FLAG_BASE64_CRLF],
     * [FLAG_BASE64_NO_CLOSE], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_NO_PADDING], [FLAG_BASE64_URL_SAFE]中的值。
     *
     * @return 返回解码后的内容
     */
    @Throws(IOException::class)
    @JvmOverloads
    @JvmStatic
    fun base64DecodeInputStream(input: InputStream, flags: Int = FLAG_BASE64_DEFAULT): String {
        Base64InputStream(input, flags).use { base64Stream ->
            IOUtil.toBufferedInputStream(base64Stream, 1 * 1024 * 1024).use { bis ->
                val mediateList = mutableListOf<Byte>()
                val buffer = ByteArray(512 * 1024)
                var count = IOUtil.read(bis, buffer)
                while (count > 0) {
                    mediateList.addAll(buffer.toList())
                    count = IOUtil.read(bis, buffer)
                }
                val retArray = mediateList.toByteArray()
                return String(retArray)
            }
        }
    }
}