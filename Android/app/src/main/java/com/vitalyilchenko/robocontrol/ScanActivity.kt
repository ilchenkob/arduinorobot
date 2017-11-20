package com.vitalyilchenko.robocontrol

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView

class ScanActivity : Activity() {

    private val SCAN_DURATION_MS: Long = 10_000

    private val scanHandler: Handler = Handler()
    private var refreshContainer: SwipeRefreshLayout? = null
    private var deviceList: ListView? = null
    private var listAdapter: DeviceListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        BluetoothService.init(this)
        listAdapter = DeviceListAdapter(this)
        refreshContainer = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        refreshContainer?.setOnRefreshListener{
            startScanning()
        }
        deviceList = findViewById(R.id.device_list)
        deviceList?.adapter = listAdapter
        deviceList?.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            stopIfScanning()
            var clickedItem = listAdapter!!.getItem(position)
            var connectedDeviceAddress = ""
            if (BluetoothService.isConnected()) {
                connectedDeviceAddress = BluetoothService.connectedDevice?.address ?: ""
                BluetoothService.disconnect()
            }

            if (connectedDeviceAddress != clickedItem.address)
                BluetoothService.connect(this, clickedItem.address)
        }

        BluetoothService.onDeviceDiscovered = { devices ->
            listAdapter?.setItems(devices)
        }
        BluetoothService.onDeviceStateChanged = { isConnected ->
            if (isConnected) {
                var controlIntent = Intent(this, ControlActivity::class.java)
                startActivity(controlIntent)
            }
        }

        checkPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_refresh) {
            startScanning()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private  fun checkPermissions() {
        var permissions = Array<String>(2, { "" })
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.set(0, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
            permissions.set(1, android.Manifest.permission.BLUETOOTH)
            requestPermissions(permissions, 11)
    }

    private fun startScanning() {
        stopIfScanning()

        refreshContainer?.isRefreshing = true
        BluetoothService.startScan()
        scanHandler.postDelayed({
            if (BluetoothService.isScanning) {
                BluetoothService.stopScan()
                refreshContainer?.isRefreshing = false
            }
        }, SCAN_DURATION_MS)
    }

    private fun stopIfScanning() {
        if (BluetoothService.isScanning) {
            scanHandler.removeCallbacksAndMessages(null)
            BluetoothService.stopScan()
            refreshContainer?.isRefreshing = false
        }
    }
}
