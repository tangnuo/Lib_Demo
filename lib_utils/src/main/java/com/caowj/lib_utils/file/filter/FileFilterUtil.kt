package com.caowj.lib_utils.file.filter

import com.caowj.lib_utils.file.IOCase
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import java.util.*

/**
 * 工具类，方便用于过滤文件。提供了所有可访问的过滤器类型。因此不必导入所有的Filter类型。
 *
 * @version $Id$
 * @since 1.0
 */
object FileFilterUtil {

    //-----------------------------------------------------------------------
    /* Constructed on demand and then cached */
    @JvmStatic
    private val cvsFilter = notFileFilter(
            and(directoryFileFilter(), nameFileFilter("CVS")))

    //-----------------------------------------------------------------------
    /* Constructed on demand and then cached */
    @JvmStatic
    private val svnFilter = notFileFilter(
            and(directoryFileFilter(), nameFileFilter(".svn")))

    /**
     * 在多个[File]对象上应用[IOFileFilter]过滤器。返回符合过滤器的结果数组。
     *
     * 此方法无法保证最终返回的数组是线程安全。
     *
     * <pre>
     * Set<File> allFiles = ...
     * Set<File> javaFiles = FileFilterUtil.filterSet(allFiles, FileFilterUtil.suffixFileFilter(".java"));
     * </pre>
     *
     * @param filter 文件过滤器，不为null
     * @param files 一组文件对象，文件对象不为null
     * @return 符合文件过滤器条件的一组文件
     * @throws IllegalArgumentException  若 `files` 包含null
     * @since 2.0
     */
    @JvmStatic
    fun filter(filter: IOFileFilter, vararg files: File?): Array<File> {
        val acceptedFiles = ArrayList<File>()
        for (file in files) {
            requireNotNull(file) { "file array contains null" }
            if (filter.accept(file)) {
                acceptedFiles.add(file)
            }
        }
        return acceptedFiles.toTypedArray()
    }

    /**
     * 在多个[File]对象上应用[IOFileFilter]过滤器。返回符合过滤器的结果数组。
     *
     * 此方法无法保证最终返回的数组是线程安全。
     *
     * <pre>
     * Set&lt;File&gt; allFiles = ...
     * Set&lt;File&gt; javaFiles = FileFilterUtil.filterSet(allFiles,
     * FileFilterUtil.suffixFileFilter(".java"));
    </pre> *
     *
     * @param filter the filter to apply to the set of files.
     * @param files the array of files to apply the filter to.
     * @return a subset of `files` that is accepted by the file filter.
     * @throws IllegalArgumentException if the filter is `null` or `files` contains
     * a `null` value.
     * @since 2.0
     */
    @JvmStatic
    fun filter(filter: IOFileFilter, files: Iterable<File>): Array<File> {
        val acceptedFiles = filterList(filter, files)
        return acceptedFiles.toTypedArray()
    }

    /**
     * 在多个[File]对象上应用[IOFileFilter]过滤器。返回符合过滤器的结果数组。
     *
     * 此方法无法保证最终返回的数组是线程安全。
     *
     * <pre>
     * List&lt;File&gt; filesAndDirectories = ...
     * List&lt;File&gt; directories = FileFilterUtil.filterList(filesAndDirectories,
     * FileFilterUtil.directoryFileFilter());
    </pre> *
     *
     * @param filter the filter to apply to each files in the list.
     * @param files the collection of files to apply the filter to.
     * @return a subset of `files` that is accepted by the file filter.
     * @throws IllegalArgumentException if the filter is `null` or `files` contains
     * a `null` value.
     * @since 2.0
     */
    @JvmStatic
    fun filterList(filter: IOFileFilter, files: Iterable<File>): List<File> {
        return filter(filter, files, ArrayList())
    }

    /**
     * 在多个[File]对象上应用[IOFileFilter]过滤器。返回符合过滤器的结果数组。
     *
     * 此方法无法保证最终返回的数组是线程安全。
     *
     * <pre>
     * List&lt;File&gt; filesAndDirectories = ...
     * List&lt;File&gt; directories = FileFilterUtil.filterList(filesAndDirectories,
     * FileFilterUtil.directoryFileFilter());
    </pre> *
     *
     * @param filter the filter to apply to each files in the list.
     * @param files the collection of files to apply the filter to.
     * @return a subset of `files` that is accepted by the file filter.
     * @throws IllegalArgumentException if the filter is `null` or `files` contains
     * a `null` value.
     * @since 2.0
     */
    @JvmStatic
    fun filterList(filter: IOFileFilter, vararg files: File): List<File> {
        val acceptedFiles = filter(filter, *files)
        return Arrays.asList(*acceptedFiles)
    }

