package xyz.dcln.androidutils

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import xyz.dcln.androidutils.utils.ActivityUtils
import xyz.dcln.androidutils.utils.AppUtils

object AndroidUtils {
    private var application: Application? = null
    private var isInitialized = false

    /**
     * Initialize AndroidUtils with the given Application object.
     */
    fun init(application: Application) {
        if (isInitialized) {
            return
        }
        AndroidUtils.application = application
        AppUtils.init()
        ActivityUtils.init()
        isInitialized = true
    }

    /**
     * Get the Application object.
     */
    fun getApplication(): Application {
        return application ?: throw NullPointerException("AndroidUtils has not been initialized.")
    }

    /**
     * Call the given block on the main thread, ensuring that it is executed on the main thread.
     * @param block The code to execute on the main thread.
     */
    fun <T> withMain(block: () -> T): T? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post {
                block()
            }
            return null
        }
        return block()
    }

    /**
     * Call the given block with the Application object, ensuring that it is executed on the main thread
     * in the context of the initialized Application.
     * @param block The code to execute with the Application object.
     */
    fun <T> withApplication(block: (application: Application) -> T): T? {
        val app = application ?: return null
        return withMain { block(app) }
    }

    /**
     * Internal ContentProvider class used to automatically initialize AndroidUtils.
     */
    class InitProvider : ContentProvider() {

        override fun onCreate(): Boolean {
            context?.let {
                AndroidUtils.init(it.applicationContext as Application)
            }
            return false
        }

        override fun insert(uri: Uri, values: ContentValues?): Uri? {
            return null
        }

        override fun query(
            uri: Uri, projection: Array<String>?, selection: String?,
            selectionArgs: Array<String>?, sortOrder: String?
        ): Cursor? {
            return null
        }

        override fun update(
            uri: Uri, values: ContentValues?, selection: String?,
            selectionArgs: Array<String>?
        ): Int {
            return 0
        }

        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
            return 0
        }

        override fun getType(uri: Uri): String? {
            return null
        }
    }
}