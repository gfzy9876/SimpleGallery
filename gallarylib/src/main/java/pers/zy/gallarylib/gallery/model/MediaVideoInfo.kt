package pers.zy.gallarylib.gallery.model

import android.os.Parcel
import android.os.Parcelable

/**
 * date: 2020/6/11   time: 2:16 PM
 * author zy
 * Have a nice day :)
 **/
class MediaVideoInfo(
    realPath: String,
    contentUriPath: String,
    mimeType: String,
    size: Long,
    displayName: String,
    width: Long,
    height: Long,
    var thumbnailPath: String?,
    var duration: Long
) : BaseMediaInfo(
    realPath,
    contentUriPath,
    mimeType,
    size,
    displayName,
    width,
    height
) {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(thumbnailPath)
        parcel.writeLong(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaVideoInfo> {
        override fun createFromParcel(parcel: Parcel): MediaVideoInfo {
            return MediaVideoInfo(parcel)
        }

        override fun newArray(size: Int): Array<MediaVideoInfo?> {
            return arrayOfNulls(size)
        }
    }
}