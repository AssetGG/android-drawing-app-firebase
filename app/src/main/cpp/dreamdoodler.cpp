#include <jni.h>

#include <android/log.h>
#include <android/bitmap.h>
#include <algorithm>
#include <stdlib.h>
/**
 * This class uses c++ to make image adjustments to the bitmap.
 * Code adapted from Abakashap on Github
 * https://github.com/VMMobility/Android-NDK-Image-Processing-with-C-/blob/master/Android%20NDK%20Image%20Processing%20with%20C%2B%2B/jni/imageprocessing.cpp
 */

#define  LOG_TAG    "ADJUSTMENTS"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static int rgb_clamp(int value) {
    if(value > 255) {
        return 255;
    }
    if(value < 0) {
        return 0;
    }
    return value;
}

static void setBrightness(AndroidBitmapInfo* info, void* pixels, float brightnessValue){
    int xx, yy, red, green, blue;
    uint32_t* line;

    for(yy = 0; yy < info->height; yy++){
        line = (uint32_t*)pixels;
        for(xx =0; xx < info->width; xx++){

            //extract the RGB values from the pixel
            red = (int) ((line[xx] & 0x00FF0000) >> 16);
            green = (int)((line[xx] & 0x0000FF00) >> 8);
            blue = (int) (line[xx] & 0x000000FF );

            //manipulate each value
            red = rgb_clamp((int)(red * brightnessValue));
            green = rgb_clamp((int)(green * brightnessValue));
            blue = rgb_clamp((int)(blue * brightnessValue));

            // set the new pixel back in
            line[xx] =
                    ((red << 16) & 0x00FF0000) |
                    ((green << 8) & 0x0000FF00) |
                    (blue & 0x000000FF) | (0xFF000000);
        }

        pixels = (char*)pixels + info->stride;
    }
}

static void invert(AndroidBitmapInfo* info, void* pixels) {
    int xx, yy, red, green, blue;
    uint32_t* line;

    for(yy = 0; yy < info->height; yy++) {
        line = (uint32_t *) pixels;
        for (xx = 0; xx < info->width; xx++) {
            line[xx] = 0xFFFFFF - line[xx] | 0xFF000000;
        }

        pixels = (char *) pixels + info->stride;
    }
}


static void addNoise(AndroidBitmapInfo* info, void* pixels, int noiseLevel){
    int xx, yy, red, green, blue;
    uint32_t* line;

    for(yy = 0; yy < info->height; yy++){
        line = (uint32_t*)pixels;
        for(xx =0; xx < info->width; xx++){

            //extract the RGB values from the pixel
            red = (int) ((line[xx] & 0x00FF0000) >> 16);
            green = (int)((line[xx] & 0x0000FF00) >> 8);
            blue = (int) (line[xx] & 0x000000FF );

            //manipulate each value. Apply a jitter ranging from +/- noiseLevel / 2
            red = rgb_clamp((int)(red + (rand() % noiseLevel) / 2 - (rand() % noiseLevel) / 2));
            green = rgb_clamp((int)(green +  (rand() % noiseLevel) / 2 - (rand() % noiseLevel) / 2));
            blue = rgb_clamp((int)(blue +  (rand() % noiseLevel) / 2 - (rand() % noiseLevel) / 2));

            // set the new pixel back in
            line[xx] =
                    ((red << 16) & 0x00FF0000) |
                    ((green << 8) & 0x0000FF00) |
                    (blue & 0x000000FF) | (0xFF000000);
        }

        pixels = (char*)pixels + info->stride;
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_dreamdoodler_NativeWrappers_setBrightness(JNIEnv *env, jobject thiz,
                                                           jobject bitmap, jfloat brightness) {
    AndroidBitmapInfo  info;
    int ret;
    void* pixels;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    setBrightness(&info, pixels, brightness);

    AndroidBitmap_unlockPixels(env, bitmap);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_dreamdoodler_NativeWrappers_invertColors(JNIEnv *env, jobject thiz,
                                                          jobject bitmap) {
    AndroidBitmapInfo  info;
    int ret;
    void* pixels;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    invert(&info, pixels);

    AndroidBitmap_unlockPixels(env, bitmap);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_dreamdoodler_NativeWrappers_noise(JNIEnv *env, jobject thiz, jobject bitmap,
                                                   jint noise_level) {
    if(noise_level == 0) {
        // do nothing if the noise level is zero
        return;
    }
    AndroidBitmapInfo  info;
    int ret;
    void* pixels;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    addNoise(&info, pixels, noise_level);

    AndroidBitmap_unlockPixels(env, bitmap);
}