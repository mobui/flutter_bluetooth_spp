import 'dart:collection';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter_bluetooth_spp/flutter_bluetooth_spp.dart';
import 'package:flutter_bluetooth_spp/flutter_bluetooth_spp_platform_interface.dart';

typedef MenuEntry = DropdownMenuEntry<String>;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _flutterBluetoothSppPlugin = FlutterBluetoothSpp();
  StreamSubscription<String>? subs = null;
  String dropdownValue = '';
  List<String> logList = [];
  List<MenuEntry> menuEntries = [];
  String connectedDevice = "";
  late Timer timer;

  @override
  void initState() {
    super.initState();
    initPlatformState();
    timer = Timer.periodic(Duration(seconds: 1), (_) async {
      final device = await _flutterBluetoothSppPlugin.getConnectedDevice();
      connectedDevice = "${device?.name ?? "-"} ${device?.address ?? "-"}";
      setState(() {});
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    List<Device> bondedDevices = [];
    try {
      bondedDevices = await _flutterBluetoothSppPlugin.getBondedDevices();
    } on Exception catch (e) {
      bondedDevices = [];
    }
    if (!mounted) return;

    menuEntries = UnmodifiableListView<MenuEntry>(
      bondedDevices.map<MenuEntry>(
        (d) => MenuEntry(value: d.address, label: d.name),
      ),
    );

    dropdownValue = menuEntries.firstOrNull?.value ?? "";

    setState(() {});
  }

  void connect() async {

      try {
        final connections = await _flutterBluetoothSppPlugin.connectToDevice(dropdownValue);
        subs =  connections.listen((data) {
            logList.add(data);
            setState(() {});
        } );
      } catch (e){
        setState(() {});
    }
  }

  void disconnect() async {
    try {
      subs?.cancel();
      await _flutterBluetoothSppPlugin.disconnect();
      } catch (e){
    }
  }

  @override
  void dispose() {
    // TODO: implement dispose
    timer.cancel();
    subs?.cancel();
    _flutterBluetoothSppPlugin.disconnect();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Тестирование Bluetooth')),
        body: Center(
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: DropdownMenu<String>(
                  width: double.infinity,
                  initialSelection: dropdownValue,
                  onSelected: (String? value) {
                    // This is called when the user selects an item.
                    setState(() {
                      dropdownValue = value!;
                    });
                  },
                  dropdownMenuEntries: menuEntries,
                ),
              ),
              const SizedBox(height: 20),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  OutlinedButton(
                    onPressed: connect,
                    child: Text("Подключиться"),
                  ),
                  FilledButton(
                    onPressed: disconnect,
                    child: Text("Отключиться"),
                    style: FilledButton.styleFrom(backgroundColor: Colors.red),
                  ),
                ],
              ),
              Text(connectedDevice),
              Expanded(
                child: ListView.builder(
                  padding: const EdgeInsets.all(8),
                  itemCount: logList.length,
                  itemBuilder: (BuildContext context, int index) {
                    return SizedBox(height: 40, child: Text(logList[index]));
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
