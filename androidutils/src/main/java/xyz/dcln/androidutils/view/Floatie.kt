package xyz.dcln.androidutils.view

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import xyz.dcln.androidutils.R
import xyz.dcln.androidutils.utils.CoroutineUtils
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * `Floatie` - A flexible and easy-to-use floating window manager for Android.
 *
 * Features:
 * 1. Easy to create and manage floating windows with customizable content.
 * 2. Support for dragging floating windows across the screen.
 * 3. Lifecycle-aware: automatically removes itself when the associated `Activity` is destroyed.
 * 4. Extensive customization options for window size, position, appearance, and behavior.
 * 5. Supports handling multiple floating window instances, identified by unique tags.
 *
 * Example Usage:
 * ```kotlin
 * val myFloatie = Floatie.create(context = this) {
 *     setContentView(R.layout.my_text_view) {
 *         if (this is TextView) {
 *             text = "Hello, Floatie!"
 *         }
 *     }
 *     setXOffset(100)
 *     setYOffset(200)
 *     setDraggable(true)
 * }
 * myFloatie.show()
 * ```
 *
 * @param context The application context used to create the floating window.
 * @param tag A unique tag to identify and manage multiple floating window instances.
 * @param reuse Whether to reuse existing instances with the same tag.
 *
 * @author Dillon
 * @version 1.0
 * @since 2023-08-15
 */
