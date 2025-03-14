import 'package:ekyc/config/enum.dart';

class EkycResponse {
  EkycStatus status;
  String message;
  dynamic data;

  EkycResponse({required this.status, required this.message, this.data});

  factory EkycResponse.fromJson(Map<String, dynamic> json) {
    late EkycStatus responseStatus;
    if (json['error'] != null) {
      final int error = json['error'];
      if (error == 0) {
        responseStatus = EkycStatus.verified;
      } else {
        responseStatus = EkycStatus.failed;
      }
    } else {
      responseStatus = EkycStatus.failed;
    }
    return EkycResponse(
      status: responseStatus,
      message: json['error_text'],
      data: json['data'],
    );
  }
}
