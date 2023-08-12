package xyz.dcln.androidutils.utils.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.EditText
import android.widget.TextView


/**
 * 获取EditText的文本内容。
 *
 * @param trim 是否需要对获取到的文本进行trim操作（默认为true）
 * @return 返回EditText的文本内容。如果trim为true（默认值），则返回的字符串将去除前导和尾随的空白。
 */
fun EditText.textString(trim: Boolean = true): String {
    val content = this.text.toString()
    return if (trim) content.trim() else content
}

/**
 * 获取EditText的文本内容长度。
 *
 * @param trim 是否需要对获取到的文本进行trim操作（默认为false）
 * @return 返回EditText的文本内容长度。如果trim为true，则计算trim后的字符串长度；否则直接返回字符串长度。
 */
fun EditText.length(trim: Boolean = false): Int {
    return if (trim) this.text.toString().trim().length else this.text.length
}

/**
 * 获取EditText的文本内容长度，去除空格或换行等字符。
 *
 * @return 返回EditText的文本内容长度，已排除空格、换行符、制表符等空白字符。
 */
fun EditText.realLength(): Int {
    return this.text.toString().filterNot { it.isWhitespace() }.length
}

// 判断text内容是否空串
fun EditText.isEmpty(): Boolean = this.text.toString().isEmpty()

//  判断内容是否空白（不含任何可见字符，例如只含空格或换行符）
fun EditText.isBlank(): Boolean = this.text.toString().isBlank()