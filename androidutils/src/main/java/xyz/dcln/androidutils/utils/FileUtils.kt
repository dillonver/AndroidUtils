package xyz.dcln.androidutils.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import java.io.BufferedInputStream
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Collections

object FileUtils {

    /**
     * 根据路径返回文件
     *
     * @param filePath 文件路径
     * @return 文件对象或 null
     */
    fun getFileByPath(filePath: String?): File? {
        return filePath?.takeIf { it.isNotBlank() }?.let { File(it) }
    }


    /**
     * 判断文件是否存在
     *
     * @param file 文件对象
     * @return 存在返回 true，否则返回 false
     */
    fun isFileExists(file: File?): Boolean {
        return file?.exists() ?: false
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return 存在返回 true，否则返回 false
     */
    fun isFileExists(filePath: String?): Boolean {
        return getFileByPath(filePath)?.let { isFileExists(it) } ?: false
    }

    /**
     * 判断是否是目录
     *
     * @param dirPath 目录路径
     * @return 是目录返回 true，否则返回 false
     */
    fun isDir(dirPath: String?): Boolean {
        return isDir(getFileByPath(dirPath))
    }

    /**
     * 判断是否是目录
     *
     * @param file 文件对象
     * @return 是目录返回 true，否则返回 false
     */
    fun isDir(file: File?): Boolean {
        return file?.let { it.exists() && it.isDirectory } ?: false
    }

    /**
     * 判断是否是文件
     *
     * @param filePath 文件路径
     * @return 是文件返回 true，否则返回 false
     */
    fun isFile(filePath: String?): Boolean {
        return isFile(getFileByPath(filePath))
    }

    /**
     * 判断是否是文件
     *
     * @param file 文件对象
     * @return 是文件返回 true，否则返回 false
     */
    fun isFile(file: File?): Boolean {
        return file?.let { it.exists() && it.isFile } ?: false
    }

    /**
     * 创建目录，如果目录存在则不做任何操作
     *
     * @param dirPath 目录路径
     * @return 创建成功或目录已存在返回 true，否则返回 false
     */
    fun createOrExistsDir(dirPath: String?): Boolean {
        return createOrExistsDir(getFileByPath(dirPath))
    }

    /**
     * 创建目录，如果目录存在则不做任何操作
     *
     * @param file 文件对象
     * @return 创建成功或目录已存在返回 true，否则返回 false
     */
    fun createOrExistsDir(file: File?): Boolean {
        return file?.let { if (it.exists()) it.isDirectory else it.mkdirs() } ?: false
    }

    /**
     * 创建文件，如果文件存在则不做任何操作
     *
     * @param filePath 文件路径
     * @return 创建成功或文件已存在返回 true，否则返回 false
     */
    fun createOrExistsFile(filePath: String?): Boolean {
        return createOrExistsFile(getFileByPath(filePath))
    }

    /**
     * 创建文件，如果文件存在则不做任何操作
     *
     * @param file 文件对象
     * @return 创建成功或文件已存在返回 true，否则返回 false
     */
    fun createOrExistsFile(file: File?): Boolean {
        return file?.let {
            if (it.exists()) it.isFile else (it.parentFile?.let { createOrExistsDir(it) } ?: false && it.createNewFile())
        } ?: false
    }

    /**
     * 创建文件，如果文件存在则删除旧文件再创建新文件
     *
     * @param filePath 文件路径
     * @return 创建成功返回 true，否则返回 false
     */
    fun createFileByDeleteOldFile(filePath: String?): Boolean {
        return createFileByDeleteOldFile(getFileByPath(filePath))
    }

    /**
     * 创建文件，如果文件存在则删除旧文件再创建新文件
     *
     * @param file 文件对象
     * @return 创建成功返回 true，否则返回 false
     */
    fun createFileByDeleteOldFile(file: File?): Boolean {
        return file?.let {
            if (it.exists() && !it.delete()) return false
            (it.parentFile?.let { createOrExistsDir(it) } ?: false && it.createNewFile())
        } ?: false
    }

    /**
     * 删除文件或目录
     *
     * @param filePath 文件路径
     * @return 删除成功返回 true，否则返回 false
     */
    fun delete(filePath: String?): Boolean {
        return delete(getFileByPath(filePath))
    }

    /**
     * 删除文件或目录
     *
     * @param file 文件对象
     * @return 删除成功返回 true，否则返回 false
     */
    fun delete(file: File?): Boolean {
        return file?.let { if (it.isDirectory) deleteDir(it) else it.delete() } ?: false
    }

    /**
     * 删除目录
     *
     * @param dir 目录对象
     * @return 删除成功返回 true，否则返回 false
     */
    private fun deleteDir(dir: File): Boolean {
        return dir.takeIf { it.exists() && it.isDirectory }?.let {
            it.listFiles()?.forEach { file -> if (file.isFile) file.delete() else deleteDir(file) }
            it.delete()
        } ?: false
    }

    /**
     * 删除目录下的所有文件和子目录
     *
     * @param dirPath 目录路径
     * @return 删除成功返回 true，否则返回 false
     */
    fun deleteAllInDir(dirPath: String?): Boolean {
        return deleteAllInDir(getFileByPath(dirPath))
    }

    /**
     * 删除目录下的所有文件和子目录
     *
     * @param dir 目录对象
     * @return 删除成功返回 true，否则返回 false
     */
    fun deleteAllInDir(dir: File?): Boolean {
        return dir?.takeIf { it.exists() && it.isDirectory }?.let {
            it.listFiles()?.forEach { file -> delete(file) }
            true
        } ?: false
    }

    /**
     * 删除目录下的所有文件
     *
     * @param dirPath 目录路径
     * @return 删除成功返回 true，否则返回 false
     */
    fun deleteFilesInDir(dirPath: String?): Boolean {
        return deleteFilesInDir(getFileByPath(dirPath))
    }

    /**
     * 删除目录下的所有文件
     *
     * @param dir 目录对象
     * @return 删除成功返回 true，否则返回 false
     */
    fun deleteFilesInDir(dir: File?): Boolean {
        return deleteFilesInDirWithFilter(dir) { it.isFile }
    }

    /**
     * 删除目录下符合过滤条件的文件
     *
     * @param dirPath 目录路径
     * @param filter 文件过滤器
     * @return 删除成功返回 true，否则返回 false
     */
    fun deleteFilesInDirWithFilter(dirPath: String?, filter: FileFilter): Boolean {
        return deleteFilesInDirWithFilter(getFileByPath(dirPath), filter)
    }

    /**
     * 删除目录下符合过滤条件的文件
     *
     * @param dir 目录对象
     * @param filter 文件过滤器
     * @return 删除成功返回 true，否则返回 false
     */
    fun deleteFilesInDirWithFilter(dir: File?, filter: FileFilter): Boolean {
        return dir?.takeIf { it.exists() && it.isDirectory }?.let {
            it.listFiles()?.filter { file -> filter.accept(file) }?.all { delete(it) } ?: true
        } ?: false
    }

    /**
     * 返回目录下的文件列表（不遍历子目录）
     *
     * @param dirPath 目录路径
     * @param comparator 文件排序比较器，默认值为 null
     * @return 文件列表
     */
    @JvmOverloads
    fun listFilesInDir(dirPath: String?, comparator: Comparator<File?>? = null): List<File?> {
        return listFilesInDir(getFileByPath(dirPath), false, comparator)
    }

    /**
     * 返回目录下的文件列表（不遍历子目录）
     *
     * @param dir 目录对象
     * @param comparator 文件排序比较器，默认值为 null
     * @return 文件列表
     */
    @JvmOverloads
    fun listFilesInDir(dir: File?, comparator: Comparator<File?>? = null): List<File?> {
        return listFilesInDir(dir, false, comparator)
    }

    /**
     * 返回目录下的文件列表
     *
     * @param dirPath 目录路径
     * @param isRecursive 是否递归遍历子目录
     * @return 文件列表
     */
    fun listFilesInDir(dirPath: String?, isRecursive: Boolean): List<File?> {
        return listFilesInDir(getFileByPath(dirPath), isRecursive)
    }

    /**
     * 返回目录下的文件列表
     *
     * @param dir 目录对象
     * @param isRecursive 是否递归遍历子目录
     * @param comparator 文件排序比较器，默认值为 null
     * @return 文件列表
     */
    @JvmOverloads
    fun listFilesInDir(
        dir: File?,
        isRecursive: Boolean,
        comparator: Comparator<File?>? = null,
    ): List<File?> {
        return dir?.takeIf { it.exists() && it.isDirectory }?.let {
            val files = mutableListOf<File?>()
            it.listFiles()?.forEach { file ->
                if (file.isFile) files.add(file)
                else if (isRecursive && file.isDirectory) files.addAll(listFilesInDir(file, true))
            }
            comparator?.let { files.sortWith(it) }
            files
        } ?: emptyList()
    }


    /**
     * 列出指定目录下的文件，不遍历子目录
     */
    fun listFilesInDirWithFilter(
        dir: File?,
        filter: FileFilter,
        isRecursive: Boolean = false,
        comparator: Comparator<File?>? = null,
    ): List<File?> {
        val files = listFilesInDirWithFilterInner(dir, filter, isRecursive)
        comparator?.let { Collections.sort(files, it) }
        return files
    }

    private fun listFilesInDirWithFilterInner(
        dir: File?,
        filter: FileFilter,
        isRecursive: Boolean,
    ): MutableList<File?> {
        val list = mutableListOf<File?>()
        if (!isDir(dir)) return list
        val files = dir!!.listFiles()
        files?.forEach { file ->
            if (filter.accept(file)) list.add(file)
            if (isRecursive && file.isDirectory) list.addAll(
                listFilesInDirWithFilterInner(
                    file,
                    filter,
                    true
                )
            )
        }
        return list
    }

    /**
     * 获取文件最后修改时间
     */
    fun getFileLastModified(filePath: String?): Long {
        return getFileLastModified(getFileByPath(filePath))
    }

    fun getFileLastModified(file: File?): Long {
        return file?.lastModified() ?: -1
    }

    /**
     * 获取文件字符集
     */
    fun getFileCharsetSimple(filePath: String?): String {
        return getFileCharsetSimple(getFileByPath(filePath))
    }

    fun getFileCharsetSimple(file: File?): String {
        if (file == null) return ""
        return if (isUtf8(file)) "UTF-8" else detectCharset(file)
    }

    private fun detectCharset(file: File): String {
        var p = 0
        var `is`: InputStream? = null
        try {
            `is` = BufferedInputStream(FileInputStream(file))
            p = (`is`.read() shl 8) + `is`.read()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return when (p) {
            0xfffe -> "Unicode"
            0xfeff -> "UTF-16BE"
            else -> "GBK"
        }
    }

    /**
     * 检查文件是否为UTF-8编码
     */
    fun isUtf8(filePath: String?): Boolean {
        return isUtf8(getFileByPath(filePath))
    }

    fun isUtf8(file: File?): Boolean {
        if (file == null) return false
        val bytes = ByteArray(24)
        var `is`: InputStream? = null
        try {
            `is` = BufferedInputStream(FileInputStream(file))
            val read = `is`.read(bytes)
            if (read != -1) {
                return isUtf8(bytes.copyOfRange(0, read)) == 100
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun isUtf8(raw: ByteArray): Int {
        if (raw.size > 3 && raw[0] == 0xEF.toByte() && raw[1] == 0xBB.toByte() && raw[2] == 0xBF.toByte()) {
            return 100
        }
        val len = raw.size
        var child = 0
        var i = 0
        while (i < len) {
            if ((raw[i].toInt() and 0xFF.toByte().toInt()) == 0xFF.toByte()
                    .toInt() || (raw[i].toInt() and 0xFE.toByte().toInt()) == 0xFE.toByte().toInt()
            ) {
                return 0
            }
            if (child == 0) {
                if ((raw[i].toInt() and 0x7F.toByte()
                        .toInt()) == raw[i].toInt() && raw[i].toInt() != 0
                ) {
                    i++
                } else if ((raw[i].toInt() and 0xC0.toByte().toInt()) == 0xC0.toByte().toInt()) {
                    child = (0 until 7).firstOrNull {
                        ((0x80 shr it).toByte()
                            .toInt() and raw[i].toInt()) == (0x80 shr it).toByte()
                            .toInt()
                    } ?: 0
                    i++
                }
            } else {
                child = if (raw.size - i > child) child else (raw.size - i)
                val currentNotUtf8 = (0 until child).any {
                    (raw[i + it].toInt() and 0x80.toByte().toInt()) != 0x80.toByte().toInt()
                }
                if (currentNotUtf8) {
                    i++
                    child = 0
                } else {
                    i += child
                }
            }
        }
        return 100 * ((raw.count { it in 0x00..0x7F } + raw.count { it in 0xC0..0xDF }) / len.toFloat()).toInt()
    }

    /**
     * 获取文件长度
     */
    fun getLength(filePath: String?): Long {
        return getLength(getFileByPath(filePath))
    }

    fun getLength(file: File?): Long {
        return if (file == null) 0 else if (file.isDirectory) getDirLength(file) else getFileLength(
            file
        )
    }

    private fun getDirLength(dir: File): Long {
        if (!isDir(dir)) return 0
        return dir.listFiles()?.sumOf { if (it.isDirectory) getDirLength(it) else it.length() } ?: 0
    }


    private fun getFileLength(file: File?): Long {
        return file?.length() ?: -1
    }

    /**
     * 获取文件MD5值
     */
    fun getFileMD5(filePath: String?): ByteArray? {
        return getFileMD5(getFileByPath(filePath))
    }

    fun getFileMD5(file: File?): ByteArray? {
        if (file == null) return null
        return try {
            val fis = FileInputStream(file)
            val md = MessageDigest.getInstance("MD5")
            DigestInputStream(fis, md).use { dis ->
                val buffer = ByteArray(1024 * 256)
                while (dis.read(buffer) > 0) {
                }
                md.digest()
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取文件所在目录路径
     */
    fun getDirName(filePath: String): String {
        return filePath.takeIf { it.isNotBlank() }?.let {
            it.substringBeforeLast(File.separator, "")
        } ?: ""
    }

    fun getDirName(file: File?): String {
        return file?.absolutePath?.let { getDirName(it) } ?: ""
    }

    /**
     * 获取文件名
     */
    fun getFileName(filePath: String): String {
        return filePath.takeIf { it.isNotBlank() }?.let {
            it.substringAfterLast(File.separator, it)
        } ?: ""
    }

    fun getFileName(file: File?): String {
        return file?.name ?: ""
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
     * 获取或创建用于存储文件的目录，优先使用外部缓存目录，如果不可用则回退到应用内部文件目录。
     *
     * @param directoryName 要获取或创建的目录名称。
     * @param recreateIfExists 如果目录已存在，是否重新创建它。
     *
     * @return 指向所需目录的 File 对象。
     */
    fun getOrCreateStorageDirectory(
        directoryName: String,
        recreateIfExists: Boolean = false,
    ): File {
        // 尝试在应用的外部缓存目录中检索或创建目录
        val externalCacheDir = AppUtils.getApp().externalCacheDirs.firstOrNull()?.let {
            val directory = File(it, directoryName) // 目标目录

            // 如果需要，删除现有目录及其内容
            if (recreateIfExists && directory.exists()) {
                directory.deleteRecursively()
            }

            // 确保目录存在（如果不存在则创建它）
            directory.apply { mkdirs() }
        }

        // 如果无法使用外部缓存目录，则回退到应用的文件目录
        return externalCacheDir ?: AppUtils.getApp().filesDir
    }


}
