package pers.zy.gallerymodel

import android.app.Application
import androidx.multidex.MultiDex
import pers.zy.gallerylib.tools.GalleryCommon

/**
 * date: 4/16/21   time: 6:41 PM
 * author zy
 * Have a nice day :)
 **/
class App : Application() {

    companion object {
        lateinit var INSTANCE: App
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        MultiDex.install(this)
        GalleryCommon.init(this)
    }

}