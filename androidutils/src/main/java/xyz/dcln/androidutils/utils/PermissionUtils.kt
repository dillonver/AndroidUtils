package xyz.dcln.androidutils.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import xyz.dcln.androidutils.utils.permisson.PermissionBase


/**
 * Description:
 * Author: Dillon
 * Date: 2023/4/6 23:19
 */
object PermissionUtils {

    // 判断是否有某个权限
    fun hasPermission(permission: String): Boolean = PermissionBase.hasPermission(permission)

    // 判断是否拥有多个权限
    fun hasPermissions(vararg permissions: String): Boolean =
        PermissionBase.hasPermissions(*permissions)


    /**
     * Checks if the given permission is a special permission or not.
     *
     * @param permission The permission to check.
     * @return `true` if the given permission is a special permission, `false` otherwise.
     */
    fun isSpecialPermission(permission: String?): Boolean =
        PermissionBase.isSpecialPermission(permission)

    /**
     * Launches the app settings page.
     */
    fun openSettings() = PermissionBase.openSettings()

    /**
     * Request a single permission from the user.
     * @param permission The permission to request.
     * @param onGranted Callback invoked when the permission is granted.
     * @param onDenied Callback invoked when the permission is denied, but the user has not selected "Don't ask again".
     * @param onDeniedPermanently Callback invoked when the permission is denied, and the user has selected "Don't ask again".
     * @param launchSettingsOnDeniedPermanently Whether to launch the device settings page when the user has permanently denied the permission.
     */
    fun AppCompatActivity.requestSinglePermission(
        permission: String,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null,
        onDeniedPermanently: (() -> Unit)? = null,
        launchSettingsOnDeniedPermanently: Boolean = true
    ) {
        PermissionBase.requestSinglePermission(
            this,
            permission,
            onGranted,
            onDenied,
            onDeniedPermanently,
            launchSettingsOnDeniedPermanently
        )
    }


    /**
     * Request multiple permissions from the user.
     * @param permissions The permissions to request.
     * @param onAllGranted Callback invoked when all permissions are granted.
     * @param onAllDenied Callback invoked when all permissions are denied, but the user has not selected "Don't ask again" for any of them.
     * @param onPartialGranted Callback invoked when some permissions are granted.
     * @param onPartialDenied Callback invoked when some permissions are denied, but the user has not selected "Don't ask again" for any of them.
     * @param onDeniedPermanently Callback invoked when any permission is denied, and the user has selected "Don't ask again" for that permission.
     * @param launchSettingsOnDeniedPermanently Whether to launch the device settings page when the user has permanently denied the permission.
     */
    fun AppCompatActivity.requestMultiPermissions(
        vararg permissions: String,
        onAllGranted: (() -> Unit)? = null,
        onAllDenied: (() -> Unit)? = null,
        onPartialGranted: ((List<String>) -> Unit)? = null,
        onPartialDenied: ((List<String>) -> Unit)? = null,
        onDeniedPermanently: ((List<String>) -> Unit)? = null,
        launchSettingsOnDeniedPermanently: Boolean = true
    ) {
        PermissionBase.requestMultiPermissions(
            this,
            permissions,
            onAllGranted,
            onAllDenied,
            onPartialGranted,
            onPartialDenied,
            onDeniedPermanently,
            launchSettingsOnDeniedPermanently
        )
    }

    /**
     *Requests a special permission that requires a custom intent to launch.
     *@param permissionCheck A function to check if the permission has already been granted.
     *@param permissionIntent A function to create the intent to request the permission.
     *@param autoReturn Whether to automatically return to the calling activity after the permission is granted, if the app is not in the foreground.
     *@param autoReturnSeconds The number of seconds before automatically returning to the calling activity after the permission is granted, if autoReturn is true.
     *At least 30 seconds. Otherwise, return not to take effect.
     *@param onGranted A callback invoked when the permission is granted.
     *@param onDenied A callback invoked when the permission is denied.
     */
    fun AppCompatActivity.requestSpecialPermission(
        permissionCheck: () -> Boolean,
        permissionIntent: () -> Intent,
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        PermissionBase.requestSpecialPermission(
            this,
            permissionCheck,
            permissionIntent,
            autoReturn,
            autoReturnSeconds,
            onGranted,
            onDenied
        )
    }


    fun isGrantedCanDrawOverlaysPermission() = Settings.canDrawOverlays(AppUtils.getAppContext())

