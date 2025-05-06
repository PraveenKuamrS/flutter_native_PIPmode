package com.example.flutter_native_pip_mode

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.graphics.Rect // Add this import
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result

class MainActivity : FlutterActivity() {
    private val CHANNEL = "pip_channel"
    private var pipCallback: Result? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPip: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPip, newConfig)
        if (isInPip) {
            // Set fixed size for PIP window (similar to Rapido)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(3, 4)) // Portrait-style ratio
                .setSourceRectHint(Rect(0, 0, 300, 400)) // Exact dimensions
                .build()
            setPictureInPictureParams(params)
            
            pipCallback?.success(true)
            pipCallback = null
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "enterPipMode" -> {
                    pipCallback = result
                    enterPipMode(call.arguments as? Map<String, Any>)
                }
                "isPipSupported" -> {
                    result.success(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun enterPipMode(params: Map<String, Any>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspectRatio = params?.get("aspectRatio") as? Map<String, Int>
                val width = aspectRatio?.get("width") ?: 16
                val height = aspectRatio?.get("height") ?: 9
                
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(width, height))
                    .build().let { 
                        enterPictureInPictureMode(it) 
                    }
            } catch (e: Exception) {
                pipCallback?.error("PIP_ERROR", e.message, null)
                pipCallback = null
            }
        } else {
            pipCallback?.error(
                "UNSUPPORTED", 
                "PIP requires Android Oreo (8.0) or higher", 
                null
            )
            pipCallback = null
        }
    }
}