    /**
     * 在多个[File]对象上应用[IOFileFilter]过滤器。返回符合过滤器的结果数组。
     *
     * 此方法无法保证最终返回的数组是线程安全。
     * <pre>
     * Set&lt;File&gt; allFiles = ...
     * Set&lt;File&gt; javaFiles = FileFilterUtil.filterSet(allFiles,
     * FileFilterUtil.suffixFileFilter(".java"));
    </pre> *
     *
     * @param filter the filter to apply to the set of files.
     * @param files the collection of files to apply the filter to.
     * @return a subset of `files` that is accepted by the file filter.
     * @throws IllegalArgumentException if the filter is `null` or `files` contains
     * a `null` value.
     * @since 2.0
     */
    @JvmStatic
    fun filterSet(filter: IOFileFilter, vararg files: File): Set<File> {
        val acceptedFiles = filter(filter, *files)
        return HashSet(Arrays.asList(*acceptedFiles))
    }

    /**
     * 在多个[File]对象上应用[IOFileFilter]过滤器。返回符合过滤器的结果数组。
     *
     * 此方法无法保证最终返回的数组是线程安全。
     *
     * <pre>
     * Set&lt;File&gt; allFiles = ...
     * Set&lt;File&gt; javaFiles = FileFilterUtil.filterSet(allFiles,
     * FileFilterUtil.suffixFileFilter(".java"));
    </pre> *
     *
     * @param filter the filter to apply to the set of files.
     * @param files the collection of files to apply the filter to.
     * @return a subset of `files` that is accepted by the file filter.
     * @throws IllegalArgumentException if the filter is `null` or `files` contains
     * a `null` value.
     * @since 2.0
     */
    @JvmStatic
    fun filterSet(filter: IOFileFilter, files: Iterable<File>): Set<File> {
        return filter(filter, files, HashSet())
    }

    /**
     * 在多个[File]对象上应用[IOFileFilter]过滤器。返回符合过滤器的结果数组。
     *
     * 此方法无法保证最终返回的数组是线程安全。
     *
     * <pre>
     * List&lt;File&gt; files = ...
     * List&lt;File&gt; directories = FileFilterUtil.filterList(files,
     * FileFilterUtil.sizeFileFilter(FileUtils.FIFTY_MB),
     * new ArrayList&lt;File&gt;());
    </pre> *
     *
     * @param filter the filter to apply to the collection of files.
     * @param files the collection of files to apply the filter to.
     * @param acceptedFiles the list of files to add accepted files to.
     * @param <T> the type of the file collection.
     * @return a subset of `files` that is accepted by the file filter.
     * @throws IllegalArgumentException if the filter is `null` or `files` contains
     * a `null` value.
    </T> */
    @JvmStatic
    private fun <T : MutableCollection<File>> filter(filter: IOFileFilter,
                                                     files: Iterable<File?>?, acceptedFiles: T): T {
        if (files != null) {
            for (file in files) {
                requireNotNull(file) { "file collection contains null" }
                if (filter.accept(file)) {
                    acceptedFiles.add(file)
                }
            }
        }
        return acceptedFiles
    }

    /**
     * 返回前缀过滤器——过滤以给定text开始的文件名对象。
     *
     * @param prefix 文件名前缀
     * @return 前缀过滤器
     * @see PrefixFileFilter
     */
    @JvmStatic
    fun prefixFileFilter(prefix: String): IOFileFilter {
        return PrefixFileFilter(prefix)
    }

    /**
     * 返回前缀过滤器——过滤以给定text开始的文件名对象.
     *
     * @param prefix 文件名前缀
     * @param caseSensitivity 大小写，null表示大小写敏感
     * @return 前缀过滤器
     * @see PrefixFileFilter
     *
     * @since 2.0
     */
    @JvmStatic
    fun prefixFileFilter(prefix: String, caseSensitivity: IOCase): IOFileFilter {
        return PrefixFileFilter(prefix, caseSensitivity)
    }

