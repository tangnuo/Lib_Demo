package com.caowj.lib_utils.file.filter

import com.caowj.lib_utils.file.IOCase
import java.io.File
import java.io.Serializable


/**
 * 文件名前缀过滤器。
 *
 *
 * 例如：打印当前目录内"Test"开头的文件和目录。
 *
 * <pre>
 * val dir = File(".");
 * val files = dir.list( PrefixFileFilter("Test") );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *   println(files[i]);
 * }
 * </pre>
 *
 * @version $Id$
 * @see FileFilterUtil.prefixFileFilter
 * @see FileFilterUtil.prefixFileFilter
 * @since 1.0
 */
internal class PrefixFileFilter : AbstractFileFilter, Serializable {

    /**
     * 要搜索的文件前缀列表
     */
    private val prefixes: Array<String>

    /**
     * 忽略大小写
     */
    private val caseSensitivity: IOCase

    /**
     * 构造单个前缀的文件过滤器，大小写铭感可控。
     *
     * @param prefix  前缀，不为null
     * @param caseSensitivity 是否大小写敏感，null表示大小写敏感
     * @since 1.4
     */
    @JvmOverloads
    constructor(prefix: String, caseSensitivity: IOCase? = IOCase.SENSITIVE) {
        this.prefixes = arrayOf(prefix)
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    /**
     * 构造文件过滤器——一组前缀，大小写铭感
     *
     * @param prefixes 允许的前缀，不为null
     * @param caseSensitivity  处理大小写铭感，null表示大小写敏感[com.caowj.lib_utils.file.IOCase]
     * @throws IllegalArgumentException if the prefix is null
     * @since 1.4
     */
    @JvmOverloads
    constructor(prefixes: Array<String>, caseSensitivity: IOCase? = IOCase.SENSITIVE) {
        this.prefixes = prefixes.asList().toTypedArray()
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    /**
     * 构造单个前缀的文件过滤器，大小写铭感可控。
     *
     * @param prefixes 允许的前缀，不为null
     * @param caseSensitivity 处理大小写铭感，null表示大小写敏感[com.caowj.lib_utils.file.IOCase]
     * @since 1.4
     */
    @JvmOverloads
    constructor(prefixes: List<String>, caseSensitivity: IOCase? = IOCase.SENSITIVE) {
        this.prefixes = prefixes.toTypedArray()
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    /**
     * 检查文件名是否已前缀开始。
     *
     * @param file 检查的文件
     * @return true 若文件名以前缀命中的一个开始
     */
    override fun accept(file: File): Boolean {
        val name = file.name
        for (prefix in this.prefixes) {
            if (caseSensitivity.checkStartsWith(name, prefix)) {
                return true
            }
        }
        return false
    }

    /**
     * 检查文件名是否已前缀开始。
     *
     * @param file 文件目录
     * @param name 文件名
     * @return true 若文件名以前缀命中的一个开始
     */
    override fun accept(file: File, name: String): Boolean {
        for (prefix in prefixes) {
            if (caseSensitivity.checkStartsWith(name, prefix)) {
                return true
            }
        }
        return false
    }

    /**
     *
     * @return String 表示
     */
    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append(super.toString())
        buffer.append("(")
        if (prefixes != null) {
            for (i in prefixes.indices) {
                if (i > 0) {
                    buffer.append(",")
                }
                buffer.append(prefixes[i])
            }
        }
        buffer.append(")")
        return buffer.toString()
    }

    companion object {

        private const val serialVersionUID = 8533897440809599867L
    }

}