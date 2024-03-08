package xyz.dcln.androidutils.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import java.io.File
import java.util.*

/**
 * Created by dcl on 2023/7/7.
 */
object DeviceUtils {
    // 判断设备是否 rooted
    fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        return try {
            File("/system/app/Superuser.apk").exists()
        } catch (e: Exception) {
            false
        }
    }

    // 具有默认值的通用方法，用于获取系统设置属性
    private fun getSettingsInt(name: String, defaultValue: Int = 0): Int =
        try {
            Settings.Secure.getInt(AppUtils.getApp().contentResolver, name, defaultValue)
        } catch (e: Exception) {
            defaultValue
        }

    // 获取设备系统版本号
    fun getSDKVersionName(): String = Build.VERSION.RELEASE

    // 获取设备系统版本码
    fun getSDKVersionCode(): Int = Build.VERSION.SDK_INT

    // 获取设备 AndroidID
    @SuppressLint("HardwareIds")
    fun getAndroidID(): String = Settings.Secure.getString(
        AppUtils.getApp().contentResolver,
        Settings.Secure.ANDROID_ID
    )

    // 获取设备厂商
    fun getManufacturer(): String = Build.MANUFACTURER

    // 获取设备型号
    fun getModel(): String = Build.MODEL

    // 获取设备 ABIs
    fun getABIs(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Build.SUPPORTED_ABIS
        } else {
            if (!TextUtils.isEmpty(Build.CPU_ABI2)) {
                arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
            } else {
                arrayOf(Build.CPU_ABI)
            }
        }

    // 判断是否是平板
    fun isTablet(): Boolean =
        (AppUtils.getApp().resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE

    // 判断是否是模拟器
    fun isEmulator(): Boolean =
        listOf(
            "generic",
            "unknown",
            "google_sdk",
            "Emulator",
            "Android SDK built for x86",
            "QC_Reference_Phone",
            "Genymotion"
        ).any { Build.FINGERPRINT.startsWith(it) || Build.MODEL.contains(it) }

    // 开发者选项是否打开
    fun isDevelopmentSettingsEnabled(): Boolean =
        getSettingsInt(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) != 0

    // 获取唯一设备 ID
    fun getUniqueDeviceId(): String {
        val androidId = getAndroidID()
        if (!TextUtils.isEmpty(androidId) && androidId != "9774d56d682e549c") {
            // 如果能够获取到有效的 Android ID，则返回 Android ID
            return androidId
        }
        // 否则，返回独特的 UUID
        return getUniqueId()
    }

    // 获取独特的 UUID
    private fun getUniqueId(): String {
        val prefUniqueId = "PREF_UNIQUE_ID"
        val sharedPreferences: SharedPreferences =
            AppUtils.getApp().getSharedPreferences(prefUniqueId, Context.MODE_PRIVATE)
        var uniqueId = sharedPreferences.getString(prefUniqueId, null)

        if (uniqueId == null) {
            // 如果不存在保存的 UUID，则生成一个新的 UUID 并保存到 SharedPreferences 中
            uniqueId = UUID.randomUUID().toString()
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(prefUniqueId, uniqueId)
            editor.apply()
        }

        // 返回独特的 UUID
        return uniqueId
    }

    // 判断是否同一设备
    fun isSameDevice(deviceUniqueId: String): Boolean =
        getUniqueDeviceId() == deviceUniqueId


    /**
     * check the system is harmony os
     *
     * @return true if it is harmony os
     */
    fun isHarmonyOS(): Boolean {
        try {
            val clz = ReflectUtils.getClassByName("com.huawei.system.BuildEx") ?: return false
            val method = ReflectUtils.getMethod(clz, "getOsBrand") ?: return false
            return "harmony" == method.invoke(clz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}
