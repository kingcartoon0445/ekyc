import 'package:flutter/material.dart';
import 'common_snackbar_content.dart';
import 'snack_bar_type.dart';

/// AnimatedSnackBar
/// Widget hiển thị snackbar với hiệu ứng scale và fade in
class AnimatedSnackBar extends StatefulWidget {
  final String message;
  final VoidCallback onClose;
  final SnackBarType type;

  ///Duration theo giây
  final int duration;
  const AnimatedSnackBar({
    super.key,
    required this.type,
    required this.message,
    required this.onClose,
    required this.duration,
  });

  @override
  State<AnimatedSnackBar> createState() => _AnimatedSnackBarState();
}

class _AnimatedSnackBarState extends State<AnimatedSnackBar>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    // Khởi tạo controller và animation
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 300),
    );

    // Animation fade in
    _fadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOut,
      reverseCurve: Curves.easeIn,
    ));
    // Animation scale
    _scaleAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOut,
      reverseCurve: Curves.easeIn,
    ));
    _controller.addStatusListener((status) {
      if (status == AnimationStatus.reverse) {
        widget.onClose();
      }
    });
    _controller.forward();

    // Bắt đầu đếm thời gian để tự động đóng snackbar
    Future.delayed(Duration(seconds: widget.duration, milliseconds: 700), () {
      if (mounted) {
        _controller.reverse();
      }
    });
  }

  // Lấy icon tương ứng với loại snackbar
  Icon? _getSnackBarIcon() {
    if (widget.type == SnackBarType.success) {
      return const Icon(
        Icons.check_circle_outline,
        color: Colors.green,
      );
    } else if (widget.type == SnackBarType.error) {
      return const Icon(
        Icons.error_outline,
        color: Colors.red,
      );
    }
    return null;
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return ScaleTransition(
          scale: _scaleAnimation,
          child: FadeTransition(
            opacity: _fadeAnimation,
            child: CustomSnackbar(
              icon: _getSnackBarIcon(),
              message: widget.message,
              onClose: () {
                _controller.reverse().then((_) {
                  widget.onClose();
                });
              },
            ),
          ),
        );
      },
    );
  }
}
