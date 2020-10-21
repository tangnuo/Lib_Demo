package com.caowj.lib_utils.digest

/**
 * 摘要算法
 */
enum class DigestAlgorithm(private val algorithm: String) {
    MD5("MD5"), SHA1("SHA-1"), SHA224("SHA-224"),
    SHA256("SHA-256"), SHA384("SHA-384"), SHA512("SHA-512");

    /**
     * 获取算法名
     *
     * @return 真实算法名
     */
    fun getAlgorithm(): String = algorithm
}