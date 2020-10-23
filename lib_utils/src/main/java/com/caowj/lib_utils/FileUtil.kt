package com.caowj.lib_utils

import android.os.Build
import android.text.TextUtils
import com.caowj.lib_utils.file.*
import com.caowj.lib_utils.file.FilenameUtil
import com.caowj.lib_utils.file.filter.*
import com.caowj.lib_utils.file.output.NullOutputStream
import java.io.*
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream
import java.util.zip.Checksum


/**
 * 文件操作工具类。
 *
 *
 * <p>
 * 提供一下方便操作入口：
 * <ul>
 * <li>读文件
 * <li>写文件
 * <li>创建目录(包含父路径上的目录)
 * <li>拷贝文件和目录
 * <li>删除文件和目录
 * <li>File和URL相互转换
 * <li>通过过滤器和扩展名检索文件和目录
 * <li>比较文件内容
 * <li>文件最后修改日期
 * <li>计算校验和
 * </ul>
 * <p>
 * 尽可能使用字符集。依赖于平台默认意味着取决于系统本身的字符集。
 * <p>
 */
object FileUtil {

    /**
     * 1KB字节
     */
    const val ONE_KB = 1024L

    /**
     * 1KB BigInteger对象
     *
     * @since 2.4
     */
    @JvmField
    val ONE_KB_BI: BigInteger = BigInteger.valueOf(ONE_KB)

    /**
     * 1MB
     */
    const val ONE_MB = ONE_KB * ONE_KB

