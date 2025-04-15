package ru.mobui.flutter_bluetooth_spp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

/** FlutterBluetoothSppPlugin */
class FlutterBluetoothSppPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel : MethodChannel
  private lateinit var activity: Activity
  private var bluetoothAdapter: BluetoothAdapter? = null
  private var bluetoothConnection: BluetoothConnection? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_bluetooth_spp")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when(call.method){
      "requestPermissions"-> requestPermissions(result)
      "getBondedDevices" -> callWithRequestPermission(result){ getBondedDevices(it) }

    }
    if (call.method == "requestPermissions") {
      requestPermissions(result);
    } else {
      result.notImplemented()
    }
  }

  private fun requestPermissions(result: Result) {
    BluetoothPermissions.checkBluetoothPermissions(activity) {
      result.success(it)
    }
  }

  private fun callWithRequestPermission(result: Result, call: (result: Result) -> Unit): Unit {
    BluetoothPermissions.checkBluetoothPermissions(activity) {
      if (!it) {
        result.error("BLUETOOTH_PERMISSIONS_NOT_GRANTED", "Bluetooth permissions not granted", null)
        return@checkBluetoothPermissions
      }
      call.invoke(result)
    }
  }

  private fun getBondedDevices(result: Result): Unit{
    val adapter = bluetoothAdapter ?: return result.error("BLUETOOTH_NOT_AVAILABLE", "Bluetooth not available", null)
    val bondedDevices = adapter.bondedDevices.map { device ->
      mapOf(
        "name" to (device.name ?: "Unknown"),
        "address" to device.address
      )
    }
    result.success(bondedDevices)
  }

  fun connectToDevice(address: String, result: Result) {
    val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val device = bluetoothAdapter?.getRemoteDevice(address)
    var  bluetoothSocket: BluetoothSocket? = null

    try {
      bluetoothSocket = device?.createRfcommSocketToServiceRecord(sppUUID)
      bluetoothAdapter?.cancelDiscovery() //
      bluetoothSocket?.connect()

      result.success(true)
    } catch (e: IOException) {
      e.printStackTrace()
      result.error("BLUETOOTH_NOT_CONNECTED", "Bluetooth not connected with device $address", null)
      try {
        bluetoothSocket?.close()
      } catch (_: IOException) { }
    }
    bluetoothConnection = BluetoothConnection(device!!, bluetoothSocket!!, BluetoothReader(bluetoothSocket, ::onDataReceived))
    bluetoothConnection?.reader?.startReading()
  }

  fun onDataReceived(data: String) {
    channel.invokeMethod("onDataReceived", data)
  }

  fun disconnect(result: Result) {
    bluetoothConnection?.close();
    bluetoothConnection = null;
  }




  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    val bluetoothManager: BluetoothManager? = getSystemService(activity,BluetoothManager::class.java)
    bluetoothAdapter = bluetoothManager?.adapter
    binding.addRequestPermissionsResultListener(BluetoothPermissions)

  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivity() {
    bluetoothAdapter = null
  }
}
