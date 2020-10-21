package com.caowj.lib_utils.file.filter

import java.io.File

/**
 * 抽象类通过接口IOFileFilter定义继承Java FileFilter和FilenameFilter。

 * 注意：子类必须重写其中一个accept方法，否则子类会死循环。
 *
 * @version $Id$
 * @since 1.0
 */
abstract class AbstractFileFilter : IOFileFilter {

    /**
     * 检查File是否被接受。
     *
     * @param file 检查的文件
     * @return true 匹配
     */
    override fun accept(file: File): Boolean {
        return accept(file.parentFile, file.name)
    }

    /**
     * 检查File是否被接受。
     *
     * @param dir 目录类型文件
     * @param name 目录内文件
     * @return true 匹配
     */
    override fun accept(dir: File, name: String): Boolean {
        return accept(File(dir, name))
    }

    /**
     *
     * @return String表示
     */
    override fun toString(): String {
        return javaClass.simpleName
    }

}
