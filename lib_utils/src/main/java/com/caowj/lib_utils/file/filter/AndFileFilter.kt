package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.Serializable

/**
 *
 * 提供与一组文件过滤器进行逻辑与(AND)运算的[java.io.FileFilter]。这个过滤器在列表中所有过滤器
 * 都返回true的条件下返回true。反之，返回false。文件过滤器列表的检查也在遇到第一个返回false的
 * 过滤器时停止。
 *
 * @version $Id$
 * @see FileFilterUtil.and
 * @since 1.0
 */
internal class AndFileFilter : AbstractFileFilter, ConditionalFileFilter, Serializable {
    override var fileFilters: MutableList<IOFileFilter> = arrayListOf()
        set(value) {
            field.clear()
            field.addAll(value)
        }

    /**
     *
     * @since 1.1
     */
    constructor()

    /**
     * @param filters 一个IOFileFilter实例列表(List), copied, 传入null则忽略
     * @since 1.1
     */
    constructor(filters: List<IOFileFilter>?) {
        filters?.let { this.fileFilters = ArrayList(filters) }
    }

    /**
     * @param filter1 第一个filter, 不为null
     * @param filter2 第二个filter, 不为null
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
        if (this.fileFilters.isEmpty()) {
            return false
        }
        for (fileFilter in fileFilters) {
            if (!fileFilter.accept(file)) {
                return false
            }
        }
        return true
    }

    /**
     * {@inheritDoc}
     */
    override fun accept(file: File, name: String): Boolean {
        if (this.fileFilters.isEmpty()) {
            return false
        }
        for (fileFilter in fileFilters) {
            if (!fileFilter.accept(file, name)) {
                return false
            }
        }
        return true
    }

    /**
     * @return String表示
     */
    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append(super.toString())
        buffer.append("(")
        for (i in fileFilters.indices) {
            if (i > 0) {
                buffer.append(",")
            }
            val filter = fileFilters[i]
            buffer.append(filter.toString())
        }
        buffer.append(")")
        return buffer.toString()
    }

    companion object {

        private const val serialVersionUID = 7215974688563965257L
    }

}
