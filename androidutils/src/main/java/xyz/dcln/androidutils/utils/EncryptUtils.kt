package xyz.dcln.androidutils.utils


import android.annotation.SuppressLint
import java.io.File
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 4:03
 */
object EncryptUtils {
    // MD2 加密
    fun encryptMD2(data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("MD2")
        return md.digest(data)
    }

    fun encryptMD2ToString(data: ByteArray): String {
        val md = MessageDigest.getInstance("MD2")
        val digest = md.digest(data)
        return bytes2HexString(digest)
    }

    // MD5 加密
    fun encryptMD5(data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(data)
    }

    fun encryptMD5ToString(data: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(data)
        return bytes2HexString(digest)
    }

    // MD5 加密文件
    fun encryptMD5File(file: File): ByteArray {
        val fis = file.inputStream()
        val buffer = ByteArray(1024)
        val md = MessageDigest.getInstance("MD5")
        var len: Int
        while (fis.read(buffer).also { len = it } != -1) {
            md.update(buffer, 0, len)
        }
        fis.close()
        return md.digest()
    }

    fun encryptMD5File2String(file: File): String {
        val digest = encryptMD5File(file)
        return bytes2HexString(digest)
    }

    // SHA1 加密
    fun encryptSHA1(data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA1")
        return md.digest(data)
    }

    fun encryptSHA1ToString(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA1")
        val digest = md.digest(data)
        return bytes2HexString(digest)
    }

    // SHA224 加密
    fun encryptSHA224(data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-224")
        return md.digest(data)
    }

    fun encryptSHA224ToString(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-224")
        val digest = md.digest(data)
        return bytes2HexString(digest)
    }

    // SHA256 加密
    fun encryptSHA256(data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(data)
    }

    fun encryptSHA256ToString(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(data)
        return bytes2HexString(digest)
    }

    // SHA384 加密
    fun encryptSHA384(data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-384")
        return md.digest(data)
    }

    fun encryptSHA384ToString(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-384")
        val digest = md.digest(data)
        return bytes2HexString(digest)
    }

    // SHA512 加密
    fun encryptSHA512(data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-512")
        return md.digest(data)
    }

    fun encryptSHA512ToString(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(data)
        return bytes2HexString(digest)
    }

