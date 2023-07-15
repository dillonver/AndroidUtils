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

    fun createInterval(
        end: Long,
        period: Long,
        unit: TimeUnit,
        start: Long = 0,
        initialDelay: Long = 0,
        onTick: (IntervalController, Long) -> Unit,
        onFinish: ((Long) -> Unit)? = null
    ): IntervalController {
        return IntervalController(
            end,
            period,
            unit,
            start,
            initialDelay,
            onTick,
            onFinish
        )
    }

    class IntervalController(
        val end: Long,
        private val period: Long,
        private val unit: TimeUnit,
        val start: Long,
        private val initialDelay: Long,
        private val onTick: (IntervalController, Long) -> Unit,
        private val onFinish: ((Long) -> Unit)? = null
    ) : Serializable, Closeable {
        private var countTime = 0L
        private var delay = 0L
        private var scope: CoroutineScope? = null
        private lateinit var ticker: ReceiveChannel<Unit>

        var count = start
        var state = IntervalStatus.STATE_IDLE
            private set

        fun start() = apply {
            if (state == IntervalStatus.STATE_ACTIVE) return this
            state = IntervalStatus.STATE_ACTIVE
            count = start
            launch()
        }

        fun stop() {
            if (state == IntervalStatus.STATE_IDLE) return
            scope?.cancel()
            state = IntervalStatus.STATE_IDLE
            onFinish?.invoke(count)
        }

        fun cancel() {
            if (state == IntervalStatus.STATE_IDLE) return
            scope?.cancel()
            state = IntervalStatus.STATE_IDLE
        }

        override fun close() = cancel()

        fun pause() {
            if (state != IntervalStatus.STATE_ACTIVE) return
            scope?.cancel()
            state = IntervalStatus.STATE_PAUSE
            delay = System.currentTimeMillis() - countTime
        }

        fun resume() {
            if (state != IntervalStatus.STATE_PAUSE) return
            state = IntervalStatus.STATE_ACTIVE
            launch(delay)
        }

        fun reset() {
            count = start
            delay = unit.toMillis(initialDelay)
            scope?.cancel()
            if (state == IntervalStatus.STATE_ACTIVE) launch()
        }

        fun finishEarly() {
            cancel()
            onFinish?.invoke(count)
        }

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
                    onTick.invoke(this@IntervalController, count)
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

        enum class IntervalStatus {
            STATE_ACTIVE, STATE_IDLE, STATE_PAUSE
        }
    }

    fun LifecycleOwner.interval(
        end: Long,
        period: Long,
        unit: TimeUnit,
        start: Long = 0,
        initialDelay: Long = 0,
        onTick: (IntervalController, Long) -> Unit,
        onFinish: ((Long) -> Unit)? = null
    ): IntervalController {
        return createInterval(
            end,
            period,
            unit,
            start,
            initialDelay,
            onTick,
            onFinish
        )
            .life(this)
    }
}