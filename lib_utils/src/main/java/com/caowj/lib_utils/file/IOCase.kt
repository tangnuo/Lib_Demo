package com.caowj.lib_utils.file

import java.io.Serializable

/**
 * 大小写枚举类。
 *
 * 不同文件系统有不同的大小写规则。Windows大小写不敏感，Unix大小写敏感。
 *
 * 此类可以规避差异性，控制如何执行文件比较。
 *
 * @since 1.3
 */
enum class IOCase
/**
 * @param type 类型
 * @param sensitive 是否大小写敏感
 */
(
        /*
         * 获取类型值。
         */
        val type: String,
        /**
         * 大小写标记
         */
        @field:Transient
        private val isCaseSensitive: Boolean) : Serializable {

    /**
     * 大小写敏感常量，忽略操作系统。
     */
    SENSITIVE("Sensitive", true),

    /**
     * 大小写不敏感常量，忽略操作系统。
     */
    INSENSITIVE("Insensitive", false),

    /**
     * 这个常量表示大小写敏感有OS决定。Windows上比较文件名大小写不敏感，Unix上大小写敏感。
     *
     * **Note:** 此类只考虑Windows和Unix。其他OS(例如：OSX and OpenVMS)会被认为是大小写敏感，
     * 若使用Unix文件分隔符及大小写敏感若使用Windows文件分割符。(see [ ][java.io.File.separatorChar])
     */
    SYSTEM("System", !FilenameUtil.isSystemWindows());

    /**
     * @return 解析后结果
     */
    private fun readResolve(): Any {
        return forName(type)
    }

    //-----------------------------------------------------------------------

    /**
     * 使用大小写敏感规则比较两个字符串.
     *
     * 此方法模仿 [String.compareTo] 但考虑大小写。
     *
     * @param str1 the first string to compare, not null
     * @param str2 the second string to compare, not null
     * @return true if equal using the case rules
     * @throws NullPointerException if either string is null
     */
    fun checkCompareTo(str1: String?, str2: String?): Int {
        if (str1 == null || str2 == null) {
            throw NullPointerException("The strings must not be null")
        }
        return if (isCaseSensitive) str1.compareTo(str2) else str1.compareTo(str2, ignoreCase = true)
    }

    /**
     * 使用大小写敏感规则比较两个字符串.
     *
     *
     * 此方法模仿 [String.equals] 但考虑大小写。
     *
     * @param str1 the first string to compare, not null
     * @param str2 the second string to compare, not null
     * @return true if equal using the case rules
     * @throws NullPointerException if either string is null
     */
    fun checkEquals(str1: String?, str2: String?): Boolean {
        if (str1 == null || str2 == null) {
            throw NullPointerException("The strings must not be null")
        }
        return if (isCaseSensitive) str1 == str2 else str1.equals(str2, ignoreCase = true)
    }

    /**
     *
     * 检查一字符串是否以另一字符串开始(大小写敏感)。
     *
     * 此方法模仿 [String.startsWith] 但考虑大小写。
     *
     * @param str  检查的字符串，不能null
     * @param start 用以匹配开始部分的字符串，不能null
     * @return true 若部分相等
     */
    fun checkStartsWith(str: String, start: String): Boolean {
        return str.regionMatches(0, start, 0, start.length, ignoreCase = !isCaseSensitive)
    }

    /**
     * 检查一字符串是否以另一字符串结束(大小写敏感)。
     *
     *
     * 此方法模仿 [String.endsWith] 但考虑大小写。
     *
     * @param str  检查的字符串，不能null
     * @param end 用以匹配开始部分的字符串，不能null
     * @return true 若str末尾与end匹配
     */
    fun checkEndsWith(str: String, end: String): Boolean {
        val endLen = end.length
        return str.regionMatches(str.length - endLen, end, 0, endLen, ignoreCase = !isCaseSensitive)
    }

    /**
     *  检查一字符串从给定index开始是与另一字符匹配(包含大小写)，返回匹配的首字母的index。
     *
     * 此方法模仿 [String.indexOf] 但考虑大小写。
     *
     * @param str the string to check, not null
     * @param strStartIndex the index to start at in str
     * @param search the start to search for, not null
     * @return the first index of the search String, -1 if no match or `null` string input
     * @throws NullPointerException if either string is null
     * @since 2.0
     */
    fun checkIndexOf(str: String, strStartIndex: Int, search: String): Int {
        val endIndex = str.length - search.length
        if (endIndex >= strStartIndex) {
            for (i in strStartIndex..endIndex) {
                if (checkRegionMatches(str, i, search)) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * 检查一个字符串从给定index开始匹配(包含大小写)，返回完全匹配是首字母的index。
     *
     * 此方法模仿 [String.regionMatches] 但考虑大小写。
     *
     * @param str 检索的字符串，不能null
     * @param strStartIndex 开始搜索的索引
     * @param search  要匹配的字符串，不能null
     * @return true 匹配
     */
    fun checkRegionMatches(str: String, strStartIndex: Int,
                           search: String): Boolean {
        return str.regionMatches(strStartIndex, search, 0, search.length, ignoreCase = !isCaseSensitive)
    }


    /**
     * @return a string describing the sensitivity
     */
    override fun toString(): String {
        return type
    }

    companion object {

        /**
         * Serialization version.
         */
        private const val serialVersionUID = -6343169151696340687L


        /**
         * Factory method to create an IOCase from a type.
         *
         * @param name the type to find
         * @return the IOCase object
         * @throws IllegalArgumentException if the type is invalid
         */
        fun forName(name: String): IOCase {
            for (ioCase in values()) {
                if (ioCase.type == name) {
                    return ioCase
                }
            }
            throw IllegalArgumentException("Invalid IOCase type: $name")
        }
    }

}
