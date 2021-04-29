package pers.zy.gallarylib.gallery.tools

import android.util.Log
import pers.zy.gallarylib.BuildConfig

/**
 * date: 2020/6/11   time: 2:27 PM
 * author zy
 * Have a nice day :)
 **/

private const val TAG = "ZGalleryModel"

fun d(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TAG, msg)
    }
}

fun w(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.w(TAG, msg)
    }
}

fun e(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.e(TAG, msg)
    }
}