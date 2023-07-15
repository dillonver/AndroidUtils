package xyz.dcln.androidutils.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 3:10
 */
object ClipboardUtils {

    // 复制文本到剪贴板，并执行回调函数
    fun copyText(text: String, callback: ((isSuccessful: Boolean) -> Unit)? = null) {
        val clipboardManager =
            AppUtils.getApp().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, text)
        clipboardManager.setPrimaryClip(clipData)

        val copiedText = getText()
        val isSuccessful = copiedText != null && copiedText == text
        callback?.invoke(isSuccessful) // 调用回调函数
    }

    // 从剪贴板中获取文本
    fun getText(): String? {
        val clipboardManager =
            AppUtils.getApp().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val item = clipData.getItemAt(0)
            return item.text.toString()
        }
        return null
    }

}