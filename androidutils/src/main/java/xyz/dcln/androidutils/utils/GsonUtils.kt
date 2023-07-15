package xyz.dcln.androidutils.utils



import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import xyz.dcln.androidutils.utils.LogUtils.logE
import java.lang.reflect.Type

/**
 * Description:
 * Author: Dillon
 * Date: 2023/4/5 20:27
 */
object GsonUtils {

    @PublishedApi
    internal var gson: Gson = GsonBuilder().create()

    fun setCustomGson(customGson: Gson) {
        gson = customGson
    }

    fun toJson(src: Any?): String? = gson.toJson(src)

    inline fun <reified T> fromJson(json: String?): T? =
        try {
            gson.fromJson(json, T::class.java)
        } catch (e: JsonSyntaxException) {
            logE(e)
            null
        }

    fun <T> fromJson(json: String?, type: Type?): T? =
        try {
            gson.fromJson(json, type)
        } catch (e: JsonSyntaxException) {
            logE(e)
            null
        }

    fun getListType(type: Type?): Type = TypeToken.getParameterized(List::class.java, type).type

    fun getSetType(type: Type?): Type = TypeToken.getParameterized(Set::class.java, type).type

    fun getMapType(keyType: Type?, valueType: Type?): Type =
        TypeToken.getParameterized(Map::class.java, keyType, valueType).type

    fun getArrayType(type: Type?): Type = TypeToken.getArray(type).type

    fun getType(type: Class<*>?): Type = type?.let { TypeToken.get(it).type }
        ?: throw IllegalArgumentException("Type not provided")
}