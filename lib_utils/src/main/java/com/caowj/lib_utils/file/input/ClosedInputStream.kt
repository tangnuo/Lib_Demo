package com.caowj.lib_utils.file.input


import com.caowj.lib_utils.file.IOUtil.EOF

import java.io.InputStream

/**
 * 关闭输入流。该流将EOF返回到所有尝试从流中读取内容的地方。
 *
 * 这个类通常使用来在方法中测试边界场景：接收输入流并且作为标记值而非`null`值。
 *
 * @since 1.4
 */
internal class ClosedInputStream : InputStream() {

    /**
     * 返回-1来表示stream结束。
     *
     * @return  -1
     */
    override fun read(): Int {
        return EOF
    }

    companion object {

        /**
         * A singleton.
         */
        @JvmStatic
        val CLOSED_INPUT_STREAM = ClosedInputStream()
    }

}
