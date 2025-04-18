import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bluetooth_spp/flutter_bluetooth_spp.dart';
import 'package:flutter_bluetooth_spp/flutter_bluetooth_spp_platform_interface.dart';
import 'package:flutter_bluetooth_spp/flutter_bluetooth_spp_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterBluetoothSppPlatform
    with MockPlatformInterfaceMixin
    implements FlutterBluetoothSppPlatform {


  @override
  Future<bool> requestPermissions() {
    // TODO: implement requestPermissions
    throw UnimplementedError();
  }

  @override
  Future<List<Device>> getBondedDevices() {
    // TODO: implement getBondedDevices
    throw UnimplementedError();
  }

  @override
  Future<Stream<String>>  connectToDevice(String address, {String charset = "UTF-8"}) {
    // TODO: implement connectToDevice
    throw UnimplementedError();
  }

  @override
  Future<bool> disconnect() {
    // TODO: implement disconnect
    throw UnimplementedError();
  }

  @override
  Future<Device?> getConnectedDevice() {
    // TODO: implement getConnectedDevice
    throw UnimplementedError();
  }
}

void main() {
  final FlutterBluetoothSppPlatform initialPlatform = FlutterBluetoothSppPlatform.instance;

  test('$MethodChannelFlutterBluetoothSpp is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterBluetoothSpp>());
  });

  test('getPlatformVersion', () async {
    FlutterBluetoothSpp flutterBluetoothSppPlugin = FlutterBluetoothSpp();
    MockFlutterBluetoothSppPlatform fakePlatform = MockFlutterBluetoothSppPlatform();
    FlutterBluetoothSppPlatform.instance = fakePlatform;

    expect(await flutterBluetoothSppPlugin.requestPermissions(), '42');
  });
}
