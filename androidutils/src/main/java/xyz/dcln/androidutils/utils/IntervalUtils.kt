package xyz.dcln.androidutils.utils

import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.TimeUnit

/**
 * Description:
 * Author: Dillon
 * Date: 2025/3/17 21:20
 */
/**
 * 已废弃的定时器工具，请使用 [TimerUtils] 替代。
 */
@Deprecated(
    message = "请使用 TimerUtils 替代 IntervalUtils",
    replaceWith = ReplaceWith("TimerUtils")
)
object IntervalUtils {
    /**
     * 已废弃的创建方法，请使用 [TimerUtils.build]。
     */
    @Deprecated(
        message = "请使用 TimerUtils.build 替代",
        replaceWith = ReplaceWith("TimerUtils.build(targetValue, interval, timeUnit, initialValue, initialDelay, activeOnly, onTick, onComplete)")
    )
    fun create(
        end: Long,
        period: Long,
        unit: TimeUnit,
        start: Long = 0,
        initialDelay: Long = 0,
        onTick: ((controller: TimerUtils.TimerController, count: Long) -> Unit)? = null,
        onFinish: ((count: Long) -> Unit)? = null
    ): TimerUtils.TimerController {
        return TimerUtils.build(end, period, unit, start, initialDelay, false, onTick, onFinish)
    }

    /**
     * 已废弃的启动方法，请使用 [TimerUtils.startTimer]。
     */
    @Deprecated(
        message = "请使用 TimerUtils.startTimer 替代",
        replaceWith = ReplaceWith("TimerUtils.startTimer(targetValue, interval, timeUnit, initialValue, initialDelay, activeOnly, onTick, onComplete, lifecycleOwner)")
    )
    fun interval(
        end: Long,
        period: Long,
        unit: TimeUnit,
        start: Long = 0,
        initialDelay: Long = 0,
        onTick: ((controller: TimerUtils.TimerController, count: Long) -> Unit)? = null,
        onFinish: ((count: Long) -> Unit)? = null,
        lifecycleOwner: LifecycleOwner? = null
    ) {
        TimerUtils.startTimer(
            end,
            period,
            unit,
            start,
            initialDelay,
            false,
            onTick,
            onFinish,
            lifecycleOwner
        )
    }
}