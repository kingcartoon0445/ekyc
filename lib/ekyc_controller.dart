import 'dart:convert';
import 'dart:developer';
import 'dart:io';
import 'package:ekyc/ekyc_service.dart';
import 'package:ekyc/snack_bar_base.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';
import 'package:get/get.dart';
import '../../../../channels/native_channel_manager.dart';

class EkycController extends GetxController {
  // RxBool isDoneAuthentic = false.obs;
  // RxString isMatch = "".obs;
  // RxString authenticResults = "".obs;
  /// The base64 string of the reference image from native
  String? base64RefImage;

  // late final AuthController _authController;
  late final EkycSerice _ekycSerice;
  @override
  void onInit() {
    super.onInit();
    _ekycSerice = EkycSerice();

    // _authController = Get.find<AuthController>();

    // Listen to the EID events from the native side
    eventListener((event, method) {
      if (method == ON_FINISH_EVENT) {
        // try {
        log("EID Successfully verified");
        if (Platform.isIOS) {
          // Chuyển chuỗi thành Map
          _verifyData(convertToValidJsonString(event)).whenComplete(
            () => NativeChannelManager.isCall = false,
          );
        } else {
          /// Handle the event when the EID verification is successful

          switch (method) {
            case "ID_FRONT_MISSING":
              // SnackbarBase.showError(
              //   Get.context!,
              //   "Vui lòng chụp mặt trước cccd!",
              //   duration: const Duration(milliseconds: 2000),
              // );
              break;
            case "ID_BACK_MISSING":
              // SnackbarBase.showError(
              //   Get.context!,
              //   "Vui lòng chụp mặt sau cccd!",
              //   duration: const Duration(milliseconds: 2000),
              // );
              break;
            default:
              _verifyData(_reformatJson(event)).whenComplete(
                () => NativeChannelManager.isCall = false,
              );
              break;
          }
        }

        // } catch (e) {
        //   log("Parse EID data error: $e");
        // }
      }
    });
  }

  // Format JSON string to JSON object on iOS side
  String formatToJson(String input) {
    // Loại bỏ khoảng trắng thừa và dấu ngoặc nhọn ngoài cùng
    String content = input.trim();
    if (content.startsWith('{')) {
      content = content.substring(1);
    }
    if (content.endsWith('}')) {
      content = content.substring(0, content.length - 1);
    }

    // Hàm xử lý object con
    String handleNestedObject(String objStr) {
      String trimmed = objStr.trim();
      if (!trimmed.startsWith('{') || !trimmed.endsWith('}')) return objStr;

      String innerContent = trimmed.substring(1, trimmed.length - 1);
      List<String> pairs = innerContent.split(',').map((pair) {
        List<String> kv = pair.trim().split(':');
        String key = '"${kv[0].trim()}"';
        String value = kv[1].trim();
        // Thêm dấu ngoặc kép cho value trong object con
        return '$key: "$value"';
      }).toList();

      return '{${pairs.join(', ')}}';
    }

    List<String> result = [];
    String currentPart = '';
    bool inObject = false;
    int braceCount = 0;

    // Xử lý từng ký tự
    for (int i = 0; i < content.length; i++) {
      if (content[i] == '{') {
        inObject = true;
        braceCount++;
      }
      if (content[i] == '}') {
        braceCount--;
        if (braceCount == 0) inObject = false;
      }

      if (content[i] == ',' && !inObject) {
        if (currentPart.contains(':')) {
          List<String> parts = currentPart.split(':');
          String key = parts[0].trim();
          String value = parts.sublist(1).join(':').trim();

          // Kiểm tra nếu value là một object
          if (value.startsWith('{') && value.endsWith('}')) {
            result.add('"$key": ${handleNestedObject(value)}');
          } else {
            result.add('"$key": "$value"');
          }
        }
        currentPart = '';
      } else {
        currentPart += content[i];
      }
    }

    // Xử lý phần cuối cùng
    if (currentPart.isNotEmpty && currentPart.contains(':')) {
      List<String> parts = currentPart.split(':');
      String key = parts[0].trim();
      String value = parts.sublist(1).join(':').trim();
      if (value.startsWith('{') && value.endsWith('}')) {
        result.add('"$key": ${handleNestedObject(value)}');
      } else {
        result.add('"$key": "$value"');
      }
    }

    return '{${result.join(', ')}}';
  }

