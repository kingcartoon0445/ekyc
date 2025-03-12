import 'dart:developer' as dev;
import 'dart:developer';
import 'dart:convert';
import 'package:ekyc/api_service_client.dart';
import 'package:ekyc/api_service_path.dart';
import 'package:crypto/crypto.dart';
import 'package:intl/intl.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

String encodeMD5KeyAPI({String token = APIServicePath.token}) {
  String currentDate = DateFormat('yyyyMMdd').format(DateTime.now());
  String concatString = currentDate + token;
  var bytes = utf8.encode(concatString);
  return md5.convert(bytes).toString();
}

class EkycSerice {
  static final EkycSerice _instance = EkycSerice._internal(token: "", uri: "");
  String token;
  String uri;
  factory EkycSerice({
    String? token,
  }) {
    if (token != null) _instance.token = token;
    return _instance;
  }
  EkycSerice._internal({required this.token, required this.uri});

  // Future<Map<String, dynamic>> verifyOcr({
  //   required String idFrontBase64,
  //   required String idBackBase64,
  // }) async {
  //   final String key = encodeMD5KeyAPI(token: token);
  //   final ehubKey = dotenv.env['EHUB_API_KEY'] ?? "";
  //   log("idFrontBase64 is base 64 : $idFrontBase64");
  //   log("idBackBase64 is base 64: $idFrontBase64");
  //   dev.log("verifyOcr ehubKey:$ehubKey");
  //   final Map<String, dynamic> params = {
  //     "code": "ESCO",
  //     "img_front": idFrontBase64,
  //     "img_back": idBackBase64,
  //   };
  //   dev.log("verifyOcr params: $params");
  //   // jsonLog(runtimeType, params, name: "verifyOcr params");
  //   final headers = {
  //     "x-api-key": ehubKey,
  //     "os-type": "mobile",
  //   };
  //   try {
  //     String url = "$uri/ekyc/api/base64/verify-ocrid";

  //     final response =
  //         await ApiServiceClient.post(url, params, headers: headers);

  //     dev.log("verifyOcr response:${response.body}");
  //     if (response.body != null && response.statusCode == 200) {
  //       final responseBody = response.body as Map<String, dynamic>;
  //       if (responseBody['success'] == true) {
  //         return responseBody;
  //       }
  //       return {"success": false, "message": "Xác thực OCR thất bại"};
  //     }
  //     return {"success": false, "message": "Xác thực OCR thất bại"};
  //   } catch (e) {
  //     dev.log("verifyOcr error:$e");
  //     rethrow;
  //   }
  // }

  Future<Map<String, dynamic>> postEkycData({
    required String token,
    required String data,
  }) async {
    final String key = encodeMD5KeyAPI(token: token);

    final params = {
      "type": 17,
      "key": key,
      "idkey": token,
      "data": data,
    };
    dev.log("postEkycData params:$params");
    try {
      final response = await ApiServiceClient.post(
        uri: APIServicePath.baseUrl,
        params: params,
      );
      dev.log("postEkycData response:$response");
      return response;
    } catch (e) {
      dev.log("postEkycData error:$e");
      rethrow;
    }
  }
}
