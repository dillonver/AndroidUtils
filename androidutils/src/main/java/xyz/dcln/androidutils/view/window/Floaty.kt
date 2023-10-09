package xyz.dcln.androidutils.view.window

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.widget.FrameLayout
import android.widget.LinearLayout
import xyz.dcln.androidutils.R
import xyz.dcln.androidutils.view.window.ScreenOrientationMonitor.OnScreenOrientationCallback
import xyz.dcln.androidutils.view.window.draggable.BaseDraggable
import xyz.dcln.androidutils.view.window.draggable.MovingDraggable
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
class Floaty private constructor(
    private val context: Context,
    val tag: String
) : Runnable, OnScreenOrientationCallback {
    val handler = Handler(Looper.getMainLooper())

    /** 根布局  */
    private var mDecorView: ViewGroup?
    /**
     * 获取 WindowManager 对象（可能为空）
     */
    /** 悬浮窗  */
    private var windowManager: WindowManager?
    /**
     * 获取 WindowManager 参数集（可能为空）
     */
    /** 悬浮窗参数  */
    var windowParams: WindowManager.LayoutParams?

    /** 当前是否已经显示  */
    private var isShowing = false

    /** 悬浮窗显示时长  */
    private var mDuration = 0

    /** Toast 生命周期管理  */
    private var mLifecycle: ActivityWindowLifecycle? = null
    /**
     * 获取当前的拖拽规则对象（可能为空）
     */
    /** 自定义拖动处理  */
    private var draggable: BaseDraggable? = null

    /** 吐司显示和取消监听  */
    private var mListener: OnWindowLifecycle? = null

    /** 屏幕旋转监听  */
    private var mScreenOrientationMonitor: ScreenOrientationMonitor? = null

    /** 更新任务  */
    private val mUpdateRunnable = Runnable { update() }

    /**
     * 创建一个局部悬浮窗
     */
    constructor(activity: Activity, tag: String) : this(activity as Context, tag) {
        val window = activity.window
        val decorView = window.decorView
        val params = activity.window.attributes
        if (params.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != 0 || decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0) {
            // 如果当前 Activity 是全屏模式，那么需要添加这个标记，否则会导致 WindowManager 在某些机型上移动不到状态栏的位置上
            // 如果不想让状态栏显示的时候把 WindowManager 顶下来，可以添加 FLAG_LAYOUT_IN_SCREEN，但是会导致软键盘无法调整窗口位置
            addWindowFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 如果是 Android 9.0，则需要对刘海屏进行适配，否则也会导致 WindowManager 移动不到刘海屏的位置上面
            setLayoutInDisplayCutoutMode(params.layoutInDisplayCutoutMode)
        }
        if (params.systemUiVisibility != 0) {
            setSystemUiVisibility(params.systemUiVisibility)
        }
        if (decorView.systemUiVisibility != 0) {
            mDecorView?.systemUiVisibility = decorView.systemUiVisibility
        }

        // 跟随 Activity 的生命周期
        mLifecycle = ActivityWindowLifecycle(this, activity)
        // 注册 Activity 生命周期监听
        mLifecycle?.register()
    }

    /**
     * 创建一个全局悬浮窗
     */
    constructor(application: Application, tag: String) : this(application as Context, tag) {

        // 设置成全局的悬浮窗，注意需要先申请悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setWindowType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            setWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        }
    }

    init {
        mDecorView = WindowLayout(context)
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // 配置一些默认的参数
        windowParams = WindowManager.LayoutParams()
        windowParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        windowParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        windowParams?.format = PixelFormat.TRANSLUCENT
        windowParams?.packageName = context.packageName
        // 设置触摸外层布局（除 WindowManager 外的布局，默认是 WindowManager 显示的时候外层不可触摸）
        // 需要注意的是设置了 FLAG_NOT_TOUCH_MODAL 必须要设置 FLAG_NOT_FOCUSABLE，否则就会导致用户按返回键无效
        windowParams?.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

    }


    /**
     * 设置悬浮窗宽度
     */
    fun setWidth(width: Int): Floaty {
        windowParams!!.width = width
        if (mDecorView!!.childCount > 0) {
            val contentView = mDecorView!!.getChildAt(0)
            val layoutParams = contentView.layoutParams
            if (layoutParams != null && layoutParams.width != width) {
                layoutParams.width = width
                contentView.layoutParams = layoutParams
            }
        }
        postUpdate()
        return this
    }

    /**
     * 设置悬浮窗高度
     */
    fun setHeight(height: Int): Floaty {
        windowParams!!.height = height
        if (mDecorView!!.childCount > 0) {
            val contentView = mDecorView!!.getChildAt(0)
            val layoutParams = contentView.layoutParams
            if (layoutParams != null && layoutParams.height != height) {
                layoutParams.height = height
                contentView.layoutParams = layoutParams
            }
        }
        postUpdate()
        return this
    }

    /**
     * 设置悬浮窗显示的重心
     */
    fun setGravity(gravity: Int): Floaty {
        windowParams?.gravity = gravity
        postUpdate()
        post {
            draggable?.refreshLocationCoordinate()
        }
        return this
    }

    /**
     * 设置水平偏移量
     */
    fun setXOffset(px: Int): Floaty {
        windowParams?.x = px
        postUpdate()
        post {
            draggable?.refreshLocationCoordinate()
        }
        return this
    }

    /**
     * 设置垂直偏移量
     */
    fun setYOffset(px: Int): Floaty {
        windowParams?.y = px
        postUpdate()
        post {
            draggable?.refreshLocationCoordinate()
        }
        return this
    }

    /**
     * 设置悬浮窗外层是否可触摸
     */
    fun setOutsideTouchable(touchable: Boolean): Floaty {
        val flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if (touchable) {
            addWindowFlags(flags)
        } else {
            removeWindowFlags(flags)
        }
        postUpdate()
        return this
    }

    /**
     * 设置悬浮窗背景阴影强度
     *
     * @param amount        阴影强度值，填写 0 到 1 之间的值
     */
    fun setBackgroundDimAmount(amount: Float): Floaty {
        require(!(amount < 0 || amount > 1)) { "amount must be a value between 0 and 1" }
        windowParams!!.dimAmount = amount
        val flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        if (amount != 0f) {
            addWindowFlags(flags)
        } else {
            removeWindowFlags(flags)
        }
        postUpdate()
        return this
    }

    /**
     * 添加窗口标记
     */
    fun addWindowFlags(flags: Int): Floaty {
        windowParams?.flags = windowParams!!.flags or flags
        postUpdate()
        return this
    }

    /**
     * 移除窗口标记
     */
    fun removeWindowFlags(flags: Int): Floaty {
        windowParams?.flags = windowParams!!.flags and flags.inv()
        postUpdate()
        return this
    }

    /**
     * 设置窗口标记
     */
    fun setWindowFlags(flags: Int): Floaty {
        windowParams?.flags = flags
        postUpdate()
        return this
    }

    /**
     * 是否存在某个窗口标记
     */
    fun hasWindowFlags(flags: Int): Boolean {
        return windowParams!!.flags and flags != 0
    }

    /**
     * 设置悬浮窗的显示类型
     */
    fun setWindowType(type: Int): Floaty {
        windowParams?.type = type
        postUpdate()
        return this
    }

    /**
     * 设置动画样式
     */
    fun setAnimStyle(id: Int = R.style.FloatyDefaultWindowStyle): Floaty {
        windowParams?.windowAnimations = id
        postUpdate()
        return this
    }


    /**
     * 设置软键盘模式
     *
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED]：没有指定状态,系统会选择一个合适的状态或依赖于主题的设置
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED]：不会改变软键盘状态
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN]：当用户进入该窗口时，软键盘默认隐藏
     * [WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN]：当窗口获取焦点时，软键盘总是被隐藏
     * [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE]：当软键盘弹出时，窗口会调整大小
     * [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN]：当软键盘弹出时，窗口不需要调整大小，要确保输入焦点是可见的
     */
    fun setSoftInputMode(mode: Int): Floaty {
        windowParams?.softInputMode = mode
        // 如果设置了不能触摸，则擦除这个标记，否则会导致无法弹出输入法
        removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        postUpdate()
        return this
    }

    /**
     * 设置悬浮窗 Token
     */
    fun setWindowToken(token: IBinder?): Floaty {
        windowParams!!.token = token
        postUpdate()
        return this
    }

    /**
     * 设置悬浮窗透明度
     */
    fun setWindowAlpha(alpha: Float): Floaty {
        windowParams?.alpha = alpha
        postUpdate()
        return this
    }

    /**
     * 设置垂直间距
     */
    fun setVerticalMargin(verticalMargin: Float): Floaty {
        windowParams?.verticalMargin = verticalMargin
        postUpdate()
        return this
    }

    /**
     * 设置水平间距
     */
    fun setHorizontalMargin(horizontalMargin: Float): Floaty {
        windowParams?.horizontalMargin = horizontalMargin
        postUpdate()
        return this
    }

    /**
     * 设置位图格式
     */
    fun setBitmapFormat(format: Int): Floaty {
        windowParams?.format = format
        postUpdate()
        return this
    }

    /**
     * 设置状态栏的可见性
     */
    fun setSystemUiVisibility(systemUiVisibility: Int): Floaty {
        windowParams?.systemUiVisibility = systemUiVisibility
        postUpdate()
        return this
    }

    /**
     * 设置垂直权重
     */
    fun setVerticalWeight(verticalWeight: Float): Floaty {
        windowParams?.verticalWeight = verticalWeight
        postUpdate()
        return this
    }

    /**
     * 设置挖孔屏下的显示模式
     */
    fun setLayoutInDisplayCutoutMode(mode: Int): Floaty {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            windowParams?.layoutInDisplayCutoutMode = mode
            postUpdate()
        }
        return this
    }

    /**
     * 设置悬浮窗在哪个显示屏上显示
     */
    fun setPreferredDisplayModeId(id: Int): Floaty {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            windowParams?.preferredDisplayModeId = id
            postUpdate()
        }
        return this
    }

    /**
     * 设置悬浮窗标题
     */
    fun setWindowTitle(title: CharSequence?): Floaty {
        windowParams?.title = title
        postUpdate()
        return this
    }

    /**
     * 设置屏幕的亮度
     */
    fun setScreenBrightness(screenBrightness: Float): Floaty {
        windowParams?.screenBrightness = screenBrightness
        postUpdate()
        return this
    }

    /**
     * 设置按键的亮度
     */
    fun setButtonBrightness(buttonBrightness: Float): Floaty {
        windowParams?.buttonBrightness = buttonBrightness
        postUpdate()
        return this
    }

    /**
     * 设置悬浮窗的刷新率
     */
    fun setPreferredRefreshRate(preferredRefreshRate: Float): Floaty {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            windowParams?.preferredRefreshRate = preferredRefreshRate
            postUpdate()
        }
        return this
    }

    /**
     * 设置悬浮窗的颜色模式
     */
    fun setColorMode(colorMode: Int): Floaty {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowParams?.colorMode = colorMode
            postUpdate()
        }
        return this
    }

    /**
     * 设置悬浮窗高斯模糊半径大小（Android 12 才有的）
     */
    fun setBlurBehindRadius(blurBehindRadius: Int): Floaty {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            windowParams?.blurBehindRadius = blurBehindRadius
            addWindowFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            postUpdate()
        }
        return this
    }

    /**
     * 设置悬浮窗屏幕方向
     *
     * 自适应：[ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED]
     * 横屏：[ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE]
     * 竖屏：[ActivityInfo.SCREEN_ORIENTATION_PORTRAIT]
     */
    fun setScreenOrientation(orientation: Int): Floaty {
        windowParams?.screenOrientation = orientation
        postUpdate()
        return this
    }

    /**
     * 重新设置 WindowManager 参数集
     */
    fun setWindowParams(params: WindowManager.LayoutParams?): Floaty {
        windowParams = params
        postUpdate()
        return this
    }

    /**
     * 设置随意拖动
     */
    fun setDraggable(): Floaty {
        return setDraggable(MovingDraggable())
    }

    /**
     * 设置拖动规则
     */
    fun setDraggable(draggable: BaseDraggable?): Floaty {
        this.draggable = draggable
        if (draggable != null) {
            // 如果当前是否设置了不可触摸，如果是就擦除掉这个标记
            removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            // 如果当前是否设置了可移动窗口到屏幕之外，如果是就擦除这个标记
            removeWindowFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            if (isShowing) {
                update()
                draggable.start(this)
            }
        }
        if (mScreenOrientationMonitor == null) {
            mScreenOrientationMonitor = ScreenOrientationMonitor(context!!.resources.configuration)
        }
        mScreenOrientationMonitor!!.registerCallback(context!!, this)
        return this
    }

    /**
     * 限定显示时长
     */
    fun setDuration(duration: Int): Floaty {
        mDuration = duration
        if (isShowing && mDuration != 0) {
            removeCallbacks(this)
            postDelayed(this, mDuration.toLong())
        }
        return this
    }

    /**
     * 设置生命周期监听
     */
    fun setOnToastLifecycle(listener: OnWindowLifecycle?): Floaty {
        mListener = listener
        return this
    }


    /**
     * 设置内容布局
     */
    fun setContentView(id: Int, initView: View.() -> Unit = {}): Floaty {
        return setContentView(LayoutInflater.from(context).inflate(id, mDecorView, false), initView)
    }

    fun setContentView(newView: View, initView: View.() -> Unit = {}): Floaty {
        if (mDecorView!!.childCount > 0) {
            mDecorView!!.removeAllViews()
        }
        contentView = newView.apply(initView)
        mDecorView?.addView(contentView)
        val layoutParams = contentView?.layoutParams
        if (layoutParams is MarginLayoutParams) {
            // 清除 Margin，因为 WindowManager 没有这一属性可以设置，并且会跟根布局相冲突
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = 0
            layoutParams.leftMargin = 0
            layoutParams.rightMargin = 0
        }

        // 如果当前没有设置重心，就自动获取布局重心
        if (windowParams?.gravity == Gravity.NO_GRAVITY) {
            if (layoutParams is FrameLayout.LayoutParams) {
                val gravity = layoutParams.gravity
                if (gravity != FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY) {
                    windowParams?.gravity = gravity
                }
            } else if (layoutParams is LinearLayout.LayoutParams) {
                val gravity = layoutParams.gravity
                if (gravity != FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY) {
                    windowParams?.gravity = gravity
                }
            }
            if (windowParams?.gravity == Gravity.NO_GRAVITY) {
                // 默认重心是居中
                windowParams?.gravity = Gravity.CENTER
            }
        }
        if (layoutParams != null) {
            if (windowParams?.width == WindowManager.LayoutParams.WRAP_CONTENT &&
                windowParams?.height == WindowManager.LayoutParams.WRAP_CONTENT
            ) {
                // 如果当前 Dialog 的宽高设置了自适应，就以布局中设置的宽高为主
                windowParams?.width = layoutParams.width
                windowParams?.height = layoutParams.height
            } else {
                // 如果当前通过代码动态设置了宽高，则以动态设置的为主
                layoutParams.width = windowParams!!.width
                layoutParams.height = windowParams!!.height
            }
        }
        postUpdate()
        return this
    }

    /**
     * 将悬浮窗显示在某个 View 下方（和 PopupWindow 同名方法作用类似）
     *
     * @param anchorView            锚点 View
     * @param showGravity           显示重心
     * @param xOff                  水平偏移
     * @param yOff                  垂直偏移
     */
    @JvmOverloads
    fun showAsDropDown(
        anchorView: View,
        showGravity: Int = Gravity.BOTTOM,
        xOff: Int = 0,
        yOff: Int = 0
    ) {
        var showGravity = showGravity
        require(!(mDecorView!!.childCount == 0 || windowParams == null)) { "WindowParams and view cannot be empty" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // 适配布局反方向
            showGravity = Gravity.getAbsoluteGravity(
                showGravity,
                anchorView.resources.configuration.layoutDirection
            )
        }
        val anchorViewLocation = IntArray(2)
        anchorView.getLocationOnScreen(anchorViewLocation)
        val windowVisibleRect = Rect()
        anchorView.getWindowVisibleDisplayFrame(windowVisibleRect)
        windowParams?.gravity = Gravity.TOP or Gravity.START
        windowParams?.x = anchorViewLocation[0] - windowVisibleRect.left + xOff
        windowParams?.y = anchorViewLocation[1] - windowVisibleRect.top + yOff
        if (showGravity and Gravity.LEFT == Gravity.LEFT) {
            var rootViewWidth = mDecorView!!.width
            if (rootViewWidth == 0) {
                rootViewWidth = mDecorView!!.measuredWidth
            }
            if (rootViewWidth == 0) {
                mDecorView!!.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                rootViewWidth = mDecorView!!.measuredWidth
            }
            windowParams!!.x -= rootViewWidth
        } else if (showGravity and Gravity.RIGHT == Gravity.RIGHT) {
            windowParams!!.x += anchorView.width
        }
        if (showGravity and Gravity.TOP == Gravity.TOP) {
            var rootViewHeight = mDecorView!!.height
            if (rootViewHeight == 0) {
                rootViewHeight = mDecorView!!.measuredHeight
            }
            if (rootViewHeight == 0) {
                mDecorView!!.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                rootViewHeight = mDecorView!!.measuredHeight
            }
            windowParams!!.y -= rootViewHeight
        } else if (showGravity and Gravity.BOTTOM == Gravity.BOTTOM) {
            windowParams!!.y += anchorView.height
        }
        show()
    }

    /**
     * 显示悬浮窗
     */
    fun show() {
        require(!(mDecorView?.childCount == 0 || windowParams == null)) { "WindowParams and view cannot be empty" }

        // 如果当前已经显示则进行更新
        if (isShowing) {
            update()
            return
        }
        if (context is Activity) {
            val activity = context as Activity
            if (activity.isFinishing || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed) {
                return
            }
        }
        try {
            // 如果 View 已经被添加的情况下，就先把 View 移除掉
            if (mDecorView?.parent != null) {
                windowManager?.removeViewImmediate(mDecorView)
            }
            windowManager?.addView(mDecorView, windowParams)
            // 当前已经显示
            isShowing = true
            // 如果当前限定了显示时长
            if (mDuration != 0) {
                removeCallbacks(this)
                postDelayed(this, mDuration.toLong())
            }
            // 如果设置了拖拽规则
            draggable?.start(this)

            // 回调监听
            mListener?.onWindowShow(this)
        } catch (e: NullPointerException) {
            // 如果这个 View 对象被重复添加到 WindowManager 则会抛出异常
            // java.lang.IllegalStateException: View has already been added to the window manager.
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: BadTokenException) {
            e.printStackTrace()
        }
    }

    /**
     * 销毁悬浮窗
     */
    fun cancel() {
        if (!isShowing) {
            return
        }
        try {

            // 如果当前 WindowManager 没有附加这个 View 则会抛出异常
            // java.lang.IllegalArgumentException: View not attached to window manager
            windowManager?.removeViewImmediate(mDecorView)

            // 移除销毁任务
            removeCallbacks(this)

            // 回调监听
            mListener?.onWindowCancel(this)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            // 当前没有显示
            isShowing = false
        }
    }

    /**
     * 延迟更新悬浮窗
     */
    fun postUpdate() {
        if (!isShowing) {
            return
        }
        removeCallbacks(mUpdateRunnable)
        post(mUpdateRunnable)
    }

    /**
     * 更新悬浮窗
     */
    fun update() {
        if (!isShowing) {
            return
        }
        try {
            // 更新 WindowManger 的显示
            windowManager?.updateViewLayout(mDecorView, windowParams)
        } catch (e: IllegalArgumentException) {
            // 当 WindowManager 已经消失时调用会发生崩溃
            // IllegalArgumentException: View not attached to window manager
            e.printStackTrace()
        }
    }

    /**
     * 回收释放
     */
    fun recycle() {
        if (isShowing) {
            cancel()
        }
        mScreenOrientationMonitor?.unregisterCallback(context!!)
        mListener?.onWindowRecycle(this)
        // 反注册 Activity 生命周期
        mLifecycle?.unregister()
        mListener = null
        mDecorView = null
        windowManager = null
        windowParams = null
        mLifecycle = null
        draggable = null
        mScreenOrientationMonitor = null
        // 将当前实例从静态集合中移除
        instances.remove(tag)
    }

    val decorView: View?
        /**
         * 获取根布局（可能为空）
         */
        get() = mDecorView
    var contentView: View? = if (mDecorView!!.childCount == 0) {
        null
    } else mDecorView!!.getChildAt(0)

    /**
     * 根据 ViewId 获取 View
     */
    fun findViewById(id: Int): View? {
        return mDecorView?.findViewById(id)
    }


    /**
     * 延迟执行
     */
    fun post(runnable: Runnable?): Boolean {
        return postDelayed(runnable, 0)
    }

    /**
     * 延迟一段时间执行
     */
    fun postDelayed(runnable: Runnable?, delayMillis: Long): Boolean {
        var delayMillis = delayMillis
        if (delayMillis < 0) {
            delayMillis = 0
        }
        return postAtTime(runnable, SystemClock.uptimeMillis() + delayMillis)
    }

    /**
     * 在指定的时间执行
     */
    fun postAtTime(runnable: Runnable?, uptimeMillis: Long): Boolean {
        // 发送和这个 WindowManager 相关的消息回调
        return handler.postAtTime(runnable!!, this, uptimeMillis)
    }

    /**
     * 移除消息回调
     */
    fun removeCallbacks(runnable: Runnable?) {
        handler.removeCallbacks(runnable!!)
    }

    fun removeCallbacksAndMessages() {
        handler.removeCallbacksAndMessages(this)
    }

    /**
     * 设置点击事件
     */
    fun setOnClickListener(listener: OnClickListener): Floaty {
        return setOnClickListener(mDecorView, listener)
    }

    fun setOnClickListener(id: Int, listener: OnClickListener): Floaty {
        return setOnClickListener(findViewById(id), listener)
    }

    fun setOnClickListener(view: View?, listener: OnClickListener): Floaty {
        // 如果当前是否设置了不可触摸，如果是就擦除掉
        removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        view?.isClickable = true
        view?.setOnClickListener(ViewClickWrapper(this, listener))
        return this
    }

    /**
     * 设置长按事件
     */
    fun setOnLongClickListener(listener: OnLongClickListener): Floaty {
        return setOnLongClickListener(mDecorView, listener)
    }

    fun setOnLongClickListener(id: Int, listener: OnLongClickListener): Floaty {
        return setOnLongClickListener(findViewById(id), listener)
    }

    fun setOnLongClickListener(view: View?, listener: OnLongClickListener): Floaty {
        // 如果当前是否设置了不可触摸，如果是就擦除掉
        removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        view?.isClickable = true
        view?.setOnLongClickListener(ViewLongClickWrapper(this, listener))
        return this
    }

    /**
     * 设置触摸事件
     */
    fun setOnTouchListener(listener: OnTouchListener): Floaty {
        return setOnTouchListener(mDecorView, listener)
    }

    fun setOnTouchListener(id: Int, listener: OnTouchListener): Floaty {
        return setOnTouchListener(findViewById(id), listener)
    }

    private fun setOnTouchListener(view: View?, listener: OnTouchListener): Floaty {
        // 当前是否设置了不可触摸，如果是就擦除掉
        removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        view?.isEnabled = true
        view?.setOnTouchListener(ViewTouchWrapper(this, listener))
        return this
    }

    /**
     * [Runnable]
     */
    override fun run() {
        cancel()
    }

    /**
     * [ScreenOrientationMonitor.OnScreenOrientationCallback]
     */
    override fun onScreenOrientationChange(newOrientation: Int) {
        if (!isShowing) {
            return
        }
        draggable?.onScreenOrientationChange()
    }

    /**
     * View 的点击事件监听
     */
    interface OnClickListener {
        fun onClick(window: Floaty?, view: View)
    }

    /**
     * View 的长按事件监听
     */
    interface OnLongClickListener {
        fun onLongClick(window: Floaty?, view: View): Boolean
    }

    /**
     * View 的触摸事件监听
     */
    interface OnTouchListener {
        fun onTouch(window: Floaty?, view: View, event: MotionEvent?): Boolean
    }

    /**
     * 窗口生命周期监听
     */
    interface OnWindowLifecycle {
        /**
         * 显示回调
         */
        fun onWindowShow(window: Floaty?) {}

        /**
         * 消失回调
         */
        fun onWindowCancel(window: Floaty?) {}

        /**
         * 回收回调
         */
        fun onWindowRecycle(window: Floaty?) {}
    }

    companion object {

        private val instances: ConcurrentHashMap<String, WeakReference<Floaty>> =
            ConcurrentHashMap()

        /**
         * 基于 Activity 创建一个 Floaty 实例
         */
        fun create(activity: Activity, tag: String? = null, init: Floaty.() -> Unit): Floaty {
            return baseCreate(activity, tag, init)
        }

        /**
         * 基于全局创建一个 Floaty 实例，需要悬浮窗权限
         */
        fun create(application: Application, tag: String? = null, init: Floaty.() -> Unit): Floaty {
            return baseCreate(application, tag, init)
        }


        private fun baseCreate(
            context: Context,
            tag: String? = null,
            init: Floaty.() -> Unit
        ): Floaty {
            var floatyTag = tag ?: generateUniqueTag()
            val existingFloaty = getFloatyByTag(floatyTag)
            return if (existingFloaty == null) {
                if (instances.containsKey(floatyTag)) {
                    floatyTag = generateUniqueTag()
                }
                val newFloaty = Floaty(context, floatyTag).apply(init)
                instances[floatyTag] = WeakReference(newFloaty)
                newFloaty
            } else {
                // Reuse the existing Floaty instance
                existingFloaty.apply(init)
            }
        }

        private fun generateUniqueTag(): String {
            return UUID.randomUUID().toString()
        }

        fun getFloatyByTag(tag: String): Floaty? {
            return instances[tag]?.get()
        }

        fun cancelByTag(tag: String) {
            getFloatyByTag(tag)?.recycle()
        }


        fun cancelAll() {
            for (weakRef in instances.values) {
                weakRef.get()?.recycle()
            }
            instances.clear()
        }

        fun getContentView(tag: String): View? {
            return getFloatyByTag(tag)?.contentView
        }

        fun isShowing(tag: String): Boolean {
            return getFloatyByTag(tag)?.isShowing == true
        }

    }
}