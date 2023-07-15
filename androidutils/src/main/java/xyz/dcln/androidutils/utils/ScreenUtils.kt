package xyz.dcln.androidutils.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresPermission

/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/4 0:46
 */
object ScreenUtils {
    /**
     * Return the width of screen, in pixel.
     */
    fun getScreenWidth(): Int = getScreenSize(AppUtils.getApp()).first

    /**
     * Return the height of screen, in pixel.
     */
    fun getScreenHeight(): Int = getScreenSize(AppUtils.getApp()).second

    /**
     * Return the density of screen.
     */
    fun getScreenDensity(): Float = Resources.getSystem().displayMetrics.density

    /**
     * Return the screen density expressed as dots-per-inch.
     */
    fun getScreenDensityDpi(): Int = Resources.getSystem().displayMetrics.densityDpi

    /**
     * Return the exact physical pixels per inch of the screen in the X dimension.
     */
    fun getScreenXDpi(): Float = Resources.getSystem().displayMetrics.xdpi

    /**
     * Return the exact physical pixels per inch of the screen in the Y dimension.
     */
    fun getScreenYDpi(): Float = Resources.getSystem().displayMetrics.ydpi

    /**
     * Toggle full screen mode.
     */
    fun Activity.toggleFullScreen(fullScreen: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本
            window.insetsController?.apply {
                if (fullScreen) {
                    hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                } else {
                    show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                }
            }
        } else {
            // Android 11以下版本
            if (fullScreen) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            window.decorView.systemUiVisibility =
                if (fullScreen) {
                    (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                } else {
                    View.SYSTEM_UI_FLAG_VISIBLE
                }
        }
    }

    /**
     * Determine if the screen is in full screen mode.
     */
    fun Activity.isFullScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            insets.top == 0 && insets.bottom == 0
        } else {
            // Android 11以下版本
            val fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN
            window.attributes.flags and fullScreenFlag == fullScreenFlag
        }
    }

    /**
     * Set the screen to landscape.
     */
    fun Activity.setLandscape() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    /**
     * Set the screen to portrait.
     */
    fun Activity.setPortrait() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * Return whether screen is landscape.
     */
    fun Context.isLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    /**
     * Return whether screen is portrait.
     */
    fun Context.isPortrait(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    /**
     * Return the rotation of screen.
     */
    fun Activity.getScreenRotation(): Int = windowManager.defaultDisplay.rotation

    /**
     * Return whether screen is locked.
     */
    fun Context.isScreenLock(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager?.isInteractive == false
        } else {
            @Suppress("DEPRECATION")
            powerManager?.isScreenOn == false
        }
    }

    /**
     * Set the duration of sleep.
     * <p>Must hold `<uses-permission android:name="android.permission.WRITE_SETTINGS" />`</p>
     */
    @RequiresPermission(android.Manifest.permission.WRITE_SETTINGS)
    fun setSleepDuration(duration: Int) {
        Settings.System.putInt(
            AppUtils.getApp().contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            duration
        )
    }

    /**
     * Return the duration of sleep.
     */
    fun getSleepDuration(): Int {
        return try {
            Settings.System.getInt(
                AppUtils.getApp().contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT
            )
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            -123
        }
    }



    private fun getScreenSize(context: Context): Pair<Int, Int> {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            Pair(bounds.width(), bounds.height())
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }


    private fun Activity.clearFullScreen() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


}