    /**
     * 1MB BitInteger类型
     *
     * @since 2.4
     */
    @JvmField
    val ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI)

    /**
     * 文件拷贝缓存大小
     */
    private const val FILE_COPY_BUFFER_SIZE: Long = ONE_MB * 20

    /**
     * 1GB
     */
    const val ONE_GB: Long = ONE_MB * ONE_KB

    /**
     * 1BG BigInteger
     *
     * @since 2.4
     */
    @JvmField
    val ONE_GB_BI: BigInteger = ONE_MB_BI.multiply(ONE_KB_BI)

    /**
     * 1TB
     */
    const val ONE_TB: Long = ONE_KB * ONE_GB

    /**
     * 1TB  BitInteger
     *
     * @since 2.4
     */
    @JvmField
    val ONE_TB_BI: BigInteger = ONE_KB_BI.multiply(ONE_GB_BI)

    const val FILE_SUFFIX_SEPARATOR = "."
    /**
     *  关闭URLConnection
     *
     * @param conn 要关闭的connection
     * @since 2.4
     */
    @JvmStatic
    fun close(conn: URLConnection) {
        if (conn is HttpURLConnection) {
            conn.disconnect()
        }
    }


    // =====================  构造File对象 ==================================================
    /**
     * <code>File</code>类型空数组
     */
    @JvmField
    val EMPTY_FILE_ARRAY = emptyArray<File?>()

    /**
     * 从给定的参数，构造一个File对象。
     * 如要获取directory下的 kedacom/log/log.txt的文件路径，则调用
     * getFile(directory,"kedacom","log","log.txt")
     * @param directory 父目录
     * @param names 可变参数，一连串的名称设置，最后一个name可以是目录名或者文件名。
     * @return 一个File对象
     * @since 2.1
     *
     */
    @JvmStatic
    fun getFile(directory: File, vararg names: String): File {
        var file: File = directory
        names.forEach { file = File(file, it) }

        return file
    }

    /**
     * 从给定的一串参数值，构造一个File对象。
     * 参数names可以是一个路径中每个目录名，最后一个名字可以是目录名或是文件名。
     *
     * @param names 可变参数，文件路径名
     * @return File对象
     * @since 2.1
     */
    @JvmStatic
    fun getFile(vararg names: String): File {
        var file: File? = null
        names.forEach {
            file = if (file == null) File(it) else File(file, it)
        }

        return file!!
    }

    // ====================================================================================

    // =================== 用户系统临时目录 ===============================================
    /**
     * 获取系统临时目录，Android平台上返回：data/user/0/packagename/cache
     *
     * @return 临时目录路径
     *
     * @since 2.0
     */
    @JvmStatic
    fun getTempDirectoryPath(): String? {
        return System.getProperty("java.io.tmpdir")
    }

    /**
     * 获取系统临时目录的File对象，Android平台上返回：packagename/cache
     *
     * @return 临时目录
     *
     * @since 2.0
     */
    @JvmStatic
    fun getTempDirectory(): File {
        return File(getTempDirectoryPath())
    }
    // ===================================================================================

    // ============================= FileInputStream, FileOutputStream ===================

    /**
     * 在给定的file文件上打开 {@link FileInputStream}，方法提供更好的信息提示。
     *
     * <p>
     * 抛出异常情况：
     * 1. 文件不存在；
     * 2. 文件存在但是目录类型；
     * 3. 文件存在但不可读；
     *
     * @param file 打开的文件对象，不可null
     * @return 在file上打开的{@link FileInputStream}
     * @throws FileNotFoundException 文件不存在
     * @throws IOException 文件对象是目录
     * @throws IOException 文件不可读
     */
    @Throws(IOException::class)
    @JvmStatic
    fun openInputStream(file: File): FileInputStream {
        if (file.exists()) {
            if (file.isDirectory) {
                throw IOException("File $file exists but is a directory")
            }
            if (!file.canRead()) {
                throw IOException("File $file cannot be read")
            }
        } else {
            throw FileNotFoundException("File $file does not exist")
        }
        return FileInputStream(file)
    }

    /**
     * 在给定的file对象上打开{@link FileOutputStream}，若文件不存在则创建其父路径。
     *
     * 若文件路径/文件不存在，会被主动创建。
     *
     * 异常情况：
     * 1. 文件存在但是目录类型；
     * 2. 文件存在但无法写入；
     * 3. 文件路径上的目录无法创建；
     *
     * @param file 要打开的文件对象，不能是null
     * @return 在file上打开的{@link FileOutputStream}
     * @throws IOException 文件对象是目录
     * @throws IOException 文件无法写入
     * @throws IOException 文件父路径无法成功创建
     * @since 1.3
     */
    @Throws(IOException::class)
    @JvmStatic
    fun openOutputStream(file: File): FileOutputStream {
        return openOutputStream(file, false)
    }

    /**
     * 在给定的file对象上打开{@link FileOutputStream}，若文件不存在则创建其父路径。
     *
     * 若文件路径/文件不存在，会被主动创建。
     *
     * 异常情况：
     * 1. 文件存在但是目录类型；
     * 2. 文件存在但无法写入；
     * 3. 文件路径上的目录无法创建；
     *
     * @param file 要打开的文件对象，不能是null
     * @param append {@code true}: 将写入文件的内容追加到文件内容尾部；{@code false}: 重写文件内容，覆盖原有内容.
     * @return 在file上打开的{@link FileOutputStream}
     * @throws IOException 文件对象是目录
     * @throws IOException 文件无法写入
     * @throws IOException 文件父路径无法成功创建
     * @since 2.1
     */
    @Throws(IOException::class)
    @JvmStatic
    fun openOutputStream(file: File, append: Boolean): FileOutputStream {
        if (file.exists()) {
            if (file.isDirectory) {
                throw IOException("File $file exists but is a directory")
            }
            if (!file.canWrite()) {
                throw IOException("File $file cannot be written to")
            }
        } else {
            val parent = file.parentFile
            parent?.let {
                if (!parent.mkdirs() && !parent.isDirectory) {
                    throw IOException("Directory $parent could not be created")
                }
            }
        }
        return FileOutputStream(file, append)
    }
    // ===================================================================================

    // ====================== 统计大小显示 ===================================================
    /**
     * 返回外显的文件大小KB，MB，GB等。
     * <p>
     * 若文件大小超过1GB，返回的数字采用最接近的GB的边界。
     * </p>
     * <p>
     * 类似1MB, 1KB的边界。
     * </p>
     *
     * @param size 字节大小
     * @return 可读的显示(单位： bytes,KB,MB,GB,TB)
     * @since 2.4
     */
    @JvmStatic
    fun byteCountToDisplaySize(size: BigInteger): String =
            when {
                size.divide(ONE_TB_BI) > BigInteger.ZERO -> size.divide(ONE_TB_BI).toString() + " TB"
                size.divide(ONE_GB_BI) > BigInteger.ZERO -> size.divide(ONE_GB_BI).toString() + " GB"
                size.divide(ONE_MB_BI) > BigInteger.ZERO -> size.divide(ONE_MB_BI).toString() + " MB"
                size.divide(ONE_KB_BI) > BigInteger.ZERO -> size.divide(ONE_KB_BI).toString() + " KB"
                else -> "$size bytes"
            }

    /**
     * 返回外显的文件大小KB，MB，GB等。
     * <p>
     * 若文件大小超过1GB，返回的数字采用最接近的GB的边界。
     * </p>
     * <p>
     * 类似1MB, 1KB的边界。
     * </p>
     *
     * @param size 字节大小
     * @return 可读的显示(单位： bytes,KB,MB,GB,TB)
     */
    @JvmStatic
    fun byteCountToDisplaySize(size: Long): String = byteCountToDisplaySize(BigInteger.valueOf(size))

    // ===================================================================================

    // ======================== files操作 ================================================
    /**
     * 将包含java.io.File实例的集合转换为File数组。区别于使用File.listFiles()和FileUtil.listFiles()有区别。
     *
     * @param files 包含java.io.File实例的集合对象
     * @return java.io.File数组
     */
    @JvmStatic
    fun convertFileCollectionToFileArray(files: Collection<File>): Array<File> {
        return files.toTypedArray()
    }

    /**
     * 找出给定目录内的文件（可以包含子目录）。
     * 可以根据IOFileFilter过滤出需要的找到的文件。
     *
     * @param files 找到的文件集合
     * @param directory 被搜索的目录
     * @param filter 搜索的过滤条件
     * @param includeSubDirectories 搜索搜索子目录
     *
     */
    @JvmStatic
    private fun innerListFiles(files: MutableCollection<File>, directory: File,
                               filter: IOFileFilter, includeSubDirectories: Boolean) {
        val found = directory.listFiles(filter as FileFilter)

        for (file in found) {
            if (file.isDirectory) {
                if (includeSubDirectories) {
                    files.add(file)
                }
                innerListFiles(files, file, filter, includeSubDirectories)
            } else {
                files.add(file)
            }
        }
    }

    /**
     * 找到给定目录下所有文件对象（子目录可选）。所有文件通过IOFileFilter过滤。
     *
     * <p>
     *    如果需要搜索子目录，可以传入目录的IOFileFilter过滤器。
     *
     * <p>
     *    例子：如果想搜索所有名为“temp” 的目录，应该传入<code>FileFilterUtil.NameFileFilter("temp")</code>
     *
     * <p>
     *     另一中普遍的使用是搜索一个目录树，但忽略目录产生的CVS。可以传入<code>FileFilterUtil.makeCVSAware(null)</code>
     *
     * 检查参数合法性，java实现中fileFilter有判空分支，kotlin中参数已经限制了传入不能为null。
     * 因此省略fileFilter的判空检测。
     *
     *
     * @param directory 搜索的目录
     * @param fileFilter  搜索文件的过滤器，不能传入null，使用{@link TrueFileFilter#INSTANCE}来匹配选择的目录中的所有文件
     * @param dirFilter 目录过滤器（可选）。若传入{@code null}，不搜索子目录。若要搜索所有子目录，使用{@link TrueFileFilter#INSTANCE}
     * @return 匹配的java.io.File集合
     *
     */
    @JvmStatic
    fun listFiles(directory: File, fileFilter: IOFileFilter, dirFilter: IOFileFilter?): Collection<File> {
        require(directory.isDirectory) { "Parameter 'directory' is not a directory: $directory" }

        val effFileFilter = setUpEffectiveFileFilter(fileFilter)
        val effDirFilter = setUpEffectiveDirFilter(dirFilter)

        val files = java.util.LinkedList<File>()
        innerListFiles(files, directory, FileFilterUtil.or(effFileFilter, effDirFilter), false)
        return files
    }

    /**
     * 返回结合了给定过滤器的一个综合的过滤器。
     *
     * @param fileFilter 文件过滤器
     * @return 接受文件的过滤器
     */
    @JvmStatic
    private fun setUpEffectiveFileFilter(fileFilter: IOFileFilter): IOFileFilter {
        return FileFilterUtil.and(fileFilter, FileFilterUtil.notFileFilter(DirectoryFileFilter.INSTANCE))
    }

    /**
     * 返回接受目录的过滤器，除了给定过滤器过滤的File。
     *
     * @param dirFilter 添加的他过滤器
     * @return 接受目录的过滤器
     */
    @JvmStatic
    private fun setUpEffectiveDirFilter(dirFilter: IOFileFilter?): IOFileFilter {
        return if (dirFilter == null) FalseFileFilter.INSTANCE else FileFilterUtil.and(dirFilter, DirectoryFileFilter.INSTANCE)
    }

    /**
     * 找到给定目录下所有文件对象（子目录可选）。所有文件通过IOFileFilter过滤。
     * <p>
     *     结果集合中包含有开始目录(directory)， 及所有匹配目录过滤器的子目录。
     * <p>
     *
     *@param directory 搜索的目录
     * @param fileFilter  搜索文件的过滤器，不能传入null，使用{@link TrueFileFilter#INSTANCE}来匹配选择的目录中的所有文件
     * @param dirFilter 目录过滤器（可选）。若传入{@code null}，在搜索中排除对子目录中的搜索。若要搜索所有子目录，使用{@link TrueFileFilter#INSTANCE}
     * @return 匹配的java.io.File集合
     * @since 2.2
     */
    @JvmStatic
    fun listFilesAndDirs(directory: File, fileFilter: IOFileFilter, dirFilter: IOFileFilter?): Collection<File> {
        require(directory.isDirectory) { "Parameter 'directory' is not a directory: $directory" }

        val effFileFilter = setUpEffectiveFileFilter(fileFilter)
        val effDirFilter = setUpEffectiveDirFilter(dirFilter)

        val files = java.util.LinkedList<File>()
        files.add(directory)
        innerListFiles(files, directory,
                FileFilterUtil.or(effFileFilter, effDirFilter), true)
        return files
    }

    /**
     * 返回过滤后的文件迭代器。
     *
     * @param directory 搜索的目录
     * @param fileFilter  搜索文件的过滤器，不能传入null，使用{@link TrueFileFilter#INSTANCE}来匹配选择的目录中的所有文件
     * @param dirFilter 目录过滤器（可选）。若传入{@code null}，在搜索中排除对子目录中的搜索。若要搜索所有子目录，使用{@link TrueFileFilter#INSTANCE}
     * @return 匹配的java.io.File集合
     * @since 1.2
     */
    @JvmStatic
    fun iterateFiles(directory: File, fileFilter: IOFileFilter, dirFilter: IOFileFilter?): Iterator<File> {
        return listFiles(directory, fileFilter, dirFilter).iterator()
    }

    /**
     * 返回过滤后文件和目录的迭代器。
     *
     * @param directory 搜索的目录
     * @param fileFilter  搜索文件的过滤器，不能传入null，使用{@link TrueFileFilter#INSTANCE}来匹配选择的目录中的所有文件
     * @param dirFilter 目录过滤器（可选）。若传入{@code null}，在搜索中排除对子目录中的搜索。若要搜索所有子目录，使用{@link TrueFileFilter#INSTANCE}
     * @return 匹配的java.io.File集合
     * @since 2.2
     */
    @JvmStatic
    fun iterateFilesAndDirs(directory: File, fileFilter: IOFileFilter, dirFilter: IOFileFilter?): Iterator<File> {
        return listFilesAndDirs(directory, fileFilter, dirFilter).iterator()
    }
    // ===================================================================================

    // ====================== 根据前后缀过滤 ===============================================
    /**
     * 转换文件扩展名为后缀，用于IOFileFilters。
     *
     * @param extensions 扩展名，例如：{"java", "xml"}
     * @return 后缀，例如：{".java", ".xml"}
     */
    @JvmStatic
    private fun toSuffixes(extensions: Array<String>): Array<String> {
        val suffixes = arrayListOf<String>()
        extensions.forEach { ext ->
            suffixes.add(".$ext")
        }
        return suffixes.toTypedArray()
    }

    /**
     * 根据给定扩展名在目录中找到匹配的所有文件对象（子目录可选）
     *
     * @param directory 搜索的目录
     * @param extensions 一组扩展名，例如：{"java", "xml"}。如果传入{@code null}，返回所有文件
     * @param recursive true 将搜索所有子目录
     * @return 匹配的java.io.File集合
     */
    @JvmStatic
    fun listFiles(directory: File, extensions: Array<String>?, recursive: Boolean): Collection<File> {
        val filter: IOFileFilter =
                if (extensions == null)
                    TrueFileFilter.INSTANCE
                else {
                    val suffixes = toSuffixes(extensions)
                    SuffixFileFilter(suffixes)
                }
        return listFiles(directory, filter, if (recursive) TrueFileFilter.INSTANCE else FalseFileFilter.INSTANCE)
    }

    /**
     * 返回匹配搜索到的所有文件的迭代器。
     *
     * @param directory 搜索的目录
     * @param extensions 一组扩展名，例如：{"java", "xml"}。如果传入{@code null}，返回所有文件
     * @param recursive true 将搜索所有子目录
     * @return 匹配的java.io.File集合迭代器
     * @since 1.2
     */
    @JvmStatic
    fun iterateFiles(directory: File, extensions: Array<String>?, recursive: Boolean): Iterator<File> {
        return listFiles(directory, extensions, recursive).iterator()
    }
    // ===================================================================================

    // =================================== 比较 ==========================================
    /**
     * 比较两个文件内容是否相等。
     *
     * <p>
     *     在进行逐字比较前，方法会检查两个文件内容长度是否相等，或者是否指向同一个文件。
     *
     * @param file1 第一个文件
     * @param file2 第二个文件
     * @return true 两个文件内容相等，或两这个文件均不存在，false 其他情况
     */
    @Throws(IOException::class)
    @JvmStatic
    fun contentEquals(file1: File, file2: File): Boolean {
        val file1Exists = file1.exists()
        return when {
            file1Exists != file2.exists() -> false
            !file1Exists -> true // two not existing files are equal
            file1.isDirectory || file2.isDirectory -> throw IOException("Can't compare directories, only files")  // don't want to compare directory contents
            file1.length() != file2.length() -> false  // lengths differ, cannot be equal
            file1.canonicalFile == file2.canonicalFile -> true // same file
            else -> {
                val input1 = FileInputStream(file1)
                val input2 = FileInputStream(file2)
                IOUtil.contentEquals(input1, input2)
            }
        }
    }

    /**
     * 比较两个文件内容是否相等。
     *
     * <p>
     *     在逐行比较之前，方法检查两个文件是否指向同一文件。
     *
     * @param file1 第一个文件
     * @param file2 第二个文件
     * @return true 两个文件内容相等，或两这个文件均不存在，false 其他情况
     */
    @Throws(IOException::class)
    @JvmStatic
    fun contentEqualsIgnoreEOL(file1: File, file2: File, charsetName: String?): Boolean {
        val file1Exists = file1.exists()
        return when {
            file1Exists != file2.exists() -> false
            !file1Exists -> true // two not existing files are equal
            file1.isDirectory || file2.isDirectory -> throw IOException("Can't compare directories, only files") // don't want to compare directory contents
            file1.canonicalFile == file2.canonicalFile -> true // same file
            else -> {
                val input1 = if (charsetName == null) InputStreamReader(FileInputStream(file1), Charset.defaultCharset()) else InputStreamReader(FileInputStream(file1), charsetName)
                val input2 = if (charsetName == null) InputStreamReader(FileInputStream(file2), Charset.defaultCharset()) else InputStreamReader(FileInputStream(file2), charsetName)
                IOUtil.contentEqualsIgnoreEOL(input1, input2)
            }
        }
    }
    // ===================================================================================

    // =========================== New File ==============================================
    /**
     * 将<code>URL</code>转换为一个<code>File</code>。
     *
     * <p>
     *     从1.1开始方法将decode URL。
     *     <code>file:///my%20docs/file.txt</code>这样的写法decode为<code>/my docs/file.txt</code>。
     *     从1.5开始，此方法使用UTF-8解码percent-encoded八位字节字符。
     *     除此之外，不正确的百分比编码的八位元被宽大地处理，按字面意思传递它们
     */
    @JvmStatic
    fun toFile(url: URL?): File? = if (url == null || !"file".equals(url.protocol, true)) null else {
        var filename = url.file.replace('/', File.separatorChar)
        filename = decodeUrl(filename)
        File(filename)
    }

    /**
     * 根据RFC 3986对指定的URL进行解码，即使用UTF-8字符集将百分比编码的八位字符转换为字符。
     * 这个函数主要用于没有强制使用准确URLs{@link URL}。
     * 方法会简单地接收包含非法字符或不准确%编码的字符，别切将其传给结果字符串。
     *
     */
    @JvmStatic
    internal fun decodeUrl(url: String): String {
        var decoded = url
        return url.let {
            if (it.indexOf('%') >= 0) {
                val n = it.length
                val buffer = StringBuilder()
                val bytes = ByteBuffer.allocate(n)
                var i = 0
                while (i < n) {
                    if (it[i] == '%') {
                        try {
                            do {
                                val octet = Integer.parseInt(it.substring(i + 1, i + 3), 16).toByte()
                                bytes.put(octet)
                                i += 3
                            } while (i < n && it[i] == '%')
                            continue
                        } catch (e: RuntimeException) {
                            // malformed percent-encoded octet, fall through and
                            // append characters literally
                        } finally {
                            if (bytes.position() > 0) {
                                bytes.flip()
                                buffer.append(StandardCharsets.UTF_8.decode(bytes).toString())
                                bytes.clear()
                            }
                        }
                    }
                    buffer.append(it[i++])
                }
                decoded = buffer.toString()
                decoded
            } else decoded
        }
    }

    /**
     * 将一组 <code>URL</code>转换成<code>File</code>。
     * <p>
     *     返回一组相同大小的File数组。
     *     若传入null，则返回空数组。
     *     若传入的数组包含null，返回数组中也将包含null.
     *
     * <p>
     *     方法解码URL。
     *     <code>file:///my%20docs/file.txt</code>将被解码转换成<code>/my docs/file.txt</code>。
     */
    @JvmStatic
    fun toFiles(urls: Array<URL?>?): Array<File?> {
        if (urls == null || urls.isEmpty()) {
            return EMPTY_FILE_ARRAY
        }
        val files = arrayOfNulls<File>(urls.size)
        urls.forEachIndexed { index, url ->
            url?.let {
                require(url.protocol == "file") { "URL could not be converted to a File: $url" }

                files[index] = toFile(url)
            }
        }
        return files
    }

    /**
     * 将一组<code>File</code>转换为<code>URL</code>。
     *
     * @param files 转换的File数组，不能null
     * @return 转换后的URL数组
     */
    @Throws(IOException::class)
    @JvmStatic
    fun toURLs(files: Array<File>): Array<URL?> {
        val urls = arrayOfNulls<URL>(files.size)
        files.forEachIndexed { index, file ->
            urls[index] = file.toURI().toURL()
        }
        return urls
    }
    // ===================================================================================


    // ================================= copy文件操作 ========================================
    /**
     * 将一个文件拷贝到一个目录中，保持原有日期。
     *
     * <p>
     *     方法将原文件拷贝到一个目标目录中。如果目标目录不存在将会被创建。
     *     若同名文件已经存在，将会被覆盖。
     *
     * @param srcFile 存在的文件，不能null
     * @param destDir 目标目录。不能null
     *
     * @throws IOException 原文件或目标目录非法
     * @throws IOException copy过程中的异常
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyFileToDirectory(srcFile: File, destDir: File) {
        copyFileToDirectory(srcFile, destDir, true)
    }

    /**
     * 拷贝一个文件到目标文件——保持文件日期。
     *
     * <p>
     *     方法将原文件内容拷贝到目标目录的同名文件中。目标目录若不存在将被创建。
     *     若目标目录中同名文件存在，将会被覆盖。
     * <p>
     *     <strong>Note:</strong>设置 <code>preserveFileDate</code>为true保持文件最后
     *     修改日期。但是并不能保证{@link File#setLastModified(long)}操作的成功。
     *     若修改日期操作失败，不会有任何提示。
     *
     * @param srcFile 存在的文件，不能null
     * @param destDir  目标目录，不能null
     * @param preserveFileDate true 保持文件最后修改日期
     *
     * @throws IOException 原文件或目标目录非法
     * @throws IOException copy过程中发生IO错误
     * @throws IOException copy完成后输出文件大小与原文件大小不同
     *
     * @see #copyFile(File, File, boolean)
     * @since 1.3
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyFileToDirectory(srcFile: File, destDir: File, preserveFileDate: Boolean) {
        require(!(destDir.exists() && !destDir.isDirectory)) { "Destination '$destDir' is not a directory" }
        val destFile = File(destDir, srcFile.name)
        copyFile(srcFile, destFile, preserveFileDate)
    }

    /**
     * 拷贝一个文件到目标文件——保持文件日期。
     *
     * <p>
     *     方法将原文件内容拷贝到目标目录的同名文件中。目标目录若不存在将被创建。
     *     若目标目录中同名文件存在，将会被覆盖。
     * <p>
     *     <strong>Note:</strong>设置 <code>preserveFileDate</code>为true保持文件最后
     *     修改日期。但是并不能保证{@link File#setLastModified(long)}操作的成功。
     *     若修改日期操作失败，不会有任何提示。
     *
     * @param srcFile 存在的文件，不能null
     * @param destFile  目标文件（新文件），不能null
     *
     * @throws IOException 原文件或目标目录非法
     * @throws IOException copy过程中发生IO错误
     * @throws IOException copy完成后输出文件大小与原文件大小不同
     *
     * @see #copyFileToDirectory(File, File)
     * @see #copyFile(File, File, boolean)
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyFile(srcFile: File, destFile: File) {
        copyFile(srcFile, destFile, true)
    }

    /**
     * 拷贝一个文件到新的位置。
     *
     * <p>
     *     方法将原文件内容拷贝的一个新的目标文件。
     *     目标文件所在目录若不存在将被创建。
     *     所目标文件存在，将被覆盖。
     *
     * <p>
     *     <strong>Note:</strong>设置 <code>preserveFileDate</code>为true保持文件最后
     *     修改日期。但是并不能保证{@link File#setLastModified(long)}操作的成功。
     *     若修改日期操作失败，不会有任何提示。
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyFile(srcFile: File, destFile: File, preserveFileDate: Boolean) {
        require(srcFile.exists()) { FileNotFoundException("Source '$srcFile' does not exist") }

        if (srcFile.isDirectory) {
            throw IOException("Source '$srcFile' exists but is a directory")
        }

        if (srcFile.canonicalPath == destFile.canonicalPath) {
            throw IOException("Source '$srcFile' and destination '$destFile' are the same")
        }

        val parentFile = destFile.parentFile
        parentFile?.let {
            if (!parentFile.mkdirs() && !parentFile.isDirectory) {
                throw IOException("Destination '$parentFile' directory cannot be created")
            }
        }
        if (destFile.exists() && !destFile.canWrite()) {
            throw IOException("Destination '$destFile' exists but is read-only")
        }
        doCopyFile(srcFile, destFile, preserveFileDate)
    }

    /**
     * 从一个<code>File</code>将bytes拷贝到<code>OutputStream</code>。
     *
     * <p>
     *     方法内部缓存input，因此没有必要使用<code>BufferedInputStream</code>
     *
     * @param input  读取的<code>File</code>
     * @param output  写出<code>OutputStream</code>
     * @return 拷贝的字节数（字节长度）
     *
     * @throws IOException 发生IO异常
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyFile(input: File, output: OutputStream) {
        FileInputStream(input).use { fis ->
            IOUtil.copyLarge(fis, output)
        }
    }

    /**
     * 内部copy方法。
     * 方法缓存原文件长度，并且在输出文件与当前输入文件个大小不同情况下抛出IOException。
     *
     * 因此，文件大小改变可能会失败。
     * 在拷贝过程被截断会抛出“IllegalArgumentException: Negative size”异常。
     *
     * @param srcFile 合法原文件，不能null
     * @param destFile 合法目标文件，不能null
     * @param preserveFileDate 是否保持文件数据
     * @throws IOException 发生错误
     * @throws IOException copy完成后输出文件长度与原文件大小不等
     * @throws IllegalArgumentException  "Negative size"文件拷贝过程被截断
     */
    @Throws(IOException::class)
    @JvmStatic
    private fun doCopyFile(srcFile: File, destFile: File, preserveFileDate: Boolean) {
        if (destFile.exists() && destFile.isDirectory) {
            throw IOException("Destination '$destFile' exists but is a directory")
        }

        FileInputStream(srcFile).use { fis ->
            fis.channel.use { input ->
                FileOutputStream(destFile).use { fos ->
                    fos.channel.use { output ->
                        val size = input.size() // TODO See IO-386
                        var pos: Long = 0
                        var count: Long = 0
                        while (pos < size) {
                            val remain = size - pos
                            count = if (remain > FILE_COPY_BUFFER_SIZE) FILE_COPY_BUFFER_SIZE else remain
                            val bytesCopied = output.transferFrom(input, pos, count)
                            if (bytesCopied == 0L) { // IO-385 - can happen if file is truncated after caching the size
                                break // ensure we don't loop forever
                            }
                            pos += bytesCopied
                        }
                    }
                }
            }
        }

        val srcLen = srcFile.length() // TODO See IO-386
        val dstLen = destFile.length() // TODO See IO-386
        if (srcLen != dstLen) {
            throw IOException("Failed to copy full contents from '$srcFile' to ' $destFile' Expected length: $srcLen Actual: $dstLen")
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified())
        }
    }

    // ===================================================================================

    // ========================== copy目录操作 =========================================
    /**
     * 将一个目录copy到另一个目录，保持文件修改日期。
     *
     * <p>
     *     方法将源目录及其内容拷贝到一个目标目录内。
     *
     * <p>
     *     若目标目录不存在将被创建。
     *     若目标目录已存在，方法将源目录内容与目标目录合并。
     *
     * <p>
     *     <strong>Note:</strong>方法才刚上古保持文件的最后修改日期。但是
     *     {@link File#setLastModified(long)}操作无法保证成功，若修改失败，
     *     不会有任何提示。
     *
     * @param srcDir 源目录，不能null
     * @param destDir 目标目录，不能null
     *
     * @throws IOException 源目录或目标目录非法
     * @throws IOException 拷贝过程发生IO错误
     * @since 1.2
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyDirectoryToDirectory(srcDir: File, destDir: File) {
        require(!(srcDir.exists() && !srcDir.isDirectory)) { "Source '$destDir' is not a directory" }
        require(!(destDir.exists() && !destDir.isDirectory)) { "Destination '$destDir' is not a directory" }

        copyDirectory(srcDir, File(destDir, srcDir.name), true)
    }

    /**
     * 将一个目录copy到另一个目录，保持文件修改日期。
     *
     * <p>
     *     方法将源目录及其内容拷贝到一个目标目录内。
     *
     * <p>
     *     若目标目录不存在将被创建。
     *     若目标目录已存在，方法将源目录内容与目标目录合并。
     *
     * <p>
     *     <strong>Note:</strong>方法才刚上古保持文件的最后修改日期。但是
     *     {@link File#setLastModified(long)}操作无法保证成功，若修改失败，
     *     不会有任何提示。
     *
     * @param srcDir 源目录，不能null
     * @param destDir 目标目录，不能null
     *
     * @throws IOException 源目录或目标目录非法
     * @throws IOException 拷贝过程发生IO错误
     * @since 1.1
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyDirectory(srcDir: File, destDir: File) {
        copyDirectory(srcDir, destDir, true)
    }

    /**
     * 将整个文件夹拷贝到一个新位置。
     *
     * <p>
     *     方法将拷贝源目录所有内容到一个指定目录。
     *
     * <p>
     *     若目标目录不存在将被创建。
     *     若目标目录已存在，方法将源目录内容与目标目录合并。
     *
     * <p>
     *     <strong>Note:</strong>方法才刚上古保持文件的最后修改日期。但是
     *     {@link File#setLastModified(long)}操作无法保证成功，若修改失败，
     *     不会有任何提示。
     *
     * @param srcDir 源目录，不能null
     * @param destDir 目标目录，不能null
     * @param preserveFileDate true 拷贝的文件日期保持与源文件修改日期一致
     *
     * @throws IOException 源目录或目标目录非法
     * @throws IOException 拷贝过程发生IO错误
     * @since 1.1
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyDirectory(srcDir: File, destDir: File, preserveFileDate: Boolean) {
        copyDirectory(srcDir, destDir, null, preserveFileDate)
    }

    /**
     *     /**
     * 拷贝过滤后的目录到一个新位置。
     *
     * <p>
     *     方法拷贝特定源目录内容到一个给定目标目录。
     *
     * <p>
     *     若目标目录不存在将被创建。
     *     若目标目录已存在，方法将源目录内容与目标目录合并。
     *
     * <p>
     *     <strong>Note:</strong>方法才刚上古保持文件的最后修改日期。但是
     *     {@link File#setLastModified(long)}操作无法保证成功，若修改失败，
     *     不会有任何提示。
     * </p>
     *
     * <h3>Example: 只拷贝目录</h3>
     * <pre>
     *     // 只拷贝目录结构
     *     FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false)
     * </pre>
     *
     * <h3>Example: 拷贝目录及txt文件</h3>
     * <pre>
     *     // 创建".txt"文件过滤器
     *     val txtSuffixFilter = FileFilterUtil.suffixFileFilter(".txt")
     *     val txtFiles = FileFilterUtil.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *     // 创建目录/".txt"文件过滤器
     *     val filter = FileFilterUtil.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles)
     *
     *     // 使用过滤器copy
     *     FileUtils.copyDirectory(srcDir, destDir, filter, false)
     * </pre>
     *
     * @param srcDir 存在的源目录，不能null
     * @param destDir 新目录，不能null
     * @param filter 过滤器，传null表示拷贝所有目录及文件
     *
     * @throws IOException 源目录或目标目录非法
     * @throws IOException  copy过程中发生IO异常
     * @since 1.4
    */
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyDirectory(srcDir: File, destDir: File, filter: FileFilter?) {
        copyDirectory(srcDir, destDir, filter, true)
    }


    /**
     * 拷贝过滤后的目录到一个新位置。
     *
     * <p>
     *     方法拷贝特定源目录内容到一个给定目标目录。
     *
     * <p>
     *     若目标目录不存在将被创建。
     *     若目标目录已存在，方法将源目录内容与目标目录合并。
     *
     * <p>
     *     <strong>Note:</strong>方法才刚上古保持文件的最后修改日期。但是
     *     {@link File#setLastModified(long)}操作无法保证成功，若修改失败，
     *     不会有任何提示。
     * </p>
     *
     * <h3>Example: 只拷贝目录</h3>
     * <pre>
     *     // 只拷贝目录结构
     *     FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false)
     * </pre>
     *
     * <h3>Example: 拷贝目录及txt文件</h3>
     * <pre>
     *     // 创建".txt"文件过滤器
     *     val txtSuffixFilter = FileFilterUtil.suffixFileFilter(".txt")
     *     val txtFiles = FileFilterUtil.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *     // 创建目录/".txt"文件过滤器
     *     val filter = FileFilterUtil.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles)
     *
     *     // 使用过滤器copy
     *     FileUtils.copyDirectory(srcDir, destDir, filter, false)
     * </pre>
     *
     * @param srcDir 存在的源目录，不能null
     * @param destDir 新目录，不能null
     * @param filter 过滤器，传null表示拷贝所有目录及文件
     * @param preserveFileDate true保持文件最后修改日期
     *
     * @throws IOException 源目录或目标目录非法
     * @throws IOException  copy过程中发生IO异常
     * @since 1.4
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyDirectory(srcDir: File, destDir: File, filter: FileFilter?, preserveFileDate: Boolean) {
        if (!srcDir.exists()) {
            throw FileNotFoundException("Source '$srcDir' does not exist")
        }

        if (!srcDir.isDirectory) {
            throw IOException("Source '$srcDir' exists but is not a directory")
        }

        if (srcDir.canonicalPath == destDir.canonicalPath) {
            throw IOException("Source '$srcDir' and destination '$destDir' are the same")
        }

        // Cater for destination being directory within the source directory (see IO-141)
        var exclusionList: List<String>? = null
        if (destDir.canonicalPath.startsWith(srcDir.canonicalPath)) {
            val srcFiles = if (filter == null) srcDir.listFiles() else srcDir.listFiles(filter)
            if (srcFiles != null && srcFiles.isNotEmpty()) {
                exclusionList = mutableListOf()
                srcFiles.forEach { file ->
                    val copiedFile = File(destDir, file.name)
                    exclusionList.add(copiedFile.canonicalPath)
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList)
    }

    /**
     * 内部拷贝目录方法。
     *
     * @param srcDir 合法源目录，不能null
     * @param destDir 合法目标目录，不能null
     * @param filter 过滤器，null表示拷贝所有目录及文件
     * @param preserveFileDate 是否保持修改日期
     * @param exclusionList 排除在外的目录及文件列表，可以是null
     * @throws IOException 发生错误
     * @since 1.1
     */
    @Throws(IOException::class)
    @JvmStatic
    private fun doCopyDirectory(srcDir: File, destDir: File, filter: FileFilter?, preserveFileDate: Boolean, exclusionList: List<String>?) {
        // recurse
        val srcFiles = (if (filter == null) srcDir.listFiles() else srcDir.listFiles(filter))
                ?: throw IOException("Failed to list contents of $srcDir")

        if (destDir.exists()) {
            if (!destDir.isDirectory) {
                throw IOException("Destination '$destDir' exists but is not a directory")
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory) {
                throw IOException("Destination '$destDir' directory cannot be created")
            }
        }

        if (!destDir.canWrite()) {
            throw IOException("Destination '$destDir' cannot be written to")
        }

        srcFiles.forEach { file ->
            val dstFile = File(destDir, file.name)
            if (exclusionList == null || !exclusionList.contains(file.canonicalPath)) {
                if (file.isDirectory) {
                    doCopyDirectory(file, dstFile, filter, preserveFileDate, exclusionList)
                } else {
                    doCopyFile(file, dstFile, preserveFileDate)
                }
            }
        }

        // Do this last, as the above has probably affected directory metadata
        if (preserveFileDate) {
            destDir.setLastModified(srcDir.lastModified())
        }
    }
    // ===================================================================================

    // ======================= copy URL =====================================
    /**
     * 从源输入流(InputStream)中拷贝bytes到一个目标文件(destination)。
     * 到<code>destination</code>路径上的目录若不存在将被创建。
     * <code>destination</code>目标若存在将被覆盖。
     *
     * <p>
     *     Warning: 方法不设置连接或读取超时因此可能会死锁。使用{@link #copyURLToFile(URL, File, int, int)}
     *     设置合理超时时间来防止死锁。
     *
     * @param source  从<code>InputStream</code>中拷贝bytes，将被关闭，不能null
     * @param destination 非目录<code>File</code>对象，将bytes写入到此文件对象中
     *
     * @throws IOException 若<code>source</code> URL无法打开
     * @throws IOException 若<code>destination</code>是一个目录
     * @throws IOException 若<code>destination</code>无法写入
     * @throws IOException  若<code>destination</code>需要创建但无法成功创建
     * @throws IOException 在拷贝过程中发生IO错误
     *
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyURLToFile(source: URL, destination: File) {
        copyInputStreamToFile(source.openStream(), destination)
    }

    /**
     * 从源输入流(InputStream)中拷贝bytes到一个目标文件(destination)。
     * 到<code>destination</code>路径上的目录若不存在将被创建。
     * <code>destination</code>目标若存在将被覆盖。
     *
     * <p>
     *     Warning: 方法不设置连接或读取超时因此可能会死锁。使用{@link #copyURLToFile(URL, File, int, int)}
     *     设置合理超时时间来防止死锁。
     *
     * @param source  从<code>InputStream</code>中拷贝bytes，将被关闭，不能null
     * @param destination 非目录<code>File</code>对象，将bytes写入到此文件对象中
     *  @param connectionTimeout  连接超时时间（毫秒值）
     *  @param readTimeout  从<code>source</code>读取超时（无法读取数据）
     *
     * @throws IOException 若<code>source</code> URL无法打开
     * @throws IOException 若<code>destination</code>是一个目录
     * @throws IOException 若<code>destination</code>无法写入
     * @throws IOException  若<code>destination</code>需要创建但无法成功创建
     * @throws IOException 在拷贝过程中发生IO错误
     * @since 2.0
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyURLToFile(source: URL, destination: File,
                      connectionTimeout: Int, readTimeout: Int) {
        val connection = source.openConnection()
        connection.connectTimeout = connectionTimeout
        connection.readTimeout = readTimeout
        copyInputStreamToFile(connection.getInputStream(), destination)
    }

    /**
     * 从源输入流(InputStream)中拷贝bytes到一个目标文件(destination)。
     * 到<code>destination</code>路径上的目录若不存在将被创建。
     * <code>destination</code>目标若存在将被覆盖。
     *
     * {@code source}流将被关闭。
     * {@link #copyToFile(InputStream, File)}方法没有关闭输入流。
     *
     * @param source  从<code>InputStream</code>中拷贝bytes，将被关闭，不能null
     * @param destination 非目录<code>File</code>对象，将bytes写入到此文件对象中
     * @throws IOException 若<code>destination</code>是一个目录
     * @throws IOException 若<code>destination</code>无法写入
     * @throws IOException  若<code>destination</code>需要创建但无法成功创建
     * @throws IOException 在拷贝过程中发生IO错误
     *  @since 2.0
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyInputStreamToFile(source: InputStream, destination: File) {
        source.use { `in` -> copyToFile(`in`, destination) }
    }

    /**
     * 从{@link InputStream}源拷贝字节到一个目标文件。
     * 一直到目标路径上未创建的目录会被创建。若目标文件已经存在会被重写。
     *
     * 源输入流是打开的，例如：使用{@link java.util.zip.ZipInputStream ZipInputStream}
     * See {@link #copyInputStreamToFile(InputStream, File)}关闭输入流的方法。
     *
     * @param source 源<code>InputStream</code>，从中拷贝bytes，不能null
     * @param destination 非目录<code>File</code>对象，bytes将被写入到这个文件对象中（可能覆盖），不能null
     *
     * @throws IOException 若<code>destination</code>是一个目录
     * @throws IOException 若<code>destination</code>无法写入
     * @throws IOException  若<code>destination</code>需要创建但无法成功创建
     * @throws IOException 在拷贝过程中发生IO错误
     * @since 2.5
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyToFile(source: InputStream, destination: File) {
        source.use { `in` -> openOutputStream(destination).use { out -> IOUtil.copy(`in`, out) } }
    }

    /**
     * 拷贝文件/目录到另一个目录中，保持修改日期。
     *
     * <p>
     *     方法拷贝源文件/源目录，连带着其中内容个到特定目录的同名目录。
     *
     * <p>
     *     目标目录若不存在将被创建。若目标目录存在，将合并源文件对象与目标对象。
     *
     * <p>
     *     <strong>Note:</strong> 方法尝试保持文件对象的最后修改日期。但{@link File#setLastModified(long)}
     *     操作不能保证成功。若修改日期失败，不会有任何提示。
     *
     * @param src 存在的文件/目录，不能null
     * @param destDir 目标目录，不能null
     *
     * @throws IOException 若源/目标非法
     * @throws IOException 拷贝过程发生IO错误
     * @see #copyDirectoryToDirectory(File, File)
     * @see #copyFileToDirectory(File, File)
     * @since 2.6
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyToDirectory(src: File, destDir: File) {
        when {
            src.isFile -> copyFileToDirectory(src, destDir)
            src.isDirectory -> copyDirectoryToDirectory(src, destDir)
            else -> throw IOException("The source $src does not exist")
        }
    }

    /**
     * 拷贝文件/目录到另一个目录中，保持修改日期。
     *
     * <p>
     *     方法拷贝源文件/源目录，连带着其中内容个到特定目录的同名目录。
     *
     * <p>
     *     目标目录若不存在将被创建。若目标目录存在，将合并源文件对象与目标对象。
     *
     * <p>
     *     <strong>Note:</strong> 方法尝试保持文件对象的最后修改日期。但{@link File#setLastModified(long)}
     *     操作不能保证成功。若修改日期失败，不会有任何提示。
     *
     * @param srcs 存在的文件，不能null
     * @param destDir 目标目录，不能null
     *
     * @throws IOException 若源/目标非法
     * @throws IOException 拷贝过程发生IO错误
     * @see #copyDirectoryToDirectory(File, File)
     * @since 2.6
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyToDirectory(srcs: Iterable<File>, destDir: File) {
        srcs.forEach { file -> copyFileToDirectory(file, destDir) }
    }
    // ===================================================================================


    // ========================= delete操作  =============================================
    /**
     * 递归删除目录。
     *
     * @param directory 要删除的目录
     * @throws IOException  无法删除
     */
    @Throws(IOException::class)
    @JvmStatic
    fun deleteDirectory(directory: File) {
        when {
            !directory.exists() -> return
            !isSymlink(directory) -> cleanDirectory(directory)
            !directory.delete() -> throw IOException("Unable to delete directory  $directory .")
        }
    }

    /**
     * 删除一个文件，不抛出异常。若是目录， 删除目录及其子目录。
     * <p>
     *     与File.delete()不同之处：
     * <ul>
     * <li>要删除的目录不必是空目录</li>
     * <li>若文件或目录无法删除不会抛出异常</li>
     * </ul>
     */
    @Throws(IOException::class)
    @JvmStatic
    fun deleteQuietly(file: File?): Boolean {
        if (file == null) {
            return false
        }
        try {
            if (file.isDirectory) {
                cleanDirectory(file)
            }
        } catch (ignored: Exception) {
        }

        return try {
            file.delete()
        } catch (ignored: Exception) {
            false
        }

    }

    /**
     * 确定是否{@code parent}目录包含{@code child}文件/目录。
     *
     * <p>
     *     文件在比较前标准化。
     * </p>
     *
     * 边界条件：
     * <ul>
     * <li>一个{@code directory}不能是null：若传入null，抛出IllegalArgumentException</li>
     * <li>一个{@code directory}必须是目录：所不是目录，抛出IllegalArgumentException</li>
     * <li>A directory does not contain itself: return false</li>
     * <li>一个目录不会包含自己：返回false</li>
     * <li>A null child file is not contained in any parent: return false</li>
     * <li>任何parent不会包含一个null子文件：返回false</li>
     * </ul>
     */
    @Throws(IOException::class)
    @JvmStatic
    fun directoryContains(directory: File, child: File?): Boolean {
        require(directory.isDirectory) { "Not a directory: $directory" }
        if (child == null) {
            return false
        }

        if (!directory.exists() || !child.exists()) {
            return false
        }

        // Canonicalize paths (normalizes relative paths)
        val canonicalParent = directory.canonicalPath
        val canonicalChild = child.canonicalPath

        return FilenameUtil.directoryContains(canonicalParent, canonicalChild)
    }

    /**
     * 清理目录而不删除。
     *
     * @param directory 清理的目录
     * @throws IOException 若清理不成功
     * @throws IllegalArgumentException  若{@code directory}不存在或不是目录
     */
    @Throws(IOException::class)
    @JvmStatic
    fun cleanDirectory(directory: File) {
        val files = verifiedListFiles(directory)

        var exception: IOException? = null
        files.forEach { file ->
            try {
                forceDelete(file)
            } catch (ioe: IOException) {
                exception = ioe
            }
        }

        if (null != exception) {
            throw exception!!
        }
    }

    /**
     * 罗列目录内文件，判断目录存在且是一个目录。
     *
     * @param directory 罗列的目录
     * @return 目录中文件，不返回null
     *
     * @throws IOException 若发生IO错误
     */
    @JvmStatic
    private fun verifiedListFiles(directory: File): Array<File> {
        require(directory.exists()) { "$directory does not exist" }
        require(directory.isDirectory) { "$directory is not a directory" }

        val files = directory.listFiles()
        return files ?: throw IOException("Failed to list contents of $directory")
    }

    /**
     * 确定给定文件对象是一个软链接还是一个实际文件对象。
     *
     * <p>
     *     若是软链接，返回true。
     *
     * <p>
     *     当使用jdk 1.7，方法将使用{@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
     *
     * <b>Note:</b> 总是返回false若当前实现运行在jdk 1.6上或系统是Windows上使用{@link FilenameUtils#isSystemWindows()}
     * <p>
     *     代码运行在jdk 1.7以上，使用下面的方法。
     * <br>
     * {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
     *
     * @param file 检查的文件对象
     * @return true 若文件对象是软链接
     * @throws IOException 检查过程中发生IO错误
     *  @since 2.0
     *
     */
    @JvmStatic
    fun isSymlink(file: File): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Files.isSymbolicLink(file.toPath())
    }
    // ===================================================================================

    /**
     * 在超时时间内等待NFS传播文件创建。
     *
     * <p>
     *     方法重复测试{@link File#exists()}知道返回true，直到给定的时间超时。
     *
     * @param file 检查文件，不能null
     * @param seconds 等待最大时间
     * @return true若文件存在
     *
     *
     */
    @JvmStatic
    fun waitFor(file: File, seconds: Int): Boolean {
        val finishAt = System.currentTimeMillis() + seconds * 1000L
        var wasInterrupted = false
        try {
            while (!file.exists()) {
                val remaining = finishAt - System.currentTimeMillis()
                if (remaining < 0) {
                    return false
                }
                try {
                    Thread.sleep(100.coerceAtMost(remaining.toInt()).toLong())
                } catch (ignore: InterruptedException) {
                    wasInterrupted = true
                } catch (ex: Exception) {
                    break
                }

            }
        } finally {
            if (wasInterrupted) {
                Thread.currentThread().interrupt()
            }
        }
        return true
    }

    // ======================== Read File ====================================

    /**
     * 从一个FileDescriptor对象上获取输入流，进行读取操作。
     *
     * @param fd FileDescriptor——例如：打开android选择窗口进行文件选择
     * @param encoding 编码格式
     *
     * @return 读取后的字符串
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readFileToString(fd: FileDescriptor, encoding: Charset?): String {
        val byteArray = FileInputStream(fd).use { fis -> IOUtil.toByteArray(fis) }
        return byteArray.toString(Charsets.toCharset(encoding))
    }

    /**
     * 从一个FileDescriptor对象上获取输入流，进行读取操作。
     *
     * @param fd FileDescriptor——例如：打开android选择窗口进行文件选择
     * @param encoding 编码格式
     *
     * @return 读取后的字符串
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readFileToString(fd: FileDescriptor, encoding: String?): String {
        val byteArray = FileInputStream(fd).use { fis -> IOUtil.toByteArray(fis) }
        return byteArray.toString(Charsets.toCharset(encoding))
    }

    /**
     * 读取文件内容到String。
     *
     * @param file 读取的文件对象，不能null
     * @param encoding 编码，{@code null}表示平台默认编码
     * @return 文件内容，总是不为{@code null}
     * @throws IOException 发生IO错误
     * @since 2.3
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readFileToString(file: File, encoding: Charset?): String {
        openInputStream(file).use { `in` -> return IOUtil.toString(`in`, Charsets.toCharset(encoding)) }
    }

    /**
     * 读取文件内容为String。文件会被关闭。
     *
     * @param file 读取的文件对象，不能null
     * @param encoding 编码，{@code null}表示平台默认编码
     * @return 文件内容，总是不为{@code null}
     * @throws IOException 发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException  在2.2上如果编码格式不支持，则不抛出{@link java.io
     * .UnsupportedEncodingException}
     * @since 2.3
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readFileToString(file: File, encoding: String?): String {
        return readFileToString(file, Charsets.toCharset(encoding))
    }

    /**
     * 读取文件内容成字节数组。文件会被关闭。
     *
     * @param file 读取的文件对象，不能null
     * @return 文件内容，总是不为{@code null}
     * @since 1.1
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readFileToByteArray(file: File): ByteArray {
        openInputStream(file).use { `in` ->
            val fileLength = file.length()
            return if (fileLength > 0) IOUtil.toByteArray(`in`, fileLength) else IOUtil.toByteArray(`in`)
        }
    }

    /**
     * 逐行读取文件内容到String列表。
     *
     * @param fd FileDescriptor对象——如：调用Android系统文件选择框进行文件选择
     * @param encoding 编码格式
     *
     * @return 读取的行列表
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readLines(fd: FileDescriptor, encoding: Charset?): List<String?> {
        val lines = mutableListOf<String?>()
        FileInputStream(fd).use { fis ->
            IOUtil.toBufferedInputStream(fis).use { bis ->
                IOUtil.lineIterator(bis, Charsets.toCharset(encoding)).use { iterator ->
                    while (iterator.hasNext()) {
                        lines.add(iterator.nextLine())
                    }
                }
            }
        }

        return lines
    }

    /**
     * 逐行读取文件内容到String列表。
     *
     * @param fd FileDescriptor对象——如：调用Android系统文件选择框进行文件选择
     * @param encoding 编码格式
     *
     * @return 读取的行列表
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readLines(fd: FileDescriptor, encoding: String?): List<String?> {
        val lines = mutableListOf<String?>()
        FileInputStream(fd).use { fis ->
            IOUtil.toBufferedInputStream(fis).use { bis ->
                IOUtil.lineIterator(bis, Charsets.toCharset(encoding)).use { iterator ->
                    while (iterator.hasNext()) {
                        lines.add(iterator.nextLine())
                    }
                }
            }
        }

        return lines
    }

    /**
     * 逐行读取文件内容到String列表。
     *
     * @param file 读取的文件对象，不能null
     * @param encoding 编码，{@code null}表示平台默认编码
     * @return 文件内容,列表中每个元素为一行，不为 {@code null}
     * @throws IOException 发生IO错误
     * @since 2.3
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readLines(file: File, encoding: Charset): List<String?> {
        openInputStream(file).use { `in` -> return IOUtil.readLines(`in`, Charsets.toCharset(encoding)) }
    }

    /**
     * 逐行读取文件内容到String列表。
     *
     * @param file 读取的文件对象，不能null
     * @param encoding 编码，{@code null}表示平台默认编码
     * @return 文件内容,列表中每个元素为一行，不为 {@code null}
     * @throws IOException 发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException  在2.2上如果编码格式不支持，则不抛出{@link java.io
     * .UnsupportedEncodingException}
     * @since 1.1
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readLines(file: File, encoding: String?): List<String?> {
        return readLines(file, Charsets.toCharset(encoding))
    }

    /**
     * 返回<code>File</code>行迭代器。
     *
     * <p>
     *     方法在文件上打开<code>InputStream</code>。
     *     在完成迭代器操作后应该关闭输入流来释放资源。可以调用
     *     {@link LineIterator#close()}或{@link LineIterator#closeQuietly(LineIterator)}方法。
     *
     * <p>
     *     推荐使用方式：
     * <pre>
     * val it = FileUtils.lineIterator(file, "UTF-8")
     * try {
     *   while (it.hasNext()) {
     *     String line = it.nextLine();
     *     /// do something with line
     *   }
     * } finally {
     *   LineIterator.closeQuietly(iterator);
     * }
     * </pre>
     * <p>
     *     在创建iterator时若发生异常，stream会关闭。
     *
     * @param file 读取的文件对象，不能null
     * @param encoding 编码，{@code null}表示平台默认编码
     * @return 文件行迭代器，不为{@code null}
     * @throws IOException 发生IO异常（文件关闭）
     * @since 1.2
     */
    @Throws(IOException::class)
    @JvmStatic
    fun lineIterator(file: File, encoding: String?): LineIterator {
        var `in`: InputStream? = null
        try {
            `in` = openInputStream(file)
            return IOUtil.lineIterator(`in`, encoding)
        } catch (ex: IOException) {
            try {
                `in`?.close()
            } catch (e: IOException) {
                ex.addSuppressed(e)
            }

            throw ex
        } catch (ex: RuntimeException) {
            try {
                `in`?.close()
            } catch (e: IOException) {
                ex.addSuppressed(e)
            }

            throw ex
        }

    }

    /**
     * 使用默认编码返回<code>File</code>行迭代器。
     *
     * @param file 读取的文件对象，不能null
     * @return 文件行迭代器，不为{@code null}
     * @throws IOException 发生IO异常（文件关闭）
     * @see #lineIterator(File, String)
     * @since 1.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun lineIterator(file: File): LineIterator {
        return lineIterator(file, null)
    }
    // =====================================================================================

    // ==================== Write File 操作=================================================
    /**
     * 将String内容写入到文件，若文件不存在，文件将被创建。
     *
     * <p>
     *     NOTE: 从v1.3开始，若父目录不存在也将被创建。
     *
     * @param file 写入的文件
     * @param data 写入文件的内容
     * @param encoding 使用的编码，{@code null}表示使用平台默认编码
     * @throws IOException 发生IO错误
     * @throws java.io.UnsupportedEncodingException 若VM不支持的编码
     * @since 2.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeStringToFile(file: File, data: String?, encoding: Charset?) {
        writeStringToFile(file, data, encoding, false)
    }

    /**
     * 将String内容写入到文件，若文件不存在，文件将被创建。
     *
     * <p>
     *     NOTE: 从v1.3开始，若父目录不存在也将被创建。
     *
     * @param file 写入的文件
     * @param data 写入文件的内容
     * @param encoding 使用的编码，{@code null}表示使用平台默认编码
     * @throws IOException 发生IO错误
     * @throws java.io.UnsupportedEncodingException 若VM不支持的编码
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeStringToFile(file: File, data: String?, encoding: String? = "UTF-8") {
        writeStringToFile(file, data, encoding, false)
    }

    /**
     * 将String内容写入到文件，若文件不存在，文件将被创建。
     *
     * @param file 写入的文件
     * @param data 写入文件的内容
     * @param encoding 使用的编码，{@code null}表示使用平台默认编码
     * @param append 若果值{@code true}，那么String将被添加到文件内容末尾，不会覆盖重写
     * @throws IOException 发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeStringToFile(file: File, data: String?, encoding: Charset?, append: Boolean) {
        openOutputStream(file, append).use { `out` -> IOUtil.write(data, out, encoding) }
    }

    /**
     * 将String内容写入到文件，若文件不存在，文件将被创建。
     *
     * @param file 写入的文件
     * @param data 写入文件的内容
     * @param encoding 使用的编码，{@code null}表示使用平台默认编码
     * @param append 若果值{@code true}，那么String将被添加到文件内容末尾，不会覆盖重写
     * @throws IOException 发生IO错误
     * @throws java.io.UnsupportedEncodingException 若VM不支持的编码
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeStringToFile(file: File, data: String?, encoding: String?, append: Boolean) {
        writeStringToFile(file, data, Charsets.toCharset(encoding), append)
    }

    /**
     * 将CharSequence写入到文件，文件不存在将被创建。
     *
     * @param file     写入的文件
     * @param data     写入文件的内容
     * @param encoding 使用的编码, {@code null}表示平台默认编码
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(file: File, data: CharSequence?, encoding: Charset?) {
        write(file, data, encoding, false)
    }

    /**
     *
     * 将CharSequence写入到文件，文件不存在将被创建。'
     *
     * @param file     写入的文件
     * @param data     写入文件的内容
     * @param encoding 使用的编码, {@code null}表示平台默认编码
     * @throws IOException  发生IO错误
     * @throws java.io.UnsupportedEncodingException 若VM只支持编码
     * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(file: File, data: CharSequence?, encoding: String?) {
        write(file, data, encoding, false)
    }

    /**
     * 将CharSequence写入到文件，文件不存在将被创建。'
     *
     * @param file     写入的文件
     * @param data     写入文件的内容
     * @param encoding 使用的编码, {@code null}表示平台默认编码
     * @param append   {@code true}, data将被添加到文件内容末尾，而不是覆盖
     * @throws IOException  发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(file: File, data: CharSequence?, encoding: Charset?, append: Boolean) {
        val str = data?.toString()
        writeStringToFile(file, str, encoding, append)
    }

    /**
     * 将CharSequence写入到文件，文件不存在将被创建。'
     *
     * @param file     写入的文件
     * @param data     写入文件的内容
     * @param encoding 使用的编码, {@code null}表示平台默认编码
     * @param append   {@code true}, data将被添加到文件内容末尾，而不是覆盖
     * @throws IOException  发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException  在2.2以上如果VM不支持编码将抛出{@link java.io
     * .UnsupportedEncodingException}
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(file: File, data: CharSequence?, encoding: String?, append: Boolean) {
        write(file, data, Charsets.toCharset(encoding), append)
    }

    /**
     * 将字节数组(ByteArray)写入文件，若文件不存在会被创建。
     * <p>
     * NOTE: 从v1.3开始, 文件所有父目录若不存在都将被创建。
     *
     * @param file 要写入的文件
     * @param data 写入文件的数据
     * @throws IOException 发生I/O错误
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeByteArrayToFile(file: File, data: ByteArray) {
        writeByteArrayToFile(file, data, false)
    }

    /**
     * 将字节数组(ByteArray)写入文件，若文件不存在会被创建。
     *
     * @param file 要写入的文件
     * @param data 写入文件的数据
     * @param append   {@code true}, data将被添加到文件内容末尾，而不是覆盖
     * @throws IOException 发生I/O错误
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeByteArrayToFile(file: File, data: ByteArray, append: Boolean) {
        writeByteArrayToFile(file, data, 0, data.size, append)
    }

    /**
     * 从字节数组的{@code off} 位置开始将{@code len}长度的字节写入到文件。
     *
     * @param file 写入的文件
     * @param data 写入文件的数据
     * @param off   数据的开始偏移量
     * @param len   写入的长度
     * @throws IOException 发生I/O错误
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeByteArrayToFile(file: File, data: ByteArray, off: Int, len: Int) {
        writeByteArrayToFile(file, data, off, len, false)
    }

    /**
     * 从字节数组的{@code off} 位置开始将{@code len}长度的字节写入到文件。
     *
     * @param file 写入的文件
     * @param data 写入文件的数据
     * @param off   数据的开始偏移量
     * @param len   写入的长度
     * @param append   {@code true}, data将被添加到文件内容末尾，而不是覆盖
     * @throws IOException 发生I/O错误
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeByteArrayToFile(file: File, data: ByteArray, off: Int, len: Int, append: Boolean) {
        openOutputStream(file, append).use { out -> out.write(data, off, len) }
    }

    /**
     * 将集合(Collection)内每个item <code>toString()</code>后的值逐行写入到给定<code>File</code>文件中。
     * 将使用编码格式及默认的行位。
     *
     * @param file 写入的文件
     * @param encoding 使用的编码, {@code null}表示平台默认编码
     * @param lines 写入的行，{@code null}产生空行
     * @throws IOException 发生I/O错误
     * @throws java.io.UnsupportedEncodingException VM不支持编码格式
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(file: File, encoding: String?, lines: Collection<*>?) {
        writeLines(file, encoding, lines, null, false)
    }


    /**
     * 将集合(Collection)内每个item <code>toString()</code>后的值逐行写入到给定<code>File</code>文件中。
     * 将使用编码格式及默认的行尾。
     *
     * @param file 写入的文件
     * @param encoding 使用的编码, {@code null}表示平台默认编码
     * @param lines 写入的行，{@code null}产生空行
     * @param append   {@code true}, data将被添加到文件内容末尾，而不是覆盖
     * @throws IOException 发生I/O错误
     * @throws java.io.UnsupportedEncodingException VM不支持编码格式
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(file: File, encoding: String?, lines: Collection<*>?, append: Boolean) {
        writeLines(file, encoding, lines, null, append)
    }

    /**
     * 将集合(Collection)内每个item <code>toString()</code>后的值逐行写入到给定<code>File</code>文件中。
     * 将使用默认编码格式及默认的行尾。
     *
     * @param file 写入的文件
     * @param lines 写入的行，{@code null}产生空行
     * @throws IOException 发生I/O错误
     * @since 1.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(file: File, lines: Collection<*>?) {
        writeLines(file, null, lines, null, false)
    }

    /**
     * 将集合(Collection)内每个item <code>toString()</code>后的值逐行写入到给定<code>File</code>文件中。
     * 将使用默认编码格式及默认的行尾。
     *
     * @param file 写入的文件
     * @param lines 写入的行，{@code null}产生空行
     * @param append   {@code true}, data将被添加到文件内容末尾，而不是覆盖
     * @throws IOException 发生I/O错误
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(file: File, lines: Collection<*>?, append: Boolean) {
        writeLines(file, null, lines, null, append)
    }

    /**
     * 将集合(Collection)内每个item <code>toString()</code>后的值逐行写入到给定<code>File</code>文件中。
     * 将使用默认编码格式及默认的行尾。
     *
     * @param file 写入的文件
     * @param encoding 使用的编码, {@code null}表示平台默认编码
     * @param lines 写入的行，{@code null}产生空行
     * @param lineEnding 换行符， {@code null}系统默认
     * @throws IOException 发生I/O错误
     * @throws java.io.UnsupportedEncodingException VM不支持编码格式
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(file: File, encoding: String?, lines: Collection<*>?, lineEnding: String?) {
        writeLines(file, encoding, lines, lineEnding, false)
    }

    /**
     * 将集合(Collection)内每个item <code>toString()</code>后的值逐行写入到给定<code>File</code>文件中。
     * 将使用默认编码格式及默认的行尾。
     *
     * @param file 写入的文件
     * @param encoding 使用的编码, {@code null}表示平台默认编码
     * @param lines 写入的行，{@code null}产生空行
     * @param lineEnding 换行符， {@code null}系统默认
     * @param append   {@code true}, data将被添加到文件内容末尾，而不是覆盖
     * @throws IOException 发生I/O错误
     * @throws java.io.UnsupportedEncodingException VM不支持编码格式
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(file: File, encoding: String?,
                   lines: Collection<*>?, lineEnding: String?, append: Boolean) {
        BufferedOutputStream(openOutputStream(file, append)).use { out -> IOUtil.writeLines(lines, lineEnding, out, encoding) }
    }

    /**
     * 将集合(Collection)内每个item <code>toString()</code>后的值逐行写入到给定<code>File</code>文件中。
     * 将使用默认编码格式及默认的行尾。
     *
     * @param file 写入的文件
     * @param lines 写入的行，{@code null}产生空行
     * @param lineEnding 换行符， {@code null}系统默认
     * @throws IOException 发生I/O错误
     * @since 1.3
     *
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(file: File, lines: Collection<*>?, lineEnding: String?) {
        writeLines(file, null, lines, lineEnding, false)
    }

    /**
     * 将集合(Collection)内每个item <code>toString()</code>后的值逐行写入到给定<code>File</code>文件中。
     * 将使用默认编码格式及默认的行尾。
     *
     * @param file 写入的文件
     * @param lines 写入的行，{@code null}产生空行
     * @param lineEnding 换行符， {@code null}系统默认
     * @param append   {@code true}, data将被添加到文件内容末尾，而不是覆盖
     * @throws IOException 发生I/O错误
     * @since 2.1
     *
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(file: File, lines: Collection<*>, lineEnding: String, append: Boolean) {
        writeLines(file, null, lines, lineEnding, append)
    }
    // =====================================================================================

    // ============================ Delete 操作 ==========================================
    /**
     * 删除File。若File是目录，则删除其下所有子目录。
     *
     * <p>
     *     方法与File.delete()区别：
     * <ul>
     * <li>要删除的目录不是必须为空目录</li>
     * <li>在文件或目录无法删除时会抛出异常(java.io.File methods returns a boolean)</li>
     * </ul>
     *
     *  @param file 删除的文件，不能null
     *  @throws FileNotFoundException 文件无法找到
     *  @throws IOException 删除失败
     */
    @JvmStatic
    @Throws(IOException::class)
    fun forceDelete(file: File) {
        if (file.isDirectory) {
            deleteDirectory(file)
        } else {
            val filePresent = file.exists()
            if (!file.delete()) {
                if (!filePresent) {
                    throw FileNotFoundException("File does not exist: $file")
                }
                val message = "Unable to delete file: $file"
                throw IOException(message)
            }
        }
    }

    /**
     * 安排在JVM退出时删除文件。
     * 所文件对象是目录，则删除其下所有子目录。
     *
     * @param file 删除的文件，不能null
     * @throws IOException 删除失败
     */
    @JvmStatic
    @Throws(IOException::class)
    fun forceDeleteOnExit(file: File) {
        if (file.isDirectory) {
            deleteDirectoryOnExit(file)
        } else {
            file.deleteOnExit()
        }
    }

    /**
     * 安排在JVM退出时递归删除文件。
     *
     * @param directory 删除的文件，不能null
     * @throws IOException 删除失败
     */
    @JvmStatic
    @Throws(IOException::class)
    private fun deleteDirectoryOnExit(directory: File) {
        if (!directory.exists()) {
            return
        }

        directory.deleteOnExit()
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory)
        }
    }

    /**
     * 清理目录但不删除。
     *
     * @param directory 清理的目录，不能null
     * @throws IOException 清理失败
     */
    @JvmStatic
    @Throws(IOException::class)
    private fun cleanDirectoryOnExit(directory: File) {
        val files = verifiedListFiles(directory)

        var exception: IOException? = null
        for (file in files) {
            try {
                forceDeleteOnExit(file)
            } catch (ioe: IOException) {
                exception = ioe
            }

        }

        if (null != exception) {
            throw exception
        }
    }

    /**
     * 创建目录，包含所有不存在目录。若同名文件已经存在但不是目录，将
     * 抛出异常IOException。若目录无法创建（或并不是已经存在的目录），
     * 抛出异常IOException。
     *
     * @param directory 创建的目录，不能null
     * @throws IOException 目录无法创建或文件已经存在但不是目录
     */
    @JvmStatic
    @Throws(IOException::class)
    fun forceMkdir(directory: File) {
        if (directory.exists()) {
            if (!directory.isDirectory) {
                val message = ("File "
                        + directory
                        + " exists and is "
                        + "not a directory. Unable to create directory.")
                throw IOException(message)
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory) {
                    val message = "Unable to create directory $directory"
                    throw IOException(message)
                }
            }
        }
    }

    /**
     * 创建文件不存在的父目录。
     *
     * @param file 需要创建父目录的文件对象，不能null
     * @throws IOException 父路径创建失败
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun forceMkdirParent(file: File) {
        val parent = file.parentFile ?: return
        forceMkdir(parent)
    }
    // =====================================================================================

    // ==================================== Read Info ================================
    /**
     * 返回文件或目录大小。若是普通文件，将返回文件大小。若是目录，
     * 将递归计算文件目录大小。所文件或目录被因为安全限制，其大小不会
     * 计算。
     *
     * <p>
     *     注意的是溢出情况被忽略，一旦发生溢出并且返回负值。可以查看方法
     *     {@link #sizeOfAsBigInteger(File)}做替代。
     *
     *  @param file 普通文件或目录。不能null
     *  @return 普通文件的大小或目录递归计算后的大小
     *
     *  @throws IllegalArgumentException 文件不存在
     *  @since 2.0
     */
    @JvmStatic
    fun sizeOf(file: File): Long {

        if (!file.exists()) {
            val message = "$file does not exist"
            throw IllegalArgumentException(message)
        }

        return if (file.isDirectory) {
            sizeOfDirectory0(file) // private method; expects directory
        } else {
            file.length()
        }
    }

    /**
     * 返回文件或目录大小。若是普通文件，将返回文件大小。若是目录，
     * 将递归计算文件目录大小。所文件或目录被因为安全限制，其大小不会
     * 计算。
     *
     * @param file 普通文件或目录。不能null
     * @return 普通文件的大小或目录递归计算后的大小
     *
     * @throws IllegalArgumentException 文件不存在
     * @since 2.4
     */
    @JvmStatic
    fun sizeOfAsBigInteger(file: File): BigInteger {

        if (!file.exists()) {
            val message = "$file does not exist"
            throw IllegalArgumentException(message)
        }

        return if (file.isDirectory) {
            sizeOfDirectoryBig0(file) // internal method
        } else {
            BigInteger.valueOf(file.length())
        }

    }

    /**
     * 统计目录大小（包含子目录内所有内容）
     *
     * @param directory  检查的目录，不能null
     * @return 字节大小的目录大小，目录若是安全限制返回0，若大小大于{@link Long#MAX_VALUE}返回负值
     */
    @JvmStatic
    fun sizeOfDirectory(directory: File): Long {
        checkDirectory(directory)
        return sizeOfDirectory0(directory)
    }

    // Private method, must be invoked will a directory parameter
    /**
     * 目录大小
     *
     * @param directory 检查的目录
     * @return 大小
     */
    @JvmStatic
    private fun sizeOfDirectory0(directory: File): Long {
        val files = directory.listFiles()
                ?: // null if security restricted
                return 0L
        var size: Long = 0

        for (file in files) {
            try {
                if (!isSymlink(file)) {
                    size += sizeOf0(file) // internal method
                    if (size < 0) {
                        break
                    }
                }
            } catch (ioe: IOException) {
                // Ignore exceptions caught when asking if a File is a symlink.
            }

        }

        return size
    }

    // Internal method - does not check existence
    /**
     * 文件大小
     *
     * @param file 目标文件
     * @return 文件大小
     */
    @JvmStatic
    private fun sizeOf0(file: File): Long {
        return if (file.isDirectory) {
            sizeOfDirectory0(file)
        } else {
            file.length() // will be 0 if file does not exist
        }
    }

    /**
     * 计算目录大小——此方法可以防止目录大小大于Long.MAX_VALUE情况
     *
     * @param directory 计算的目录，不能null
     * @return 目录大小(单位：字节)，目录若安全限制返回0
     * @since 2.4
     */
    @JvmStatic
    fun sizeOfDirectoryAsBigInteger(directory: File): BigInteger {
        checkDirectory(directory)
        return sizeOfDirectoryBig0(directory)
    }

    // Must be called with a directory
    /**
     * @param directory 计算的目录，不能null
     * @return 目录大小(单位：字节)，目录若安全限制返回0
     */
    @JvmStatic
    private fun sizeOfDirectoryBig0(directory: File): BigInteger {
        val files = directory.listFiles()
                ?: // null if security restricted
                return BigInteger.ZERO
        var size = BigInteger.ZERO

        for (file in files) {
            try {
                if (!isSymlink(file)) {
                    size = size.add(sizeOfBig0(file))
                }
            } catch (ioe: IOException) {
                // Ignore exceptions caught when asking if a File is a symlink.
            }

        }

        return size
    }

    // internal method; if file does not exist will return 0
    /**
     * @param fileOrDir 文件
     * @return 大小
     */
    @JvmStatic
    private fun sizeOfBig0(fileOrDir: File): BigInteger {
        return if (fileOrDir.isDirectory) {
            sizeOfDirectoryBig0(fileOrDir)
        } else {
            BigInteger.valueOf(fileOrDir.length())
        }
    }

    /**
     * 检查文件对象存在且是目录。
     *
     * @param directory 检查的目录
     * @throws IllegalArgumentException 文件不存在或存在但不是目录
     */
    @JvmStatic
    private fun checkDirectory(directory: File) {
        require(directory.exists()) { "$directory does not exist" }
        require(directory.isDirectory) { "$directory is not a directory" }
    }
    // =====================================================================================


    // =============================== Compare 操作 ====================================
    /**
     * 比较两个文件的更新时间。
     * 判断file修改时间是否比reference修改时间更近。
     *
     * @param file  <code>File</code>文件对象，不能null
     * @param reference <code>File</code>文件对象，不能null
     * @return true 文件存在，且修改日期比reference文件修改日期更新
     *
     * @throws IllegalArgumentException 若reference不存在
     */
    @JvmStatic
    fun isFileNewer(file: File, reference: File): Boolean {
        require(reference.exists()) {
            ("The reference file '$reference' doesn't exist")
        }
        return isFileNewer(file, reference.lastModified())
    }

    /**
     * 比较文件的更新时间。
     * 判断file修改时间是否比date修改时间更新。
     *
     * @param file  <code>File</code>文件对象，不能null
     * @param date  日期，不能null。
     *
     * @return true 若file修改时间比date表示的时间更新
     */
    @JvmStatic
    fun isFileNewer(file: File, date: Date): Boolean {
        return isFileNewer(file, date.time)
    }

    /**
     * 比较文件的更新时间.
     * 判断file修改时间是否比timeMillis表示的时间更新。
     *
     * @param file  <code>File</code>文件对象，不能null
     * @param timeMillis  时间表示(单位：毫秒)，从(00:00:00 GMT, January 1, 1970)开始。
     *
     * @return true 若file对象修改时间比timeMillis表示时间更新
     */
    @JvmStatic
    fun isFileNewer(file: File, timeMillis: Long): Boolean {
        return if (!file.exists()) {
            false
        } else file.lastModified() > timeMillis
    }


    /**
     * 比较文件的更新时间.
     * 判断file修改时间是否比reference修改时间更早。
     *
     * @param file  <code>File</code>文件对象，不能null
     * @param reference  文件对象，不能null
     *
     *
     * @return true 若file对象修改时间比reference表示时间更早
     */
    @JvmStatic
    fun isFileOlder(file: File, reference: File): Boolean {
        require(reference.exists()) {
            ("The reference file '$reference' doesn't exist")
        }
        return isFileOlder(file, reference.lastModified())
    }

    /**
     * 比较文件的更新时间.
     * 判断file修改时间是否比date表示时间更早。
     *
     * @param file  <code>File</code>文件对象，不能null
     * @param date  Date对象，不能null
     *
     * @return true 若file对象修改时间比reference表示时间更早
     */
    @JvmStatic
    fun isFileOlder(file: File, date: Date): Boolean {
        return isFileOlder(file, date.time)
    }

    /**
     * 比较文件的更新时间.
     * 判断file修改时间是否比timeMillis表示时间更早。
     *
     * @param file  <code>File</code>文件对象，不能null
     * @param timeMillis  时间表示(单位：毫秒)，从(00:00:00 GMT, January 1, 1970)开始
     *
     * @return true 若file对象修改时间比reference表示时间更早
     */
    @JvmStatic
    fun isFileOlder(file: File, timeMillis: Long): Boolean {
        return if (!file.exists()) {
            false
        } else file.lastModified() < timeMillis
    }
    // =====================================================================================


    // =================================== Check File ======================================
    /**
     * 使用CRC32校验方法对文件进行校验。
     * 返回校验和。
     *
     * @param file 校验文件，不能null
     * @return 校验和
     *
     * @throws IllegalArgumentException 若文件是目录
     * @throws IOException 读取文件异常
     * @since 1.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun checksumCRC32(file: File): String {
        val crc = CRC32()
        checksum(file, crc)
        return crc.value.toString(16)
    }

    /**
     * 利用给定的校验方式对文件进行校验。
     * 多个文件可能都使用一个<code>Checksum</code>实例进行校验。
     * 有需要可以使用相同的<code>Checksum</code>对象。
     *
     * For example:
     * <pre>
     *   val csum = FileUtil.checksum(file, new CRC32()).getValue();
     * </pre>
     *
     * @param file 校验的文件，不能null
     * @param checksum 使用的校验方法， 不能null
     * @return 返回校验和，随文件内容改变而改变
     * @throws IllegalArgumentException 若文件对象是目录
     * @throws IOException 读取文件发生IO错误
     * @since 1.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun checksum(file: File, checksum: Checksum): Checksum {
        require(!file.isDirectory) { "Checksums can't be computed on directories" }
        CheckedInputStream(FileInputStream(file), checksum).use { `in` -> IOUtil.copy(`in`, NullOutputStream()) }
        return checksum
    }
    // =====================================================================================


    // ======================================= Move File ======================================
    /**
     * 移动目录。
     *
     * <p>
     *     当目标目录在另一个系统上时，执行“拷贝删除”。
     *
     * @param srcDir 移动的目录，不能null
     * @param destDir 目标目录，不能null
     * @throws FileExistsException 若目标目录存在
     * @throws IOException 若源目录/目标目录非法
     * @throws IOException 若移动目录过程中发生IO错误
     * @since 1.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun moveDirectory(srcDir: File, destDir: File) {
        if (!srcDir.exists()) {
            throw FileNotFoundException("Source '$srcDir' does not exist")
        }
        if (!srcDir.isDirectory) {
            throw IOException("Source '$srcDir' is not a directory")
        }
        if (destDir.exists()) {
            throw FileExistsException("Destination '$destDir' already exists")
        }
        val rename = srcDir.renameTo(destDir)
        if (!rename) {
            if (destDir.canonicalPath.startsWith(srcDir.canonicalPath + File.separator)) {
                throw IOException("Cannot move directory: $srcDir to a subdirectory of itself: $destDir")
            }
            copyDirectory(srcDir, destDir)
            deleteDirectory(srcDir)
            if (srcDir.exists()) {
                throw IOException("Failed to delete original directory '$srcDir' after copy to '$destDir'")
            }
        }
    }

    /**
     * 移动目录到另一个目录。
     *
     * <p>
     *     当目标目录在另一个系统上时，执行“拷贝删除”。
     *
     * @param src 移动的目录，不能null
     * @param destDir 目标目录，不能null
     * @param createDestDir 若true创建目标目录，false抛出异常
     * @throws FileExistsException 若目标目录存在
     * @throws IOException 若源目录/目标目录非法
     * @throws IOException 若移动目录过程中发生IO错误
     * @since 1.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun moveDirectoryToDirectory(src: File, destDir: File, createDestDir: Boolean) {
        if (!destDir.exists() && createDestDir) {
            destDir.mkdirs()
        }
        if (!destDir.exists()) {
            throw FileNotFoundException("Destination directory '$destDir' does not exist [createDestDir=$createDestDir]")
        }
        if (!destDir.isDirectory) {
            throw IOException("Destination '$destDir' is not a directory")
        }
        moveDirectory(src, File(destDir, src.name))

    }

    /**
     * 移动文件。
     *
     * <p>
     *    当目标目录在另一个系统上时，执行“拷贝删除”。
     *
     * @param srcFile 移动的文件
     * @param destFile 目标文件
     * @throws FileExistsException 若目标文件已经存在
     * @throws IOException 若源文件/目标文件非法
     * @throws IOException 若移动文件时发生IO错误
     * @since 1.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun moveFile(srcFile: File, destFile: File) {
        if (!srcFile.exists()) {
            throw FileNotFoundException("Source '$srcFile' does not exist")
        }
        if (srcFile.isDirectory) {
            throw IOException("Source '$srcFile' is a directory")
        }
        if (destFile.exists()) {
            throw FileExistsException("Destination '$destFile' already exists")
        }
        if (destFile.isDirectory) {
            throw IOException("Destination '$destFile' is a directory")
        }
        val rename = srcFile.renameTo(destFile)
        if (!rename) {
            copyFile(srcFile, destFile)
            if (!srcFile.delete()) {
                deleteQuietly(destFile)
                throw IOException("Failed to delete original file '$srcFile' after copy to '$destFile'")
            }
        }
    }

    /**
     * 移动文件到目录。
     *
     * @param srcFile 移动的文件，不能null
     * @param destDir 目标目录，不能null
     * @param createDestDir 若true创建目标目录，false抛出异常
     * @throws FileExistsException 若目标文件已经存在
     * @throws IOException 若源文件/目标文件非法
     * @throws IOException 若移动文件时发生IO错误
     * @since 1.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun moveFileToDirectory(srcFile: File, destDir: File, createDestDir: Boolean) {
        if (!destDir.exists() && createDestDir) {
            destDir.mkdirs()
        }
        if (!destDir.exists()) {
            throw FileNotFoundException("Destination directory '$destDir'" +
                    " does not exist [createDestDir=$createDestDir]")
        }
        if (!destDir.isDirectory) {
            throw IOException("Destination '$destDir' is not a directory")
        }
        moveFile(srcFile, File(destDir, srcFile.name))
    }

    /**
     * 移动文件/目录到目标目录。
     *
     * <p>
     *    当目标目录在另一个系统上时，执行“拷贝删除”。
     *
     * @param src 移动的文件，不能null
     * @param destDir 目标目录，不能null
     * @param createDestDir 若true创建目标目录，false抛出异常
     * @throws FileExistsException 若目标文件已经存在
     * @throws IOException 若源文件/目标文件非法
     * @throws IOException 若移动文件时发生IO错误
     * @since 1.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun moveToDirectory(src: File, destDir: File, createDestDir: Boolean) {
        if (!src.exists()) {
            throw FileNotFoundException("Source '$src' does not exist")
        }
        if (src.isDirectory) {
            moveDirectoryToDirectory(src, destDir, createDestDir)
        } else {
            moveFileToDirectory(src, destDir, createDestDir)
        }
    }


    // ========================================================================================

    /**
     * 重命名文件 file
     * @param file 原始文件
     * @param newFileName 新文件名(不包含父路径,不包含文件后缀)
     * @return
     */
    @JvmStatic
    fun renameFile(file: File, newFileName: String): Boolean {
        var newFile: File? = null
        if (file.isDirectory) {
            newFile = File(file.parentFile, newFileName)
        } else {
            val temp = newFileName + file.name.substring(
                    file.name.lastIndexOf('.'))
            newFile = File(file.parentFile, temp)
        }
        return file.renameTo(newFile)

    }

    @JvmStatic
    fun deleteFilesInDirWithFilter(dir: String, filter: FileFilter): Boolean {
        return if (isSpace(dir)) false else deleteFilesInDirWithFilter(File(dir), filter)

    }


    /**
     * 根据过滤条件，删除指定文件夹下的文件
     *
     * @param dir    文件夹路径
     * @param filter 过滤条件
     * @return
     */
    @JvmStatic
    fun deleteFilesInDirWithFilter(dir: File?, filter: FileFilter): Boolean {
        if (dir == null) return false
        // dir doesn't exist then return true
        if (!dir.exists()) return true
        // dir isn't a directory then return false
        if (!dir.isDirectory) return false
        val files = dir.listFiles()
        if (files != null && files.size != 0) {
            for (file in files) {
                if (filter.accept(file)) {
                    if (file.isFile) {
                        if (!file.delete()) return false
                    } else if (file.isDirectory) {
                        if (!deleteQuietly(file)) return false
                    }
                }
            }
        }
        return true
    }


    /**
     * 获取指定文件路径的 文件名 (不包含拓展格式)
     *
     * @param file 文件路径
     * @return
     */
    @JvmStatic
    fun getFileNameWithoutExtension(file: File?): String {
        return if (file == null) "" else getFileNameWithoutExtension(file.path)
    }


    /**
     * 获取指定文件路径的 文件名 (不包含拓展格式)
     *
     * @param filePath 文件路径
     * @return
     */
    @JvmStatic
    fun getFileNameWithoutExtension(filePath: String): String {
        if (isSpace(filePath)) return ""
        val lastPoi = filePath.lastIndexOf('.')
        val lastSep = filePath.lastIndexOf(File.separator)
        if (lastSep == -1) {
            return if (lastPoi == -1) filePath else filePath.substring(0, lastPoi)
        }
        return if (lastPoi == -1 || lastSep > lastPoi) {
            filePath.substring(lastSep + 1)
        } else filePath.substring(lastSep + 1, lastPoi)
    }

    @JvmStatic
    private fun isSpace(s: String?): Boolean {
        if (s == null) return true
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    /**
     * 删除文件或者文件夹
     * @param path 文件或文件夹路径
     * @return
     */
    @JvmStatic
    fun deleteQuietly(path: String): Boolean {
        if (TextUtils.isEmpty(path)) {
            return true
        }

        val file = File(path)
        return deleteQuietly(file)
    }


    /**
     * 移动文件
     * @param srcFilePath 源文件路径
     * @param destFilePath 目标文件路径
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    @JvmStatic
    fun moveFile(srcFilePath: String, destFilePath: String) {
        if (TextUtils.isEmpty(srcFilePath) || TextUtils.isEmpty(destFilePath)) {
            throw RuntimeException("Both srcFilePath and destFilePath cannot be null.")
        }
        moveFile(File(srcFilePath), File(destFilePath))
    }

    /**
     * 拷贝文件
     * @param srcFilePath 源文件路径
     * @param destFilePath 目标路径
     * @return
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    @JvmStatic
    fun copyFile(srcFilePath: String, destFilePath: String) {
        copyFile(File(srcFilePath),File(destFilePath))
    }


    /**
     * 获取文件后缀名
     * @param filePath
     * @return
     */
    @JvmStatic
    fun getFileSuffix(filePath: String): String? {
        if (TextUtils.isEmpty(filePath)) {
            return filePath
        }
        val suffix = filePath.lastIndexOf(FILE_SUFFIX_SEPARATOR)
        val fp = filePath.lastIndexOf(File.separator)
        if (suffix == -1) {
            return ""
        }
        return if (fp >= suffix) "" else filePath.substring(suffix + 1)
    }

}