package pers.zy.gallarylib.gallery.config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pers.zy.gallarylib.gallery.ui.GalleryMediaLoader

/**
 * date: 4/20/21   time: 11:07 AM
 * author zy
 * Have a nice day :)
 **/

@Parcelize
class MediaInfoConfig(var minMediaCount: Int = 1,
        var maxMediaCount: Int = 9,
        var mimeType: Int = GalleryMediaLoader.MIME_TYPE_ALL,
        var showCamera: Boolean = false,
        var targetName: String = "") : Parcelable