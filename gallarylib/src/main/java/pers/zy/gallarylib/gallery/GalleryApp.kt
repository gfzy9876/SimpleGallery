package pers.zy.gallarylib.gallery

import android.app.Application

/**
 * date: 2020/6/9   time: 5:24 PM
 * author zy
 * Have a nice day :)
 **/
class GalleryApp : Application() {

    companion object {
        lateinit var INSTANCE: GalleryApp
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}