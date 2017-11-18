package com.vitalyilchenko.robocontrol.Models

import android.bluetooth.BluetoothDevice

/**
 * Created by vitalyilchenko on 11/14/17.
 */
class BluetoothItem(var device: BluetoothDevice) {
    val address: String
        get() { return device.address ?: "" }

    val name: String
            get() { return device.name ?: "" }

    var state: String = ""
}