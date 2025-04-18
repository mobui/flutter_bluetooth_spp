import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_bluetooth_spp_platform_interface.dart';

/// An implementation of [FlutterBluetoothSppPlatform] that uses method channels.
class MethodChannelFlutterBluetoothSpp extends FlutterBluetoothSppPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_bluetooth_spp');

  @visibleForTesting
  final dataChannel = const EventChannel('flutter_bluetooth_spp/data_stream');

  @override
  Future<Stream<String>> connectToDevice(String address,
      {String charset = "UTF-8"}) async {
    try{
      final result =  await methodChannel.invokeMethod<bool>('connectToDevice', {"address": address, "charset": charset});
      if(result ?? false) {
        return dataChannel.receiveBroadcastStream().map((event) =>
            event.toString(),);
      } else {
        throw InternalErrorException(message: "Device not connected");
      }
    } on PlatformException catch (e) {
      throw FlutterBluetoothSppException.fromPlatformException(e);
    }
  }

  @override
  Future<bool> requestPermissions() async {
    try {
      return await methodChannel.invokeMethod<bool>('requestPermissions') ?? false;
    } on PlatformException  catch (e) {
      throw FlutterBluetoothSppException.fromPlatformException(e);
    }
  }

  @override
  Future<List<Device>> getBondedDevices() async {
    try{
      final  state = await methodChannel.invokeMethod<List<Object?>>('getBondedDevices');
      return state?.map((e) => switch(e) {
        Map<Object?, Object?>() => Device(name: e["name"] as String, address: e["address"] as String),
        _ => throw InternalErrorException(message: "Device parsing error"),
 }).toList() ?? [];
    } on PlatformException catch (e) {
      throw FlutterBluetoothSppException.fromPlatformException(e);
    }
  }

  @override
  Future<bool> disconnect() async {
    try {
      return (await methodChannel.invokeMethod<bool>('disconnect') ) ?? false;
    } on PlatformException catch (e) {
      throw FlutterBluetoothSppException.fromPlatformException(e);
    }
  }

  @override
  Future<Device?> getConnectedDevice() async {
    try {
      final  device =  (await methodChannel.invokeMethod<Map<Object?, Object?>>('getConnectedDevice'));
      if(device != null) {
        return Device(name: device["name"] as String, address: device["address"] as String);
      } else {
        return null;
      }
    } on PlatformException catch (e) {
      throw FlutterBluetoothSppException.fromPlatformException(e);
    }
  }
}
