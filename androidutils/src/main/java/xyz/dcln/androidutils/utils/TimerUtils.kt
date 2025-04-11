package xyz.dcln.androidutils.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import java.io.Closeable
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * author : 刁成龙
 * e-mail : 1304937@qq.com
 * time   : 2025/3/17
 * 用于创建和管理基于间隔的定时器的工具对象，支持生命周期感知。
 */
object TimerUtils {

    /**
     * 创建一个新的定时器控制器，但不启动。
     *
     * @param targetValue 目标计数值（以 [timeUnit] 为单位）。使用 -1 表示无限时长。
     *                    若大于 [initialValue]，正向计数；否则，反向计数。
     * @param interval 每次计数的时间间隔（以 [timeUnit] 为单位）。
     * @param timeUnit 时间单位，用于 [targetValue]、[interval] 和 [initialDelay]。
     * @param initialValue 定时器的起始值（默认：0）。
     * @param initialDelay 首次计数前的延迟时间（以 [timeUnit] 为单位，默认：0）。
     * @param activeOnly 若为 true，定时器仅在生命周期处于活跃状态（即 Activity/Fragment 处于 RESUMED 状态，前台可见）时运行，
     *                   在暂停（PAUSED）或停止（STOPPED）时自动暂停，并在恢复到 RESUMED 时自动继续，默认：false。
     *                   注意：此参数仅影响生命周期触发的暂停和恢复，手动调用 pause() 和 resume() 不受此限制。
     * @param onTick 每次计数时调用的回调函数，接收控制器和当前值（可选）。
     * @param onComplete 定时器完成时调用的回调函数，接收最终值（可选）。
     * @return 新创建的 [TimerController] 实例。
     */
    fun build(
        targetValue: Long,
        interval: Long,
        timeUnit: TimeUnit,
        initialValue: Long = 0,
        initialDelay: Long = 0,
        activeOnly: Boolean = false,
        onTick: ((controller: TimerController, currentValue: Long) -> Unit)? = null,
        onComplete: ((finalValue: Long) -> Unit)? = null
    ): TimerController {
        return TimerController(targetValue, interval, timeUnit, initialValue, initialDelay, activeOnly, onTick, onComplete)
    }

    /**
     * 创建并立即启动一个定时器。
     *
     * @param targetValue 目标计数值（以 [timeUnit] 为单位）。使用 -1 表示无限时长。
     * @param interval 每次计数的时间间隔（以 [timeUnit] 为单位）。
     * @param timeUnit 时间单位，用于 [targetValue]、[interval] 和 [initialDelay]。
     * @param initialValue 定时器的起始值（默认：0）。
     * @param initialDelay 首次计数前的延迟时间（以 [timeUnit] 为单位，默认：0）。
     * @param activeOnly 若为 true，定时器仅在生命周期处于活跃状态（即 Activity/Fragment 处于 RESUMED 状态，前台可见）时运行，
     *                   在暂停（PAUSED）或停止（STOPPED）时自动暂停，并在恢复到 RESUMED 时自动继续，默认：false。
     *                   注意：此参数仅影响生命周期触发的暂停和恢复，手动调用 pause() 和 resume() 不受此限制。
     * @param onTick 每次计数时调用的回调函数，接收控制器和当前值（可选）。
     * @param onComplete 定时器完成时调用的回调函数，接收最终值（可选）。
     * @param lifecycleOwner 可选的生命周期拥有者，用于绑定定时器生命周期（默认：null）。
     */
    fun startTimer(
        targetValue: Long,
        interval: Long,
        timeUnit: TimeUnit,
        initialValue: Long = 0,
        initialDelay: Long = 0,
        activeOnly: Boolean = false,
        onTick: ((controller: TimerController, currentValue: Long) -> Unit)? = null,
        onComplete: ((finalValue: Long) -> Unit)? = null,
        lifecycleOwner: LifecycleOwner? = null
    ) {
        val timer = build(targetValue, interval, timeUnit, initialValue, initialDelay, activeOnly, onTick, onComplete)
        lifecycleOwner?.let { timer.bindLifecycle(it) }
        timer.start()
    }

