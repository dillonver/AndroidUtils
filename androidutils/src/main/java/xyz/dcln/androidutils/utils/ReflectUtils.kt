package xyz.dcln.androidutils.utils

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 4:06
 */
object ReflectUtils {
    private var targetClass: Class<*>? = null

    /**
     * 设置要反射的类
     * @param clazz 要反射的类
     * @return 当前 ReflectUtils 对象
     */
    fun reflect(clazz: Class<*>): ReflectUtils {
        targetClass = clazz
        return this
    }

    /**
     * 实例化反射对象
     * @return 反射对象的实例
     */
    fun newInstance(vararg args: Any?): Any? {
        return targetClass?.getDeclaredConstructor()?.newInstance(*args)
    }

    /**
     * 设置反射的字段
     * @param fieldName 字段名称
     * @return 反射得到的字段对象，若字段不存在则返回 null
     */
    fun field(fieldName: String): Field? {
        return try {
            targetClass?.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            null
        }
    }

    /**
     * 设置反射的方法
     * @param methodName 方法名称
     * @param parameterTypes 方法参数类型
     * @return 反射得到的方法对象，若方法不存在则返回 null
     */
    fun method(methodName: String, vararg parameterTypes: Class<*>): Method? {
        return try {
            targetClass?.getDeclaredMethod(methodName, *parameterTypes)
        } catch (e: NoSuchMethodException) {
            null
        }
    }

    /**
     * 获取反射想要获取的值
     * @param obj 目标对象
     * @param fieldName 字段名称
     * @return 反射得到的字段的值，若字段不存在或获取失败则返回 null
     */
    fun get(obj: Any?, fieldName: String): Any? {
        val field = field(fieldName)
        field?.isAccessible = true
        return field?.get(obj)
    }
}