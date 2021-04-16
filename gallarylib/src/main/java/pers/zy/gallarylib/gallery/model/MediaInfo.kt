package pers.zy.gallarylib.gallery.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pers.zy.gallarylib.gallery.engine.GalleryMediaLoader

/**
 * date: 2020/6/11   time: 2:15 PM
 * author zy
 * Have a nice day :)
 **/
@Parcelize
class MediaInfo(
    val id: Long,
    var mediaType: Int,
    var realPath: String,
    var contentUriPath: String,
    var sendBoxPath: String,
    var mimeType: String,
    var size: Long,
    var displayName: String,
    var width: Long,
    var height: Long,
    var thumbnailPath: String? = null,
    var duration: Long = 0L
) : Parcelable {

    fun createMediaInfoWrapper(): MediaInfoWrapper? {
        return when (mediaType) {
            GalleryMediaLoader.MEDIA_TYPE_IMAGE -> {
                ImageMediaInfoWrapper(this)
            }
            GalleryMediaLoader.MEDIA_TYPE_VIDEO -> {
                VideoMediaInfoWrapper(this)
            }
            else -> {
                return null
            }
        }
    }

    override fun hashCode(): Int {
        var hash = realPath.hashCode()
        hash = hash * 31 + mimeType.hashCode()
        hash = hash * 31 + mediaType.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MediaInfo) {
            hashCode() == other.hashCode()
        } else {
            false
        }
    }

    override fun toString(): String {
        return "MediaInfo(" + "id=$id, " + "mediaType=$mediaType, " + "realPath='$realPath', " + "contentUriPath='$contentUriPath', " + "sendBoxPath='$sendBoxPath', " + "mimeType='$mimeType', " + "size=$size, " + "displayName='$displayName', " + "width=$width, " + "height=$height, " + "thumbnailPath=$thumbnailPath, " + "duration=$duration" + ")"
    }


}