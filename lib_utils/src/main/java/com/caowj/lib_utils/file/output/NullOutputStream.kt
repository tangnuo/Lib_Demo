package com.caowj.lib_utils.file.output

import java.io.IOException
import java.io.OutputStream

/**
 * 这个输出流将所有数据输出到有名的<b>/dev/null</b>。
 * <p>
 *     这个输出流没有目标写入(file/socket etc.)。在这个流中的所有字节数据将被丢弃。
 * </p>
 *
 * @version $Id$
 */
internal class NullOutputStream : OutputStream() {

    /**
     * 不做任何操作 - 输出至`/dev/null`.
     *
     * @param b 输出的字节数组
     * @param off 开始偏移位
     * @param len 长度
     */
    override fun write(b: ByteArray, off: Int, len: Int) {
        //to /dev/null
    }

    /**
     * 不做任何操作 - 输出至`/dev/null`.
     *
     * @param b 输出的字节
     */
    override fun write(b: Int) {
        //to /dev/null
    }

    /**
     * 不做任何操作 - 输出至`/dev/null`.
     *
     * @param b 输出的字节
     * @throws IOException never
     */
    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        //to /dev/null
    }

    companion object {

        /**
         * A singleton.
         */
        val NULL_OUTPUT_STREAM = NullOutputStream()
    }

}
