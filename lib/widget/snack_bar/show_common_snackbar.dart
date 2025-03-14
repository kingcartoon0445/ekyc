import 'dart:async';
import 'package:flutter/material.dart';
import 'animated_snackbar.dart';
import 'snack_bar_type.dart';

class SnackbarHandler {
  static bool _isSnackbarActive = false;
  static Timer? _debounceTimer;
  static void showCommonSnackbar({
    required BuildContext context,
    required String description,
    int autoCloseDuration = 3,
    SnackBarType type = SnackBarType.success,
    VoidCallback? onDismissedCallback,
  }) {
    if (_isSnackbarActive) return;

    _debounceTimer?.cancel();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: AnimatedSnackBar(
          type: type,
          message: description,
          duration: autoCloseDuration,
          onClose: () {
            ScaffoldMessenger.of(context).hideCurrentSnackBar();
            if (onDismissedCallback != null) {
              onDismissedCallback();
            }
            _isSnackbarActive = false;
          },
        ),
        padding: EdgeInsets.zero,
        margin: EdgeInsets.only(
          left: 12,
          right: 12,
          bottom: MediaQuery.paddingOf(context).bottom + 16,
        ),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(4),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        behavior: SnackBarBehavior.floating,
        onVisible: () {
          _isSnackbarActive = true;
        },
      ),
    );
    //Tạo debounce 3 giây để *hạn chế trường hợp hiển thị nhiều snackbar cùng lúc
    _debounceTimer = Timer(Duration(seconds: autoCloseDuration), () {
      _isSnackbarActive = false;
    });
  }
}
