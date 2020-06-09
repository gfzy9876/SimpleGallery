package pers.zy.gallarylib

import android.content.Context

/**
 * date: 2020/6/7   time: 5:45 PM
 * author zy
 * Have a nice day :)
 **/

fun getStatsBarHeight(context: Context): Int {
    var height = 0
    val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resId > 0) {
        height = context.resources.getDimensionPixelSize(resId)
    }
    return height
}