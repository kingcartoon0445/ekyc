import 'package:flutter/material.dart';

void showLoadingScreen(BuildContext context) {
  showDialog(
    barrierDismissible: false,
    context: context,
    builder: (context) {
      return Scaffold(
        backgroundColor: Colors.black54,
        body: base(),
      );
    },
  );
}

Widget base({Color? color, double? size}) {
  return Center(
    child: SizedBox(
      height: size ?? 30,
      width: size ?? 30,
      child: CircularProgressIndicator.adaptive(
        strokeWidth: 2,
        valueColor: AlwaysStoppedAnimation<Color>(color ?? Colors.white),
      ),
    ),
  );
}
