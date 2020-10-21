package com.caowj.lib_utils

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by wangqian on 2019/12/23.
 * 线程池
 */
object ThreadPool {
    //CPU核心数
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    //可同时下载的任务数（核心线程数）
    private val CORE_POOL_SIZE = CPU_COUNT
    //缓存队列的大小（最大线程数）
    private val MAX_POOL_SIZE = 2 * CPU_COUNT + 1
    //非核心线程闲置的超时时间（秒），如果超时则会被回收
    private val KEEP_ALIVE_TIME = 10L

    private val sThreadFactory = KdThreadFactory("worker")

    private var THREAD_POOL_EXECUTOR: ThreadPoolExecutor = getThreadPoolExecutor()

    private fun getThreadPoolExecutor(): ThreadPoolExecutor {
        return ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                LinkedBlockingDeque(),
                sThreadFactory)
    }

    @JvmStatic
    fun execute(command: Runnable) {
        THREAD_POOL_EXECUTOR.execute(command)
    }

    /**
     * 关闭线程池，这里要说明的是：调用关闭线程池方法后，会移出正在等待的任务，线程池立即退出
     */
    @JvmStatic
    fun shutdownNow() {
        if (!THREAD_POOL_EXECUTOR.isShutdown())
            THREAD_POOL_EXECUTOR.shutdownNow()


    }

    /**
     * 关闭线程池，这里要说明的是：调用关闭线程池方法后，线程池会执行完队列中的所有任务才退出
     */
    @JvmStatic
    fun shutdown() {
        THREAD_POOL_EXECUTOR.shutdown()
    }

    /**
     * 提交任务到线程池，可以接收线程返回值
     *
     * @param task
     * @return
     */
    @JvmStatic
    fun submit(task: Runnable): Future<*> {

        return THREAD_POOL_EXECUTOR.submit(task)
    }

    /**
     * 提交任务到线程池，可以接收线程返回值
     *
     * @param task
     * @return
     */
    @JvmStatic
    fun submit(task: Callable<Any>): Future<Any> {
        return THREAD_POOL_EXECUTOR.submit(task)
    }


    internal class KdThreadFactory(private val threadName: String) : ThreadFactory {
        private val group: ThreadGroup
        private val threadNumber = AtomicInteger(1)
        private val namePrefix: String


        init {
            val s = System.getSecurityManager()
            group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup
            namePrefix = "KD-pool-thread-"
        }

        override fun newThread(r: Runnable): Thread {
            val t = Thread(group, r,
                    namePrefix + threadName + threadNumber.getAndIncrement(),
                    0)
            if (t.isDaemon)
                t.isDaemon = false
            if (t.priority != Thread.NORM_PRIORITY)
                t.priority = Thread.NORM_PRIORITY
            return t
        }


    }

}