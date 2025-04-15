
import 'flutter_bluetooth_spp_platform_interface.dart';

class FlutterBluetoothSpp {
  Future<bool> requestPermissions() {
    return FlutterBluetoothSppPlatform.instance.requestPermissions();
  }
}
