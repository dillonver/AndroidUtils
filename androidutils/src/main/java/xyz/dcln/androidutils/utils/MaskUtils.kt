package xyz.dcln.androidutils.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.FrameLayout
import androidx.core.view.isVisible
import xyz.dcln.androidutils.utils.ActivityUtils
import xyz.dcln.androidutils.utils.BarUtils
import xyz.dcln.androidutils.utils.LogUtils
import java.lang.ref.WeakReference

object MaskUtils {

    private var maskView: MaskView? = null
    private var maskContainer: WeakReference<FrameLayout?> = WeakReference(null)
    private var isUpdating = false

    /**
     * 更新遮罩
     */
    fun updateMask(
        activity: Activity? = ActivityUtils.getTopActivity(),
        holeView: View?,
        maskColor: Int = Color.BLACK,
        alpha: Float = 0.5f,
        paddingDp: Int = 0,
        cornerRadius: Float = 10f,
        overlayViews: List<OverlayViewData> = emptyList(),
    ) {
        if (!ActivityUtils.isActivityValid(activity)) {
            LogUtils.e("activity == null")
            return
        }
        if (holeView == null || !holeView.isVisible) {
            LogUtils.e("MaskHelper", "holeView == null || !holeView.isVisible")
            return
        }

        if (isUpdating) {
            return
        }
        isUpdating = true
        holeView.post {
            val container = maskContainer.get()
            if (container == null) {
                initializeMask(
                    activity,
                    holeView,
                    maskColor,
                    alpha,
                    paddingDp,
                    cornerRadius,
                    overlayViews,
                )
            } else {
                updateMaskView(
                    activity,
                    holeView,
                    maskColor,
                    alpha,
                    paddingDp,
                    cornerRadius,
                    overlayViews,
                )
            }
            isUpdating = false
        }

    }

