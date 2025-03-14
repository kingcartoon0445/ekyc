// ignore_for_file: use_build_context_synchronously

import 'dart:convert';
import 'dart:developer';
import 'dart:io';
import 'package:ekyc/config/enum.dart';
import 'package:ekyc/model/ekyc_response.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../channels/native_channel_manager.dart';
import '../config/api_type.dart';
import '../helper/encode_md5.dart';
import '../helper/enviroment.dart';
import '../widget/snack_bar/show_common_snackbar.dart';
import '../widget/snack_bar/snack_bar_type.dart';
import 'start_verify_screen.dart';

class EkycScreen extends StatefulWidget {
  const EkycScreen({super.key});

  @override
  State<EkycScreen> createState() => _EkycScreenState();
}

class _EkycScreenState extends State<EkycScreen> {
  bool isLoading = false;
  String message = "";
  String dg15 = "";
  String cccd = "";

  /// Registers native event listener.
  void _registerEventListener() {
    // Assuming eventListener is provided by your native channel manager.
    NativeChannelManager().eventListener((event, method) async {
      log("Event method: $method");
      if (method == NativeChannelMethods.ON_FINISH_EVENT) {
        _convertData(event);
        if (dg15.isNotEmpty && cccd.isNotEmpty) {
          await _loginWithId().whenComplete(() {
            NativeChannelManager.isCall = false;
            NativeChannelManager().dispose();
          });
        }
      }
    });
  }

  /// Fixes JSON string that might be incorrectly formatted (for iOS).
  Map<String, dynamic> fixJson(String incorrectJson) {
    incorrectJson = incorrectJson.replaceAll("{", "").replaceAll("}", "");
    List<String> keyValues = incorrectJson.split(", ");
    Map<String, dynamic> jsonData = {};
    for (String keyValue in keyValues) {
      List<String> pair = keyValue.split(": ");
      if (pair.length < 2) continue;
      String key = pair[0].trim();
      String value = pair[1].trim();
      // Add quotes around key and non-numeric value
      key = '"$key"';
      if (!RegExp(r'^\d+$').hasMatch(value)) {
        value = '"$value"';
      }
      jsonData[key.replaceAll('"', '')] = value.replaceAll('"', '');
    }
    return jsonData;
  }

  /// Converts event data from native channel.
  void _convertData(String event) {
    try {
      Map<String, dynamic> data;
      log("Event: $event");
      if (Platform.isIOS) {
        data = fixJson(event);
      } else {
        data = jsonDecode(event);
      }
      log("Data: $data");
      if (data.containsKey("dg15")) {
        setState(() {
          dg15 = data["dg15"];
          cccd = data["eidNumber"];
        });
        log("DG15: $dg15, CCCD: $cccd");
      }
    } catch (e) {
      log("Parse EID data error: $e");
    }
  }

  /// Calls the API to login with ID card data.
  Future<void> _loginWithId() async {
    try {
      EkycResponse loginResult;
      String key = encodeMD5KeyAPI();
      final Map<String, dynamic> params = {
        "type": ApiType.echeckIdLogin.type.toString(),
        "key": key,
        "dg15": dg15,
        "cccd": cccd,
      };
      final response = await http
          .post(Uri.parse(Enviroment.API_URL), body: params, headers: {
        HttpHeaders.contentTypeHeader: "application/x-www-form-urlencoded",
      }).then((value) => json.decode(value.body));
      loginResult = EkycResponse.fromJson(response);
      if (loginResult.status == EkycStatus.verified) {
        SnackbarHandler.showCommonSnackbar(
          context: context,
          description: loginResult.message,
          type: SnackBarType.success,
        );
      } else {
        SnackbarHandler.showCommonSnackbar(
          context: context,
          description: loginResult.message,
          type: SnackBarType.error,
        );
      }
    } catch (e) {
      log("Login with ID card error: $e");
      SnackbarHandler.showCommonSnackbar(
        context: context,
        description: "Đăng nhập thất bại\nVui lòng thử lại!",
        type: SnackBarType.error,
      );
      rethrow;
    }
  }

  /// Resets EKYC data.
  void _resetEkyc() {
    setState(() {
      dg15 = "";
      cccd = "";
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("EKYC Screen"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => StartVerifyScreen(
                      //Temp token
                      token:
                          "TWpGc056bFVaVEZLZUdsUFMyNW9XR3A2VWpaVGR6MDlPam9NZjFZMFcxOWpMV292MmFKbG1EWWw",
                      onFinishedVerify: (response) {
                        if (response.status == EkycStatus.verified) {
                          SnackbarHandler.showCommonSnackbar(
                            context: context,
                            description: response.message,
                            type: SnackBarType.success,
                          );
                        } else {
                          SnackbarHandler.showCommonSnackbar(
                            context: context,
                            description: response.message,
                            type: SnackBarType.error,
                          );
                        }
                      },
                    ),
                  ),
                );
              },
              child:
                  const Text("Xác thực EKYC", style: TextStyle(fontSize: 16)),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                _registerEventListener();
                NativeChannelManager.isCall = true;
                NativeChannelManager().startVerifyEid();
              },
              child:
                  const Text("Đăng nhập CCCD", style: TextStyle(fontSize: 16)),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _resetEkyc,
              child: const Text("Reset EKYC", style: TextStyle(fontSize: 16)),
            ),
            const SizedBox(height: 16),
            if (isLoading) const CircularProgressIndicator(),
            const SizedBox(height: 16),
            Text(message),
          ],
        ),
      ),
    );
  }
}
