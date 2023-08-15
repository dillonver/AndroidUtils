package xyz.dcln.androidutils.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import xyz.dcln.androidutils.utils.CoroutineUtils

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
    var tag: String = "default",
    private val reuse: Boolean = false
) {

    private val mWindowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    private var mView: View? = null
    private var isAddedToWindow: Boolean = false

    private var isDraggable: Boolean = false
    private var lastX: Int = 0
    private var lastY: Int = 0

    private var enterAnimation: Animation? = null
    private var exitAnimation: Animation? = null

    private var displayDuration: Long? = null

    init {
        mLayoutParams.apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE
                else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
        }

        if (context is AppCompatActivity) {
            context.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    hide()
                }
            })
        }
    }

    fun setContentView(newView: View, initView: View.() -> Unit = {}): Floatie {
        // Set the new view and apply the initialization function to it
        mView = newView.apply(initView)
        return this
    }

    fun setContentView(layoutResId: Int, initView: View.() -> Unit = {}): Floatie {
        // Inflate a new view from the given layout resource ID and set it as content
        return setContentView(LayoutInflater.from(context).inflate(layoutResId, null), initView)
    }


    fun updateView(newView: View, initView: View.() -> Unit = {}): Floatie {
        // Set the new view and apply the initialization function to it
        mView = newView.apply(initView)

        // If the FloatWindow instance is currently added to the window, update the view in-place
        if (isAddedToWindow) {
            mWindowManager.updateViewLayout(mView, mLayoutParams)
        }

        return this
    }

    fun updateView(layoutResId: Int, initView: View.() -> Unit = {}): Floatie {
        // Inflate a new view from the given layout resource ID and update
        return updateView(LayoutInflater.from(context).inflate(layoutResId, null), initView)
    }

    fun setEnterAnimation(animation: Animation): Floatie = apply {
        this.enterAnimation = animation
    }

    fun setExitAnimation(animation: Animation): Floatie = apply {
        this.exitAnimation = animation
    }

    fun withTag(newTag: String) = apply {
        // Remove the current instance from map
        instances.remove(this.tag)
        // Update the tag
        this.tag = newTag
        // Put the updated instance back into map
        instances[newTag] = this
    }

    fun setWidth(width: Int = WindowManager.LayoutParams.WRAP_CONTENT) =
        apply { mLayoutParams.width = width }

    fun setHeight(height: Int = WindowManager.LayoutParams.WRAP_CONTENT) =
        apply { mLayoutParams.height = height }

    fun setGravity(gravity: Int) = apply { mLayoutParams.gravity = gravity }
    fun setXOffset(px: Int) = apply { mLayoutParams.x = px }
    fun setYOffset(px: Int) = apply { mLayoutParams.y = px }

    fun setOutsideTouchable(touchable: Boolean) = apply {
        mLayoutParams.flags =
            mLayoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL.takeIf { touchable }
                ?: 0)
    }

    fun setBackgroundDimAmount(amount: Float) = apply { mLayoutParams.dimAmount = amount }
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
                }
            }
            true
        }
    }

    fun reset() {
        // Remove the view from window manager if it is currently added
        hide()

        // Reset the layout params to default
        mLayoutParams.apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.NO_GRAVITY
            x = 0
            y = 0
            dimAmount = 0f
        }
        lastX = 0
        lastY = 0
        enterAnimation = null
        exitAnimation = null
        displayDuration = null

        // Clear the view
        mView = null

        // Reset the flag
        isAddedToWindow = false
    }

    fun show() {
        if (!isAddedToWindow) {
            mView?.let { view ->
                mWindowManager.addView(view, mLayoutParams)
                enterAnimation?.let { view.startAnimation(it) }
                isAddedToWindow = true
                displayDuration?.let { duration ->
                    CoroutineUtils.launchOnUI(duration) { hide() }
                }
            }
        }
    }

    fun hide() {
        if (isAddedToWindow) {
            mView?.let { view ->
                exitAnimation?.let { view.startAnimation(it) }
                mWindowManager.removeView(view)
                isAddedToWindow = false
            }
        }
    }


    companion object {
        private val instances = mutableMapOf<String, Floatie>()

        fun create(
            context: Context,
            tag: String = "default",
            reuse: Boolean = false,
            init: Floatie.() -> Unit
        ): Floatie {
            return if (reuse) {
                // If reuse is true, get or create a FloatWindow instance associated with the given tag.
                instances.getOrPut(tag) {
                    Floatie(context, tag, true).apply(init)
                }
            } else {
                // If reuse is false, always create a new FloatWindow instance.
                Floatie(context, tag, false).apply(init)
            }
        }

        fun cancelAll() {
            for (window in instances.values) {
                window.hide()
            }
            instances.clear()
        }

        fun cancelByTag(tag: String) {
            instances[tag]?.let {
                it.hide()
                instances.remove(tag)
            }
        }
    }
}