@SuppressLint("ObsoleteSdkInt")
class Floatie private constructor(
    private val context: Context,
    private val reuse: Boolean = false,
    val tag: String // Make tag an instance variable instead of static
) {

    private val mWindowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    private var mView: View? = null
    private var isAddedToWindow: Boolean = false

    private var isDraggable: Boolean = false
    private var lastX: Int = 0
    private var lastY: Int = 0

    private var displayDuration: Long? = null

    private var onWindowException: ((Exception) -> Unit)? = null
    private var onPermissionException: ((SecurityException) -> Unit)? = null

    private var onShow: ((Floatie) -> Unit)? = null
    private var onHide: ((Floatie) -> Unit)? = null


    init {
        if (context is AppCompatActivity) {
            context.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    hide()
                    if (!reuse) {
                        instances.remove(tag)
                    }
                    context.lifecycle.removeObserver(this)

                }
            })
        }
        mLayoutParams.apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE
                else {
                    if (context is Application) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
                    }
                }

            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
        }

        //默认外部不能点击
        setOutsideTouchable(false)
        //默认半透明0.3f
        setBackgroundDimAmount(0.3f)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clear() {
        onShow = null
        onHide = null
        onPermissionException = null
        onWindowException = null
        mView?.setOnTouchListener(null)
    }

    fun getFloatTag(): String {
        return tag
    }


    fun setContentView(newView: View, initView: View.() -> Unit = {}): Floatie {
        if (isAddedToWindow) {
            mWindowManager.removeView(mView)
        }

        mView = newView.apply(initView)

        if (isAddedToWindow) {
            mWindowManager.addView(mView, mLayoutParams)
        }
        return this
    }

    fun setContentView(layoutResId: Int, initView: View.() -> Unit = {}): Floatie {
        // Inflate a new view from the given layout resource ID and set it as content
        return setContentView(LayoutInflater.from(context).inflate(layoutResId, null), initView)
    }

    fun setLifecycleListener(
        onShow: ((Floatie) -> Unit)? = null,
        onHide: ((Floatie) -> Unit)? = null
    ): Floatie = apply {
        this.onShow = onShow
        this.onHide = onHide
    }

    fun setPermissionExceptionCallback(callback: (SecurityException) -> Unit): Floatie = apply {
        this.onPermissionException = callback
    }

    fun setAnimationStyle(animationStyle: Int = R.style.FloatieCustomPopWindowStyle) =
        apply { mLayoutParams.windowAnimations = animationStyle }


    fun setWidth(width: Int = WindowManager.LayoutParams.WRAP_CONTENT) =
        apply { mLayoutParams.width = width }

    fun setHeight(height: Int = WindowManager.LayoutParams.WRAP_CONTENT) =
        apply { mLayoutParams.height = height }

    fun setGravity(gravity: Int) = apply { mLayoutParams.gravity = gravity }
    fun setXOffset(px: Int) = apply { mLayoutParams.x = px }
    fun setYOffset(px: Int) = apply { mLayoutParams.y = px }

    fun setOutsideTouchable(touchable: Boolean) = apply {
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if (touchable) {
            mLayoutParams.flags = mLayoutParams.flags or flags
        } else {
            mLayoutParams.flags = mLayoutParams.flags and flags.inv()
        }

        if (isAddedToWindow) {
            mWindowManager.updateViewLayout(mView, mLayoutParams)
        }
    }


    fun setBackgroundDimAmount(amount: Float) = apply {
        mLayoutParams.dimAmount = amount
        mLayoutParams.flags = mLayoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        if (isAddedToWindow) {
            mWindowManager.updateViewLayout(mView, mLayoutParams)
        }
    }

    fun setWindowFlags(flags: Int) = apply { mLayoutParams.flags = flags }

    fun setDisplayDuration(durationMillis: Long): Floatie = apply {
        this.displayDuration = durationMillis
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setDraggable(draggable: Boolean): Floatie = apply {
        this.isDraggable = draggable
        if (draggable) {
            setupTouchListener()
        } else {
            mView?.setOnTouchListener(null)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        mView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    true // 表示这个触摸事件已经被消费了
                }

                MotionEvent.ACTION_MOVE -> {
                    val x = event.rawX.toInt()
                    val y = event.rawY.toInt()
                    val dx = x - lastX
                    val dy = y - lastY

                    mLayoutParams.x += dx
                    mLayoutParams.y += dy

                    mWindowManager.updateViewLayout(v, mLayoutParams)
                    lastX = x
                    lastY = y
                    true // 表示这个触摸事件已经被消费了
                }

                else -> false // 其他情况，我们返回 false 来表示这个触摸事件没有被消费
            }
        }
    }


    fun setWindowExceptionCallback(callback: (Exception) -> Unit): Floatie = apply {
        this.onWindowException = callback
    }


    //慎用
    fun getFloatieLayoutParams(): WindowManager.LayoutParams {
        mLayoutParams.windowAnimations
        return this.mLayoutParams
    }

    fun show() {
        // Before trying to show the window, we check for the necessary permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            context is Application &&
            !Settings.canDrawOverlays(context)
        ) {
            onPermissionException?.invoke(
                SecurityException("Permission required: ACTION_MANAGE_OVERLAY_PERMISSION")
            )
            return
        }

        // Proceed to show the window as normal
        try {
            if (!isAddedToWindow) {
                mView?.let { view ->
                    mWindowManager.addView(view, mLayoutParams)
                    isAddedToWindow = true
                    onShow?.let { it(this) }
                    displayDuration?.let { duration ->
                        CoroutineUtils.launchOnUI(duration) { hide() }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle the BadTokenException
            onWindowException?.invoke(e)
        }
    }


    fun isShowing(): Boolean {
        return isAddedToWindow
    }

    fun hide() {
        mView?.let { view ->
            if (isAddedToWindow) {
                mWindowManager.removeView(view)
                isAddedToWindow = false
                onHide?.let { it(this) }
                if (!reuse) {
                    instances.remove(tag)
                }
            }
        }

        //clear()
    }


    companion object {
        private val instances: ConcurrentHashMap<String, WeakReference<Floatie>> =
            ConcurrentHashMap()

        fun create(
            context: Context,
            tag: String? = null,
            reuse: Boolean = false,
            init: Floatie.() -> Unit
        ): Floatie {
            var floatieTag = tag ?: generateUniqueTag()
            val existingFloatie = instances[floatieTag]?.get()
            return if (!reuse || existingFloatie == null) {
                if (instances.containsKey(floatieTag)) {
                    floatieTag = generateUniqueTag()
                }
                val newFloatie = Floatie(context, reuse, floatieTag).apply(init)
                instances[floatieTag] = WeakReference(newFloatie)
                newFloatie
            } else {
                // Reuse the existing Floatie instance
                existingFloatie.apply(init)
            }
        }

        private fun generateUniqueTag(): String {
            return UUID.randomUUID().toString()
        }


        fun isShowing(tag: String): Boolean {
            return instances[tag]?.get()?.isAddedToWindow == true
        }

        fun cancelAll() {
            for (weakRef in instances.values) {
                weakRef.get()?.hide()
            }
            instances.clear()
        }

        fun cancelByTag(tag: String) {
            instances[tag]?.get()?.let {
                it.hide()
                instances.remove(tag)
            }
        }

    }

}
