package com.caowj.lib_utils.file

import java.io.File
import java.io.IOException
import java.util.*

/**
 * 文件名，文件路径处理工具了。
 *
 * 在处理文件名可能的问题是，在Windows上开发，在Unix上运行。这个类目标规避这些问题。
 *
 * **NOTE**: 你可以在开发过程中只使用JDK [File] 对象，就可以不使用这个类来处理。并且使用两个参数的构造方法[File(File,String)][File]。
 *
 * 多数方法在Unix与Windows上均可运行。特定的方法会标记有'System', 'Unix' or 'Windows'
 * 多数方法可以识别分隔符(forward and back)与两套前缀集合。可以查看java文档获取更多信息。
 *
 * 这个类定义了6个组件(example C:\dev\project\file.txt)：
 *
 *  * the prefix - C:\
 *  * the path - dev\project\
 *  * the full path - C:\dev\project\
 *  * the type - file.txt
 *  * the base type - file
 *  * the extension - txt
 *
 * 注意的是这个类在目录名后以分隔符结束时执行效率最高。若删除路径的最后一个分隔符，将无法确定文件名是
 * 一个真是的文件还是一个目录。结果就是，会被认定为是文件。
 *
 * 在Unix，Windows上支持格式：
 * <pre>
 * Windows:
 * a\b\c.txt           --&gt; ""          --&gt; relative
 * \a\b\c.txt          --&gt; "\"         --&gt; current drive absolute
 * C:a\b\c.txt         --&gt; "C:"        --&gt; drive relative
 * C:\a\b\c.txt        --&gt; "C:\"       --&gt; absolute
 * \\server\a\b\c.txt  --&gt; "\\server\" --&gt; UNC
 *
 * Unix:
 * a/b/c.txt           --&gt; ""          --&gt; relative
 * /a/b/c.txt          --&gt; "/"         --&gt; absolute
 * ~/a/b/c.txt         --&gt; "~/"        --&gt; current user
 * ~                   --&gt; "~/"        --&gt; current user (slash added)
 * ~user/a/b/c.txt     --&gt; "~user/"    --&gt; named user
 * ~user               --&gt; "~user/"    --&gt; named user (slash added)
 * </pre>
 *
 * @since 1.1
 */
internal object FilenameUtil {

    /**
     * 扩展名分隔字符。
     *
     * @since 1.4
     */
    @JvmStatic
    val EXTENSION_SEPARATOR = '.'

    /**
     * 扩展名分隔字符串。
     *
     * @since 1.4
     */
    @JvmStatic
    val EXTENSION_SEPARATOR_STR = Character.toString(EXTENSION_SEPARATOR)

    @JvmStatic
    private val NOT_FOUND = -1

    /**
     * Unix分割字符
     */
    private val UNIX_SEPARATOR = '/'

    /**
     * Windows分割字符
     */
    private val WINDOWS_SEPARATOR = '\\'

    /**
     * 系统的分割字符
     */
    private val SYSTEM_SEPARATOR = File.separatorChar

    /**
     * 分隔符与系统分隔符相反
     */
    private var OTHER_SEPARATOR: Char

    init {
        OTHER_SEPARATOR = if (isSystemWindows()) {
            UNIX_SEPARATOR
        } else {
            WINDOWS_SEPARATOR
        }
    }

    /**
     * Determines if Windows file system is in use.
     *
     * @return true if the system is Windows
     */
    @JvmStatic
    fun isSystemWindows(): Boolean {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR
    }

    /**
     * 检查字符是否是分隔符。
     *
     * @param ch 检查的字符
     * @return true 若字符是分隔符
     */
    @JvmStatic
    private fun isSeparator(ch: Char): Boolean {
        return ch == UNIX_SEPARATOR || ch == WINDOWS_SEPARATOR
    }

    /**
     * 标准化路径，移除路径中的'..'，'.'。
     *
     * 此方法标准化路径为标准格式。
     *
     * 保留尾部的斜线。双斜杠会被合并为单斜杠(UNC名将被处理)。将删除单个点路径段。
     * 一个双点将导致该路径段和前一个被删除。如果双点没有父路径段，则返回“null”。
     *
     * The output will be the same on both Unix and Windows except for the separator character.
     * <pre>
     * /foo//               --&gt;   /foo/
     * /foo/./              --&gt;   /foo/
     * /foo/../bar          --&gt;   /bar
     * /foo/../bar/         --&gt;   /bar/
     * /foo/../bar/../baz   --&gt;   /baz
     * //foo//./bar         --&gt;   /foo/bar
     * /../                 --&gt;   null
     * ../foo               --&gt;   null
     * foo/bar/..           --&gt;   foo/
     * foo/../../bar        --&gt;   null
     * foo/../bar           --&gt;   bar
     * //server/foo/../bar  --&gt;   //server/bar
     * //server/../bar      --&gt;   null
     * C:\foo\..\bar        --&gt;   C:\bar
     * C:\..\bar            --&gt;   null
     * ~/foo/../bar/        --&gt;   ~/bar/
     * ~/../bar             --&gt;   null
    </pre> *
     * (Note the file separator returned will be correct for Windows/Unix)
     *
     * @param filename the filename to normalize, null returns null
     * @return the normalized filename, or null if invalid. Null bytes inside string will be removed
     */
    @JvmStatic
    fun normalize(filename: String?): String? {
        return doNormalize(filename, SYSTEM_SEPARATOR, true)
    }

