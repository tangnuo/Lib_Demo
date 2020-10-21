package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter

/**
 * 定义接口同时继承FileFilter，FilenameFilter
 *
 * @version $Id$
 * @since 1.0
 */
interface IOFileFilter : FileFilter, FilenameFilter {

    /**
     * Checks to see if the File should be accepted by this filter.
     *
     *
     * Defined in [FileFilter].
     *
     * @param file the File to check
     * @return true if this file matches the test
     */
    override fun accept(file: File): Boolean

    /**
     * Checks to see if the File should be accepted by this filter.
     *
     *
     * Defined in [FilenameFilter].
     *
     * @param dir the directory File to check
     * @param name the filename within the directory to check
     * @return true if this file matches the test
     */
    override fun accept(dir: File, name: String): Boolean

}
