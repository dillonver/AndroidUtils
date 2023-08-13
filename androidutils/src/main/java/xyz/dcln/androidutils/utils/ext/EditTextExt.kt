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
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import xyz.dcln.androidutils.utils.LogUtils
import java.util.regex.Pattern


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


/**
 * 设置软键盘的“Enter”键的行为，并在点击时调用回调。
 *
 * 注意: 该方法仅适用于单行的EditText。
 *
 * @param imeAction 指定软键盘“Enter”键的行为。常用的值包括:
 * - EditorInfo.IME_ACTION_NONE
 * - EditorInfo.IME_ACTION_GO
 * - EditorInfo.IME_ACTION_SEARCH
 * - EditorInfo.IME_ACTION_SEND
 * - EditorInfo.IME_ACTION_NEXT
 * - EditorInfo.IME_ACTION_DONE
 * 默认值为 EditorInfo.IME_ACTION_DONE。
 * @param onActionClick 当软键盘上指定的操作被点击时调用的回调函数。
 */
fun EditText.setImeAction(
    imeAction: Int = EditorInfo.IME_ACTION_DONE,
    onActionClick: () -> Unit
) {
    // 检查是否是多行输入
    if (this.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0) {
        LogUtils.e("EditTextExtension", "setImeAction is not applicable for multi-line EditText.")
        return
    }
    // 设置输入类型为给定的imeAction
    this.imeOptions = imeAction
    this.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == imeAction) {
            onActionClick.invoke()
            true
        } else {
            false
        }
    }
}

///**
// * 限制EditText只能输入数字。
// * @param isMasked 若为true，则文本以类似密码的蒙版形式显示。
// * @param allowDecimal 若为true，则允许输入小数点，但小数点不能在首位且只能输入一次。
// * @param onValidInput 当输入内容有效时调用此回调，并传入有效字符。
// * @param onInvalidInput 当输入内容无效时调用此回调，并传入无效字符。
// */
//fun EditText.limitNumbers(
//    isMasked: Boolean = false,
//    allowDecimal: Boolean = true,
//    onValidInput: ((valid: String, position: Int) -> Unit)? = null,
//    onInvalidInput: ((invalid: String, position: Int) -> Unit)? = null
//) {
//    filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
//        for (i in start until end) {
//            val char = source[i]
//            // 当允许小数点但已存在一个小数点时，拒绝新的小数点
//            if (allowDecimal && char == '.' && dest.toString().contains('.')) {
//                onInvalidInput?.invoke(char.toString(), dstart)
//                return@InputFilter ""
//            }
//            // 当小数点是第一个字符时
//            if (allowDecimal && char == '.' && dstart == 0) {
//                onInvalidInput?.invoke(char.toString(), dstart)
//                return@InputFilter ""
//            }
//            if (!char.isDigit() && !(allowDecimal && char == '.')) {
//                onInvalidInput?.invoke(char.toString(), dstart)
//                return@InputFilter ""
//            } else {
//                onValidInput?.invoke(char.toString(), dstart)
//            }
//        }
//        null
//    })
//    inputType = when {
//        isMasked -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//        allowDecimal -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
//        else -> InputType.TYPE_CLASS_NUMBER
//    }
//}
//
//
//private fun Char.isASCIISymbol(): Boolean =
//    this.code in 32..126 && !this.isLetter() && !this.isDigit()
//
///**
// * 限制EditText只能输入字母。
// * @param isMasked 若为true，则文本以类似密码的蒙版形式显示。
// * @param allowSymbols 若为true，则允许输入符号。
// * @param onValidInput 当输入内容有效时调用此回调，并传入有效字符。
// * @param onInvalidInput 当输入内容无效时调用此回调，并传入无效字符。
// */
//fun EditText.limitLetters(
//    isMasked: Boolean = false,
//    allowSymbols: Boolean = true,
//    onValidInput: ((valid: String, position: Int) -> Unit)? = null,
//    onInvalidInput: ((invalid: String, position: Int) -> Unit)? = null
//) {
//    // 设置输入类型
//    inputType = when {
//        isMasked -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//        else -> InputType.TYPE_CLASS_TEXT
//    }
//    this.doOnTextChanged { s, start, before, count ->
//        if (s == null || start >= s.length) return@doOnTextChanged
//        val char = s[start]
//        val isLetter = char.isLetter()
//        val isSymbol =
//            Regex("[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>?~]").containsMatchIn(char.toString())
//
//        if (!isLetter && (!allowSymbols || !isSymbol)) {
//            onInvalidInput?.invoke(char.toString(), start)
//            this@limitLetters.text?.delete(start, start + count)
//        } else {
//            onValidInput?.invoke(char.toString(), start)
//        }
//    }
//}
//
//
///**
// * 限制EditText只能输入数字和字母。
// *
// * @param isMasked 是否使用掩码显示文本。
// * @param allowSymbols 是否允许ASCII符号。
// * @param onValidInput 当输入有效字符时的回调，返回输入的字符。
// * @param onInvalidInput 当输入无效字符时的回调，返回输入的字符。
// */
//fun EditText.limitNumbersAndLetters(
//    isMasked: Boolean = false,
//    allowSymbols: Boolean = true,
//    onValidInput: ((valid: String, position: Int) -> Unit)? = null,
//    onInvalidInput: ((invalid: String, position: Int) -> Unit)? = null
//) {
//    filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
//        for (i in start until end) {
//            val char = source[i]
//            if (!char.isDigit() && !char.isLetter() &&
//                !(allowSymbols && char.isASCIISymbol())
//            ) {
//                onInvalidInput?.invoke(char.toString(), dstart)
//                return@InputFilter ""
//            } else {
//                onValidInput?.invoke(char.toString(), dstart)
//            }
//        }
//        null
//    })
//    inputType = if (isMasked) {
//        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//    } else {
//        InputType.TYPE_CLASS_TEXT
//    }
//}
//
//
///**
// * 清除EditText的输入限制，允许所有字符。
// */
//fun EditText.allowAllInput() {
//    // 清除所有的过滤器，以取消之前设置的任何输入限制
//    this.filters = arrayOf()
//
//    // 重置输入类型为默认类型
//    this.inputType = InputType.TYPE_CLASS_TEXT
//}
