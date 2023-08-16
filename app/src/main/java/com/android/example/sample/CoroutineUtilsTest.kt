package com.android.example.sample

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.dcln.androidutils.utils.CoroutineUtils
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnIO
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnUI

fun main() {
    // Example 1: Launch a coroutine on the UI thread
    CoroutineUtils.launchOnUI {
        println("Running on UI thread")
    }

    // Example 2: Launch a coroutine on the IO thread
    CoroutineUtils.launchOnIO {
        println("Running on IO thread")
    }

    // Example 3: Launch a coroutine on the UI thread with a delay
    CoroutineUtils.launchOnUI(delayMillis = 1000L) {
        println("Running on UI thread with a delay")
    }

    // Example 4: Run a block of code on the UI thread and wait for it to finish
    suspend fun exampleWithUIContext() {
        CoroutineUtils.withUIContext {
            println("Running a block of code on the UI thread and waiting for it to finish")
        }
    }

    // Example 5: Create a Flow and collect it on the UI thread
    val exampleFlow: Flow<Int> = flow {
        for (i in 1..5) {
            emit(i)
        }
    }

}