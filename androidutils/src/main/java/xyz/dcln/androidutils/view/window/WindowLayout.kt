package xyz.dcln.androidutils.view.window

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class WindowLayout : FrameLayout {
    /** 触摸事件监听  */
    private var mOnTouchListener: OnTouchListener? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 为什么要那么写？有人反馈给子 View 设置 OnClickListener 后，父 View 的 OnTouchListener 收不到事件
        // 经过排查发现：父 View 在 dispatchTouchEvent 方法中直接将触摸事件派发给了子 View 的 onTouchEvent 方法
        // 从而导致父 View.OnTouchListener 收不到该事件，解决方案是重写 View 的触摸规则，让父 View 的触摸监听优于子 View 的点击事件
        return if (mOnTouchListener != null && mOnTouchListener!!.onTouch(this, ev)) {
            true
        } else super.dispatchTouchEvent(ev)
    }

    override fun setOnTouchListener(l: OnTouchListener) {
        //super.setOnTouchListener(l);
        mOnTouchListener = l
    }
}