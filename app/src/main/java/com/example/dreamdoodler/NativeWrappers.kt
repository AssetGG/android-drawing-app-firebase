package com.example.dreamdoodler

import android.graphics.Bitmap

class NativeWrappers {
    external fun setBrightness(bitmap: Bitmap, brightness: Float)
    external fun invertColors(bitmap: Bitmap)
    external fun noise(bitmap: Bitmap, noiseLevel: Int)
}