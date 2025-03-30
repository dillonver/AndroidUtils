package xyz.dcln.androidutils.utils.ext

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout

import android.view.inputmethod.InputMethodManager


// 扩展函数：显示软键盘
fun Activity.showKeyboard(view: View) {
    // 获取输入法管理器
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    // 请求焦点
    view.requestFocus()
    // 显示软键盘
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

// 扩展函数：隐藏软键盘
fun Activity.hideKeyboard() {
    // 获取当前焦点视图，如果没有焦点则创建一个新的视图
    val view = currentFocus ?: View(this)
    // 获取输入法管理器
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    // 隐藏软键盘
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

// 扩展函数：添加软键盘可见性监听器
fun AppCompatActivity.addKeyboardVisibilityListener(onKeyboardVisibilityChanged: (Boolean) -> Unit) {
    // 获取根视图
    val rootView = findViewById<View>(android.R.id.content)

    // 在布局完成后设置窗口插入监听器
    rootView.doOnLayout {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            // 判断软键盘是否可见
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            // 回调键盘可见性变化
            onKeyboardVisibilityChanged(imeVisible)
            insets
        }
    }
}
