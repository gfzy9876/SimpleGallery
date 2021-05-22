package pers.zy.gallerylib.ui

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
import pers.zy.gallerylib.config.MediaInfoConfig
import pers.zy.gallerylib.tools.GalleryCommon
import pers.zy.gallerylib.model.MediaInfo
import pers.zy.gallerylib.model.BucketInfo
import pers.zy.gallerylib.tools.d
import java.io.File

/**
 * date: 2020/6/10   time: 7:09 PM
 * author zy
 * Have a nice day :)
 **/
class GalleryMediaLoader(lifecycleOwner: LifecycleOwner) : CoroutineScope by MainScope(), LifecycleObserver {

    companion object {
        const val BUCKET_ID_NON_SELECTIVE = -1L

        val QUERY_URL: Uri = MediaStore.Files.getContentUri(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.VOLUME_EXTERNAL
        } else {
            "external"
        })

        /**
         * SELECT_MIME_TYPE
         * */
        const val SELECT_MIME_TYPE_IMAGE = 1
        const val SELECT_MIME_TYPE_VIDEO = 2
        const val SELECT_MIME_TYPE_ALL = 3

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
        const val COLUMN_DISPLAY_NAME = MediaStore.Files.FileColumns.DISPLAY_NAME //文件名

        val COLUMN_BUCKET_DISPLAY_NAME = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//当前所在文件夹名
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        } else {
            "bucket_display_name"
        }
        val COLUMN_BUCKET_ID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//当前所在文件夹id
            MediaStore.Files.FileColumns.BUCKET_ID
        } else {
            "bucket_id"
        }
        val COLUMN_DURATION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//时间
            MediaStore.Files.FileColumns.DURATION
        } else {
            "duration"
        }

        /**
         * sort
         * */
        const val DEFAULT_SORT = "$COLUMN_ID DESC"
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cancel()
    }

    /**
     * @param selectMimeType 获取媒体类型: [SELECT_MIME_TYPE_IMAGE] [SELECT_MIME_TYPE_VIDEO] [SELECT_MIME_TYPE_ALL]
     * @param bucketId 文件夹id，默认不区分文件夹，选择所有文件
     * @param page 获取图片第几页
     * */
    fun loadMedia(
        selectMimeType: Int,
        bucketId: Long = BUCKET_ID_NON_SELECTIVE,
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
                    getMediaSelection(selectMimeType, bucketId),
                    getMediaSelectionArgs(selectMimeType),
                    getSortOrder(page)
                )
                cursor?.let { c ->
                    if (!c.moveToFirst()) return@let
                    do {
                        createMediaInfo(c)?.let {
                            resultList.add(it)
                        }
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
    private fun getMediaSelection(selectMimeType: Int, bucketId: Long): String {
        return when (selectMimeType) {
            SELECT_MIME_TYPE_IMAGE -> {
                if (bucketId != BUCKET_ID_NON_SELECTIVE) {
                    "$COLUMN_MEDIA_TYPE=?" +
                            getGifLimitSelection() +
                            " AND $COLUMN_BUCKET_ID=$bucketId" +
                            " AND $COLUMN_SIZE>0"
                } else {
                    "$COLUMN_MEDIA_TYPE=?" +
                            getGifLimitSelection() +
                            " AND $COLUMN_SIZE>0"
                }
            }
            SELECT_MIME_TYPE_VIDEO -> {
                if (bucketId != BUCKET_ID_NON_SELECTIVE) {
                    "$COLUMN_MEDIA_TYPE=?" +
                            " AND $COLUMN_BUCKET_ID=${bucketId}" +
                            " AND $COLUMN_DURATION>0" +
                            " AND $COLUMN_SIZE>0"
                } else {
                    "$COLUMN_MEDIA_TYPE=?" +
                            " AND $COLUMN_DURATION>0" +
                            " AND $COLUMN_SIZE>0"
                }
            }
            else ->  {
                if (bucketId != BUCKET_ID_NON_SELECTIVE) {
                    "($COLUMN_MEDIA_TYPE=?" +
                            " OR $COLUMN_MEDIA_TYPE=?)" +
                            " AND $COLUMN_BUCKET_ID=${bucketId}" +
                            getGifLimitSelection() +
                            " AND $COLUMN_SIZE>0"
                } else {
                    "($COLUMN_MEDIA_TYPE=?" +
                            " OR $COLUMN_MEDIA_TYPE=?)" +
                            getGifLimitSelection() +
                            " AND $COLUMN_SIZE>0"
                }
            }
        }
    }

    /**
     * media file 筛选条件value，对应筛选条件[getMediaSelection]中 '=?'
     * */
    private fun getMediaSelectionArgs(selectMimeType: Int): Array<String>? {
        return when (selectMimeType) {
            SELECT_MIME_TYPE_IMAGE -> {
                arrayOf(MEDIA_TYPE_IMAGE.toString())
            }
            SELECT_MIME_TYPE_VIDEO -> {
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
     * media file 排列方式：按id降序排列；以及是否需要分页
     * */
    private fun getSortOrder(page: Int): String {
        return "$DEFAULT_SORT${if (MediaInfoConfig.pagingLoad) {
            " LIMIT ${page * MediaInfoConfig.perPage}, $MediaInfoConfig.perPage"
        } else {
            ""
        }}"
    }

    private fun createMediaInfo(c: Cursor): MediaInfo? {
        val id = c.getLong(c.getColumnIndex(COLUMN_ID))
        val realPath = c.getString(c.getColumnIndex(COLUMN_DATA))
        val contentUriPath = createContentPathUri(id).toString()
        /** 在返回时，[Build.VERSION.SDK_INT]>=[Build.VERSION_CODES.Q], 将sendBoxPath修改为沙盒路径的文件path*/
        val sendBoxPath = realPath
        val mediaType = c.getInt(c.getColumnIndex(COLUMN_MEDIA_TYPE))
        val mimeType = c.getString(c.getColumnIndex(COLUMN_MIME_TYPE))
        val width = c.getLong(c.getColumnIndex(COLUMN_WIDTH))
        val height = c.getLong(c.getColumnIndex(COLUMN_HEIGHT))
        val size = c.getLong(c.getColumnIndex(COLUMN_SIZE))
        val displayName = c.getString(c.getColumnIndex(COLUMN_DISPLAY_NAME))
        val duration = c.getLong(c.getColumnIndex(COLUMN_DURATION))
        val file = File(realPath)
        return if (file.exists() && file.length() > 0) {
            MediaInfo(id, mediaType, realPath, contentUriPath, sendBoxPath, mimeType, size, displayName, width, height, duration)
        } else {
            null
        }
    }

    /**
     * loadBucket
     * @param selectMimeType 获取媒体类型: [SELECT_MIME_TYPE_IMAGE] [SELECT_MIME_TYPE_VIDEO] [SELECT_MIME_TYPE_ALL]
     * */
    fun loadBucket(
        selectMimeType: Int,
        successCall: (List<BucketInfo>) -> Unit,
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
                    getBucketSelection(selectMimeType),
                    getBucketSelectionArgs(selectMimeType),
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
            successCall.invoke(result)
        }
    }

    /**
     * bucket file 获取需要的列信息
     * */
    private fun getBucketProjection(): Array<String> = if (GalleryCommon.lessThanAndroidQ()) {
        arrayOf(
            COLUMN_ID,
//            COLUMN_DATA,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            COLUMN_MIME_TYPE,
            "COUNT(*) AS count"
        )
    } else {
        arrayOf(
            COLUMN_ID,
//            COLUMN_DATA,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            COLUMN_MIME_TYPE
        )
    }

    /**
     * bucket file 筛选条件
     * */
    private fun getBucketSelection(selectMimeType: Int): String {
        return when (selectMimeType) {
            SELECT_MIME_TYPE_IMAGE -> {
                if (GalleryCommon.lessThanAndroidQ()) {
                    "$COLUMN_MEDIA_TYPE=?" +
                            getGifLimitSelection() +
                            " AND $COLUMN_SIZE>0)" +
                            " GROUP BY ($COLUMN_BUCKET_ID"
                } else {
                    "$COLUMN_MEDIA_TYPE=?" +
                            getGifLimitSelection() +
                            " AND $COLUMN_SIZE>0"
                }
            }
            SELECT_MIME_TYPE_VIDEO -> {
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
                            getGifLimitSelection() +
                            " OR $COLUMN_MEDIA_TYPE=?)" +
                            " AND $COLUMN_SIZE>0)" +
                            " GROUP BY ($COLUMN_BUCKET_ID"
                } else {
                    "($COLUMN_MEDIA_TYPE=?" +
                            getGifLimitSelection() +
                            " OR $COLUMN_MEDIA_TYPE=?)" +
                            " AND $COLUMN_SIZE>0"
                }
            }
        }
    }


    /**
     * bucket file 筛选条件value，对应筛选条件[getBucketSelection]中 '=?'
     * */
    private fun getBucketSelectionArgs(selectMimeType: Int): Array<String> {
        return when (selectMimeType) {
            SELECT_MIME_TYPE_IMAGE -> {
                arrayOf(MEDIA_TYPE_IMAGE.toString())
            }
            SELECT_MIME_TYPE_VIDEO -> {
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
//        val path = c.getString(c.getColumnIndex(COLUMN_DATA))
        val id = c.getLong(c.getColumnIndex(COLUMN_ID))
        val contentUriPath = createContentPathUri(id).toString()
        return if (GalleryCommon.lessThanAndroidQ()) {
            val count = c.getInt(c.getColumnIndex("count"))
            BucketInfo(bucketId, bucketDisplayName, count, contentUriPath)
        } else {
            BucketInfo(bucketId, bucketDisplayName, 0, contentUriPath)
        }
    }

    private fun createNonSelectiveBucket(c: Cursor): BucketInfo {
        val sumPreviewId = c.getLong(c.getColumnIndex(COLUMN_ID))
//        val sumPreviewRealPath = c.getString(c.getColumnIndex(COLUMN_DATA))
        val sumPreviewContentUri = createContentPathUri(sumPreviewId)
        return BucketInfo(BUCKET_ID_NON_SELECTIVE, "所有", 0, sumPreviewContentUri.toString())
    }

    private fun getGifLimitSelection(): String {
        return if (MediaInfoConfig.showGif) {
            " AND $COLUMN_MIME_TYPE!='image/gif'"
        } else {
            ""
        }
    }
}