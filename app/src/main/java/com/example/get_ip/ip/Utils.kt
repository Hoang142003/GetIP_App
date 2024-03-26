package com.example.get_ip.ip

import org.apache.commons.lang3.ArrayUtils
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException

object Utils {
    fun parseIpAddress(ip: Int): String? {
        try {
            val byteAddress = BigInteger.valueOf(ip.toLong()).toByteArray()
            ArrayUtils.reverse(byteAddress)
            return InetAddress.getByAddress(byteAddress).hostAddress
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }

        return null
    }
}