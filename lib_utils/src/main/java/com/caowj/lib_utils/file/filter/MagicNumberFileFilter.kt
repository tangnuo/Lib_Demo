package com.caowj.lib_utils.file.filter

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.io.Serializable
import java.nio.charset.Charset
import java.util.*

/**
 * 匹配包含"magic number"的文件过滤器。一个"magic number"是特殊文件格式：唯一的字节序列。
 * 例如：所有的Java类文件都是`0xCAFEBABE`开头的文件。
 *
 * <pre>
 * val dir = File(".")
 * val javaClassFileFilter = MagicNumberFileFilter(byteArrayOf(0xCA, 0xFE, 0xBA, 0xBE);
 * val javaClassFiles = dir.list(javaClassFileFilter);
 * for (String javaClassFile : javaClassFiles) {
 *    println(javaClassFile);
 * }
 * </pre>
 *
 * 有时，像TAR格式的文件，"magic number"会是一个特定大小的偏移量。这种情况的TAR格式文件，偏移量是257字节。
 *
 * <pre>
 * val dir = File(".")
 * val tarFileFilter = MagicNumberFileFilter("ustar", 257);
 * val tarFiles = dir.list(tarFileFilter);
 * for (String tarFile : tarFiles) {
 *   println(tarFile);
 * }
</pre> *
 *
 * @see FileFilterUtil.magicNumberFileFilter
 * @see FileFilterUtil.magicNumberFileFilter
 * @see FileFilterUtil.magicNumberFileFilter
 * @see FileFilterUtil.magicNumberFileFilter
 * @since 2.0
 */
internal class MagicNumberFileFilter : AbstractFileFilter, Serializable {

    /**
     * The magic number to compare against the file's bytes at the provided offset.
     */
    private val magicNumbers: ByteArray

    /**
     * The offset (in bytes) within the files that the magic number's bytes should appear.
     */
    private val byteOffset: Long

    /**
     *
     *
     * Constructs a new MagicNumberFileFilter and associates it with the magic number to test for in
     * files and the byte offset location in the file to to look for that magic number.
     *
     *
     * <pre>
     * MagicNumberFileFilter tarFileFilter =
     * MagicNumberFileFilter("ustar", 257);
    </pre> *
     *
     * @param magicNumber the magic number to look for in the file. The string is converted to bytes
     * using the platform default charset.
     * @param offset the byte offset in the file to start comparing bytes.
     * @throws IllegalArgumentException if `magicNumber` is `null` or the empty
     * String, or `offset` is a negative number.
     */
    @JvmOverloads
    constructor(magicNumber: String?, offset: Long = 0) {
        requireNotNull(magicNumber) { "The magic number cannot be null" }
        require(!magicNumber.isEmpty()) { "The magic number must contain at least one byte" }
        require(offset >= 0) { "The offset cannot be negative" }

        this.magicNumbers = magicNumber
                .toByteArray(Charset.defaultCharset()) // explicitly uses the platform default
        // charset
        this.byteOffset = offset
    }

    /**
     *
     *
     * Constructs a new MagicNumberFileFilter and associates it with the magic number to test for in
     * files and the byte offset location in the file to to look for that magic number.
     *
     *
     * <pre>
     * MagicNumberFileFilter tarFileFilter =
     * MagicNumberFileFilter(new byte[] {0x75, 0x73, 0x74, 0x61, 0x72}, 257);
    </pre> *
     *
     * <pre>
     * MagicNumberFileFilter javaClassFileFilter =
     * MagicNumberFileFilter(new byte[] {0xCA, 0xFE, 0xBA, 0xBE}, 0);
    </pre> *
     *
     * @param magicNumber the magic number to look for in the file.
     * @param offset the byte offset in the file to start comparing bytes.
     * @throws IllegalArgumentException if `magicNumber` is `null`, or contains no
     * bytes, or `offset` is a negative number.
     */
    @JvmOverloads
    constructor(magicNumber: ByteArray?, offset: Long = 0) {
        requireNotNull(magicNumber) { "The magic number cannot be null" }
        require(magicNumber.size != 0) { "The magic number must contain at least one byte" }
        require(offset >= 0) { "The offset cannot be negative" }

        this.magicNumbers = ByteArray(magicNumber.size)
        System.arraycopy(magicNumber, 0, this.magicNumbers, 0, magicNumber.size)
        this.byteOffset = offset
    }

    /**
     *
     *
     * Accepts the provided file if the file contains the file filter's magic number at the
     * specified offset.
     *
     *
     *
     *
     * If any [IOException]s occur while reading the file, the file will be rejected.
     *
     *
     * @param file the file to accept or reject.
     * @return `true` if the file contains the filter's magic number at the specified offset,
     * `false` otherwise.
     */
    override fun accept(file: File): Boolean {
        if (file != null && file.isFile && file.canRead()) {
            try {
                RandomAccessFile(file, "r").use { randomAccessFile ->
                    val fileBytes = ByteArray(this.magicNumbers.size)
                    randomAccessFile.seek(byteOffset)
                    val read = randomAccessFile.read(fileBytes)
                    return if (read != magicNumbers.size) {
                        false
                    } else Arrays.equals(this.magicNumbers, fileBytes)
                }
            } catch (ioe: IOException) {
                // Do nothing, fall through and do not accept file
            }

        }

        return false
    }

    /**
     * Returns a String representation of the file filter, which includes the magic number bytes and
     * byte offset.
     *
     * @return a String representation of the file filter.
     */
    override fun toString(): String {
        val builder = StringBuilder(super.toString())
        builder.append("(")
        builder.append(String(magicNumbers,
                Charset.defaultCharset()))// TODO perhaps use hex if value is not
        // printable
        builder.append(",")
        builder.append(this.byteOffset)
        builder.append(")")
        return builder.toString()
    }

    companion object {

        /**
         * The serialization version unique identifier.
         */
        private const val serialVersionUID = -547733176983104172L
    }
}
/**
 *
 *
 * Constructs a new MagicNumberFileFilter and associates it with the magic number to test for in
 * files. This constructor assumes a starting offset of `0`.
 *
 *
 *
 *
 * It is important to note that *the array is not cloned* and that any changes to the
 * magic number array after construction will affect the behavior of this file filter.
 *
 *
 * <pre>
 * MagicNumberFileFilter javaClassFileFilter =
 * MagicNumberFileFilter(new byte[] {(byte) 0xCA, (byte) 0xFE,
 * (byte) 0xBA, (byte) 0xBE});
</pre> *
 *
 * @param magicNumber the magic number to look for in the file.
 * @throws IllegalArgumentException if `magicNumber` is `null`, or contains no
 * bytes.
 */
/**
 *
 *
 * Constructs a new MagicNumberFileFilter and associates it with the magic number to test for in
 * files. This constructor assumes a starting offset of `0`.
 *
 *
 * Example usage:
 * <pre>
 * `MagicNumberFileFilter xmlFileFilter =
 * MagicNumberFileFilter("<?xml");
` *
</pre> *
 *
 * @param magicNumber the magic number to look for in the file. The string is converted to bytes
 * using the platform default charset.
 * @throws IllegalArgumentException if `magicNumber` is `null` or the empty
 * String.
 */
