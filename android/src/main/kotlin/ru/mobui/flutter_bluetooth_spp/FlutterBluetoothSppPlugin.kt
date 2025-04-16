package ru.mobui.flutter_bluetooth_spp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import androidx.core.content.ContextCompat.getSystemService
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.IOException
import java.util.UUID

/** FlutterBluetoothSppPlugin */
class FlutterBluetoothSppPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel : MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var activity: Activity
  private var bluetoothAdapter: BluetoothAdapter? = null

  private var dataSink: EventChannel.EventSink? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_bluetooth_spp")
    channel.setMethodCallHandler(this)
    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger,"flutter_bluetooth_spp/data_stream");
    eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
      override fun onListen(arguments: Any?, sink: EventChannel.EventSink?) {
        dataSink = sink
      }

      override fun onCancel(arguments: Any?) {
        dataSink = null
      }
    })
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when(call.method){
      "requestPermissions"-> requestPermissions(result)
      "getBondedDevices" -> callWithRequestPermission(result){ getBondedDevices(it) }
      "connectToDevice"-> callWithRequestPermission(result){ connectToDevice(it, call.argument<String>("address")) }
      "disconnect"-> callWithRequestPermission(result){ disconnect(it) }
      "getConnectedDevice"-> callWithRequestPermission(result){ getConnectedDevice(it) }
      else -> result.notImplemented()
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

  private fun requestPermissions(result: Result) {
    BluetoothPermissions.checkBluetoothPermissions(activity) {
      result.success(it)
    }
  }

  private fun getBondedDevices(result: Result): Unit{
    val adapter = bluetoothAdapter ?: return result.error("BLUETOOTH_NOT_AVAILABLE", "Bluetooth not available", null)
    val bondedDevices = adapter.bondedDevices.map { it.toMap() }
    result.success(bondedDevices)
  }

  private fun connectToDevice(result: Result, address: String?) {
  BluetoothConnector.connect(bluetoothAdapter!!, address!!) {
      dataSink?.success(it)
    }
  }

  private fun  disconnect(result: Result) {
    BluetoothConnector.disconnect()
  }

  private fun  getConnectedDevice(result: Result) {
    result.success(BluetoothConnector.getConnectedDevice(result)?.toMap())
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

fun BluetoothDevice.toMap(): Map<String, String> {
  return mapOf(
    "name" to (this.name ?: "Unknown"),
    "address" to this.address
  )
}