    // HmacMD5 加密
    fun encryptHmacMD5(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "HmacMD5")
        val mac = Mac.getInstance(secretKey.algorithm)
        mac.init(secretKey)
        return mac.doFinal(data)
    }

    fun encryptHmacMD5ToString(data: ByteArray, key: ByteArray): String {
        val digest = encryptHmacMD5(data, key)
        return bytes2HexString(digest)
    }

    // HmacSHA1 加密
    fun encryptHmacSHA1(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "HmacSHA1")
        val mac = Mac.getInstance(secretKey.algorithm)
        mac.init(secretKey)
        return mac.doFinal(data)
    }

    fun encryptHmacSHA1ToString(data: ByteArray, key: ByteArray): String {
        val digest = encryptHmacSHA1(data, key)
        return bytes2HexString(digest)
    }

    // HmacSHA224 加密
    fun encryptHmacSHA224(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "HmacSHA224")
        val mac = Mac.getInstance(secretKey.algorithm)
        mac.init(secretKey)
        return mac.doFinal(data)
    }

    fun encryptHmacSHA224ToString(data: ByteArray, key: ByteArray): String {
        val digest = encryptHmacSHA224(data, key)
        return bytes2HexString(digest)
    }

    // HmacSHA256 加密
    fun encryptHmacSHA256(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "HmacSHA256")
        val mac = Mac.getInstance(secretKey.algorithm)
        mac.init(secretKey)
        return mac.doFinal(data)
    }

    fun encryptHmacSHA256ToString(data: ByteArray, key: ByteArray): String {
        val digest = encryptHmacSHA256(data, key)
        return bytes2HexString(digest)
    }

    // HmacSHA384 加密
    fun encryptHmacSHA384(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "HmacSHA384")
        val mac = Mac.getInstance(secretKey.algorithm)
        mac.init(secretKey)
        return mac.doFinal(data)
    }

    fun encryptHmacSHA384ToString(data: ByteArray, key: ByteArray): String {
        val digest = encryptHmacSHA384(data, key)
        return bytes2HexString(digest)
    }

    // HmacSHA512 加密
    fun encryptHmacSHA512(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "HmacSHA512")
        val mac = Mac.getInstance(secretKey.algorithm)
        mac.init(secretKey)
        return mac.doFinal(data)
    }

    fun encryptHmacSHA512ToString(data: ByteArray, key: ByteArray): String {
        val digest = encryptHmacSHA512(data, key)
        return bytes2HexString(digest)
    }

    // DES 加密
    fun encryptDES(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val desKeySpec = DESKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("DES")
        val secretKey = keyFactory.generateSecret(desKeySpec)

        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        val ips = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ips)

        return cipher.doFinal(data)
    }

    fun encryptDES2HexString(data: ByteArray, key: ByteArray, iv: ByteArray): String {
        val encryptData = encryptDES(data, key, iv)
        return bytes2HexString(encryptData)
    }

    fun encryptDES2Base64(data: ByteArray, key: ByteArray, iv: ByteArray): String {
        val encryptData = encryptDES(data, key, iv)
        return Base64.encodeToString(encryptData, Base64.NO_WRAP)
    }

    // DES 解密
    fun decryptDES(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val desKeySpec = DESKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("DES")
        val secretKey = keyFactory.generateSecret(desKeySpec)

        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        val ips = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ips)

        return cipher.doFinal(data)
    }

    fun decryptHexStringDES(data: String, key: ByteArray, iv: ByteArray): ByteArray {
        val decryptData = hexString2Bytes(data)
        return decryptDES(decryptData, key, iv)
    }

    fun decryptBase64DES(data: String, key: ByteArray, iv: ByteArray): ByteArray {
        val decryptData = Base64.decode(data, Base64.NO_WRAP)
        return decryptDES(decryptData, key, iv)
    }

    // 3DES 加密
    fun encrypt3DES(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val tripleDesKeySpec = SecretKeySpec(key, "DESede")
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val ips = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, tripleDesKeySpec, ips)

        return cipher.doFinal(data)
    }

    fun encrypt3DES2HexString(data: ByteArray, key: ByteArray, iv: ByteArray): String {
        val encryptData = encrypt3DES(data, key, iv)
        return bytes2HexString(encryptData)
    }

    fun encrypt3DES2Base64(data: ByteArray, key: ByteArray, iv: ByteArray): String {
        val encryptData = encrypt3DES(data, key, iv)
        return Base64.encodeToString(encryptData, Base64.NO_WRAP)
    }

    // 3DES 解密
    fun decrypt3DES(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val tripleDesKeySpec = SecretKeySpec(key, "DESede")
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val ips = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, tripleDesKeySpec, ips)

        return cipher.doFinal(data)
    }

    fun decryptHexString3DES(data: String, key: ByteArray, iv: ByteArray): ByteArray {
        val decryptData = hexString2Bytes(data)
        return decrypt3DES(decryptData, key, iv)
    }

    fun decryptBase64_3DES(data: String, key: ByteArray, iv: ByteArray): ByteArray {
        val decryptData = Base64.decode(data, Base64.NO_WRAP)
        return decrypt3DES(decryptData, key, iv)
    }

    // AES 加密
    fun encryptAES(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ips = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ips)

        return cipher.doFinal(data)
    }

    fun encryptAES2HexString(data: ByteArray, key: ByteArray, iv: ByteArray): String {
        val encryptData = encryptAES(data, key, iv)
        return bytes2HexString(encryptData)
    }

    fun encryptAES2Base64(data: ByteArray, key: ByteArray, iv: ByteArray): String {
        val encryptData = encryptAES(data, key, iv)
        return Base64.encodeToString(encryptData, Base64.NO_WRAP)
    }

    // AES 解密
    fun decryptAES(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ips = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ips)

        return cipher.doFinal(data)
    }

    fun decryptHexStringAES(data: String, key: ByteArray, iv: ByteArray): ByteArray {
        val decryptData = hexString2Bytes(data)
        return decryptAES(decryptData, key, iv)
    }

    fun decryptBase64AES(data: String, key: ByteArray, iv: ByteArray): ByteArray {
        val decryptData = Base64.decode(data, Base64.NO_WRAP)
        return decryptAES(decryptData, key, iv)
    }

    // RSA 加密
    fun encryptRSA(data: ByteArray, publicKey: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(publicKey)
        val publicKeyObj = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKeyObj)

        return cipher.doFinal(data)
    }

    fun encryptRSA2HexString(data: ByteArray, publicKey: ByteArray): String {
        val encryptData = encryptRSA(data, publicKey)
        return bytes2HexString(encryptData)
    }

    fun encryptRSA2Base64(data: ByteArray, publicKey: ByteArray): String {
        val encryptData = encryptRSA(data, publicKey)
        return Base64.encodeToString(encryptData, Base64.NO_WRAP)
    }

    // RSA 解密
    fun decryptRSA(data: ByteArray, privateKey: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(privateKey)
        val privateKeyObj = keyFactory.generatePrivate(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKeyObj)

        return cipher.doFinal(data)
    }

    fun decryptHexStringRSA(data: String, privateKey: ByteArray): ByteArray {
        val decryptData = hexString2Bytes(data)
        return decryptRSA(decryptData, privateKey)
    }

    fun decryptBase64RSA(data: String, privateKey: ByteArray): ByteArray {
        val decryptData = Base64.decode(data, Base64.NO_WRAP)
        return decryptRSA(decryptData, privateKey)
    }

    // RC4 加解密
    fun rc4(data: ByteArray, key: ByteArray): ByteArray {
        val state = IntArray(256)
        var x = 0
        var y = 0

        for (i in 0..255) {
            state[i] = i
        }

        for (i in 0..255) {
            x = (x + state[i] + key[i % key.size].toInt()) % 256
            val tmp = state[x]
            state[x] = state[i]
            state[i] = tmp
        }

        val result = ByteArray(data.size)
        for (i in data.indices) {
            x = (x + 1) % 256
            y = (state[x] + y) % 256
            val tmp = state[x]
            state[x] = state[y]
            state[y] = tmp
            result[i] = (data[i].toInt() xor state[(state[x] + state[y]) % 256]).toByte()
        }
        return result
    }

    // 字节数组转十六进制字符串
    private fun bytes2HexString(bytes: ByteArray): String {
        val builder = StringBuilder()
        for (b in bytes) {
            val hex = Integer.toHexString(b.toInt() and 0xFF)
            if (hex.length == 1) {
                builder.append('0')
            }
            builder.append(hex)
        }
        return builder.toString()
    }

    // 十六进制字符串转字节数组
    private fun hexString2Bytes(hexString: String): ByteArray {
        val len = hexString.length / 2
        val result = ByteArray(len)
        var i = 0
        while (i < len) {
            val index = i * 2
            val byteStr = hexString.substring(index, index + 2)
            result[i] = Integer.parseInt(byteStr, 16).toByte()
            i++
        }
        return result
    }




    /**
     * AES 加密
     *
     * @param data      待加密内容
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @return 返回Base64转码后的加密数据
     */
    @SuppressLint("GetInstance")
    fun simpleAesEncrypt(data: String, secretKey: String): String? {
        try {
            //创建密码器
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            //初始化为加密密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretKey))
            val encryptByte = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            // 将加密以后的数据进行 Base64 编码
            return base64Encode(encryptByte)
        } catch (e: Exception) {
            handleException(e)
        }
        return null
    }


    /**
     * AES 解密
     *
     * @param base64Data 加密的密文 Base64 字符串
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     */
    @SuppressLint("GetInstance")
    fun simpleAesDecrypt(base64Data: String?, secretKey: String): String? {
        try {
            if (base64Data.isNullOrBlank()) return null
            val data = base64Decode(base64Data)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            //设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey))
            //执行解密操作
            val result = cipher.doFinal(data)
            return String(result, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            handleException(e)
        }
        return null
    }

    /**
     * 使用密码获取 AES 秘钥
     */
    private fun getSecretKey(secretKey: String): SecretKeySpec {
        val secretKeyNew = toMakeKey(secretKey, 32,  "0")
        return SecretKeySpec(secretKeyNew.toByteArray(StandardCharsets.UTF_8), "AES")
    }

    /**
     * 如果 AES 的密钥小于 `length` 的长度，就对秘钥进行补位，保证秘钥安全。
     *
     * @param secretKey 密钥 key
     * @param length    密钥应有的长度
     * @param text      默认补的文本
     * @return 密钥
     */
    private fun toMakeKey(secretKey: String, length: Int = 32, text: String = "0"): String {
        // 获取密钥长度
        val strLen = secretKey.length
        // 判断长度是否小于应有的长度
        if (strLen > length) {
            LogUtils.e("ERR KEY")
            return ""
        }
        if (strLen == length) {
            return secretKey
        }
        // 补全位数
        val builder = StringBuilder()
        // 将key添加至builder中
        builder.append(secretKey)
        // 遍历添加默认文本
        for (i in 0 until length - strLen) {
            builder.append(text)
        }
        // 赋值
        return builder.toString()

    }

    /**
     * 将 Base64 字符串 解码成 字节数组
     */
    private fun base64Decode(data: String?): ByteArray {
        return Base64.decode(data, Base64.NO_WRAP)
    }

    fun base64DecodeToString(encodedString: String?): String? {
        if (encodedString.isNullOrBlank()) return null
        // 将字节数组转换为字符串
        return String(base64Decode(encodedString), Charsets.UTF_8)
    }


    /**
     * 将 字节数组 转换成 Base64 编码
     */
    private fun base64Encode(data: ByteArray?): String {
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }

    /**
     * 处理异常
     */
    private fun handleException(e: Exception) {
        e.printStackTrace()
    }

}
