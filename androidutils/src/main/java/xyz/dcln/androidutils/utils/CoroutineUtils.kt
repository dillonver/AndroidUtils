package xyz.dcln.androidutils.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

object CoroutineUtils {

    /**
     * Launches a new coroutine on the UI thread with an optional delay and returns a Job.
     * @param delayMillis The delay in milliseconds before the coroutine starts (default: 0).
     * @param block The coroutine code to be executed.
     */
    fun launchOnUI(
        delayMillis: Long = 0L,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val uiScope = CoroutineScope(Dispatchers.Main + CoroutineName("UI"))
        return uiScope.launch {
            delay(delayMillis)
            block()
        }
    }

    /**
     * Launches a new coroutine on the IO thread with an optional delay and returns a Job.
     * @param delayMillis The delay in milliseconds before the coroutine starts (default: 0).
     * @param block The coroutine code to be executed.
     */
    fun launchOnIO(
        delayMillis: Long = 0L,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val ioScope = CoroutineScope(Dispatchers.IO + CoroutineName("IO"))
        return ioScope.launch {
            delay(delayMillis)
            block()
        }
    }

    /**
     * Runs a block of code on the UI thread and returns a result.
     * @param block The coroutine code to be executed.
     */
    suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Main) { block() }
    }

    /**
     * Runs a block of code on the IO thread and returns a result.
     * @param block The coroutine code to be executed.
     */
    suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO) { block() }
    }

    /**
     * Repeats a block of code multiple times on the IO thread and suspends until it's done.
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

    /**
     * Periodically executes a block of code on the specified dispatcher.
     * @param intervalMillis The interval time in milliseconds between each execution of the block.
     * @param block The coroutine code to be executed periodically.
     */
    fun periodicTask(
        intervalMillis: Long,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val scope = CoroutineScope(dispatcher + CoroutineName("PeriodicTask"))
        return scope.launch {
            while (isActive) {
                block()
                delay(intervalMillis)
            }
        }
    }

    /**
     * Safely runs a coroutine and catches any exceptions.
     * @param block The coroutine code to be executed.
     * @param onError Optional callback that gets invoked if an exception is caught.
     */
    fun launchCatching(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend CoroutineScope.() -> Unit,
        onError: (Throwable) -> Unit = {}
    ): Job {
        val scope = CoroutineScope(dispatcher + CoroutineName("SafeLaunch"))
        return scope.launch {
            try {
                block()
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }
}
