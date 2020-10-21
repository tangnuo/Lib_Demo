package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.Serializable

/**
 * 文件过滤器：只接受文件类型(非目录)文件对象。
 *
 * 例如：打印当前目录中的文件(非目录)。
 *
 * <pre>
 * val dir = File(".")
 * val files = dir.list( FileFileFilter.FILE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *   println(files[i]);
 * }
 * </pre>
 *
 * @version $Id$
 * @since 1.3
 */
internal class FileFileFilter : AbstractFileFilter(), Serializable {

    /**
     * 检查文件是否是一个真实文件(非目录).
     *
     * @param file 检查的文件对象
     * @return true 若文件对象是真实文件(非目录)
     */
    override fun accept(file: File): Boolean {
        return file.isFile
    }

    companion object {

        /**
         * 单例
         */
        @get:JvmName("getFile")
        @JvmStatic
        val FILE: IOFileFilter = FileFileFilter()
        private const val serialVersionUID = 5345244090827540862L
    }

}
