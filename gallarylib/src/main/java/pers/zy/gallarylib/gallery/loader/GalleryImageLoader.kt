package pers.zy.gallarylib.gallery.loader

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.annotation.IntRange
import pers.zy.gallarylib.gallery.commons.belowAndroidQ
import pers.zy.gallarylib.gallery.model.BaseMediaInfo
import pers.zy.gallarylib.gallery.model.MediaImageInfo

/**
 * date: 2020/6/9   time: 3:29 PM
 * author zy
 * Have a nice day :)
 **/
class GalleryImageLoader(context: Context) : BaseGalleryMediaLoader(context) {

    override fun getMediaSelection(): String =
        if (selectBucketId != BUCKET_ID_NO_SELECT) {
            "${COLUMN_MEDIA_TYPE}=?" +
                    " AND ${COLUMN_MIME_TYPE}!='image/gif'" +
                    " AND ${COLUMN_BUCKET_ID}=$selectBucketId" +
                    " AND ${COLUMN_SIZE}>0"
        } else {
            "${COLUMN_MEDIA_TYPE}=?" +
                    " AND ${COLUMN_MIME_TYPE}!='image/gif'" +
                    " AND ${COLUMN_SIZE}>0"
        }

    override fun getMediaSelectionArgs(): Array<String> =
        arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())

    override fun getMediaSortOrder(@IntRange(from = 0) page: Int, perPage: Int): String =
        "$DEFAULT_SORT LIMIT ${page * perPage}, $perPage"

    override fun createMediaInfo(c: Cursor, context: Context): BaseMediaInfo {
        val id = c.getLong(c.getColumnIndex(COLUMN_ID))
        val path = c.getString(c.getColumnIndex(COLUMN_DATA))
        val contentUriPath = if (belowAndroidQ()) {
            path
        } else {
            createContentPathUri(id).toString()
        }
        val mimeType = c.getString(c.getColumnIndex(COLUMN_MIME_TYPE))
        val width = c.getLong(c.getColumnIndex(COLUMN_WIDTH))
        val height = c.getLong(c.getColumnIndex(COLUMN_HEIGHT))
        val size = c.getLong(c.getColumnIndex(COLUMN_SIZE))
        val displayName = c.getString(c.getColumnIndex(COLUMN_DISPLAY_NAME))
        return MediaImageInfo(path, contentUriPath, mimeType, size, displayName, width, height)
    }
}