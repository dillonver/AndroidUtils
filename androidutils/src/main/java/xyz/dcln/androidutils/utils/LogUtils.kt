package xyz.dcln.androidutils.utils

import android.util.Log
import com.google.gson.Gson
import xyz.dcln.androidutils.AndroidUtils
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock

object LogUtils {
    private val TAG = javaClass.simpleName
    private var isLoggingEnabled = true
    private var logCacheDir = getAppLogCacheDir()
    private var logCacheFileName: String = "log_cache.txt"
    private var logCacheMaxSize: Long = 10 * 1024 * 1024 // 默认 10MB
    private val lock = ReentrantLock()
    private val gson = Gson()

    /**
     * 开启日志输出
     */
    fun enableLogging(): LogUtils {
        isLoggingEnabled = true
        return this
    }

    /**
     * 关闭日志输出
     */
    fun disableLogging(): LogUtils {
        isLoggingEnabled = false
        return this
    }


    /**
     * 配置缓存文件
     *
     * @param fileName 缓存文件名 ,为避免写入读取权限问题，缓存位置为应用的私有文件目录下的 log_cache 子目录
     * @param maxSize  缓存文件最大大小，单位为字节，默认为 10MB
     */
    fun setCacheFile(
        fileName: String = "log_cache.txt",
        maxSize: Long = 10 * 1024 * 1024
    ): LogUtils {
        logCacheFileName = fileName
        logCacheMaxSize = maxSize
        return this
    }


