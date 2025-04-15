import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_bluetooth_spp_method_channel.dart';

abstract class FlutterBluetoothSppPlatform extends PlatformInterface {
  /// Constructs a FlutterBluetoothSppPlatform.
  FlutterBluetoothSppPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterBluetoothSppPlatform _instance = MethodChannelFlutterBluetoothSpp();

  /// The default instance of [FlutterBluetoothSppPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterBluetoothSpp].
  static FlutterBluetoothSppPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterBluetoothSppPlatform] when
  /// they register themselves.
  static set instance(FlutterBluetoothSppPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> requestPermissions() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
