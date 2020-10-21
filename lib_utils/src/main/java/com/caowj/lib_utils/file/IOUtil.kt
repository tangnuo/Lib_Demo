package com.caowj.lib_utils.file

import com.caowj.lib_utils.file.IOUtil.toByteArray
import com.caowj.lib_utils.file.input.StringBuilderWriter
import com.caowj.lib_utils.file.output.ByteArrayOutputStream
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.charset.Charset

/**
 * IO操作工具类。
 *
 * NOTE: 此类聚焦于InputStream, OutputStream, Reader, Writer. 每个方法至少有其中一个
 * 作为参数。
 *
 * <p>
 * 此类提供了输入/输出操作static诸多方法。
 * <ul>
 * <li>toXxx/read - 从stream中读取数据
 * <li>write - 将数据接入到stream中
 * <li>copy - 将数据从一个stream拷贝到另一个stream
 * <li>contentEquals - 比较两个stream中数据相等性
 * </ul>
 * <p>
 * byte-to-char方法及char-to-byte方法涉及到转换步骤。 两种方法在每种场景中均有使用，
 * 一种是使用平台默认字符集，而另一个可以指定一种字符编码。推荐使用指定编码集的方法，因为
 * 使用平台默认编码可能导致不可预期的结果。例如：开发与生产在不同的OS上。
 * <p>
 *     类中所有从stream中读取的数据方法在内部实现中均有缓存。这就意味着没有理由使用
 *     <code>BufferedInputStream</code>或<code>BufferedReader</code>。默认缓存大小时4K.
 * <p>
 *     各种拷贝方法都是代理：
 * <ul>
 * <li>{@link #copyLarge(InputStream, OutputStream, byte[])}</li>
 * <li>{@link #copyLarge(InputStream, OutputStream, long, long, byte[])}</li>
 * <li>{@link #copyLarge(Reader, Writer, char[])}</li>
 * <li>{@link #copyLarge(Reader, Writer, long, long, char[])}</li>
 * </ul>
 * 例如： {@link #copy(InputStream, OutputStream)} 调用 {@link #copyLarge(InputStream,
 * OutputStream)}，而它调用 {@link #copy(InputStream, OutputStream, int)},此方法创建缓存，
 * 并调用{@link #copyLarge(InputStream, OutputStream, byte[])}.
 */
object IOUtil {
    // NOTE: 此类聚焦于InputStream, OutputStream, Reader, Writer. 每个方法至少有其中一个
    // 作为参数。

    /**
     * 表示文件结束(end of file)。
     *
     * @since 2.5 (made public)
     */
    @JvmField
    val EOF = -1
    /**
     * Unix目录分隔符
     */
    @JvmField
    val DIR_SEPARATOR_UNIX = '/'
    /**
     * Windows目录分隔符
     */
    @JvmField
    val DIR_SEPARATOR_WINDOWS = '\\'
    /**
     * 系统目录分隔符
     */
    @JvmField
    val DIR_SEPARATOR = File.separatorChar
    /**
     * Unix换行符
     */
    @JvmField
    val LINE_SEPARATOR_UNIX = "\n"
    /**
     * The Windows换行符
     */
    @JvmField
    val LINE_SEPARATOR_WINDOWS = "\r\n"
    /**
     * The system line separator string.
     */
    @JvmField
    var LINE_SEPARATOR: String = ""
    /**
     * The default buffer size ({@value}) to use for [.copyLarge]
     * and [.copyLarge]
     */
    private val DEFAULT_BUFFER_SIZE = 1024 * 4
    /**
     * The default buffer size to use for the skip() methods.
     */
    private val SKIP_BUFFER_SIZE = 2048
    // Allocated in the relevant skip method if necessary.
    /**
     * buffers是静态的，且在线程之间是共享的。
     * 这只所以可能是因为buffers是只写不读的。
     *
     * 在创建时不必同步因为：
     * - 我们不在意buffer是否创建多次；
     * - 我们总是使用相同的大小的buffer，因此如果buffer被重新创建也同样是可以的。
     * (如果buffer大小可变，我们需要同步来保证其他线程不会创建一个更小的buffer。)
     */
    private var SKIP_CHAR_BUFFER: CharArray? = null
    private var SKIP_BYTE_BUFFER: ByteArray? = null

    init {
        // avoid security issues
        StringBuilderWriter(4).use { buf ->
            PrintWriter(buf).use { out ->
                out.println()
                LINE_SEPARATOR = buf.toString()
            }
        }
    }

    /**
     * 关闭一个URLConnection。
     *
     * @param conn  要关闭的Connection
     * @since 2.4
     */
    @JvmStatic
    fun close(conn: URLConnection) {
        if (conn is HttpURLConnection) {
            conn.disconnect()
        }
    }


    /**
     * 获取一个`InputStream`的所有内容并且返回有相同内容的新的InputStream。
     *
     * 这个方法在下面场景更加有利：
     *
     *  * 源InputStream效率较低；
     *  * 关联网络资源，而网络连接资源无法长时间被持有。
     *  * 网络超时相关联。
     *
     *
     * 可以支持使用[.toByteArray]，因为方法避免不必要的内存分配并且拷贝数组。
     * 这个放大在内部就有缓存，因此不必要使用`BufferedInputStream`。
     *
     * @param input 要完全缓存的Stream
     * @return 缓存后的InputStream
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toBufferedInputStream(input: InputStream): InputStream {
        return ByteArrayOutputStream.toBufferedInputStream(input)
    }


    /**
     * 获取一个`InputStream`的所有内容并且返回有相同内容的新的InputStream。
     *
     * 这个方法在下面场景更加有利：
     *
     *  * 源InputStream效率较低；
     *  * 关联网络资源，而网络连接资源无法长时间被持有。
     *  * 网络超时相关联。
     *
     *
     * 可以支持使用[.toByteArray]，因为方法避免不必要的内存分配并且拷贝数组。
     * 这个放大在内部就有缓存，因此不必要使用`BufferedInputStream`。
     *
     * @param input 要完全缓存的Stream
     * @param size 初始缓存大小
     * @return 缓存后的InputStream
     * @throws IOException if an I/O error occurs
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toBufferedInputStream(input: InputStream, size: Int): InputStream {
        return ByteArrayOutputStream.toBufferedInputStream(input, size)
    }

    /**
     * 若reader是一个[BufferedReader]则直接返回这个reader，否则基于reader创建一个新
     * BufferedReader并返回。
     *
     * @param reader  要封装或直接返回的reader，不能是null
     * @return  传入的reader或者一个新的[BufferedReader]对象
     * @throws NullPointerException if the input parameter is null
     * @see .buffer
     * @since 2.2
     */
    @JvmStatic
    fun toBufferedReader(reader: Reader): BufferedReader {
        return if (reader is BufferedReader)
            reader
        else
            BufferedReader(reader)
    }

    /**
     * 若reader是一个[BufferedReader]则直接返回这个reader，否则基于reader创建一个新
     * BufferedReader并返回。
     *
     * @param reader  要封装或直接返回的reader，不能是null
     * @param size 缓存大小
     * @return  传入的reader或者一个新的[BufferedReader]对象
     * @throws NullPointerException if the input parameter is null
     * @see .buffer
     * @since 2.5
     */
    @JvmStatic
    fun toBufferedReader(reader: Reader, size: Int): BufferedReader {
        return if (reader is BufferedReader)
            reader
        else
            BufferedReader(reader, size)
    }

