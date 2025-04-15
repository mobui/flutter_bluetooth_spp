import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_bluetooth_spp_platform_interface.dart';

/// An implementation of [FlutterBluetoothSppPlatform] that uses method channels.
class MethodChannelFlutterBluetoothSpp extends FlutterBluetoothSppPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_bluetooth_spp');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
