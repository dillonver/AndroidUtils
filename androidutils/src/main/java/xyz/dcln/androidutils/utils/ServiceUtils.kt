package xyz.dcln.androidutils.utils

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build

object ServiceUtils {

    /**
     * 获取所有运行的服务。
     *
     * @param context 上下文对象。
     * @return 所有运行的服务的列表。
     */
    fun getAllRunningServices(context: Context): List<ActivityManager.RunningServiceInfo> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses.flatMap { processInfo ->
            processInfo.pkgList.map { packageName ->
                ActivityManager.RunningServiceInfo().apply {
                    service = ComponentName(packageName, "")
                    process = processInfo.processName
                }
            }
        }
    }

    /**
     * 启动服务。
     *
     * @param context 上下文对象。
     * @param serviceClass 要启动的服务的类。
     */
    fun startService(context: Context, serviceClass: Class<*>) {
        val intent = Intent(context, serviceClass)
        context.startService(intent)
    }

    /**
     * 停止服务。
     *
     * @param context 上下文对象。
     * @param serviceClass 要停止的服务的类。
     * @return 如果成功停止服务，则返回 true；否则返回 false。
     */
    fun stopService(context: Context, serviceClass: Class<*>): Boolean {
        val intent = Intent(context, serviceClass)
        return context.stopService(intent)
    }

    /**
     * 绑定服务。
     *
     * @param context 上下文对象。
     * @param serviceClass 要绑定的服务的类。
     * @param connection 与服务之间的连接对象。
     * @param flags 绑定标志位。
     * @return 如果成功绑定服务，则返回 true；否则返回 false。
     */
    fun bindService(context: Context, serviceClass: Class<*>, connection: ServiceConnection, flags: Int): Boolean {
        val intent = Intent(context, serviceClass)
        return context.bindService(intent, connection, flags)
    }

    /**
     * 解绑服务。
     *
     * @param context 上下文对象。
     * @param connection 之前与服务之间建立的连接对象。
     */
    fun unbindService(context: Context, connection: ServiceConnection) {
        context.unbindService(connection)
    }

    /**
     * 判断服务是否运行。
     *
     * @param context 上下文对象。
     * @param serviceClass 要判断的服务的类。
     * @return 如果服务正在运行，则返回 true；否则返回 false。
     */
    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        for (service in services) {
            val componentName = ComponentName(service.service.packageName, service.service.className)
            if (componentName == ComponentName(context, serviceClass)) {
                return true
            }
        }
        return false
    }
}