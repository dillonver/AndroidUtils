package xyz.dcln.androidutils.utils

import android.app.ActivityManager
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnUI


/**
 * Description:
 * Author: Dillon
 * Date: 2023/4/5 21:49
 */
/**
 * Description:
 * Author: Dillon
 * Date: 2024/5/22 10:01
 */
object ToastUtils {
    private var toastInstance: Toast? = null
    private var dialog: Dialog? = null
    private const val CHANNEL_ID = "toast_channel_id"

    data class DisplayConfig(
        val context: Context,
        var duration: Int = Toast.LENGTH_SHORT,
        @ColorInt var bgColor: Int = Color.BLACK,
        var cornerRadius: Float = 10f,
        var title: CharSequence? = "提示",
        var msg: CharSequence? = null,
        var gravity: Int = Gravity.CENTER,
        var xOffset: Int = 0,
        var yOffset: Int = 0,
        var textSize: Float = 16f,
        @ColorInt var textColor: Int = Color.WHITE,
        var leftIcon: Drawable? = null,
        var topIcon: Drawable? = null,
        var rightIcon: Drawable? = null,
        var bottomIcon: Drawable? = null,
        var leftPadding: Float = 10f,
        var rightPadding: Float = 10f,
        var topPadding: Float = 5f,
        var bottomPadding: Float = 5f,
        var cancelCurrent: Boolean = true,
        var autoDismiss: Boolean = true,  // 新增属性，用于控制通知是否自动消失
        var dismissDuration: Long = 2000  // 新增属性，设置通知自动消失的时间（以毫秒为单位）
    )

    private fun dpToPx(dp: Float, metrics: DisplayMetrics): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
    }

    private fun toast(context: Context, block: DisplayConfig.() -> Unit) {
        val config = DisplayConfig(context).apply(block)
        val message = config.msg
        if (message.isNullOrBlank()) {
            LogUtils.w("message.isNullOrBlank")
            return
        }
        if (config.cancelCurrent) {
            toastInstance?.cancel()
        }
        val textView = createTextView(config)
        toastInstance = Toast(context).apply {
            duration = config.duration
            // 从Android R（30）开始，Toast中的gravity、xOffset和yOffset参数不再使用
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                setGravity(config.gravity, config.xOffset, config.yOffset)
            }
            view = textView
            show()
        }
    }

    private fun createTextView(config: DisplayConfig): TextView {
        return TextView(config.context).apply {
            text = config.msg
            setTextColor(config.textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, config.textSize)
            setCompoundDrawablesWithIntrinsicBounds(
                config.leftIcon,
                config.topIcon,
                config.rightIcon,
                config.bottomIcon
            )
            setBackgroundAndPadding(config)
        }
    }

    private fun TextView.setBackgroundAndPadding(config: DisplayConfig) {
        val metrics = config.context.resources.displayMetrics
        setPadding(
            dpToPx(config.leftPadding, metrics),
            dpToPx(config.topPadding, metrics),
            dpToPx(config.rightPadding, metrics),
            dpToPx(config.bottomPadding, metrics)
        )
        background = GradientDrawable().apply {
            setColor(config.bgColor)
            cornerRadius = config.cornerRadius
        }
    }

    /**
     * 显示自定义的Toast消息。
     * @param context 用于显示Toast的上下文，默认为通过 [AppUtils.getApp] 获取的应用程序上下文。
     * @param block 一个带接收者的lambda，通过 [DisplayConfig] 配置Toast的属性。
     */
    fun showToast(context: Context? = AppUtils.getApp(), block: DisplayConfig.() -> Unit) {
        // 如果是Android R及以上版本，并且应用不在前台运行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !isAppInForeground(context)) {
            // 检查通知权限是否开启
            if (areNotificationsEnabled(context ?: AppUtils.getApp())) {
                // 显示通知
                showNotification(context ?: AppUtils.getApp(), block)
            } else {
                // 提示用户开启通知权限
                promptEnableNotifications(context ?: AppUtils.getApp())
            }
        } else {
            // 显示Toast
            launchOnUI {
                toast(context ?: AppUtils.getApp(), block)
            }
        }
    }

    /**
     * 检查应用是否在前台运行。
     * @param context 上下文
     * @return Boolean 应用是否在前台运行
     */
    private fun isAppInForeground(context: Context?): Boolean {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        return appProcesses.any { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && it.processName == packageName }
    }

    /**
     * 检查通知权限是否开启。
     * @param context 上下文
     * @return Boolean 是否开启通知权限
     */
    private fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * 提示用户开启通知权限。
     * @param context 上下文
     */
    private fun promptEnableNotifications(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("启用通知")
            .setMessage("请启用通知以接收重要提醒。")
            .setPositiveButton("设置") { _, _ ->
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 显示通知。
     * @param context 上下文
     * @param block 一个带接收者的lambda，通过 [DisplayConfig] 配置通知的属性。
     */
    private fun showNotification(context: Context, block: DisplayConfig.() -> Unit) {
        val config = DisplayConfig(context).apply(block)
        val message = config.msg ?: return
        createNotificationChannel(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(config.title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // 设置通知自动消失时间
        if (config.autoDismiss) {
            builder.setTimeoutAfter(config.dismissDuration)
        }

        val notification = builder.build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * 创建通知渠道。
     * @param context 上下文
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Toast Notifications"
            val descriptionText = "用于显示Toast样式的通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 显示短时间的Toast消息。
     * @param message 消息内容
     * @param context 上下文，默认为通过 [AppUtils.getApp] 获取的应用程序上下文。
     * @param cancelCurrentToast 是否取消当前显示的Toast
     */
    fun  Any.toastShort(
        message: String?,
        context: Context? = AppUtils.getApp(),
        cancelCurrentToast: Boolean? = true
    ) {
        if (message.isNullOrBlank()) {
            LogUtils.w("message.isNullOrBlank")
            return
        }
        launchOnUI {
            showToast(context ?: AppUtils.getApp()) {
                msg = message
                duration = Toast.LENGTH_SHORT
                cancelCurrent = cancelCurrentToast ?: true
            }
        }
    }

    /**
     * 显示长时间的Toast消息。
     * @param message 消息内容
     * @param context 上下文，默认为通过 [AppUtils.getApp] 获取的应用程序上下文。
     * @param cancelCurrentToast 是否取消当前显示的Toast
     */
    fun Any.toastLong(
        message: String?,
        context: Context? = AppUtils.getApp(),
        cancelCurrentToast: Boolean? = true
    ) {
        if (message.isNullOrBlank()) {
            LogUtils.w("message.isNullOrBlank")
            return
        }
        launchOnUI {
            showToast(context ?: AppUtils.getApp()) {
                msg = message
                duration = Toast.LENGTH_LONG
                cancelCurrent = cancelCurrentToast ?: true
            }
        }
    }

    /**
     * 取消当前显示的Toast或Dialog。
     */
    fun cancel() {
        toastInstance?.cancel()
        toastInstance = null
        dialog?.dismiss()
        dialog = null
    }
}