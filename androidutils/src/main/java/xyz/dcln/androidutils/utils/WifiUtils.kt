package xyz.dcln.androidutils.utils

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

object WifiUtils {

    private var wifiScanReceiver: BroadcastReceiver? = null

    private var wifiStateReceiver: BroadcastReceiver? = null

    private var wifiStateChangeListener: ((Boolean) -> Unit)? = null

    private fun getWifiManager(context: Context): WifiManager? {
        return context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }

    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = getWifiManager(context)
        return wifiManager?.isWifiEnabled == true
    }

    @RequiresPermission(anyOf = [permission.CHANGE_WIFI_STATE ])
    fun setWifiEnabled(context: Context, enabled: Boolean, onWifiStateChanged: ((Boolean) -> Unit)? = null) {
        val wifiManager = getWifiManager(context)

        // If callback is provided, register the receiver
        if (onWifiStateChanged != null) {
            registerWifiStateReceiver(context, onWifiStateChanged)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Open system UI for the user to enable/disable Wi-Fi
            context.startActivity(Intent(Settings.Panel.ACTION_WIFI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } else {
            @Suppress("DEPRECATION")
            wifiManager?.isWifiEnabled = enabled
        }


    }

    private fun registerWifiStateReceiver(context: Context, listener: (Boolean) -> Unit) {
        unregisterWifiStateReceiver(context)  // First, ensure any previous receiver is unregistered

        wifiStateChangeListener = listener

        wifiStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                    WifiManager.WIFI_STATE_ENABLED -> {
                        wifiStateChangeListener?.invoke(true)
                    }
                    WifiManager.WIFI_STATE_DISABLED -> {
                        wifiStateChangeListener?.invoke(false)
                    }
                    // Other states can be handled if needed
                }
            }
        }

        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiStateReceiver, intentFilter)

        // If context is a LifecycleOwner, observe its lifecycle to unregister the receiver automatically
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_DESTROY -> {
                            unregisterWifiStateReceiver(context)
                            context.lifecycle.removeObserver(this)
                        }

                        else -> {
                        }
                    }
                }
            })
        }
    }

    fun unregisterWifiStateReceiver(context: Context) {
        if (wifiStateReceiver != null) {
            context.unregisterReceiver(wifiStateReceiver)
            wifiStateReceiver = null
            wifiStateChangeListener = null
        }
    }


    @RequiresPermission(anyOf = [permission.ACCESS_WIFI_STATE, permission.ACCESS_FINE_LOCATION])
    fun requestWifiScan(
        context: Context,
        onScanResultsAvailable: (List<ScanResult>?) -> Unit,
        onError: (reason: String?) -> Unit
    ) {

        val wifiManager = getWifiManager(context)
        if (!hasRequiredPermissions(context)) {
            onError("缺少必要权限")
            return
        }

        if (!isLocationEnabled(context)) {
            // Notify the user or direct them to settings
            onError("位置服务未开启")
            return
        }

        // Register the BroadcastReceiver if it's null
        if (wifiScanReceiver == null) {
            wifiScanReceiver = object : BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                override fun onReceive(c: Context, intent: Intent) {
                    if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                        if (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {
                            onScanResultsAvailable(wifiManager?.scanResults)
                        } else {
                            onError("limit")  // Handle rate limit exceeded or any other failure
                        }
                    }
                }
            }

            val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(wifiScanReceiver, intentFilter)

            // If context is a LifecycleOwner, observe its lifecycle to unregister the receiver automatically
            if (context is LifecycleOwner) {
                context.lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        when (event) {
                            Lifecycle.Event.ON_DESTROY -> {
                                unregisterWifiScanReceiver(context)
                                context.lifecycle.removeObserver(this)
                            }

                            else -> {
                            }
                        }
                    }
                })
            }
        }
        // Start the Wi-Fi scan  应用在后台每2分钟只能扫描4次，而在前台每2分钟只能扫描次数是之前的2倍。
        // wifiManager?.scanResults 这个是拿wifi缓存
        @Suppress("DEPRECATION")
        CoroutineUtils.launchOnIO {
            wifiManager?.startScan()
        }

    }

    fun unregisterWifiScanReceiver(context: Context) {
        if (wifiScanReceiver != null) {
            context.unregisterReceiver(wifiScanReceiver)
            wifiScanReceiver = null
        }
    }

    /**
     * Determines if the device is within the range of a specific Wi-Fi network by checking for the BSSID and, optionally, the SSID.
     *
     * @param context Android context.
     * @param bssidTarget The target BSSID to check against. Note: This might not be the physical MAC of the router.
     * @param ssidTarget (Optional) The target SSID to check against. If not provided, only BSSID is used for verification.
     * @param onResult Callback that returns `true` if the target Wi-Fi is in range, `false` otherwise.
     * @param onError Callback for handling errors, returns an error reason string.
     */

    @RequiresPermission(anyOf = [permission.ACCESS_WIFI_STATE, permission.ACCESS_FINE_LOCATION])
    fun isWithinWifiRange(
        context: Context,
        bssidTarget: String,
        onResult: (result: Boolean) -> Unit,
        onError: (reason: String?) -> Unit
    ) {
        requestWifiScan(context, { scanResults ->
            val isTargetWifiInRange = scanResults?.any { it.BSSID == bssidTarget } == true
            onResult(isTargetWifiInRange)
        }, onError)
    }

    @RequiresPermission(anyOf = [permission.ACCESS_WIFI_STATE, permission.ACCESS_FINE_LOCATION])
    fun isWithinWifiRange(
        context: Context,
        bssidTargets: List<String>,
        onResult: (result: Boolean) -> Unit,
        onError: (reason: String?) -> Unit
    ) {
        requestWifiScan(context, { scanResults ->
            val isTargetWifiInRange = scanResults?.any { scanResult ->
                bssidTargets.contains(scanResult.BSSID)
            } == true
            onResult(isTargetWifiInRange)
        }, onError)
    }



    /**
     * Connects to a specified Wi-Fi network.
     *
     * @param context Android context.
     * @param ssid SSID of the Wi-Fi network to connect to.
     * @param password Password of the Wi-Fi network.
     * @param onSuccess Callback function to be invoked when successfully connected.
     * @param onFailure Callback function to be invoked when connection fails.
     */
    fun connectToWifi(
        context: Context,
        ssid: String,
        password: String? = null,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及更高版本
            connectToWifiQAndAbove(context, ssid, password, onSuccess, onFailure)
        } else {
            // Android 10以下版本
            val success = connectToWifiPreQ(context, ssid, password)
            if (success) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWifiQAndAbove(
        context: Context,
        ssid: String,
        password: String?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val builder = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)

        // If a password is provided, use it.
        password?.let {
            builder.setWpa2Passphrase(it)
        }

        val wifiNetworkSpecifier = builder.build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                onSuccess()
                connectivityManager.unregisterNetworkCallback(this)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                onFailure()
                connectivityManager.unregisterNetworkCallback(this)
            }
        }

        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    @Suppress("DEPRECATION")
    private fun connectToWifiPreQ(context: Context, ssid: String, password: String?): Boolean {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }

        val wifiConfig = WifiConfiguration().apply {
            SSID = "\"" + ssid + "\""
            // If a password is provided, use it.
            if (!password.isNullOrEmpty()) {
                preSharedKey = "\"" + password + "\""
            } else {
                // For open networks
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
        }

        val netId = wifiManager.addNetwork(wifiConfig)
        if (netId == -1) {
            return false
        }

        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()

        return true
    }


    private fun hasRequiredPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission.ACCESS_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
    }

}
