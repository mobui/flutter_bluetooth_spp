package ru.mobui.flutter_bluetooth_spp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.IOException
import java.nio.charset.Charset
import java.util.UUID
import kotlin.time.Duration

data class BluetoothConnection(
    val device: BluetoothDevice,
    val socket: BluetoothSocket,
    val reader: BluetoothReader
)

class BluetoothConnector(
    private val bluetoothAdapter: BluetoothAdapter?,
    private val address: String,
    private val charset: Charset = Charsets.UTF_8,
    private val reconnectTimeout: Long  = -1
) {

    private var bluetoothConnection: BluetoothConnection? = null
    private var onDataReceived: ((String) -> Unit)? = null

    fun connect(onDataReceived: (String) -> Unit):BluetoothConnector {
        this.onDataReceived = onDataReceived
        if (bluetoothAdapter == null) {
            throw Exception("Bluetooth adapter not found")
        }
        val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val device = bluetoothAdapter.getRemoteDevice(address)
        var bluetoothSocket: BluetoothSocket? = null

        if (device == null) {
            throw Exception("Device not found $address")
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(sppUUID)!!
            bluetoothAdapter.cancelDiscovery()
            bluetoothSocket.connect()
            val reader = BluetoothReader(bluetoothSocket, onDataReceived, ::onError)
            bluetoothConnection = BluetoothConnection(device, bluetoothSocket, reader)
            bluetoothConnection?.reader?.startReading(charset)
        } catch (e: IOException) {
            disconnect()
            throw Exception("Error connecting to device ${device.name}, ${device.address}")
        }
        return this
    }

    fun onError(e: Exception) {
        disconnect()
        if(reconnectTimeout > 0) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(reconnectTimeout)
                reconnect()
            }
        }
    }

    fun reconnect() {
        val onDataReceived = this.onDataReceived!!
        try {
            disconnect()
            connect(onDataReceived)
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun disconnect() {
        try {
            bluetoothConnection?.reader?.stopReading()
            bluetoothConnection?.socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error disconnecting from device, ${bluetoothConnection?.device?.name}, ${bluetoothConnection?.device?.address}")
        } finally {
            bluetoothConnection = null
            onDataReceived = null
        }
    }

    fun getConnectedDevice() = bluetoothConnection?.device
}

fun BluetoothDevice.toMap(): Map<String, String> {
    return mapOf(
        "name" to (this.name ?: "Unknown"),
        "address" to this.address
    )
}