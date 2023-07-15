package xyz.dcln.androidutils.utils

import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption


/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/9 4:03
 */

object FileIOUtils {
    private const val DEFAULT_BUFFER_SIZE = 4096

    /**
     * 将输入流写入文件
     *
     * @param inputStream 输入流
     * @param file        目标文件
     */
    fun writeFileFromIS(inputStream: InputStream, file: File) {
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
        }
    }

    /**
     * 将字节数组写入文件（使用流）
     *
     * @param bytes 字节数组
     * @param file  目标文件
     */
    fun writeFileFromBytesByStream(bytes: ByteArray, file: File) {
        ByteArrayInputStream(bytes).use { inputStream ->
            writeFileFromIS(inputStream, file)
        }
    }

    /**
     * 将字节数组写入文件（使用通道）
     *
     * @param bytes 字节数组
     * @param file  目标文件
     */
    fun writeFileFromBytesByChannel(bytes: ByteArray, file: File) {
        FileChannel.open(
            file.toPath(),
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { channel ->
            val buffer = ByteBuffer.wrap(bytes)
            channel.write(buffer)
        }
    }


    /**
     * 将字节数组写入文件（使用 RandomAccessFile 和内存映射ByteBuffer）
     *
     * @param bytes 字节数组
     * @param file  目标文件
     */
    fun writeFileFromBytesByMap(bytes: ByteArray, file: File) {
        RandomAccessFile(file, "rw").use { randomAccessFile ->
            val channel = randomAccessFile.channel
            val buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, bytes.size.toLong())
            buffer.put(bytes)
            buffer.force()
        }
    }

    /**
     * 将字符串写入文件
     *
     * @param content 字符串内容
     * @param file    目标文件
     */
    fun writeFileFromString(content: String, file: File) {
        FileOutputStream(file).use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(content)
            }
        }
    }

    /**
     * 读取文件到字符串链表中
     *
     * @param file 目标文件
     * @return 字符串链表
     */
    fun readFile2List(file: File): List<String> {
        return file.useLines { it.toList() }
    }

    /**
     * 读取文件到字符串中
     *
     * @param file 目标文件
     * @return 字符串
     */
    fun readFile2String(file: File): String {
        return file.readText()
    }

    /**
     * 读取文件到字节数组中（使用流）
     *
     * @param file 目标文件
     * @return 字节数组
     */
    fun readFile2BytesByStream(file: File): ByteArray {
        FileInputStream(file).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                inputStream.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
                return outputStream.toByteArray()
            }
        }
    }

    /**
     * 读取文件到字节数组中（使用通道）
     *
     * @param file 目标文件
     * @return 字节数组
     */
    fun readFile2BytesByChannel(file: File): ByteArray {
        FileChannel.open(
            file.toPath(),
            StandardOpenOption.READ
        ).use { channel ->
            val size = channel.size().toInt()
            val buffer = ByteBuffer.allocate(size)
            channel.read(buffer)
            buffer.flip()
            return buffer.array()
        }
    }

    /**
     * 读取文件到字节数组中（使用 RandomAccessFile 和内存映射ByteBuffer）
     *
     * @param file 目标文件
     * @return 字节数组
     */
    fun readFile2BytesByMap(file: File): ByteArray {
        RandomAccessFile(file, "r").use { randomAccessFile ->
            val channel = randomAccessFile.channel
            val size = channel.size().toInt()
            val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size.toLong())
            val bytes = ByteArray(size)
            buffer.get(bytes)
            return bytes
        }
    }
}