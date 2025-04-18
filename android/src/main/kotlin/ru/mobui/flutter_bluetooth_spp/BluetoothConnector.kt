package ru.mobui.flutter_bluetooth_spp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.nio.charset.Charset
import java.util.UUID

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

class  BluetoothConnector(private val bluetoothAdapter: BluetoothAdapter?) {
    private var bluetoothConnection: BluetoothConnection? = null

    fun connect(address: String,charset: Charset = Charsets.UTF_8, onDataReceived: (String) -> Unit ) {
        val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val device = bluetoothAdapter?.getRemoteDevice(address)
        var  bluetoothSocket: BluetoothSocket? = null

        if (device == null) {
            throw Exception("Device not found $address")
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(sppUUID)
            bluetoothAdapter?.cancelDiscovery() //
            bluetoothSocket?.connect()
            val reader =  BluetoothReader(bluetoothSocket!!, onDataReceived)
            bluetoothConnection = BluetoothConnection(device, bluetoothSocket, reader)
            bluetoothConnection?.reader?.startReading(charset)
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                bluetoothSocket?.close()
            } catch (_: IOException) { }
            throw  Exception("Error connecting to device ${device.name}, ${device.address}")
        }

    }

    fun disconnect() {
        try {
            bluetoothConnection?.close()

        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error disconnecting from device, ${bluetoothConnection?.device?.name}, ${bluetoothConnection?.device?.address}")
        }
        bluetoothConnection = null
    }

    fun getConnectedDevice() = bluetoothConnection?.device
}

fun BluetoothDevice.toMap(): Map<String, String> {
    return mapOf(
        "name" to (this.name ?: "Unknown"),
        "address" to this.address
    )
}