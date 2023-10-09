package xyz.dcln.androidutils.view.window

import android.view.View

internal class ViewClickWrapper(
    private val mWindow: Floaty ,
    private val mListener: Floaty.OnClickListener?
) : View.OnClickListener {
    override fun onClick(view: View) {
        if (mListener == null) {
            return
        }
        mListener.onClick(mWindow, view)
    }
}