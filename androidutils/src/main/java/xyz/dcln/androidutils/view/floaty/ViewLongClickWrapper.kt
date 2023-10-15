package xyz.dcln.androidutils.view.floaty

import android.view.View

internal class ViewLongClickWrapper(
    private val mWindow: Floaty ,
    private val mListener: Floaty.OnLongClickListener?
) : View.OnLongClickListener {
    override fun onLongClick(view: View): Boolean {
        return mListener?.onLongClick(mWindow, view) ?: false
    }
}
