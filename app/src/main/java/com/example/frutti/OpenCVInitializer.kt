package com.example.frutti

import android.content.Context
import android.util.Log
import org.opencv.android.OpenCVLoader // Import the loader

object OpenCVInitializer {
    private const val TAG = "OpenCVInitializer"
    private var isInitialized = false

    fun initOpenCV(context: Context, callback: (() -> Unit)? = null) {
        if (isInitialized) {
            Log.d(TAG, "OpenCV already initialized.")
            callback?.invoke()
            return
        }

        // For OpenCV 4.9.0+, rely solely on initLocal() or initDebug()
        // Ensure the correct Gradle dependency ('org.opencv:opencv:4.9.0' or SDK module import)
        // is used so that native libraries are bundled.
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully using initLocal()")
            isInitialized = true
            callback?.invoke()
        } else {
            // initLocal() failed. This means the native libraries (.so files)
            // were not found or couldn't be loaded from within the app package.
            // This usually indicates an issue with the Gradle dependency setup
            // or how the native libs were included.
            Log.e(TAG, "OpenCV initialization failed using initLocal(). Check Gradle dependencies and native libs inclusion.")
            // You could add more specific error handling or feedback here if needed.

            // --- DO NOT USE initAsync / BaseLoaderCallback for OpenCV 4.9.0+ ---
        }
    }

    // Function remains, although marked as unused in your previous screenshots.
    fun isOpenCVInitialized(): Boolean {
        return isInitialized
    }
}