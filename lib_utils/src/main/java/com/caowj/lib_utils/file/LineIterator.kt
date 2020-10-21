package com.caowj.lib_utils.file

import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.Reader
import java.util.*

/**
 * 在一个Reader上的迭代器，可以逐行获取内容。
 *
 * LineIterator建立在一个打开的Reader上，因此在结束迭代器操作后，应该Reader来释放内容资源。
 * 可以直接close Reader操作，也可以调用[.close]方法来关闭Reader。
 *
 * 使用方式：
 * <pre>
 * val it = FileUtil.lineIterator(file, "UTF-8");
 * try {
 * while (it.hasNext()) {
 * val line = it.nextLine();
 * // do something with line
 * }
 * } finally {
 * it.close();
 * }
 * </pre>
 *
 * @since 1.2
 */
open class LineIterator
/**
 * 在一个Reader对象上创建迭代器。
 *
 * @param reader 创建迭代器的Reader，不能null
 */
constructor(reader: Reader) : Iterator<String?>, Closeable {

    private val bufferedReader: BufferedReader = if (reader is BufferedReader) {
        reader
    } else {
        BufferedReader(reader)
    }
    /**
     * 当前行
     */
    private var cachedLine: String? = null
    /**
     * 表示当前迭代器是否完全结束的标记
     */
    private var finished = false

    /**
     * 判断Reader是否还有更多行。若发生IOException，则胡主动关闭。
     *
     * @return `true`若未结束
     * @throws IllegalStateException  发生IO错误
     */
    override fun hasNext(): Boolean {
        when {
            cachedLine != null -> return true
            finished -> return false
            else -> try {
                while (true) {
                    val line = bufferedReader.readLine()
                    if (line == null) {
                        finished = true
                        return false
                    } else if (isValidLine(line)) {
                        cachedLine = line
                        return true
                    }
                }
            } catch (ioe: IOException) {
                try {
                    close()
                } catch (e: IOException) {
                    ioe.addSuppressed(e)
                }

                throw IllegalStateException(ioe)
            }
        }
    }

    /**
     * 可重写此方法检查每行的准确性。默认总是返回true。
     *
     * @param line 要检查的行
     * @return true 若准确， false 会从iterator上移除
     */
    protected fun isValidLine(line: String): Boolean {
        return true
    }

    /**
     * 从Reader上读取下一行。
     *
     * @return 输入流中的下一行
     * @throws NoSuchElementException  若没有行返回
     */
    override fun next(): String? {
        return nextLine()
    }

    /**
     * 从Reader上读取下一行。
     *
     * @return 输入流中的下一行
     * @throws NoSuchElementException  若没有行返回
     */
    fun nextLine(): String? {
        if (!hasNext()) {
            throw NoSuchElementException("No more lines")
        }
        val currentLine = cachedLine
        cachedLine = null
        return currentLine
    }

    /**
     * 关闭潜在的Reader。这个方法在只处理一个大文件的开始几行时非常有用。
     * 若不关闭iterator，那么Reader会一直打开。
     * 此方法可以调用多次。
     *
     * @throws IOException 若无法关闭Reader
     */
    @Throws(IOException::class)
    override fun close() {
        finished = true
        cachedLine = null
        this.bufferedReader.close()
    }
}
