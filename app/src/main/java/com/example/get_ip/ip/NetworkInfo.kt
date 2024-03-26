package com.example.get_ip.ip

import android.content.Context
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.wifi.WifiManager

object NetworkInfo {

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return networkInfo?.isConnected ?: false
    }

    fun getDhcpInfo(context: Context): DhcpInfo? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.dhcpInfo
    }

    fun getGatewayAddress(context: Context): String {
        val dhcpInfo = getDhcpInfo(context)
        return Utils.parseIpAddress(dhcpInfo?.gateway ?: 0)!!
    }

    fun getDeviceIpAddress(context: Context): String {
        val dhcpInfo = getDhcpInfo(context)
        return Utils.parseIpAddress(dhcpInfo?.ipAddress ?: 0)!!
    }
}