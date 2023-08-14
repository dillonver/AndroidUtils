package xyz.dcln.androidutils.utils

import android.content.Context
import android.os.Build
import android.os.Environment

/**
 * Created by dcl on 2023/7/7.
 */
object PathUtils {
    private val appContext: Context = AppUtils.getAppContext()

    // 内部存储路径
    fun getInternalAppDataPath(): String? = appContext.filesDir.parent // 应用数据文件夹路径
    fun getInternalAppFilesPath(): String = appContext.filesDir.absolutePath // 应用文件目录路径
    fun getInternalAppCachePath(): String = appContext.cacheDir.absolutePath // 应用缓存目录路径
    fun getInternalAppCodeCacheDir(): String = appContext.codeCacheDir.absolutePath // 应用代码缓存目录路径
    fun getInternalAppNoBackupFilesPath(): String =
        appContext.noBackupFilesDir.absolutePath // 应用非备份文件目录路径

    fun getInternalAppSpPath(): String =
        "${appContext.filesDir.parent}/shared_prefs" // 应用SharedPreferences路径

    // 系统路径
    fun getRootPath(): String = Environment.getRootDirectory().absolutePath // 根目录路径
    fun getDataPath(): String = Environment.getDataDirectory().absolutePath // 系统数据目录路径
    fun getDownloadCachePath(): String =
        Environment.getDownloadCacheDirectory().absolutePath // 系统下载缓存目录路径

    // 根据Android版本和目录类型获取外部存储路径
    private fun getExternalPath(type: String?): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appContext.getExternalFilesDir(type)?.absolutePath
        } else {
            if (type == null) Environment.getExternalStorageDirectory()?.absolutePath
            else Environment.getExternalStoragePublicDirectory(type)?.absolutePath
        }
    }

    fun getExternalStoragePath(): String? = getExternalPath(null) // 外部存储根目录路径
    fun getExternalAppDataPath(): String? = appContext.externalCacheDir?.parent // 外部存储应用数据目录路径
    fun getExternalAppCachePath(): String? =
        appContext.externalCacheDir?.absolutePath // 外部存储应用缓存目录路径

    // 获取指定类型的外部存储路径
    private fun getExternalPathForType(type: String): String? = getExternalPath(type)

    // 使用上述函数获取特定目录类型的外部存储路径
    fun getExternalMusicPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_MUSIC) // 音乐目录路径

    fun getExternalPodcastsPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_PODCASTS) // 播客目录路径

    fun getExternalRingtonesPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_RINGTONES) // 铃声目录路径

    fun getExternalAlarmsPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_ALARMS) // 闹钟目录路径

    fun getExternalNotificationsPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_NOTIFICATIONS) // 通知目录路径

    fun getExternalPicturesPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_PICTURES) // 图片目录路径

    fun getExternalMoviesPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_MOVIES) // 视频目录路径

    fun getExternalDownloadsPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_DOWNLOADS) // 下载目录路径

    fun getExternalDcimPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_DCIM) // 相册目录路径

    fun getExternalDocumentsPath(): String? =
        getExternalPathForType(Environment.DIRECTORY_DOCUMENTS) // 文档目录路径

    // 应用特定的外部存储路径
    fun getExternalAppFilesPath(): String? =
        appContext.getExternalFilesDir(null)?.absolutePath // 外部存储应用文件目录路径

    fun getExternalAppMusicPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath // 外部存储应用音乐目录路径

    fun getExternalAppPodcastsPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_PODCASTS)?.absolutePath // 外部存储应用播客目录路径

    fun getExternalAppRingtonesPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_RINGTONES)?.absolutePath // 外部存储应用铃声目录路径

    fun getExternalAppAlarmsPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_ALARMS)?.absolutePath // 外部存储应用闹钟目录路径

    fun getExternalAppNotificationsPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS)?.absolutePath // 外部存储应用通知目录路径

    fun getExternalAppPicturesPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath // 外部存储应用图片目录路径

    fun getExternalAppMoviesPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath // 外部存储应用视频目录路径

    fun getExternalAppDownloadPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath // 外部存储应用下载目录路径

    fun getExternalAppDcimPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath // 外部存储应用相册目录路径

    fun getExternalAppDocumentsPath(): String? =
        appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath // 外部存储应用文档目录路径

}
