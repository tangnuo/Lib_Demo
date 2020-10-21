package com.caowj.lib_utils.file

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 此类中包含Java平台的每个实现。
 *
 *  Java文档[Standard charsets](https://docs.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html):
 *
 *
 * <cite>Java平台的每个实现均需要支持下来的字符编码。可以通过查阅发信发新版本文档来检查在使用时使用的编码是否支持。</cite>
 *
 *
 *
 * <br>`US-ASCII`
 *  7位ASCII, a.k.a. ISO646-US, a.k.a.Unicode字符集中基本拉丁字符部分
 *  </br>
 * <br>`ISO-8859-1`
 *  拉丁字母No. 1, a.k.a. ISO-LATIN-1.
 *  </br>
 * <br>`UTF-8`
 * 8位Unicode转换格式
 * </br>
 * <br> `UTF-16BE`
 * 16位Unicode转换格式，大尾数法字节顺序
 * </br>
 * <br>`UTF-16LE`
 * 16位Unicode转换格式，小尾数法字节顺序
 * </br>
 * <br>`UTF-16`
 * 16位Unicode转换格式，由强制的初始字节顺序标记指定的字节顺序(或者输入顺序, 大尾数法输出)
 * </br>
 *
 *
 * @version $Id$
 * @see [Standard charsets](https://docs.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html)
 *
 * @since 2.3
 */
object Charsets {
    // 类中只包含需要的编码格式。保证在Java平台上加载的准确性。


    /**
     * 构件公认的字符集名称映射，适用于所有的Java平台。
     *
     * Java文档连接 [Standard charsets](https://docs.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html):
     *
     *
     * @return  不可修改的，大小写敏感的标准字符集名称map
     * @see Charset.availableCharsets
     * @since 2.5
     */
    fun requiredCharsets(): SortedMap<String, Charset> {
        // maybe cache?
        val m = TreeMap<String, Charset>(String.CASE_INSENSITIVE_ORDER)
        m[StandardCharsets.ISO_8859_1.name()] = StandardCharsets.ISO_8859_1
        m[StandardCharsets.US_ASCII.name()] = StandardCharsets.US_ASCII
        m[StandardCharsets.UTF_16.name()] = StandardCharsets.UTF_16
        m[StandardCharsets.UTF_16BE.name()] = StandardCharsets.UTF_16BE
        m[StandardCharsets.UTF_16LE.name()] = StandardCharsets.UTF_16LE
        m[StandardCharsets.UTF_8.name()] = StandardCharsets.UTF_8
        return Collections.unmodifiableSortedMap(m)
    }

    /**
     * 返回给定字符集，若给定null则返回平台默认字符集。
     *
     * @param charset 字符集或null
     * @return 返回给定字符集，null则返回默认字符集
     */
    fun toCharset(charset: Charset?): Charset {
        return charset ?: Charset.defaultCharset()
    }

    /**
     * 返回给定字符集，若给定null则返回平台默认字符集。
     *
     * @param charset 字符集或null
     * @return 返回给定字符集，null则返回默认字符集
     * @throws java.nio.charset.UnsupportedCharsetException If the named charset is unavailable
     */
    fun toCharset(charset: String?): Charset {
        return if (charset == null) Charset.defaultCharset() else Charset.forName(charset)
    }
}
