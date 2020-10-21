package com.caowj.lib_utils.file.filter

import com.caowj.lib_utils.file.IOCase
import java.io.File
import java.io.Serializable


/**
 * 文件名过滤器。
 *
 * 例如：打印当前目录下名为"Test"的所有文件及目录。
 *
 * <pre>
 * val dir = File(".");
 * val files = dir.list( NameFileFilter("Test") );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *   println(files[i]);
 * }
 * </pre>
 *
 * @version $Id$
 * @see FileFilterUtil.nameFileFilter
 * @see FileFilterUtil.nameFileFilter
 * @since 1.0
 */
internal class NameFileFilter : AbstractFileFilter, Serializable {
    /**
     * 搜索的文件名
     */
    private val names: Array<String>
    /**
     * 大小写
     */
    private val caseSensitivity: IOCase

    /**
     * 构造一个名称过滤器，并且声明大小写敏感设置。
     *
     * @param name  允许的文件名，不为null
     * @param caseSensitivity  如何处理大小写，null表示大小写敏感
     */
    @JvmOverloads
    constructor(name: String, caseSensitivity: IOCase? = null) {
        this.names = arrayOf(name)
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    /**
     * 构造一个名称过滤器，并且声明大小写敏感设置。
     *
     * @param names 允许的文件名，不为null
     * @param caseSensitivity 如何处理大小写，null表示大小写敏感
     */
    @JvmOverloads
    constructor(names: Array<String>, caseSensitivity: IOCase? = null) {
        this.names = names.asList().toTypedArray()
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    /**
     * 构造一个名称过滤器，并且声明大小写敏感设置。
     *
     * @param names 允许的文件名，不为null
     * @param caseSensitivity 如何处理大小写，null表示大小写敏感
     */
    @JvmOverloads
    constructor(names: List<String>, caseSensitivity: IOCase? = null) {
        this.names = names.toTypedArray()
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    //-----------------------------------------------------------------------

    /**
     * 检查文件名的匹配。
     *
     * @param file 要检查的文件
     * @return true 若文件名匹配
     */
    override fun accept(file: File): Boolean {
        val name = file.name
        for (name2 in this.names) {
            if (caseSensitivity.checkEquals(name, name2)) {
                return true
            }
        }
        return false
    }

    /**
     * 检查文件名的匹配。
     *
     * @param dir 文件目录
     * @param name 文件名
     * @return true 若文件名匹配
     */
    override fun accept(dir: File, name: String): Boolean {
        for (name2 in names) {
            if (caseSensitivity.checkEquals(name, name2)) {
                return true
            }
        }
        return false
    }

    /**
     * @return String表示
     */
    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append(super.toString())
        buffer.append("(")
        for (i in names.indices) {
            if (i > 0) {
                buffer.append(",")
            }
            buffer.append(names[i])
        }
        buffer.append(")")
        return buffer.toString()
    }

    companion object {

        private const val serialVersionUID = 176844364689077340L
    }

}
