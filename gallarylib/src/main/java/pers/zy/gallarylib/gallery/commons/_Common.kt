package pers.zy.gallarylib.gallery.commons

import android.os.Build
import android.util.Log
import pers.zy.gallarylib.BuildConfig
import pers.zy.gallarylib.gallery.GalleryApp

/**
 * date: 2020/6/7   time: 5:45 PM
 * author zy
 * Have a nice day :)
 **/

fun getStatsBarHeight(): Int {
    var height = 0
    val resId = GalleryApp.INSTANCE.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resId > 0) {
        height = GalleryApp.INSTANCE.resources.getDimensionPixelSize(resId)
    }
    return height
}

fun lessThanQ(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
}

fun d(msg: String) {
    Log.d("ZZZ", msg)
}

fun e(msg: String) {
    Log.e("ZZZ", msg)
}