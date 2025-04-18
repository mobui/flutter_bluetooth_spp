package ru.mobui.flutter_bluetooth_spp

import android.Manifest
import android.app.Activity

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow



class BluetoothPermissions: PluginRegistry.RequestPermissionsResultListener {
    companion object {
        const val REQUEST_CODE = 1001
    }


    private var permissionResult: ((granted: Boolean) -> Unit)? = null

    private val bluetoothPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }.toTypedArray()


    public fun checkBluetoothPermissions(context: Activity, permissionResult: ((granted: Boolean) -> Unit)): Unit {
        this.permissionResult = permissionResult
        if (bluetoothPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            permissionResult?.invoke(true)
        } else {
            ActivityCompat.requestPermissions(context, bluetoothPermissions, REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            permissionResult?.invoke(allGranted)
            return true
        }
        return false
    }
}