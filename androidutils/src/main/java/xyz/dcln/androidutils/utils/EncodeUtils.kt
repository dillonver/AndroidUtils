package xyz.dcln.androidutils.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import kotlin.text.Charsets.UTF_8

/**
 * Created by dcl on 2023/7/7.
 */
object EncodeUtils {
    /**
     * URL 编码
     */
    fun urlEncode(input: String): String {
        return URLEncoder.encode(input, UTF_8.name())
    }

    /**
     * URL 解码
     */
    fun urlDecode(input: String): String {
        return URLDecoder.decode(input, UTF_8.name())
    }

    /**
     * Base64 编码
     */
    fun base64Encode(input: ByteArray): ByteArray {
        return Base64.getEncoder().encode(input)
    }

    /**
     * Base64 编码为字符串
     */
    fun base64Encode2String(input: ByteArray): String {
        return Base64.getEncoder().encodeToString(input)
    }

    /**
     * Base64 解码
     */
    fun base64Decode(input: ByteArray): ByteArray {
        return Base64.getDecoder().decode(input)
    }

    /**
     * Html 编码
     */
    fun htmlEncode(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    /**
     * Html 解码
     */
    fun htmlDecode(input: String): String {
        return input.replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
    }

    /**
     * 二进制编码
     */
    fun binaryEncode(input: ByteArray): String {
        return input.joinToString("") { byte ->
            Integer.toBinaryString(byte.toInt() and 0xFF).padStart(8, '0')
        }
    }

    /**
     * 二进制解码
     */
    fun binaryDecode(input: String): ByteArray {
        val regex = "(.{1,8})".toRegex()
        val matchResult = regex.findAll(input)
        val byteList = mutableListOf<Byte>()

        matchResult.forEach {
            val byteValue = it.value.toInt(2).toByte()
            byteList.add(byteValue)
        }

        return byteList.toByteArray()
    }
}