import 'package:another_flushbar/flushbar.dart';
import 'package:flutter/material.dart';

class SnackbarBase {
  static void showSuccess(
    BuildContext context,
    String message, {
    FlushbarPosition? position,
    Duration duration = const Duration(seconds: 3),
    EdgeInsets margin = const EdgeInsets.all(8.0),
    VoidCallback? onClosed,
  }) {
    _showFlushbarBase(
      context,
      message,
      backgroundColor: Colors.green,
      icon: Icons.check_circle,
      margin: margin,
      duration: duration,
      position: position,
      onClosed: onClosed,
    );
  }

  static void showError(
    BuildContext context,
    String message, {
    FlushbarPosition? position,
    Duration duration = const Duration(seconds: 3),
    EdgeInsets margin = const EdgeInsets.all(8.0),
    VoidCallback? onClosed,
  }) {
    _showFlushbarBase(
      context,
      message,
      backgroundColor: Colors.red,
      icon: Icons.error,
      duration: duration,
      margin: margin,
      position: position,
      onClosed: onClosed,
    );
  }

  static void showInfo(
    BuildContext context,
    String message, {
    FlushbarPosition? position,
    Duration duration = const Duration(seconds: 3),
    EdgeInsets margin = const EdgeInsets.all(8.0),
    VoidCallback? onClosed,
  }) {
    _showFlushbarBase(
      context,
      message,
      backgroundColor: Colors.blue,
      icon: Icons.info,
      duration: duration,
      margin: margin,
      position: position,
      onClosed: onClosed,
    );
  }

  static void _showFlushbarBase(
    BuildContext context,
    String message, {
    required Color backgroundColor,
    required IconData icon,
    FlushbarPosition? position,
    Duration duration = const Duration(seconds: 3),
    EdgeInsets margin = const EdgeInsets.all(8.0),
    VoidCallback? onClosed,
  }) {
    Flushbar(
      message: message,
      icon: Icon(
        icon,
        size: 28.0,
        color: Colors.white,
      ),
      duration: duration,
      backgroundColor: backgroundColor,
      flushbarStyle: FlushbarStyle.FLOATING,
      flushbarPosition: position ?? FlushbarPosition.BOTTOM,
      margin: margin,
      borderRadius: BorderRadius.circular(8.0),
    ).show(context).whenComplete(() {
      if (onClosed != null) {
        onClosed();
      }
    });
  }
}
