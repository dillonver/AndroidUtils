package xyz.dcln.androidutils.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

object FileUtils {
    /**
     * 判断文件是否存在
     * @param path 文件路径
     * @return 文件是否存在
     */
    fun isFileExists(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }

    /**
     * 根据文件路径获取文件
     * @param path 文件路径
     * @return 文件对象，如果文件不存在则返回 null
     */
    fun getFileByPath(path: String): File? {
        val file = File(path)
        return if (file.exists()) {
            file
        } else {
            null
        }
    }


    /**
     * 读取文件内容
     * @param path 文件路径
     * @return 文件内容
     */
    fun readFile(path: String): String {
        val file = File(path)
        return file.readText()
    }

    /**
     * 写入文件内容
     * @param path 文件路径
     * @param content 文件内容
     */
    fun writeFile(path: String, content: String) {
        val file = File(path)
        file.writeText(content)
    }

    /**
     * 复制文件
     * @param sourcePath 源文件路径
     * @param destinationPath 目标文件路径
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun copyFile(sourcePath: String, destinationPath: String) {
        val sourceFile = File(sourcePath)
        val destinationFile = File(destinationPath)
        sourceFile.inputStream().use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * 删除文件
     * @param path 文件路径
     * @return 操作是否成功
     */
    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    /**
     * 重命名文件
     * @param path 文件路径
     * @param newName 新文件名称
     * @return 操作是否成功
     */
    fun renameFile(path: String, newName: String): Boolean {
        val file = File(path)
        val newFile = File(file.parent, newName)
        return file.renameTo(newFile)
    }

    /**
     * 获取文件大小
     * @param path 文件路径
     * @return 文件大小，单位为字节
     */
    fun getFileSize(path: String): Long {
        val file = File(path)
        return file.length()
    }

    /**
     * 获取目录下的所有文件
     * @param directory 目录路径
     * @return 目录下的所有文件列表
     */
    fun listFiles(directory: String): List<File> {
        val dir = File(directory)
        return if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * 创建文件
     * @param path 文件路径
     * @return 操作是否成功
     */
    fun createFile(path: String): Boolean {
        val file = File(path)
        return try {
            file.createNewFile()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 打开文件的输入流
     * @param path 文件路径
     * @return 文件输入流，如果打开失败则返回 null
     */
    fun openInputStream(path: String): InputStream? {
        return try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.inputStream()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 打开文件的输出流
     * @param path 文件路径
     * @return 文件输出流，如果打开失败则返回 null
     */
    fun openOutputStream(path: String): OutputStream? {
        return try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.outputStream()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取内部存储目录
     * @param context 上下文对象
     * @return 内部存储目录的绝对路径，如果获取失败则返回 null
     */
    fun getInternalStorageDirectory(context: Context): String? {
        return context.filesDir.absolutePath
    }

    /**
     * 获取外部存储目录
     * @return 外部存储目录的绝对路径，如果外部存储不可用或获取失败则返回 null
     */
    fun getExternalStorageDirectory(): String? {
        return if (isExternalStorageAvailable()) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            null
        }
    }


    /**
     * 获取缓存目录
     * @param context 上下文对象
     * @return 缓存目录的绝对路径，如果获取失败则返回 null
     */
    fun getCacheDirectory(context: Context): String? {
        return context.cacheDir.absolutePath
    }

    /**
     * 获取外部缓存目录
     * @param context 上下文对象
     * @return 外部缓存目录的绝对路径，如果外部缓存不可用或获取失败则返回 null
     */
    fun getExternalCacheDirectory(context: Context): String? {
        return if (isExternalStorageAvailable()) {
            context.externalCacheDir?.absolutePath
        } else {
            null
        }
    }

    /**
     * 检查外部存储是否可用
     * @return 外部存储是否可用
     */
    private fun isExternalStorageAvailable(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY
    }

    /**
     * 计算文件的 MD5 值
     * @param file 文件对象
     * @return 文件的 MD5 值，如果计算失败则返回 null
     */
    fun calculateFileMD5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { inputStream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        val md5Bytes = digest.digest()
        return byteArrayToHexString(md5Bytes)
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            val hex = Integer.toHexString(b.toInt() and 0xFF)
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex)
        }
        return sb.toString()
    }


    /**
     * 在指定的文件路径创建新目录。
     *
     * @param path 希望创建目录的文件路径。
     * @param deleteIfExists 标志，指示当目录已经存在时是否删除该目录及其内容。
     * @return 布尔值，指示目录是否成功创建或已存在（且未要求删除）；
     *         如果目录无法按要求创建或删除，则返回 false。
     */
    fun createDirectory(path: String, deleteIfExists: Boolean = false): Boolean {
        // 创建一个 File 对象，代表你想要创建的目录
        val directory = File(path)

        // 如果目录已经存在，且 deleteIfExists 标志设置为 true
        if (directory.exists() && deleteIfExists) {
            // 尝试删除目录及其内容
            // 如果删除操作不成功，则返回 false
            if (!directory.deleteRecursively()) {
                return false
            }
        }

        // 如果目录不存在（或刚被删除）
        if (!directory.exists()) {
            // 尝试创建目录（以及任何必要但不存在的父目录）
            // 返回 mkdirs() 的结果，如果目录成功创建，则返回 true，否则返回 false
            return directory.mkdirs()
        }

        // 如果目录已经存在且 deleteIfExists 为 false，
        // 或目录在删除后成功重新创建，则返回 true
        return true
    }


    /**
     * Deletes a directory located at the specified path.
     *
     * @param path The path of the directory to delete.
     * @param onlyContents If true, only the contents of the directory will be deleted, leaving the directory itself intact.
     *                     If false, the directory and all its contents will be deleted.
     *
     * @return True if the operation was successful, false otherwise.
     */
    fun deleteDirectory(path: String, onlyContents: Boolean = false): Boolean {
        val directory = File(path)

        if (directory.exists()) {
            return if (onlyContents && directory.isDirectory) {
                // Delete only the contents of the directory
                directory.listFiles()?.forEach {
                    if (it.isDirectory) {
                        it.deleteRecursively() // Delete subdirectories and their contents
                    } else {
                        it.delete() // Delete individual files
                    }
                }
                true
            } else {
                // Delete the directory and its contents
                directory.deleteRecursively()
            }
        }

        // Return false if the directory does not exist
        return false
    }


    /**
     * 在指定的名字和路径下创建或获取输出目录。
     *
     * @param name 想要创建或获取的目录的名字。
     * @param deleteIfExists 标志，指示当目录已经存在时是否删除该目录及其内容。
     * @return 返回一个 File 对象，代表创建或已存在的目录；
     *         如果目录无法按要求创建或删除，则返回 null。
     */
    fun simpleOutputDirectory(name: String, deleteIfExists: Boolean = false): File? {
        // 获取合适的基本目录，可能是外部缓存目录或应用的文件目录
        val baseDir = AppUtils.getApp().externalCacheDirs.firstOrNull()
            ?: AppUtils.getApp().filesDir

        // 构建目标目录的完整路径
        val targetDirPath = File(baseDir, name).absolutePath

        // 使用 createDirectory 函数来处理目录的创建（以及可能的删除）
        val success = createDirectory(targetDirPath, deleteIfExists)

        // 如果无法创建目录，则返回 null
        if (!success) {
            return null
        }

        // 返回一个 File 对象，代表创建的目录
        return File(targetDirPath)
    }


}