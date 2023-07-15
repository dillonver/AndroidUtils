package xyz.dcln.androidutils.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Created by dcl on 2023/7/7.
 */
object KeyboardUtils {
    /**
     * 显示软键盘
     */
    fun showSoftInput(view: View) {
        view.requestFocus()
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 隐藏软键盘
     */
    fun hideSoftInput(activity: Activity) {
        val view = activity.currentFocus ?: return
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 切换键盘显示与否状态
     */
    fun toggleSoftInput(activity: Activity) {
        val view = activity.currentFocus ?: return
        if (isSoftInputVisible(activity)) {
            hideSoftInput(activity)
        } else {
            showSoftInput(view)
        }
    }

    /**
     * 判断软键盘是否可见
     */
    fun isSoftInputVisible(activity: Activity): Boolean {
        val rootView = activity.window.decorView.rootView
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height
        val keyboardHeight = screenHeight - rect.bottom
        return keyboardHeight > screenHeight * 0.15
    }

    /**
     * 注册软键盘改变监听器
     */
    fun registerSoftInputChangedListener(
        activity: Activity,
        listener: (Boolean) -> Unit
    ) {
        val rootView = activity.window.decorView.rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            listener.invoke(isSoftInputVisible(activity))
        }
    }

    /**
     * 注销软键盘改变监听器
     */
    fun unregisterSoftInputChangedListener(
        activity: Activity,
        listener: (Boolean) -> Unit
    ) {
        val rootView = activity.window.decorView.rootView
        rootView.viewTreeObserver.removeOnGlobalLayoutListener {
            listener.invoke(isSoftInputVisible(activity))
        }
    }

    fun toggleKeyboardType(activity: Activity,editText: EditText) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: return

        // 判断当前键盘类型
        val curInputType = editText.inputType
        val numericKeyboardFlags =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        val isNumericKeyboard = (curInputType and numericKeyboardFlags) != 0

        // 切换键盘类型
        val newInputType = if (isNumericKeyboard) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        } else {
            InputType.TYPE_CLASS_NUMBER
        }

        // 更新输入框的键盘类型
        editText.inputType = newInputType

        // 强制显示软键盘
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun toggleKeyboardType(activity: Activity, editText: EditText, inputType: Int) {// InputType.TYPE_CLASS_NUMBER（数字键盘）、InputType.TYPE_CLASS_TEXT（字母键盘）
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // 更新输入框的键盘类型
        editText.inputType = inputType

        // 强制显示软键盘
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun getCurrentFocusedEditText(activity: Activity): EditText? {
        val view = activity.window.decorView
        val focusedView = view.findFocus()

        if (focusedView is EditText && focusedView.isFocused) {
            return focusedView
        }

        return null
    }

    fun Activity.hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus

        currentFocusView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}