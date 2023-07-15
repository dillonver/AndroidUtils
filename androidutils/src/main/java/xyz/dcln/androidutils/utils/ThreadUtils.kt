package xyz.dcln.androidutils.utils

import kotlinx.coroutines.*
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnIO
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnUI

object ThreadUtils {
    fun Any.runOnMainThread(delayMillis: Long = 0L, block: suspend CoroutineScope.() -> Unit) {
        CoroutineUtils.launchOnUI(delayMillis, block)
    }

    fun Any.runOnIOThread(delayMillis: Long = 0L, block: suspend CoroutineScope.() -> Unit) {
        CoroutineUtils.launchOnIO(delayMillis, block)

    }

}