    /**
     * 用于管理周期性定时器的控制器，支持生命周期绑定。
     */
    class TimerController(
        val targetValue: Long,
        private val interval: Long,
        private val timeUnit: TimeUnit,
        val initialValue: Long,
        private val initialDelay: Long,
        private val activeOnly: Boolean,
        private val onTick: ((controller: TimerController, currentValue: Long) -> Unit)? = null,
        private val onComplete: ((finalValue: Long) -> Unit)? = null
    ) : Serializable, Closeable {
        private var lastTickTime = 0L
        private var remainingDelay = 0L
        private var scope: CoroutineScope? = null
        private var lifecycleObserver: LifecycleEventObserver? = null

        var currentValue = initialValue
        var status = TimerStatus.IDLE
            private set

        /**
         * 启动定时器（如果尚未运行）。
         *
         * @return 当前 [TimerController] 实例，用于链式调用。
         */
        fun start() = apply {
            if (status == TimerStatus.RUNNING) return this
            status = TimerStatus.RUNNING
            currentValue = initialValue
            launch()
        }

        /**
         * 停止定时器并触发完成回调。
         */
        fun stop() {
            if (status == TimerStatus.IDLE) return
            cleanup()
            onComplete?.invoke(currentValue)
        }

        /**
         * 取消定时器，不触发完成回调。
         */
        fun cancel() {
            if (status == TimerStatus.IDLE) return
            cleanup()
        }

        /**
         * 关闭定时器（等同于 [cancel]）。
         */
        override fun close() = cancel()

        /**
         * 暂停定时器，保留当前状态。
         */
        fun pause() {
            if (status != TimerStatus.RUNNING) return
            scope?.cancel()
            status = TimerStatus.PAUSED
            remainingDelay = System.currentTimeMillis() - lastTickTime
        }

        /**
         * 恢复暂停的定时器。
         */
        fun resume() {
            if (status != TimerStatus.PAUSED) return
            status = TimerStatus.RUNNING
            launch(remainingDelay)
        }

        /**
         * 重置定时器到初始状态。
         */
        fun reset() {
            currentValue = initialValue
            remainingDelay = timeUnit.toMillis(initialDelay)
            scope?.cancel()
            if (status == TimerStatus.RUNNING) launch()
        }

        /**
         * 提前结束定时器并触发完成回调。
         */
        fun completeEarly() {
            cancel()
            onComplete?.invoke(currentValue)
        }

        /**
         * 将定时器绑定到生命周期拥有者。
         *
         * @param lifecycleOwner 要绑定的生命周期拥有者。
         * @return 当前 [TimerController] 实例，用于链式调用。
         */
        fun bindLifecycle(lifecycleOwner: LifecycleOwner) = apply {
            val lifecycle = when (lifecycleOwner) {
                is Fragment -> lifecycleOwner.viewLifecycleOwner.lifecycle
                else -> lifecycleOwner.lifecycle
            }
            setupLifecycleObserver(lifecycle)
        }

        /**
         * 将定时器绑定到特定生命周期。
         *
         * @param lifecycle 要绑定的生命周期。
         * @return 当前 [TimerController] 实例，用于链式调用。
         */
        fun bindLifecycle(lifecycle: Lifecycle) = apply {
            setupLifecycleObserver(lifecycle)
        }

        /**
         * 启动定时器的内部实现，使用协程循环执行计数。
         *
         * @param delay 初始延迟时间（毫秒），默认使用 [initialDelay] 转换为毫秒。
         */
        private fun launch(delay: Long = timeUnit.toMillis(initialDelay)) {
            scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            scope?.launch {
                delay(delay)  // 初始延迟
                while (isActive) {
                    onTick?.invoke(this@TimerController, currentValue)
                    if (targetValue != -1L && currentValue == targetValue) {
                        cleanup()
                        onComplete?.invoke(currentValue)
                        break
                    }
                    currentValue += if (targetValue != -1L && initialValue > targetValue) -1 else 1
                    lastTickTime = System.currentTimeMillis()
                    delay(timeUnit.toMillis(interval))  // 每次循环延迟
                }
            }
        }

        /**
         * 设置生命周期观察者，根据生命周期状态自动管理定时器。
         *
         * @param lifecycle 要绑定的生命周期对象。
         */
        private fun setupLifecycleObserver(lifecycle: Lifecycle) {
            lifecycleObserver?.let { lifecycle.removeObserver(it) }
            lifecycleObserver = object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> if (activeOnly) resume()
                        Lifecycle.Event.ON_PAUSE -> if (activeOnly) pause()
                        Lifecycle.Event.ON_DESTROY -> cancel()
                        else -> {}
                    }
                }
            }.also { lifecycle.addObserver(it) }
        }

        /**
         * 清理定时器资源，取消协程并更新状态。
         */
        private fun cleanup() {
            scope?.cancel()
            status = TimerStatus.IDLE
        }

        /**
         * 表示定时器可能状态的枚举类。
         */
        enum class TimerStatus {
            RUNNING,  // 运行中
            IDLE,     // 空闲
            PAUSED    // 已暂停
        }
    }

    /**
     * 在 LifecycleOwner 上启动定时器的扩展函数。
     *
     * @param targetValue 目标计数值（以 [timeUnit] 为单位）。使用 -1 表示无限时长。
     * @param interval 每次计数的时间间隔（以 [timeUnit] 为单位）。
     * @param timeUnit 时间单位，用于 [targetValue]、[interval] 和 [initialDelay]。
     * @param initialValue 定时器的起始值（默认：0）。
     * @param initialDelay 首次计数前的延迟时间（以 [timeUnit] 为单位，默认：0）。
     * @param activeOnly 若为 true，定时器仅在生命周期处于活跃状态（即 Activity/Fragment 处于 RESUMED 状态，前台可见）时运行，
     *                   在暂停（PAUSED）或停止（STOPPED）时自动暂停，并在恢复到 RESUMED 时自动继续，默认：false。
     *                   注意：此参数仅影响生命周期触发的暂停和恢复，手动调用 pause() 和 resume() 不受此限制。
     * @param onTick 每次计数时调用的回调函数，接收控制器和当前值（可选）。
     * @param onComplete 定时器完成时调用的回调函数，接收最终值（可选）。
     */
    fun LifecycleOwner.startTimer(
        targetValue: Long,
        interval: Long,
        timeUnit: TimeUnit,
        initialValue: Long = 0,
        initialDelay: Long = 0,
        activeOnly: Boolean = false,
        onTick: ((controller: TimerController, currentValue: Long) -> Unit)? = null,
        onComplete: ((finalValue: Long) -> Unit)? = null
    ) {
        build(targetValue, interval, timeUnit, initialValue, initialDelay, activeOnly, onTick, onComplete)
            .bindLifecycle(this)
            .start()
    }
}