package pers.zy.gallerylib.tools

import android.util.Log
import pers.zy.gallerylib.BuildConfig

/**
 * date: 2020/6/11   time: 2:27 PM
 * author zy
 * Have a nice day :)
 **/

private const val TAG = "ZGalleryModel"

internal fun d(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TAG, msg)
    }
}

internal fun w(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.w(TAG, msg)
    }
}

internal fun e(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.e(TAG, msg)
    }
}