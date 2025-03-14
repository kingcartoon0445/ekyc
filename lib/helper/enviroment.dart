import 'package:flutter_dotenv/flutter_dotenv.dart';

class Enviroment {
  static const String API_URL = "https://echeck.numbala.com/APIs-Android.htm";
  static late String? apiKey;
  static loadEnv() async {
    // Load enviroment
    await dotenv.load(fileName: ".env").whenComplete(
      () {
        apiKey = dotenv.env['ECHECK_TOKEN'];
      },
    );
  }
}
