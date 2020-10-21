package com.caowj.lib_utils

import com.caowj.lib_utils.digest.DigestAlgorithm
import com.caowj.lib_utils.file.Charsets
import java.io.*
import java.nio.charset.Charset
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*

/**
 * 信息摘要工具类
 */
object DigestUtil {

    /**
     * 生成摘要。
     *
     * @param input 需要生成摘要从中读取内容的InputStream，不能是null
     * @param algorithm 生成摘要的算法，[DigestAlgorithm]，不能是null
     *
     * @return 给定摘要算法计算后的字节数组
     *
     * @see [DigestAlgorithm.SHA224]
     * @see [DigestAlgorithm.SHA256]
     * @see [DigestAlgorithm.SHA384]
     * @see [DigestAlgorithm.SHA512]
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digest(input: InputStream, algorithm: DigestAlgorithm): String {
        val md = MessageDigest.getInstance(algorithm.getAlgorithm())
        (if (input is DigestInputStream) input else DigestInputStream(input, md)).use { dis ->
            val buffer = ByteArray(512 * 1024)
            var count = dis.read(buffer)
            while (count > -1) {
                count = dis.read(buffer)
            }
            val digests = dis.messageDigest.digest()
            val appender = StringBuilder()
            digests.forEach {
                appender.append(String.format("%02X", it))
            }
            return appender.toString().toLowerCase(Locale.CHINA)
        }
    }

    /**
     * 生成摘要。
     *
     * @param file 生成摘要的文件对象，不能是null
     * @param algorithm 生成摘要的算法，[DigestAlgorithm]，不能是null
     *
     * @return 给定摘要算法计算后的字节数组
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digest(file: File, algorithm: DigestAlgorithm): String {
        require(file.exists()) { "找不到指定文件！" }
        require(file.canRead()) { "文件内容无法读取！" }
        val fis = FileInputStream(file)
        return digest(fis, algorithm)
    }

    /**
     * 生成摘要。
     *
     * @param input 生成摘要的String，不能是null
     * @param charset 编码集
     * @param algorithm 生成摘要的算法，[DigestAlgorithm]，不能是null
     *
     * @return 给定摘要算法计算后的字节数组
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digest(input: String, charset: Charset, algorithm: DigestAlgorithm): String {
        return digest(ByteArrayInputStream(input.toByteArray(Charsets.toCharset(charset))), algorithm)
    }

    /**
     * 生成摘要。
     *
     * @param input 生成摘要的String，不能是null
     * @param charset 编码集
     * @param algorithm 生成摘要的算法，[DigestAlgorithm]，不能是null
     *
     * @return 给定摘要算法计算后的字节数组
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digest(input: String, charset: String, algorithm: DigestAlgorithm): String {
        return digest(ByteArrayInputStream(input.toByteArray(Charsets.toCharset(charset))), algorithm)
    }

    /**
     * 生成摘要。
     *
     * @param input 生成摘要的String，不能是null
     * @param algorithm 生成摘要的算法，[DigestAlgorithm]，不能是null
     *
     * @return 给定摘要算法计算后的字节数组
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digest(input: String, algorithm: DigestAlgorithm): String {
        return digest(ByteArrayInputStream(input.toByteArray(Charset.defaultCharset())), algorithm)
    }


    /**
     * 通过MD5算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容的InputStream，不能是null
     *
     * @return MD5算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestMD5(input: InputStream): String {
        return digest(input, DigestAlgorithm.MD5)
    }

    /**
     * 通过MD5算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容String，不能是null
     * @param charset 编码集，不能null
     *
     * @return MD5算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestMD5(input: String, charset: Charset = Charset.defaultCharset()): String {
        println("charset: $charset")
        return digest(
                ByteArrayInputStream(input.toByteArray(Charsets.toCharset(charset))),
                DigestAlgorithm.MD5
        )
    }

    /**
     * 通过MD5算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容String，不能是null
     * @param charset 编码集，不能null
     *
     * @return MD5算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestMD5(input: String, charset: String = Charset.defaultCharset().name()): String {
        return digest(
                ByteArrayInputStream(input.toByteArray(Charsets.toCharset(charset))),
                DigestAlgorithm.MD5
        )
    }

    /**
     * 通过MD5算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容String，不能是null
     *
     * @return MD5算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestMD5(input: String): String {
        return digest(
                ByteArrayInputStream(input.toByteArray(Charset.defaultCharset())),
                DigestAlgorithm.MD5
        )
    }

    /**
     * 通过MD5算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容的File，不能为null
     *
     * @return MD5算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestMD5(input: File): String {
        require(input.exists()) { "找不到指定文件！" }
        require(input.canRead()) { "文件内容无法读取！" }
        return digest(FileInputStream(input), DigestAlgorithm.MD5)
    }

    /**
     * 通过SHA1算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容的InputStream，不能是null
     *
     * @return SHA1算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestSHA1(input: InputStream): String {
        return digest(input, DigestAlgorithm.SHA1)
    }

    /**
     * 通过SHA1算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容的String，不能是null
     * @param charset 编码集，不能null
     *
     * @return SHA1算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestSHA1(input: String, charset: Charset = Charset.defaultCharset()): String {
        return digest(
                ByteArrayInputStream(input.toByteArray(Charsets.toCharset(charset))),
                DigestAlgorithm.SHA1
        )
    }

    /**
     * 通过SHA1算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容的String，不能是null
     * @param charset 编码集，不能null
     *
     * @return SHA1算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestSHA1(input: String, charset: String = Charset.defaultCharset().name()): String {
        println("charset: $charset")
        return digest(
                ByteArrayInputStream(input.toByteArray(Charsets.toCharset(charset))),
                DigestAlgorithm.SHA1
        )
    }

    /**
     * 通过SHA1算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容的String，不能是null
     *
     * @return SHA1算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestSHA1(input: String): String {
        return digest(
                ByteArrayInputStream(input.toByteArray(Charset.defaultCharset())),
                DigestAlgorithm.SHA1
        )
    }

    /**
     * 通过SHA1算法生成摘要。
     *
     * @param input 需要生成摘要从中读取内容的File，不能为null
     *
     * @return SHA1算法计算后的结果
     */
    @Throws(IOException::class)
    @JvmStatic
    fun digestSHA1(input: File): String {
        require(input.exists()) { "找不到指定文件！" }
        require(input.canRead()) { "文件内容无法读取！" }
        return digest(FileInputStream(input), DigestAlgorithm.SHA1)
    }
}