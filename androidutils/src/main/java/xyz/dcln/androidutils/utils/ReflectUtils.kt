package xyz.dcln.androidutils.utils

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Description:
 * Author: Dillon
 * Date: 2024/3/8 11:18
 */
object ReflectUtils {

    /**
     * 通过类名获取Class对象。
     * @param className 完整的类名，包含包路径。
     * @return Class对象，如果找不到对应的类则返回null。
     */
    fun getClassByName(className: String): Class<*>? {
        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 通过反射创建类的一个新实例。
     * @param classObject 类的Class对象。
     * @param args 构造函数的参数。
     * @return 创建的类实例，如果创建失败则返回null。
     */
    fun createInstance(classObject: Class<*>, vararg args: Any?): Any? {
        return try {
            val constructor = classObject.getDeclaredConstructor(*args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
            constructor.isAccessible = true
            constructor.newInstance(*args)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取指定的字段对象。
     * @param classObject 类的Class对象。
     * @param fieldName 字段名称。
     * @return Field对象，如果找不到对应的字段则返回null。
     */
    fun getField(classObject: Class<*>, fieldName: String): Field? {
        return try {
            val field = classObject.getDeclaredField(fieldName)
            field.isAccessible = true
            field
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取指定的方法对象。
     * @param classObject 类的Class对象。
     * @param methodName 方法名称。
     * @param parameterTypes 方法参数类型数组，没有参数则传入空数组。
     * @return Method对象，如果找不到对应的方法则返回null。
     */
    fun getMethod(classObject: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method? {
        return try {
            val method = classObject.getDeclaredMethod(methodName, *parameterTypes)
            method.isAccessible = true
            method
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取对象或类的字段值。
     * @param classObject 类的Class对象。
     * @param obj 对象实例，静态字段时传入null。
     * @param fieldName 字段名称。
     * @return 字段值，如果获取失败则返回null。
     */
    fun getTarget(classObject: Class<*>, obj: Any?, fieldName: String): Any? {
        return try {
            val field = classObject.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(obj)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 设置对象或类的字段值。
     * @param classObject 类的Class对象。
     * @param obj 对象实例，静态字段时传入null。
     * @param fieldName 字段名称。
     * @param value 新的字段值。
     */
    fun setTarget(classObject: Class<*>, obj: Any?, fieldName: String, value: Any?) {
        try {
            val field = classObject.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(obj, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 调用对象或类的方法。
     * @param classObject 类的Class对象。
     * @param methodName 方法名称。
     * @param parameterTypes 方法参数类型数组。
     * @param obj 对象实例，静态方法时传入null。
     * @param args 方法参数。
     * @return 方法调用的返回值，如果调用失败则返回null。
     */
    fun invokeMethod(classObject: Class<*>, methodName: String, parameterTypes: Array<Class<*>>, obj: Any?, vararg args: Any?): Any? {
        return try {
            val method = classObject.getDeclaredMethod(methodName, *parameterTypes)
            method.isAccessible = true
            method.invoke(obj, *args)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}