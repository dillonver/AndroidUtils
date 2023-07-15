package xyz.dcln.androidutils.utils.permisson

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import xyz.dcln.androidutils.utils.ActivityUtils
import xyz.dcln.androidutils.utils.ActivityUtils.isOrientationLocked
import xyz.dcln.androidutils.utils.ActivityUtils.toggleOrientationLock
import xyz.dcln.androidutils.utils.AppUtils
import xyz.dcln.androidutils.utils.AppUtils.isAppForeground
import xyz.dcln.androidutils.utils.IntervalUtils
import xyz.dcln.androidutils.utils.LogUtils.logW
import java.util.concurrent.TimeUnit


/**
 * Description:
 * Author: Dillon
 * Date: 2023/4/6 23:19
 */
internal
object PermissionBase {

    // 判断是否有某个权限
    fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            AppUtils.getAppContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

    // 判断是否拥有多个权限
    fun hasPermissions(vararg permissions: String): Boolean =
        permissions.all { hasPermission(it) }

    /**
     * Checks if the given permission is a special permission or not.
     *
     * @param permission The permission to check.
     * @return `true` if the given permission is a special permission, `false` otherwise.
     */
    fun isSpecialPermission(permission: String?): Boolean {
        // Define an array of special permissions for different Android versions
        // If the device is running on Android S or higher, include all special permissions
        // If the device is running on Android R or higher, include only the relevant special permissions
        // If the device is running on an earlier version of Android, include only the relevant special permissions
        val specialPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.SCHEDULE_EXACT_ALARM,
                Manifest.permission.PACKAGE_USAGE_STATS,
                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Manifest.permission.BIND_VPN_SERVICE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.PACKAGE_USAGE_STATS,
                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Manifest.permission.BIND_VPN_SERVICE
            )
        } else {
            arrayOf(
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.PACKAGE_USAGE_STATS,
                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Manifest.permission.BIND_VPN_SERVICE
            )
        }

        // Check if the given permission is in the specialPermissions array
        return permission in specialPermissions
    }

    /**
     * Launches the app settings page.
     */
    fun openSettings() {
        val packageName = AppUtils.getAppContext().packageName
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        AppUtils.getAppContext().startActivity(intent)
    }

    /**
     * Request a single permission from the user.
     * @param permission The permission to request.
     * @param onGranted Callback invoked when the permission is granted.
     * @param onDenied Callback invoked when the permission is denied, but the user has not selected "Don't ask again".
     * @param onDeniedPermanently Callback invoked when the permission is denied, and the user has selected "Don't ask again".
     * @param launchSettingsOnDeniedPermanently Whether to launch the device settings page when the user has permanently denied the permission.
     */
    fun requestSinglePermission(
        activity: AppCompatActivity,
        permission: String,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null,
        onDeniedPermanently: (() -> Unit)? = null,
        launchSettingsOnDeniedPermanently: Boolean = false
    ) {
        // Check if the permission has already been granted
        if (hasPermission(permission)) {
            onGranted?.invoke()
            return
        }
        // Register the request permission launcher
        val registry = activity.activityResultRegistry
        val launcher = registry.register(
            "requestSinglePermission",
            ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result) {
                onGranted?.invoke()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    onDenied?.invoke()
                } else {
                    onDeniedPermanently?.invoke()
                    if (launchSettingsOnDeniedPermanently) {
                        openSettings()
                    }
                }
            }
        }
        launcher.launch(permission)
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
    fun requestMultiPermissions(
        activity: AppCompatActivity,
        permissions: Array<out String>,
        onAllGranted: (() -> Unit)? = null,
        onAllDenied: (() -> Unit)? = null,
        onPartialGranted: ((List<String>) -> Unit)? = null,
        onPartialDenied: ((List<String>) -> Unit)? = null,
        onDeniedPermanently: ((List<String>) -> Unit)? = null,
        launchSettingsOnDeniedPermanently: Boolean = false
    ) {
        // Check if all permissions have already been granted
        if (hasPermissions(*permissions)) {
            onAllGranted?.invoke()
            return
        }
        // Register the request multiple permissions launcher
        val registry = activity.activityResultRegistry
        val launcher = registry.register(
            "requestMultiPermissions",
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val grantedPermissions = mutableListOf<String>()
            val deniedPermissions = mutableListOf<String>()
            val permanentlyDeniedPermissions = mutableListOf<String>()

            // Iterate over the results and categorize the permissions
            result.forEach { (permission, granted) ->
                if (granted) {
                    grantedPermissions.add(permission)
                } else {
                    deniedPermissions.add(permission)
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            permission
                        )
                    ) {
                        permanentlyDeniedPermissions.add(permission)
                    }
                }
            }

            // Invoke the corresponding callbacks based on the permission results
            when {
                grantedPermissions.size == permissions.size -> onAllGranted?.invoke()
                deniedPermissions.size == permissions.size -> onAllDenied?.invoke()


                else -> {
                    onPartialGranted?.invoke(grantedPermissions)
                    onPartialDenied?.invoke(deniedPermissions)
                }
            }

            // Invoke the permanently denied callback if any permission has been denied permanently
            if (permanentlyDeniedPermissions.isNotEmpty()) {
                onDeniedPermanently?.invoke(permanentlyDeniedPermissions)
                if (launchSettingsOnDeniedPermanently) {
                    openSettings()
                }
            }
        }
        // Launch the permission request
        launcher.launch(permissions.toList().toTypedArray())
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
    fun requestSpecialPermission(
        activity: AppCompatActivity,
        permissionCheck: () -> Boolean,
        permissionIntent: () -> Intent,
        autoReturn: Boolean = true,
        autoReturnSeconds: Long = 60,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        var resultFinish = false
        // Check if the permission has already been granted
        if (permissionCheck()) {
            onGranted?.invoke()
            return
        }

        // Lock the screen orientation during the permission request
        val lockOrientation = activity.isOrientationLocked()
        activity.toggleOrientationLock(true)

        // Register the request permission launcher
        val registry = activity.activityResultRegistry
        val launcher = registry.register(
            "requestSpecialPermission",
            ActivityResultContracts.StartActivityForResult()
        ) {
            activity.toggleOrientationLock(lockOrientation)
            if (!resultFinish) {
                if (permissionCheck()) {
                    onGranted?.invoke()
                } else {
                    onDenied?.invoke()
                }
                resultFinish = true
            }

        }

        // Launch the permission request
        launcher.launch(permissionIntent())

        if (!autoReturn) return
        // Ensure autoReturnSeconds is at least 30 seconds
        if (autoReturnSeconds < 30) {
            logW("autoReturnSeconds must be >= 30")
            return
        }
        IntervalUtils.createInterval(
            end = 0,
            period = 1,
            unit = TimeUnit.SECONDS,
            start = autoReturnSeconds,
            onTick = { controller, count ->
                if (permissionCheck()) {
                    if (!isAppForeground()) {
                        ActivityUtils.startActivity(
                            activity,
                            activity::class.java,
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        )
                    }
                    controller.cancel()
                } else {
                    if (count != autoReturnSeconds) {
                        if (isAppForeground()) {
                            controller.cancel()
                        }
                    }
                }
            },
            onFinish = {
                if (!isAppForeground()) {
                    ActivityUtils.startActivity(
                        activity,
                        activity::class.java,
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }
            }
        ).life(activity.lifecycle).start()
    }

}