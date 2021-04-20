package pers.zy.gallarylib.gallery.tools

import android.app.Application
import android.os.Build
import android.util.TypedValue
import android.widget.Toast

/**
 * date: 2020/6/7   time: 5:45 PM
 * author zy
 * Have a nice day :)
 **/

object GallaryCommon {
    lateinit var app: Application

    fun init(app: Application) {
        this.app = app
    }

    fun getStatsBarHeight(): Int {
        var height = 0
        val resId = app.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            height = app.resources.getDimensionPixelSize(resId)
        }
        return height
    }

    fun getScreenHeight(): Int = app.resources.displayMetrics.heightPixels

    fun lessThanAndroidQ(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    fun makeToast(msg: String) {
        Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
    }
}

val Float.dp: Int
    get() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, GallaryCommon.app.resources.displayMetrics).toInt()
    }

val Float.dpF: Float
    get() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, GallaryCommon.app.resources.displayMetrics)
    }