    /**
     * 返回过滤器对象——过滤以给定text结束的文件名对象。
     *
     * @param suffix 文件名后缀
     * @return 后缀过滤器
     * @see SuffixFileFilter
     */
    @JvmStatic
    fun suffixFileFilter(suffix: String): IOFileFilter {
        return SuffixFileFilter(suffix)
    }

    /**
     * 返回过滤器对象——过滤以给定text结束的文件名对象。
     *
     * @param suffix 文件名后缀
     * @param caseSensitivity  大小写，null表示大小写敏感
     * @return 后缀过滤器
     * @see SuffixFileFilter
     *
     * @since 2.0
     */
    @JvmStatic
    fun suffixFileFilter(suffix: String, caseSensitivity: IOCase): IOFileFilter {
        return SuffixFileFilter(suffix, caseSensitivity)
    }

    /**
     * 名称过滤器——过滤匹配给定text的文件名(filename)对象。
     *
     * @param name 文件对象名
     * @return 名称过滤器
     * @see NameFileFilter
     */
    @JvmStatic
    fun nameFileFilter(name: String): IOFileFilter {
        return NameFileFilter(name)
    }

    /**
     * 名称过滤器——过滤匹配给定text的文件名(filename)对象。
     *
     * @param name 文件对象名
     * @param caseSensitivity  大小写，null表示大小写敏感
     * @return 名称过滤器
     * @see NameFileFilter
     *
     * @since 2.0
     */
    @JvmStatic
    fun nameFileFilter(name: String, caseSensitivity: IOCase): IOFileFilter {
        return NameFileFilter(name, caseSensitivity)
    }

    //-----------------------------------------------------------------------

    /**
     * 目录过滤器——只接受目录类型File对象。
     *
     * @return 目录过滤器
     * @see DirectoryFileFilter.getDirectory
     */
    @JvmStatic
    fun directoryFileFilter(): IOFileFilter {
        return DirectoryFileFilter.DIRECTORY
    }

    /**
     * 文件过滤器——只接受真实文件。
     *
     * @return 文件过滤器
     * @see FileFileFilter.getFile
     */
    @JvmStatic
    fun fileFileFilter(): IOFileFilter {
        return FileFileFilter.FILE
    }

    /**
     * 对传入的一组过滤器进行条件运算——AND运算。
     *
     * @param filters 进行AND运算的IOFileFilters
     * @return  运算后的Filter
     * @see AndFileFilter
     *
     * @since 2.0
     */
    @JvmStatic
    fun and(vararg filters: IOFileFilter): IOFileFilter {
        return AndFileFilter(toList(*filters))
    }

    /**
     * 对传入的一组过滤器进行条件运算——OR 运算。
     *
     * @param filters 进行OR运算的IOFileFilters
     * @return 运算后的Filter
     * @see OrFileFilter
     *
     * @since 2.0
     */
    @JvmStatic
    fun or(vararg filters: IOFileFilter): IOFileFilter {
        return OrFileFilter(toList(*filters))
    }

    //-----------------------------------------------------------------------

    /**
     * 转换一组过滤器。
     *
     * @param filters The file filters
     * @return The list of file filters
     * @throws IllegalArgumentException if the filters are null or contain a null value.
     * @since 2.0
     */
    @JvmStatic
    fun toList(vararg filters: IOFileFilter): List<IOFileFilter> {
        val list = ArrayList<IOFileFilter>(filters.size)
        for (i in filters.indices) {
            list.add(filters[i])
        }
        return list
    }

    /**
     * 条件过滤器——过滤出传入过滤器之外的文件对象。
     *
     * @param filter 排除在外的filter
     * @return 排除在外的filter
     * @see NotFileFilter
     */
    @JvmStatic
    fun notFileFilter(filter: IOFileFilter): IOFileFilter {
        return NotFileFilter(filter)
    }

    //-----------------------------------------------------------------------

    /**
     * 将FileFilter作为IOFileFilter返回。
     *
     * @param filter 封装的Filter
     * @return 实现后的IOFileFilter
     * @see DelegateFileFilter
     */
    @JvmStatic
    fun asFileFilter(filter: FileFilter): IOFileFilter {
        return DelegateFileFilter(filter)
    }

    /**
     * 将`FilenameFilter`封装为 `IOFileFilter`。
     *
     * @param filter 封装的Filter
     * @return 实现后的IOFileFilter
     * @see DelegateFileFilter
     */
    @JvmStatic
    fun asFileFilter(filter: FilenameFilter): IOFileFilter {
        return DelegateFileFilter(filter)
    }

