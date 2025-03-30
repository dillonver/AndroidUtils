package xyz.dcln.androidutils.utils

import java.io.BufferedReader

object RootUtils {

    /**
     * 请求 root 权限
     * @return 如果成功获得 root 权限返回 true，否则返回 false
     */
    fun requestRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val output = process.outputStream.bufferedWriter()
            output.write("exit\n")
            output.flush()
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 执行 Shell 命令
     * @param command 命令字符串
     * @param callback 回调函数，接受执行成功或失败标志、命令输出结果和错误信息
     */
    fun executeShellCommand(command: String, callback: (success: Boolean, result: String, error: String?) -> Unit) {
        if (!hasRootAccess()) {
            if (!requestRootAccess()) {
                callback(false, "", "Error: No root access")
                return
            }
        }

        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val error = process.errorStream.bufferedReader().use(BufferedReader::readText)
            process.waitFor()
            if (process.exitValue() == 0) {
                callback(true, output, null)
            } else {
                callback(false, "", "命令执行失败: $error")
            }
        } catch (e: Exception) {
            callback(false, "", "错误: ${e.message}")
        }
    }

    /**
     * 获取前台应用包名
     * @param callback 回调函数，接受执行成功或失败标志、前台应用包名和错误信息
     */
    fun getForegroundApp(callback: (success: Boolean, result: String, error: String?) -> Unit) {
        executeShellCommand("dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'") { success, output, error ->
            if (error != null) {
                callback(false, "", error)
            } else {
                val regex = Regex("u0 ([^/]+)")
                val result = regex.find(output)?.groupValues?.get(1) ?: "未找到"
                callback(true, result, null)
            }
        }
    }

    /**
     * 检查设备是否有 root 权限
     * @return 如果设备有 root 权限返回 true，否则返回 false
     */
    fun hasRootAccess(): Boolean {
        return try {
            var hasRoot = false
            executeShellCommand("echo root") { success, result, error ->
                hasRoot = success && result.trim() == "root"
            }
            hasRoot
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 重启设备
     * @param callback 回调函数，接受执行成功或失败标志、执行结果和错误信息
     */
    fun rebootDevice(callback: (success: Boolean, result: String, error: String?) -> Unit) {
        executeShellCommand("reboot", callback)
    }

    /**
     * 授予或撤销应用权限
     * @param packageName 应用包名
     * @param permission 权限名称
     * @param grant true 表示授予权限，false 表示撤销权限
     * @param callback 回调函数，接受执行成功或失败标志、执行结果和错误信息
     */
    fun managePermission(packageName: String, permission: String, grant: Boolean, callback: (success: Boolean, result: String, error: String?) -> Unit) {
        val action = if (grant) "grant" else "revoke"
        executeShellCommand("pm $action $packageName $permission", callback)
    }

    /**
     * 授予应用权限
     * @param packageName 应用包名
     * @param permission 要授予的权限
     * @param callback 回调函数，接受执行成功或失败标志、执行结果和错误信息
     */
    fun grantPermission(packageName: String, permission: String, callback: (success: Boolean, result: String, error: String?) -> Unit) {
        managePermission(packageName, permission, true, callback)
    }

    /**
     * 撤销应用权限
     * @param packageName 应用包名
     * @param permission 要撤销的权限
     * @param callback 回调函数，接受执行成功或失败标志、执行结果和错误信息
     */
    fun revokePermission(packageName: String, permission: String, callback: (success: Boolean, result: String, error: String?) -> Unit) {
        managePermission(packageName, permission, false, callback)
    }

    /**
     * 卸载应用
     * @param packageName 要卸载的应用包名
     * @param callback 回调函数，接受执行成功或失败标志、执行结果和错误信息
     */
    fun uninstallApp(packageName: String, callback: (success: Boolean, result: String, error: String?) -> Unit) {
        executeShellCommand("pm uninstall $packageName", callback)
    }

    /**
     * 安装应用
     * @param apkFilePath 要安装的 APK 文件路径
     * @param callback 回调函数，接受执行成功或失败标志、执行结果和错误信息
     */
    fun installApp(apkFilePath: String, callback: (success: Boolean, result: String, error: String?) -> Unit) {
        executeShellCommand("pm install $apkFilePath", callback)
    }

    /**
     * 清除应用数据
     * @param packageName 要清除数据的应用包名
     * @param callback 回调函数，接受执行成功或失败标志、执行结果和错误信息
     */
    fun clearAppData(packageName: String, callback: (success: Boolean, result: String, error: String?) -> Unit) {
        executeShellCommand("pm clear $packageName", callback)
    }
}
