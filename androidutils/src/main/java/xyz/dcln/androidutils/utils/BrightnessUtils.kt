package xyz.dcln.androidutils.utils

import android.provider.Settings

/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 2:04
 */
object BrightnessUtils {
    /**
     * 判断是否开启自动调节亮度
     */
    fun isAutoBrightnessEnabled(): Boolean {
        val mode = Settings.System.getInt(
            AppUtils.getApp().contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        return mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    }

    /**
     * 设置是否开启自动调节亮度
     */
    fun setAutoBrightnessEnabled(enabled: Boolean) {
        val mode =
            if (enabled) Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC else Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        Settings.System.putInt(
            AppUtils.getApp().contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            mode
        )
    }

    /**
     * 获取屏幕亮度
     */
    fun getBrightness(): Int {
        return try {
            Settings.System.getInt(
                AppUtils.getApp().contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Settings.SettingNotFoundException) {
            // 处理异常情况
            // 返回一个默认的亮度值
            -1
        }
    }

    /**
     * 设置屏幕亮度
     */
    fun setBrightness(brightness: Int) {
        Settings.System.putInt(
            AppUtils.getApp().contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            brightness
        )
    }

}