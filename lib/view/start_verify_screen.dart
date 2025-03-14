// ignore_for_file: use_build_context_synchronously

import 'dart:convert';
import 'dart:developer';
import 'dart:io';
import 'package:ekyc/helper/enviroment.dart';
import 'package:ekyc/helper/loading_screen.dart';
import 'package:ekyc/helper/string_extension.dart';
import 'package:ekyc/model/ekyc_response.dart';
import 'package:http/http.dart' as http;
import 'package:ekyc/channels/native_channel_manager.dart';
import 'package:ekyc/config/colors.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../config/api_type.dart';
import '../config/enum.dart';
import '../helper/encode_md5.dart';

class StartVerifyScreen extends StatefulWidget {
  final Widget? logo;
  final String? token;
  final Function(EkycResponse response)? onFinishedVerify;
  const StartVerifyScreen({
    super.key,
    this.logo,
    this.onFinishedVerify,
    this.token,
  });

  @override
  State<StartVerifyScreen> createState() => _StartVerifyScreenState();
}

class _StartVerifyScreenState extends State<StartVerifyScreen> {
  /// Posts the eKYC data to your API.
  Future<Map<String, dynamic>> _postEkycData({
    required String token,
    required String data,
  }) async {
    showLoadingScreen(context);
    final params = {
      "type": ApiType.echeckEKYC.type.toString(),
      "key": encodeMD5KeyAPI(),
      "idkey": token,
      "data": data,
    };
    log("postEkycData params: $params");
    try {
      final response = await http
          .post(Uri.parse(Enviroment.API_URL), body: params, headers: {
        HttpHeaders.contentTypeHeader: "application/x-www-form-urlencoded",
      }).then((value) {
        Navigator.pop(context);
        return json.decode(value.body) as Map<String, dynamic>;
      });
      log("postEkycData response: $response");
      return response;
    } catch (e) {
      log("postEkycData error: $e");
      rethrow;
    }
  }

  /// Registers a listener for native events and triggers verification.
  void _registerEventListener() {
    NativeChannelManager().eventListener((event, method) async {
      if (method == NativeChannelMethods.ON_FINISH_EVENT) {
        log("EID Successfully verified");
        if (Platform.isIOS) {
          await verifyEKYC(widget.token, event.formatEkycResponseIos());
        } else {
          await verifyEKYC(widget.token, event.formatEkycResponseAndroid());
        }
        NativeChannelManager.isCall = false;
        NativeChannelManager().dispose();
      }
    });
  }

  /// Verifies eKYC data by calling the API and updates UI state accordingly.
  Future<void> verifyEKYC(String? token, String data) async {
    late EkycResponse ekycResponse;
    if (token != null && token.isNotEmpty) {
      final response = await _postEkycData(token: token, data: data);
      ekycResponse = EkycResponse.fromJson(response);
      widget.onFinishedVerify?.call(ekycResponse);
    } else {
      ekycResponse = EkycResponse(
        status: EkycStatus.failed,
        message: "Phiên đăng nhập không hợp lệ..!",
      );
      log("postEkycData token missing");
      widget.onFinishedVerify?.call(ekycResponse);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF333545),
      appBar: AppBar(
        toolbarHeight: 100,
        automaticallyImplyLeading: false,
        flexibleSpace: Padding(
          padding: const EdgeInsets.only(top: 24.0),
          child: Stack(
            children: [
              const Align(
                alignment: Alignment.centerLeft,
                child: BackButton(color: Colors.white),
              ),
              if (widget.logo != null) widget.logo!,
            ],
          ),
        ),
        backgroundColor: EkycColor.primaryColor,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            const SizedBox(
              height: 240,
              width: 240,
              child: Icon(CupertinoIcons.person_crop_rectangle,
                  size: 240, color: Colors.white),
            ),
            const SizedBox(height: 12),
            const Text(
              "Xác thực eKYC",
              style: TextStyle(color: Colors.white, fontSize: 22),
            ),
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 18, vertical: 16),
              child: Text(
                "Xác minh thông tin thật giả của CCCD gắn chip, eKYC và tích hợp với dịch vụ xác thực của C06.\n\n"
                "Bước 1: Xác minh toàn vẹn dữ liệu CA/PA/AA.\n"
                "Bước 2: Xác định thực thể sống.\n"
                "Bước 3: Xác thực với C06.\n"
                "Bước 4: Hiển thị thông tin chủ thẻ và khuôn mặt.",
                style: TextStyle(color: Colors.white, fontSize: 15),
              ),
            ),
            SizedBox(
              height: 48,
              child: ElevatedButton(
                onPressed: () {
                  _registerEventListener();
                  NativeChannelManager.isCall = true;
                  NativeChannelManager().startVerifyEkyc();
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: EkycColor.themeColor,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: const Text(
                  "Xác thực ngay",
                  style: TextStyle(color: Colors.white, fontSize: 16),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
