package ru.mobui.flutter_bluetooth_spp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.nio.charset.Charset

/** FlutterBluetoothSppPlugin */
class FlutterBluetoothSppPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private lateinit var activity: Activity
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothConnector: BluetoothConnector? = null
    private var bluetoothPermissions: BluetoothPermissions = BluetoothPermissions()
    private var dataSink: EventChannel.EventSink? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        // bluetoothAdapter
        bluetoothAdapter = getBluetoothAdapter(flutterPluginBinding.applicationContext)
        // MethodChannel
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_bluetooth_spp")
        channel.setMethodCallHandler(this)
        // EventChannel
        eventChannel =
            EventChannel(flutterPluginBinding.binaryMessenger, "flutter_bluetooth_spp/data_stream")
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, sink: EventChannel.EventSink?) {
                dataSink = sink
            }

            override fun onCancel(arguments: Any?) {
                dataSink = null
            }
        })
    }

    private fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        val bluetoothManager: BluetoothManager? =
            getSystemService(context, BluetoothManager::class.java)
        return bluetoothManager?.adapter
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        bluetoothConnector?.disconnect()
        bluetoothConnector = null
        bluetoothAdapter = null
        bluetoothConnector = null
        eventChannel.setStreamHandler(null)
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "requestPermissions" -> requestPermissions(result)
            "getBondedDevices" -> callWithRequestPermission(result) { getBondedDevices(it) }
            "connectToDevice" -> callWithRequestPermission(result) {
                connectToDevice(
                    it,
                    call.argument<String>("address"),
                    call.argument<String>("charset"),
                )
            }

            "disconnect" -> callWithRequestPermission(result) { disconnect(it) }
            "getConnectedDevice" -> callWithRequestPermission(result) { getConnectedDevice(it) }
            else -> result.notImplemented()
        }
    }


    private fun callWithRequestPermission(result: Result, call: (result: Result) -> Unit) {
        bluetoothPermissions.checkBluetoothPermissions(activity) {
            if (!it) {
                result.error(
                    "BLUETOOTH_PERMISSIONS_NOT_GRANTED",
                    "Bluetooth permissions not granted",
                    null
                )
                return@checkBluetoothPermissions
            }
            call.invoke(result)
        }
    }

    private fun requestPermissions(result: Result) {
        bluetoothPermissions.checkBluetoothPermissions(activity) {
            result.success(it)
        }
    }

    private fun getBondedDevices(result: Result) {
        val adapter = bluetoothAdapter ?: return result.error(
            "BLUETOOTH_NOT_AVAILABLE",
            "Bluetooth not available",
            null
        )
        val bondedDevices = adapter.bondedDevices.map { it.toMap() }
        result.success(bondedDevices)
    }

    private fun connectToDevice(result: Result, address: String?, charset: String?) {
        val charset =  Charset.forName(charset ?: "UTF-8");
        if (address == null) {
            result.error("INVALID_ADDRESS", "Invalid address", null)
            return
        }
        try {
            bluetoothConnector?.disconnect()
            bluetoothConnector = BluetoothConnector(bluetoothAdapter, address,charset).connect{
                dataSink?.success(it)
            }
            result.success(true)
        } catch (e: Exception) {
            result.error("CONNECT_ERROR", "Error connecting", e)
        }

    }

    private fun disconnect(result: Result) {
        try {
            bluetoothConnector?.disconnect()
            bluetoothConnector = null;
            result.success(true)
        } catch (e:Exception) {
            result.error("DISCONNECT_ERROR", "Error disconnecting", e)
        }

    }

    private fun getConnectedDevice(result: Result) {
        result.success(bluetoothConnector?.getConnectedDevice()?.toMap())
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener(bluetoothPermissions)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
    }
}

