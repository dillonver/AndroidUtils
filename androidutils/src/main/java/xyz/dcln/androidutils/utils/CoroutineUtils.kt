package xyz.dcln.androidutils.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

object CoroutineUtils {
    private val uiScope = CoroutineScope(Dispatchers.Main + CoroutineName("UI"))
    private val ioScope = CoroutineScope(Dispatchers.IO + CoroutineName("IO"))

    // Launches a new coroutine with the given scope, delay, and block.
    private fun launch(
        scope: CoroutineScope,
        delayMillis: Long = 0L,
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch {
            delay(delayMillis)
            block()
        }
    }

    /**
     * Launches a new coroutine on the UI thread with an optional delay.
     * @param delayMillis The delay in milliseconds before the coroutine starts (default: 0).
     * @param block The coroutine code to be executed.
     */
    fun Any.launchOnUI(delayMillis: Long = 0L, block: suspend CoroutineScope.() -> Unit) =
        launch(uiScope, delayMillis, block)

    /**
     * Launches a new coroutine on the IO thread with an optional delay.
     * @param delayMillis The delay in milliseconds before the coroutine starts (default: 0).
     * @param block The coroutine code to be executed.
     */
    fun Any.launchOnIO(delayMillis: Long = 0L, block: suspend CoroutineScope.() -> Unit) =
        launch(ioScope, delayMillis, block)

    /**
     * Cancels all coroutines launched by this object.
     */
    fun cancelAll() {
        uiScope.coroutineContext.cancelChildren()
        ioScope.coroutineContext.cancelChildren()
    }

    // Wraps a Flow into a coroutine with the given scope and action.
    private fun <T> Flow<T>.onThread(scope: CoroutineScope, action: (T) -> Unit) {
        scope.launch { collect { value -> action(value) } }
    }

    /**
     * Wraps a Flow into a coroutine on the UI thread.
     * @param action The action to be executed when the Flow emits a new value.
     */
    fun <T> Flow<T>.onUIThread(action: (T) -> Unit) = onThread(uiScope, action)

    /**
     * Wraps a Flow into a coroutine on the IO thread.
     * @param action The action to be executed when the Flow emits a new value.
     */
    fun <T> Flow<T>.onIOThread(action: (T) -> Unit) = onThread(ioScope, action)

    /**
     * Cancels the observation of the given Flow.
     * @param job The job returned by the onUIThread or onIOThread function.
     */
    fun cancelFlowObservation(job: Job) {
        job.cancel()
    }

    // Runs a block of code with the given scope and suspends until it's done.
    private suspend fun withContext(
        scope: CoroutineScope,
        block: suspend CoroutineScope.() -> Unit
    ) {
        withContext(scope.coroutineContext) { block() }
    }

    /**
     * Runs a block of code on the UI thread and suspends until it's done.
     * @param block The coroutine code to be executed.
     */
    suspend fun withUIContext(block: suspend CoroutineScope.() -> Unit) =
        withContext(uiScope, block)

    /**
     * Runs a block of code on the IO thread and suspends until it's done.
     * @param block The coroutine code to be executed.
     */
    suspend fun withIOContext(block: suspend CoroutineScope.() -> Unit) =
        withContext(ioScope, block)

    /**
     * Runs a block of code multiple times on the IO thread and suspends until it's done.
     * @param times The number of times the block should be run.
     * @param block The coroutine code to be executed.
     */
    suspend fun repeatIo(times: Int, block: suspend CoroutineScope.() -> Unit) {
        withIOContext {
            repeat(times) {
                launch { block() }
            }
        }
    }
}