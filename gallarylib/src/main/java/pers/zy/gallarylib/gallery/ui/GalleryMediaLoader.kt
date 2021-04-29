package pers.zy.gallarylib.gallery.ui

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.IntRange
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import pers.zy.gallarylib.gallery.config.MediaInfoConfig
import pers.zy.gallarylib.gallery.tools.GalleryCommon
import pers.zy.gallarylib.gallery.model.MediaInfo
import pers.zy.gallarylib.gallery.model.BucketInfo

/**
 * date: 2020/6/10   time: 7:09 PM
 * author zy
 * Have a nice day :)
 **/
class GalleryMediaLoader(lifecycleOwner: LifecycleOwner) : CoroutineScope by MainScope(),
    LifecycleObserver {

    companion object {
        const val BUCKET_ID_NON_SELECTIVE = -1L

        val QUERY_URL: Uri = MediaStore.Files.getContentUri("external")

        /**
         * MIME_TYPE
         * */
        const val MIME_TYPE_IMAGE = 1
        const val MIME_TYPE_VIDEO = 2
        const val MIME_TYPE_ALL = 3

        /**
         * Media type
         * */
        const val MEDIA_TYPE_IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        const val MEDIA_TYPE_VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

        /**
         * MediaColumnName
         * */
        const val COLUMN_ID = MediaStore.Files.FileColumns._ID  //文件id
        const val COLUMN_DATA = MediaStore.Files.FileColumns.DATA  //文件绝对路径
        const val COLUMN_MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE
        const val COLUMN_MIME_TYPE = MediaStore.Files.FileColumns.MIME_TYPE
        const val COLUMN_WIDTH = MediaStore.Files.FileColumns.WIDTH //宽
        const val COLUMN_HEIGHT = MediaStore.Files.FileColumns.HEIGHT //高
        const val COLUMN_SIZE = MediaStore.Files.FileColumns.SIZE //大小
        const val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name" //当前所在文件夹名
        const val COLUMN_DISPLAY_NAME = MediaStore.Files.FileColumns.DISPLAY_NAME //文件名
        const val COLUMN_BUCKET_ID = "bucket_id" //当前所在文件夹id
        const val COLUMN_DURATION = "duration" //时间
        const val COLUMN_THUMB_DATA = MediaStore.Video.Thumbnails.DATA //缩略图绝对路径, targetSdk 29失效

        /**
         * sort
         * */
        const val DEFAULT_SORT = "${MediaStore.Files.FileColumns._ID} DESC"
    }

    //默认选择所有文件
    var selectBucketId: Long = BUCKET_ID_NON_SELECTIVE

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cancel()
    }

    /**
     * @param mimeType 获取媒体类型: [MIME_TYPE_IMAGE] [MIME_TYPE_VIDEO] [MIME_TYPE_ALL]
     * @param page 获取图片第几页
     * @param successCall
     * @param errorCall
     * */
    fun loadMedia(
        mimeType: Int,
        @IntRange(from = 0) page: Int = 0,
        successCall: (List<MediaInfo>) -> Unit,
        errorCall: ((Throwable) -> Unit)? = null
    ) {
        launch(coroutineContext + CoroutineExceptionHandler { _, throwable ->
            if (errorCall != null) {
                errorCall.invoke(throwable)
            } else {
                throw RuntimeException("media获取异常: $throwable")
            }
        }) {
            val mediaList = withContext(coroutineContext + Dispatchers.IO) {
                val resultList = mutableListOf<MediaInfo>()
                val cursor = GalleryCommon.app.contentResolver.query(
                    QUERY_URL,
                    getMediaProjection(),
                    getMediaSelection(mimeType),
                    getMediaSelectionArgs(mimeType),
                    """
                        $DEFAULT_SORT${
                        if (MediaInfoConfig.pagingLoad) {
                            " LIMIT ${page * MediaInfoConfig.perPage}, $MediaInfoConfig.perPage"
                        } else {
                            ""
                        }
                    }
                    """.trimIndent()
                )
                cursor?.let { c ->
                    if (!c.moveToFirst()) return@let
                    do {
                        val mediaInfo = createMediaInfo(c)
                        resultList.add(mediaInfo)
                    } while (c.moveToNext())
                    c.close()
                }
                cursor?.close()
                return@withContext resultList
            }
            successCall.invoke(mediaList)
        }
    }

    private fun createContentPathUri(id: Long): Uri = ContentUris.withAppendedId(QUERY_URL, id)

    /**
     * media file 获取需要的列信息: [COLUMN_ID], [COLUMN_DATA]等......
     * */
    private fun getMediaProjection(): Array<String> = arrayOf(
        COLUMN_ID,
        COLUMN_DATA,
        COLUMN_BUCKET_ID,
        COLUMN_MEDIA_TYPE,
        COLUMN_MIME_TYPE,
        COLUMN_WIDTH,
        COLUMN_HEIGHT,
        COLUMN_SIZE,
        COLUMN_DISPLAY_NAME,
        COLUMN_DURATION
    )

    /**
     * media file 筛选条件(SQL)
     * */
    private fun getMediaSelection(mimeType: Int): String {
        return when (mimeType) {
            MIME_TYPE_IMAGE -> {
                if (selectBucketId != BUCKET_ID_NON_SELECTIVE) {
                    "$COLUMN_MEDIA_TYPE=?" +
                            getGiftLimitSelection() +
                            " AND $COLUMN_BUCKET_ID=$selectBucketId" +
                            " AND $COLUMN_SIZE>0"
                } else {
                    "$COLUMN_MEDIA_TYPE=?" +
                            getGiftLimitSelection() +
                            " AND $COLUMN_SIZE>0"
                }
            }
            MIME_TYPE_VIDEO -> {
                if (selectBucketId != BUCKET_ID_NON_SELECTIVE) {
                    "$COLUMN_MEDIA_TYPE=?" +
                            " AND $COLUMN_BUCKET_ID=${selectBucketId}" +
                            " AND $COLUMN_DURATION>0" +
                            " AND $COLUMN_SIZE>0"
                } else {
                    "$COLUMN_MEDIA_TYPE=?" +
                            " AND $COLUMN_DURATION>0" +
                            " AND $COLUMN_SIZE>0"
                }
            }
            else ->  {
                if (selectBucketId != BUCKET_ID_NON_SELECTIVE) {
                    "($COLUMN_MEDIA_TYPE=?" +
                            " OR $COLUMN_MEDIA_TYPE=?)" +
                            " AND $COLUMN_BUCKET_ID=${selectBucketId}" +
                            getGiftLimitSelection() +
                            " AND $COLUMN_SIZE>0"
                } else {
                    "($COLUMN_MEDIA_TYPE=?" +
                            " OR $COLUMN_MEDIA_TYPE=?)" +
                            getGiftLimitSelection() +
                            " AND $COLUMN_SIZE>0"
                }
            }
        }
    }

    /**
     * media file 筛选条件value，对应筛选条件[getMediaSelection]中 '=?'
     * */
    private fun getMediaSelectionArgs(mimeType: Int): Array<String>? {
        return when (mimeType) {
            MIME_TYPE_IMAGE -> {
                arrayOf(MEDIA_TYPE_IMAGE.toString())
            }
            MIME_TYPE_VIDEO -> {
                arrayOf(MEDIA_TYPE_VIDEO.toString())
            }
            else -> {
                arrayOf(
                    MEDIA_TYPE_IMAGE.toString(),
                    MEDIA_TYPE_VIDEO.toString()
                )
            }
        }
    }

    private fun createMediaInfo(c: Cursor): MediaInfo {
        val id = c.getLong(c.getColumnIndex(COLUMN_ID))
        val realPath = c.getString(c.getColumnIndex(COLUMN_DATA))
        val contentUriPath = createContentPathUri(id).toString()
        // 在返回时，sdk>=Q, 将sendBoxPath修改为沙盒路径的文件path
        val sendBoxPath = realPath
        val mediaType = c.getInt(c.getColumnIndex(COLUMN_MEDIA_TYPE))
        val mimeType = c.getString(c.getColumnIndex(COLUMN_MIME_TYPE))
        val width = c.getLong(c.getColumnIndex(COLUMN_WIDTH))
        val height = c.getLong(c.getColumnIndex(COLUMN_HEIGHT))
        val size = c.getLong(c.getColumnIndex(COLUMN_SIZE))
        val displayName = c.getString(c.getColumnIndex(COLUMN_DISPLAY_NAME))
        val duration = c.getLong(c.getColumnIndex(COLUMN_DURATION))

        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            val thumbCursor = GalleryCommon.app.contentResolver.query(
                //TODO:ZY AndroidQ无法获取视频缩略图
                MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                arrayOf(
                    COLUMN_THUMB_DATA,
                    COLUMN_ID
                 ),
                "$COLUMN_ID=$id",
                null,
                null
            )
            return if (thumbCursor != null && thumbCursor.moveToFirst()) {
                val thumbnailsPath = thumbCursor.getString(thumbCursor.getColumnIndex(COLUMN_DATA))
                thumbCursor.close()
                MediaInfo(id, mediaType, realPath, contentUriPath, sendBoxPath, mimeType, size, displayName, width, height, thumbnailsPath, duration)
            } else {
                MediaInfo(id, mediaType, realPath, contentUriPath, sendBoxPath, mimeType, size, displayName, width, height, null, duration)
            }
        } else {
            return MediaInfo(id, mediaType, realPath, contentUriPath, sendBoxPath, mimeType, size, displayName, width, height, null, duration)
        }
    }

    /**
     * loadBucket
     * */

    fun loadBucket(
        mimeType: Int,
        bucketListCall: (List<BucketInfo>) -> Unit,
        errorCall: ((Throwable) -> Unit)? = null
    ) {
        launch(coroutineContext + CoroutineExceptionHandler { _, throwable ->
            if (errorCall != null) {
                errorCall.invoke(throwable)
            } else {
                throw RuntimeException("获取bucket异常: ${throwable}")
            }
        }) {
            val result = withContext(coroutineContext + Dispatchers.IO) {
                val resultList = mutableListOf<BucketInfo>()
                val cursor = GalleryCommon.app.contentResolver.query(
                    QUERY_URL,
                    getBucketProjection(),
                    getBucketSelection(mimeType),
                    getBucketSelectionArgs(mimeType),
                    DEFAULT_SORT
                )
                cursor?.let { c ->
                    if (GalleryCommon.lessThanAndroidQ()) {
                        loadBucketListBelowAndroidQ(c, resultList)
                    } else {
                        loadBucketListGreaterThanOrEqualsAndroidQ(c, resultList)
                    }
                }
                cursor?.close()
                return@withContext resultList
            }
            bucketListCall.invoke(result)
        }
    }

    /**
     * bucket file 获取需要的列信息
     * */
    private fun getBucketProjection(): Array<String> = if (GalleryCommon.lessThanAndroidQ()) {
        arrayOf(
            COLUMN_ID,
            COLUMN_DATA,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            COLUMN_MIME_TYPE,
            "COUNT(*) AS count"
        )
    } else {
        arrayOf(
            COLUMN_ID,
            COLUMN_DATA,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            COLUMN_MIME_TYPE
        )
    }

    /**
     * bucket file 筛选条件
     * */
    private fun getBucketSelection(mimeType: Int): String {
        return when (mimeType) {
            MIME_TYPE_IMAGE -> {
                if (GalleryCommon.lessThanAndroidQ()) {
                    "$COLUMN_MEDIA_TYPE=?" +
                            getGiftLimitSelection() +
                            " AND $COLUMN_SIZE>0)" +
                            " GROUP BY ($COLUMN_BUCKET_ID"
                } else {
                    "$COLUMN_MEDIA_TYPE=?" +
                            getGiftLimitSelection() +
                            " AND $COLUMN_SIZE>0"
                }
            }
            MIME_TYPE_VIDEO -> {
                if (GalleryCommon.lessThanAndroidQ()) {
                    "$COLUMN_MEDIA_TYPE=?" +
                            " AND $COLUMN_SIZE>0)" +
                            " GROUP BY ($COLUMN_BUCKET_ID"
                } else {
                    "$COLUMN_MEDIA_TYPE=?" +
                            " AND $COLUMN_SIZE>0"
                }
            }
            else -> {
                if (GalleryCommon.lessThanAndroidQ()) {
                    "($COLUMN_MEDIA_TYPE=?" +
                            getGiftLimitSelection() +
                            " OR $COLUMN_MEDIA_TYPE=?)" +
                            " AND $COLUMN_SIZE>0)" +
                            " GROUP BY ($COLUMN_BUCKET_ID"
                } else {
                    "($COLUMN_MEDIA_TYPE=?" +
                            getGiftLimitSelection() +
                            " OR $COLUMN_MEDIA_TYPE=?)" +
                            " AND $COLUMN_SIZE>0"
                }
            }
        }
    }


    /**
     * bucket file 筛选条件value，对应筛选条件[getBucketSelection]中 '=?'
     * */
    private fun getBucketSelectionArgs(mimeType: Int): Array<String>? {
        return when (mimeType) {
            MIME_TYPE_IMAGE -> {
                arrayOf(MEDIA_TYPE_IMAGE.toString())
            }
            MIME_TYPE_VIDEO -> {
                arrayOf(MEDIA_TYPE_VIDEO.toString())
            }
            else -> {
                arrayOf(
                    MEDIA_TYPE_IMAGE.toString(),
                    MEDIA_TYPE_VIDEO.toString()
                )
            }
        }
    }

    /**
     * 低于[Build.VERSION_CODES.Q]获取每个bucket count方法
     * */
    private fun loadBucketListBelowAndroidQ(c: Cursor, resultList: MutableList<BucketInfo>) {
        if (c.moveToFirst()) {
            val nonSelectiveBucketInfo = createNonSelectiveBucket(c)
            resultList.add(nonSelectiveBucketInfo)
            do {
                val bucket = createBucket(c)
                resultList.add(bucket)
                nonSelectiveBucketInfo.count += bucket.count
            } while (c.moveToNext())
        }
    }

    /**
     * 大于等于[Build.VERSION_CODES.Q]获取每个bucket count方法
     * */
    private fun loadBucketListGreaterThanOrEqualsAndroidQ(c: Cursor, resultList: MutableList<BucketInfo>) {
        if (c.moveToFirst()) {
            val bucketIdInfoMap = hashMapOf<Long, BucketInfo>()
            val nonSelectiveBucketInfo = createNonSelectiveBucket(c)
            resultList.add(nonSelectiveBucketInfo)
            do {
                val bucketId = c.getLong(c.getColumnIndex(COLUMN_BUCKET_ID))
                val bucketInfo = if (bucketIdInfoMap[bucketId] == null) {
                    val newBucket = createBucket(c)
                    bucketIdInfoMap[bucketId] = newBucket
                    newBucket
                } else {
                    bucketIdInfoMap[bucketId]!!
                }
                bucketInfo.count++
                nonSelectiveBucketInfo.count++
            } while (c.moveToNext())
            bucketIdInfoMap.values.forEach {
                resultList.add(it)
            }
        }
    }

    private fun createBucket(c: Cursor): BucketInfo {
        val bucketId = c.getLong(c.getColumnIndex(COLUMN_BUCKET_ID))
        val bucketDisplayName = c.getString(c.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME)) ?: "其他"
        val path = c.getString(c.getColumnIndex(COLUMN_DATA))
        val id = c.getLong(c.getColumnIndex(COLUMN_ID))
        val contentUriPath = createContentPathUri(id).toString()
        return if (GalleryCommon.lessThanAndroidQ()) {
            val count = c.getInt(c.getColumnIndex("count"))
            BucketInfo(bucketId, bucketDisplayName, count, path, contentUriPath)
        } else {
            BucketInfo(bucketId, bucketDisplayName, 0, path, contentUriPath)
        }
    }

    private fun createNonSelectiveBucket(c: Cursor): BucketInfo {
        val sumPreviewId = c.getLong(c.getColumnIndex(COLUMN_ID))
        val sumPreviewRealPath = c.getString(c.getColumnIndex(COLUMN_DATA))
        val sumPreviewContentUri = createContentPathUri(sumPreviewId)
        return BucketInfo(BUCKET_ID_NON_SELECTIVE, "所有", 0, sumPreviewRealPath, sumPreviewContentUri.toString())
    }

    private fun getGiftLimitSelection(): String {
        return if (MediaInfoConfig.showGif) {
            " AND $COLUMN_MIME_TYPE!='image/gif'"
        } else {
            ""
        }
    }
}