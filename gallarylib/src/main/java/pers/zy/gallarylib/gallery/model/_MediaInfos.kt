package pers.zy.gallarylib.gallery.model

import android.os.Parcel
import android.os.Parcelable

/**
 * date: 2020/6/7   time: 3:38 PM
 * author zy
 * Have a nice day :)
 **/

data class MediaImageInfo(
    var realPath: String,
    var contentUriPath: String,
    var mimeType: String,
    var width: Int,
    var height: Int,
    var size: Long,
    var displayName: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(realPath)
        parcel.writeString(contentUriPath)
        parcel.writeString(mimeType)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeLong(size)
        parcel.writeString(displayName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaImageInfo> {
        override fun createFromParcel(parcel: Parcel): MediaImageInfo {
            return MediaImageInfo(parcel)
        }

        override fun newArray(size: Int): Array<MediaImageInfo?> {
            return arrayOfNulls(size)
        }
    }
}

data class MediaVideoInfo(
    var path: String,
    var size: Int?,
    var title: String,
    var mimeType: Int,
    var width: Int?,
    var height: Int?,
    var thumbPath: String?
)