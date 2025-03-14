import 'dart:convert';

extension StringExtension on String {
  String formatEkycResponseIos() {
    String result = trim();
    if (result.startsWith('[') && result.endsWith(']')) {
      result = result.substring(1, result.length - 1);
    }
    result = '{$result}';
    final pattern = RegExp(r'\["([^"]+)": ');
    result = result.replaceAllMapped(pattern, (match) {
      return '{"${match.group(1)}": ';
    });
    return result.replaceAll("[", "{").replaceAll("]", "}");
  }

  String formatEkycResponseAndroid() {
    Map<String, dynamic> jsonData = json.decode(this);
    if (jsonData.containsKey('onboardFaceImagePath')) {
      jsonData.remove('onboardFaceImagePath');
    }
    if (jsonData.containsKey('verifyIdRequestModel')) {
      jsonData.remove('verifyIdRequestModel');
    }
    if (jsonData.containsKey('idFace')) {
      jsonData.remove('idFace');
    }
    if (jsonData.containsKey('verificationStatus')) {
      Map<String, dynamic> verificationStatus = jsonData['verificationStatus'];
      if (verificationStatus.containsKey('isMatch')) {
        verificationStatus['ff'] = verificationStatus.remove('isMatch');
      }
    }
    return json.encode(jsonData);
  }
}
