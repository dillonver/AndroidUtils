package xyz.dcln.androidutils.utils

import android.content.Context
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 3:33
 */
object CleanUtils {

    // 清除内部缓存
    fun cleanInternalCache() {
        deleteFilesInDir(AppUtils.getApp().cacheDir)
    }

    // 清除内部文件
    fun cleanInternalFiles() {
        deleteFilesInDir(AppUtils.getApp().filesDir)
    }

    // 清除内部数据库
    fun cleanInternalDbs() {
        deleteFilesWithExtension(AppUtils.getApp().getDatabasePath(""), ".db")
    }

    // 根据名称清除内部数据库
    fun cleanInternalDbByName(dbName: String) {
        AppUtils.getApp().getDatabasePath(dbName)?.let { deleteFile(it) }
    }

    // 清除内部 SP
    fun cleanInternalSp() {
        AppUtils.getApp().getSharedPreferences("", Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
    }

    // 清除外部缓存
    fun cleanExternalCache() {
        if (isExternalStorageAvailable()) {
            deleteFilesInDir(AppUtils.getApp().externalCacheDir)
        }
    }

    // 删除目录下的所有文件
    private fun deleteFilesInDir(dir: File?) {
        dir?.let {
            if (it.isDirectory) {
                val path = Paths.get(it.absolutePath)
                Files.newDirectoryStream(path)
                    .use { directoryStream ->
                        for (child in directoryStream) {
                            deleteFile(child.toFile())
                        }
                    }
            }
        }
    }

    // 删除指定扩展名的文件
    private fun deleteFilesWithExtension(dir: File?, extension: String) {
        dir?.let {
            if (it.isDirectory) {
                val path = Paths.get(it.absolutePath)
                Files.walk(path)
                    .map(Path::toFile)
                    .filter { file -> file.isFile && file.name.endsWith(extension) }
                    .forEach { file -> deleteFile(file) }
            }
        }
    }

    // 删除文件
    private fun deleteFile(file: File?) {
        file?.let {
            if (it.exists()) {
                it.delete()
            }
        }
    }

    // 检查外部存储是否可用
    private fun isExternalStorageAvailable(): Boolean {
        return android.os.Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED
    }
}