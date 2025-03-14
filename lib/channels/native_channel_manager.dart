// ignore_for_file: constant_identifier_names

import 'dart:developer';

import 'package:flutter/services.dart';

class NativeChannelMethods {
  static const CHANNEL_NAME = 'GLOBEDR_CHANNEL';
  static const START_VERIFY_METHOD = 'START_VERIFY_METHOD';
  static const ON_FINISH_EVENT = "ON_FINISH_EID";
  static const ON_NONE_EVENT = "NONE";
  static const ON_FINISH_VEHICLE_SCAN_EVENT = "ON_FINISH_VEHICLE_SCAN";
  static const ON_FINISH_EID = "ON_FINISH_EID";

  static const START_CAPTURE_EID = "CAPTURE_EID";
}

class NativeChannelManager {
  // Singleton instance.
  static final NativeChannelManager _instance =
      NativeChannelManager._internal();

  // Static field to control event handling.
  static bool isCall = true;

  final MethodChannel platform;

  // Factory constructor returns the singleton instance.
  factory NativeChannelManager() {
    return _instance;
  }

  // Private constructor without parameters.
  NativeChannelManager._internal()
      : platform = const MethodChannel(NativeChannelMethods.CHANNEL_NAME);

  /// Invokes the native method to start capturing EID.
  Future<void> startVerifyEkyc() async {
    String message;
    try {
      final String? result =
          await platform.invokeMethod(NativeChannelMethods.START_CAPTURE_EID);
      message = 'Native message: $result';
    } on PlatformException catch (e) {
      message = "Failed to get native message: '${e.message}'.";
    }
    log(message);
  }

  /// Invokes the native method to start verifying EID.
  Future<void> startVerifyEid() async {
    String message;
    try {
      final String? result =
          await platform.invokeMethod(NativeChannelMethods.START_VERIFY_METHOD);
      message = 'Native message: $result';
    } on PlatformException catch (e) {
      message = "Failed to get native message: '${e.message}'.";
    }
    log(message);
  }

  /// Sets the method call handler for native events.
  Future<void> eventListener(
      Function(String event, String method) event) async {
    platform.setMethodCallHandler((call) {
      log("Method call handler: ${call.method}");
      if (call.method == NativeChannelMethods.ON_FINISH_EVENT &&
          NativeChannelManager.isCall) {
        // Pass the event data to the provided callback.
        event(call.arguments.toString(), call.method.toString());
        return Future.value(NativeChannelMethods.ON_FINISH_EVENT);
      } else {
        return Future.value(NativeChannelMethods.ON_NONE_EVENT);
      }
    });
  }

  void dispose() {
    platform.setMethodCallHandler(null);
    log("NativeEidManager disposed.");
  }
}