    /**
     * 时间过滤器——过滤最后修改时间早于给定时间(long时间表示)的文件对象。
     *
     * @param cutoff 截止时间
     * @return 一个适当配置的文件过滤器
     * @since 1.2
     */
    @JvmStatic
    fun ageFileFilter(cutoff: Long): IOFileFilter {
        return AgeFileFilter(cutoff)
    }

    /**
     * 时间过滤器——过滤最后修改时间早于给定时间(long时间表示)的文件对象。
     *
     * @param cutoff 截止时间
     * @param acceptOlder 若true，早于cutoff时间的文件返回， 若false, 返回更新的文件
     * @return 一个适当配置的文件过滤器
     * @see AgeFileFilter
     *
     * @since 1.2
     */
    @JvmStatic
    fun ageFileFilter(cutoff: Long, acceptOlder: Boolean): IOFileFilter {
        return AgeFileFilter(cutoff, acceptOlder)
    }

    /**
     * 时间过滤器——过滤最后修改时间早于给定时间(Date时间表示)的文件对象。
     *
     * @param cutoffDate 截止时间
     * @return 一个适当配置的文件过滤器
     * @see AgeFileFilter
     *
     * @since 1.2
     */
    @JvmStatic
    fun ageFileFilter(cutoffDate: Date): IOFileFilter {
        return AgeFileFilter(cutoffDate)
    }

    /**
     * 时间过滤器——过滤最后修改时间早于给定时间(Date时间表示)的文件对象。
     *
     * @param cutoffDate 截止时间
     * @param acceptOlder 若true，早于cutoff时间的文件返回， 若false, 返回更新的文件
     * @return 一个适当配置的文件过滤器
     * @see AgeFileFilter
     *
     * @since 1.2
     */
    @JvmStatic
    fun ageFileFilter(cutoffDate: Date, acceptOlder: Boolean): IOFileFilter {
        return AgeFileFilter(cutoffDate, acceptOlder)
    }

    //-----------------------------------------------------------------------

    /**
     * 时间过滤器——过滤修改时间早于或等于`cutoffReference`文件的修改时间。
     *
     * @param cutoffReference 用于获取修改时间的File
     * @return 一个适当配置的文件过滤器
     * @see AgeFileFilter
     *
     * @since 1.2
     */
    @JvmStatic
    fun ageFileFilter(cutoffReference: File): IOFileFilter {
        return AgeFileFilter(cutoffReference)
    }

    /**
     * 时间过滤器——过滤修改时间早于或等于`cutoffReference`文件的修改时间。
     *
     * @param cutoffReference 用于获取修改时间的File
     * @param acceptOlder 若true，早于cutoff时间的文件返回， 若false, 返回更新的文件
     * @return 一个适当配置的文件过滤器
     * @see AgeFileFilter
     *
     * @since 1.2
     */
    @JvmStatic
    fun ageFileFilter(cutoffReference: File,
                      acceptOlder: Boolean): IOFileFilter {
        return AgeFileFilter(cutoffReference, acceptOlder)
    }

    /**
     * 文件大小过滤器——过滤大于给定大小的文件。
     *
     * @param threshold 给定文件大小标准
     * @return 一个适当配置的文件过滤器
     * @since 1.2
     */
    @JvmStatic
    fun sizeFileFilter(threshold: Long): IOFileFilter {
        return SizeFileFilter(threshold)
    }

    /**
     * 文件大小过滤器——过滤大于给定大小的文件。
     *
     * @param threshold 给定文件大小标准
     * @param acceptLarger  若true， 返回更大文件，若false，返回更小文件
     * @return 一个适当配置的文件过滤器
     * @see SizeFileFilter
     *
     * @since 1.2
     */
    @JvmStatic
    fun sizeFileFilter(threshold: Long, acceptLarger: Boolean): IOFileFilter {
        return SizeFileFilter(threshold, acceptLarger)
    }

