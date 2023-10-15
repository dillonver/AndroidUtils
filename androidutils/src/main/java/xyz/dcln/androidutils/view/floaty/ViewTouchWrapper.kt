package xyz.dcln.androidutils.view.floaty

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

internal class ViewTouchWrapper(
    private val mWindow: Floaty ,
    private val mListener: Floaty.OnTouchListener?
) : View.OnTouchListener {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        return mListener?.onTouch(mWindow, view, event) ?: false
    }
}