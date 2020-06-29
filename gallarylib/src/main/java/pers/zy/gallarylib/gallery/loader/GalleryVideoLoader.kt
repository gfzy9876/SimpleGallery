package pers.zy.gallarylib.gallery.loader

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.annotation.IntRange
import pers.zy.gallarylib.gallery.commons.belowAndroidQ
import pers.zy.gallarylib.gallery.model.BaseMediaInfo
import pers.zy.gallarylib.gallery.model.MediaVideoInfo

/**
 * date: 2020/6/28   time: 3:36 PM
 * author zy
 * Have a nice day :)
 **/
class GalleryVideoLoader(context: Context) : BaseGalleryMediaLoader(context) {

    override fun getMediaSelection(): String =
        if (selectBucketId != BUCKET_ID_NO_SELECT) {
            "${COLUMN_MEDIA_TYPE}=?" +
                    " AND ${COLUMN_BUCKET_ID}=${selectBucketId}" +
                    " AND ${COLUMN_DURATION}>0" +
                    " AND ${COLUMN_SIZE}>0"
        } else {
            "${COLUMN_MEDIA_TYPE}=?" +
                    " AND ${COLUMN_DURATION}>0" +
                    " AND ${COLUMN_SIZE}>0"
        }

    override fun getMediaSelectionArgs(): Array<String> =
        arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

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
        val duration = c.getLong(c.getColumnIndex(COLUMN_DURATION))
        val thumbCursor = context.contentResolver.query(
            MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
            arrayOf(
                COLUMN_THUMB_DATA,
                COLUMN_ID
            ),
            "${COLUMN_ID}=$id",
            null,
            null
        )
        return if (thumbCursor != null && thumbCursor.moveToFirst()) {
            val thumbnailsPath = thumbCursor.getString(thumbCursor.getColumnIndex(COLUMN_DATA))
            thumbCursor.close()
            MediaVideoInfo(path, contentUriPath, mimeType, size, displayName, width, height, thumbnailsPath, duration)
        } else {
            // TODO: 2020/6/28 处理cursor==null
//            throw RuntimeException("处理cursor==null情况！！！！！！！！！！！！！")
            MediaVideoInfo(path, contentUriPath, mimeType, size, displayName, width, height, "", duration)
        }
    }

}