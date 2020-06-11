package pers.zy.gallarylib.gallery.loader

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.LifecycleOwner
import pers.zy.gallarylib.gallery.commons.belowAndroidQ
import pers.zy.gallarylib.gallery.commons.d
import pers.zy.gallarylib.gallery.model.BaseMediaInfo
import pers.zy.gallarylib.gallery.model.MediaImageInfo

/**
 * date: 2020/6/9   time: 3:29 PM
 * author zy
 * Have a nice day :)
 **/
class GalleryImageLoader(context: Context, lifecycleOwner: LifecycleOwner)
    : BaseGalleryMediaLoader<MediaImageInfo>(context) {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun getMediaProjection(): Array<String> = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.BUCKET_ID
    )

    override fun getMediaSelection(): String = if (selectBucketId != BUCKET_ID_NO_SELECT) {
        "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?" +
                " AND ${MediaStore.Files.FileColumns.MIME_TYPE}!='image/gif'" +
                " AND ${BUCKET_ID}=$selectBucketId" +
                " AND ${MediaStore.Files.FileColumns.SIZE}>0"
    } else {
        "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?" +
                " AND ${MediaStore.Files.FileColumns.MIME_TYPE}!='image/gif'" +
                " AND ${MediaStore.Files.FileColumns.SIZE}>0"
    }

    override fun getMediaSelectionArgs(): Array<String> =
        arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())

    override fun getMediaSortOrder(page: Int, perPage: Int): String = "$DEFAULT_IMAGE_SORT LIMIT ${page * perPage}, $perPage"

    override fun createMediaInfo(c: Cursor): BaseMediaInfo {
        val id = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns._ID))
        val path = c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.DATA))
        val contentUriPath = if (belowAndroidQ()) {
            path
        } else {
            createContentPathUri(id).toString()
        }
        val mimeType = c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE))
        val width = c.getInt(c.getColumnIndex(MediaStore.Files.FileColumns.WIDTH))
        val height = c.getInt(c.getColumnIndex(MediaStore.Files.FileColumns.HEIGHT))
        val size = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
        val bucketDisplayName = c.getString(c.getColumnIndex(BUCKET_DISPLAY_NAME)) ?: "其他"
        val displayName = c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))
        val bucketId = c.getString(c.getColumnIndex(BUCKET_ID))
        val mediaImageInfo = MediaImageInfo(path, contentUriPath, mimeType, size, displayName, width, height)
        return mediaImageInfo
    }
}