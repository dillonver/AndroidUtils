package xyz.dcln.androidutils.utils


/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 4:05
 */
object RegexUtils {
    // 邮箱正则
    private val EMAIL = Regex("\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*")
    // URL正则
    private val URL = Regex("^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(:[0-9]+)?(/.*)?\$")
    // 汉字正则
    private val CHINESE_CHARS = Regex("[\\u4E00-\\u9FA5]+")
    // yyyy-MM-dd日期格式正则
    private val DATE = Regex("^\\d{4}-\\d{2}-\\d{2}\$")
    // IP地址正则
    private val IP = Regex("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$")
    // 双字节字符正则
    private val DOUBLE_BYTE = Regex("[^\\x00-\\xff]+")
    // 整数正则
    private val INTEGER = Regex("^-?\\d+\$")
    // 正整数正则
    private val POSITIVE_INTEGER = Regex("^\\d+\$")
    // 浮点数正则
    private val FLOAT = Regex("^-?\\d+(\\.\\d+)?\$")
    // 正浮点数正则
    private val POSITIVE_FLOAT = Regex("^\\d+(\\.\\d+)?\$")
    // 符号正则
    private val SYMBOL = Regex("^[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>?~]+$")
    // 数字正则
    private val DIGIT = Regex("^\\d+$")
    // 字母正则
    private val LETTER = Regex("^[a-zA-Z]+$")

    // 判断是否为合法的邮箱地址
    fun isValidEmail(email: String) = EMAIL.matches(email)

    // 判断是否为合法的URL
    fun isValidUrl(url: String) = URL.matches(url)

    // 判断是否包含汉字
    fun containsChineseCharacters(text: String) = CHINESE_CHARS.containsMatchIn(text)

    // 判断是否为yyyy-MM-dd日期格式
    fun isValidDate(date: String) = DATE.matches(date)

    // 判断是否为合法的IP地址
    fun isValidIpAddress(ip: String) = IP.matches(ip)

    // 判断是否包含双字节字符
    fun hasDoubleByteCharacters(text: String) = DOUBLE_BYTE.containsMatchIn(text)

    // 判断是否为整数
    fun isValidInteger(text: String) = INTEGER.matches(text)

    // 判断是否为正整数
    fun isValidPositiveInteger(text: String) = POSITIVE_INTEGER.matches(text)

    // 判断是否为浮点数
    fun isValidFloat(text: String) = FLOAT.matches(text)

    // 判断是否为正浮点数
    fun isValidPositiveFloat(text: String) = POSITIVE_FLOAT.matches(text)

    // 判断是否为符号
    fun isSymbol(input: String) = SYMBOL.matches(input)

    // 判断是否全为数字
    fun isDigit(input: String) = DIGIT.matches(input)

    // 判断是否全为字母
    fun isLetter(input: String) = LETTER.matches(input)
}

