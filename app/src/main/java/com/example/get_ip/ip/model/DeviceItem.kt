package com.example.get_ip.ip.model

import com.example.get_ip.ip.DeviceFinder

class DeviceItem {
    var ipAddress: String = DeviceFinder.UNKNOWN
    var macAddress: String = DeviceFinder.UNKNOWN
    var deviceName: String = DeviceFinder.UNKNOWN
    var vendorName: String = DeviceFinder.UNKNOWN

    fun isIpAddressAndDeviceNameSame(): Boolean {
        return ipAddress == deviceName
    }
}