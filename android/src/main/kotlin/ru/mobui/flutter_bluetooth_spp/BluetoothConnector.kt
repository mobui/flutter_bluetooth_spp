package ru.mobui.flutter_bluetooth_spp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import io.flutter.plugin.common.MethodChannel.Result
import java.io.IOException
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

object BluetoothConnector {
    private var bluetoothConnection: BluetoothConnection? = null

    fun connect(bluetoothAdapter: BluetoothAdapter, address: String, onDataReceived: (String) -> Unit ): Unit {
        val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val device = bluetoothAdapter?.getRemoteDevice(address)
        var  bluetoothSocket: BluetoothSocket? = null

        try {
            bluetoothSocket = device?.createRfcommSocketToServiceRecord(sppUUID)
            bluetoothAdapter?.cancelDiscovery() //
            bluetoothSocket?.connect()
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                bluetoothSocket?.close()
            } catch (_: IOException) { }
        }
        val reader =  BluetoothReader(bluetoothSocket!!, onDataReceived);
        bluetoothConnection = BluetoothConnection(device!!, bluetoothSocket, reader)
        bluetoothConnection?.reader?.startReading()
    }

    fun disconnect() {
        bluetoothConnection?.close();
        bluetoothConnection = null;
    }

    fun getConnectedDevice(result: Result) = bluetoothConnection?.device
}