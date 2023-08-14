package xyz.dcln.androidutils.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object TimeUtils {

    private const val DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss"
    enum class TimeUnit(val factor: Long) {
        MILLISECONDS(1),
        SECONDS(1000),
        MINUTES(60_000),
        HOURS(3_600_000),
        DAYS(86_400_000);

        fun convert(sourceDuration: Long, sourceUnit: TimeUnit) = sourceDuration * sourceUnit.factor / factor
    }
    // 获取安全的日期格式
    fun getSafeDateFormat(pattern: String = DEFAULT_PATTERN): SimpleDateFormat =
        SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }

    // 将时间戳转为时间字符串
    fun millis2String(millis: Long, pattern: String = DEFAULT_PATTERN): String =
        getSafeDateFormat(pattern).format(Date(millis))

    // 将时间字符串转为时间戳
    fun string2Millis(time: String, pattern: String = DEFAULT_PATTERN): Long? {
        return try {
            getSafeDateFormat(pattern).parse(time)?.time
        } catch (e: Exception) {
            null
        }
    }

    // 将时间字符串转为 Date 类型
    fun string2Date(time: String, pattern: String = DEFAULT_PATTERN): Date? =
        getSafeDateFormat(pattern).parse(time)

    // 将 Date 类型转为时间字符串
    fun date2String(date: Date, pattern: String = DEFAULT_PATTERN): String =
        getSafeDateFormat(pattern).format(date)

    // 将 Date 类型转为时间戳
    fun date2Millis(date: Date): Long = date.time

    // 将时间戳转为 Date 类型
    fun millis2Date(millis: Long): Date = Date(millis)

    // 获取两个时间差（单位：unit）
    fun getTimeSpan(time1: Long, time2: Long, unit: TimeUnit): Long =
        unit.convert(abs(time2 - time1), TimeUnit.MILLISECONDS)

    // 获取合适型两个时间差 (例如 "5天3小时2分1秒" 这样的格式)
    fun getFitTimeSpan(time1: Long, time2: Long, isChinese: Boolean = true): String {
        val totalMillis = abs(time2 - time1)
        val days = totalMillis / (24 * 60 * 60 * 1000)
        val hours = (totalMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (totalMillis % (60 * 60 * 1000)) / (60 * 1000)
        val seconds = (totalMillis % (60 * 1000)) / 1000

        return if (isChinese) {
            "${days}天${hours}小时${minutes}分${seconds}秒"
        } else {
            "${days}d ${hours}h ${minutes}m ${seconds}s"
        }
    }


    // 获取当前毫秒时间戳
    fun getNowMills(): Long = System.currentTimeMillis()

    // 获取当前时间字符串
    fun getNowString(pattern: String = DEFAULT_PATTERN): String = millis2String(getNowMills(), pattern)

    // 获取当前 Date
    fun getNowDate(): Date = Date()

    // 获取与当前时间的差（单位：unit）
    fun getTimeSpanByNow(time: Long, unit: TimeUnit): Long = getTimeSpan(time, getNowMills(), unit)

    // 获取合适型与当前时间的差
    fun getFitTimeSpanByNow(time: Long): String = getFitTimeSpan(time, getNowMills())

    // 获取友好型与当前时间的差 (例如：1小时前，1天前)
    fun getFriendlyTimeSpanByNow(time: Long, isChinese: Boolean = true): String {
        val span = getNowMills() - time
        return if (isChinese) {
            when {
                span < 60 * 1000 -> "刚刚"
                span < 60 * 60 * 1000 -> "${span / (60 * 1000)}分钟前"
                span < 24 * 60 * 60 * 1000 -> "${span / (60 * 60 * 1000)}小时前"
                else -> "${span / (24 * 60 * 60 * 1000)}天前"
            }
        } else {
            when {
                span < 60 * 1000 -> "Just now"
                span < 60 * 60 * 1000 -> "${span / (60 * 1000)} minutes ago"
                span < 24 * 60 * 60 * 1000 -> "${span / (60 * 60 * 1000)} hours ago"
                else -> "${span / (24 * 60 * 60 * 1000)} days ago"
            }
        }
    }


    // 获取与给定时间等于时间差的时间戳
    fun getMillis(time: Long, span: Long): Long = time + span

    // 获取与给定时间等于时间差的时间字符串
    fun getString(time: Long, span: Long, pattern: String = DEFAULT_PATTERN): String = millis2String(getMillis(time, span), pattern)

    // 获取与给定时间等于时间差的 Date
    fun getDate(time: Long, span: Long): Date = millis2Date(getMillis(time, span))

    // 获取与当前时间等于时间差的时间戳
    fun getMillisByNow(span: Long): Long = getMillis(getNowMills(), span)

    // 获取与当前时间等于时间差的时间字符串
    fun getStringByNow(span: Long, pattern: String = DEFAULT_PATTERN): String = millis2String(getMillisByNow(span), pattern)

    // 获取与当前时间等于时间差的 Date
    fun getDateByNow(span: Long): Date = millis2Date(getMillisByNow(span))

    // 判断是否今天
    fun isToday(time: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time }
        val cal2 = Calendar.getInstance()  // 默认即为当前时间
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // 判断是否闰年
    fun isLeapYear(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    // 获取中式星期
    fun getChineseWeek(time: Long): String {
        val weekDays = arrayOf("日", "一", "二", "三", "四", "五", "六")
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        val dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        return "星期${weekDays[dayIndex]}"
    }

    // 获取美式星期
    fun getUSWeek(time: Long): String {
        val weekDays = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        val dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        return weekDays[dayIndex]
    }

    // 判断是否上午
    fun isAm(time: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        return cal.get(Calendar.AM_PM) == Calendar.AM
    }

    // 判断是否下午
    fun isPm(time: Long): Boolean = !isAm(time)

    // 根据日历字段获取值
    fun getValueByCalendarField(time: Long, field: Int): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        return cal.get(field)
    }

    // 获取生肖
    fun getChineseZodiac(year: Int): String {
        val zodiacs = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
        return zodiacs[(year - 4) % 12]
    }

    // 获取星座
    fun getZodiac(month: Int, day: Int): String {
        val cutOffDates = arrayOf(20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22)
        val zodiacs = arrayOf("摩羯", "水瓶", "双鱼", "白羊", "金牛", "双子", "巨蟹", "狮子", "处女", "天秤", "天蝎", "射手")

        return if (day < cutOffDates[month - 1]) zodiacs[month - 1] else zodiacs[month % 12]
    }
}

