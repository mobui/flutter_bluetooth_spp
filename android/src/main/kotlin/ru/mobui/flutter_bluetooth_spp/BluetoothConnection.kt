package ru.mobui.flutter_bluetooth_spp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket

data class BluetoothConnection(
    val device: BluetoothDevice,
    val socket: BluetoothSocket,
    val reader: BluetoothReader
) {
    fun close() {
        reader.stopReading()
        socket.close()
    }
}
