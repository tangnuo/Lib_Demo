package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.Serializable
import java.util.*

/**
 * 提供与一组过滤器的条件逻辑或(OR)运算的[java.io.FileFilter]。这个过滤器在
 * 过滤器列表中任一过滤器均返回true的情况下返回true。反之，返回false。文件过滤器的检查在其中一个
 * 过滤器返回true时停止。
 *
 * @version $Id$
 * @see FileFilterUtil.or
 * @since 1.0
 */
internal class OrFileFilter : AbstractFileFilter, ConditionalFileFilter, Serializable {
    override var fileFilters: MutableList<IOFileFilter> = arrayListOf()
        set(value) {
            field.clear()
            field.addAll(value)
        }

    /**
     * @since 1.1
     */
    constructor()

    /**
     * @param filters 文件过滤器，传入null被忽略
     * @since 1.1
     */
    constructor(filters: List<IOFileFilter>?) {
        filters?.let { this.fileFilters = ArrayList(filters) }
    }

    /**
     *
     * @param filter1 第一个filter, 不为null
     * @param filter2  第二个filter,  不为null
     * @throws IllegalArgumentException if either filter is null
     */
    constructor(filter1: IOFileFilter, filter2: IOFileFilter) {
        this.fileFilters = ArrayList(2)
        addFileFilter(filter1)
        addFileFilter(filter2)
    }

    /**
     * {@inheritDoc}
     */
    override fun addFileFilter(ioFileFilter: IOFileFilter) {
        this.fileFilters.add(ioFileFilter)
    }

    /**
     * {@inheritDoc}
     */
    override fun removeFileFilter(ioFileFilter: IOFileFilter): Boolean {
        return this.fileFilters.remove(ioFileFilter)
    }

    /**
     * {@inheritDoc}
     */
    override fun accept(file: File): Boolean {
        for (fileFilter in fileFilters) {
            if (fileFilter.accept(file)) {
                return true
            }
        }
        return false
    }

    /**
     * {@inheritDoc}
     */
    override fun accept(file: File, name: String): Boolean {
        for (fileFilter in fileFilters) {
            if (fileFilter.accept(file, name)) {
                return true
            }
        }
        return false
    }

    /**
     * Provide a String representation of this file filter.
     *
     * @return a String representation
     */
    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append(super.toString())
        buffer.append("(")
        if (fileFilters != null) {
            for (i in fileFilters.indices) {
                if (i > 0) {
                    buffer.append(",")
                }
                val filter = fileFilters[i]
                buffer.append(filter.toString())
            }
        }
        buffer.append(")")
        return buffer.toString()
    }

    companion object {

        private const val serialVersionUID = 5767770777065432721L
    }

}
