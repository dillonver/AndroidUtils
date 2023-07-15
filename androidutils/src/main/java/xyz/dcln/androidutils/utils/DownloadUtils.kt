package xyz.dcln.androidutils.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import java.io.File

object DownloadUtils {
    private lateinit var url: String
    private var noticeTitle: String? = null
    private var noticeDes: String? = null

    private lateinit var downloadManager: DownloadManager
    private var downloadId: Long = 0
    private lateinit var path: String
    private var listener: DownloadManagerListener? = null


    /**
     * 开始下载文件
     *
     * @param url      下载链接
     * @param title    通知标题（可选）
     * @param des      通知描述（可选）
     * @param prepare  开始回调函数
     * @param success  成功回调函数
     * @param failure  失败回调函数
     */
    fun download(
        url: String,
        title: String? = null,
        des: String? = null,
        success: ((path: String?) -> Unit)? = null,
        failure: ((Throwable?) -> Unit)? = null,
        prepare: ((url: String?) -> Unit)? = null
        ) {
        init(url, title, des)
        setListener(object : DownloadManagerListener {
            override fun onPrepare(url: String?) {
                prepare?.invoke(url)
            }

            override fun onSuccess(path: String?) {
                success?.invoke(path)
            }

            override fun onFailed(throwable: Throwable?) {
                failure?.invoke(throwable)
            }
        })

        download()
    }

    /**
     * 初始化下载参数
     *
     * @param url         下载链接
     * @param noticeTitle 通知标题（可选）
     * @param noticeDes   通知描述（可选）
     * @return DownloadUtils 对象本身以支持链式调用
     */
    private fun init(
        url: String,
        noticeTitle: String? = null,
        noticeDes: String? = null
    ): DownloadUtils {
        this.url = url
        this.noticeTitle = noticeTitle
        this.noticeDes = noticeDes
        return this
    }

    /**
     * 设置下载监听器
     *
     * @param listener 下载监听器
     * @return DownloadUtils 对象本身以支持链式调用
     */
    private fun setListener(listener: DownloadManagerListener?): DownloadUtils {
        this.listener = listener
        return this
    }

    /**
     * 开始下载文件
     */
    private fun download() {
        // 创建下载请求
        val request = createDownloadRequest()

        // 获取系统的 DownloadManager
        downloadManager =
            AppUtils.getApp().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // 将下载请求加入下载队列，加入下载队列后会返回一个唯一的下载 id
        listener?.onPrepare(url)
        try {
            downloadId = downloadManager.enqueue(request)
        } catch (e: IllegalArgumentException) {
            listener?.onFailed(e)
            return
        }

        // 注册广播接收者，监听下载完成的广播
        AppUtils.getApp()
            .registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }


    // 创建下载请求
    private fun createDownloadRequest(): DownloadManager.Request {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            val name = getFileNameByUrl(url)
            setTitle(noticeTitle ?: name)
            setDescription(noticeDes ?: "文件下载中...")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            // 设置文件下载路径
            val file = File(AppUtils.getApp().externalCacheDir, name)
            setDestinationUri(Uri.parse(file.toURI().toString()))
            path = file.absolutePath
        }
        return request
    }

    // 广播接收者，用于监听下载的各个状态
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context, intent: Intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                // 查询该下载任务的下载状态
                val query = DownloadManager.Query().apply {
                    setFilterById(downloadId)
                }
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> handleDownloadSuccess(cursor)
                        DownloadManager.STATUS_FAILED -> handleDownloadFailure(cursor)
                        else -> {
                        }
                    }
                }
                cursor?.close()
            }
        }
    }

    private fun handleDownloadSuccess(cursor: Cursor) {
        // 下载成功后的处理
        listener?.onSuccess(path)
        cursor.close()

        // 注销广播接收者
        AppUtils.getApp().unregisterReceiver(receiver)
    }

    private fun handleDownloadFailure(cursor: Cursor) {
        // 下载失败后的处理
        listener?.onFailed(Exception("下载失败"))
        cursor.close()

        // 注销广播接收者
        AppUtils.getApp().unregisterReceiver(receiver)
    }

    /**
     * 根据下载链接获取文件名
     *
     * @param url 下载链接
     * @return 文件名
     */
    private fun getFileNameByUrl(url: String): String {
        var filename = url.substring(url.lastIndexOf("/") + 1)
        filename = filename.substring(
            0,
            if (filename.indexOf("?") == -1) filename.length else filename.indexOf("?")
        )
        return filename
    }

    /**
     * 下载管理器监听器
     */
    internal interface DownloadManagerListener {
        /**
         * 下载准备就绪
         */
        fun onPrepare(url: String?)

        /**
         * 下载成功
         *
         * @param path 下载文件的路径
         */
        fun onSuccess(path: String?)

        /**
         * 下载失败
         *
         * @param throwable 失败的异常信息
         */
        fun onFailed(throwable: Throwable?)
    }
}
