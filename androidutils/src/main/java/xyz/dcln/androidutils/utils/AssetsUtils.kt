package xyz.dcln.androidutils.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object AssetsUtils {

    /**
     * 从 Assets 目录中读取文件并将内容作为字符串返回
     *
     * @param fileName 文件名
     * @param context 上下文对象，默认为 AppUtils.getApp()
     * @return 文件内容字符串，如果发生异常则返回空字符串
     */
    fun readFileAsString(fileName: String, context: Context = AppUtils.getApp()): String {
        val assetManager = context.assets
        return try {
            assetManager.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 列出指定目录下的文件列表
     *
     * @param context 上下文对象
     * @param directoryName 目录名
     * @return 文件列表数组
     */
    fun listFilesInDirectory(
        directoryName: String,
        context: Context = AppUtils.getApp()
    ): Array<String> {
        val assetManager = context.assets
        return assetManager.list(directoryName) ?: arrayOf()
    }

    /**
     * 检查指定文件是否存在于 Assets 目录中
     *
     * @param context 上下文对象
     * @param fileName 文件名
     * @return 文件是否存在的布尔值
     */
    fun fileExistsInAssets(
        fileName: String,
        context: Context = AppUtils.getApp()
    ): Boolean {
        val assetManager = context.assets
        return try {
            assetManager.open(fileName).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 将 Assets 目录中的文件复制到应用的本地存储中
     *
     * @param assetFileName 要复制的文件名
     * @param outputFileName 复制后在本地存储中的文件名
     * @param context 上下文对象，默认为 AppUtils.getApp()
     */
    fun copyAssetFileToStorage(assetFileName: String, outputFileName: String, context: Context = AppUtils.getApp()) {
        try {
            val inputStream: InputStream = context.assets.open(assetFileName)
            val outputStream: OutputStream = FileOutputStream(File(context.filesDir, outputFileName))
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    /**
     * 从 Assets 目录中读取 JSON 文件并返回其内容作为字符串
     *
     * @param context 上下文对象
     * @param fileName JSON 文件名
     * @return JSON 文件内容字符串
     */
    fun readJsonFromAssets(fileName: String, context: Context = AppUtils.getApp()): String {
        return readFileAsString(fileName, context)
    }

    /**
     * 从 Assets 目录中加载图片文件并返回 Bitmap 对象
     *
     * @param context 上下文对象
     * @param fileName 图片文件名
     * @return 加载的图片的 Bitmap 对象，如果发生异常则返回 null
     */
    fun loadBitmapFromAssets(fileName: String, context: Context = AppUtils.getApp()): Bitmap? {
        var bitmap: Bitmap? = null
        var inputStream: InputStream? = null
        try {
            inputStream = context.assets.open(fileName)
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }

    /**
     * 在应用安装时将数据写入到应用的私有文件目录中。
     *
     * @param fileName 写入的文件名
     * @param data 要写入的数据
     * @param context 上下文对象，用于获取文件输出流
     */
    fun writeToPrivateFileOnInstall(
        fileName: String,
        data: ByteArray,
        context: Context = AppUtils.getApp()
    ) {
        try {
            // 获取应用的私有文件输出流，用于写入数据到私有文件目录中
            val outputStream: OutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

            // 将数据写入到文件中
            outputStream.write(data)

            // 关闭输出流
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun saveImageFromAssetsToPictures(
        context: Context,
        fileName: String,
        callback: ((result: Boolean, msg: String?, uri: Uri?) -> Unit)? = null
    ) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open(fileName)
            val imageBitmap = BitmapFactory.decodeStream(inputStream)

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above
                val imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let { uri ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    callback?.invoke(true, "Success", uri)
                }
            } else {
                // For Android 10 below
                val picturesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val destFile = File(picturesDir, fileName)

                FileOutputStream(destFile).use { outputStream ->
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }

                val targetUri = UriUtils.file2Uri(destFile)

                callback?.invoke(true, "Fail", targetUri)
                @Suppress("DEPRECATION")
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    data = targetUri
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 处理异常，例如提示用户保存失败
            callback?.invoke(false, "Error", null)
        }
    }
}