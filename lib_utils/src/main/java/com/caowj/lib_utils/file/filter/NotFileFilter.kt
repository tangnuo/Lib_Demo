package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.Serializable

/**
 * 这个过滤器产生给定过滤器的逻辑非(NOT)运算。
 *
 * @version $Id$
 * @see FileFilterUtil.notFileFilter
 * @since 1.0
 */
internal class NotFileFilter
/**
 * 构造一个非运算后的新文件过滤器
 *
 * @param filter 过滤器，不能null
 */
(private val filter: IOFileFilter) : AbstractFileFilter(), Serializable {


    /**
     * 返回过滤恶气Not运算后结果。
     *
     * @param file 检查的文件
     * @return true 若文件过滤器返回false
     */
    override fun accept(file: File): Boolean {
        return !filter.accept(file)
    }

    /**
     * 返回过滤恶气Not运算后结果。
     *
     * @param file 文件目录
     * @param name 文件名
     * @return true 若过滤器返回false
     */
    override fun accept(file: File, name: String): Boolean {
        return !filter.accept(file, name)
    }

    /**
     * @return String 表示
     */
    override fun toString(): String {
        return super.toString() + "(" + filter.toString() + ")"
    }

    companion object {

        private const val serialVersionUID = 6131563330944994230L
    }

}
