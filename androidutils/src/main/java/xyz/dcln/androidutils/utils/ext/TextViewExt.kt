package xyz.dcln.androidutils.utils.ext

import android.widget.TextView


/**
 * 将字符串截断并设置到 TextView 控件中显示
 * @param text 要截断和设置到 TextView 的字符串
 * @param maxLength 最大长度限制
 * @param truncationMarker 截断标记，默认为 "..."
 */
fun TextView.truncateAndSet(text: String?, maxLength: Int, truncationMarker: String = "...") {
    if (!text.isNullOrEmpty()) {
        val truncatedText =
            if (text.length > maxLength) text.substring(0, maxLength) + truncationMarker else text
        this.text = truncatedText
    }
}

// 获取TextView的文本内容。
fun TextView.textString(trim: Boolean = true): String {
    val content = this.text.toString()
    return if (trim) content.trim() else content
}

// 判断text内容是否空串
fun TextView.isEmpty(): Boolean = this.text.toString().isEmpty()

// 判断内容是否空白（不含任何可见字符，例如只含空格或换行符）
fun TextView.isBlank(): Boolean = this.text.toString().isBlank()

// 获取内容长度
fun TextView.length(trim: Boolean = false): Int {
    return if (trim) this.text.toString().trim().length else this.text.length
}

// 获取真实长度，去除空格或换行等字符
fun TextView.realLength(): Int {
    return this.text.toString().filterNot { it.isWhitespace() }.length
}