    /**
     * 大小文件过滤器——过滤大于等于minimum且小于等于maximum文件。
     *
     * @param minSizeInclusive 最小文件大小(包含)
     * @param maxSizeInclusive 最大文件大小(包含)
     * @return 一个适当配置的文件过滤器
     * @see SizeFileFilter
     *
     * @since 1.3
     */
    @JvmStatic
    fun sizeRangeFileFilter(minSizeInclusive: Long,
                            maxSizeInclusive: Long): IOFileFilter {
        val minimumFilter = SizeFileFilter(minSizeInclusive, true)
        val maximumFilter = SizeFileFilter(maxSizeInclusive + 1L, false)
        return AndFileFilter(minimumFilter, maximumFilter)
    }

    /**
     * 特征数字过滤器——文件以特定数字开头的文件。
     *
     * @param magicNumber  与文件开头特征码匹配的文件
     * @return 文件以特定数字开头
     * @since 2.0
     */
    @JvmStatic
    fun magicNumberFileFilter(magicNumber: String): IOFileFilter {
        return MagicNumberFileFilter(magicNumber)
    }

    /**
     * 特征值过滤器——以特征值开头的文件。
     *
     * @param magicNumber 文件内容特征码
     * @param offset 偏移量，从偏移位置匹配特征值
     * @return 满足从offset匹配特征码的文件过虑器
     * @see MagicNumberFileFilter
     *
     * @since 2.0
     */
    @JvmStatic
    fun magicNumberFileFilter(magicNumber: String, offset: Long): IOFileFilter {
        return MagicNumberFileFilter(magicNumber, offset)
    }

    /**
     * 特征值过滤器——以特征值开头的文件。
     *
     * @param magicNumber 文件内容特征码
     * @return 匹配特征码的文件过虑器
     * @see MagicNumberFileFilter
     *
     * @since 2.0
     */
    @JvmStatic
    fun magicNumberFileFilter(magicNumber: ByteArray): IOFileFilter {
        return MagicNumberFileFilter(magicNumber)
    }

    /**
     * 特征值过滤器——以特征值开头的文件。
     *
     * @param magicNumber 文件内容特征码
     * @param offset 偏移量，从偏移位置匹配特征值
     * @return 满足从offset匹配特征码的文件过虑器
     * @see MagicNumberFileFilter
     *
     * @since 2.0
     */
    @JvmStatic
    fun magicNumberFileFilter(magicNumber: ByteArray, offset: Long): IOFileFilter {
        return MagicNumberFileFilter(magicNumber, offset)
    }

    /**
     * 封装过过滤器，至过滤使用时过滤掉CVS目录。传入null表示除去CVS目录外的所有文件对象。
     *
     * @param filter  封装的filter，null表示不注册任何filter
     * @return
     * @since 1.1 (method existed but had bug in 1.0)
     */
    @JvmStatic
    fun makeCVSAware(filter: IOFileFilter?): IOFileFilter {
        return if (filter == null) {
            cvsFilter
        } else {
            and(filter, cvsFilter)
        }
    }

    //-----------------------------------------------------------------------

    /**
     * 装饰一个过滤器——将传入的filter应用到目录。
     *
     * @param filter 要封装的过滤器，null表示不限制过滤器
     * @return 封装后的新filter
     * @see DirectoryFileFilter.getDirectory
     * @since 1.3
     */
    @JvmStatic
    fun makeDirectoryOnly(filter: IOFileFilter?): IOFileFilter {
        return if (filter == null) {
            DirectoryFileFilter.DIRECTORY
        } else AndFileFilter(DirectoryFileFilter.DIRECTORY, filter)
    }

    /**
     * 装饰一个过滤器——将传入的filter应用真是文件。
     *
     * @param filter 要封装的过滤器，null表示不限制过滤器
     * @return 封装后的新filter
     * @see FileFileFilter.getFile
     * @since 1.3
     */
    @JvmStatic
    fun makeFileOnly(filter: IOFileFilter?): IOFileFilter {
        return if (filter == null) {
            FileFileFilter.FILE
        } else AndFileFilter(FileFileFilter.FILE, filter)
    }

    // ==================================================================
    /**
     * 返回接受任意类型的过滤器。
     *
     * @return true过滤器
     * @see TrueFileFilter.TRUE
     */
    @JvmStatic
    fun trueFileFilter(): IOFileFilter {
        return TrueFileFilter.TRUE
    }

    /**
     * 返回不首页任何类型的过滤器。
     *
     * @return false过滤器
     * @see FalseFileFilter.FALSE
     */
    @JvmStatic
    fun falseFileFilter(): IOFileFilter {
        return FalseFileFilter.FALSE
    }
    // ==================================================================

}
