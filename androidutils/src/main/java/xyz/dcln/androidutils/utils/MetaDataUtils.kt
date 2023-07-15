package xyz.dcln.androidutils.utils

import android.content.pm.PackageManager

/**
 * Created by dcl on 2023/7/7.
 */
object MetaDataUtils {
    /**
     * 获取 application 的 meta-data 值
     *
     * @param key meta-data 键，可选
     * @return meta-data 值，若未找到则返回空字符串
     */
    fun getMetaDataInApp(key: String = ""): Map<String, String> {
        return getMetaData(AppUtils.getApp().packageName, key)
    }

    /**
     * 获取 activity 的 meta-data 值
     *
     * @param activityClass 目标 Activity 类
     * @param key meta-data 键，可选
     * @return meta-data 值，若未找到则返回空字符串
     */
    fun getMetaDataInActivity(activityClass: Class<*>, key: String = ""): Map<String, String> {
        return getMetaData(activityClass.`package`?.name ?: "", key)
    }

    /**
     * 获取 service 的 meta-data 值
     *
     * @param serviceClass 目标 Service 类
     * @param key meta-data 键，可选
     * @return meta-data 值，若未找到则返回空字符串
     */
    fun getMetaDataInService(serviceClass: Class<*>, key: String = ""): Map<String, String> {
        return getMetaData(serviceClass.`package`?.name ?: "", key)
    }

    /**
     * 获取 receiver 的 meta-data 值
     *
     * @param receiverClass 目标 BroadcastReceiver 类
     * @param key meta-data 键，可选
     * @return meta-data 值，若未找到则返回空字符串
     */
    fun getMetaDataInReceiver(receiverClass: Class<*>, key: String = ""): Map<String, String> {
        return getMetaData(receiverClass.`package`?.name ?: "", key)
    }

    /**
     * 获取指定包名的组件的 meta-data 值
     *
     * @param packageName 目标组件所在的包名
     * @param key meta-data 键，可选
     * @return meta-data 值的键值对映射，若未找到则返回空的映射
     */
    private fun getMetaData(packageName: String, key: String = ""): Map<String, String> {
        val metaDataMap = mutableMapOf<String, String>()

        try {
            val appInfo = AppUtils.getApp().packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )
            val metaData = appInfo.metaData

            metaData?.keySet()?.forEach { metaKey ->
                if (key.isEmpty() || key == metaKey) {
                    val value = metaData.getString(metaKey) ?: ""
                    metaDataMap[metaKey] = value
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return metaDataMap
    }
}