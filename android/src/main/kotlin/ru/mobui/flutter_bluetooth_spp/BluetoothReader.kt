package ru.mobui.flutter_bluetooth_spp

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

class BluetoothReader(
    private val socket: BluetoothSocket,
    private val onDataReceived: (String) -> Unit,
    private val onError: (Exception) -> Unit
) {

    private var readJob: Job? = null

    fun startReading(charset: Charset) {
        readJob = CoroutineScope(Dispatchers.IO).launch {
            val inputStream: InputStream = socket.inputStream
            val buffer = ByteArray(1024)

            try {
                while (isActive) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val message = buffer.copyOf(bytesRead).toString(charset)
                        launch(Dispatchers.Main) {
                            onDataReceived(message)
                        }
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onError(e)
                }
            } finally {
                inputStream.close()
                stopReading();
            }
        }
    }

    fun stopReading() {
        readJob?.cancel()
    }
}