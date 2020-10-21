package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.Serializable

/**
 * 目录过滤器——只接受`File`是目录类型的文件对象。
 *
 *
 * 如下代码表示如何列出当前目录下的所有子目录
 *
 * <pre>
 * val dir = new File(".");
 * val files = dir.list( DirectoryFileFilter.INSTANCE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *    println(files[i]);
 * }
 * </pre>
 *
 * @version $Id$
 * @since 1.0
 */
internal class DirectoryFileFilter: AbstractFileFilter(), Serializable {

    /**
     * Checks to see if the file is a directory.
     *
     * @param file the File to check
     * @return true if the file is a directory
     */
    override fun accept(file: File): Boolean {
        return file.isDirectory
    }

    companion object {

        /**
         * Singleton instance of directory filter.
         *
         * @since 1.3
         */
        @JvmField
        val DIRECTORY: IOFileFilter = DirectoryFileFilter()
        /**
         * Singleton instance of directory filter. Please use the identical
         * DirectoryFileFilter.DIRECTORY constant. The new type is more JDK 1.5 friendly as it doesn't
         * clash with other values when using static imports.
         */
        @JvmField
        val INSTANCE = DIRECTORY
        private const val serialVersionUID = -5148237843784525732L
    }

}
