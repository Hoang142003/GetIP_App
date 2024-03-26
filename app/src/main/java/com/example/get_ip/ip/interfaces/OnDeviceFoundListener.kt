package com.example.get_ip.ip.interfaces

import com.example.get_ip.ip.DeviceFinder
import com.example.get_ip.ip.model.DeviceItem

interface OnDeviceFoundListener {
    fun onStart(deviceFinder: DeviceFinder?)
    fun onFinished(deviceFinder: DeviceFinder?, deviceItems: List<DeviceItem>)
    fun onFailed(deviceFinder: DeviceFinder?, errorCode: Int)
}