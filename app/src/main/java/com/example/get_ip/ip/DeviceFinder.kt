package com.example.get_ip.ip

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.get_ip.ip.interfaces.OnDeviceFoundListener
import com.example.get_ip.ip.macinfo.MacAddressInfo
import com.example.get_ip.ip.model.DeviceItem
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DeviceFinder(private val context: Context, private val onDeviceFoundListener: OnDeviceFoundListener) {

    companion object {
        const val UNKNOWN = "UnKnown"
        const val ERROR_USER_STOPPED = 0
        const val WIFI_NOT_CONNECTED = 2
        const val OTHERS = 3
    }

    private var executorService: ExecutorService? = null
    private var isRunning = false
    private var timeout = 500
    private var stopRequested = false

    private val reachableDevices = mutableListOf<DeviceItem>()

    private val handler = Handler(Looper.getMainLooper())

    fun setTimeout(timeout: Int): DeviceFinder {
        this.timeout = timeout
        return this
    }

    fun start() {
        isRunning = true
        stopRequested = false

        reachableDevices.clear()

        Thread { startPing() }.start()
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    fun stop() {
        stopRequested = true
        executorService?.shutdownNow()

        if (isRunning) {
            sendFailedEvent(ERROR_USER_STOPPED)
        }

        isRunning = false
    }

    private fun sendStartEvent() {
        handler.post { onDeviceFoundListener.onStart(this) }
    }

    private fun sendFailedEvent(errorCode: Int) {
        handler.post { onDeviceFoundListener.onFailed(this, errorCode) }
    }

    private fun sendFinishedEvent(deviceItems: List<DeviceItem>) {
        handler.post { onDeviceFoundListener.onFinished(this, deviceItems) }
    }

    private fun isBelowAndroidR(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R
    }

    private fun startPing() {
        if (!NetworkInfo.isWifiConnected(context)) {
            isRunning = false
            sendFailedEvent(WIFI_NOT_CONNECTED)
            return
        }

        executorService = Executors.newFixedThreadPool(255)
        sendStartEvent()

        val gatewayAddress = NetworkInfo.getGatewayAddress(context)
        val lastDotIndex = gatewayAddress.lastIndexOf(".")

        val ipPrefix = gatewayAddress.substring(0, lastDotIndex + 1)

        var ipAddressToPing: String

        try {
            for (i in 0 until 255) {
                ipAddressToPing = ipPrefix + (i + 1)
                executorService?.execute(Ping(ipAddressToPing))
            }
        } catch (e: Exception) {
            e.printStackTrace()

            if (stopRequested) {
                sendFailedEvent(ERROR_USER_STOPPED)
            } else {
                sendFailedEvent(OTHERS)
            }

            return
        }

        executorService?.shutdown()

        try {
            val wait = executorService?.awaitTermination(10, TimeUnit.MINUTES) ?: false

            if (wait) {
                if (isBelowAndroidR()) {
                    MacAddressInfo.setMacAddress(context, reachableDevices)
                }
                sendFinishedEvent(reachableDevices)
            } else {
                sendFailedEvent(OTHERS)
            }

        } catch (e: InterruptedException) {
            if (stopRequested) {
                sendFailedEvent(ERROR_USER_STOPPED)
            } else {
                sendFailedEvent(OTHERS)
            }

            e.printStackTrace()
        }

        isRunning = false
    }

    inner class Ping(private val ipAddress: String) : Runnable {

        override fun run() {
            try {
                val inetAddress = InetAddress.getByName(ipAddress)

                if (Thread.currentThread().isInterrupted) {
                    return
                }

                if (inetAddress.isReachable(timeout)) {
                    val deviceItem = DeviceItem()
                    // Vendor name and mac address still not set
                    deviceItem.ipAddress = ipAddress
                    deviceItem.deviceName = inetAddress.hostName
                    reachableDevices.add(deviceItem)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
