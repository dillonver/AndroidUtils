package xyz.dcln.androidutils.view.window

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Build
import android.os.Bundle

internal class ActivityWindowLifecycle(
    private var mWindow: Floaty?,
    private var mActivity: Activity?
) : ActivityLifecycleCallbacks {
    /**
     * 注册监听
     */
    fun register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mActivity?.registerActivityLifecycleCallbacks(this)
        } else {
            mActivity?.application?.registerActivityLifecycleCallbacks(this)
        }
    }

    /**
     * 取消监听
     */
    fun unregister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mActivity?.unregisterActivityLifecycleCallbacks(this)
        } else {
            mActivity?.application?.unregisterActivityLifecycleCallbacks(this)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {
        // 一定要在 onPaused 方法中销毁掉，如果放在 onDestroyed 方法中还是有一定几率会导致内存泄露
        if (mActivity === activity && mActivity!!.isFinishing) {
            mWindow?.recycle()
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (mActivity === activity) {
            mActivity = null
            mWindow?.recycle()
            mWindow = null
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

}