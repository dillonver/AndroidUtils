package xyz.dcln.androidutils.utils

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * Description:
 * Author: Dillon
 * Date: 2024/5/21 12:01
 */
object FragmentUtils {

    // 扩展函数：创建一个新的Fragment实例并传递参数
    inline fun <reified T : Fragment> newInstance(vararg params: Pair<String, Any?>): T {
        val fragment = T::class.java.getDeclaredConstructor().newInstance()
        fragment.arguments = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Parcelable -> putParcelable(key, value)
                    is java.io.Serializable -> putSerializable(key, value)
                    else -> putString(key, Gson().toJson(value))
                }
            }
        }
        return fragment
    }

    // 委托类：处理Fragment参数
    class FragmentArgumentDelegate<T : Any>(private val key: String, private val clazz: Class<T>) : ReadWriteProperty<Fragment, T?> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
            val args = thisRef.arguments ?: return null
            return try {
                when (clazz) {
                    String::class.java -> args.getString(key) as T?
                    Int::class.java -> args.getInt(key) as T?
                    Boolean::class.java -> args.getBoolean(key) as T?
                    Float::class.java -> args.getFloat(key) as T?
                    Long::class.java -> args.getLong(key) as T?
                    Double::class.java -> args.getDouble(key) as T?
                    Parcelable::class.java -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            args.getParcelable(key, clazz as Class<Parcelable>) as T?
                        } else {
                            @Suppress("DEPRECATION")
                            args.getParcelable(key) as T?
                        }
                    }
                    java.io.Serializable::class.java -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            args.getSerializable(key, clazz) as T?
                        } else {
                            @Suppress("DEPRECATION")
                            args.getSerializable(key) as T?
                        }
                    }
                    else -> args.getString(key)?.let { Gson().fromJson(it, clazz) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null // 在发生异常时返回 null
            }
        }

        override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
            if (thisRef.arguments == null) {
                thisRef.arguments = Bundle()
            }
            with(thisRef.requireArguments()) {
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Parcelable -> putParcelable(key, value)
                    is Serializable -> putSerializable(key, value)
                    else -> putString(key, Gson().toJson(value))
                }
            }
        }
    }


    // 辅助函数：创建委托
    inline fun <reified T : Any> fragmentArgument(key: String): FragmentArgumentDelegate<T> {
        return FragmentArgumentDelegate(key, T::class.java)
    }

    // 向Fragment的生命周期添加观察者
    inline fun Fragment.observeLifecycle(crossinline onEvent: (source: LifecycleOwner) -> Unit) {
        this.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onEvent(owner)
            }
        })
    }


    // 检查fragment当前是否对用户可见
    val Fragment.isVisibleToUser: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && isVisible


    // LiveData：观察Fragment可见性
    val Fragment.isVisibleLiveData: LiveData<Boolean>
        get() {
            val liveData = MutableLiveData<Boolean>()
            this.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    liveData.value = true
                }

                override fun onPause(owner: LifecycleOwner) {
                    liveData.value = false
                }
            })
            return liveData
        }

    // 判断Fragment是否存在
    fun FragmentManager.isFragmentExists(tag: String): Boolean {
        return findFragmentByTag(tag) != null
    }

    // 判断Fragment是否可见
    fun FragmentManager.isFragmentVisible(tag: String): Boolean {
        val fragment = findFragmentByTag(tag)
        return fragment?.isVisible == true
    }

    // 判断Fragment是否存在（传入Fragment的class）
    inline fun <reified T : Fragment> FragmentManager.isFragmentExists(): Boolean {
        return findFragmentByTag(T::class.java.name) != null
    }

    // 判断Fragment是否可见（传入Fragment的class）
    inline fun <reified T : Fragment> FragmentManager.isFragmentVisible(): Boolean {
        val fragment = findFragmentByTag(T::class.java.name)
        return fragment?.isVisible == true
    }

}
