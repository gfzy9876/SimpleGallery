package pers.zy.gallarylib.gallery.engine

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import pers.zy.gallarylib.gallery.commons.d

/**
 * date: 2020/6/30   time: 11:42 AM
 * author zy
 * Have a nice day :)
 **/
class GalleryObserver(handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        d("onChange: ${uri}")
    }
}