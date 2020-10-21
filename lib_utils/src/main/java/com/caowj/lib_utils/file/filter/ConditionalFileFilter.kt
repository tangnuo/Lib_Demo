package com.caowj.lib_utils.file.filter

/**
 * Defines operations for conditional file filters.
 * 条件文件过滤器(AndFileFilter, OrFileFilter)操作
 *
 * @version $Id$
 * @since 1.1
 */
internal interface ConditionalFileFilter {

    /**
     * 返回条件文件过滤器的过滤器列表。
     *
     * @return 文件过滤器列表
     * @since 1.1
     */
    var fileFilters: MutableList<IOFileFilter>

    /**
     * 在列表末尾添加给定的文件过滤器。
     *
     * @param ioFileFilter 添加的过滤器
     * @since 1.1
     */
    fun addFileFilter(ioFileFilter: IOFileFilter)

    /**
     * 移除给定过滤器
     *
     * @param ioFileFilter 要移除的过滤器
     * @return `true` 过滤器在列表中可以找到，反之 `false`
     * @since 1.1
     */
    fun removeFileFilter(ioFileFilter: IOFileFilter): Boolean

}
