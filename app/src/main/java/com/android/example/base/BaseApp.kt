package com.android.example.base

import android.app.Application
import xyz.dcln.androidutils.utils.LogUtils
import xyz.dcln.androidutils.utils.NetworkUtils
import kotlin.system.exitProcess


open class BaseApp : Application(), Thread.UncaughtExceptionHandler {

    override fun onCreate() {
        super.onCreate()
        NetworkUtils.registerNetworkCallback(onConnected = {
            LogUtils.i("onConnected")
        }, onDisconnected = {
            LogUtils.i("onDisconnected")

        })
        Thread.setDefaultUncaughtExceptionHandler(this)

    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        // 处理异常并防止应用程序崩溃
        LogUtils.e( "BaseApp UncaughtException\n$ex")
        exitProcess(1)
    }

}