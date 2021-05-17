package pers.zy.gallerylib.model

import android.os.Parcel
import android.os.Parcelable

/**
 * date: 2020/6/11   time: 11:41 AM
 * author zy
 * Have a nice day :)
 **/

class BucketInfo(
    var id: Long,
    val displayName: String,
    var count: Int,
    val previewRealPath: String,
    val previewContentUri: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(displayName)
        parcel.writeInt(count)
        parcel.writeString(previewRealPath)
        parcel.writeString(previewContentUri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BucketInfo> {
        override fun createFromParcel(parcel: Parcel): BucketInfo {
            return BucketInfo(parcel)
        }

        override fun newArray(size: Int): Array<BucketInfo?> {
            return arrayOfNulls(size)
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is BucketInfo) {
            hashCode() == other.hashCode()
        } else {
            false
        }
    }

    override fun toString(): String {
        return "BucketInfo(id=$id, displayName='$displayName', count=$count, previewRealPath='$previewRealPath', previewContentUri='$previewContentUri')"
    }


}