package xyz.dcln.androidutils.utils


import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import java.io.Closeable
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * Description:
 * Author: Dillon
 * Date: 2023/4/6 21:20
 */

object IntervalUtils {

    /**
     * 创建IntervalController对象，但不启动定时器。
     *
     * @param end 结束计时的时间点（以单位为unit的时间长度表示）。-1代表永不停止。end比start大,正计时，反之倒计时。
     * @param period 定时器周期的长度（以单位为unit的时间长度表示）。
     * @param unit 时间单位，如 TimeUnit.MILLISECONDS、TimeUnit.SECONDS 等。
     * @param start 起始计时的时间点（以单位为unit的时间长度表示），默认为0。
     * @param initialDelay 初始延迟时间（以单位为unit的时间长度表示），默认为0。
     * @param onTick 每个周期触发的回调函数，接受一个 IntervalController 对象和当前计时值作为参数，默认为null。
     * @param onFinish 计时结束时触发的回调函数，接受最终计时值作为参数，默认为null。
     * @return 创建的 IntervalController 对象。
     */
    fun create(
        end: Long,
        period: Long,
        unit: TimeUnit,
        start: Long = 0,
        initialDelay: Long = 0,
        onTick: ((controller: IntervalController, count: Long) -> Unit)? = null,
        onFinish: ((remain: Long) -> Unit)? = null,
    ): IntervalController {
        return IntervalController(end, period, unit, start, initialDelay, onTick, onFinish)
    }

    /**
     * 直接启动定时器。
     *
     * @param end 结束计时的时间点（以单位为unit的时间长度表示）。-1代表永不停止。end比start大,正计时，反之倒计时。
     * @param period 定时器周期的长度（以单位为unit的时间长度表示）。
     * @param unit 时间单位，如 TimeUnit.MILLISECONDS、TimeUnit.SECONDS 等。
     * @param start 起始计时的时间点（以单位为unit的时间长度表示），默认为0。
     * @param initialDelay 初始延迟时间（以单位为unit的时间长度表示），默认为0。
     * @param onTick 每个周期触发的回调函数，接受一个 IntervalController 对象和当前计时值作为参数，默认为null。
     * @param onFinish 计时结束时触发的回调函数，接受最终计时值作为参数，默认为null。
     * @param lifecycleOwner 用于绑定生命周期的 LifecycleOwner 对象，默认为null。
     */
    fun interval(
        end: Long,
        period: Long,
        unit: TimeUnit,
        start: Long = 0,
        initialDelay: Long = 0,
        onTick: ((controller: IntervalController, count: Long) -> Unit)? = null,
        onFinish: ((remain: Long) -> Unit)? = null,
        lifecycleOwner: LifecycleOwner? = null
    ) {
        val interval = create(end, period, unit, start, initialDelay, onTick, onFinish)
        if (lifecycleOwner != null) interval.life(lifecycleOwner)
        interval.start()
    }