    /**
     * 初始化遮罩
     */
    private fun initializeMask(
        activity: Activity?,
        holeView: View,
        maskColor: Int,
        alpha: Float,
        paddingDp: Int,
        cornerRadius: Float,
        overlayViews: List<OverlayViewData>,
    ) {
        if (activity == null || !ActivityUtils.isActivityValid(activity)) {
            LogUtils.e("activity == null")
            return
        }
        val rootView =
            activity.window?.decorView?.findViewById<ViewGroup>(android.R.id.content) ?: return

        val location = IntArray(2)
        getViewLocation(holeView, location, activity)
        val holeRect = RectF(
            (location[0] - paddingDp.dp).toFloat(),
            (location[1] - paddingDp.dp).toFloat(),
            (location[0] + holeView.width + paddingDp.dp).toFloat(),
            (location[1] + holeView.height + paddingDp.dp).toFloat()
        )

        val actualCornerRadius =
            if (cornerRadius == 0f) getCornerRadiusFromView(holeView) else cornerRadius

        maskView = MaskView(activity).apply {
            setHoleRect(holeRect, actualCornerRadius)
            setMaskColorAndAlpha(maskColor, alpha)
        }

        val newMaskContainer = FrameLayout(activity).apply {
            addView(
                maskView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
        maskContainer = WeakReference(newMaskContainer)

        val originHoleRect = RectF(
            (location[0]).toFloat(),
            (location[1]).toFloat(),
            (location[0] + holeView.width).toFloat(),
            (location[1] + holeView.height).toFloat()
        )
        addOverlayViewsToContainer(holeView=holeView,holeRect=originHoleRect, overlayViews=overlayViews)

        rootView.addView(newMaskContainer)
    }

    /**
     * 更新遮罩视图
     */
    private fun updateMaskView(
        activity: Activity?,
        holeView: View,
        maskColor: Int,
        alpha: Float,
        paddingDp: Int,
        cornerRadius: Float,
        overlayViews: List<OverlayViewData>,
    ) {
        if (activity == null || !ActivityUtils.isActivityValid(activity)) {
            LogUtils.e("activity == null")
            return
        }
        val location = IntArray(2)
        getViewLocation(holeView, location, activity)
        val holeRect = RectF(
            (location[0] - paddingDp.dp).toFloat(),
            (location[1] - paddingDp.dp).toFloat(),
            (location[0] + holeView.width + paddingDp.dp).toFloat(),
            (location[1] + holeView.height + paddingDp.dp).toFloat()
        )

        val actualCornerRadius =
            if (cornerRadius == 0f) getCornerRadiusFromView(holeView) else cornerRadius

        maskView?.apply {
            setHoleRect(holeRect, actualCornerRadius)
            setMaskColorAndAlpha(maskColor, alpha)
            clearClickableRects()
        }

        maskContainer.get()?.let { container ->
            // 清除现有的覆盖视图及其动画
            for (i in 0 until container.childCount) {
                val child = container.getChildAt(i)
                if (child != maskView) {
                    child.clearAnimation()
                }
            }
            container.removeAllViews()

            // 重新添加遮罩视图
            container.addView(
                maskView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
            val originHoleRect = RectF(
                (location[0]).toFloat(),
                (location[1]).toFloat(),
                (location[0] + holeView.width).toFloat(),
                (location[1] + holeView.height).toFloat()
            )
            // 添加新的覆盖视图
            addOverlayViewsToContainer(holeView=holeView,holeRect=originHoleRect, overlayViews=overlayViews)
        }
    }

    /**
     * 获取视图在屏幕中的位置
     */
    private fun getViewLocation(view: View, location: IntArray, activity: Activity) {
        view.getLocationInWindow(location)
        if (!isImmersiveStatusBar(activity)) {
            location[1] -= BarUtils.getStatusBarHeight()
        }
    }

    /**
     * 是否沉浸式状态栏
     */
    private fun isImmersiveStatusBar(activity: Activity): Boolean {
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

    /**
     * 将覆盖视图添加到容器中
     */
    private fun addOverlayViewsToContainer(
        holeView: View,
        holeRect: RectF,
        overlayViews: List<OverlayViewData>,
    ) {
        overlayViews.forEach { overlayViewData ->
            overlayViewData.view?.let { overlayView ->
                try {
                    val overlayParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        when (overlayViewData.gravity) {
                            Gravity.TOP or Gravity.START -> {
                                leftMargin = (holeRect.left + overlayViewData.offsetX).toInt()
                                topMargin =
                                    (holeRect.top - overlayView.height + overlayViewData.offsetY).toInt()
                            }

                            Gravity.TOP or Gravity.END -> {
                                leftMargin =
                                    (holeRect.right - overlayView.width + overlayViewData.offsetX).toInt()
                                topMargin =
                                    (holeRect.top - overlayView.height + overlayViewData.offsetY).toInt()
                            }

                            Gravity.BOTTOM or Gravity.START -> {
                                leftMargin = (holeRect.left + overlayViewData.offsetX).toInt()
                                topMargin = (holeRect.bottom + overlayViewData.offsetY).toInt()
                            }

                            Gravity.BOTTOM or Gravity.END -> {
                                leftMargin =
                                    (holeRect.right - overlayView.width + overlayViewData.offsetX).toInt()
                                topMargin = (holeRect.bottom + overlayViewData.offsetY).toInt()
                            }

                            Gravity.TOP -> {
                                leftMargin =
                                    (holeRect.left + holeView.width / 2 - overlayView.width / 2 + overlayViewData.offsetX).toInt()
                                topMargin =
                                    (holeRect.top - overlayView.height + overlayViewData.offsetY).toInt()
                            }

                            Gravity.BOTTOM -> {
                                leftMargin =
                                    (holeRect.left + holeView.width / 2 - overlayView.width / 2 + overlayViewData.offsetX).toInt()
                                topMargin = (holeRect.bottom + overlayViewData.offsetY).toInt()
                            }

                            Gravity.START -> {
                                leftMargin =
                                    (holeRect.left - overlayView.width + overlayViewData.offsetX).toInt()
                                topMargin =
                                    (holeRect.top + holeView.height / 2 - overlayView.height / 2 + overlayViewData.offsetY).toInt()
                            }

                            Gravity.END -> {
                                leftMargin = (holeRect.right + overlayViewData.offsetX).toInt()
                                topMargin =
                                    (holeRect.top + holeView.height / 2 - overlayView.height / 2 + overlayViewData.offsetY).toInt()
                            }

                            Gravity.CENTER -> {
                                leftMargin =
                                    (holeRect.left + holeView.width / 2 - overlayView.width / 2 + overlayViewData.offsetX).toInt()
                                topMargin =
                                    (holeRect.top + holeView.height / 2 - overlayView.height / 2 + overlayViewData.offsetY).toInt()
                            }

                            else -> {
                                leftMargin = (holeRect.left + overlayViewData.offsetX).toInt()
                                topMargin = (holeRect.top + overlayViewData.offsetY).toInt()
                            }
                        }
                    }

                    overlayView.isClickable = true

                    val parent = overlayView.parent as? ViewGroup
                    parent?.removeView(overlayView)

                    maskContainer.get()?.addView(overlayView, overlayParams)
                    overlayViewData.animation?.let { anim ->
                        overlayView.startAnimation(anim)
                    }

                    overlayView.post {
                        val overlayLocation = IntArray(2)
                        overlayView.getLocationInWindow(overlayLocation)
                        val overlayRect = RectF(
                            overlayLocation[0].toFloat(),
                            overlayLocation[1].toFloat(),
                            (overlayLocation[0] + overlayView.width).toFloat(),
                            (overlayLocation[1] + overlayView.height).toFloat()
                        )
                        maskView?.addClickableRect(overlayRect)
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
            }
        }
    }


    /**
     * 隐藏遮罩
     */
    fun hideMask(activity: Activity? = ActivityUtils.getTopActivity()) {
        maskContainer.get()?.let {
            val rootView =
                activity?.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)
            rootView?.removeView(it)
            maskContainer.clear()
            maskView = null
        }
    }

    /**
     * 从视图中获取圆角半径
     */
    private fun getCornerRadiusFromView(view: View): Float {
        return when (val background = view.background) {
            is GradientDrawable -> {
                val radii = background.cornerRadii
                if (radii != null && radii.isNotEmpty()) radii[0] else background.cornerRadius
            }

            else -> 0f
        }
    }

    private val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private class MaskView(context: Context) : View(context) {

        private var holeRect: RectF? = null
        private val paint = Paint().apply {
            color = Color.BLACK
            alpha = 128
            isAntiAlias = true
        }
        private var cornerRadius: Float = 20f
        private val clickableRects: MutableList<RectF> = mutableListOf()

        /**
         * 设置遮罩孔矩形和圆角半径
         */
        fun setHoleRect(rect: RectF, cornerRadius: Float) {
            holeRect = rect
            this.cornerRadius = cornerRadius
            invalidate()
        }

        /**
         * 设置遮罩颜色和透明度
         */
        fun setMaskColorAndAlpha(color: Int, alpha: Float) {
            paint.color = color
            paint.alpha = (alpha * 255).toInt()
            invalidate()
        }

        /**
         * 添加可点击的矩形区域
         */
        fun addClickableRect(rect: RectF) {
            clickableRects.add(rect)
        }

        /**
         * 清除所有可点击的矩形区域
         */
        fun clearClickableRects() {
            clickableRects.clear()
        }


        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            holeRect?.let {
                val path = Path().apply {
                    addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
                    addRoundRect(it, cornerRadius, cornerRadius, Path.Direction.CCW)
                }
                canvas.drawPath(path, paint)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            clickableRects.forEach {
                if (it.contains(event.x, event.y)) {
                    return false
                }
            }
            holeRect?.let {
                if (it.contains(event.x, event.y)) {
                    return false
                }
            }
            return true
        }
    }

    data class OverlayViewData(
        val view: View?,
        val offsetX: Int = 0,
        val offsetY: Int = 0,
        val gravity: Int = Gravity.NO_GRAVITY,
        val animation: Animation? = null,
    )


}


