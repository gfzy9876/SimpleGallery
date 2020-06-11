package pers.zy.gallarylib.gallery.model

import android.os.Parcel
import android.os.Parcelable
import pers.zy.gallarylib.gallery.commons.d

/**
 * date: 2020/6/11   time: 2:15 PM
 * author zy
 * Have a nice day :)
 **/
abstract class BaseMediaInfo(
    var realPath: String,
    var contentUriPath: String,
    var mimeType: String,
    var size: Long,
    var displayName: String
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(realPath)
        parcel.writeString(contentUriPath)
        parcel.writeString(mimeType)
        parcel.writeLong(size)
        parcel.writeString(displayName)
    }

    override fun toString(): String {
        return "BaseMediaInfo(realPath='$realPath', contentUriPath='$contentUriPath', mimeType='$mimeType', size=$size, displayName='$displayName')"
    }


}