    /**
     * 若reader是一个[BufferedReader]则直接返回这个reader，否则基于reader创建一个新
     * BufferedReader并返回。
     *
     * @param reader  要封装或直接返回的reader，不能是null
     * @return  传入的reader或者一个新的[BufferedReader]对象
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @JvmStatic
    fun buffer(reader: Reader): BufferedReader {
        return if (reader is BufferedReader)
            reader
        else
            BufferedReader(reader)
    }

    /**
     * 若reader是一个[BufferedReader]则直接返回这个reader，否则基于reader创建一个新
     * BufferedReader并返回。
     *
     * @param reader  要封装或直接返回的reader，不能是null
     * @param size 缓存大小
     * @return  传入的reader或者一个新的[BufferedReader]对象
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    fun buffer(reader: Reader, size: Int): BufferedReader {
        return if (reader is BufferedReader)
            reader
        else
            BufferedReader(reader, size)
    }

    /**
     * 若writer是一个[BufferedWriter]则直接返回这个writer，否则基于writer创建一个新
     * BufferedWriter并返回。
     *
     * @param writer 要封装的Writer或者直接返回的writer，不能null
     * @return 直接返回的Writer或一个新的[BufferedWriter]
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @JvmStatic
    fun buffer(writer: Writer): BufferedWriter {
        return if (writer is BufferedWriter)
            writer
        else
            BufferedWriter(writer)
    }


    /**
     * 若writer是一个[BufferedWriter]则直接返回这个writer，否则基于writer创建一个新
     * BufferedWriter并返回。
     *
     * @param writer 要封装的Writer或者直接返回的writer，不能null
     * @param size 缓存大小
     * @return 直接返回的Writer或一个新的[BufferedWriter]
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @JvmStatic
    fun buffer(writer: Writer, size: Int): BufferedWriter {
        return if (writer is BufferedWriter)
            writer
        else
            BufferedWriter(writer, size)
    }

    /**
     * 若outputStream是一个[BufferedOutputStream]则直接返回这个outputStream，否则基于outputStream创建一个新
     * BufferedOutputStream并返回。
     *
     * @param outputStream 要封装的OutputStream或者直接返回的outputStream，不能null
     * @return  直接传入的OutputStream或一个新的 [BufferedOutputStream]
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @JvmStatic
    fun buffer(outputStream: OutputStream): BufferedOutputStream {
        return if (outputStream is BufferedOutputStream)
            outputStream
        else
            BufferedOutputStream(outputStream)
    }


    /**
     * 若outputStream是一个[BufferedOutputStream]则直接返回这个outputStream，否则基于outputStream创建一个新
     * BufferedOutputStream并返回。
     *
     * @param outputStream 要封装的OutputStream或者直接返回的outputStream，不能null
     * @param size 缓存大小
     * @return  直接传入的OutputStream或一个新的 [BufferedOutputStream]
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @JvmStatic
    fun buffer(outputStream: OutputStream, size: Int): BufferedOutputStream {
        return if (outputStream is BufferedOutputStream)
            outputStream
        else
            BufferedOutputStream(outputStream, size)
    }


    /**
     * 若inputStream是一个[BufferedInputStream]则直接返回这个inputStream，否则基于inputStream创建一个新
     * BufferedInputStream并返回。
     *
     * @param inputStream 直接返回传入的inputStream或封装后的InputStream，不能null
     * @return 传入的InputStream 或 一个新创建的 [BufferedInputStream] 对象
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @JvmStatic
    fun buffer(inputStream: InputStream): BufferedInputStream {
        return if (inputStream is BufferedInputStream)
            inputStream
        else
            BufferedInputStream(inputStream)
    }


    /**
     * 若inputStream是一个[BufferedInputStream]则直接返回这个inputStream，否则基于inputStream创建一个新
     * BufferedInputStream并返回。
     *
     * @param inputStream 直接返回传入的inputStream或封装后的InputStream，不能null
     * @param size 缓存大小
     * @return 传入的InputStream 或 一个新创建的 [BufferedInputStream] 对象
     * @throws NullPointerException if the input parameter is null
     * @since 2.5
     */
    @JvmStatic
    fun buffer(inputStream: InputStream, size: Int): BufferedInputStream {
        return if (inputStream is BufferedInputStream)
            inputStream
        else
            BufferedInputStream(inputStream, size)
    }

    /**
     * 将 一个`InputStream`内容读取到`byte[]`。
     *
     * 这个内部就有缓存，因此不必要使用`BufferedInputStream`。
     *
     * @param input 从中读取内容的`InputStream`
     * @return 最终的字节数组
     * @throws NullPointerException if the input is null
     * @throws IOException  若发生IO错误
     */
    @Throws(IOException::class)
    @JvmStatic
    fun toByteArray(input: InputStream): ByteArray {
        ByteArrayOutputStream().use { output ->
            copy(input, output)
            return output.toByteArray()
        }
    }

