package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import java.io.Serializable

/**
 * 此类将Java FileFilter 或 FilenameFilter 转换为IO FileFilter。
 *
 *
 * @version $Id$
 * @see FileFilterUtil.asFileFilter
 * @see FileFilterUtil.asFileFilter
 * @since 1.0
 */
internal class DelegateFileFilter : AbstractFileFilter, Serializable {
    /**
     * Filename filter
     */
    private val filenameFilter: FilenameFilter?
    /**
     * File filter
     */
    private val fileFilter: FileFilter?

    /**
     * 依据FilenameFilter构造代理文件过滤器。
     *
     * @param filter 封装的FilenameFilter
     */
    constructor(filter: FilenameFilter) {
        this.filenameFilter = filter
        this.fileFilter = null
    }

    /**
     * 依据FileFilter构造代理文件过滤器。
     *
     * @param filter 封装的FilenameFilter
     */
    constructor(filter: FileFilter) {
        this.fileFilter = filter
        this.filenameFilter = null
    }

    /**
     * Checks the filter.
     *
     * @param file the file to check
     * @return true if the filter matches
     */
    override fun accept(file: File): Boolean {
        return fileFilter?.accept(file) ?: super.accept(file)
    }

    /**
     * 检查filter。
     *
     * @param dir 目录
     * @param name 目录中的文件名
     * @return true 若文件名匹配
     */
    override fun accept(dir: File, name: String): Boolean {
        return filenameFilter?.accept(dir, name) ?: super.accept(dir, name)
    }

    /**
     * @return String 表示
     */
    override fun toString(): String {
        val delegate = fileFilter?.toString() ?: filenameFilter!!.toString()
        return super.toString() + "(" + delegate + ")"
    }

    companion object {

        private const val serialVersionUID = -8723373124984771318L
    }

}
