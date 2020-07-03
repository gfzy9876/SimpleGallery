package pers.zy.gallarylib.gallery.model

import android.os.Parcel
import android.os.Parcelable

/**
 * date: 2020/6/11   time: 2:15 PM
 * author zy
 * Have a nice day :)
 **/
open class LocalMediaInfo(
    var realPath: String,
    var contentUriPath: String,
    var mediaType: Int,
    var mimeType: String,
    var size: Long,
    var displayName: String,
    var width: Long,
    var height: Long,
    var thumbnailPath: String? = null,
    var duration: Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(realPath)
        parcel.writeString(contentUriPath)
        parcel.writeInt(mediaType)
        parcel.writeString(mimeType)
        parcel.writeLong(size)
        parcel.writeString(displayName)
        parcel.writeLong(width)
        parcel.writeLong(height)
        parcel.writeString(thumbnailPath)
        parcel.writeLong(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocalMediaInfo> {
        override fun createFromParcel(parcel: Parcel): LocalMediaInfo {
            return LocalMediaInfo(parcel)
        }

        override fun newArray(size: Int): Array<LocalMediaInfo?> {
            return arrayOfNulls(size)
        }
    }

    override fun hashCode(): Int {
        var hash = realPath.hashCode()
        hash = hash * 31 + mimeType.hashCode()
        hash = hash * 31 + mediaType.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        return if (other is LocalMediaInfo) {
            hashCode() == other.hashCode()
        } else {
            false
        }
    }

    override fun toString(): String {
        return "BaseMediaInfo(realPath='$realPath', contentUriPath='$contentUriPath', mimeType='$mimeType', size=$size, displayName='$displayName', width=$width, height=$height, thumbnailPath=$thumbnailPath, duration=$duration)"
    }

}