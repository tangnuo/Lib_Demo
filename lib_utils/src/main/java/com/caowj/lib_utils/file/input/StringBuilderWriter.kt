package com.caowj.lib_utils.file.input

import java.io.Serializable
import java.io.Writer

/**
 * [Writer]实现，将输出写到[StringBuilder]。
 *
 *
 * **NOTE:** 这个实现是`java.io.StringWriter`的另一种实现方式，为更好的性能提供了非同步的实现。
 * 要在多线程使用，应该使用`java.io.StringWriter`。
 *
 * @since 2.0
 */
internal class StringBuilderWriter : Writer, Serializable {
    /**
     * 基础的builder
     */
    val builder: StringBuilder

    /**
     *  默认容量的[StringBuilder]
     */
    constructor() {
        this.builder = StringBuilder()
    }

    /**
     *  构造给定容量的[StringBuilder]
     *
     * @param capacity The initial capacity of the underlying [StringBuilder]
     */
    constructor(capacity: Int) {
        this.builder = StringBuilder(capacity)
    }

    /**
     *
     * 从给定builder创建一个新builder，若传入null，则构造一个默认容量大小的builder。
     *
     * @param builder 传入的builder，可能null
     */
    constructor(builder: StringBuilder?) {
        this.builder = builder ?: StringBuilder()
    }

    /**
     * 在Writer默认追加一个字符。
     *
     * @param value 要追加的字符
     * @return 返回Writer
     */
    override fun append(value: Char): Writer {
        builder.append(value)
        return this
    }

    /**
     * Writer尾部添加字符序列(CharSequence)。
     *
     * @param value 添加的字符序列
     * @return This writer instance
     */
    override fun append(value: CharSequence?): Writer {
        builder.append(value)
        return this
    }

    /**
     * 在[StringBuilder]上添加部分字符。
     *
     *
     * @param value 添加的字符
     * @param start 首字符开始位置
     * @param end  字符结束为止(end不包含)
     * @return Writer
     */
    override fun append(value: CharSequence?, start: Int, end: Int): Writer {
        builder.append(value, start, end)
        return this
    }

    override fun close() {
        // no-op
    }

    override fun flush() {
        // no-op
    }


    /**
     * 将字符串写入[StringBuilder].
     *
     * @param value 写入的字符串值
     */
    override fun write(value: String?) {
        if (value != null) {
            builder.append(value)
        }
    }

    /**
     *  将字符数组的部分字符写入[StringBuilder].
     *
     * @param value  字符数组
     * @param offset 首字符开始位置
     * @param length 长度
     */
    override fun write(value: CharArray?, offset: Int, length: Int) {
        if (value != null) {
            builder.append(value, offset, length)
        }
    }

    /**
     * @return 返回builder的字符串值
     */
    override fun toString(): String {
        return builder.toString()
    }

    companion object {

        private const val serialVersionUID = -146927496096066153L
    }
}
