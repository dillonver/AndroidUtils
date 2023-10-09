package xyz.dcln.androidutils.utils

import android.app.Activity
import android.app.Application
import android.view.View
import xyz.dcln.androidutils.view.window.Floaty
import java.util.UUID
/**
object FloatyUtils{

    /**
     * 基于 Activity 创建一个 EasyWindow 实例
     */
    fun create(activity: Activity?): Floaty<*>? {
        return Floaty.with(activity)
    }

    /**
     * 基于全局创建一个 EasyWindow 实例，需要悬浮窗权限
     */
    fun create(application: Application?): Floaty<*>? {
        return Floaty.with(application)
    }


    fun getFloatyByTag(tag: String): FloatyUtils? {
        return Floaty.
    }

    fun cancelByTag(tag: String) {
        Floaty.cancelByTag(tag)
    }


    fun cancelAll() {
        for (weakRef in instances.values) {
            weakRef.get()?.hide()
        }
        instances.clear()
    }

    fun getContentView(tag: String): View? {
        return getFloatyByTag(tag)?.getContentView()
    }

    fun isShowing(tag: String): Boolean {
        return getFloatyByTag(tag)?.isAddedToWindow == true
    }
}


class FloatyUtils private constructor(
    private val context: Context,
    private val reuse: Boolean = false,
    val tag: String // Make tag an instance variable instead of static
) {

    private val mWindowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    private var mView: View? = null
    private var isAddedToWindow: Boolean = false

    private var dismissOnOutsideClick: Boolean = true
    private var isDraggable: Boolean = false

    private var lastX: Int = 0
    private var lastY: Int = 0

    private var displayDuration: Long? = null

    private var onWindowException: ((Exception) -> Unit)? = null
    private var onPermissionException: ((SecurityException) -> Unit)? = null

    private var onShow: ((FloatyUtils) -> Unit)? = null
    private var onHide: ((FloatyUtils) -> Unit)? = null


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

        //默认半透明0.3f
        setBackgroundDimAmount(0.3f)

        //默认外部点击关闭
        setDismissOnOutsideClick(true)

        //默认不穿透到悬浮窗下面
        setTouchThroughEnabled(false)
    }


    fun getFloatTag(): String {
        return tag
    }


    fun setContentView(newView: View, initView: View.() -> Unit = {}): FloatyUtils {
        if (isAddedToWindow) {
            mWindowManager.removeView(mView)
        }
        mView = newView.apply(initView)

        if (isAddedToWindow) {
            mWindowManager.addView(mView, mLayoutParams)
        }

        //默认是否允许拖动
        setDraggable(isDraggable)

        return this
    }

    fun setContentView(layoutResId: Int, initView: View.() -> Unit = {}): FloatyUtils {
        // Inflate a new view from the given layout resource ID and set it as content
        return setContentView(LayoutInflater.from(context).inflate(layoutResId, null), initView)
    }

    fun setLifecycleListener(
        onShow: ((FloatyUtils) -> Unit)? = null,
        onHide: ((FloatyUtils) -> Unit)? = null
    ): FloatyUtils = apply {
        this.onShow = onShow
        this.onHide = onHide
    }

    fun setPermissionExceptionCallback(callback: (SecurityException) -> Unit): FloatyUtils = apply {
        this.onPermissionException = callback
    }

    fun setAnimationStyle(animationStyle: Int = R.style.FloatyDefaultWindowStyle) =
        apply { mLayoutParams.windowAnimations = animationStyle }


    fun setWidth(width: Int = WindowManager.LayoutParams.WRAP_CONTENT) =
        apply { mLayoutParams.width = width }

    fun setHeight(height: Int = WindowManager.LayoutParams.WRAP_CONTENT) =
        apply { mLayoutParams.height = height }

    fun setGravity(gravity: Int) = apply { mLayoutParams.gravity = gravity }
    fun setXOffset(px: Int) = apply { mLayoutParams.x = px }
    fun setYOffset(px: Int) = apply { mLayoutParams.y = px }


    fun setBackgroundDimAmount(amount: Float) = apply {
        mLayoutParams.dimAmount = amount
        mLayoutParams.flags = mLayoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        if (isAddedToWindow) {
            mWindowManager.updateViewLayout(mView, mLayoutParams)
        }
    }

    fun setWindowFlags(flags: Int) = apply { mLayoutParams.flags = flags }

    fun setDisplayDuration(durationMillis: Long): FloatyUtils = apply {
        this.displayDuration = durationMillis
    }

    fun setDraggable(draggable: Boolean): FloatyUtils = apply {
        this.isDraggable = draggable
        setupTouchListener()
    }


    /**
     * 设置外部点击是否透传给下层UI元素。
     *
     * 当此值设置为true时，点击Floaty外部区域时，事件会被传递到下层的UI元素，
     * 在这种情况下，setDismissOnOutsideClick方法的设置将无效。
     *
     * @param through 如果为true，点击外部区域事件会传递到下层UI元素；如果为false，则不会。
     * @return 返回Floaty实例，便于链式调用。
     */
    fun setTouchThroughEnabled(through: Boolean) = apply {
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if (through) {
            mLayoutParams.flags = mLayoutParams.flags or flags
        } else {
            mLayoutParams.flags = mLayoutParams.flags and flags.inv()
        }

        if (isAddedToWindow) {
            mWindowManager.updateViewLayout(mView, mLayoutParams)
        }
    }

    /**
     * 设置点击Floaty外部区域是否会导致Floaty消失。
     *
     * 注意：当setTouchThroughEnabled设置为true时，此方法设置将无效，
     * 因为此时点击事件已经被传递给了下层UI元素(如需关闭Floaty，请手动处理)。
     *
     * @param dismissOnOutsideClick 如果为true，点击外部区域会导致Floaty消失；如果为false，则不会。
     * @return 返回Floaty实例，便于链式调用。
     */
    fun setDismissOnOutsideClick(dismissOnOutsideClick: Boolean): FloatyUtils = apply {
        this.dismissOnOutsideClick = dismissOnOutsideClick

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        LogUtils.i("setupTouchListener")
        mView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDraggable) {
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
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (dismissOnOutsideClick) {
                        val location = IntArray(2)
                        v.getLocationOnScreen(location)
                        val rect = Rect(
                            location[0],
                            location[1],
                            location[0] + v.width,
                            location[1] + v.height
                        )

                        if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                            hide()
                        }
                    }
                    true
                }

                else -> false
            }
        }
    }


    fun setWindowExceptionCallback(callback: (Exception) -> Unit): FloatyUtils = apply {
        this.onWindowException = callback
    }


    //慎用
    fun getFloatyLayoutParams(): WindowManager.LayoutParams {
        mLayoutParams.windowAnimations
        return this.mLayoutParams
    }

    fun getContentView(): View? {
        return this.mView
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




}
*/