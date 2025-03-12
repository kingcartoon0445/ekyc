// ignore_for_file: constant_identifier_names

import 'dart:developer';

import 'package:flutter/services.dart';

const platform = MethodChannel(CHANNEL_NAME);
//constants
const CHANNEL_NAME = 'GLOBEDR_CHANNEL';
const START_VERIFY_METHOD = 'START_VERIFY_METHOD';
const ON_FINISH_EVENT = "ON_FINISH_EID";
const ON_NONE_EVENT = "NONE";
const ON_FINISH_VEHICLE_SCAN_EVENT = "ON_FINISH_VEHICLE_SCAN";
const ON_FINISH_EID = "ON_FINISH_EID";

const START_CAPTURE_EID = "CAPTURE_EID";

class NativeChannelManager {
  static final NativeChannelManager _instance =
      NativeChannelManager._internal();
  static bool isCall = false;
  NativeChannelManager._internal();
  factory NativeChannelManager() {
    return _instance;
  }
}

Future<void> startCaptureEid() async {
  String message;
  try {
    final String result = await platform.invokeMethod(START_CAPTURE_EID);
    message = 'Native message: $result';
  } on PlatformException catch (e) {
    message = "Failed to get native message: '${e.message}'.";
  }
  log(message);
}

Future<void> startVerifyEid() async {
  String message;
  try {
    final String result = await platform.invokeMethod(START_VERIFY_METHOD);
    message = 'Native message: $result';
  } on PlatformException catch (e) {
    message = "Failed to get native message: '${e.message}'.";
  }
  log(message);
}

Future<void> startVehiclePlateScanning() async {
  String message;
  try {
    final String result = await platform.invokeMethod("VEHICLE_PLATE_SCAN");
    message = 'Native message: $result';
  } on PlatformException catch (e) {
    message = "Failed to get native message: '${e.message}'.";
  }
  log(message);
}

Future<void> eventListener(Function(String event, String method) event) async {
  platform.setMethodCallHandler((call) {
    log("Method call handler: ${call.method}");
    if (call.method == ON_FINISH_EVENT && NativeChannelManager.isCall) {
      // EkycController().postEkycData(call.arguments);
      event(call.arguments.toString(), call.method.toString());

      return Future(() => ON_FINISH_EVENT);
    } else {
      return Future(() => ON_NONE_EVENT);
    }
  });
}
