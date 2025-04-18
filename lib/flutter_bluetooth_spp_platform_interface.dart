import 'package:flutter/services.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_bluetooth_spp_method_channel.dart';

abstract class FlutterBluetoothSppPlatform extends PlatformInterface {

  FlutterBluetoothSppPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterBluetoothSppPlatform _instance = MethodChannelFlutterBluetoothSpp();

  static FlutterBluetoothSppPlatform get instance => _instance;

  static set instance(FlutterBluetoothSppPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> requestPermissions() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<List<Device>> getBondedDevices() {
    throw UnimplementedError('getBondedDevices() has not been implemented.');
  }

  Future<Stream<String>> connectToDevice(String address, { String charset = "UTF-8"} ) {
    throw UnimplementedError('connectToDevice() has not been implemented.');
  }

  Future<bool> disconnect() {
    throw UnimplementedError('disconnect() has not been implemented.');
  }

  Future<Device?> getConnectedDevice() {
    throw UnimplementedError('getConnectedDevice() has not been implemented.');
  }


}

class Device {
  final String name;
  final String address;

  Device({required this.name, required this.address});

  @override
  String toString() {
    return "{name: $name, address: $address}";
  }
}


sealed class FlutterBluetoothSppException implements Exception {
  final String code;
  final String? message;
  final String? details;

  FlutterBluetoothSppException(
      { required this.code, this.message = "Platform error", this.details = "No details",});

  factory FlutterBluetoothSppException.fromPlatformException(
      PlatformException e) {
    return switch (e.code) {
        "BLUETOOTH_PERMISSIONS_NOT_GRANTED" => BluetoothPermissionsNotGrantedException(message: e.message),
      String() => throw UnimplementedError(),
    };
  }

  @override
  String toString() => "$code: $message";
}

class BluetoothPermissionsNotGrantedException
    extends FlutterBluetoothSppException {
  BluetoothPermissionsNotGrantedException({super.message = "Bluetooth permissions not granted",  super.details = null})
      : super(code: "BLUETOOTH_PERMISSIONS_NOT_GRANTED");
}

class InternalErrorException
    extends FlutterBluetoothSppException {
  InternalErrorException({super.message = "Internal error",  super.details = null})
      : super(code: "INTERNAL_ERROR");
}