    /**
     * 标准化路径，移除路径中的'..'，'.'。
     *
     * 此方法标准化路径为标准格式。
     *
     * 保留尾部的斜线。双斜杠会被合并为单斜杠(UNC名将被处理)。将删除单个点路径段。
     * 一个双点将导致该路径段和前一个被删除。如果双点没有父路径段，则返回“null”。
     *
     *
     * The output will be the same on both Unix and Windows except for the separator character.
     * <pre>
     * /foo//               --&gt;   /foo/
     * /foo/./              --&gt;   /foo/
     * /foo/../bar          --&gt;   /bar
     * /foo/../bar/         --&gt;   /bar/
     * /foo/../bar/../baz   --&gt;   /baz
     * //foo//./bar         --&gt;   /foo/bar
     * /../                 --&gt;   null
     * ../foo               --&gt;   null
     * foo/bar/..           --&gt;   foo/
     * foo/../../bar        --&gt;   null
     * foo/../bar           --&gt;   bar
     * //server/foo/../bar  --&gt;   //server/bar
     * //server/../bar      --&gt;   null
     * C:\foo\..\bar        --&gt;   C:\bar
     * C:\..\bar            --&gt;   null
     * ~/foo/../bar/        --&gt;   ~/bar/
     * ~/../bar             --&gt;   null
    </pre> *
     * The output will be the same on both Unix and Windows including the separator character.
     *
     * @param filename the filename to normalize, null returns null
     * @param unixSeparator `true` if a unix separator should be used or `false` if a
     * windows separator should be used.
     * @return the normalized filename, or null if invalid. Null bytes inside string will be removed
     * @since 2.0
     */
    @JvmStatic
    fun normalize(filename: String, unixSeparator: Boolean): String? {
        val separator = if (unixSeparator) UNIX_SEPARATOR else WINDOWS_SEPARATOR
        return doNormalize(filename, separator, true)
    }


    /**
     * 规范化路径，删除双点和单点路径步骤，并删除最后的目录分隔符。
     *
     * 保留尾部的斜线。双斜杠会被合并为单斜杠(UNC名将被处理)。将删除单个点路径段。
     * 一个双点将导致该路径段和前一个被删除。如果双点没有父路径段，则返回“null”。
     *
     *
     * The output will be the same on both Unix and Windows except for the separator character.
     * <pre>
     * /foo//               --&gt;   /foo
     * /foo/./              --&gt;   /foo
     * /foo/../bar          --&gt;   /bar
     * /foo/../bar/         --&gt;   /bar
     * /foo/../bar/../baz   --&gt;   /baz
     * //foo//./bar         --&gt;   /foo/bar
     * /../                 --&gt;   null
     * ../foo               --&gt;   null
     * foo/bar/..           --&gt;   foo
     * foo/../../bar        --&gt;   null
     * foo/../bar           --&gt;   bar
     * //server/foo/../bar  --&gt;   //server/bar
     * //server/../bar      --&gt;   null
     * C:\foo\..\bar        --&gt;   C:\bar
     * C:\..\bar            --&gt;   null
     * ~/foo/../bar/        --&gt;   ~/bar
     * ~/../bar             --&gt;   null
     * </pre>
     * (Note the file separator returned will be correct for Windows/Unix)
     *
     * @param filename the filename to normalize, null returns null
     * @return the normalized filename, or null if invalid. Null bytes inside string will be removed
     */
    @JvmStatic
    fun normalizeNoEndSeparator(filename: String?): String? {
        return doNormalize(filename, SYSTEM_SEPARATOR, false)
    }

    /**
     * The output will be the same on both Unix and Windows including the separator character.
     * <pre>
     * /foo//               --&gt;   /foo
     * /foo/./              --&gt;   /foo
     * /foo/../bar          --&gt;   /bar
     * /foo/../bar/         --&gt;   /bar
     * /foo/../bar/../baz   --&gt;   /baz
     * //foo//./bar         --&gt;   /foo/bar
     * /../                 --&gt;   null
     * ../foo               --&gt;   null
     * foo/bar/..           --&gt;   foo
     * foo/../../bar        --&gt;   null
     * foo/../bar           --&gt;   bar
     * //server/foo/../bar  --&gt;   //server/bar
     * //server/../bar      --&gt;   null
     * C:\foo\..\bar        --&gt;   C:\bar
     * C:\..\bar            --&gt;   null
     * ~/foo/../bar/        --&gt;   ~/bar
     * ~/../bar             --&gt;   null
     * </pre>
     *
     * @param filename the filename to normalize, null returns null
     * @param unixSeparator `true` if a unix separator should be used or `false` if a
     * windows separator should be used.
     * @return the normalized filename, or null if invalid. Null bytes inside string will be removed
     * @since 2.0
     */
    @JvmStatic
    fun normalizeNoEndSeparator(filename: String,
                                unixSeparator: Boolean): String? {
        val separator = if (unixSeparator) UNIX_SEPARATOR else WINDOWS_SEPARATOR
        return doNormalize(filename, separator, false)
    }