  Future<void> _postEkycData({
    required String data,
  }) async {
    try {
      final token = "";
      //await _authController.getCurrentIdKey();

      if (token != null && token.isNotEmpty) {
        final response =
            await _ekycSerice.postEkycData(token: token, data: data);
        final String responseText = response["error_text"];
        if (response["error"] == 0) {
          if (response.isNotEmpty && response["error"] == 0) {
            // _authController.fetchUser();
            // SnackbarBase.showSuccess(
            //   Get.context!,
            //   responseText,
            //   duration: const Duration(milliseconds: 2000),
            //   onClosed: () {
            //     Get.offAllNamed(Routes.main);
            //   },
            // );
          } else {
            if (response.isEmpty) {
              SnackbarBase.showError(
                Get.context!,
                "Ekyc thất bại..!",
                duration: const Duration(milliseconds: 2000),
              );
            } else {
              SnackbarBase.showError(
                Get.context!,
                responseText,
                duration: const Duration(milliseconds: 2000),
              );
            }
          }
          log("postEkycData success:${response["error_text"]}");
        } else {
          log("postEkycData response:${response["error_text"]}");
          SnackbarBase.showError(
            Get.context!,
            responseText,
            duration: const Duration(milliseconds: 2000),
          );
        }
      } else {
        log("postEkycData token missing");
      }
    } catch (e) {
      log("postEkycData error: $e");
      SnackbarBase.showError(
        Get.context!,
        "Có lỗi xảy ra...! Vui lòng thử lại sau",
        duration: const Duration(milliseconds: 2000),
      );
      return;
    }
  }

  String convertToValidJsonString(String input) {
    // 1. Loại bỏ dấu ngoặc vuông không cần thiết ở đầu và cuối
    String result = input.trim();
    if (result.startsWith('[') && result.endsWith(']')) {
      result = result.substring(1, result.length - 1);
    }

    // 2. Thêm dấu ngoặc nhọn bao quanh
    result = '{$result}';

    // 3. Thay thế các cặp key-value không có dấu ngoặc kép
    final pattern = RegExp(r'\["([^"]+)": ');
    result = result.replaceAllMapped(pattern, (match) {
      return '{"${match.group(1)}": ';
    });

    // 4. Xử lý verificationStatus
    final verificationStatusPattern = RegExp(r'\["([^"]+)": "([^"]+)"');
    if (result.contains('verificationStatus')) {
      // String verificationPart =
      //     result.split('verificationStatus": [')[1].split(']')[0];
      // String fixedVerificationPart =
      //     '{${verificationPart.replaceAllMapped(verificationStatusPattern, (match) => '"${match.group(1)}": "${match.group(2)}"')}}';
      // result = result.replaceFirst(RegExp(r'verificationStatus": \[.*?\]'),
      //     'verificationStatus": $fixedVerificationPart');
    }
    String a = result.replaceAll("[", "{").replaceAll("]", "}");

    return a;
  }

  /// Main function to verify the data
  Future<void> _verifyData(String data) async {
    EasyLoading.show(
      status: 'Đang xác thực...',
      maskType: EasyLoadingMaskType.black,
    );
    // Map<String, dynamic> jsonData = {};
    // try {
    //   log(data);
    //   jsonData = json.decode(data);
    // } catch (e) {
    //   log("duy lỗi rồi: $e");
    // }
    await _postEkycData(data: data).whenComplete(
      () => EasyLoading.dismiss(),
    );
    // Map<String, dynamic> jsonData = {};
    // try {
    //   log(data);
    //   jsonData = json.decode(data);
    // } catch (e) {
    //   log("duy lỗi rồi: $e");
    // }

    // final String verifyOcrResult = await _verifyOcr(jsonData).whenComplete(
    //   () => EasyLoading.dismiss(),
    // );

    // if (verifyOcrResult == "Success") {
    //   EasyLoading.show(
    //     status: 'Đang xác thực...',
    //     maskType: EasyLoadingMaskType.black,
    //   );

    // } else {
    //   switch (verifyOcrResult) {
    //     case "VerifyFailed":
    //       SnackbarBase.showError(
    //         Get.context!,
    //         "Xác thực OCR thất bại! Vui lòng kiểm tra ảnh chụp 2 mặt CCCD thử lại sau",
    //         duration: const Duration(milliseconds: 4000),
    //       );
    //       break;
    //     case "UncatchException":
    //       SnackbarBase.showError(
    //         Get.context!,
    //         "Có lỗi xảy ra! Vui lòng thử lại sau",
    //         duration: const Duration(milliseconds: 2000),
    //       );
    //       break;
    //     default:
    //       showDialogAnnouncement(
    //         Get.context!,
    //         contentWidget: VerifyOcrDialogContent(
    //             title: "Xác thực OCR thất bại!", content: verifyOcrResult),
    //         content: "",
    //         showCancleButton: false,
    //         onAgree: () => Get.back(),
    //       );
    //       break;
    //   }
    // }
  }

  /// Method to log large base64 strings in chunks to avoid log truncation
  void logLargeBase64String(String tag, String base64String,
      {int chunkSize = 1000}) {
    int length = base64String.length;
    for (int i = 0; i < length; i += chunkSize) {
      int end = (i + chunkSize < length) ? i + chunkSize : length;
      log("$tag: ${base64String.substring(i, end)}");
    }
  }

  // /// Method to format the JSON of the base 64 of the front and back id images
  // /// and call the verifyOcr API
  // Future<String> _verifyOcr(Map<String, dynamic> jsonData) async {
  //   List<String> base64Keys = ['idFront', 'idBack'];

