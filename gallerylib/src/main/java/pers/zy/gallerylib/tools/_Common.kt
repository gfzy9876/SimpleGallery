package pers.zy.gallerylib.tools

import android.app.Application
import android.os.Build
import android.util.TypedValue
import android.widget.Toast

/**
 * date: 2020/6/7   time: 5:45 PM
 * author zy
 * Have a nice day :)
 **/

object GalleryCommon {
    lateinit var app: Application

    fun init(app: Application) {
        this.app = app
    }

    internal fun getStatsBarHeight(): Int {
        var height = 0
        val resId = app.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            height = app.resources.getDimensionPixelSize(resId)
        }
        return height
    }

    internal fun getScreenWidth(): Int = app.resources.displayMetrics.widthPixels

    internal fun getScreenHeight(): Int = app.resources.displayMetrics.heightPixels

    internal fun lessThanAndroidQ(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    internal fun makeToast(msg: String) {
        Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
    }
}

val Float.dp: Int
    get() {
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, GalleryCommon.app.resources.displayMetrics) + 0.5f).toInt()
    }

val Float.dpF: Float
    get() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, GalleryCommon.app.resources.displayMetrics)
    }