    /**
     * 读取日志缓存
     * @param fileName 缓存文件名
     * @param limit 读取的行数限制，默认为 -1，表示不限制行数
     */
    fun readLogCache(fileName: String = "log_cache.txt", limit: Int = -1): String? {
        if (logCacheDir != null) {
            val logCacheFile = File(logCacheDir, fileName)
            if (logCacheFile.exists()) {
                val lines = mutableListOf<String>()
                var lineCount = 0
                var lineLength = 0L
                BufferedReader(FileReader(logCacheFile)).use { reader ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        lines.add(line)
                        lineCount++
                        lineLength += line.length + 1 // +1 是因为 BufferedReader 会在每行末尾加上换行符
                        if (limit in 1..lineCount) {
                            break
                        }
                    }
                }
                return lines.joinToString("\n")
            }
        }
        return null
    }


    /**
     * 删除日志缓存
     */
    fun deleteLogCache(): LogUtils {
        if (logCacheDir != null) {
            val logCacheFile = File(logCacheDir, logCacheFileName)
            if (logCacheFile.exists()) {
                if (logCacheFile.delete()) {
                    Log.d(TAG, "Log cache file deleted successfully.")
                } else {
                    Log.e(TAG, "Failed to delete log cache file.")
                }
            } else {
                Log.d(TAG, "Log cache file doesn't exist.")
            }
        }
        return this
    }


    /**
     * 输出 VERBOSE 级别的日志
     *
     * @param obj           日志内容
     * @param tag           日志标签
     * @param cacheEnabled  是否缓存日志
     */
    fun v(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.VERBOSE,
            tag = tag,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    fun v(obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.VERBOSE,
            tag = TAG,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    /**
     * 输出 DEBUG 级别的日志
     *
     * @param obj           日志内容
     * @param tag           日志标签
     * @param cacheEnabled  是否缓存日志
     */
    fun d(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.DEBUG,
            tag = tag,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    fun d(obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.DEBUG,
            tag = TAG,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    /**
     * 输出 INFO 级别的日志
     *
     * @param obj           日志内容
     * @param tag           日志标签
     * @param cacheEnabled  是否缓存日志
     */
    fun i(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.INFO,
            tag = tag,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    fun i(obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.INFO,
            tag = TAG,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    /**
     * 输出 WARN 级别的日志
     *
     * @param obj           日志内容
     * @param tag           日志标签
     * @param cacheEnabled  是否缓存日志
     */
    fun w(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.WARN,
            tag = tag,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    fun w(obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.WARN,
            tag = TAG,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    /**
     * 输出 ERROR 级别的日志
     *
     * @param obj           日志内容
     * @param tag           日志标签
     * @param cacheEnabled  是否缓存日志
     */
    fun e(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.ERROR,
            tag = tag,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    fun e(obj: Any?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        log(
            level = Log.ERROR,
            tag = TAG,
            obj = obj,
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    fun e(tag: String = TAG, throwable: Throwable?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        if (throwable == null) return
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        log(
            level = Log.ERROR,
            tag = tag,
            obj = sw.toString(),
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }

    fun e(throwable: Throwable?, cacheEnabled: Boolean = false) {
        if (!isLoggingEnabled) return
        if (throwable == null) return
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        log(
            level = Log.ERROR,
            tag = TAG,
            obj = sw.toString(),
            location = getLocation(),
            cacheEnabled = cacheEnabled
        )
    }


    /**
     * 判断对象是否为数据类
     *
     * @param obj   待判断对象
     * @return      true：是数据类，false：不是数据类
     */
    fun isDataClass(obj: Any?): Boolean = obj?.let { it::class.isData } ?: false


    /**
     * 获取日志级别字符
     *
     * @param level 日志级别
     * @return      日志级别字符
     */
    private fun getLogLevelChar(level: Int): Char {
        return when (level) {
            Log.VERBOSE -> 'V'
            Log.DEBUG -> 'D'
            Log.INFO -> 'I'
            Log.WARN -> 'W'
            Log.ERROR -> 'E'
            Log.ASSERT -> 'A'
            else -> '?'
        }
    }

    /**
     * 缓存日志
     *
     * @param level         日志级别
     * @param tag           日志标签
     * @param messages      日志内容
     * @param cacheEnabled  是否缓存日志
     */
    private fun cacheLog(
        level: Int,
        tag: String,
        messages: String?,
        cacheEnabled: Boolean = false
    ) {
        if (messages.isNullOrBlank()) return
        if (cacheEnabled && logCacheDir != null) {
            lock.lock()
            try {
                val logCacheFile = File(logCacheDir, logCacheFileName)
                val logCacheSize = logCacheFile.length()
                if (logCacheSize > logCacheMaxSize) {
                    // 日志滚动
                    val lines = mutableListOf<String>()
                    var lineCount = 0
                    var lineLength = 0L
                    var index = 0
                    BufferedReader(FileReader(logCacheFile)).use { reader ->
                        while (true) {
                            val line = reader.readLine() ?: break
                            lineLength += line.length + 1 // +1 是因为 BufferedReader 会在每行末尾加上换行符
                            lines.add(line)
                            lineCount++
                            if (lineLength > logCacheMaxSize / 2 || lineCount > 10000) {
                                break
                            }
                        }
                    }
                    if (lines.isNotEmpty()) {
                        val newLogCacheFile =
                            File(
                                logCacheDir,
                                "${logCacheFileName}_${System.currentTimeMillis()}"
                            )
                        BufferedWriter(FileWriter(newLogCacheFile)).use { writer ->
                            lines.forEach {
                                writer.write(it)
                                writer.newLine()
                            }
                        }
                        logCacheFile.delete()
                        logCacheFile.renameTo(newLogCacheFile)
                    }
                }
                FileWriter(logCacheFile, true).use {
                    val timestamp = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss.SSS",
                        Locale.getDefault()
                    ).format(Date())
                    it.write("$timestamp " + getLogLevelChar(level) + " $tag: ")
                    it.write(messages)
                    it.write("\n")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to cache log: ${e.message}")
            } finally {
                lock.unlock()
            }
        }
    }


    /**
     * 获取调用位置
     *
     */
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

    private const val MAX_LOG_LENGTH = 4000
    private fun printLongMessage(printer: (String, String) -> Int, tag: String, message: String) {
        if (message.length <= MAX_LOG_LENGTH) {
            printer(tag, message)
            return
        }
        message.chunked(MAX_LOG_LENGTH).forEach { chunk ->
            printer(tag, chunk)
        }
    }

    /**
     * 输出日志
     *
     * @param level         日志级别
     * @param tag           日志标签
     * @param obj           日志内容
     * @param cacheEnabled  是否缓存日志
     */
    private fun log(
        level: Int,
        tag: String = TAG,
        obj: Any?,
        location: String,
        cacheEnabled: Boolean = false
    ) {
        val message = when (obj) {
            is Int, is Long, is Float, is Double -> obj.toString()
            is String -> "\"$obj\""
            else -> obj?.let { if (isDataClass(it)) gson.toJson(it) else it.toString() } ?: "null"
        }
        val fullMessage = "$location: $message"

        // 根据不同的日志级别打印日志
        when (level) {
            Log.VERBOSE -> {
                printLongMessage(Log::v, tag, fullMessage)
            }
            Log.DEBUG -> {
                printLongMessage(Log::d, tag, fullMessage)
            }
            Log.INFO -> {
                printLongMessage(Log::i, tag, fullMessage)
            }
            Log.WARN -> {
                printLongMessage(Log::w, tag, fullMessage)
            }
            Log.ERROR -> {
                printLongMessage(Log::e, tag, fullMessage)
            }
        }

        // 缓存日志
        cacheLog(level, tag, fullMessage, cacheEnabled = cacheEnabled)
    }


    /**
     * 为避免写入读取权限问题，缓存位置为应用的私有文件目录下的 log_cache 子目录
     */
    private fun getAppLogCacheDir(): String? {
        return AndroidUtils.withApplication { application ->
            val cacheDir = application.cacheDir
            val logCacheDir = File(cacheDir, "log_cache")
            if (!logCacheDir.exists()) {
                logCacheDir.mkdirs()
            }
            logCacheDir.absolutePath
        }
    }

    fun Any.logI(obj: Any?, cacheEnabled: Boolean = false) =
        i(obj, cacheEnabled)

    fun Any.logI(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) =
        i(tag, obj, cacheEnabled)

    fun Any.logW(obj: Any?, cacheEnabled: Boolean = false) =
        w(obj, cacheEnabled)

    fun Any.logW(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) =
        w(tag, obj, cacheEnabled)


    fun Any.logD(obj: Any?, cacheEnabled: Boolean = false) =
        d(obj, cacheEnabled)

    fun Any.logD(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) =
        d(tag, obj, cacheEnabled)

    fun Any.logV(obj: Any?, cacheEnabled: Boolean = false) =
        v(obj, cacheEnabled)

    fun Any.logV(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) =
        v(tag, obj, cacheEnabled)

    fun Any.logE(obj: Any?, cacheEnabled: Boolean = false) =
        e(obj, cacheEnabled)

    fun Any.logE(tag: String = TAG, obj: Any?, cacheEnabled: Boolean = false) =
        e(tag, obj, cacheEnabled)

    fun Any.logE(obj: Throwable?, cacheEnabled: Boolean = false) =
        e(obj, cacheEnabled)

    fun Any.logE(tag: String = TAG, obj: Throwable?, cacheEnabled: Boolean = false) =
        e(tag, obj, cacheEnabled)
}