    /**
     * 将 一个`InputStream`内容读取到`byte[]`。
     * 当`InputStream`大小可知使用`toByteArray(InputStream)`方法。
     *
     * **NOTE:** 在使用[toByteArray]读取字节数组前，此方法检查不截断前提下的长度是否可以安全地转换为int，
     * 不论什么情况下，数组大小都不能大于Integer.MAX_VALUE。
     *
     * @param input  从中读取的`InputStream`
     * @param size `InputStream`的大小
     * @return 要求返回的字节数组
     * @throws IOException              发生IO错误，或InputStream大小与参数size不同
     * @throws IllegalArgumentException 大小小于0，或大于Integer.MAX_VALUE
     * @see toByteArray
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(input: InputStream, size: Long): ByteArray {

        require(size <= Integer.MAX_VALUE) { "Size cannot be greater than Integer max value: $size" }

        return toByteArray(input, size.toInt())
    }

    /**
     * 将 一个`InputStream`内容读取到`byte[]`。
     * 当`InputStream`大小可知使用`toByteArray(InputStream)`方法。
     *
     * **NOTE:** 在使用[toByteArray]读取字节数组前，此方法检查不截断前提下的长度是否可以安全地转换为int，
     * 不论什么情况下，数组大小都不能大于Integer.MAX_VALUE。
     *
     * @param input  从中读取的`InputStream`
     * @param size `InputStream`的大小
     * @return 要求返回的字节数组
     * @throws IOException              发生IO错误，或InputStream大小与参数size不同
     * @throws IllegalArgumentException 大小小于0
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(input: InputStream, size: Int): ByteArray {

        require(size >= 0) { "Size must be equal or greater than zero: $size" }

        if (size == 0) {
            return ByteArray(0)
        }

        val data = ByteArray(size)
        var offset = 0
        var read: Int = input.read(data, offset, size - offset)
        while (offset < size && read != EOF) {
            offset += read
            read = input.read(data, offset, size - offset)
        }

        if (offset != size) {
            throw IOException("Unexpected read size. current: $offset, expected: $size")
        }

        return data
    }


    /**
     * 用给定编码从Reader读取内容到byte[]。
     *
     * @param input  从中读取的Reader
     * @param encoding  编码，null表示系统默认
     * @return 按要求返回的数组
     * @throws IOException          发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(input: Reader, encoding: Charset?): ByteArray {
        ByteArrayOutputStream().use { output ->
            copy(input, output, encoding)
            return output.toByteArray()
        }
    }

    /**
     * 用给定编码从Reader读取内容到byte[]。
     *
     * @param input  从中读取的Reader
     * @param encoding  编码，null表示系统默认
     * @return 按要求返回的数组
     * @throws IOException          发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of
     * [.UnsupportedEncodingException][java.io] in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(input: Reader, encoding: String?): ByteArray {
        return toByteArray(input, Charsets.toCharset(encoding))
    }

    /**
     * 从URI读取内容到byte[]。
     *
     * @param uri 源`URI`
     * @return 要求的byte[]
     * @throws IOException       发生IO错误
     * @since 2.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(uri: URI): ByteArray {
        return toByteArray(uri.toURL())
    }

    /**
     * 从URL读取内容到byte[]。
     *
     * @param url  源`URL`
     * @return 要求的byte[]
     * @throws IOException          发生IO错误
     * @since 2.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(url: URL): ByteArray {
        val conn = url.openConnection()
        try {
            return toByteArray(conn)
        } finally {
            close(conn)
        }
    }

    /**
     * 从URLConnection读取内容到byte[]。
     *
     * @param urlConn 源`URLConnection`
     * @return 要求的byte[]
     * @throws IOException          发生IO错误
     * @since 2.4
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(urlConn: URLConnection): ByteArray {
        urlConn.getInputStream().use { inputStream -> return toByteArray(inputStream) }
    }

    /**
     * 使用编码读取InputStream内容到一个到字符数组(CharArray)。
     *
     *
     * @param is  从中读取数据的`InputStream`
     * @param encoding 编码，null表示默认
     * @return 返回的字符数组
     * @throws IOException          发生I/O错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toCharArray(`is`: InputStream, encoding: Charset?): CharArray {
        val output = CharArrayWriter()
        copy(`is`, output, encoding)
        return output.toCharArray()
    }

    /**
     * 使用编码读取InputStream内容到一个到字符数组(CharArray)。
     *
     *
     * @param is  从中读取数据的`InputStream`
     * @param encoding 编码，null表示默认
     * @return 返回的字符数组
     * @throws IOException          发生I/O错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toCharArray(`is`: InputStream, encoding: String?): CharArray {
        return toCharArray(`is`, Charsets.toCharset(encoding))
    }

    /**
     * 读取Reader内容到一个到字符数组(CharArray)。
     *
     *
     * @param input 读取数据的Reader
     * @return 返回的字符数组
     * @throws IOException          发生I/O错误
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toCharArray(input: Reader): CharArray {
        val sw = CharArrayWriter()
        copy(input, sw)
        return sw.toCharArray()
    }

    // copy from InputStream
    /**
     * 将一个`InputStream`中字节拷贝到一个`OutputStream`。
     *
     * 因方法内部实现有缓存，因此不使用`BufferedInputStream`。
     *
     * 大的输入/输出流(大于2GB)将在完成拷贝后返回一个字节拷贝值`-1`，
     * 因为无法返回准确的int类型字节数。因为large streams 使用`copyLarge(InputStream, OutputStream)`
     * 方法。
     *
     * @param input 读取数据的InputStream
     * @param output 写入数据的OutputStream
     * @return 拷贝的字节数，或者-1若大于Integer.MAX_VALUE
     * @throws IOException  发生I/O错误
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream): Int {
        val count = copyLarge(input, output)
        return if (count > Integer.MAX_VALUE) {
            -1
        } else count.toInt()
    }

    /**
     * 使用定义好大小的缓存进行数据拷贝，将数据从`InputStream` 拷贝到 `OutputStream`。
     *
     * @param input 读取数据的InputStream
     * @param output 写入数据的OutputStream
     * @param bufferSize 缓存大小
     * @return 拷贝的字节数
     * @throws IOException  发生I/O错误
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream, bufferSize: Int): Long {
        return copyLarge(input, output, ByteArray(bufferSize))
    }

    /**
     * 使用定义好大小的缓存进行数据拷贝，将数据从`InputStream` 拷贝到 `OutputStream`。
     *
     * 给定缓存的大小是 [.DEFAULT_BUFFER_SIZE].
     *
     * @param input 读取数据的InputStream
     * @param output 写入数据的OutputStream
     * @return 拷贝的字节数
     * @throws IOException  发生I/O错误
     * @since 1.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyLarge(input: InputStream, output: OutputStream): Long {
        return copy(input, output, DEFAULT_BUFFER_SIZE)
    }


    /**
     *
     * 从一个大于2GB的`InputStream`中将字节数据复制到一个`OutputStream`。
     *
     * @param input 读取数据的InputStream
     * @param output 写入数据的OutputStream
     * @param buffer 复制使用的缓存
     * @return 复制的字节数
     * @throws IOException  发生I/O错误
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyLarge(input: InputStream, output: OutputStream, buffer: ByteArray): Long {
        var count: Long = 0
        var n: Int = input.read(buffer)
        while (EOF != n) {
            output.write(buffer, 0, n)
            count += n.toLong()
            n = input.read(buffer)
        }
        return count
    }


    /**
     * 从一个大于2GB的`InputStream`中将字节数据复制到一个`OutputStream`，可选跳过输入的字节。
     *
     *
     * 注意使用[.skip]的实现。这表示这个方法可能比实际使用skip实现的方法低效。
     *
     *  缓存大小[.DEFAULT_BUFFER_SIZE].
     *
     * @param input 读取数据的InputStream
     * @param output 写入数据的OutputStream
     * @param inputOffset  跳过的字节数
     * @param length 复制的字符数
     * @return 复制的字节数
     * @throws IOException  发生I/O错误
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyLarge(input: InputStream, output: OutputStream, inputOffset: Long, length: Long): Long {
        return copyLarge(input, output, inputOffset, length, ByteArray(DEFAULT_BUFFER_SIZE))
    }

    /**
     * 从一个大于2GB的`InputStream`中将字节数据复制到一个`OutputStream`，可选跳过输入的字节。
     *
     *
     * 注意使用[.skip]的实现。这表示这个方法可能比实际使用skip实现的方法低效。
     *
     *
     * @param input 读取数据的InputStream
     * @param output 写入数据的OutputStream
     * @param inputOffset  跳过的字节数
     * @param length  复制的字节数
     * @param buffer 复制使用的缓存
     * @return 复制的数量
     * @throws IOException 发生IO错误
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyLarge(input: InputStream, output: OutputStream,
                  inputOffset: Long, length: Long, buffer: ByteArray): Long {
        if (inputOffset > 0) {
            skipFully(input, inputOffset)
        }
        if (length == 0L) {
            return 0
        }
        val bufferLength = buffer.size
        var bytesToRead = bufferLength
        if (length in 1 until bufferLength) {
            bytesToRead = length.toInt()
        }
        var read: Int = input.read(buffer, 0, bytesToRead)
        var totalRead: Long = 0
        while (bytesToRead > 0 && EOF != read) {
            output.write(buffer, 0, read)
            totalRead += read.toLong()
            if (length > 0) { // only adjust length if not reading to the end
                // Note the cast must work because buffer.length is an integer
                bytesToRead = Math.min(length - totalRead, bufferLength.toLong()).toInt()
            }
            read = input.read(buffer, 0, bytesToRead)
        }
        return totalRead
    }

    /**
     * 从一个 `InputStream` 将字节数据在编码后复制到`Writer`。
     *
     * @param input 读取数据的InputStream
     * @param output 写入的Writer
     * @param inputEncoding 针对输入流内数据使用的编码，null表示平台默认
     * @throws IOException 发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: InputStream, output: Writer, inputEncoding: Charset?) {
        val `in` = InputStreamReader(input,
                Charsets.toCharset(inputEncoding))
        copy(`in`, output)
    }

    /**
     * 从Reader复制字符数据到Writer。
     *
     *
     * 大于2GB的输入流在复制结束后返回-1，因为无法返回int类型的值。
     * 可以换用`copyLarge(Reader, Writer)` 。
     *
     * @param input 读取的`Reader`
     * @param output 写入的`Writer`
     * @return 复制的字符数，若大于Integer.MAX_VALUE返回-1
     * @throws IOException 发生IO错误
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: Reader, output: Writer): Int {
        val count = copyLarge(input, output)
        return if (count > Integer.MAX_VALUE) {
            -1
        } else count.toInt()
    }

    /**
     * Character encoding names can be found at
     * [IANA](http://www.iana.org/assignments/character-sets).
     *
     *
     * @param input the `InputStream` to read from
     * @param output the `Writer` to write to
     * @param inputEncoding the encoding to use for the InputStream, null means platform default
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [ .UnsupportedEncodingException][java.io] in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: InputStream, output: Writer, inputEncoding: String) {
        copy(input, output, Charsets.toCharset(inputEncoding))
    }

    /**
     * 从大于2GB的Reader中复制数据到Writer。
     *
     * 缓存大小 [.DEFAULT_BUFFER_SIZE].
     *
     * @param input 读取的`Reader`
     * @param output 写入的`Writer`
     * @return 复制的字符数
     * @throws IOException 发生IO错误
     * @since 1.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyLarge(input: Reader, output: Writer): Long {
        return copyLarge(input, output, CharArray(DEFAULT_BUFFER_SIZE))
    }

    /**
     * 从大于2GB的Reader中复制数据到Writer。
     *
     * @param input 读取的`Reader`
     * @param output 写入的`Writer`
     * @param buffer 复制使用的缓存
     * @return 复制的字符数
     * @throws IOException 发生IO错误
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyLarge(input: Reader, output: Writer, buffer: CharArray): Long {
        var count: Long = 0
        var n: Int = input.read(buffer)
        while (EOF != n) {
            output.write(buffer, 0, n)
            count += n.toLong()
            n = input.read(buffer)
        }
        return count
    }

    /**
     *
     * 缓存大小 [.DEFAULT_BUFFER_SIZE].
     *
     * @param input the `Reader` to read from
     * @param output the `Writer` to write to
     * @param inputOffset : number of chars to skip from input before copying -ve values are
     * ignored
     * @param length : number of chars to copy. -ve means all
     * @return the number of chars copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyLarge(input: Reader, output: Writer, inputOffset: Long, length: Long): Long {
        return copyLarge(input, output, inputOffset, length, CharArray(DEFAULT_BUFFER_SIZE))
    }

    /**
     * @param input the `Reader` to read from
     * @param output the `Writer` to write to
     * @param inputOffset : number of chars to skip from input before copying -ve values are
     * ignored
     * @param length : number of chars to copy. -ve means all
     * @param buffer the buffer to be used for the copy
     * @return the number of chars copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyLarge(input: Reader, output: Writer,
                  inputOffset: Long, length: Long,
                  buffer: CharArray): Long {
        if (inputOffset > 0) {
            skipFully(input, inputOffset)
        }
        if (length == 0L) {
            return 0
        }
        var bytesToRead = buffer.size
        if (length > 0 && length < buffer.size) {
            bytesToRead = length.toInt()
        }
        var read: Int = input.read(buffer, 0, bytesToRead)
        var totalRead: Long = 0
        while (bytesToRead > 0 && EOF != read) {
            output.write(buffer, 0, read)
            totalRead += read.toLong()
            if (length > 0) { // only adjust length if not reading to the end
                // Note the cast must work because buffer.length is an integer
                bytesToRead = Math.min(length - totalRead, buffer.size.toLong()).toInt()
            }
            read = input.read(buffer, 0, bytesToRead)
        }
        return totalRead
    }


    /**
     * 使用给定的字符编码从Reader读取数据到OutputStream。
     *
     * @param input the `Reader` to read from
     * @param output the `OutputStream` to write to
     * @param outputEncoding the encoding to use for the OutputStream, null means platform default
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: Reader, output: OutputStream, outputEncoding: Charset?) {
        val out = OutputStreamWriter(output, Charsets.toCharset(outputEncoding))
        copy(input, out)
        // XXX Unless anyone is planning on rewriting OutputStreamWriter,
        // we have to flush here.
        out.flush()
    }

    /**
     *
     * @param input the `Reader` to read from
     * @param output the `OutputStream` to write to
     * @param outputEncoding the encoding to use for the OutputStream, null means platform default
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [ .UnsupportedEncodingException][java.io] in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: Reader, output: OutputStream, outputEncoding: String) {
        copy(input, output, Charsets.toCharset(outputEncoding))
    }


    /**
     * 比较两个streams的内容相等情况。
     *
     * @param input1 the first stream
     * @param input2 the second stream
     * @return true if the content of the streams are equal or they both don't exist, false
     * otherwise
     * @throws IOException if an I/O error occurs
     */
    @JvmStatic
    @Throws(IOException::class)
    fun contentEquals(input1: InputStream, input2: InputStream): Boolean {
        var input1 = input1
        var input2 = input2
        if (input1 === input2) {
            return true
        }
        if (input1 !is BufferedInputStream) {
            input1 = BufferedInputStream(input1)
        }
        if (input2 !is BufferedInputStream) {
            input2 = BufferedInputStream(input2)
        }

        var ch = input1.read()
        while (EOF != ch) {
            val ch2 = input2.read()
            if (ch != ch2) {
                return false
            }
            ch = input1.read()
        }

        val ch2 = input2.read()
        return ch2 == EOF
    }


