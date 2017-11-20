package com.vitalyilchenko.robocontrol

/**
 * Created by vitalyilchenko on 11/14/17.
 */
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import com.vitalyilchenko.robocontrol.Models.BluetoothItem
import java.util.*
import java.util.concurrent.TimeUnit

object BluetoothService {

    private val leServiceID: String = "0000ffe0-0000-1000-8000-00805f9b34fb"
    private val leCharacteristicID: String = "0000ffe1-0000-1000-8000-00805f9b34fb"

    private val _gattCallback: GattCallback = GattCallback(this)
    private val _scanCallback: LeScanCallback = LeScanCallback(this)
    private var _bluetoothManager: BluetoothManager? = null
    private var _connectedGatt: BluetoothGatt? = null
    private var _leCharacteristic: BluetoothGattCharacteristic? = null

    private var _lastCommand: String = ""
    private var _isWriting: Boolean = false

    private val _deviceCache: MutableList<BluetoothItem> = ArrayList()

    var onDeviceDiscovered = fun(devices: List<BluetoothItem>): Unit = null!!

    var onDeviceStateChanged = fun(isConnected: Boolean): Unit = null!!

    var connectedDevice: BluetoothDevice? = null
        private set

    fun init(context: Context) {
        _bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    fun isConnected(): Boolean {
        //var state = _bluetoothManager?.getConnectionState(connectedDevice, BluetoothGatt.GATT)
        return _connectedGatt != null
                //&& state == BluetoothProfile.STATE_CONNECTED
    }

    var isScanning: Boolean = false
        private set

    fun write(command: String)
    {
        if (isConnected() && _leCharacteristic != null) {
            _lastCommand = command
            if (!_isWriting) {
                _isWriting = true
                _leCharacteristic?.setValue(command)
                _connectedGatt?.writeCharacteristic(_leCharacteristic)
            }
        }
    }

    fun startScan() {
        _deviceCache.clear()
        isScanning = true
        _bluetoothManager?.adapter?.bluetoothLeScanner?.startScan(_scanCallback)
    }

    fun stopScan() {
        isScanning = false
        _bluetoothManager?.adapter?.bluetoothLeScanner?.stopScan(_scanCallback)
    }

    fun connect(context: Context, address: String) {
        if (isScanning)
            stopScan()

        var item = _deviceCache.find { d -> d.address == address }
        if (item != null) {
            connectedDevice = item.device
            _connectedGatt = connectedDevice?.connectGatt(context, true, _gattCallback)
        }
    }

    fun disconnect() {
        if (_connectedGatt != null) {
            var item = _deviceCache.find { i -> i.address == connectedDevice?.address }
            item?.state = "Disconnected"

            _connectedGatt?.disconnect()
            _connectedGatt?.close()

            _connectedGatt = null
            connectedDevice = null
        }
    }

    private fun handleScanResult(result: ScanResult) {
        if (_deviceCache.count({ d -> d.address == result.device.address}) == 0)
        {
            _deviceCache.add(BluetoothItem(result.device))
            onDeviceDiscovered(_deviceCache)
        }
    }

    private fun handleServiceDiscovered() {
        onDeviceStateChanged(true)
        var item = _deviceCache.find { i -> i.address == connectedDevice?.address }
        item?.state = "Connected"
    }

    private fun handleCharacteristicWrite(characteristic: BluetoothGattCharacteristic?) {
        if (characteristic != null) {
            var value = characteristic.getStringValue(0)
            if (value != _lastCommand) {

                // we need to make a delay before sending the next command
                TimeUnit.MILLISECONDS.sleep(100)

                Log.i("ROBOT_COMMAND", _lastCommand)

                _leCharacteristic?.setValue(_lastCommand)
                _connectedGatt?.writeCharacteristic(_leCharacteristic)
            }
            else {
                _isWriting = false
            }
        }
    }

    private fun handleConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                _connectedGatt?.discoverServices()
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                onDeviceStateChanged(false)
                if (connectedDevice != null) {
                    var item = _deviceCache.find { i -> i.address == connectedDevice?.address }
                    item?.state = "Disconnected"
                }
            }
        }
    }

    private class LeScanCallback(val btService: BluetoothService) : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result != null)
                btService.handleScanResult(result)
        }
    }

    private class GattCallback(val btService: BluetoothService) : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            btService.handleConnectionStateChange(gatt, status, newState)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            btService.handleCharacteristicWrite(characteristic)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                var service = _connectedGatt?.getService(UUID.fromString(leServiceID))
                if (service != null) {
                    var characteristic = service.getCharacteristic(UUID.fromString(leCharacteristicID))
                    if (characteristic != null) {
                        _leCharacteristic = characteristic
                        btService.handleServiceDiscovered()
                    }
                    else {
                        btService.disconnect()
                    }
                }
                else {
                    btService.disconnect()
                }
            }
        }
    }
}