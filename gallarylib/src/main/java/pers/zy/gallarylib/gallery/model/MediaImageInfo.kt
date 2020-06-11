package pers.zy.gallarylib.gallery.model

import android.os.Parcel
import android.os.Parcelable

/**
 * date: 2020/6/7   time: 3:38 PM
 * author zy
 * Have a nice day :)
 **/
class MediaImageInfo(
    realPath: String,
    contentUriPath: String,
    mimeType: String,
    size: Long,
    displayName: String,
    var width: Int,
    var height: Int
) : BaseMediaInfo(
    realPath,
    contentUriPath,
    mimeType,
    size,
    displayName
) {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeInt(width)
        parcel.writeInt(height)
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

    override fun toString(): String {
        return "MediaImageInfo(${super.toString()} width=$width, height=$height)"
    }

}
