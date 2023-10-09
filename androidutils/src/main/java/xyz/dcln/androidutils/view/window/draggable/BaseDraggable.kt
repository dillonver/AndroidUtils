package xyz.dcln.androidutils.view.window.draggable

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import xyz.dcln.androidutils.view.window.Floaty
import kotlin.math.abs

abstract class BaseDraggable : View.OnTouchListener {
    protected var window: Floaty? = null
        private set
    protected var decorView: View? = null
        private set
    private val mTempRect = Rect()
    protected var windowWidth = 0
        private set
    protected var windowHeight = 0
        private set
    private var mCurrentViewOnScreenX = 0
    private var mCurrentViewOnScreenY = 0
    protected var windowInvisibleWidth = 0
        private set
    protected var windowInvisibleHeight = 0
        private set

    @SuppressLint("ClickableViewAccessibility")
    fun start(window: Floaty) {
        this.window = window
        decorView = window.decorView
        decorView?.setOnTouchListener { v, event ->
            refreshLocationCoordinate()
            onTouch(v, event)
        }
        decorView?.post { refreshLocationCoordinate() }
    }

    fun refreshLocationCoordinate() {
        decorView?.let {
            it.getWindowVisibleDisplayFrame(mTempRect)
            windowWidth = mTempRect.width()
            windowHeight = mTempRect.height()
            val location = IntArray(2)
            it.getLocationOnScreen(location)
            mCurrentViewOnScreenX = location[0]
            mCurrentViewOnScreenY = location[1]
            windowInvisibleWidth = mTempRect.left
            windowInvisibleHeight = mTempRect.top
        }
    }

    fun onScreenOrientationChange() {
        decorView?.let { view ->
            val viewWidth = view.width
            val viewHeight = view.height
            val startX = mCurrentViewOnScreenX - windowInvisibleWidth
            val startY = mCurrentViewOnScreenY - windowInvisibleHeight
            val percentX = startX.toFloat() / windowWidth + viewWidth / 2f
            val percentY = startY.toFloat() / windowHeight + viewHeight / 2f
            window?.postDelayed({
                view.getWindowVisibleDisplayFrame(mTempRect)
                val x = (mTempRect.width() * percentX - viewWidth / 2f).toInt()
                val y = (mTempRect.height() * percentY - viewHeight / 2f).toInt()
                updateLocation(x, y)
                window!!.post { refreshLocationCoordinate() }
            }, 100)
        }
    }

    protected fun updateLocation(x: Int, y: Int) {
        window?.windowParams?.let { params ->
            if (params.x != x || params.y != y) {
                params.x = x
                params.y = y
                window!!.update()
            }
        }
    }

    protected fun isFingerMove(downX: Float, upX: Float, downY: Float, upY: Float) =
        Math.abs(downX - upX) >= minTouchDistance || Math.abs(downY - upY) >= minTouchDistance

    protected val minTouchDistance = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 1f,
        Resources.getSystem().displayMetrics
    )
}