    /**
     * 定时器控制器的内部类。
     *
     * @property end 结束计时的时间点（以单位为unit的时间长度表示）。
     * @property period 定时器周期的长度（以单位为unit的时间长度表示）。
     * @property unit 时间单位，如 TimeUnit.MILLISECONDS、TimeUnit.SECONDS 等。
     * @property start 起始计时的时间点（以单位为unit的时间长度表示）。
     * @property initialDelay 初始延迟时间（以单位为unit的时间长度表示）。
     * @property onTick 每个周期触发的回调函数，接受一个 IntervalController 对象和当前计时值作为参数，默认为null。
     * @property onFinish 计时结束时触发的回调函数，接受最终计时值作为参数，默认为null。
     */
    class IntervalController(
        val end: Long,
        private val period: Long,
        private val unit: TimeUnit,
        val start: Long,
        private val initialDelay: Long,
        private val onTick: ((controller: IntervalController, count: Long) -> Unit)? = null,
        private val onFinish: ((remain: Long) -> Unit)? = null
    ) : Serializable, Closeable {
        private var countTime = 0L
        private var delay = 0L
        private var scope: CoroutineScope? = null
        private lateinit var ticker: ReceiveChannel<Unit>

        var count = start
        var state = IntervalStatus.STATE_IDLE
            private set

        /**
         * 启动定时器。
         *
         * @return 当前 IntervalController 对象。
         */
        fun start() = apply {
            if (state == IntervalStatus.STATE_ACTIVE) return this
            state = IntervalStatus.STATE_ACTIVE
            count = start
            launch()
        }

        /**
         * 停止定时器。
         */
        fun stop() {
            if (state == IntervalStatus.STATE_IDLE) return
            scope?.cancel()
            state = IntervalStatus.STATE_IDLE
            onFinish?.invoke(count)
        }

        /**
         * 取消定时器。
         */
        fun cancel() {
            if (state == IntervalStatus.STATE_IDLE) return
            scope?.cancel()
            state = IntervalStatus.STATE_IDLE
        }

        /**
         * 关闭定时器。
         */
        override fun close() = cancel()

        /**
         * 暂停定时器。
         */
        fun pause() {
            if (state != IntervalStatus.STATE_ACTIVE) return
            scope?.cancel()
            state = IntervalStatus.STATE_PAUSE
            delay = System.currentTimeMillis() - countTime
        }

        /**
         * 恢复定时器。
         */
        fun resume() {
            if (state != IntervalStatus.STATE_PAUSE) return
            state = IntervalStatus.STATE_ACTIVE
            launch(delay)
        }

        /**
         * 重置定时器。
         */
        fun reset() {
            count = start
            delay = unit.toMillis(initialDelay)
            scope?.cancel()
            if (state == IntervalStatus.STATE_ACTIVE) launch()
        }

        /**
         * 提前结束定时器。
         */
        fun finishEarly() {
            cancel()
            onFinish?.invoke(count)
        }

        /**
         * 将定时器绑定到生命周期。
         *
         * @param lifecycleOwner 绑定生命周期的 LifecycleOwner 对象。
         * @return 当前 IntervalController 对象。
         */
        fun life(lifecycleOwner: LifecycleOwner) = apply {
            val lifecycle: Lifecycle = when (lifecycleOwner) {
                is Fragment -> lifecycleOwner.viewLifecycleOwner.lifecycle
                else -> lifecycleOwner.lifecycle
            }

            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_DESTROY -> cancel()
                        else -> {
                        }
                    }
                }
            })
        }

        /**
         * 将定时器绑定到生命周期。
         *
         * @param lifecycle 绑定生命周期的 Lifecycle 对象。
         * @return 当前 IntervalController 对象。
         */
        fun life(lifecycle: Lifecycle) = apply {
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_DESTROY -> cancel()
                        else -> {
                        }
                    }
                }
            })
        }

        @OptIn(ObsoleteCoroutinesApi::class)
        private fun launch(delay: Long = unit.toMillis(initialDelay)) {
            scope = CoroutineScope(Dispatchers.Main)
            scope?.launch {
                ticker = ticker(unit.toMillis(period), delay, mode = TickerMode.FIXED_DELAY)
                for (unit in ticker) {
                    onTick?.invoke(this@IntervalController, count)
                    if (end != -1L && count == end) {
                        scope?.cancel()
                        state = IntervalStatus.STATE_IDLE
                        onFinish?.invoke(count)
                    }
                    if (end != -1L && start > end) count-- else count++
                    countTime = System.currentTimeMillis()
                }
            }
        }

        /**
         * 定时器的状态枚举类。
         */
        enum class IntervalStatus {
            STATE_ACTIVE, STATE_IDLE, STATE_PAUSE
        }
    }

    /**
     * 在 LifecycleOwner 上启动定时器。
     *
     * @param end 结束计时的时间点（以单位为unit的时间长度表示）。-1代表永不停止。end比start大,正计时，反之倒计时。
     * @param period 定时器周期的长度（以单位为unit的时间长度表示）。
     * @param unit 时间单位，如 TimeUnit.MILLISECONDS、TimeUnit.SECONDS 等。
     * @param start 起始计时的时间点（以单位为unit的时间长度表示），默认为0。
     * @param initialDelay 初始延迟时间（以单位为unit的时间长度表示），默认为0。
     * @param onTick 每个周期触发的回调函数，接受一个 IntervalController 对象和当前计时值作为参数，默认为null。
     * @param onFinish 计时结束时触发的回调函数，接受最终计时值作为参数，默认为null。
     */
    fun LifecycleOwner.interval(
        end: Long,
        period: Long,
        unit: TimeUnit,
        start: Long = 0,
        initialDelay: Long = 0,
        onTick: ((controller: IntervalController, count: Long) -> Unit)? = null,
        onFinish: ((remain: Long) -> Unit)? = null,
    ) {
        create(end, period, unit, start, initialDelay, onTick, onFinish).life(this).start()
    }
}