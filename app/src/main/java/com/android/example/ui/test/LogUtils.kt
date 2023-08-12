package com.android.example.ui.test

import android.content.Context
import android.util.Log
import xyz.dcln.androidutils.utils.AppUtils
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
        trace: Boolean = true,
        writeToCache: Boolean = false,
    ) {
        log(Level.INFO, tag, trace, writeToCache, *messages)
    }

    // DEBUG 级别的日志
    fun d(
        vararg messages: Any?,
        tag: String = DEFAULT_TAG,
        trace: Boolean = true,
        writeToCache: Boolean = false,
    ) {
        log(Level.DEBUG, tag, trace, writeToCache, *messages)
    }

    // VERBOSE 级别的日志
    fun v(
        vararg messages: Any?,
        tag: String = DEFAULT_TAG,
        trace: Boolean = true,
        writeToCache: Boolean = false,
    ) {
        log(Level.VERBOSE, tag, trace, writeToCache, *messages)
    }

    // ERROR 级别的日志
    fun e(
        vararg messages: Any?,
        tag: String = DEFAULT_TAG,
        trace: Boolean = true,
        writeToCache: Boolean = false,
    ) {
        log(Level.ERROR, tag, trace, writeToCache, *messages)
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
        trace: Boolean,
        writeToCache: Boolean,
        vararg messages: Any?
    ) {
        if (!isLogEnabled) return

        val traceInfo = if (trace) {
            getLocation()
        } else null

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
        INFO, VERBOSE, DEBUG, ERROR
    }
}