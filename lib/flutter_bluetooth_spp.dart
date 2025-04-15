
import 'flutter_bluetooth_spp_platform_interface.dart';

class FlutterBluetoothSpp {
  Future<String?> getPlatformVersion() {
    return FlutterBluetoothSppPlatform.instance.getPlatformVersion();
  }
}
