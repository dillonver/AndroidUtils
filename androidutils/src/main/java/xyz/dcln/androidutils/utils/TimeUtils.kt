package xyz.dcln.androidutils.utils

import kotlin.math.abs

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    // 默认的日期时间格式
    private const val DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss"

    /**
     * 时间单位枚举类，定义了各单位的毫秒数以及单位转换方法。
     */
    enum class TimeUnit(val factor: Long) {
        MILLISECONDS(1),
        SECONDS(1000),
        MINUTES(60_000),
        HOURS(3_600_000),
        DAYS(86_400_000);

        /**
         * 将指定时间单位的时长转换为当前单位的时长。
         * @param sourceDuration 源时长
         * @param sourceUnit 源单位
         * @return 当前单位的时长
         */
        fun convert(sourceDuration: Long, sourceUnit: TimeUnit) =
            sourceDuration * sourceUnit.factor / factor
    }

    /**
     * 获取安全的日期格式化对象，默认使用默认格式。
     * @param pattern 自定义的日期时间格式，默认为 DEFAULT_PATTERN
     * @return SimpleDateFormat 对象
     */
    fun getSafeDateFormat(pattern: String = DEFAULT_PATTERN): SimpleDateFormat =
        SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }

    /**
     * 将时间戳转换为指定格式的时间字符串。
     * @param millis 时间戳
     * @param pattern 自定义的日期时间格式，默认为 DEFAULT_PATTERN
     * @return 格式化后的时间字符串
     */
    fun millis2String(millis: Long, pattern: String = DEFAULT_PATTERN): String =
        getSafeDateFormat(pattern).format(Date(millis))

    /**
     * 将指定格式的时间字符串转换为时间戳。
     * @param time 时间字符串
     * @param pattern 自定义的日期时间格式，默认为 DEFAULT_PATTERN
     * @return 时间戳，转换失败返回 null
     */
    fun string2Millis(time: String, pattern: String = DEFAULT_PATTERN): Long? =
        try {
            getSafeDateFormat(pattern).parse(time)?.time
        } catch (e: Exception) {
            null
        }

    /**
     * 将指定格式的时间字符串转换为 Date 对象。
     * @param time 时间字符串
     * @param pattern 自定义的日期时间格式，默认为 DEFAULT_PATTERN
     * @return Date 对象，转换失败返回 null
     */
    fun string2Date(time: String, pattern: String = DEFAULT_PATTERN): Date? =
        getSafeDateFormat(pattern).parse(time)

    /**
     * 将 Date 对象转换为指定格式的时间字符串。
     * @param date Date 对象
     * @param pattern 自定义的日期时间格式，默认为 DEFAULT_PATTERN
     * @return 格式化后的时间字符串
     */
    fun date2String(date: Date, pattern: String = DEFAULT_PATTERN): String =
        getSafeDateFormat(pattern).format(date)

    /**
     * 将 Date 对象转换为时间戳。
     * @param date Date 对象
     * @return 时间戳
     */
    fun date2Millis(date: Date): Long = date.time

    /**
     * 将时间戳转换为 Date 对象。
     * @param millis 时间戳
     * @return Date 对象
     */
    fun millis2Date(millis: Long): Date = Date(millis)

    /**
     * 获取两个时间点的时间差。
     * @param time1 第一个时间戳
     * @param time2 第二个时间戳
     * @param unit 时间单位，参见 TimeUnit 枚举
     * @return 时间差，单位为 unit
     */
    fun getTimeSpan(time1: Long, time2: Long, unit: TimeUnit): Long =
        unit.convert(abs(time2 - time1), TimeUnit.MILLISECONDS)

    /**
     * 获取友好型的时间差描述，例如 "5天3小时2分1秒" 或 "5 days 3 hours 2 minutes 1 second"。
     * @param time1 第一个时间戳
     * @param time2 第二个时间戳
     * @param isChinese 是否使用中文描述，默认为 true
     * @return 友好型时间差描述字符串
     */
    fun getFitTimeSpan(time1: Long, time2: Long, isChinese: Boolean = true): String {
        val totalMillis = abs(time2 - time1)
        val days = totalMillis / (24 * 60 * 60 * 1000)
        val hours = (totalMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (totalMillis % (60 * 60 * 1000)) / (60 * 1000)
        val seconds = (totalMillis % (60 * 1000)) / 1000

        return if (isChinese) {
            "${days}天${hours}小时${minutes}分${seconds}秒"
        } else {
            "$days days $hours hours $minutes minutes $seconds seconds"
        }
    }

    /**
     * 获取当前毫秒级时间戳。
     * @return 当前毫秒级时间戳
     */
    fun getNowMills(): Long = System.currentTimeMillis()

    /**
     * 获取当前时间的格式化字符串，默认使用 DEFAULT_PATTERN 格式。
     * @param pattern 自定义的日期时间格式，默认为 DEFAULT_PATTERN
     * @return 当前时间的格式化字符串
     */
    fun getNowString(pattern: String = DEFAULT_PATTERN): String =
        millis2String(getNowMills(), pattern)

    /**
     * 获取当前时间的 Date 对象。
     * @return 当前时间的 Date 对象
     */
    fun getNowDate(): Date = Date()

    /**
     * 获取当前时间与给定时间的时间差。
     * @param time 给定时间戳
     * @param unit 时间单位，参见 TimeUnit 枚举
     * @return 当前时间与给定时间的时间差，单位为 unit
     */
    fun getTimeSpanByNow(time: Long, unit: TimeUnit): Long = getTimeSpan(time, getNowMills(), unit)

    /**
     * 获取当前时间与给定时间的友好型时间差描述。
     * @param time 给定时间戳
     * @param isChinese 是否使用中文描述，默认为 true
     * @param showYearAfter3day 超过三天是否显示年份
     * @param showTimeAfter3day 超过三天是否显示时间
     * @return 当前时间与给定时间的友好型时间差描述
     */
    fun getFriendlyTimeSpanByNow(
        time: Long,
        isChinese: Boolean = true,
        showYearAfter3day: Boolean = true,
        showTimeAfter3day: Boolean = true,
    ): String {
        return if (isChinese) {
            getFriendlyTimeChineseSpanByNow(
                time = time,
                showYearAfter3day = showYearAfter3day,
                showTimeAfter3day = showTimeAfter3day
            )
        } else {
            getFriendlyTimeDefaultSpanByNow(
                time = time,
                showYearAfter3day = showYearAfter3day,
                showTimeAfter3day = showTimeAfter3day
            )
        }
    }

    // 获取友好型与当前时间的中文描述
    private fun getFriendlyTimeChineseSpanByNow(
        time: Long,
        showYearAfter3day: Boolean = true,
        showTimeAfter3day: Boolean = true,
    ): String {
        val span = getNowMills() - time
        val patternP1 = "yyyy年"
        val patternP2 = "MM月dd日"
        val patternP3 = " HH:mm"
        val targetPattern = if (showYearAfter3day) {
            patternP1
        } else {
            ""
        } + patternP2 + if (showTimeAfter3day) {
            patternP3
        } else {
            ""
        }

        return when {
            span < 60 * 1000 -> "刚刚"
            span < 60 * 60 * 1000 -> "${span / (60 * 1000)}分钟前"
            span < 24 * 60 * 60 * 1000 -> "${span / (60 * 60 * 1000)}小时前"
            span < 3 * 24 * 60 * 60 * 1000 -> "${span / (24 * 60 * 60 * 1000)}天前"
            else -> SimpleDateFormat(targetPattern, Locale.CHINESE).format(Date(time))
        }
    }

    // 获取友好型与当前时间的默认描述
    private fun getFriendlyTimeDefaultSpanByNow(
        time: Long,
        showYearAfter3day: Boolean = true,
        showTimeAfter3day: Boolean = true,
    ): String {
        val span = getNowMills() - time
        val patternP1 = "yyyy-"
        val patternP2 = "MM-dd"
        val patternP3 = " HH:mm"
        val targetPattern = if (showYearAfter3day) {
            patternP1
        } else {
            ""
        } + patternP2 + if (showTimeAfter3day) {
            patternP3
        } else {
            ""
        }

        return when {
            span < 60 * 1000 -> "Just now"
            span < 60 * 60 * 1000 -> "${span / (60 * 1000)} minutes ago"
            span < 24 * 60 * 60 * 1000 -> "${span / (60 * 60 * 1000)} hours ago"
            span < 3 * 24 * 60 * 60 * 1000 -> "${span / (24 * 60 * 60 * 1000)} days ago"
            else -> SimpleDateFormat(targetPattern, Locale.ENGLISH).format(Date(time))
        }
    }

    /**
     * 获取与给定时间相差指定时长后的时间戳。
     * @param time 给定时间戳
     * @param span 时间差，单位为毫秒
     * @return 与给定时间相差指定时长后的时间戳
     */
    fun getMillis(time: Long, span: Long): Long = time + span

    /**
     * 获取与给定时间相差指定时长后的时间字符串。
     * @param time 给定时间戳
     * @param span 时间差，单位为毫秒
     * @param pattern 自定义的日期时间格式，默认为 DEFAULT_PATTERN
     * @return 与给定时间相差指定时长后的时间字符串
     */
    fun getString(time: Long, span: Long, pattern: String = DEFAULT_PATTERN): String =
        millis2String(getMillis(time, span), pattern)

    /**
     * 获取与给定时间相差指定时长后的 Date 对象。
     * @param time 给定时间戳
     * @param span 时间差，单位为毫秒
     * @return 与给定时间相差指定时长后的 Date 对象
     */
    fun getDate(time: Long, span: Long): Date = millis2Date(getMillis(time, span))

    /**
     * 获取与当前时间相差指定时长后的时间戳。
     * @param span 时间差，单位为毫秒
     * @return 与当前时间相差指定时长后的时间戳
     */
    fun getMillisByNow(span: Long): Long = getMillis(getNowMills(), span)

    /**
     * 获取与当前时间相差指定时长后的时间字符串。
     * @param span 时间差，单位为毫秒
     * @param pattern 自定义的日期时间格式，默认为 DEFAULT_PATTERN
     * @return 与当前时间相差指定时长后的时间字符串
     */
    fun getStringByNow(span: Long, pattern: String = DEFAULT_PATTERN): String =
        millis2String(getMillisByNow(span), pattern)

    /**
     * 获取与当前时间相差指定时长后的 Date 对象。
     * @param span 时间差，单位为毫秒
     * @return 与当前时间相差指定时长后的 Date 对象
     */
    fun getDateByNow(span: Long): Date = millis2Date(getMillisByNow(span))

    /**
     * 判断给定时间是否为今天。
     * @param time 给定时间戳
     * @return true 如果给定时间为今天，否则 false
     */
    fun isToday(time: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time }
        val cal2 = Calendar.getInstance()  // 默认即为当前时间
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(
            Calendar.DAY_OF_YEAR
        )
    }

    /**
     * 判断给定年份是否为闰年。
     * @param year 年份
     * @return true 如果是闰年，否则 false
     */
    fun isLeapYear(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    /**
     * 获取中式星期几的描述。
     * @param time 时间戳
     * @return 中式星期几的描述，例如 "星期一"、"星期二"
     */
    fun getChineseWeek(time: Long): String {
        val weekDays = arrayOf("日", "一", "二", "三", "四", "五", "六")
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        val dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        return "星期${weekDays[dayIndex]}"
    }

    /**
     * 获取美式星期几的描述。
     * @param time 时间戳
     * @return 美式星期几的描述，例如 "Sunday"、"Monday"
     */
    fun getUSWeek(time: Long): String {
        val weekDays =
            arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        val dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        return weekDays[dayIndex]
    }

    /**
     * 判断给定时间是否为上午。
     * @param time 时间戳
     * @return true 如果是上午，否则 false
     */
    fun isAm(time: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        return cal.get(Calendar.AM_PM) == Calendar.AM
    }

    /**
     * 判断给定时间是否为下午。
     * @param time 时间戳
     * @return true 如果是下午，否则 false
     */
    fun isPm(time: Long): Boolean = !isAm(time)

    /**
     * 根据日历字段获取指定时间的值。
     * @param time 时间戳
     * @param field 日历字段，例如 Calendar.YEAR、Calendar.MONTH
     * @return 指定日历字段的值
     */
    fun getValueByCalendarField(time: Long, field: Int): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        return cal.get(field)
    }

    /**
     * 根据年份获取对应的生肖。
     * @param year 年份
     * @return 对应的生肖，例如 "鼠"、"牛"
     */
    fun getChineseZodiac(year: Int): String {
        val zodiacs =
            arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
        return zodiacs[(year - 4) % 12]
    }

    /**
     * 根据月份和日期获取对应的星座。
     * @param month 月份，从 1 开始
     * @param day 日期
     * @return 对应的星座，例如 "摩羯"、"水瓶"
     */
    fun getZodiac(month: Int, day: Int): String {
        val cutOffDates = arrayOf(20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22)
        val zodiacs = arrayOf(
            "摩羯", "水瓶", "双鱼", "白羊", "金牛", "双子",
            "巨蟹", "狮子", "处女", "天秤", "天蝎", "射手"
        )

        return if (day < cutOffDates[month - 1]) zodiacs[month - 1] else zodiacs[month % 12]
    }


    /**
     * 获取指定日期的开始时间（00:00:00.000）
     */
    private fun getStartOfDay(calendar: Calendar): Date {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * 获取指定日期的结束时间（23:59:59.999）
     */
    private fun getEndOfDay(calendar: Calendar): Date {
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    /**
     * 获取今天的开始日期
     * @return 今天的开始日期对象
     */
    fun getTodayStartDate(): Date = getStartOfDay(Calendar.getInstance())

    /**
     * 获取今天的开始时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return 今天的开始时间字符串
     */
    fun getTodayStart(): String = date2String(getTodayStartDate())

    /**
     * 获取今天的结束日期
     * @return 今天的结束日期对象
     */
    fun getTodayEndDate(): Date = getEndOfDay(Calendar.getInstance())

    /**
     * 获取今天的结束时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return 今天的结束时间字符串
     */
    fun getTodayEnd(): String = date2String(getTodayEndDate())

    /**
     * 获取昨天的开始日期
     * @return 昨天的开始日期对象
     */
    fun getYesterdayStartDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return getStartOfDay(calendar)
    }

    /**
     * 获取昨天的开始时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return 昨天的开始时间字符串
     */
    fun getYesterdayStart(): String = date2String(getYesterdayStartDate())

    /**
     * 获取昨天的结束日期
     * @return 昨天的结束日期对象
     */
    fun getYesterdayEndDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return getEndOfDay(calendar)
    }

    /**
     * 获取昨天的结束时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return 昨天的结束时间字符串
     */
    fun getYesterdayEnd(): String = date2String(getYesterdayEndDate())

    /**
     * 获取本周的开始日期（周一为第一天）
     * @return 本周的开始日期对象
     */
    fun getWeekStartDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return getStartOfDay(calendar)
    }

    /**
     * 获取本周的开始时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return 本周的开始时间字符串
     */
    fun getWeekStart(): String = date2String(getWeekStartDate())

    /**
     * 获取本周的结束日期（周日为最后一天）
     * @return 本周的结束日期对象
     */
    fun getWeekEndDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek + 6)
        return getEndOfDay(calendar)
    }

    /**
     * 获取本周的结束时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return 本周的结束时间字符串
     */
    fun getWeekEnd(): String = date2String(getWeekEndDate())

    /**
     * 获取本月的开始日期
     * @return 本月的开始日期对象
     */
    fun getMonthStartDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return getStartOfDay(calendar)
    }

    /**
     * 获取本月的开始时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return 本月的开始时间字符串
     */
    fun getMonthStart(): String = date2String(getMonthStartDate())

    /**
     * 获取本月的结束日期
     * @return 本月的结束日期对象
     */
    fun getMonthEndDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return getEndOfDay(calendar)
    }

    /**
     * 获取本月的结束时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return 本月的结束时间字符串
     */
    fun getMonthEnd(): String = date2String(getMonthEndDate())
}

