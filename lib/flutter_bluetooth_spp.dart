
import 'flutter_bluetooth_spp_platform_interface.dart';

class FlutterBluetoothSpp {
  Future<bool> requestPermissions() {
    return FlutterBluetoothSppPlatform.instance.requestPermissions();
  }

  Future<List<Device>> getBondedDevices() {
    return FlutterBluetoothSppPlatform.instance.getBondedDevices();
  }

  Future<Stream<String>> connectToDevice (String address, {String charset = "UTF-8"}) {
    return FlutterBluetoothSppPlatform.instance.connectToDevice(address, charset: charset);
  }

  Future<bool> disconnect() {
    return FlutterBluetoothSppPlatform.instance.disconnect();
  }

  Future<Device?> getConnectedDevice() {
    return FlutterBluetoothSppPlatform.instance.getConnectedDevice();
  }
}