    /**
     * 执行规范化路径的方法。
     *
     * @param filename 文件名
     * @param separator 分隔符
     * @param keepSeparator true 若保留最后的分隔符
     * @return the normalized filename. Null bytes inside string will be removed.
     */
    @JvmStatic
    private fun doNormalize(filename: String?, separator: Char,
                            keepSeparator: Boolean): String? {
        if (filename == null) {
            return null
        }

        failIfNullBytePresent(filename)

        var size = filename.length
        if (size == 0) {
            return filename
        }
        val prefix = getPrefixLength(filename)
        if (prefix < 0) {
            return null
        }

        val array = CharArray(size + 2)  // +1 for possible extra slash, +2 for arraycopy
        filename.toCharArray(array, 0, 0, filename.length)

        // fix separators throughout
        val otherSeparator = if (separator == SYSTEM_SEPARATOR) OTHER_SEPARATOR else SYSTEM_SEPARATOR
        for (i in array.indices) {
            if (array[i] == otherSeparator) {
                array[i] = separator
            }
        }

        // add extra separator on the end to simplify code below
        var lastIsDirectory = true
        if (array[size - 1] != separator) {
            array[size++] = separator
            lastIsDirectory = false
        }

        // adjoining slashes
        run {
            var i = prefix + 1
            while (i < size) {
                if (array[i] == separator && array[i - 1] == separator) {
                    System.arraycopy(array, i, array, i - 1, size - i)
                    size--
                    i--
                }
                i++
            }
        }

        // dot slash
        run {
            var i = prefix + 1
            while (i < size) {
                if (array[i] == separator && array[i - 1] == '.' &&
                        (i == prefix + 1 || array[i - 2] == separator)) {
                    if (i == size - 1) {
                        lastIsDirectory = true
                    }
                    System.arraycopy(array, i + 1, array, i - 1, size - i)
                    size -= 2
                    i--
                }
                i++
            }
        }

        // double dot slash
        var i = prefix + 2
        outer@ while (i < size) {
            if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' &&
                    (i == prefix + 2 || array[i - 3] == separator)) {
                if (i == prefix + 2) {
                    return null
                }
                if (i == size - 1) {
                    lastIsDirectory = true
                }
                var j: Int
                j = i - 4
                while (j >= prefix) {
                    if (array[j] == separator) {
                        // remove b/../ from a/b/../c
                        System.arraycopy(array, i + 1, array, j + 1, size - i)
                        size -= i - j
                        i = j + 1
                        i++
                        continue@outer
                    }
                    j--
                }
                // remove a/../ from a/../c
                System.arraycopy(array, i + 1, array, prefix, size - i)
                size -= i + 1 - prefix
                i = prefix + 1
            }
            i++
        }

