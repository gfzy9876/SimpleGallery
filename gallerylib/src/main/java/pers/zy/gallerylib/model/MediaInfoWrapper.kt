package pers.zy.gallerylib.model

/**
 * date: 4/15/21   time: 4:02 PM
 * author zy
 * Have a nice day :)
 **/
abstract class MediaInfoWrapper(val mediaInfo: MediaInfo, var selected: Boolean = false) {
    override fun hashCode(): Int = mediaInfo.id.hashCode()

    override fun equals(other: Any?): Boolean {
        return if (other is MediaInfoWrapper) {
            hashCode() == other.hashCode()
        } else {
            false
        }
    }
}

class ImageMediaInfoWrapper(mediaInfo: MediaInfo) : MediaInfoWrapper(mediaInfo)

class VideoMediaInfoWrapper(mediaInfo: MediaInfo) : MediaInfoWrapper(mediaInfo)