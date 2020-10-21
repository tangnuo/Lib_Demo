package com.caowj.lib_utils.file.filter

import com.caowj.lib_utils.FileUtil
import java.io.File
import java.io.Serializable
import java.util.*

/**
 * 基于截止时间过滤文件，可以过滤更晚更新，更早，或者时间相等的文件。
 *
 * 例如：打印当前目录一天前的文件和目录:
 *
 * <pre>
 * val dir = File(".");
 * // We are interested in files older than one day
 * val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
 * val files = dir.list( new AgeFileFilter(cutoff) );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *  println(files[i]);
 * }
 * </pre>
 *
 * @see FileFilterUtil.ageFileFilter
 * @see FileFilterUtil.ageFileFilter
 * @see FileFilterUtil.ageFileFilter
 * @see FileFilterUtil.ageFileFilter
 * @see FileFilterUtil.ageFileFilter
 * @see FileFilterUtil.ageFileFilter
 * @since 1.2
 */
internal class AgeFileFilter
/**
 * 构造一个Age文件过滤器，基于一个给定的截止时间比较，过滤文件。
 *
 * @param cutoff 文件截止时间(阈值时间)
 * @param acceptOlder  若是true，更早的文件(早于或等于cutoff)被接受，反之晚于cutoff时间被接受
 */
@JvmOverloads constructor(
        /**
         * 时间阀值点(用于比较)
         */
        private val cutoff: Long,
        /**
         * 决定是否过滤出更早或更晚的文件
         */
        private val acceptOlder: Boolean = true) : AbstractFileFilter(), Serializable {

    /**
     * @param cutoffDate 时间阀值点(用于比较)
     * @param acceptOlder 决定是否过滤出更早或更晚的文件
     */
    @JvmOverloads
    constructor(cutoffDate: Date, acceptOlder: Boolean = true) : this(cutoffDate.time, acceptOlder)

    /**
     * 基于传入cutoffReference文件对象的修改时间进行比较。
     *
     * @param cutoffReference 文件最后的修改时间作为时间阀值点
     * @param acceptOlder 决定是否过滤出更早或更晚的文件
     */
    @JvmOverloads
    constructor(cutoffReference: File, acceptOlder: Boolean = true) : this(cutoffReference.lastModified(), acceptOlder)

    //-----------------------------------------------------------------------

    /**
     * 检查文件最后修改时间是否顺利匹配
     *
     * 若最近修改时间等于截止时间并且需要更新的文件，文件不会被接受。
     * 若最近修改时间等于截止时间并且需要更早的文件，文件被接受。
     *
     * @param file 检查的文件
     * @return true 文件匹配时间点
     */
    override fun accept(file: File): Boolean {
        val newer = FileUtil.isFileNewer(file, cutoff)
        return acceptOlder != newer
    }

    /**
     * Provide a String representation of this file filter.
     *
     * @return a String representation
     */
    override fun toString(): String {
        val condition = if (acceptOlder) "<=" else ">"
        return super.toString() + "(" + condition + cutoff + ")"
    }

    companion object {

        private const val serialVersionUID = -2132740084016138541L
    }
}
