package xyz.dcln.androidutils.utils.ext

import android.content.res.Resources
import android.util.TypedValue
import java.text.NumberFormat

/**
 * 数值相关拓展属性/方法
 */

private val displayMetrics = Resources.getSystem().displayMetrics

/**
 * 将 dp 值转换为对应的 px 值
 * @receiver dp 值
 * @return 转换后的 px 值
 */
val Float.dp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)

/**
 * 将 dp 值转换为对应的 px 值
 * @receiver dp 值
 * @return 转换后的 px 值
 */
val Int.dp: Int
    get() = this.toFloat().dp.toInt()

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
    get() = this.toFloat().sp.toInt()

/**
 * 在整数前面补零并返回格式化后的字符串
 * @return 格式化后的字符串
 */
fun Int.appendZero(): String = String.format("%02d", this)

/**
 * 将 Float 类型的数字转换为百分比字符串。
 *
 * @param decimalPlaces 保留的小数位数
 * @return 百分比表示的字符串。
 */
fun Float.toPercent(decimalPlaces: Int? = 0): String {
    val percentage = (this * 100).toDouble()
    return if (decimalPlaces != null) {
        "%.${decimalPlaces}f%%".format(percentage)
    } else {
        "%f%%".format(percentage)
    }
}