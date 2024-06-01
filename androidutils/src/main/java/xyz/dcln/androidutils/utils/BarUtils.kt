package xyz.dcln.androidutils.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout

/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/8 16:11
 */
object BarUtils {

    // 获取状态栏高度（px）
    @SuppressLint("PrivateApi", "InternalInsetResource", "DiscouragedApi")
    fun getStatusBarHeight(): Int = with(AppUtils.getApp().resources) {
        val resourceId = getIdentifier("status_bar_height", "dimen", "android")
        getDimensionPixelSize(resourceId)
    }.takeIf { it > 0 } ?: run {
        try {
            val clazz = Class.forName("com.android.internal.R\$dimen")
            val field = clazz.getField("status_bar_height")
            val x = field[null] as Int
            AppUtils.getApp().resources.getDimensionPixelSize(x)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    //判断是否沉浸式状态栏
    fun isImmersiveStatusBar(activity: Activity): Boolean {
        return ActivityUtils.isImmersiveMode(activity)
    }

    // 设置沉浸式状态栏
    fun setImmersiveStatusBar(activity: Activity) {
        activity.window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            var systemUiVisibility = decorView.systemUiVisibility
            systemUiVisibility =
                systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = systemUiVisibility
            statusBarColor = Color.TRANSPARENT
        }
    }


    // 设置状态栏是否可见
    fun setStatusBarVisibility(activity: Activity, isVisible: Boolean) {
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        if (isVisible) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            insetsController?.show(WindowInsetsCompat.Type.statusBars())
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                insetsController?.let { controller ->
                    controller.hide(WindowInsetsCompat.Type.statusBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_FULLSCREEN
                        )
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    // 判断状态栏是否可见
    fun isStatusBarVisible(activity: Activity): Boolean {
        val flags = activity.window.attributes.flags
        return flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == 0
    }

    // 设置状态栏是否为浅色模式
    fun setStatusBarLightMode(activity: Activity, isLightMode: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = activity.window.decorView
            var systemUiVisibility = decorView.systemUiVisibility
            systemUiVisibility = if (isLightMode) {
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = systemUiVisibility
        }
    }

    // 判断状态栏是否为浅色模式
    fun isStatusBarLightMode(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = activity.window.decorView
            val systemUiVisibility = decorView.systemUiVisibility
            return systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR != 0
        }
        return false
    }


    // 设置状态栏颜色
    fun setStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }


    // 透明状态栏
    fun transparentStatusBar(activity: Activity) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    // 获取ActionBar高度
    fun getActionBarHeight(activity: Activity): Int {
        val typedValue = TypedValue()
        if (activity.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            return TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                activity.resources.displayMetrics
            )
        }
        return 0
    }


    // 获取导航栏高度
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getNavBarHeight(): Int {
        val resourceId =
            AppUtils.getApp().resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            AppUtils.getApp().resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    // 导航栏是否可见
    fun isNavBarVisible(activity: Activity): Boolean {
        val decorView: View = activity.window.decorView
        val insets = ViewCompat.getRootWindowInsets(decorView)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true
        } else {
            val navigationBarInsets = insets?.getInsets(WindowInsetsCompat.Type.navigationBars())
            (navigationBarInsets?.bottom ?: 0) > 0
        }
    }

    /**
     * 设置导航栏颜色
     */
    fun setNavBarColor(activity: Activity, color: Int) {
        if (isSupportNavBar()) {
            val window: Window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.navigationBarColor = color
        }
    }

    /**
     * 获取导航栏颜色
     */
    fun getNavBarColor(activity: Activity): Int {
        if (isSupportNavBar()) {
            val window: Window = activity.window
            return window.navigationBarColor
        }
        return Color.BLACK // 默认返回黑色
    }

    /**
     * 判断是否支持导航栏
     */
    fun isSupportNavBar(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP // 5.0及以上的系统支持导航栏

    /**
     * 设置导航栏是否为浅色模式
     */
    fun setNavBarLightMode(activity: Activity, isLightMode: Boolean) {
        if (isSupportNavBar() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView: View = activity.window.decorView
            var systemUiVisibility: Int = decorView.systemUiVisibility
            systemUiVisibility = if (isLightMode) {
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            decorView.systemUiVisibility = systemUiVisibility
        }
    }

    /**
     * 判断导航栏是否为浅色模式
     */
    fun isNavBarLightMode(activity: Activity): Boolean {
        if (isSupportNavBar() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView: View = activity.window.decorView
            val systemUiVisibility: Int = decorView.systemUiVisibility
            return systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR != 0
        }
        return false
    }

    /**
     * 透明导航栏
     */
    fun transparentNavBar(activity: Activity) {
        if (isSupportNavBar()) {
            val window: Window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    /**
     * 设置导航栏是否可见
     */
    fun setNavBarVisibility(activity: Activity, isVisible: Boolean) {
        if (isSupportNavBar() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val flags: Int = activity.window.decorView.systemUiVisibility
            activity.window.decorView.systemUiVisibility = if (isVisible) {
                flags and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
            } else {
                flags or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            }
        }
    }
}