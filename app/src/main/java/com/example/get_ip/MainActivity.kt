package com.example.get_ip

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.get_ip.ip.DeviceFinder
import com.example.get_ip.ip.interfaces.OnDeviceFoundListener
import com.example.get_ip.ip.model.DeviceItem

class MainActivity : AppCompatActivity() {
    private val devices: MutableList<String> = ArrayList()
    private var start: Long = 0
    private var end: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.listview)
        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, devices
        )
        listView.setAdapter(arrayAdapter)
        val devicesFinder = DeviceFinder(this, object : OnDeviceFoundListener {
            override fun onStart(deviceFinder: DeviceFinder?) {
                start = System.currentTimeMillis()
            }

            override fun onFinished(deviceFinder: DeviceFinder?, deviceItems: List<DeviceItem>) {
                end = System.currentTimeMillis()
                val time: Float = (end - start) / 1000f
                Toast.makeText(
                    applicationContext, "Scan finished in " + time
                            + " seconds", Toast.LENGTH_SHORT
                ).show()
                for (deviceItem in deviceItems) {
                    val data = """
                    Device Name: ${deviceItem.deviceName}
                    Ip Address: ${deviceItem.ipAddress}
                    MAC Address: ${deviceItem.macAddress}
                    Vendor Name: ${deviceItem.vendorName}
                    """.trimIndent()
                    devices.add(data)
                }
                arrayAdapter.notifyDataSetChanged()
            }

            override fun onFailed(deviceFinder: DeviceFinder?, errorCode: Int) {
            }
        })
        devicesFinder.setTimeout(500).start()
    }
}