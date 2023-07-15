package xyz.dcln.androidutils.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.resumeWithException


object WiFiTransferUtils {
    private var serverSocket: ServerSocket? = null
    private var isListening: Boolean = false

    fun sendMessage(
        hostAddress: String,
        port: Int,
        message: String,
        messageSentCallback: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = send(hostAddress, port, message)
            messageSentCallback(result)
        }
    }

    fun startListening(
        port: Int,
        startCallback: (Boolean) -> Unit,
        messageReceivedCallback: (String, String) -> Unit,
        errorHandler: (String) -> Unit
    ) {
        if (isListening) {
            // 已经在监听中，无需重复启动
            startCallback.invoke(true)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            startCallback.invoke(true)
            isListening = true
            while (isListening) {
                try {
                    serverSocket = ServerSocket(port)
                    val clientSocket = serverSocket?.accept()
                    val (message, ipAddress) = receive(clientSocket)
                    messageReceivedCallback(message, ipAddress)
                    serverSocket?.close() // 接收完消息后关闭连接
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorHandler("接收消息时发生错误")
                } finally {
                    serverSocket = null // 重置ServerSocket
                }
            }
        }
    }

    fun stopListening() {
        isListening = false
        serverSocket?.close()
        serverSocket = null
    }

    private suspend fun send(hostAddress: String, port: Int, message: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            var socket: Socket? = null
            try {
                socket = Socket(hostAddress, port)
                val outputStream = socket.getOutputStream()
                outputStream.write(message.toByteArray())
                outputStream.flush()
                continuation.resume(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(false, null)
            } finally {
                socket?.close() // 关闭连接
            }
        }

    private suspend fun receive(clientSocket: Socket?): Pair<String, String> =
        suspendCancellableCoroutine { continuation ->
            var inputStream: BufferedInputStream? = null
            var outputStream: ByteArrayOutputStream? = null
            try {
                inputStream = BufferedInputStream(clientSocket?.getInputStream())
                outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }
                val message = outputStream.toString()
                val ipAddress = clientSocket?.inetAddress?.hostAddress ?: ""
                continuation.resume(Pair(message, ipAddress), null)
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resumeWithException(e)
            } finally {
                inputStream?.close()
                outputStream?.close()
                clientSocket?.close()
            }
        }

    //--------------------------------------------
    fun sendFile(
        hostAddress: String,
        port: Int,
        file: File,
        fileSentCallback: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = sendFile(hostAddress, port, file)
            fileSentCallback(result)
        }
    }

    private suspend fun sendFile(hostAddress: String, port: Int, file: File): Boolean =
        suspendCancellableCoroutine { continuation ->
            var socket: Socket? = null
            var fileInputStream: FileInputStream? = null
            try {
                socket = Socket(hostAddress, port)
                val outputStream = socket.getOutputStream()
                fileInputStream = FileInputStream(file)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
                continuation.resume(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(false, null)
            } finally {
                fileInputStream?.close()
                socket?.close()
            }
        }

    fun receiveFile(
        port: Int,
        saveDirectory: String,
        fileReceivedCallback: (Boolean, File?) -> Unit,
        errorHandler: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = receiveFile(port, saveDirectory)
            if (result.first) {
                fileReceivedCallback(true, result.second)
            } else {
                errorHandler()
            }
        }
    }

    private suspend fun receiveFile(port: Int, saveDirectory: String): Pair<Boolean, File?> =
        suspendCancellableCoroutine { continuation ->
            var serverSocket: ServerSocket? = null
            var clientSocket: Socket? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                serverSocket = ServerSocket(port)
                clientSocket = serverSocket.accept()
                val inputStream = clientSocket.getInputStream()

                val fileNameSizeBytes = ByteArray(4)
                inputStream.read(fileNameSizeBytes)
                val fileNameSize = byteArrayToInt(fileNameSizeBytes)

                val fileNameBytes = ByteArray(fileNameSize)
                inputStream.read(fileNameBytes)
                val fileName = String(fileNameBytes)

                val file = File(saveDirectory, fileName)
                fileOutputStream = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }
                fileOutputStream.flush()

                val absolutePath = file.absolutePath
                continuation.resume(Pair(true, File(absolutePath)), null)
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(Pair(false, null), null)
            } finally {
                fileOutputStream?.close()
                clientSocket?.close()
                serverSocket?.close()
            }
        }

    private fun byteArrayToInt(byteArray: ByteArray): Int {
        return byteArray[0].toInt() and 0xFF shl 24 or
                (byteArray[1].toInt() and 0xFF) shl 16 or
                (byteArray[2].toInt() and 0xFF) shl 8 or
                (byteArray[3].toInt() and 0xFF)
    }

}
