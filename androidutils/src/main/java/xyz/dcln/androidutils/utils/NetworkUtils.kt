package xyz.dcln.androidutils.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnUI
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException


object NetworkUtils {

    private val connectivityManager: ConnectivityManager by lazy {
        AppUtils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val wifiManager: WifiManager by lazy {
        AppUtils.getApp().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private const val INTERNET_CAPABILITY = NetworkCapabilities.NET_CAPABILITY_INTERNET

    private val networkCallbacksList: MutableList<ConnectivityManager.NetworkCallback> =
        mutableListOf()

    /**
     * 检查当前是否有网络连接。
     *
     * @return 当前是否有网络连接
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(INTERNET_CAPABILITY)
    }


    /**
     * 断开当前的网络连接。
     *
     * @return 是否成功断开网络连接
     */
    @RequiresPermission(allOf = [Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_NETWORK_STATE])
    fun disconnectNetwork(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        return try {
            connectivityManager.bindProcessToNetwork(null)
            connectivityManager.unregisterNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {})//空回调用于取消注册
            connectivityManager.bindProcessToNetwork(network)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * 检查当前是否有网络连接，并且网络可用。
     *
     * @return 当前是否有网络连接并且网络可用
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isConnectedAndAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return hasInternetAndValidatedCapabilities(capabilities)
    }

    /**
     * 检查网络是否已连接并可用。
     *
     * @param networkCapabilities 网络能力
     * @return 如果网络已连接并可用，则为 true；否则为 false
     */
    private fun hasInternetAndValidatedCapabilities(networkCapabilities: NetworkCapabilities?): Boolean {
        return networkCapabilities?.hasCapability(INTERNET_CAPABILITY) == true
                && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * 网络类型枚举类。
     */
    enum class NetworkType {
        WIFI,   // Wi-Fi网络
        MOBILE, // 移动数据网络
        UNKNOWN, // 未知网络类型
        NONE    // 无网络连接
    }

    /**
     * 获取当前网络类型。
     *
     * @return 当前网络类型
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.MOBILE
            else -> NetworkType.UNKNOWN
        }
    }

    /**
     * 注册网络回调，监听网络连接状态变化。
     *
     * @param onConnected 当网络连接可用时执行的回调函数
     * @param onDisconnected 当网络连接断开时执行的回调函数
     * @return 注册的网络回调对象
     */
    @RequiresPermission(allOf = [Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_NETWORK_STATE])
    fun registerNetworkCallback(
        onConnected: () -> Unit,
        onDisconnected: () -> Unit
    ): ConnectivityManager.NetworkCallback {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(INTERNET_CAPABILITY)
            .build()

        var hisNetworkConnected = false // 记录网络是否连接

        val networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (!hisNetworkConnected) { // 当前网络之前未连接，现在变为连接状态
                    hisNetworkConnected = true
                    onConnected()
                }
            }

            @RequiresPermission(allOf = [Manifest.permission.ACCESS_NETWORK_STATE])
            override fun onLost(network: Network) {
                super.onLost(network)
                if (hisNetworkConnected) { // 网络之前已连接，现在断开连接
                    CoroutineUtils.launchOnUI(800) {//关闭wifi，移动数据恢复需要时间
                        if (!isConnectedAndAvailable()) {
                            hisNetworkConnected = false
                            onDisconnected()
                        }
                    }
                }
            }

            @RequiresPermission(allOf = [Manifest.permission.ACCESS_NETWORK_STATE])
            override fun onUnavailable() {
                super.onUnavailable()
                if (hisNetworkConnected) { // 网络之前已连接，现在无法连接
                    CoroutineUtils.launchOnUI(800) {//移动数据恢复需要时间
                        if (!isConnectedAndAvailable()) {
                            hisNetworkConnected = false
                            onDisconnected()
                        }
                    }
                }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        networkCallbacksList.add(networkCallback)

        return networkCallback
    }


    /**
     * 取消注册网络回调。
     *
     * @param networkCallback 要取消的网络回调对象
     */
    fun unregisterNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        networkCallbacksList.remove(networkCallback)
    }

    /**
     * 取消注册所有网络回调。
     */
    fun unregisterAllNetworkCallbacks() {
        for (networkCallback in networkCallbacksList) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
        networkCallbacksList.clear()
    }


    /**
     * 获取附近的 Wi-Fi 列表。
     *
     * @return 附近的 Wi-Fi 列表
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION])
    fun getNearbyWifiList(): List<ScanResult>? {
        if (ActivityCompat.checkSelfPermission(
                AppUtils.getApp(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        return wifiManager.scanResults
    }

    /**
     * 获取设备的IPv4地址。
     *
     * @return 设备的IPv4地址，如果获取失败则返回空
     */
    fun getIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取设备的IPv6地址。
     *
     * @return 设备的IPv6地址，如果获取失败则返回空
     */
    fun getIpv6Address(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet6Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // 获取本机wifi对应的ip地址，返回一个字符串，如"192.168.1.100"
    fun getWifiIpAddress(): String? {
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return intToInetAddress(ipAddress).hostAddress
    }

    // 将int类型的ip地址转换为InetAddress对象
    private fun intToInetAddress(hostAddress: Int): InetAddress {
        val addressBytes = byteArrayOf(
            (0xff and hostAddress).toByte(),
            (0xff and (hostAddress shr 8)).toByte(),
            (0xff and (hostAddress shr 16)).toByte(),
            (0xff and (hostAddress shr 24)).toByte()
        )
        return try {
            InetAddress.getByAddress(addressBytes)
        } catch (e: UnknownHostException) {
            throw AssertionError()
        }
    }
}

