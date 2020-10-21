package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.Serializable

/**
 * 大小过滤器——基于给定的大小，过滤出等于，大于或小于给定值的文件。
 *
 * 例如：打印当前目录中大小大于1MB的文件及目录。
 *
 * <pre>
 * val dir = File(".")
 * val files = dir.list( SizeFileFilter(1024 * 1024) )
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *   println(files[i]);
 * }
 * </pre>
 *
 * @see FileFilterUtil.sizeFileFilter
 * @see FileFilterUtil.sizeFileFilter
 * @see FileFilterUtil.sizeRangeFileFilter
 * @since 1.2
 */
internal class SizeFileFilter
/**
 * 基于一个阀值点构造文件过滤器。
 *
 * @param size 过滤器的阀值
 * @param acceptLarger  若是true, 返回包含等于或大于size的文件，反之小于(不包含等于)
 * @throws IllegalArgumentException if the size is negative
 */
@JvmOverloads constructor(
        /**
         * 阀值
         */
        private val size: Long,
        /**
         * 是否接收大于size或小于size的文件
         */
        private val acceptLarger: Boolean = true) : AbstractFileFilter(), Serializable {

    init {
        require(size >= 0) { "The size must be non-negative" }
    }

    //-----------------------------------------------------------------------

    /**
     * 检查文件大小是有利。
     *
     * 若需要大小小于等于阀值的文件，文件不会被接受。若需要大小大于等于阀值的文件，文件将被接受。
     *
     * @param file 检查的文件
     * @return true 若文件匹配
     */
    override fun accept(file: File): Boolean {
        val smaller = file.length() < size
        return acceptLarger != smaller
    }

    /**
     *
     * @return String 表示
     */
    override fun toString(): String {
        val condition = if (acceptLarger) ">=" else "<"
        return super.toString() + "(" + condition + size + ")"
    }

    companion object {

        private const val serialVersionUID = 7388077430788600069L
    }

}