  //   // Map to hold formatted base64 strings
  //   Map<String, String> formattedBase64Strings = {};

  //   for (String key in base64Keys) {
  //     if (jsonData.containsKey(key)) {
  //       String base64String = jsonData[key];
  //       // Remove any newline characters from the base64 string
  //       String formattedBase64String =
  //           base64String.replaceAll('\n', '').replaceAll('\r', '');
  //       formattedBase64Strings[key] = formattedBase64String;
  //     }
  //   }
  //   // Log the base64 strings for idFront and idBack
  //   log("_verifyOcr idFront base64:${formattedBase64Strings['idFront']}" ?? "");
  //   log("_verifyOcr idBack base64: ${formattedBase64Strings['idBack']}" ?? "");
  //   // Call the verifyOcr API
  //   try {
  //     final response = await _ekycSerice.verifyOcr(
  //       idFrontBase64: formattedBase64Strings['idFront']!,
  //       idBackBase64: formattedBase64Strings['idBack']!,
  //     );
  //     if (response["success"] == true) {
  //       final frontInvalideCode = response["data"]["front_invalid_code"] ?? "";
  //       final backInvalideCode = response["data"]["back_invalid_code"] ?? "";
  //       if (frontInvalideCode == "0" && backInvalideCode == "0") {
  //         return "Success";
  //       } else {
  //         final errorMessage = _getResponseMessage(
  //           frontInvalideCode,
  //           backInvalideCode,
  //         );
  //         return errorMessage;
  //       }
  //     }
  //     return "VerifyFailed";
  //   } catch (e) {
  //     return "UncatchException";
  //   }
  // }

  /// Get the response message based on the error codes for both the front and back id images
  String _getResponseMessage(String frontErrorCode, String backErrorCode) {
    String message = "";

    if (frontErrorCode.isNotEmpty) {
      message += "Mặt trước CCCD : ${_getErrorMessage(frontErrorCode)}\n";
    }

    if (backErrorCode.isNotEmpty) {
      message += "Mặt sau CCCD : ${_getErrorMessage(backErrorCode)}\n";
    }

    if (message.isEmpty) {
      message = "Xác thực thành công!";
    }

    return message.trim();
  }

  /// Get the error message based on the error code for the OCR verification
  String _getErrorMessage(String errorCode) {
    switch (errorCode) {
      case "0":
        return "Hình ảnh hợp lệ!";
      case "1":
        return "Hình ảnh giấy tờ có dấu hiệu của màn hình điện tử!";
      case "2":
        return "Hình ảnh giấy tờ là bản photocopy!";
      case "3":
        return "Trường id trên giấy tờ không đúng định dạng!";
      case "5":
        return "Hình ảnh giấy tờ bị mất góc!";
      case "6":
        return "Hình ảnh giấy tờ chụp quá sát góc!";
      case "7":
        return "Hình ảnh giấy tờ có 1 số trường bị che!";
      case "8":
        return "Hình ảnh giấy tờ thiếu dấu vân tay!";
      case "9":
        return "Hình ảnh giấy tờ bị mờ!";
      case "10":
        return "Hình ảnh giấy tờ bị chói sáng!";
      case "11":
        return "Thông tin trên giấy tờ có dấu hiệu bị chỉnh sửa!";
      case "12":
        return "Ảnh chân dung trên giấy tờ có dấu hiệu bị thay thế!";
      case "13":
        return "Giấy tờ tuỳ thân hết hạn!";
      case "14":
        return "Trường ngày sinh trên giấy tờ không đúng định dạng!";
      default:
        return "Có lỗi xảy ra!";
    }
  }

  /// Reformat the JSON string to remove unnecessary fields
  /// and rename the isMatch field to ff
  String _reformatJson(String jsonString) {
    log("Reformatting JSON string: $jsonString");
    // Parse the JSON string to a Map
    Map<String, dynamic> jsonData = json.decode(jsonString);
    log("Reformatted JSON dg15: ${jsonData['dg15']}");
    // Remove the specified fields
    if (jsonData.containsKey('onboardFaceImagePath')) {
      jsonData.remove('onboardFaceImagePath');
    }

    if (jsonData.containsKey('verifyIdRequestModel')) {
      jsonData.remove('verifyIdRequestModel');
    }

    if (jsonData.containsKey('idFace')) {
      jsonData.remove('idFace');
    }
    // Check if the verificationStatus field exists
    if (jsonData.containsKey('verificationStatus')) {
      Map<String, dynamic> verificationStatus = jsonData['verificationStatus'];

      // Rename the isMatch field to ff
      if (verificationStatus.containsKey('isMatch')) {
        verificationStatus['ff'] = verificationStatus.remove('isMatch');
      }
    }
    // Convert the updated Map back to a JSON string
    return json.encode(jsonData);
  }
}