    /**
     * Requests the SYSTEM_ALERT_WINDOW permission (overlay permission) for the given activity.
     *
     * @param autoReturnSeconds The number of seconds before the app automatically checks the permission and returns.
     * Must be at least 30 seconds.
     * @param onGranted A function that will be called when the permission is granted.
     * @param onDenied A function that will be called when the permission request fails.
     */
    @RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun AppCompatActivity.requestDrawOverlays(
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedCanDrawOverlaysPermission() },
            permissionIntent = {
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${AppUtils.getAppPackageName()}")
                )
            },
            autoReturn = autoReturn,
            autoReturnSeconds = autoReturnSeconds,
            onGranted = onGranted,
            onDenied = onDenied
        )

    }

    /**
     * Checks if  has picture-in-picture permission.
     *
     * @return `true` if the current activity has picture-in-picture permission, `false` otherwise.
     */
    fun isGrantedPictureInPicturePermission(): Boolean {
        val context = AppUtils.getAppContext()
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                context.applicationInfo.uid, context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                context.applicationInfo.uid, context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun AppCompatActivity.requestPictureInPicture(
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedPictureInPicturePermission() },
            permissionIntent = {
                Intent(
                    "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                    Uri.parse("package:${AppUtils.getAppPackageName()}")
                )
            },
            autoReturn = autoReturn,
            autoReturnSeconds = autoReturnSeconds,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    /**
     * 是否有所有文件的管理权限
     */
    @RequiresApi(Build.VERSION_CODES.R)
    @RequiresPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    fun isGrantedManageStoragePermission() = Environment.isExternalStorageManager()

    @RequiresApi(Build.VERSION_CODES.R)
    @RequiresPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    fun AppCompatActivity.requestManageExternalStorage(
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedManageStoragePermission() },
            permissionIntent = {
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${AppUtils.getAppPackageName()}")
                )
            },
            autoReturn = autoReturn,
            autoReturnSeconds = autoReturnSeconds,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    /**
     * 是否有安装权限
     */
    private fun isGrantedInstallPermission(): Boolean {
        return AppUtils.getAppContext().packageManager.canRequestPackageInstalls()
    }

    @RequiresPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES)
    fun AppCompatActivity.requestInstallPermission(
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedInstallPermission() },
            permissionIntent = {
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${AppUtils.getAppPackageName()}")
                )
            },
            autoReturn = autoReturn,
            autoReturnSeconds = autoReturnSeconds,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    /**
     * 是否有系统设置权限
     */
    private fun isGrantedWriteSettingsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(AppUtils.getAppContext())
        } else true
    }

    @RequiresPermission(Manifest.permission.WRITE_SETTINGS)
    fun AppCompatActivity.requestWriteSettingsPermission(
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedWriteSettingsPermission() },
            permissionIntent = {
                Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:${AppUtils.getAppPackageName()}")
                )
            },
            autoReturn = autoReturn,
            autoReturnSeconds = autoReturnSeconds,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }


    /**
     * Checks if the app has been granted permission to ignore battery optimizations.
     * @return True if the permission has been granted, false otherwise.
     */
    fun isGrantedIgnoreBatteryPermission(): Boolean {
        return AppUtils.getAppContext().getSystemService(PowerManager::class.java)
            .isIgnoringBatteryOptimizations(AppUtils.getAppPackageName())
    }

    /**
     * Request permission to ignore battery optimizations.
     * @param onGranted Callback invoked when the permission is granted.
     * @param onDenied Callback invoked when the permission is denied, but the user has not selected "Don't ask again".
     */
    @SuppressLint("BatteryLife")
    fun AppCompatActivity.requestIgnoreBatteryOptimizations(
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = {
                isGrantedIgnoreBatteryPermission()
            },
            permissionIntent = {
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${AppUtils.getAppPackageName()}")
                )
            },
            autoReturn = false,
            onGranted = onGranted,
            onDenied = onDenied
        )

    }

    /**
     *Check whether the permission for do-not-disturb mode is granted.
     */
    fun isGrantedNotDisturbPermission(): Boolean {
        return AppUtils.getAppContext()
            .getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted
    }

    /**
     * Request the permission to access Do Not Disturb mode.
     * @param autoReturn Whether to automatically return to the original page after permission is granted/denied.
     * @param autoReturnSeconds When autoReturn is true, the number of seconds before returning to the original page.
     * @param onGranted Callback function to be invoked when permission is granted.
     * @param onDenied Callback function to be invoked when permission is denied.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
    fun AppCompatActivity.requestNotDisturbPermission(
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedNotDisturbPermission() },
            permissionIntent = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent(
                        "android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS",
                        Uri.parse("package:${AppUtils.getAppPackageName()}")
                    )

                } else {
                    Intent(
                        Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
                        Uri.parse("package:${AppUtils.getAppPackageName()}")
                    )
                }
            },
            autoReturn = autoReturn,
            autoReturnSeconds = autoReturnSeconds,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    /**
     *Check whether the VPN permission is granted.
     */
    fun isGrantedVpnPermission(): Boolean {
        return VpnService.prepare(AppUtils.getAppContext()) == null
    }

    /**
     *Request the VPN permission from the user.
     *@param onGranted Callback function to be invoked when permission is granted.
     *@param onDenied Callback function to be invoked when permission is denied.
     */
    fun AppCompatActivity.requestVpnPermission(
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedVpnPermission() },
            permissionIntent = {
                VpnService.prepare(AppUtils.getAppContext())
            },
            autoReturn = false,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    /**
     *Check whether the Notify permission is granted.
     */
    fun isGrantedNotifyPermission(): Boolean {
        return NotificationManagerCompat.from(AppUtils.getAppContext()).areNotificationsEnabled()
    }

    /**
     *Request the Notify permission from the user.
     *@param onGranted Callback function to be invoked when permission is granted.
     *@param onDenied Callback function to be invoked when permission is denied.
     */
    fun AppCompatActivity.requestNotifyPermission(
        autoReturn: Boolean = false,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedNotifyPermission() },
            permissionIntent = {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    AppUtils.getAppPackageName()
                )
            },
            autoReturn = autoReturn,
            autoReturnSeconds = autoReturnSeconds,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    private fun isGrantedPackageUsageStatsPermission(): Boolean {
        val appOps =
            AppUtils.getAppContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                AppUtils.getAppContext().applicationInfo.uid, AppUtils.getAppContext().packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                AppUtils.getAppContext().applicationInfo.uid, AppUtils.getAppContext().packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    fun AppCompatActivity.requestPackageUsageStatsPermission(
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        requestSpecialPermission(
            permissionCheck = { isGrantedPackageUsageStatsPermission() },
            permissionIntent = {
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            },
            autoReturn = autoReturn,
            autoReturnSeconds = autoReturnSeconds,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }
}