import 'package:flutter/material.dart';
import '../channels/native_channel_manager.dart';
import '../config/colors.dart';

class LoginWithIdScreen extends StatelessWidget {
  const LoginWithIdScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: InkWell(
          borderRadius: BorderRadius.circular(8),
          onTap: () {
            NativeChannelManager.isCall = true;
            NativeChannelManager().startVerifyEid();
          },
          child: Container(
            decoration: BoxDecoration(
              color: EkycColor.primaryColor,
              borderRadius: BorderRadius.circular(8),
            ),
            padding: const EdgeInsets.all(12),
            alignment: Alignment.center,
            margin: const EdgeInsets.only(top: 16),
            child: Stack(
              children: [
                Align(
                  alignment: Alignment.centerLeft,
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(4),
                    child: Image.asset("assets/images/cccd.jpg", width: 40),
                  ),
                ),
                const Center(
                  child: Text(
                    "Đăng nhập bằng CCCD",
                    style: TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.w500),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
