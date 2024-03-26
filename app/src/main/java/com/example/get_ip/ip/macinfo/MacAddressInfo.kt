package com.example.get_ip.ip.macinfo

import android.content.Context
import com.example.get_ip.ip.DeviceFinder
import com.example.get_ip.ip.NetworkInfo
import com.example.get_ip.ip.model.DeviceItem
import com.example.get_ip.ip.vendor.VendorInfo
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

object MacAddressInfo {
    /**
     * Runs command "ip n"
     * Sample result:
     * 192.168.1.1 dev wlan0 lladdr c4:70:0b:17:79:b8 REACHABLE
     * 192.168.1.144 dev wlan0  FAILED
     * 192.168.1.35 dev wlan0  FAILED
     * 192.168.1.178 dev wlan0  FAILED
     * 192.168.1.45 dev wlan0 lladdr 0a:e0:af:b1:77:7f REACHABLE
     */
    fun setMacAddress(context: Context, deviceItems: List<DeviceItem>) {
        val deviceItemHashMap = HashMap<String, DeviceItem>()

        for (deviceItem in deviceItems) {
            deviceItemHashMap[deviceItem.ipAddress] = deviceItem
        }

        val currentDeviceIpAddress = NetworkInfo.getDeviceIpAddress(context)
        val currentDeviceItem = deviceItemHashMap[currentDeviceIpAddress]

        if (currentDeviceItem != null) {
            val currentDeviceMacAddress = getCurrentDeviceMacAddress(currentDeviceIpAddress)
            currentDeviceItem.macAddress = currentDeviceMacAddress
            currentDeviceItem.vendorName = VendorInfo.getVendorName(context, currentDeviceMacAddress)
        }

        val runtime = Runtime.getRuntime()

        try {
            val process = runtime.exec("ip n")
            process.waitFor()

            val exitCode = process.exitValue()

            if (exitCode != 0) {
                return
            }

            val inputStreamReader = InputStreamReader(process.inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var line: String?
            var macAddress: String
            var ipAddress: String
            var values: Array<String>

            while (bufferedReader.readLine().also { line = it } != null) {
                values = line!!.split(" ").toTypedArray()

                if (values.size == 6) {
                    ipAddress = values[0]
                    macAddress = values[4]

                    val deviceItem = deviceItemHashMap[ipAddress]

                    if (deviceItem != null) {
                        deviceItem.macAddress = macAddress
                        deviceItem.vendorName = VendorInfo.getVendorName(context, deviceItem.macAddress)
                    }
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun getCurrentDeviceMacAddress(ipAddress: String): String {
        return try {
            val localIP = InetAddress.getByName(ipAddress)
            val networkInterface = NetworkInterface.getByInetAddress(localIP)

            if (networkInterface == null) {
                DeviceFinder.UNKNOWN
            } else {
                val hardwareAddress = networkInterface.hardwareAddress ?: return DeviceFinder.UNKNOWN

                val stringBuilder = StringBuilder(18)
                for (b in hardwareAddress) {
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.append(":")
                    }

                    stringBuilder.append(String.format("%02x", b))
                }

                stringBuilder.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            DeviceFinder.UNKNOWN
        }
    }
}
