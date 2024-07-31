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

    fun toJson(src: Any?): String? {
        return try {
            gson.toJson(src)
        } catch (e: Exception) {
            logE("Failed to convert object to JSON", e)
            null
        }
    }

    inline fun <reified T> fromJson(json: String?): T? {
        if (json.isNullOrEmpty()) {
            logE("Input json string is null or empty")
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
        if (json.isNullOrEmpty() || type == null) {
            logE("Input json string is null or empty, or type is null")
            return null
        }

        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            logE("Failed to parse JSON", e)
            null
        }
    }

    fun getListType(type: Type?): Type? {
        return try {
            if (type == null) {
                logE("Type parameter cannot be null")
                return null
            }
            TypeToken.getParameterized(List::class.java, type).type
        } catch (e: Exception) {
            logE("Failed to get list type", e)
            null
        }
    }

    fun getSetType(type: Type?): Type? {
        return try {
            if (type == null) {
                logE("Type parameter cannot be null")
                return null
            }
            TypeToken.getParameterized(Set::class.java, type).type
        } catch (e: Exception) {
            logE("Failed to get set type", e)
            null
        }
    }

    fun getMapType(keyType: Type?, valueType: Type?): Type? {
        return try {
            if (keyType == null || valueType == null) {
                logE("Key or Value Type parameters cannot be null")
                return null
            }
            TypeToken.getParameterized(Map::class.java, keyType, valueType).type
        } catch (e: Exception) {
            logE("Failed to get map type", e)
            null
        }
    }

    fun getArrayType(type: Type?): Type? {
        return try {
            if (type == null) {
                logE("Type parameter cannot be null")
                return null
            }
            TypeToken.getArray(type).type
        } catch (e: Exception) {
            logE("Failed to get array type", e)
            null
        }
    }

    fun getType(type: Class<*>?): Type? {
        return try {
            if (type == null) {
                logE("Type parameter cannot be null")
                return null
            }
            TypeToken.get(type).type
        } catch (e: Exception) {
            logE("Failed to get type", e)
            null
        }
    }

    fun logE(message: String, e: Exception? = null) {
        // Implement your logging mechanism here, for example:
        println("$message ${e?.message}")
        e?.printStackTrace()
    }
}