        if (size <= 0) {  // should never be less than 0
            return ""
        }
        if (size <= prefix) {  // should never be less than prefix
            return String(array, 0, size)
        }
        return if (lastIsDirectory && keepSeparator) {
            String(array, 0, size)  // keep trailing separator
        } else String(array, 0, size - 1) // lose trailing separator

    }


    /**
     * 将文件名连接到一个路径上。
     *
     * 等同于在第一个参数后边添加第二个参数路径。
     * 第一个参数是基础路径，第二个参数是连接上去的路径。最后返回的路径是规范化后的路径。
     *
     * The output will be the same on both Unix and Windows except for the separator character.
     * <pre>
     * /foo/ + bar          --&gt;   /foo/bar
     * /foo + bar           --&gt;   /foo/bar
     * /foo + /bar          --&gt;   /bar
     * /foo + C:/bar        --&gt;   C:/bar
     * /foo + C:bar         --&gt;   C:bar (*)
     * /foo/a/ + ../bar     --&gt;   foo/bar
     * /foo/ + ../../bar    --&gt;   null
     * /foo/ + /bar         --&gt;   /bar
     * /foo/.. + /bar       --&gt;   /bar
     * /foo + bar/c.txt     --&gt;   /foo/bar/c.txt
     * /foo/c.txt + bar     --&gt;   /foo/c.txt/bar (!)
     * </pre>
     * @param basePath the base path to attach to, always treated as a path
     * @param fullFilenameToAdd the filename (or path) to attach to the base
     * @return the concatenated path, or null if invalid.  Null bytes inside string will be removed
     */
    @JvmStatic
    fun concat(basePath: String?, fullFilenameToAdd: String): String? {
        val prefix = getPrefixLength(fullFilenameToAdd)
        if (prefix < 0) {
            return null
        }
        if (prefix > 0) {
            return normalize(fullFilenameToAdd)
        }
        if (basePath == null) {
            return null
        }
        val len = basePath.length
        if (len == 0) {
            return normalize(fullFilenameToAdd)
        }
        val ch = basePath[len - 1]
        return if (isSeparator(ch)) {
            normalize(basePath + fullFilenameToAdd)
        } else {
            normalize("$basePath/$fullFilenameToAdd")
        }
    }


    /**
     * @param path the path to check
     */
    @JvmStatic
    private fun failIfNullBytePresent(path: String) {
        val len = path.length
        for (i in 0 until len) {
            require(path[i].toInt() != 0) { "Null byte present in file/path type. There are no " + "known legitimate use cases for such data, but several injection attacks may use it" }
        }
    }


    /**
     * 确定 `parent` 目录是否包含 `child` 元素(文件或目录)。
     *
     * 边界场景：
     *
     *  * `directory` 不能为 null: 若是 null, 抛 IllegalArgumentException
     *  * 目录不能包含自身： 返回 false
     *  * A null child file is not contained in any parent: return false
     *
     * @param canonicalParent 作为parent目录的File对象
     * @param canonicalChild  检查是否在目录中的元素
     * @return true 若child存在于parent目录中，反之，返回false
     * @throws IOException if an IO error occurs while checking the files.
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun directoryContains(canonicalParent: String,
                          canonicalChild: String?): Boolean {
        if (canonicalChild == null) {
            return false
        }

        return if (IOCase.SYSTEM.checkEquals(canonicalParent, canonicalChild)) {
            false
        } else IOCase.SYSTEM.checkStartsWith(canonicalChild, canonicalParent)
    }


    /**
     * 转换反斜杠为Unix上的斜杠。
     *
     * @param path 转换的路径， null ignored
     * @return 更新后的路径
     */
    @JvmStatic
    fun separatorsToUnix(path: String?): String? {
        return if (path == null || path.indexOf(WINDOWS_SEPARATOR) == NOT_FOUND) {
            path
        } else path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR)
    }

    /**
     * 转换斜杠为Windows上的反斜杠。
     *
     * @param path 转换的路径， null ignored
     * @return 更新后的路径
     */
    @JvmStatic
    fun separatorsToWindows(path: String?): String? {
        return if (path == null || path.indexOf(UNIX_SEPARATOR) == NOT_FOUND) {
            path
        } else path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR)
    }

    /**
     * 转换分隔符为系统分隔符。
     *
     * @param path 转换的路径， null ignored
     * @return 更新后的路径
     */
    @JvmStatic
    fun separatorsToSystem(path: String?): String? {
        if (path == null) {
            return null
        }
        return if (isSystemWindows()) {
            separatorsToWindows(path)
        } else {
            separatorsToUnix(path)
        }
    }

    /**
     * 返回文件名前缀长度。
     *
     * The prefix length includes the first slash in the full filename if applicable. Thus, it is
     * possible that the length returned is greater than the length of the input string.
     * <pre>
     * Windows:
     * a\b\c.txt           --&gt; ""          --&gt; relative
     * \a\b\c.txt          --&gt; "\"         --&gt; current drive absolute
     * C:a\b\c.txt         --&gt; "C:"        --&gt; drive relative
     * C:\a\b\c.txt        --&gt; "C:\"       --&gt; absolute
     * \\server\a\b\c.txt  --&gt; "\\server\" --&gt; UNC
     * \\\a\b\c.txt        --&gt;  error, length = -1
     *
     * Unix:
     * a/b/c.txt           --&gt; ""          --&gt; relative
     * /a/b/c.txt          --&gt; "/"         --&gt; absolute
     * ~/a/b/c.txt         --&gt; "~/"        --&gt; current user
     * ~                   --&gt; "~/"        --&gt; current user (slash added)
     * ~user/a/b/c.txt     --&gt; "~user/"    --&gt; named user
     * ~user               --&gt; "~user/"    --&gt; named user (slash added)
     * //server/a/b/c.txt  --&gt; "//server/"
     * ///a/b/c.txt        --&gt; error, length = -1
    </pre> *
     *
     * Note that a leading // (or \\) is used to indicate a UNC type on Windows. These must be
     * followed by a server type, so double-slashes are not collapsed to a single slash at the start
     * of the filename.
     *
     * @param filename the filename to find the prefix in, null returns -1
     * @return the length of the prefix, -1 if invalid or null
     */
    @JvmStatic
    fun getPrefixLength(filename: String?): Int {
        if (filename == null) {
            return NOT_FOUND
        }
        val len = filename.length
        if (len == 0) {
            return 0
        }
        var ch0 = filename[0]
        if (ch0 == ':') {
            return NOT_FOUND
        }
        if (len == 1) {
            if (ch0 == '~') {
                return 2  // return a length greater than the input
            }
            return if (isSeparator(ch0)) 1 else 0
        } else {
            if (ch0 == '~') {
                var posUnix = filename.indexOf(UNIX_SEPARATOR, 1)
                var posWin = filename.indexOf(WINDOWS_SEPARATOR, 1)
                if (posUnix == NOT_FOUND && posWin == NOT_FOUND) {
                    return len + 1  // return a length greater than the input
                }
                posUnix = if (posUnix == NOT_FOUND) posWin else posUnix
                posWin = if (posWin == NOT_FOUND) posUnix else posWin
                return Math.min(posUnix, posWin) + 1
            }
            val ch1 = filename[1]
            if (ch1 == ':') {
                ch0 = Character.toUpperCase(ch0)
                if (ch0 >= 'A' && ch0 <= 'Z') {
                    return if (len == 2 || isSeparator(filename[2]) == false) {
                        2
                    } else 3
                } else if (ch0 == UNIX_SEPARATOR) {
                    return 1
                }
                return NOT_FOUND

            } else if (isSeparator(ch0) && isSeparator(ch1)) {
                var posUnix = filename.indexOf(UNIX_SEPARATOR, 2)
                var posWin = filename.indexOf(WINDOWS_SEPARATOR, 2)
                if (posUnix == NOT_FOUND && posWin == NOT_FOUND || posUnix == 2 || posWin == 2) {
                    return NOT_FOUND
                }
                posUnix = if (posUnix == NOT_FOUND) posWin else posUnix
                posWin = if (posWin == NOT_FOUND) posUnix else posWin
                return Math.min(posUnix, posWin) + 1
            } else {
                return if (isSeparator(ch0)) 1 else 0
            }
        }
    }

    /**
     * 返回最后一个目录分隔符索引。
     *
     * @param filename 文件名路径中最后一分隔符索引, null 返回 -1
     * @return 最后一分隔符索引， 或 -1
     */
    @JvmStatic
    fun indexOfLastSeparator(filename: String?): Int {
        if (filename == null) {
            return NOT_FOUND
        }
        val lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR)
        val lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR)
        return lastUnixPos.coerceAtLeast(lastWindowsPos)
    }

    /**
     * 返回最后一个扩展名中'.'的索引。
     *
     *
     * @param filename 文件名路径中最后扩展名'.'索引, null 返回 -1
     * @return 扩展名'.'索引， 或 -1
     * character
     */
    @JvmStatic
    fun indexOfExtension(filename: String?): Int {
        if (filename == null) {
            return NOT_FOUND
        }
        val extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR)
        val lastSeparator = indexOfLastSeparator(filename)
        return if (lastSeparator > extensionPos) NOT_FOUND else extensionPos
    }

    /**
     * Gets the prefix from a full filename, such as `C:/` or `~/`.
     * 获取完整文件名的前缀，例如：`C:/` 或 `~/`。
     *
     * <pre>
     * Windows:
     * a\b\c.txt           --&gt; ""          --&gt; relative
     * \a\b\c.txt          --&gt; "\"         --&gt; current drive absolute
     * C:a\b\c.txt         --&gt; "C:"        --&gt; drive relative
     * C:\a\b\c.txt        --&gt; "C:\"       --&gt; absolute
     * \\server\a\b\c.txt  --&gt; "\\server\" --&gt; UNC
     *
     * Unix:
     * a/b/c.txt           --&gt; ""          --&gt; relative
     * /a/b/c.txt          --&gt; "/"         --&gt; absolute
     * ~/a/b/c.txt         --&gt; "~/"        --&gt; current user
     * ~                   --&gt; "~/"        --&gt; current user (slash added)
     * ~user/a/b/c.txt     --&gt; "~user/"    --&gt; named user
     * ~user               --&gt; "~user/"    --&gt; named user (slash added)
     * </pre>
     *
     * @param filename 文件名，null 返回 null
     * @return the prefix of the file, null if invalid. Null bytes inside string will be removed
     */
    @JvmStatic
    fun getPrefix(filename: String?): String? {
        if (filename == null) {
            return null
        }
        val len = getPrefixLength(filename)
        if (len < 0) {
            return null
        }
        if (len > filename.length) {
            failIfNullBytePresent(filename + UNIX_SEPARATOR)
            return filename + UNIX_SEPARATOR
        }
        val path = filename.substring(0, len)
        failIfNullBytePresent(path)
        return path
    }

    /**
     * 返回除去前缀后的路劲。
     *
     * <pre>
     * C:\a\b\c.txt --&gt; a\b\
     * ~/a/b/c.txt  --&gt; a/b/
     * a.txt        --&gt; ""
     * a/b/c        --&gt; a/b/
     * a/b/c/       --&gt; a/b/c/
     * </pre>
     *
     *
     * @param filename the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid. Null bytes
     * inside string will be removed
     */
    @JvmStatic
    fun getPath(filename: String?): String? {
        return doGetPath(filename, 1)
    }

    /**
     * <pre>
     * C:\a\b\c.txt --&gt; a\b
     * ~/a/b/c.txt  --&gt; a/b
     * a.txt        --&gt; ""
     * a/b/c        --&gt; a/b
     * a/b/c/       --&gt; a/b/c
     * </pre>
     *
     *
     * The output will be the same irrespective of the machine that the code is running on.
     *
     *
     * This method drops the prefix from the result. See [.getFullPathNoEndSeparator]
     * for the method that retains the prefix.
     *
     * @param filename the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid. Null bytes
     * inside string will be removed
     */
    @JvmStatic
    fun getPathNoEndSeparator(filename: String?): String? {
        return doGetPath(filename, 0)
    }

    /**
     * @param filename the filename
     * @param separatorAdd 0 to omit the end separator, 1 to return it
     * @return the path. Null bytes inside string will be removed
     */
    @JvmStatic
    private fun doGetPath(filename: String?, separatorAdd: Int): String? {
        if (filename == null) {
            return null
        }
        val prefix = getPrefixLength(filename)
        if (prefix < 0) {
            return null
        }
        val index = indexOfLastSeparator(filename)
        val endIndex = index + separatorAdd
        if (prefix >= filename.length || index < 0 || prefix >= endIndex) {
            return ""
        }
        val path = filename.substring(prefix, endIndex)
        failIfNullBytePresent(path)
        return path
    }

    /**
     * 获取filename的完整路径，前缀+路径。
     *
     * <pre>
     * C:\a\b\c.txt --&gt; C:\a\b\
     * ~/a/b/c.txt  --&gt; ~/a/b/
     * a.txt        --&gt; ""
     * a/b/c        --&gt; a/b/
     * a/b/c/       --&gt; a/b/c/
     * C:           --&gt; C:
     * C:\          --&gt; C:\
     * ~            --&gt; ~/
     * ~/           --&gt; ~/
     * ~user        --&gt; ~user/
     * ~user/       --&gt; ~user/
     * </pre>
     *
     * @param filename the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     */
    @JvmStatic
    fun getFullPath(filename: String?): String? {
        return doGetFullPath(filename, true)
    }

    /**
     * <pre>
     * C:\a\b\c.txt --&gt; C:\a\b
     * ~/a/b/c.txt  --&gt; ~/a/b
     * a.txt        --&gt; ""
     * a/b/c        --&gt; a/b
     * a/b/c/       --&gt; a/b/c
     * C:           --&gt; C:
     * C:\          --&gt; C:\
     * ~            --&gt; ~
     * ~/           --&gt; ~
     * ~user        --&gt; ~user
     * ~user/       --&gt; ~user
    </pre> *
     *
     * @param filename the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     */
    @JvmStatic
    fun getFullPathNoEndSeparator(filename: String?): String? {
        return doGetFullPath(filename, false)
    }

    /**
     * Does the work of getting the path.
     *
     * @param filename the filename
     * @param includeSeparator true to include the end separator
     * @return the path
     */
    @JvmStatic
    private fun doGetFullPath(filename: String?, includeSeparator: Boolean): String? {
        if (filename == null) {
            return null
        }
        val prefix = getPrefixLength(filename)
        if (prefix < 0) {
            return null
        }
        if (prefix >= filename.length) {
            return if (includeSeparator) {
                getPrefix(filename)  // add end slash if necessary
            } else {
                filename
            }
        }
        val index = indexOfLastSeparator(filename)
        if (index < 0) {
            return filename.substring(0, prefix)
        }
        var end = index + if (includeSeparator) 1 else 0
        if (end == 0) {
            end++
        }
        return filename.substring(0, end)
    }

    /**
     * 从完整路径取最小路径。
     * <pre>
     * a/b/c.txt --&gt; c.txt
     * a.txt     --&gt; a.txt
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     *
     * @param filename the filename to query, null returns null
     * @return the type of the file without the path, or an empty string if none exists. Null bytes
     * inside string will be removed
     */
    @JvmStatic
    fun getName(filename: String?): String? {
        if (filename == null) {
            return null
        }
        failIfNullBytePresent(filename)
        val index = indexOfLastSeparator(filename)
        return filename.substring(index + 1)
    }

    /**
     * 获取文件名，除去完整filename中的path与extension.
     *
     * <pre>
     * a/b/c.txt --&gt; c
     * a.txt     --&gt; a
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     *
     * @param filename the filename to query, null returns null
     * @return the type of the file without the path, or an empty string if none exists. Null bytes
     * inside string will be removed
     */
    @JvmStatic
    fun getBaseName(filename: String): String? {
        return removeExtension(getName(filename))
    }

    /**
     * 获取扩展名。
     * <pre>
     * foo.txt      --&gt; "txt"
     * a/b/c.jpg    --&gt; "jpg"
     * a/b.txt/c    --&gt; ""
     * a/b/c        --&gt; ""
    </pre> *
     *
     *
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to retrieve the extension of.
     * @return the extension of the file or an empty string if none exists or `null` if the
     * filename is `null`.
     */
    @JvmStatic
    fun getExtension(filename: String?): String? {
        if (filename == null) {
            return null
        }
        val index = indexOfExtension(filename)
        return if (index == NOT_FOUND) {
            ""
        } else {
            filename.substring(index + 1)
        }
    }

    /**
     * Removes the extension from a filename.
     *
     *
     * This method returns the textual part of the filename before the last dot. There must be no
     * directory separator after the dot.
     * <pre>
     * foo.txt    --&gt; foo
     * a\b\c.jpg  --&gt; a\b\c
     * a\b\c      --&gt; a\b\c
     * a.b\c      --&gt; a.b\c
    </pre> *
     *
     *
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the filename minus the extension
     */
    @JvmStatic
    fun removeExtension(filename: String?): String? {
        if (filename == null) {
            return null
        }
        failIfNullBytePresent(filename)

        val index = indexOfExtension(filename)
        return if (index == NOT_FOUND) {
            filename
        } else {
            filename.substring(0, index)
        }
    }

    /**
     * 判断两个filename是否完全相等。
     *
     * @param filename1 the first filename to query, may be null
     * @param filename2 the second filename to query, may be null
     * @return true if the filenames are equal, null equals null
     * @see IOCase.SENSITIVE
     */
    @JvmStatic
    fun equals(filename1: String, filename2: String): Boolean {
        return equals(filename1, filename2, false, IOCase.SENSITIVE)
    }

    /**
     * 判断两个filename基于系统大小写敏感设置是否相等(Window大小写不敏感，Unix大小写敏感)。
     *
     * @param filename1 the first filename to query, may be null
     * @param filename2 the second filename to query, may be null
     * @return true if the filenames are equal, null equals null
     * @see IOCase.SYSTEM
     */
    @JvmStatic
    fun equalsOnSystem(filename1: String, filename2: String): Boolean {
        return equals(filename1, filename2, false, IOCase.SYSTEM)
    }


    /**
     * 规范化后的两个filename是否相等。
     *
     * @param filename1 the first filename to query, may be null
     * @param filename2 the second filename to query, may be null
     * @return true if the filenames are equal, null equals null
     * @see IOCase.SENSITIVE
     */
    @JvmStatic
    fun equalsNormalized(filename1: String, filename2: String): Boolean {
        return equals(filename1, filename2, true, IOCase.SENSITIVE)
    }

    /**
     * 判断filenames基于系统大小写后规范化后是否相等。
     *
     * @param filename1 the first filename to query, may be null
     * @param filename2 the second filename to query, may be null
     * @return true if the filenames are equal, null equals null
     * @see IOCase.SYSTEM
     */
    @JvmStatic
    fun equalsNormalizedOnSystem(filename1: String, filename2: String): Boolean {
        return equals(filename1, filename2, true, IOCase.SYSTEM)
    }

    /**
     *
     * @param filename1 the first filename to query, may be null
     * @param filename2 the second filename to query, may be null
     * @param normalized whether to normalize the filenames
     * @param caseSensitivity what case sensitivity rule to use, null means case-sensitive
     * @return true if the filenames are equal, null equals null
     * @since 1.3
     */
    @JvmStatic
    fun equals(
            filename1: String?, filename2: String?,
            normalized: Boolean, caseSensitivity: IOCase?): Boolean {
        var filename1 = filename1
        var filename2 = filename2
        var caseSensitivity = caseSensitivity

        if (filename1 == null || filename2 == null) {
            return filename1 == null && filename2 == null
        }
        if (normalized) {
            filename1 = normalize(filename1)
            filename2 = normalize(filename2)
            if (filename1 == null || filename2 == null) {
                throw NullPointerException(
                        "Error normalizing one or both of the file names")
            }
        }
        if (caseSensitivity == null) {
            caseSensitivity = IOCase.SENSITIVE
        }
        return caseSensitivity.checkEquals(filename1, filename2)
    }


    /**
     *
     * @param filename the filename to query, null returns false
     * @param extension the extension to check for, null or empty checks for no extension
     * @return true if the filename has the specified extension
     * @throws IllegalArgumentException if the supplied filename contains null bytes
     */
    @JvmStatic
    fun isExtension(filename: String?, extension: String?): Boolean {
        if (filename == null) {
            return false
        }
        failIfNullBytePresent(filename)

        if (extension == null || extension.isEmpty()) {
            return indexOfExtension(filename) == NOT_FOUND
        }
        val fileExt = getExtension(filename)
        return fileExt == extension
    }

    /**
     * @param filename the filename to query, null returns false
     * @param extensions the extensions to check for, null checks for no extension
     * @return true if the filename is one of the extensions
     * @throws IllegalArgumentException if the supplied filename contains null bytes
     */
    @JvmStatic
    fun isExtension(filename: String?, extensions: Array<String>?): Boolean {
        if (filename == null) {
            return false
        }
        failIfNullBytePresent(filename)

        if (extensions == null || extensions.size == 0) {
            return indexOfExtension(filename) == NOT_FOUND
        }
        val fileExt = getExtension(filename)
        for (extension in extensions) {
            if (fileExt == extension) {
                return true
            }
        }
        return false
    }

    /**
     *
     * This method obtains the extension as the textual part of the filename after the last dot.
     * There must be no directory separator after the dot. The extension check is case-sensitive on
     * all platforms.
     *
     * @param filename the filename to query, null returns false
     * @param extensions the extensions to check for, null checks for no extension
     * @return true if the filename is one of the extensions
     * @throws IllegalArgumentException if the supplied filename contains null bytes
     */
    @JvmStatic
    fun isExtension(filename: String?, extensions: Collection<String>?): Boolean {
        if (filename == null) {
            return false
        }
        failIfNullBytePresent(filename)

        if (extensions == null || extensions.isEmpty()) {
            return indexOfExtension(filename) == NOT_FOUND
        }
        val fileExt = getExtension(filename)
        for (extension in extensions) {
            if (fileExt == extension) {
                return true
            }
        }
        return false
    }

    /**
     * 检查文件名是否匹配通配符(包含大小写)。
     *
     * <p>
     *     '?'和'*'表示一个或多个(0或更多)。检查包括大小写。
     *
     * @param filename the filename to match on
     * @param wildcardMatcher the wildcard string to match against
     * @return true if the filename matches the wildcard string
     * @see IOCase.SENSITIVE
     */
    @JvmStatic
    fun wildcardMatch(filename: String, wildcardMatcher: String): Boolean {
        return wildcardMatch(filename, wildcardMatcher, IOCase.SENSITIVE)
    }

    /**
     * Checks a filename to see if it matches the specified wildcard matcher using the case rules of
     * the system.
     *
     *
     * The wildcard matcher uses the characters '?' and '*' to represent a single or multiple (zero
     * or more) wildcard characters. This is the same as often found on Dos/Unix command lines. The
     * check is case-sensitive on Unix and case-insensitive on Windows.
     * @param filename the filename to match on
     * @param wildcardMatcher the wildcard string to match against
     * @return true if the filename matches the wildcard string
     * @see IOCase.SYSTEM
     */
    @JvmStatic
    fun wildcardMatchOnSystem(filename: String,
                              wildcardMatcher: String): Boolean {
        return wildcardMatch(filename, wildcardMatcher, IOCase.SYSTEM)
    }

    /**
     * Checks a filename to see if it matches the specified wildcard matcher allowing control over
     * case-sensitivity.
     *
     *
     * The wildcard matcher uses the characters '?' and '*' to represent a single or multiple (zero
     * or more) wildcard characters. N.B. the sequence "*?" does not work properly at present in
     * match strings.
     *
     * @param filename the filename to match on
     * @param wildcardMatcher the wildcard string to match against
     * @param caseSensitivity what case sensitivity rule to use, null means case-sensitive
     * @return true if the filename matches the wildcard string
     * @since 1.3
     */
    @JvmStatic
    fun wildcardMatch(filename: String?, wildcardMatcher: String?,
                      caseSensitivity: IOCase?): Boolean {
        var caseSensitivity = caseSensitivity
        if (filename == null && wildcardMatcher == null) {
            return true
        }
        if (filename == null || wildcardMatcher == null) {
            return false
        }
        if (caseSensitivity == null) {
            caseSensitivity = IOCase.SENSITIVE
        }
        val wcs = splitOnTokens(wildcardMatcher)
        var anyChars = false
        var textIdx = 0
        var wcsIdx = 0
        val backtrack = Stack<IntArray>()

        // loop around a backtrack stack, to handle complex * matching
        do {
            if (backtrack.size > 0) {
                val array = backtrack.pop()
                wcsIdx = array[0]
                textIdx = array[1]
                anyChars = true
            }

            // loop whilst tokens and text left to process
            while (wcsIdx < wcs.size) {

                if (wcs[wcsIdx] == "?") {
                    // ? so move to next text char
                    textIdx++
                    if (textIdx > filename.length) {
                        break
                    }
                    anyChars = false

                } else if (wcs[wcsIdx] == "*") {
                    // set any chars status
                    anyChars = true
                    if (wcsIdx == wcs.size - 1) {
                        textIdx = filename.length
                    }

                } else {
                    // matching text token
                    if (anyChars) {
                        // any chars then try to locate text token
                        textIdx = caseSensitivity.checkIndexOf(filename, textIdx, wcs[wcsIdx])
                        if (textIdx == NOT_FOUND) {
                            // token not found
                            break
                        }
                        val repeat = caseSensitivity
                                .checkIndexOf(filename, textIdx + 1, wcs[wcsIdx])
                        if (repeat >= 0) {
                            backtrack.push(intArrayOf(wcsIdx, repeat))
                        }
                    } else {
                        // matching from current position
                        if (!caseSensitivity.checkRegionMatches(filename, textIdx, wcs[wcsIdx])) {
                            // couldnt match token
                            break
                        }
                    }

                    // matched text token, move text index to end of matched token
                    textIdx += wcs[wcsIdx].length
                    anyChars = false
                }

                wcsIdx++
            }

            // full match
            if (wcsIdx == wcs.size && textIdx == filename.length) {
                return true
            }

        } while (backtrack.size > 0)

        return false
    }

    /**
     * Splits a string into a number of tokens. The text is split by '?' and '*'. Where multiple '*'
     * occur consecutively they are collapsed into a single '*'.
     *
     * @param text the text to split
     * @return the array of tokens, never null
     */
    @JvmStatic
    internal fun splitOnTokens(text: String): Array<String> {
        // used by wildcardMatch
        // package level so a unit test may run on this

        if (text.indexOf('?') == NOT_FOUND && text.indexOf('*') == NOT_FOUND) {
            return arrayOf(text)
        }

        val array = text.toCharArray()
        val list = ArrayList<String>()
        val buffer = StringBuilder()
        var prevChar: Char = 0.toChar()
        for (ch in array) {
            if (ch == '?' || ch == '*') {
                if (buffer.length != 0) {
                    list.add(buffer.toString())
                    buffer.setLength(0)
                }
                if (ch == '?') {
                    list.add("?")
                } else if (prevChar != '*') {// ch == '*' here; check if previous char was '*'
                    list.add("*")
                }
            } else {
                buffer.append(ch)
            }
            prevChar = ch
        }
        if (buffer.isNotEmpty()) {
            list.add(buffer.toString())
        }

        return list.toTypedArray()
    }
}