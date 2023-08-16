package xyz.dcln.androidutils.utils



import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import xyz.dcln.androidutils.utils.LogUtils.logE


/**
 * Description:
 * Author: Dillon
 * Date: 2023/4/5 20:27
 */

object GsonUtils {

    @PublishedApi
    internal var gson: Gson = GsonBuilder().create()

    @Synchronized
    fun setCustomGson(customGson: Gson) {
        gson = customGson
    }

    fun toJson(src: Any?): String? = gson.toJson(src)

     inline fun <reified T> fromJson(json: String?): T? {
        if (json == null) {
            logE("Input json string is null")
            return null
        }

        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            logE("Failed to parse JSON", e)
            null
        }
    }

    fun <T> fromJson(json: String?, type: Type?): T? {
        if (json == null || type == null) {
            logE("Input json string or type is null")
            return null
        }

        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            logE("Failed to parse JSON", e)
            null
        }
    }

    fun getListType(type: Type?): Type {
        if (type == null) {
            throw IllegalArgumentException("Type parameter cannot be null")
        }
        return TypeToken.getParameterized(List::class.java, type).type
    }

    fun getSetType(type: Type?): Type {
        if (type == null) {
            throw IllegalArgumentException("Type parameter cannot be null")
        }
        return TypeToken.getParameterized(Set::class.java, type).type
    }

    fun getMapType(keyType: Type?, valueType: Type?): Type {
        if (keyType == null || valueType == null) {
            throw IllegalArgumentException("Key or Value Type parameters cannot be null")
        }
        return TypeToken.getParameterized(Map::class.java, keyType, valueType).type
    }

    fun getArrayType(type: Type?): Type {
        if (type == null) {
            throw IllegalArgumentException("Type parameter cannot be null")
        }
        return TypeToken.getArray(type).type
    }

    fun getType(type: Class<*>?): Type {
        if (type == null) {
            throw IllegalArgumentException("Type parameter cannot be null")
        }
        return TypeToken.get(type).type
    }


}
