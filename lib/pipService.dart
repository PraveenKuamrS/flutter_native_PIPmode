// pip_service.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PipService {
  static const platform = MethodChannel('pip_channel');

  static Future<void> enterPipMode() async {
    try {
      await platform.invokeMethod('enterPipMode');
    } catch (e) {
      debugPrint('PIP Error: $e');
    }
  }
}
