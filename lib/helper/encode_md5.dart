import 'dart:convert';
import 'dart:developer';
import 'package:crypto/crypto.dart';
import 'package:ekyc/helper/enviroment.dart';
import 'package:intl/intl.dart';

String encodeMD5KeyAPI() {
  if (Enviroment.apiKey != null) {
    String token = Enviroment.apiKey!;
    String currentDate = DateFormat('yyyyMMdd').format(DateTime.now());
    String concatString = currentDate + token;
    var bytes = utf8.encode(concatString);
    return md5.convert(bytes).toString();
  } else {
    log("encodeMD5KeyAPI: API key is null");
    throw Exception("API key is null");
  }
}
