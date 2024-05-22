package xyz.dcln.androidutils.utils

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable


object IntentUtils {

    /**
     * 从Intent中获取值的通用方法
     * @param intent 传入的Intent
     * @param key 参数的键
     * @return 返回对应键的值
     */
    inline fun <reified T> getValueFromIntent(intent: Intent, key: String): T? {
        return try {
            @Suppress("UNCHECKED_CAST")
            when (T::class) {
                Int::class -> intent.getIntExtra(key, 0) as T?
                Float::class -> intent.getFloatExtra(key, 0f) as T?
                Double::class -> intent.getDoubleExtra(key, 0.0) as T?
                Long::class -> intent.getLongExtra(key, 0L) as T?
                String::class -> intent.getStringExtra(key) as T?
                Boolean::class -> intent.getBooleanExtra(key, false) as T?
                Bundle::class -> intent.getBundleExtra(key) as T?
                Parcelable::class -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(key, T::class.java) as T?
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(key) as T?
                    }
                }

                Serializable::class -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getSerializableExtra(key, (T::class.java) as Class<Serializable>) as T?
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getSerializableExtra(key) as? T
                    }
                }

                else -> {
                    val jsonString = intent.getStringExtra(key)
                    jsonString?.let { GsonUtils.fromJson(it, T::class.java) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // 可以根据需要将异常信息记录到日志中
            null // 在发生异常时返回 null
        }
    }

}
