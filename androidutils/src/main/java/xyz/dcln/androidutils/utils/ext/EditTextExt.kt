package xyz.dcln.androidutils.utils.ext

import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import xyz.dcln.androidutils.utils.LogUtils
import java.util.WeakHashMap


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


// Validators for different input patterns
private val alphanumericValidator = { input: CharSequence? ->
    input?.matches(Regex("^[a-zA-Z0-9]*$")) ?: false
}

private val alphabeticValidator = { input: CharSequence? ->
    input?.matches(Regex("^[a-zA-Z]*$")) ?: false
}

private val numericValidator = { input: CharSequence? ->
    input?.matches(Regex("^[0-9]*$")) ?: false
}

// Map to associate each EditText with its corresponding TextWatcher
private val textWatcherMap = WeakHashMap<EditText, TextWatcher>()

/**
 * Limit the input of the EditText based on the given validator function.
 * @param validator The function to validate the input.
 * @param maxLength Maximum allowed length for the input. Defaults to -1, indicating no limit.
 * @param onInvalidInput Callback function to handle invalid input.
 */
fun EditText.limitInput(
    validator: (CharSequence?) -> Boolean,
    maxLength: Int = -1,
    onInvalidInput: ((CharSequence?) -> Unit)? = null
) {
    // Remove the current TextWatcher if it exists
    textWatcherMap[this]?.let { this.removeTextChangedListener(it) }

    // Clear the current text
    this.text.clear()

    // Store the last valid text for potential restoration upon validation failure
    var lastValidText = text.toString()

    val watcher = this.doOnTextChanged { changedText, _, _, _ ->
        if (changedText.isNullOrEmpty() || validator(changedText) && (maxLength == -1 || changedText.length <= maxLength)) {
            lastValidText = text.toString()
        } else {
            onInvalidInput?.invoke(changedText)
            this.setText(lastValidText)
            this.setSelection(lastValidText.length)  // Move the cursor to the end of the text
        }
    }

    // Store the TextWatcher in the map
    textWatcherMap[this] = watcher
}

/** Limit the input to alphanumeric characters. */
fun EditText.limitToAlphanumeric(
    maxLength: Int = -1,
    onInvalidInput: ((CharSequence?) -> Unit)? = null
) = limitInput(alphanumericValidator, maxLength, onInvalidInput)

/** Limit the input to alphabetic characters. */
fun EditText.limitToAlphabetic(
    maxLength: Int = -1,
    onInvalidInput: ((CharSequence?) -> Unit)? = null
) = limitInput(alphabeticValidator, maxLength, onInvalidInput)

/** Limit the input to numeric characters. */
fun EditText.limitToNumeric(
    maxLength: Int = -1,
    onInvalidInput: ((CharSequence?) -> Unit)? = null
) = limitInput(numericValidator, maxLength, onInvalidInput)