    /**
     * 比较两个readers的内容相等情况。
     *
     *
     * @param input1 the first reader
     * @param input2 the second reader
     * @return true if the content of the readers are equal or they both don't exist, false
     * otherwise
     * @throws NullPointerException if either input is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun contentEquals(input1: Reader, input2: Reader): Boolean {
        var input1 = input1
        var input2 = input2
        if (input1 === input2) {
            return true
        }

        input1 = toBufferedReader(input1)
        input2 = toBufferedReader(input2)

        var ch = input1.read()
        while (EOF != ch) {
            val ch2 = input2.read()
            if (ch != ch2) {
                return false
            }
            ch = input1.read()
        }

        val ch2 = input2.read()
        return ch2 == EOF
    }

    /**
     * @param input1 the first reader
     * @param input2 the second reader
     * @return true if the content of the readers are equal (ignoring EOL differences),  false
     * otherwise
     * @throws NullPointerException if either input is null
     * @throws IOException if an I/O error occurs
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun contentEqualsIgnoreEOL(input1: Reader, input2: Reader): Boolean {
        if (input1 === input2) {
            return true
        }
        val br1 = toBufferedReader(input1)
        val br2 = toBufferedReader(input2)

        var line1: String? = br1.readLine()
        var line2: String? = br2.readLine()
        while (line1 != null && line2 != null && line1 == line2) {
            line1 = br1.readLine()
            line2 = br2.readLine()
        }
        return if (line1 == null) line2 == null else line1 == line2
    }


    /**
     * 在一个InputStream字节流跳过部分字节。这样实现确保在结束之前可以读取可能多的数据。这与[InputStream]
     * 子类实现中的skip()不同。
     *
     * 注意注意是使用[InputStream.read]而不是[InputStream.skip]。这个方法在使用时效率没有实际的skip()
     * 方高，但这确保了跳过的字节数的正确性。
     *
     * @param input byte stream to skip
     * @param toSkip number of bytes to skip.
     * @return number of bytes actually skipped.
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @see InputStream.skip
     * @see [IO-203 - Add skipFully](https://issues.apache.org/jira/browse/IO-203)
     * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun skip(input: InputStream, toSkip: Long): Long {
        require(toSkip >= 0) { "Skip count must be non-negative, actual: $toSkip" }
        /*
         * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
         * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
         * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
         */
        if (SKIP_BYTE_BUFFER == null) {
            SKIP_BYTE_BUFFER = ByteArray(SKIP_BUFFER_SIZE)
        }
        var remain = toSkip
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            val n = input
                    .read(SKIP_BYTE_BUFFER, 0, Math.min(remain, SKIP_BUFFER_SIZE.toLong()).toInt()).toLong()
            if (n < 0) { // EOF
                break
            }
            remain -= n
        }
        return toSkip - remain
    }

    /**
     * @param input character stream to skip
     * @param toSkip number of characters to skip.
     * @return number of characters actually skipped.
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @see Reader.skip
     * @see [IO-203 - Add skipFully](https://issues.apache.org/jira/browse/IO-203)
     * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun skip(input: Reader, toSkip: Long): Long {
        require(toSkip >= 0) { "Skip count must be non-negative, actual: $toSkip" }
        /*
         * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
         * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
         * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
         */
        if (SKIP_CHAR_BUFFER == null) {
            SKIP_CHAR_BUFFER = CharArray(SKIP_BUFFER_SIZE)
        }
        var remain = toSkip
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            val n = input
                    .read(SKIP_CHAR_BUFFER, 0, Math.min(remain, SKIP_BUFFER_SIZE.toLong()).toInt()).toLong()
            if (n < 0) { // EOF
                break
            }
            remain -= n
        }
        return toSkip - remain
    }

    /**
     * @param input ReadableByteChannel to skip
     * @param toSkip number of bytes to skip.
     * @return number of bytes actually skipped.
     * @throws IOException if there is a problem reading the ReadableByteChannel
     * @throws IllegalArgumentException if toSkip is negative
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun skip(input: ReadableByteChannel, toSkip: Long): Long {
        require(toSkip >= 0) { "Skip count must be non-negative, actual: $toSkip" }
        val skipByteBuffer = ByteBuffer
                .allocate(Math.min(toSkip, SKIP_BUFFER_SIZE.toLong()).toInt())
        var remain = toSkip
        while (remain > 0) {
            skipByteBuffer.position(0)
            skipByteBuffer.limit(Math.min(remain, SKIP_BUFFER_SIZE.toLong()).toInt())
            val n = input.read(skipByteBuffer)
            if (n == EOF) {
                break
            }
            remain -= n.toLong()
        }
        return toSkip - remain
    }

    /**
     * @param input stream to skip
     * @param toSkip the number of bytes to skip
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @throws EOFException if the number of bytes skipped was incorrect
     * @see InputStream.skip
     * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun skipFully(input: InputStream, toSkip: Long) {
        require(toSkip >= 0) { "Bytes to skip must not be negative: $toSkip" }
        val skipped = skip(input, toSkip)
        if (skipped != toSkip) {
            throw EOFException("Bytes to skip: $toSkip actual: $skipped")
        }
    }


    /**
     * @param input ReadableByteChannel to skip
     * @param toSkip the number of bytes to skip
     * @throws IOException if there is a problem reading the ReadableByteChannel
     * @throws IllegalArgumentException if toSkip is negative
     * @throws EOFException if the number of bytes skipped was incorrect
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun skipFully(input: ReadableByteChannel, toSkip: Long) {
        require(toSkip >= 0) { "Bytes to skip must not be negative: $toSkip" }
        val skipped = skip(input, toSkip)
        if (skipped != toSkip) {
            throw EOFException("Bytes to skip: $toSkip actual: $skipped")
        }
    }

    /**
     * @param input stream to skip
     * @param toSkip the number of characters to skip
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @throws EOFException if the number of characters skipped was incorrect
     * @see Reader.skip
     * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun skipFully(input: Reader, toSkip: Long) {
        val skipped = skip(input, toSkip)
        if (skipped != toSkip) {
            throw EOFException("Chars to skip: $toSkip actual: $skipped")
        }
    }


    /**
     * @param input where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(input: Reader, buffer: CharArray, offset: Int, length: Int): Int {
        require(length >= 0) { "Length must not be negative: $length" }
        var remaining = length
        while (remaining > 0) {
            val location = length - remaining
            val count = input.read(buffer, offset + location, remaining)
            if (EOF == count) { // EOF
                break
            }
            remaining -= count
        }
        return length - remaining
    }


    /**
     * @param input where to read input from
     * @param buffer destination
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(input: Reader, buffer: CharArray): Int {
        return read(input, buffer, 0, buffer.size)
    }


    /**
     * @param input 从中读取内容的InputStream
     * @param buffer 目标缓存
     * @param offset 缓存初始偏移位
     * @param length 读取的长度，必须大于0
     * @return  读取的长度，若到达EOF可能比需要读取的length小
     * @throws IOException 发生IO错误
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(input: InputStream, buffer: ByteArray, offset: Int, length: Int): Int {
        require(length >= 0) { "Length must not be negative: $length" }
        var remaining = length
        while (remaining > 0) {
            val location = length - remaining
            val count = input.read(buffer, offset + location, remaining)
            if (EOF == count) { // EOF
                break
            }
            remaining -= count
        }
        return length - remaining
    }


    /**
     * @param input 读取的InputStream
     * @param buffer 读入到buffer
     * @return  实际读取到的内容长度，若达到最后即等于EOF值时返回的值小于需求的大小
     * @throws IOException 发生读取错误
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(input: InputStream, buffer: ByteArray): Int {
        return read(input, buffer, 0, buffer.size)
    }


    /**
     * @param input the byte channel to read
     * @param buffer byte buffer destination
     * @return the actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(input: ReadableByteChannel, buffer: ByteBuffer): Int {
        val length = buffer.remaining()
        while (buffer.remaining() > 0) {
            val count = input.read(buffer)
            if (EOF == count) { // EOF
                break
            }
        }
        return length - buffer.remaining()
    }

    /**
     * @param input where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException if the number of characters read was incorrect
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readFully(input: Reader, buffer: CharArray, offset: Int, length: Int) {
        val actual = read(input, buffer, offset, length)
        if (actual != length) {
            throw EOFException("Length to read: $length actual: $actual")
        }
    }

    /**
     * @param input where to read input from
     * @param buffer destination
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException if the number of characters read was incorrect
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readFully(input: Reader, buffer: CharArray) {
        readFully(input, buffer, 0, buffer.size)
    }

    /**
     * @param input where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException if the number of bytes read was incorrect
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readFully(input: InputStream, buffer: ByteArray, offset: Int, length: Int) {
        val actual = read(input, buffer, offset, length)
        if (actual != length) {
            throw EOFException("Length to read: $length actual: $actual")
        }
    }

    /**
     * @param input where to read input from
     * @param buffer destination
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException if the number of bytes read was incorrect
     * @since 2.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readFully(input: InputStream, buffer: ByteArray) {
        readFully(input, buffer, 0, buffer.size)
    }

    /**
     * @param input where to read input from
     * @param length length to read, must be &gt;= 0
     * @return the bytes read from input
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if length is negative
     * @throws EOFException if the number of bytes read was incorrect
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readFully(input: InputStream, length: Int): ByteArray {
        val buffer = ByteArray(length)
        readFully(input, buffer, 0, buffer.size)
        return buffer
    }

    /**
     * @param input the byte channel to read
     * @param buffer byte buffer destination
     * @throws IOException if there is a problem reading the file
     * @throws EOFException if the number of bytes read was incorrect
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readFully(input: ReadableByteChannel, buffer: ByteBuffer) {
        val expected = buffer.remaining()
        val actual = read(input, buffer)
        if (actual != expected) {
            throw EOFException("Length to read: $expected actual: $actual")
        }
    }

    /**
     * Returns an Iterator for the lines in a `Reader`.
     *
     *
     * `LineIterator`持有Reader引用。使用完iterator后应该关闭reader释放资源。
     * 可以直接调用reader的close()关闭，也可以调用[LineIterator.close]。
     *
     * 推荐使用方式：
     * <pre>
     * try {
     * val it = IOUtil.lineIterator(reader);
     * while (it.hasNext()) {
     * val line = it.nextLine();
     * /// do something with line
     * }
     * } finally {
     * IOUtil.close(reader);
     * }
    </pre> *
     *
     * @param reader  读取数据的Reader，不能 null
     * @return reader上iterator
     * @since 1.2
     */
    @JvmStatic
    fun lineIterator(reader: Reader): LineIterator {
        return LineIterator(reader)
    }

    /**
     * 返回`InputStream`上的迭代器，并且使用给定的字符编码集(null表示平台默认)。
     * 推荐使用方式：
     * <pre>
     * try {
     * val it = IOUtil.lineIterator(reader, charset);
     * while (it.hasNext()) {
     * val line = it.nextLine();
     * /// do something with line
     * }
     * } finally {
     * IOUtil.close(reader);
     * }
    </pre> *
     *
     * @param input the `InputStream` to read from, not null
     * @param encoding the encoding to use, null means platform default
     * @return an Iterator of the lines in the reader, never null
     * @throws IllegalArgumentException if the input is null
     * @throws IOException              if an I/O error occurs, such as if the encoding is invalid
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun lineIterator(input: InputStream, encoding: Charset?): LineIterator {
        return LineIterator(InputStreamReader(input, Charsets.toCharset(encoding)))
    }

    /**
     * 返回`InputStream`上的迭代器，并且使用给定的字符编码集(null表示平台默认)。
     * 推荐使用方式：
     * <pre>
     * try {
     * val it = IOUtil.lineIterator(reader, charset);
     * while (it.hasNext()) {
     * val line = it.nextLine();
     * /// do something with line
     * }
     * } finally {
     * IOUtil.close(reader);
     * }
    </pre> *
     *
     * @param input the `InputStream` to read from, not null
     * @param encoding the encoding to use, null means platform default
     * @return an Iterator of the lines in the reader, never null
     * @throws IllegalArgumentException if the input is null
     * @throws IOException              if an I/O error occurs, such as if the encoding is invalid
     * @since 1.2
     */
    @JvmStatic
    @Throws(IOException::class)
    fun lineIterator(input: InputStream, encoding: String?): LineIterator {
        return lineIterator(input, Charsets.toCharset(encoding))
    }

    // write byte[]
    //-----------------------------------------------------------------------

    /**
     * 将byte[]以给定编码写出到OutputStream。
     *
     * @param data 写出的byte[]
     * @param output  写出的`OutputStream`
     * @throws IOException          发生IO错误
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: ByteArray?, output: OutputStream) {
        if (data != null) {
            output.write(data)
        }
    }

    /**
     * 将byte[]块方法写出到OutputStream，避免内存溢出。
     *
     * @param data 写出的byte[]
     * @param output  写出的`OutputStream`
     * @throws IOException          发生IO错误
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeChunked(data: ByteArray?, output: OutputStream) {
        if (data != null) {
            var bytes = data.size
            var offset = 0
            while (bytes > 0) {
                val chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE)
                output.write(data, offset, chunk)
                bytes -= chunk
                offset += chunk
            }
        }
    }

    /**
     * 将byte[]以给定编码写出到Writer。
     *
     * @param data 写出的byte[]
     * @param output  写出的`Writer`
     * @param encoding 编码，null表示默认
     * @throws IOException                                 发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: ByteArray?, output: Writer, encoding: Charset?) {
        if (data != null) {
            output.write(String(data, Charsets.toCharset(encoding)))
        }
    }

    /**
     * 将byte[]以给定编码写出到Writer。
     *
     * @param data 写出的byte[]
     * @param output  写出的`Writer`
     * @param encoding 编码，null表示默认
     * @throws IOException                                 发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: ByteArray?, output: Writer, encoding: String?) {
        write(data, output, Charsets.toCharset(encoding))
    }

    // write char[]
    //-----------------------------------------------------------------------

    /**
     *  将`char[]`块方法写入到Writer。
     *
     * @param data 输出的字符数组，写出时不修改，忽略null
     * @param output  写出`Writer`
     * @throws IOException          发生IO错误
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: CharArray?, output: Writer) {
        if (data != null) {
            output.write(data)
        }
    }

    /**
     * 将`char[]`块方法写入到Writer。
     * 这样做为了避免在输出一个大的字节数组时可能发生的内存溢出。
     *
     * @param data 输出的字符数组，写出时不修改，忽略null
     * @param output  写出`Writer`
     * @throws IOException          发生IO错误
     * @since 2.5
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeChunked(data: CharArray?, output: Writer) {
        if (data != null) {
            var bytes = data.size
            var offset = 0
            while (bytes > 0) {
                val chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE)
                output.write(data, offset, chunk)
                bytes -= chunk
                offset += chunk
            }
        }
    }

    /**
     * 将`char[]`以给定编码写出的到`OutputStream`。
     *
     * 编码集可见[IANA](http://www.iana.org/assignments/character-sets).
     *
     *
     * @param data 输出的字符数组，写出时不修改，忽略null
     * @param output  写出的`OutputStream`
     * @param encoding 使用的编码，null表示默认
     * @throws IOException          发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: CharArray?, output: OutputStream, encoding: Charset?) {
        if (data != null) {
            output.write(String(data).toByteArray(Charsets.toCharset(encoding)))
        }
    }

    /**
     * 将`char[]`以给定编码写出的到`OutputStream`。
     *
     * 编码集可见[IANA](http://www.iana.org/assignments/character-sets).
     *
     *
     * @param data 输出的字符数组，写出时不修改，忽略null
     * @param output  写出的`OutputStream`
     * @param encoding 使用的编码，null表示默认
     * @throws IOException                                  发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [ .UnsupportedEncodingException][java.io] in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: CharArray, output: OutputStream, encoding: String?) {
        write(data, output, Charsets.toCharset(encoding))
    }

    /**
     * 将字符写到`Writer`.
     *
     * @param data 要输出的CharSequence， null不处理
     * @param output 写出的Writer
     * @throws IOException          发生IO错误
     * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: CharSequence?, output: Writer) {
        if (data != null) {
            write(data.toString(), output)
        }
    }

    /**
     * 将`CharSequence`以给定编码写出的到`OutputStream`。
     *
     *
     * @param data  要写出的`CharSequence`， null忽略
     * @param output  写出的`OutputStream`
     * @param encoding 使用的编码，null表示默认
     * @throws IOException          发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: CharSequence?, output: OutputStream, encoding: Charset?) {
        if (data != null) {
            write(data.toString(), output, encoding)
        }
    }

    /**
     * 将`CharSequence`以给定编码写出的到`OutputStream`。
     *
     *
     * @param data  要写出的`CharSequence`， null忽略
     * @param output  写出的`OutputStream`
     * @param encoding 使用的编码，null表示默认
     * @throws IOException          发生IO错误
     * * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: CharSequence?, output: OutputStream, encoding: String?) {
        write(data, output, Charsets.toCharset(encoding))
    }

    // write String
    //-----------------------------------------------------------------------

    /**
     * 将`String` 写出到 `Writer`。
     *
     * @param data 要写出的String
     * @param output  写出的`Writer`
     * @throws IOException          发生IO错误
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: String?, output: Writer) {
        if (data != null) {
            output.write(data)
        }
    }

    /**
     * 将String以给定的编码写出到`OutputStream`。
     *
     *
     * @param data 要写出的String
     * @param output  写出的`Writer`
     * @param encoding 使用的编码，null表示默认
     * @throws IOException          发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: String?, output: OutputStream, encoding: Charset?) {
        if (data != null) {
            output.write(data.toByteArray(Charsets.toCharset(encoding)))
        }
    }

    /**
     * 将String以给定的编码写出到`OutputStream`。
     *
     *
     * @param data 要写出的String
     * @param output  写出的`Writer`
     * @param encoding 使用的编码，null表示默认
     * @throws IOException          发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [ .UnsupportedEncodingException][java.io] in version 2.2 if the encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun write(data: String?, output: OutputStream, encoding: String?) {
        write(data, output, Charsets.toCharset(encoding))
    }

    // write StringBuffer
    //-----------------------------------------------------------------------
    /**
     * 将一个集合中每个item`toString()`方法返回值以给定编码逐行写入到`OutputStream`。
     *
     *
     * @param lines 数据集合，每个item即一行， null实体产生空行
     * @param lineEnding 行分隔符(换行符)，null表示系统默认
     * @param output  写出的`OutputStream`，不为null，也不会关闭
     * @param encoding 使用的编码，null表示默认
     * @throws NullPointerException if the output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(lines: Collection<*>?, lineEnding: String?, output: OutputStream,
                   encoding: Charset?) {
        var lineEnd = lineEnding
        if (lines == null) {
            return
        }
        if (lineEnd == null) {
            lineEnd = LINE_SEPARATOR
        }
        val cs = Charsets.toCharset(encoding)
        for (line in lines) {
            if (line != null) {
                output.write(line.toString().toByteArray(cs))
            }
            output.write(lineEnd.toByteArray(cs))
        }
    }

    /**
     * 将一个集合中每个item`toString()`方法返回值以给定编码逐行写入到`OutputStream`。
     *
     *
     * @param lines 数据集合，每个item即一行， null实体产生空行
     * @param lineEnding 行分隔符(换行符)，null表示系统默认
     * @param output  写出的`OutputStream`，不为null，也不会关闭
     * @param encoding 使用的编码，null表示默认
     * @throws NullPointerException                         if the output is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(lines: Collection<*>?, lineEnding: String?,
                   output: OutputStream, encoding: String?) {
        writeLines(lines, lineEnding, output, Charsets.toCharset(encoding))
    }

    /**
     * 将一个集合中每个item`toString()`方法返回值以给定编码逐行写入到`Writer`。
     *
     * @param lines 数据集合，每个item即一行， null实体产生空行
     * @param lineEnding 行分隔符(换行符)，null表示系统默认
     * @param writer  写出的`Writer`，不为null，也不会关闭
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLines(lines: Collection<*>?, lineEnding: String?,
                   writer: Writer) {
        var lineEnd = lineEnding
        if (lines == null) {
            return
        }
        if (lineEnd == null) {
            lineEnd = LINE_SEPARATOR
        }
        for (line in lines) {
            if (line != null) {
                writer.write(line.toString())
            }
            writer.write(lineEnd)
        }
    }

    // read toString
    //-----------------------------------------------------------------------

    /**
     * 从InputStream读取内并返回String(包含编码)。
     *
     *
     * @param input  从中读取内容的InputStream
     * @param encoding 编码，null表示默认
     * @return 按要求返回的String
     * @throws IOException          发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toString(input: InputStream, encoding: Charset?): String {
        StringBuilderWriter().use { sw ->
            copy(input, sw, encoding)
            return sw.toString()
        }
    }

    /**
     * 从InputStream读取内并返回String(包含编码)。
     *
     *
     * @param input  从中读取内容的InputStream
     * @param encoding 编码，null表示默认
     * @return 按要求返回的String
     * @throws IOException          发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toString(input: InputStream, encoding: String?): String {
        return toString(input, Charsets.toCharset(encoding))
    }

    /**
     * 从Reader读取内容并返回String。
     *
     *
     * @param input 从中读取数据的Reader
     * @return 返回String
     * @throws IOException          发生IO错误
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toString(input: Reader): String {
        StringBuilderWriter().use { sw ->
            copy(input, sw)
            return sw.toString()
        }
    }

    /**
     * 从URL读取数据。
     *
     * @param uri URI源
     * @param encoding URL地址内容的编码
     * @return URL地址读取的String
     * @throws IOException 发生IO错误
     * @since 2.3.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toString(uri: URI, encoding: Charset?): String {
        return toString(uri.toURL(), Charsets.toCharset(encoding))
    }

    /**
     * 从URL读取数据。
     *
     * @param uri URI源
     * @param encoding URL地址内容的编码
     * @return URL地址读取的String
     * @throws IOException 发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toString(uri: URI, encoding: String?): String {
        return toString(uri, Charsets.toCharset(encoding))
    }

    /**
     * 从URL读取数据。
     *
     * @param url URL源
     * @param encoding URL地址内容的编码
     * @return URL地址读取的String
     * @throws IOException 发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toString(url: URL, encoding: Charset?): String {
        url.openStream().use { inputStream -> return toString(inputStream, encoding) }
    }

    /**
     *  从URL读取内容。
     *
     * @param url  源URL
     * @param encoding URL地址内容的编码
     * @return URL地址读取的String
     * @throws IOException 发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     * @since 2.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toString(url: URL, encoding: String?): String {
        return toString(url, Charsets.toCharset(encoding))
    }

    /**
     * 使用编码将byte[]转换成String。
     *
     * 参考编码集[IANA](http://www.iana.org/assignments/character-sets).
     *
     * @param input 字节数组
     * @param encoding URL地址内容的编码，null表示系统默认
     * @return the requested String
     * @throws IOException          发生IO错误
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toString(input: ByteArray, encoding: String?): String {
        return String(input, Charsets.toCharset(encoding))
    }

    // readLines
    //-----------------------------------------------------------------------
    /**
     * 使用编码从InputStream读取内容到一个String列表，每行一个item。
     *
     * @param input  读取内容的`InputStream`， 不能null
     * @param encoding 编码，null表示系统默认
     * @return the list of Strings, never null
     * @throws IOException          发生IO错误
     * @since 2.3
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readLines(input: InputStream, encoding: Charset?): List<String> {
        val reader = InputStreamReader(input, Charsets.toCharset(encoding))
        return readLines(reader)
    }

    /**
     * 使用编码从InputStream读取内容到一个String列表，每行一个item。
     *
     * @param input  读取内容的`InputStream`， 不能null
     * @param encoding 编码，null表示系统默认
     * @return String列表
     * @throws IOException          发生IO错误
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readLines(input: InputStream, encoding: String?): List<String> {
        return readLines(input, Charsets.toCharset(encoding))
    }

    /**
     * 使用编码从Reader读取内容到一个String列表，每行一个item。
     *
     * @param input 读取内容的`Reader`， 不能null
     * @return String列表
     * @throws IOException          发生IO错误
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readLines(input: Reader): List<String> {
        val reader = toBufferedReader(input)
        val list = arrayListOf<String>()
        var line = reader.readLine()
        while (line != null) {
            list.add(line)
            line = reader.readLine()
        }
        return list
    }

    // resources
    //-----------------------------------------------------------------------

    /**
     * 使用编码将classpath资源内容读取到String。
     *
     * 预期给定的“名称”是绝对的。否则，就无法定义该行为。
     *
     *
     * @param name  需要的资源名
     * @param encoding  编码，null表示默认编码
     * @return 字符串
     * @throws IOException 发生IO错误
     *
     * @since 2.6
     */
    @JvmStatic
    @Throws(IOException::class)
    fun resourceToString(name: String, encoding: Charset?): String {
        return resourceToString(name, encoding, null)
    }

    /**
     * 使用编码将classpath资源内容读取到String。
     *
     * 预期给定的“名称”是绝对的。否则，就无法定义该行为。
     *
     *
     * @param name  需要的资源名
     * @param encoding  编码，null表示默认编码
     * @param classLoader 加载资源的loader
     * @return the requested String
     * @throws IOException 发生IO错误
     *
     * @since 2.6
     */
    @JvmStatic
    @Throws(IOException::class)
    fun resourceToString(name: String, encoding: Charset?, classLoader: ClassLoader?): String {
        return toString(resourceToURL(name, classLoader), encoding)
    }

    /**
     * 获取classpath资源内容到一个字节数组(ByteArray)。
     *
     * 预期给定的“名称”是绝对的。否则，就无法定义该行为。
     *
     * @param name 需要的资源名
     * @return 字节数组
     * @throws IOException 发生IO错误
     *
     * @since 2.6
     */
    @JvmStatic
    @Throws(IOException::class)
    fun resourceToByteArray(name: String): ByteArray {
        return resourceToByteArray(name, null)
    }

    /**
     * 获取classpath资源内容到一个字节数组(ByteArray)。
     *
     * 预期给定的“名称”是绝对的。否则，就无法定义该行为。
     *
     *
     * @param name 需要的资源名
     * @param classLoader 加载资源的loader
     * @return 字节数组
     * @throws IOException 发生IO错误
     *
     * @since 2.6
     */
    @JvmStatic
    @Throws(IOException::class)
    fun resourceToByteArray(name: String, classLoader: ClassLoader?): ByteArray {
        return toByteArray(resourceToURL(name, classLoader))
    }

    /**
     * 获取指向classpath资源的URL对象。
     *
     * 预期给定的“名称”是绝对的。否则，就无法定义该行为。
     *
     * @param name 需要的资源名
     * @return 资源的URL表示
     * @throws IOException 发生IO错误
     *
     * @since 2.6
     */
    @JvmStatic
    @Throws(IOException::class)
    fun resourceToURL(name: String): URL {
        return resourceToURL(name, null)
    }

    /**
     * 获取指向classpath资源的URL对象。
     *
     * 预期给定的“名称”是绝对的。否则，就无法定义该行为。
     *
     *
     * @param name 需要的资源名
     * @param classLoader 加载资源的loader
     * @return 资源的URL表示
     * @throws IOException 发生IO错误
     *
     * @since 2.6
     */
    @JvmStatic
    @Throws(IOException::class)
    fun resourceToURL(name: String, classLoader: ClassLoader?): URL {
        // What about the thread context class loader?
        // What about the system class loader?

        return (if (classLoader == null) IOUtil::class.java.getResource(name) else classLoader.getResource(name))
                ?: throw IOException("Resource not found: $name")
    }


    /**
     * 使用编码将CharSequence写入到InputStream。
     *
     *
     * @param input 要写入到stream的CharSequence
     * @param encoding 编码，null表示系统默认
     * @return 一个input stream
     * @throws IOException  encoding非法
     * @since 2.3
     */
    @JvmStatic
    fun toInputStream(input: CharSequence, encoding: Charset?): InputStream {
        return toInputStream(input.toString(), encoding)
    }

    /**
     * 使用编码将CharSequence写入到InputStream。
     *
     *
     * @param input 要写入到stream的CharSequence
     * @param encoding 编码，null表示系统默认
     * @return 一个input stream
     * @throws IOException  encoding非法
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     * @since 2.0
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toInputStream(input: CharSequence, encoding: String?): InputStream {
        return toInputStream(input, Charsets.toCharset(encoding))
    }

    /**
     * 使用编码将String写入到InputStream。
     *
     * @param input 要写入到stream的String
     * @param encoding  编码，null表示系统默认
     * @return  一个input stream
     * @since 2.3
     */
    @JvmStatic
    fun toInputStream(input: String, encoding: Charset?): InputStream {
        return ByteArrayInputStream(input.toByteArray(Charsets.toCharset(encoding)))
    }

    /**
     * 使用编码将String写入到InputStream。
     *
     * @param input 要写入到stream的String
     * @param encoding 编码，null表示系统默认
     * @return 一个input stream
     * @throws IOException        encoding非法
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of [                                                      .UnsupportedEncodingException][java.io] in version 2.2 if the
     * encoding is not supported.
     * @since 1.1
     */
    @JvmStatic
    @Throws(IOException::class)
    fun toInputStream(input: String, encoding: String?): InputStream {
        val bytes = input.toByteArray(Charsets.toCharset(encoding))
        return ByteArrayInputStream(bytes)
    }

}