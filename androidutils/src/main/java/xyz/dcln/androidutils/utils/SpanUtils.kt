package xyz.dcln.androidutils.utils

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*

object SpanUtils {
    // 创建粗体文本样式
    fun bold(text: CharSequence): SpannableString {
        val spannable = SpannableString.valueOf(text)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // 创建斜体文本样式
    fun italic(text: CharSequence): SpannableString {
        val spannable = SpannableString.valueOf(text)
        spannable.setSpan(StyleSpan(Typeface.ITALIC), 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // 创建下划线文本样式
    fun underline(text: CharSequence): SpannableString {
        val spannable = SpannableString.valueOf(text)
        spannable.setSpan(UnderlineSpan(), 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // 创建删除线文本样式
    fun strikethrough(text: CharSequence): SpannableString {
        val spannable = SpannableString.valueOf(text)
        spannable.setSpan(StrikethroughSpan(), 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // 创建指定颜色的文本样式
    fun color(text: CharSequence, color: Int): SpannableString {
        val spannable = SpannableString.valueOf(text)
        spannable.setSpan(ForegroundColorSpan(color), 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // 创建指定背景颜色的文本样式
    fun background(text: CharSequence, color: Int): SpannableString {
        val spannable = SpannableString.valueOf(text)
        spannable.setSpan(BackgroundColorSpan(color), 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // 创建指定字体大小的文本样式
    fun size(text: CharSequence, size: Float): SpannableString {
        val spannable = SpannableString.valueOf(text)
        spannable.setSpan(AbsoluteSizeSpan(size.toInt()), 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // 同时应用多个文本样式
    fun multiple(text: CharSequence, vararg spans: Any): SpannableString {
        val spannable = SpannableString.valueOf(text)
        for (span in spans) {
            spannable.setSpan(span, 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        return spannable
    }

    // 拼接多个文本样式
    fun append(vararg spans: Any): SpannableString {
        val builder = SpannableStringBuilder()
        for (span in spans) {
            builder.append(span.toString())
        }
        return SpannableString.valueOf(builder)
    }
}
