package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.Serializable

/**
 * 返回false的问价你过滤器。不接受任何类型文件对象。
 *
 * @version $Id$
 * @since 1.0
 */
internal class FalseFileFilter : IOFileFilter, Serializable {

    /**
     * Returns false.
     *
     * @param file the file to check (ignored)
     * @return false
     */
    override fun accept(file: File): Boolean {
        return false
    }

    /**
     * Returns false.
     *
     * @param dir the directory to check (ignored)
     * @param name the filename (ignored)
     * @return false
     */
    override fun accept(dir: File, name: String): Boolean {
        return false
    }

    companion object {

        /**
         * Singleton instance of false filter.
         *
         * @since 1.3
         */
        @JvmField
        val FALSE: IOFileFilter = FalseFileFilter()
        /**
         * Singleton instance of false filter. Please use the identical FalseFileFilter.FALSE constant.
         * The new type is more JDK 1.5 friendly as it doesn't clash with other values when using static
         * imports.
         */
        @JvmField
        val INSTANCE = FALSE

        private const val serialVersionUID = 6210271677940926200L
    }

}
