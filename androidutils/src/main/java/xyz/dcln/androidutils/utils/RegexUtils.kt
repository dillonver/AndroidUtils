package xyz.dcln.androidutils.utils


/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 4:05
 */
object RegexUtils {
    // 1. 验证邮箱
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*")
        return email.matches(emailRegex)
    }

    // 2. 验证 URL
    fun isValidUrl(url: String): Boolean {
        val urlRegex = Regex("^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(:[0-9]+)?(/.*)?\$")
        return url.matches(urlRegex)
    }

    // 3. 验证汉字
    fun containsChineseCharacters(text: String): Boolean {
        val chineseCharRegex = Regex("[\\u4E00-\\u9FA5]+")
        return text.contains(chineseCharRegex)
    }

    // 4. 验证 yyyy-MM-dd 格式的日期校验
    fun isValidDate(date: String): Boolean {
        val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}\$")
        return date.matches(dateRegex)
    }

    // 5. 验证 IP 地址
    fun isValidIpAddress(ip: String): Boolean {
        val ipRegex =
            Regex("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$")
        return ip.matches(ipRegex)
    }

    // 6. 验证双字节
    fun hasDoubleByteCharacters(text: String): Boolean {
        val doubleByteRegex = Regex("[^\\x00-\\xff]+")
        return text.contains(doubleByteRegex)
    }

    // 7. 验证整数
    fun isValidInteger(text: String): Boolean {
        val integerRegex = Regex("^-?\\d+\$")
        return text.matches(integerRegex)
    }

    // 8. 验证正整数
    fun isValidPositiveInteger(text: String): Boolean {
        val positiveIntegerRegex = Regex("^\\d+\$")
        return text.matches(positiveIntegerRegex)
    }

    // 9. 验证负整数
    fun isValidNegativeInteger(text: String): Boolean {
        val negativeIntegerRegex = Regex("^-\\d+\$")
        return text.matches(negativeIntegerRegex)
    }

    // 10. 验证非负整数
    fun isValidNonNegativeInteger(text: String): Boolean {
        val nonNegativeIntegerRegex = Regex("^\\d+\$")
        return text.matches(nonNegativeIntegerRegex)
    }

    // 11. 验证非正整数
    fun isValidNonPositiveInteger(text: String): Boolean {
        val nonPositiveIntegerRegex = Regex("^-\\d+\$")
        return text.matches(nonPositiveIntegerRegex)
    }

    // 12. 验证浮点数
    fun isValidFloat(text: String): Boolean {
        val floatRegex = Regex("^-?\\d+(\\.\\d+)?\$")
        return text.matches(floatRegex)
    }

    // 13. 验证正浮点数
    fun isValidPositiveFloat(text: String): Boolean {
        val positiveFloatRegex = Regex("^\\d+(\\.\\d+)?\$")
        return text.matches(positiveFloatRegex)
    }

    // 14. 验证负浮点数
    fun isValidNegativeFloat(text: String): Boolean {
        val negativeFloatRegex = Regex("^-\\d+(\\.\\d+)?\$")
        return text.matches(negativeFloatRegex)
    }

    // 15. 验证非负浮点数
    fun isValidNonNegativeFloat(text: String): Boolean {
        val nonNegativeFloatRegex = Regex("^\\d+(\\.\\d+)?\$")
        return text.matches(nonNegativeFloatRegex)
    }

    // 16. 验证非正浮点数
    fun isValidNonPositiveFloat(text: String): Boolean {
        val nonPositiveFloatRegex = Regex("^-\\d+(\\.\\d+)?\$")
        return text.matches(nonPositiveFloatRegex)
    }

    // 匹配ASCII范围内的符号
    fun isSymbol(input: String): Boolean {
        return Regex("^[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>?~]+$").matches(input)
    }

    //是否包含中文字符
    fun containsChinese(input: String): Boolean {
        return Regex("[\u4e00-\u9fa5]").containsMatchIn(input)
    }

    // 匹配0-9的数字
    fun isDigit(input: String): Boolean {
        return input.all { it.isDigit() }
    }

    // 匹配大小写字母
    fun isLetter(input: String): Boolean {
        return input.all { it.isLetter() }
    }


}