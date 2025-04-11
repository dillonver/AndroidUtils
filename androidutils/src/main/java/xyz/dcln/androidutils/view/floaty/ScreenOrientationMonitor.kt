package xyz.dcln.androidutils.view.floaty

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration

internal class ScreenOrientationMonitor(configuration: Configuration) : ComponentCallbacks {
    /** 当前屏幕的方向  */
    private var mScreenOrientation: Int = configuration.orientation

    /** 屏幕旋转回调  */
    private var mCallback: OnScreenOrientationCallback? = null


    /**
     * 注册监听
     */
    fun registerCallback(context: Context, callback: OnScreenOrientationCallback?) {
        context.applicationContext.registerComponentCallbacks(this)
        mCallback = callback
    }

    /**
     * 取消监听
     */
    fun unregisterCallback(context: Context) {
        context.applicationContext.unregisterComponentCallbacks(this)
        mCallback = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (mScreenOrientation == newConfig.orientation) {
            return
        }
        mScreenOrientation = newConfig.orientation
        mCallback?.onScreenOrientationChange(mScreenOrientation)
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
    }

    /**
     * 屏幕方向监听器
     */
    internal interface OnScreenOrientationCallback {
        /**
         * 监听屏幕旋转了
         *
         * @param newOrientation         最新的屏幕方向
         */
        fun onScreenOrientationChange(newOrientation: Int)
    }
}