package xyz.dcln.androidutils.utils

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import kotlin.random.Random

object ColorUtils {

    /**
     * 根据 ColorRes 资源 ID 获取对应的颜色值
     * @param context 上下文对象
     * @param resId ColorRes 资源 ID
     * @return 对应的颜色值
     */
    fun getColor(resId: Int, context: Context = AppUtils.getApp()): Int {
        return ContextCompat.getColor(context, resId)
    }

    /**
     * 获取随机颜色值
     * @return 随机的颜色值
     */
    fun getRandomColor(): Int {
        val red = Random.nextInt(256)
        val green = Random.nextInt(256)
        val blue = Random.nextInt(256)
        return Color.rgb(red, green, blue)
    }

    /**
     * 将十六进制字符串颜色值转换为 Color 对象
     * @param hexString 十六进制颜色字符串，例如 "#RRGGBB" 或 "#AARRGGBB"
     * @return 对应的 Color 对象
     */
    fun parseColor(hexString: String): Int {
        return Color.parseColor(hexString)
    }

    /**
     * 将 RGB 颜色值转换为对应的十六进制字符串
     * @param red 红色通道值，取值范围 0-255
     * @param green 绿色通道值，取值范围 0-255
     * @param blue 蓝色通道值，取值范围 0-255
     * @return 对应的十六进制颜色字符串，例如 "#RRGGBB"
     */
    fun rgbToHexString(red: Int, green: Int, blue: Int): String {
        return String.format("#%02X%02X%02X", red, green, blue)
    }

    /**
     * 将 ARGB 颜色值转换为对应的十六进制字符串
     * @param alpha 透明度通道值，取值范围 0-255
     * @param red 红色通道值，取值范围 0-255
     * @param green 绿色通道值，取值范围 0-255
     * @param blue 蓝色通道值，取值范围 0-255
     * @return 对应的十六进制颜色字符串，例如 "#AARRGGBB"
     */
    fun argbToHexString(alpha: Int, red: Int, green: Int, blue: Int): String {
        return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
    }

    /**
     * 提取颜色的红色通道值
     * @param color 颜色值
     * @return 红色通道值，取值范围 0-255
     */
    fun getRed(color: Int): Int {
        return Color.red(color)
    }

    /**
     * 提取颜色的绿色通道值
     * @param color 颜色值
     * @return 绿色通道值，取值范围 0-255
     */
    fun getGreen(color: Int): Int {
        return Color.green(color)
    }

    /**
     * 提取颜色的蓝色通道值
     * @param color 颜色值
     * @return 蓝色通道值，取值范围 0-255
     */
    fun getBlue(color: Int): Int {
        return Color.blue(color)
    }

    /**
     * 提取颜色的透明度通道值
     * @param color 颜色值
     * @return 透明度通道值，取值范围 0-255
     */
    fun getAlpha(color: Int): Int {
        return Color.alpha(color)
    }

    /**
     * 修改颜色的透明度
     * @param color 颜色值
     * @param alpha 透明度通道值，取值范围 0-255
     * @return 修改透明度后的颜色值
     */
    fun setAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}