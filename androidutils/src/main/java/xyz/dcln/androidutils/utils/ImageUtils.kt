package xyz.dcln.androidutils.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


object ImageUtils {

    /**
     * 将 Bitmap 转换为 Drawable。
     *
     * @param bitmap 待转换的 Bitmap。
     * @return 转换后的 Drawable，如果输入的 Bitmap 为 null，则返回 null。
     */
    fun bitmap2Drawable(bitmap: Bitmap?): Drawable? {
        return bitmap?.let { BitmapDrawable(AppUtils.getApp().resources, it) }
    }

    /**
     * 将 View 转换为 Bitmap。
     *
     * @param view 待转换的 View。
     * @return 转换后的 Bitmap，如果输入的 View 为 null，则返回 null。
     */
    fun view2Bitmap(view: View?): Bitmap? {
        if (view == null) return null
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        )
        view.layout(view.left, view.top, view.right, view.bottom)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        return bitmap
    }

    /**
     * 从文件中获取 Bitmap。
     *
     * @param file 待获取 Bitmap 的文件。
     * @return 获取到的 Bitmap，如果文件为 null 或者获取失败，则返回 null。
     */
    fun getBitmap(file: File?): Bitmap? {
        return file?.let { BitmapFactory.decodeFile(it.absolutePath) }
    }

    /**
     * 从文件路径中获取 Bitmap。
     *
     * @param filePath 待获取 Bitmap 的文件路径。
     * @return 获取到的 Bitmap，如果文件路径为空或获取失败，则返回 null。
     */
    fun getBitmap(filePath: String): Bitmap? {
        if (filePath.isBlank()) return null
        return BitmapFactory.decodeFile(filePath)
    }

    /**
     * 从资源中获取 Bitmap。
     *
     * @param resId 待获取 Bitmap 的资源 ID。
     * @return 获取到的 Bitmap，如果资源不存在或获取失败，则返回 null。
     */
    fun getBitmap(@DrawableRes resId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(AppUtils.getApp(), resId) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        Canvas(bitmap).apply {
            drawable.setBounds(0, 0, width, height)
            drawable.draw(this)
        }
        return bitmap
    }

    /**
     * 从资源中获取 Bitmap，支持按照指定的最大宽度和高度进行缩放。
     *
     * @param resId 待获取 Bitmap 的资源 ID。
     * @param maxWidth 缩放后的最大宽度。
     * @param maxHeight 缩放后的最大高度。
     * @return 缩放后的 Bitmap，如果资源不存在或获取失败，则返回 null。
     */
    fun getBitmap(@DrawableRes resId: Int, maxWidth: Int, maxHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(AppUtils.getApp().resources, resId, this)
            inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
            inJustDecodeBounds = false
        }
        return BitmapFactory.decodeResource(AppUtils.getApp().resources, resId, options)
    }

    /**
     * 给 Bitmap 添加颜色滤镜。
     *
     * @param src 待处理的源 Bitmap。
     * @param color 颜色滤镜。
     * @param recycle 是否在处理后回收源 Bitmap，默认为 false。
     * @return 添加颜色滤镜后的 Bitmap，如果源 Bitmap 为空或已回收，则返回 null。
     */
    fun drawColor(src: Bitmap, @ColorInt color: Int, recycle: Boolean = false): Bitmap? {
        if (isEmptyBitmap(src)) return null
        val ret = if (recycle) src else src.copy(src.config, true)
        Canvas(ret).drawColor(color, PorterDuff.Mode.DARKEN)
        return ret
    }

    /**
     * 旋转 Bitmap。
     *
     * @param src 待旋转的源 Bitmap。
     * @param degrees 旋转的角度。
     * @param px 旋转的中心点 x 坐标。
     * @param py 旋转的中心点 y 坐标。
     * @param recycle 是否在旋转后回收源 Bitmap，默认为 false。
     * @return 旋转后的 Bitmap，如果源 Bitmap 为空或旋转角度为 0，则返回源 Bitmap。
     */
    fun rotate(src: Bitmap, degrees: Int, px: Float, py: Float, recycle: Boolean = false): Bitmap {
        if (isEmptyBitmap(src) || degrees == 0) return src
        val matrix = Matrix().apply { setRotate(degrees.toFloat(), px, py) }
        val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        if (recycle && ret != src && !src.isRecycled) src.recycle()
        return ret
    }

    /**
     * 保存 Bitmap 到文件。
     *
     * @param src 待保存的 Bitmap。
     * @param file 目标文件。
     * @param format 图片格式，默认为 JPEG 格式。
     * @param quality 图片质量，取值范围 0-100，0 表示最小尺寸，100 表示最高质量。
     * @param recycle 是否在保存后回收源 Bitmap，默认为 false。
     * @return true 表示保存成功，false 表示保存失败。
     */
    fun save(
        src: Bitmap?,
        file: File?,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 100,
        recycle: Boolean = false,
    ): Boolean {
        if (src == null || file == null || src.isRecycled || src.width == 0 || src.height == 0) return false
        if (!FileUtils.createFileByDeleteOldFile(file)) {
            Log.e("ImageUtils", "创建或删除文件失败: $file")
            return false
        }
        var outputStream: OutputStream? = null
        var success = false
        try {
            outputStream = BufferedOutputStream(FileOutputStream(file))
            success = src.compress(format, quality, outputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (recycle && !src.isRecycled) src.recycle()
            outputStream?.closeQuietly()
        }
        return success
    }

    /**
     * 关闭 OutputStream，忽略可能发生的 IOException。
     */
    private fun OutputStream?.closeQuietly() {
        try {
            this?.close()
        } catch (e: IOException) {
            // 忽略异常
        }
    }

    /**
     * 判断文件是否为图片。
     *
     * @param file 待判断的文件。
     * @return true 表示是图片，false 表示不是图片或文件不存在。
     */
    fun isImage(file: File?): Boolean {
        if (file == null || !file.exists()) return false
        return isImage(file.path)
    }

    /**
     * 判断文件路径是否为图片。
     *
     * @param filePath 文件路径。
     * @return true 表示是图片，false 表示不是图片或路径为空。
     */
    fun isImage(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, options)
            options.outWidth > 0 && options.outHeight > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取 Bitmap 的尺寸。
     *
     * @param filePath 文件路径。
     * @return 包含宽度和高度的 IntArray，如果文件路径为空则返回 [0, 0]。
     */
    fun getSize(filePath: String?): IntArray {
        if (filePath.isNullOrBlank()) return intArrayOf(0, 0)
        return getSize(FileUtils.getFileByPath(filePath))
    }

    /**
     * 获取 Bitmap 的尺寸。
     *
     * @param file 文件。
     * @return 包含宽度和高度的 IntArray，如果文件为 null 则返回 [0, 0]。
     */
    fun getSize(file: File?): IntArray {
        if (file == null) return intArrayOf(0, 0)
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, opts)
        return intArrayOf(opts.outWidth, opts.outHeight)
    }

    /**
     * 计算缩放比例。
     *
     * @param options BitmapFactory.Options 对象。
     * @param maxWidth 最大宽度。
     * @param maxHeight 最大高度。
     * @return 缩放比例。
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int): Int {
        var height = options.outHeight
        var width = options.outWidth
        var inSampleSize = 1

        while (height > maxHeight || width > maxWidth) {
            height = height shr 1
            width = width shr 1
            inSampleSize = inSampleSize shl 1
        }
        return inSampleSize
    }

    /**
     * 从文件中加载 Bitmap，支持按照指定的最大宽度和高度进行缩放，并处理图片的旋转。
     *
     * @param file 图片文件。
     * @param maxWidth 最大宽度。
     * @param maxHeight 最大高度。
     * @param handleRotation 是否根据 EXIF 数据处理图片旋转，默认为 true。
     * @return 加载的 Bitmap，如果文件无效则返回 null。
     */
    fun getBitmap(
        file: File?,
        maxWidth: Int = 0,
        maxHeight: Int = 0,
        handleRotation: Boolean = true,
    ): Bitmap? {
        if (file == null) return null
        return getBitmap(file.absolutePath, maxWidth, maxHeight, handleRotation)
    }

    /**
     * 从指定的文件路径加载 Bitmap，支持按照指定的最大宽度和高度进行缩放，并处理图片的旋转。
     *
     * @param filePath 图片文件路径。
     * @param maxWidth 最大宽度。
     * @param maxHeight 最大高度。
     * @param handleRotation 是否根据 EXIF 数据处理图片旋转，默认为 true。
     * @return 加载的 Bitmap，如果文件路径为空或无效则返回 null。
     */
    fun getBitmap(
        filePath: String?,
        maxWidth: Int = 0,
        maxHeight: Int = 0,
        handleRotation: Boolean = true,
    ): Bitmap? {
        if (filePath.isNullOrBlank()) return null

        val bitmap = BitmapFactory.Options().run {
            if (maxWidth > 0 && maxHeight > 0) {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(filePath, this)
                inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                inJustDecodeBounds = false
            }
            BitmapFactory.decodeFile(filePath, this)
        }

        return if (handleRotation && bitmap != null) {
            val rotationDegree = getRotateDegree(filePath)
            if (rotationDegree != 0) {
                rotate(bitmap, rotationDegree, bitmap.width / 2f, bitmap.height / 2f, true)
            } else {
                bitmap
            }
        } else {
            bitmap
        }
    }

    /**
     * 为给定的 Bitmap 添加图片水印。
     *
     * @param src 源 Bitmap。
     * @param watermark 水印 Bitmap。
     * @param x 水印左上角 x 坐标，默认为 0。
     * @param y 水印左上角 y 坐标，默认为 0。
     * @param alpha 水印透明度，范围 0-255，默认为 255（不透明）。
     * @param recycle 是否在添加水印后回收源 Bitmap，默认为 false。
     * @return 添加了水印的 Bitmap，如果源 Bitmap 为空或已回收则返回 null。
     */
    fun addImageWatermark(
        src: Bitmap?,
        watermark: Bitmap?,
        x: Int = 0,
        y: Int = 0,
        alpha: Int = 255,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null || src.isRecycled) return null
        if (watermark == null || watermark.isRecycled) return src

        val result = src.copy(src.config, true)

        Canvas(result).apply {
            drawBitmap(watermark, x.toFloat(), y.toFloat(), Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.alpha = alpha
            })
        }

        if (recycle && result != src) src.recycle()

        return result
    }

    /**
     * 为给定的 Bitmap 添加文字水印。
     *
     * @param src 源 Bitmap。
     * @param content 水印文字内容。
     * @param textSize 文字大小。
     * @param color 文字颜色。
     * @param x 文字左下角 x 坐标，默认为 0。
     * @param y 文字左下角 y 坐标，默认为 0。
     * @param recycle 是否在添加水印后回收源 Bitmap，默认为 false。
     * @return 添加了文字水印的 Bitmap，如果源 Bitmap 为空或已回收或文字内容为空则返回 null。
     */
    fun addTextWatermark(
        src: Bitmap?,
        content: String?,
        textSize: Float,
        @ColorInt color: Int,
        x: Float = 0f,
        y: Float = 0f,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null || src.isRecycled || content.isNullOrEmpty()) return null

        val result = src.copy(src.config, true)

        Canvas(result).apply {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                this.textSize = textSize
                getTextBounds(content, 0, content.length, Rect())
                drawText(content, x, y + textSize, this)
            }
        }

        if (recycle && result != src) src.recycle()

        return result
    }

    /**
     * 判断 Bitmap 是否为空（null 或宽高为 0）。
     *
     * @param src 待判断的 Bitmap。
     * @return true 表示 Bitmap 为空，false 表示 Bitmap 不为空。
     */
    private fun isEmptyBitmap(src: Bitmap?): Boolean {
        return src == null || src.width == 0 || src.height == 0
    }

    /**
     * 获取图片的旋转角度。
     *
     * @param filePath 图片文件路径。
     * @return 图片的旋转角度，默认为 0。
     */
    private fun getRotateDegree(filePath: String): Int {
        try {
            val exifInterface = ExifInterface(filePath)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            Log.e("ImageUtils", "获取旋转角度失败: ${e.message}")
            return 0
        }
    }


    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(AppUtils.getApp(), id)
    }

    //通知相册扫描指定的图片
    fun notifyGallery(
        context: Context,
        imageFile: File?,
    ) {
        if (imageFile == null) {
            LogUtils.e("imageFile == null")
            return
        }
        // 通知媒体库更新
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 and above
            MediaScannerConnection.scanFile(
                context,
                arrayOf(imageFile.absolutePath),
                arrayOf("image/png")
            ) { path, uri ->
                // 扫描完成后的操作，如果需要的话
            }
        } else { // Below Android 10
            @Suppress("DEPRECATION")
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri = Uri.fromFile(imageFile)
            intent.data = uri
            context.sendBroadcast(intent)
        }

    }
}
