package xyz.dcln.androidutils.utils


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import xyz.dcln.androidutils.AndroidUtils
import xyz.dcln.androidutils.utils.BusUtils.receive
import xyz.dcln.androidutils.utils.BusUtils.sendEventSticky
import java.io.Serializable
import java.lang.ref.WeakReference

object ActivityUtils {
    /**
     * Used for synchronizing the top Activity stack
     */
    private val topActivityLock = Any()

    /**
     * Holds the weak reference of the top Activity
     */
    private var topActivityReference: WeakReference<Activity>? = null

    private const val TAG_TOP_ACTIVITY_CHANGED = "tag_top_activity_changed"

    /**
     * Activity cache
     * managed using LinkedHashMap to ensure order and cache size
     */
    private val activityCache: LinkedHashMap<Int, WeakReference<Activity>> =
        LinkedHashMap(10, 0.75f, true)


    internal fun init() {
        val application = AndroidUtils.getApplication()
        // Register Activity lifecycle callbacks for managing Activity cache
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                synchronized(topActivityLock) {
                    topActivityReference = WeakReference(activity)
                    topActivityReference?.get()
                        ?.let { sendEventSticky(TAG_TOP_ACTIVITY_CHANGED, it) }
                }
                addActivity(activity)
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                topActivityReference = WeakReference(activity)
                topActivityReference?.get()?.let { sendEventSticky(TAG_TOP_ACTIVITY_CHANGED, it) }
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                removeActivity(activity)

            }
        })
    }

    fun LifecycleOwner.addTopActivityChangeListener(block: suspend CoroutineScope.(value: Activity) -> Unit): Job {
        return receive(
            tags = arrayOf(TAG_TOP_ACTIVITY_CHANGED),
            lifeEvent = Lifecycle.Event.ON_DESTROY,
            sticky = true,
            block = block
        )
    }


    /**
     * Add Activity to the cache
     * @param activity The Activity instance to be added
     */
    private fun addActivity(activity: Activity) {
        synchronized(activityCache) {
            if (!activity.isFinishing && !activity.isDestroyed) {
                activityCache[activity.hashCode()] = WeakReference(activity)
            }
        }
    }

    /**
     * Remove Activity from the cache
     * @param activity The Activity instance to be removed
     */
    private fun removeActivity(activity: Activity) {
        synchronized(activityCache) {
            activityCache.entries.removeIf { entry ->
                entry.value.get() == null || entry.value.get() == activity
            }
        }
    }

    /**
     * Check if the Activity is valid (not destroyed and not finished)
     * @param activity The Activity instance to be checked
     * @return `true` if the Activity is valid, `false` otherwise
     */
    fun isActivityValid(activity: Activity?): Boolean {
        return activity != null && !activity.isFinishing && !activity.isDestroyed
    }


    /**
     * Get the current top Activity
     * @return The current top Activity instance, or `null` if there's no available Activity
     */
    fun getTopActivity(): Activity? {
        synchronized(topActivityLock) {
            val activity = topActivityReference?.get()
            return if (isActivityValid(activity)) activity else null
        }
    }

    /**
     * Determine that at least 1 activity of the specified type exists in the stack
     * @param clazz Specifies the Activity type
     * @return true exists, false does not exist
     */
    fun isActivityExist(clazz: Class<out Activity>): Boolean {
        synchronized(activityCache) {
            removeNullReferences()
            activityCache.values.forEach { weakRef ->
                weakRef.get()?.let { activity ->
                    if (!activity.isFinishing && !activity.isDestroyed && activity.javaClass == clazz) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Determines whether the incoming activity is the current stack top activity.
     *
     * @param activity The Activity instance to judge.
     * @return true if the incoming activity is a stack top activity, otherwise false.
     */
    fun isTopActivity(activity: Activity): Boolean {
        return activity === getTopActivity()
    }

    /**
     * Back to Desktop
     */
    fun backToHome() {
        getTopActivity()?.let {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory(Intent.CATEGORY_HOME)
            }
            it.startActivity(homeIntent)
        }
    }


    /**
     * Initiating Activities
     * @param intent Data to be passed when starting the activity
     * @param params The parameters to be passed when starting the Activity, passed in as a Pair array
     */
    inline fun <reified T : Activity> startActivity(
        intent: Intent? = null,
        flags: Int? = null,
        vararg params: Pair<String, Any?> = emptyArray()
    ) {
        getTopActivity()?.let { activity ->
            val startIntent = intent ?: Intent(activity, T::class.java)
            startIntent.setClass(activity, T::class.java)
            flags?.let { startIntent.flags = it }
            params.forEach { pair ->
                val (key, value) = pair
                when (value) {
                    is Int -> startIntent.putExtra(key, value)
                    is Float -> startIntent.putExtra(key, value)
                    is Double -> startIntent.putExtra(key, value)
                    is Long -> startIntent.putExtra(key, value)
                    is String -> startIntent.putExtra(key, value)
                    is Boolean -> startIntent.putExtra(key, value)
                    is Bundle -> startIntent.putExtra(key, value)
                    is Parcelable -> startIntent.putExtra(key, value)
                    is Serializable -> startIntent.putExtra(key, value)
                    else -> throw IllegalArgumentException("Unsupported value type: ${value?.javaClass?.name}")
                }
            }
            activity.startActivity(startIntent)
        }
    }

    fun startActivity(
        activity: Activity,
        clazz: Class<out Activity>,
        flags: Int? = null
    ) {
        val startIntent = Intent(activity, clazz)
        flags?.let { startIntent.flags = it }
        activity.startActivity(startIntent)
    }

    /**
     * Initiating Activities
     * @param clazz Class of Activity started by
     * @param intent Data to be passed when starting the activity
     * @param params The parameters to be passed when starting the Activity, passed in as a Pair array
     */
    fun startActivity(
        clazz: Class<out Activity>,
        intent: Intent? = null,
        flags: Int? = null,
        vararg params: Pair<String, Any?> = emptyArray()
    ) {
        getTopActivity()?.let { activity ->
            val startIntent = intent ?: Intent(activity, clazz)
            startIntent.setClass(activity, clazz)
            flags?.let { startIntent.flags = it }
            params.forEach { pair ->
                val (key, value) = pair
                when (value) {
                    is Int -> startIntent.putExtra(key, value)
                    is Float -> startIntent.putExtra(key, value)
                    is Double -> startIntent.putExtra(key, value)
                    is Long -> startIntent.putExtra(key, value)
                    is String -> startIntent.putExtra(key, value)
                    is Boolean -> startIntent.putExtra(key, value)
                    is Bundle -> startIntent.putExtra(key, value)
                    is Parcelable -> startIntent.putExtra(key, value)
                    is Serializable -> startIntent.putExtra(key, value)
                    else -> throw IllegalArgumentException("Unsupported value type: ${value?.javaClass?.name}")
                }
            }
            activity.startActivity(startIntent)
        }
    }

    /**
     * End the specified type of Activity
     * @param cls Activity type
     */
    fun <T : Activity> finishActivity(cls: Class<T>) {
        synchronized(activityCache) {
            val iterator = activityCache.values.iterator()
            while (iterator.hasNext()) {
                val activity = iterator.next().get()
                if (activity != null && activity::class.java == cls && isActivityValid(activity)) {
                    activity.finish()
                    iterator.remove()
                }
            }
        }
    }


    /**
     * End of Activities
     * @param clazz The activity type specified, do not pass or null will end all activities
     * @param keepCurrent Whether to keep the current activity from ending, the default is false
     */
    fun finishActivities(
        clazz: Class<out Activity>? = null,
        keepCurrent: Boolean = false
    ) {
        val currentActivity = if (keepCurrent) getTopActivity() else null
        synchronized(activityCache) {
            val iterator = activityCache.values.iterator()
            while (iterator.hasNext()) {
                val weakActivity = iterator.next()
                val activity = weakActivity.get()
                if (activity != null && activity != currentActivity) {
                    if (clazz == null || activity.javaClass == clazz) {
                        if (isActivityValid(activity)) {
                            activity.finish()
                            iterator.remove()
                        }
                    }
                }
            }
        }
    }

    /**
     * Start an activity with a return value
     * @param cls Class object of the activity to start
     * @param requestCode
     */
    fun startActivityForResult(
        cls: Class<out Activity>,
        requestCode: Int,
        flags: Int? = null
    ) {
        getTopActivity()?.let { activity ->
            val intent = Intent(activity, cls)
            flags?.let { intent.flags = it }
            activity.startActivityForResult(intent, requestCode)
        }
    }


    /**
     * Get all Activity instances that have been created
     * @return a list of activity instances that have been created
     */
    fun getActivityList(): List<Activity> {
        synchronized(activityCache) {
            val activityList = mutableListOf<Activity>()
            activityCache.values.forEach { weakRef ->
                weakRef.get()?.let { activity ->
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        activityList.add(activity)
                    }
                }
            }
            return activityList
        }
    }


    /**
     * End Eligible Activities
     *
     * @param clazz The type of activity to end, no type restriction when null
     * @param predicate condition, activities that meet the condition will be closed
     */
    fun finishActivitiesInRange(
        clazz: Class<out Activity>? = null,
        predicate: (Activity) -> Boolean
    ) {
        synchronized(activityCache) {
            val iterator = activityCache.values.iterator()
            while (iterator.hasNext()) {
                val activity = iterator.next().get()
                if (activity != null && (clazz == null || clazz == activity.javaClass) && predicate(
                        activity
                    ) && isActivityValid(activity)
                ) {
                    activity.finish()
                    iterator.remove()
                }
            }
        }
    }

    private fun removeNullReferences() {
        synchronized(activityCache) {
            activityCache.entries.removeIf { entry -> entry.value.get() == null }
        }
    }

    fun Activity.requestNoTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    /**
     * Toggle the orientation lock of the activity.
     *
     * @param lockOrientation True if the orientation should be locked, false otherwise.
     */
    @SuppressLint("SourceLockedOrientationActivity")
    fun Activity.toggleOrientationLock(lockOrientation: Boolean) {
        if (lockOrientation) {
            // Lock the orientation if not locked yet.
            if (!isOrientationLocked()) {
                // Get the display of the activity.
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Use the new method in Android 11 and higher.
                    this.display
                } else {
                    // Use the old method for backward compatibility.
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                // Get the device rotation.
                val rotation = display?.rotation
                requestedOrientation = when (rotation) {
                    // Portrait.
                    0, 2 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    // Landscape.
                    else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        } else {
            // Unlock the orientation if locked.
            if (isOrientationLocked()) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    /**
     * Determine if the orientation of the activity is locked.
     *
     * @return True if the activity orientation is locked, false otherwise.
     */
    fun Activity.isOrientationLocked(): Boolean {
        return requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    /**
     * Adds a LifecycleObserver to listen to the Activity's lifecycle events
     */
    fun AppCompatActivity.addLifecycleObserver(
        onCreate: (() -> Unit)? = null,
        onStart: (() -> Unit)? = null,
        onResume: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null,
        onStop: (() -> Unit)? = null,
        onDestroy: (() -> Unit)? = null
    ) {
        this.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                // 在生命周期处于 Created 状态时调用
                onCreate?.invoke()

            }

            override fun onStart(owner: LifecycleOwner) {
                // 在生命周期处于 Started 状态时调用
                onStart?.invoke()

            }

            override fun onResume(owner: LifecycleOwner) {
                // 在生命周期处于 Resumed 状态时调用
                onResume?.invoke()

            }

            override fun onPause(owner: LifecycleOwner) {
                // 在生命周期处于 Paused 状态时调用
                onPause?.invoke()
            }

            override fun onStop(owner: LifecycleOwner) {
                // 在生命周期处于 Stopped 状态时调用
                onStop?.invoke()

            }

            override fun onDestroy(owner: LifecycleOwner) {
                // 在生命周期处于 Destroyed 状态时调用
                onDestroy?.invoke()

            }
        })
    }

    /**
     * 从Intent中获取值的通用方法
     * @param intent 传入的Intent
     * @param key 参数的键
     * @return 返回对应键的值
     */
    inline fun <reified T> getValueFromIntent(intent: Intent, key: String): T? {
        return IntentUtils.getValueFromIntent(intent, key)
    }


    /**
     * 是否沉浸式状态栏
     */
    fun isImmersiveMode(activity: Activity): Boolean {
        val window = activity.window
        val systemUiVisibility = window.decorView.systemUiVisibility

        val hasFullscreenFlag = (systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0
        val hasStableFlag = (systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0
        val isStatusBarTransparent = window.statusBarColor == Color.TRANSPARENT

        val hasDrawsSystemBarBackgroundsFlag =
            (window.attributes.flags and WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) != 0
        val noTranslucentStatusFlag =
            (window.attributes.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) == 0

        return hasFullscreenFlag && hasStableFlag && isStatusBarTransparent &&
                hasDrawsSystemBarBackgroundsFlag && noTranslucentStatusFlag
    }
}
