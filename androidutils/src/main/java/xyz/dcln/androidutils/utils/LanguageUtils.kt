package xyz.dcln.androidutils.utils

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
}