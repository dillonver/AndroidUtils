package xyz.dcln.androidutils.utils.ext

import android.content.res.Resources
import android.util.TypedValue

private val displayMetrics = Resources.getSystem().displayMetrics

/**
 * 将 dp 值转换为对应的 px 值
 * @receiver dp 值
 * @return 转换后的 px 值
 */
val Float.dp: Float
    get() = this * displayMetrics.density

/**
 * 将 dp 值转换为对应的 px 值
 * @receiver dp 值
 * @return 转换后的 px 值
 */
val Int.dp: Int
    get() = (this * displayMetrics.density).toInt()

/**
 * 将 sp 值转换为对应的 px 值
 * @receiver sp 值
 * @return 转换后的 px 值
 */
val Float.sp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, displayMetrics)

/**
 * 将 sp 值转换为对应的 px 值
 * @receiver sp 值
 * @return 转换后的 px 值
 */
val Int.sp: Int
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), displayMetrics).toInt()

/**
 * 将 px 值转换为对应的 dp 值
 * @receiver px 值
 * @return 转换后的 dp 值
 */
val Float.pxToDp: Float
    get() = this / displayMetrics.density

/**
 * 将 px 值转换为对应的 dp 值
 * @receiver px 值
 * @return 转换后的 dp 值
 */
val Int.pxToDp: Int
    get() = (this / displayMetrics.density).toInt()

/**
 * 将 px 值转换为对应的 sp 值
 * @receiver px 值
 * @return 转换后的 sp 值
 */
val Float.pxToSp: Float
    get() = this / (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1f, displayMetrics) / 1f)

/**
 * 将 px 值转换为对应的 sp 值
 * @receiver px 值
 * @return 转换后的 sp 值
 */
val Int.pxToSp: Int
    get() = (this / (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1f, displayMetrics) / 1f)).toInt()

/**
 * 在整数前面补零并返回格式化后的字符串
 * @return 格式化后的字符串
 */
fun Int.appendZero(): String = "%02d".format(this)

/**
 * 将 Float 类型的数字转换为百分比字符串
 * @param decimalPlaces 保留的小数位数
 * @return 百分比表示的字符串
 */
fun Float.toPercent(decimalPlaces: Int = 0): String {
    return "%.${decimalPlaces}f%%".format(this * 100)
}
