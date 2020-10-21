package com.caowj.lib_utils.file

import java.io.File
import java.io.IOException

/**
 * 异常表示文件已经存在。
 *
 * @since 2.0
 */
class FileExistsException : IOException {

    constructor() : super()

    /**
     * 带有消息的Exception
     *
     * @param message  错误信息
     */
    constructor(message: String) : super(message)

    /**
     * @param file 存在的文件
     */
    constructor(file: File) : super("File $file exists")

    companion object {

        /**
         * Defines the serial version UID.
         */
        private val serialVersionUID = 1L
    }

}
