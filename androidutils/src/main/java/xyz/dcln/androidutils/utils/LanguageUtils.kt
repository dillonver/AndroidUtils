package xyz.dcln.androidutils.utils

import android.os.Build
import java.util.Locale

/**
 * Created by dcl on 2023/7/7.
 */
object LanguageUtils {
    /**
     * 获取系统语言
     *
     * @return 当前系统的语言代码
     */
    fun getSystemLanguage(): String {
        val currentLocale = Locale.getDefault()
        return currentLocale.language
    }


    /**
     * 获取系统国家
     *
     * @return 当前系统的国家代码
     */
    fun getSystemCountry(): String {
        val currentLocale = Locale.getDefault()
        return currentLocale.country
    }

    /**
     * 获取系统语言标签
     *
     * @return 当前系统的IETF BCP 47语言标签
     */
    fun getSystemLanguageTag(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Locale.getDefault().toLanguageTag()
        }
        return "${getSystemLanguage()}-${getSystemCountry()}"  // Fallback for older versions
    }

    /**
     * 获取系统语言显示名称
     *
     * @return 当前系统语言的显示名称
     */
    fun getSystemLanguageDisplayName(): String {
        val currentLocale = Locale.getDefault()
        return currentLocale.displayLanguage
    }

    /**
     * 获取系统国家显示名称
     *
     * @return 当前系统国家的显示名称
     */
    fun getSystemCountryDisplayName(): String {
        val currentLocale = Locale.getDefault()
        return currentLocale.displayCountry
    }
}