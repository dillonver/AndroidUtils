package xyz.dcln.androidutils.utils

import android.content.Context
import android.util.Log
import java.io.File

object LogUtils {

    // 默认的日志标签
    private const val DEFAULT_TAG = "LogUtils"
    private const val MAX_LOG_LENGTH = 4000
    private const val DEFAULT_CACHE_SIZE_LIMIT = 5 * 1024 * 1024L // 默认为5MB
    private const val LOG_CACHE_FILENAME = "logcache.txt"

    // 日志是否启用
    var isLogEnabled = true

    // 设置日志缓存的大小限制
    var logCacheSizeLimit: Long = DEFAULT_CACHE_SIZE_LIMIT

    // INFO 级别的日志
    fun i(
        vararg messages: Any?,
        tag: String = DEFAULT_TAG,
        writeToCache: Boolean = false,
    ) {
        log(Level.INFO, tag, writeToCache, *messages)
    }

    // DEBUG 级别的日志
    fun d(
        vararg messages: Any?,
        tag: String = DEFAULT_TAG,
        writeToCache: Boolean = false,
    ) {
        log(Level.DEBUG, tag, writeToCache, *messages)
    }

    // VERBOSE 级别的日志
    fun v(
        vararg messages: Any?,
        tag: String = DEFAULT_TAG,
        writeToCache: Boolean = false,
    ) {
        log(Level.VERBOSE, tag, writeToCache, *messages)
    }

    // ERROR 级别的日志
    fun e(
        vararg messages: Any?,
        tag: String = DEFAULT_TAG,
        writeToCache: Boolean = false,
    ) {
        log(Level.ERROR, tag, writeToCache, *messages)
    }

    // WARN 级别的日志
    fun w(
        vararg messages: Any?,
        tag: String = DEFAULT_TAG,
        writeToCache: Boolean = false,
    ) {
        log(Level.WARN, tag, writeToCache, *messages)
    }

    private fun formatMessage(message: Any?): String {
        return when (message) {
            is String -> "\"$message\""
            is Number -> message.toString()
            else -> message?.toString() ?: "null"
        }
    }

    //获取调用位置
    private fun getLocation(): String {
        val traces = Thread.currentThread().stackTrace
        for (i in 4 until traces.size) {
            val trace = traces[i]
            if (!trace.className.startsWith(this.javaClass.name)) {
                val fileName = trace.fileName ?: "Unknown"
                val lineNumber = trace.lineNumber
                return "($fileName:$lineNumber)"
            }
        }
        return ""
    }

    //核心
    private fun log(
        level: Level,
        tag: String,
        writeToCache: Boolean,
        vararg messages: Any?
    ) {
        if (!isLogEnabled) return

        val traceInfo = getLocation()
        val formattedMessages = messages.map { formatMessage(it) }
        val logMessage = buildString {
            traceInfo?.let { appendLine(it) }
            formattedMessages.forEach { appendLine(it) }
        }.trimEnd()

        print(level, tag, logMessage)

        if (writeToCache) {
            checkAndMaintainCacheSize()
            writeLogToLocalCache(logMessage)
        }
    }


    // 打印日志，并处理过长的日志消息
    private fun print(level: Level, tag: String, message: String) {

        if (message.length > MAX_LOG_LENGTH) {
            val chunkCount = message.length / MAX_LOG_LENGTH
            for (i in 0..chunkCount) {
                val start = i * MAX_LOG_LENGTH
                val end = if (i == chunkCount) message.length else start + MAX_LOG_LENGTH
                val partMessage = message.substring(start, end)
                printSingle(level, tag, partMessage)
            }
        } else {
            printSingle(level, tag, message)
        }
    }

    // 根据日志级别打印消息
    private fun printSingle(level: Level, tag: String, message: String) {
        when (level) {
            Level.WARN -> Log.w(tag, message)
            Level.INFO -> Log.i(tag, message)
            Level.VERBOSE -> Log.v(tag, message)
            Level.DEBUG -> Log.d(tag, message)
            Level.ERROR -> Log.e(tag, message)
        }
    }

    // 检查并维护日志缓存的大小
    private fun checkAndMaintainCacheSize() {
        val file = File(AppUtils.getAppContext().filesDir, LOG_CACHE_FILENAME)
        if (file.exists() && file.length() > logCacheSizeLimit) {
            file.delete()
        }
    }

    // 将日志写入应用的私有缓存目录
    private fun writeLogToLocalCache(log: String) {
        AppUtils.getAppContext().openFileOutput(LOG_CACHE_FILENAME, Context.MODE_APPEND).use {
            it.write("$log\n".toByteArray())
        }
    }

    // 从应用的私有缓存目录读取日志
    fun readLogsFromLocalCache(): String? {
        return try {
            AppUtils.getAppContext().openFileInput(LOG_CACHE_FILENAME).bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            Log.e(DEFAULT_TAG, "Error reading logs from local cache: ${e.localizedMessage}")
            null
        }
    }

    // 从应用的私有缓存目录删除日志文件
    fun deleteLogsFromLocalCache() {
        val file = File(AppUtils.getAppContext().filesDir, LOG_CACHE_FILENAME)
        if (file.exists()) {
            file.delete()
        }
    }

    // 日志级别的枚举
    private enum class Level {
        WARN, INFO, VERBOSE, DEBUG, ERROR
    }


    fun Any.logI(vararg messages: Any?, tag: String = DEFAULT_TAG, writeToCache: Boolean = false) =
        i(messages = messages, tag = tag, writeToCache = writeToCache)

    fun Any.logW(vararg messages: Any?, tag: String = DEFAULT_TAG, writeToCache: Boolean = false) =
        w(messages = messages, tag = tag, writeToCache = writeToCache)

    fun Any.logD(vararg messages: Any?, tag: String = DEFAULT_TAG, writeToCache: Boolean = false) =
        d(messages = messages, tag = tag, writeToCache = writeToCache)

    fun Any.logV(vararg messages: Any?, tag: String = DEFAULT_TAG, writeToCache: Boolean = false) =
        v(messages = messages, tag = tag, writeToCache = writeToCache)

    fun Any.logE(vararg messages: Any?, tag: String = DEFAULT_TAG, writeToCache: Boolean = false) =
        e(messages = messages, tag = tag, writeToCache = writeToCache)


}