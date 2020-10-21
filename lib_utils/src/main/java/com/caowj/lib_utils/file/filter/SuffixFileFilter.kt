package com.caowj.lib_utils.file.filter

import com.caowj.lib_utils.file.IOCase
import java.io.File
import java.io.Serializable


/**
 * 文件后缀过滤器。用以检索特定后缀的文件。
 *
 * For example, to retrieve and print all `*.java` files in the current directory:
 * 例如： 检索打印当全部当前目录下所有`*.java`文件
 *
 * <pre>
 * val dir = File(".")
 * val files = dir.list( SuffixFileFilter(".java") )
 * for (int i = 0; i &lt; files.length; i++) {
 *   println(files[i]);
 * }
 * </pre>
 *
 * @version $Id$
 * @since 1.0
 */
internal class SuffixFileFilter : AbstractFileFilter, Serializable {

    /**
     * 搜索的文件后缀列表
     */
    private val suffixes: Array<String>

    /**
     * 是否大小写敏感
     */
    private val caseSensitivity: IOCase

    /**
     * 构造单个后缀名，大小写敏感的文件过滤器。
     *
     * @param suffix  允许的后缀，不为null
     * @param caseSensitivity  设置大小写敏感，null表示大小写敏感
     * @since 1.4
     */
    @JvmOverloads
    constructor(suffix: String, caseSensitivity: IOCase? = IOCase.SENSITIVE) {
        this.suffixes = arrayOf(suffix)
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    /**
     * 构造单个后缀名，大小写敏感的文件过滤器。
     *
     * @param suffixes  允许的后缀，不为null
     * @param caseSensitivity  设置大小写敏感，null表示大小写敏感
     * @since 1.4
     */
    @JvmOverloads
    constructor(suffixes: Array<String>, caseSensitivity: IOCase? = IOCase.SENSITIVE) {
        this.suffixes = suffixes.asList().toTypedArray()
        System.arraycopy(suffixes, 0, this.suffixes, 0, suffixes.size)
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    /**
     * 构造单个后缀名，大小写敏感的文件过滤器。
     *
     * @param suffixes  允许的后缀，不为null
     * @param caseSensitivity  设置大小写敏感，null表示大小写敏感
     * @since 1.4
     */
    @JvmOverloads
    constructor(suffixes: List<String>, caseSensitivity: IOCase? = IOCase.SENSITIVE) {
        this.suffixes = suffixes.toTypedArray()
        this.caseSensitivity = caseSensitivity ?: IOCase.SENSITIVE
    }

    /**
     * 检查文件名是否以后缀名结束。
     *
     * @param file 检查的文件
     * @return true 若文件以给定的后缀结尾
     */
    override fun accept(file: File): Boolean {
        val name = file.name
        for (suffix in this.suffixes) {
            if (caseSensitivity.checkEndsWith(name, suffix)) {
                return true
            }
        }
        return false
    }

    /**
     * 检查文件名是否以后缀名结束。
     *
     * @param file 文件目录
     * @param name 文件名
     * @return true 若文件以给定的后缀结尾
     */
    override fun accept(file: File, name: String): Boolean {
        for (suffix in this.suffixes) {
            if (caseSensitivity.checkEndsWith(name, suffix)) {
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
        for (i in suffixes.indices) {
            if (i > 0) {
                buffer.append(",")
            }
            buffer.append(suffixes[i])
        }
        buffer.append(")")
        return buffer.toString()
    }

    companion object {

        private const val serialVersionUID = -3389157631240246157L
    }

}
