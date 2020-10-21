package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.Serializable

/**
 * 过滤接收任意类型的文件对象。
 *
 * @version $Id$
 * @since 1.0
 */
internal class TrueFileFilter : IOFileFilter, Serializable {

    /**
     * Returns true.
     *
     * @param file the file to check (ignored)
     * @return true
     */
    override fun accept(file: File): Boolean {
        return true
    }

    /**
     * Returns true.
     *
     * @param dir the directory to check (ignored)
     * @param name the filename (ignored)
     * @return true
     */
    override fun accept(dir: File, name: String): Boolean {
        return true
    }

    companion object {

        /**
         * Singleton instance of true filter.
         *
         * @since 1.3
         */
        @JvmField
        val TRUE: IOFileFilter = TrueFileFilter()
        /**
         * Singleton instance of true filter. Please use the identical TrueFileFilter.TRUE constant. The
         * new type is more JDK 1.5 friendly as it doesn't clash with other values when using static
         * imports.
         */
        @JvmField
        val INSTANCE = TRUE
        private const val serialVersionUID = 8782512160909720199L
    }

}
