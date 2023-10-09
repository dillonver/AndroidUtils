package xyz.dcln.androidutils.view.window.draggable

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout

class SpringDraggable @JvmOverloads constructor(
    /** 回弹的方向  */
    private val mOrientation: Int = ORIENTATION_HORIZONTAL
) : BaseDraggable() {
    /** 手指按下的坐标  */
    private var mViewDownX = 0f
    private var mViewDownY = 0f

    /** 触摸移动标记  */
    private var mTouchMoving = false

    init {
        when (mOrientation) {
            ORIENTATION_HORIZONTAL, ORIENTATION_VERTICAL -> {}
            else -> throw IllegalArgumentException("You cannot pass in directions other than horizontal or vertical")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val rawMoveX: Float
        val rawMoveY: Float
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录按下的位置（相对 View 的坐标）
                mViewDownX = event.x
                mViewDownY = event.y
                mTouchMoving = false
            }

            MotionEvent.ACTION_MOVE -> {
                // 记录移动的位置（相对屏幕的坐标）
                rawMoveX = event.rawX - windowInvisibleWidth
                rawMoveY = event.rawY - windowInvisibleHeight
                var newX = rawMoveX - mViewDownX
                var newY = rawMoveY - mViewDownY
                if (newX < 0) {
                    newX = 0f
                }
                if (newY < 0) {
                    newY = 0f
                }

                // 更新移动的位置
                updateLocation(newX.toInt(), newY.toInt())
                if (!mTouchMoving && isFingerMove(mViewDownX, event.x, mViewDownY, event.y)) {
                    // 如果用户移动了手指，那么就拦截本次触摸事件，从而不让点击事件生效
                    mTouchMoving = true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 记录移动的位置（相对屏幕的坐标）
                rawMoveX = event.rawX - windowInvisibleWidth
                rawMoveY = event.rawY - windowInvisibleHeight
                when (mOrientation) {
                    ORIENTATION_HORIZONTAL -> {
                        var startX = rawMoveX - mViewDownX
                        // 如果在最左边向左移动就会产生负数，这里需要处理掉，因为坐标没有负数这一说
                        if (startX < 0) {
                            startX = 0f
                        }
                        var endX: Float
                        // 获取当前屏幕的宽度
                        val screenWidth = windowWidth
                        if (rawMoveX < screenWidth / 2f) {
                            // 回弹到屏幕左边
                            endX = 0f
                        } else {
                            // 回弹到屏幕右边（注意减去 View 宽度，因为 View 坐标系是从屏幕左上角开始算的）
                            endX = (screenWidth - v.width).toFloat()
                            // 如果在最右边向右移动就会产生负数，这里需要处理掉，因为坐标没有负数这一说
                            if (endX < 0) {
                                endX = 0f
                            }
                        }
                        val y = rawMoveY - mViewDownY
                        // 从移动的点回弹到边界上
                        startHorizontalAnimation(startX, endX, y)
                    }

                    ORIENTATION_VERTICAL -> {
                        val x = rawMoveX - mViewDownX
                        var startY = rawMoveY - mViewDownY
                        // 如果在最顶部向上移动就会产生负数，这里需要处理掉，因为坐标没有负数这一说
                        if (startY < 0) {
                            startY = 0f
                        }
                        var endY: Float
                        // 获取当前屏幕的高度
                        val screenHeight = windowHeight
                        if (rawMoveY < screenHeight / 2f) {
                            // 回弹到屏幕顶部
                            endY = 0f
                        } else {
                            // 回弹到屏幕底部（注意减去 View 高度，因为 View 坐标系是从屏幕左上角开始算的）
                            endY = (screenHeight - v.height).toFloat()
                            // 如果在最底部向下移动就会产生负数，这里需要处理掉，因为坐标没有负数这一说
                            if (endY < 0) {
                                endY = 0f
                            }
                        }
                        // 从移动的点回弹到边界上
                        startVerticalAnimation(x, startY, endY)
                    }

                    else -> {}
                }
                return try {
                    mTouchMoving
                } finally {
                    // 重置触摸移动标记
                    mTouchMoving = false
                }
            }

            else -> {}
        }
        return false
    }

    /**
     * 执行水平回弹动画
     *
     * @param startX        X 轴起点坐标
     * @param endX          X 轴终点坐标
     * @param y             Y 轴坐标
     * @param duration      动画时长
     */
    protected fun startHorizontalAnimation(
        startX: Float,
        endX: Float,
        y: Float,
        duration: Long = calculateAnimationDuration(startX, endX)
    ) {
        val animator = ValueAnimator.ofFloat(startX, endX)
        animator.duration = duration
        animator.addUpdateListener { animation: ValueAnimator ->
            updateLocation(
                animation.animatedValue as Int,
                y.toInt()
            )
        }
        animator.start()
    }

    /**
     * 执行垂直回弹动画
     *
     * @param x             X 轴坐标
     * @param startY        Y 轴起点坐标
     * @param endY          Y 轴终点坐标
     * @param duration      动画时长
     */
    protected fun startVerticalAnimation(
        x: Float,
        startY: Float,
        endY: Float,
        duration: Long = calculateAnimationDuration(startY, endY)
    ) {
        val animator = ValueAnimator.ofFloat(startY, endY)
        animator.duration = duration
        animator.addUpdateListener { animation: ValueAnimator ->
            updateLocation(
                x.toInt(),
                animation.animatedValue as Int
            )
        }
        animator.start()
    }

    /**
     * 根据距离算出动画的时间
     *
     * @param startCoordinate               起始坐标
     * @param endCoordinate                 结束坐标
     */
    fun calculateAnimationDuration(startCoordinate: Float, endCoordinate: Float): Long {
        // 为什么要根据距离来算出动画的时间？
        // 因为不那么做，如果悬浮球回弹的距离比较短的情况，加上 ValueAnimator 动画更新回调次数比较多的情况下
        // 会导致自动回弹的时候出现轻微卡顿，但这其实不是卡顿，而是一次滑动的距离太短的导致的
        var animationDuration = (Math.abs(endCoordinate - startCoordinate) / 2f).toLong()
        if (animationDuration > 800) {
            animationDuration = 800
        }
        return animationDuration
    }

    companion object {
        /** 水平方向回弹  */
        const val ORIENTATION_HORIZONTAL = LinearLayout.HORIZONTAL

        /** 垂直方向回弹  */
        const val ORIENTATION_VERTICAL = LinearLayout.VERTICAL
    }
}