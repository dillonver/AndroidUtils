package xyz.dcln.androidutils.utils


import android.Manifest
import android.app.ActivityManager
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import xyz.dcln.androidutils.AndroidUtils
import xyz.dcln.androidutils.utils.BusUtils.receive
import xyz.dcln.androidutils.utils.BusUtils.sendEventSticky
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.security.MessageDigest
import kotlin.system.exitProcess


object AppUtils {
    private const val TAG_APP_STATE = "tag_app_state"

    private var isAppForeground = false

    internal fun init() {
        // Observe the process lifecycle to detect foreground/background transitions.
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                // The app has entered the foreground.
                Lifecycle.Event.ON_START -> {
                    isAppForeground = true
                    sendEventSticky(TAG_APP_STATE, true)

                }
                // The app has entered the background.
                Lifecycle.Event.ON_STOP -> {
                    isAppForeground = false
                    sendEventSticky(TAG_APP_STATE, false)

                }
                // Do nothing for other events.
                else -> {}
            }
        })
    }

    fun LifecycleOwner.addAppStateListener(block: suspend CoroutineScope.(value: Boolean) -> Unit): Job {
        return receive(
            tags = arrayOf(TAG_APP_STATE),
            lifeEvent = Lifecycle.Event.ON_DESTROY,
            sticky = true,
            block = block
        )
    }


    /**
     * Check whether the application is currently in the foreground.
     *
     * @return True if the application is in the foreground, false otherwise.
     *
     * Note: Calling this function in `onCreate()` may return an incorrect result because the application has not finished initializing yet.
     * It is recommended to call this function in `onResume()` and `onPause()`, which will be called when the application enters or leaves the foreground.
     * If you need to check the foreground/background status of the application at other times, please use this function.
     */
    fun Any.isAppForeground(): Boolean = isAppForeground

    /**
     * Get the application.
     *
     * @return The application.
     */
    fun getApp(): Application = AndroidUtils.getApplication()

    /**
     * Get the application context.
     *
     * @return The application context.
     */
    fun getAppContext(): Context = getApp().applicationContext

    /**
     * Get the application package name.
     *
     * @return The application package name.
     */
    fun getAppPackageName(): String = getApp().packageName

    fun getAppPackageManager() = getAppContext().packageManager

    /**
     * Get the application icon.
     *
     * @return The application icon as a drawable.
     */
    fun getAppIcon(): Drawable? = getApp().applicationInfo.loadIcon(getApp().packageManager)

    /**
     * Get the application name.
     *
     * @return The application name as a string.
     */
    fun getAppName(): String {
        val appInfo = getApp().applicationInfo
        val packageManager = getApp().packageManager
        return appInfo.loadLabel(packageManager).toString()
    }



    /**
     * Get the application signatures.
     *
     * @return The application signatures as an array of Signature objects, or null if not found.
     */
    fun getAppSignatures(): Array<Signature>? {
        return try {
            val packageManager = getApp().packageManager
            val packageInfo = packageManager.getPackageInfo(
                getApp().packageName,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    @Suppress("DEPRECATION")
                    PackageManager.GET_SIGNATURES
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = packageInfo.signingInfo
                if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                } else {
                    null
                }
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }



    /**
     * Install an APK file
     * @param context The context object
     * @param path The path of the APK file
     */
    fun installApk(context: Context, path: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val file = File(path)
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                UriUtils.file2Uri(file)
            } else {
                Uri.fromFile(file)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }

    /**
     * Uninstall an app.
     *
     * @param packageName The package name of the app.
     */
    fun uninstallApp(packageName: String) {
        val uri = Uri.fromParts("package", packageName, null)
        val intent = Intent(Intent.ACTION_DELETE) // Use ACTION_DELETE as an alternative
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Start in a new task
        getAppContext().startActivity(intent)
    }


    /**
     * Determine whether an app is installed.
     *
     * @param packageName The package name of the app.
     * @return `true` if the app is installed, `false` otherwise.
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            getAppContext().packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_ACTIVITIES
            )
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Exit the app.
     */
    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    fun exitApp() {
        val activityManager =
            getAppContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses(getAppContext().packageName)
        exitProcess(0)
    }


    /**
     * Determine whether the app has root privileges.
     *
     * @return `true` if the app has root privileges, `false` otherwise.
     */
    fun isAppRoot(): Boolean {
        val process = Runtime.getRuntime().exec("su")
        val outputStream = process.outputStream
        outputStream.write("echo test".toByteArray())
        outputStream.flush()
        return try {
            process.waitFor()
            true
        } catch (e: InterruptedException) {
            false
        } finally {
            try {
                outputStream.close()
            } catch (e: IOException) {
                // Ignore exception
            }
            process.destroy()
        }
    }

    /**
     * Determine whether the app is a debug version.
     *
     * @return `true` if the app is a debug version, `false` otherwise.
     */
    fun isAppDebug(): Boolean {
        val ai = getAppContext().applicationInfo
        return ai.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    /**
     * Determine whether the app is a system app.
     *
     * @return `true` if the app is a system app, `false` otherwise.
     */
    fun isAppSystem(): Boolean {
        val ai = getAppContext().applicationInfo
        return ai.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    /**
     * Determine whether the app is running.
     *
     * @return `true` if the app is running, `false` otherwise.
     */
    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    fun isAppRunning(): Boolean {
        val packageName = getAppContext().packageName
        return getAppProcesses().any { it.processName == packageName }
    }

    /**
     * Get the app processes.
     *
     * @return List of app processes.
     */
    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    fun getAppProcesses(): List<ActivityManager.RunningAppProcessInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getAppProcessesForAndroidQAndAbove()
        } else {
            getAppProcessesForBelowAndroidQ()
        }
    }

    private fun getAppProcessesForBelowAndroidQ(): List<ActivityManager.RunningAppProcessInfo> {
        val activityManager =
            getAppContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    private fun getAppProcessesForAndroidQAndAbove(): List<ActivityManager.RunningAppProcessInfo> {
        val context = getAppContext()
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 1000 * 60 * 60 * 24,
            currentTime
        )

        val runningAppProcessInfos = mutableListOf<ActivityManager.RunningAppProcessInfo>()
        val packageManager = context.packageManager

        stats.forEach { usageStats ->
            try {
                val applicationInfo = packageManager.getApplicationInfo(usageStats.packageName, 0)
                val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo(
                    usageStats.packageName,
                    applicationInfo.uid,
                    arrayOf<String>()
                )
                runningAppProcessInfos.add(runningAppProcessInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                // Ignore apps that cannot be found
            }
        }
        return runningAppProcessInfos
    }


    /**
     * Launch the app.
     */
    fun launchApp(packageName: String?) {
        if (packageName.isNullOrBlank()) {
            LogUtils.e("packageName.isNullOrBlank")
            return
        }
        val intent = getAppContext().packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getAppContext().startActivity(intent)
        } else {
            // APP没有安装或无法启动
            LogUtils.e("APP没有安装或无法启动")
        }
    }


    /**
     * Relaunch the app.
     */

    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    fun relaunchApp(targetPackageName: String) {
        val context = getAppContext()
        val currentPackageName = context.packageName

        if (currentPackageName == targetPackageName) {
            // 重启自身
            relaunchSelf()
        } else {
            // 重启其他APP
            relaunchOtherApp(targetPackageName)
        }
    }

    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    fun relaunchSelf() {
        val context = getAppContext()
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            val restartIntent = Intent.makeRestartActivityTask(intent.component)
            context.startActivity(restartIntent)
            exitApp()
        } else {
            LogUtils.e("无法重新启动应用程序")
        }
    }

    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    fun relaunchOtherApp(packageName: String) {
        val context = getAppContext()

        // 停止其他应用（需要KILL_BACKGROUND_PROCESSES权限）
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.killBackgroundProcesses(packageName)

        // 重新启动其他应用
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            LogUtils.e("无法获取$packageName 的启动Intent")
        }
    }


    /**
     * Launch the app details settings.
     */
    fun launchAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = "package:${getAppContext().packageName}".toUri()
        getAppContext().startActivity(intent)
    }


    /**
     * Get the app path.
     *
     * @return The app path.
     */
    fun getAppPath(): String {
        val applicationInfo = getAppContext().applicationInfo
        return applicationInfo.sourceDir
    }


    /**
     * Get the app version name.
     *
     * @return The app version name, or an empty string ("") if not found.
     */
    fun getAppVersionName(): String {
        val packageManager = getAppContext().packageManager
        return try {
            // 使用兼容性方法获取 PackageInfo
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(getAppContext().packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(getAppContext().packageName, 0)
            }

            // 确保 versionName 不为 null，使用空合并运算符（?:）提供默认值
            packageInfo.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            // 如果包名未找到，返回空字符串
            ""
        } catch (e: Exception) {
            // 捕获其他可能的异常，返回空字符串
            ""
        }
    }

    /**
     * Get the app version code.
     *
     * @return The app version code.
     */
    fun getAppVersionCode(): Long {
        val packageManager = getAppContext().packageManager
        return try {
            val info = packageManager.getPackageInfo(getAppContext().packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                info.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }
    }

    /**
     * Get the app minimum SDK version.
     *
     * @return The app minimum SDK version, or -1 if not found or not available.
     */
    fun getAppMinSdkVersion(): Int {
        val packageManager = getAppContext().packageManager
        return try {
            // 获取 PackageInfo，使用最新 API 确保兼容性
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    getAppContext().packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    getAppContext().packageName,
                    PackageManager.GET_ACTIVITIES
                )
            }

            // 获取 ApplicationInfo
            val applicationInfo = packageInfo.applicationInfo
                ?: return -1 // 如果 ApplicationInfo 为空，返回 -1

            // 从 Android N (API 24) 开始使用 minSdkVersion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                applicationInfo.minSdkVersion
            } else {
                // 对于 API < 24，尝试从 AndroidManifest.xml 中解析 minSdkVersion
                // 但由于直接获取可能不可靠，返回保守的默认值
                Build.VERSION_CODES.BASE // 返回 Android 1.0 的值作为默认
            }
        } catch (e: PackageManager.NameNotFoundException) {
            -1 // 包未找到，返回 -1
        } catch (e: Exception) {
            -1 // 捕获其他异常，返回 -1
        }
    }

    /**
     * Get the app target SDK version.
     *
     * @return The app target SDK version, or -1 if not found or not available.
     */
    fun getAppTargetSdkVersion(): Int {
        val packageManager = getAppContext().packageManager
        return try {
            // 根据 Android 版本选择合适的 getPackageInfo 方法
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    getAppContext().packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    getAppContext().packageName,
                    PackageManager.GET_ACTIVITIES
                )
            }

            // 获取 ApplicationInfo，并检查是否为空
            val applicationInfo = packageInfo.applicationInfo ?: return -1

            // 返回 targetSdkVersion
            applicationInfo.targetSdkVersion
        } catch (e: PackageManager.NameNotFoundException) {
            -1 // 包未找到，返回 -1
        } catch (e: Exception) {
            -1 // 捕获其他异常，返回 -1
        }
    }

    /**
     * Get the SHA1 signature of the app.
     *
     * @return The SHA1 signature of the app.
     */
    fun getAppSignaturesSHA1(): String {
        val signatures = getAppSignatures() ?: return ""
        return hashSignature(signatures, "SHA1")
    }

    /**
     * Get the SHA256 signature of the app.
     *
     * @return The SHA256 signature of the app.
     */
    fun getAppSignaturesSHA256(): String {
        val signatures = getAppSignatures() ?: return ""
        return hashSignature(signatures, "SHA256")
    }

    /**
     * Get the MD5 signature of the app.
     *
     * @return The MD5 signature of the app.
     */
    fun getAppSignaturesMD5(): String {
        val signatures = getAppSignatures() ?: return ""
        return hashSignature(signatures, "MD5")
    }

    /**
     * Get the app info.
     *
     * @return The app info.
     */
    fun getAppInfo(): String {
        val builder = StringBuilder()
        builder.appendLine("App name: ${getAppName()}")
        builder.appendLine("Package name: ${getAppPackageName()}")
        builder.appendLine("Version name: ${getAppVersionName()}")
        builder.appendLine("Version code: ${getAppVersionCode()}")
        builder.appendLine("Minimum SDK version: ${getAppMinSdkVersion()}")
        builder.appendLine("Target SDK version: ${getAppTargetSdkVersion()}")
        builder.appendLine("SHA1 signature: ${getAppSignaturesSHA1()}")
        return builder.toString()
    }

    fun getAppInfoAsMap(): Map<String, String> {
        return mapOf(
            "App name" to getAppName(),
            "Package name" to getAppPackageName(),
            "Version name" to getAppVersionName(),
            "Version code" to getAppVersionCode().toString(),
            "Minimum SDK version" to getAppMinSdkVersion().toString(),
            "Target SDK version" to getAppTargetSdkVersion().toString(),
            "SHA1 signature" to getAppSignaturesSHA1()
        )
    }

    /**
     * Get info of all installed apps.
     *
     * @return A list of strings containing app information, or empty list if no apps found or error occurs.
     */
    fun getAppsInfo(): List<String> {
        val packageManager = getAppContext().packageManager
        val appsInfo = mutableListOf<String>()

        return try {
            // 检查权限（从 Android 11 开始需要 QUERY_ALL_PACKAGES 权限）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!packageManager.canRequestPackageInstalls()) {
                    return emptyList() // 如果没有安装权限，返回空列表
                }
            }

            // 获取所有已安装的包，使用最新 API
            val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }

            // 遍历包信息
            installedPackages.forEach { packageInfo ->
                try {
                    // 安全获取应用名，可能为空
                    val appName = packageInfo.applicationInfo?.loadLabel(packageManager)?.toString() ?: "Unknown App"
                    val packageName = packageInfo.packageName ?: "Unknown Package"

                    // 安全获取版本名，可能为空
                    val versionName = packageInfo.versionName ?: "No version name"

                    // 获取版本码，处理不同 API 级别
                    val versionCode = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> packageInfo.longVersionCode.toString()
                        else -> packageInfo.versionCode.toString()
                    }

                    // 构造信息字符串
                    appsInfo.add("App name: $appName, Package name: $packageName, Version name: $versionName, Version code: $versionCode")
                } catch (e: Exception) {
                    // 忽略单个应用的错误，继续处理其他应用
                    appsInfo.add("App name: Unknown, Package name: ${packageInfo.packageName ?: "Unknown"}, Version name: Error, Version code: Error")
                }
            }

            appsInfo
        } catch (e: SecurityException) {
            // 捕获权限相关异常
            emptyList()
        } catch (e: Exception) {
            // 捕获其他所有异常
            emptyList()
        }
    }

    /**
     * Get the APK info.
     *
     * @param apkPath The path of the APK file.
     * @return The APK info as a string, or empty string ("") if info cannot be retrieved.
     * @throws SecurityException if permission is denied.
     * @throws IllegalArgumentException if apkPath is invalid or empty.
     */
    fun getApkInfo(apkPath: String): String {
        // 输入验证
        if (apkPath.isEmpty()) {
            throw IllegalArgumentException("APK path cannot be empty")
        }

        val packageManager = getAppContext().packageManager

        return try {
            // 检查权限（如果 APK 在外部存储上，可能需要 READ_EXTERNAL_STORAGE）
            if (getAppContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Permission READ_EXTERNAL_STORAGE is required to read the APK file")
            }

            // 获取 APK 信息，使用最新 API
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    apkPath,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_META_DATA)
            } ?: return "" // 如果 APK 信息不可用，返回空字符串

            // 确保 applicationInfo 不为空
            val applicationInfo = packageInfo.applicationInfo ?: return ""

            // 安全获取应用名，可能为空
            val appName = packageManager.getApplicationLabel(applicationInfo)?.toString() ?: "Unknown App"
            val packageName = packageInfo.packageName ?: "Unknown Package"
            val versionName = packageInfo.versionName ?: "No version name"

            // 获取版本码，处理不同 API 级别
            val versionCode = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> packageInfo.longVersionCode.toString()
                else -> packageInfo.versionCode.toString()
            }

            // 构造并返回信息字符串
            "App name: $appName, Package name: $packageName, Version name: $versionName, Version code: $versionCode"
        } catch (e: SecurityException) {
            throw e // 抛出权限异常，让调用者处理
        } catch (e: FileNotFoundException) {
            "" // 文件未找到，返回空字符串
        } catch (e: Exception) {
            "" // 其他异常，返回空字符串
        }
    }
    /**
     * Determine whether the app is installed for the first time.
     *
     * @return `true` if the app is installed for the first time, `false` otherwise.
     */
    fun isFirstTimeInstalled(): Boolean {
        val sharedPref = getAppContext().getSharedPreferences("AppUtils", Context.MODE_PRIVATE)
        val versionCode = getAppVersionCode()
        val savedVersionCode = sharedPref.getLong("version_code", -1)
        return if (savedVersionCode == -1L) {
            sharedPref.edit().putLong("version_code", versionCode).apply()
            true
        } else {
            savedVersionCode != versionCode
        }
    }

    /**
     * Hash the signatures using the specified algorithm.
     *
     * @param signatures The signatures to hash.
     * @param algorithm The hash algorithm to use.
     * @return The hashed signatures as a string.
     */
    private fun hashSignature(signatures: Array<Signature>, algorithm: String): String {
        val messageDigest = MessageDigest.getInstance(algorithm)
        signatures.forEach { signature ->
            messageDigest.update(signature.toByteArray())
        }
        val digest = messageDigest.digest()
        val hexString = StringBuilder()
        for (i in digest.indices) {
            val hex = Integer.toHexString(0xFF and digest[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}