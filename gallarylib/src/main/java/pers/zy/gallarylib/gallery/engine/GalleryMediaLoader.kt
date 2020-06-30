package pers.zy.gallarylib.gallery.engine

import android.content.Context
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
import pers.zy.gallarylib.gallery.commons.lessThanAndroidQ
import pers.zy.gallarylib.gallery.model.BaseMediaInfo
import pers.zy.gallarylib.gallery.model.BucketInfo
import pers.zy.gallarylib.gallery.model.MediaImageInfo
import pers.zy.gallarylib.gallery.model.MediaVideoInfo
import java.lang.ref.WeakReference

/**
 * date: 2020/6/10   time: 7:09 PM
 * author zy
 * Have a nice day :)
 **/
open class GalleryMediaLoader (context: Context) : CoroutineScope by MainScope(), LifecycleObserver {

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
        const val COLUMN_THUMB_DATA = MediaStore.Video.Thumbnails.DATA //缩略图绝对路径

        /**
         * sort
         * */
        const val DEFAULT_SORT = "${MediaStore.Files.FileColumns._ID} DESC"
    }

    private val contextReference: WeakReference<Context> = WeakReference(context)
    //默认选择所有文件
    var selectBucketId: Long = BUCKET_ID_NON_SELECTIVE

    init {
        if (context !is LifecycleOwner) {
            throw RuntimeException("context: $context not implements LifecycleOwner")
        }
        context.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cancel()
    }

    /**
     * @param mimeType 获取媒体类型: [MIME_TYPE_IMAGE] [MIME_TYPE_VIDEO] [MIME_TYPE_ALL]
     * @param page 获取图片第几页（索引从0开始）
     * @param perPage 每页加载图片个数
     * @param successCall
     * @param errorCall
     * */
    fun loadMedia(
        @IntRange(from = 1, to = 3) mimeType: Int,
        @IntRange(from = 0) page: Int = 0, perPage: Int = 200,
        successCall: (List<BaseMediaInfo>) -> Unit, errorCall: ((Throwable) -> Unit)? = null
    ) {
        launch(coroutineContext + CoroutineExceptionHandler { _, throwable ->
            if (errorCall != null) {
                errorCall.invoke(throwable)
            } else {
                throw RuntimeException("media获取异常: $throwable")
            }
        }) {
            val mediaList = withContext(coroutineContext + Dispatchers.IO) {
                val resultList = mutableListOf<BaseMediaInfo>()
                val context = contextReference.get() ?: return@withContext resultList
                val cursor = context.contentResolver.query(
                    QUERY_URL,
                    getMediaProjection(),
                    getMediaSelection(mimeType),
                    getMediaSelectionArgs(mimeType),
                    "$DEFAULT_SORT LIMIT ${page * perPage}, $perPage"
                )
                cursor?.let { c ->
                    if (!c.moveToFirst()) return@let
                    do {
                        val mediaInfo = createMediaInfo(c, context)
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

    private fun getGiftLimitSelection(): String {
        return if (false) {
            " AND ${COLUMN_MIME_TYPE}!='image/gif'"
        } else {
            ""
        }
    }

    private fun createContentPathUri(id: Long): Uri = QUERY_URL.buildUpon().appendPath(id.toString()).build()

    /**
     * media file 获取需要的列信息: [COLUMN_ID], [COLUMN_DATA]......
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
    private fun getMediaSelection(mimeType: Int): String? {
        return when (mimeType) {
            MIME_TYPE_IMAGE -> {
                if (selectBucketId != BUCKET_ID_NON_SELECTIVE) {
                    "${COLUMN_MEDIA_TYPE}=?" +
                            getGiftLimitSelection() +
                            " AND ${COLUMN_BUCKET_ID}=$selectBucketId" +
                            " AND ${COLUMN_SIZE}>0"
                } else {
                    "${COLUMN_MEDIA_TYPE}=?" +
                            getGiftLimitSelection() +
                            " AND ${COLUMN_SIZE}>0"
                }
            }
            MIME_TYPE_VIDEO -> {
                if (selectBucketId != BUCKET_ID_NON_SELECTIVE) {
                    "${COLUMN_MEDIA_TYPE}=?" +
                            " AND ${COLUMN_BUCKET_ID}=${selectBucketId}" +
                            " AND ${COLUMN_DURATION}>0" +
                            " AND ${COLUMN_SIZE}>0"
                } else {
                    "${COLUMN_MEDIA_TYPE}=?" +
                            " AND ${COLUMN_DURATION}>0" +
                            " AND ${COLUMN_SIZE}>0"
                }
            }
            else ->  {
                if (selectBucketId != BUCKET_ID_NON_SELECTIVE) {
                    "(${COLUMN_MEDIA_TYPE}=?" +
                            " OR ${COLUMN_MEDIA_TYPE}=?)" +
                            " AND ${COLUMN_BUCKET_ID}=${selectBucketId}" +
                            getGiftLimitSelection() +
                            " AND ${COLUMN_SIZE}>0"
                } else {
                    "(${COLUMN_MEDIA_TYPE}=?" +
                            " OR ${COLUMN_MEDIA_TYPE}=?)" +
                            getGiftLimitSelection() +
                            " AND ${COLUMN_SIZE}>0"
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
                arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
            }
            MIME_TYPE_VIDEO -> {
                arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
            }
            else -> {
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                )
            }
        }
    }

    private fun createMediaInfo(c: Cursor, context: Context): BaseMediaInfo {
        val id = c.getLong(c.getColumnIndex(COLUMN_ID))
        val path = c.getString(c.getColumnIndex(COLUMN_DATA))
        val contentUriPath = if (lessThanAndroidQ()) {
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

        val mediaType = c.getInt(c.getColumnIndex(COLUMN_MEDIA_TYPE))
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
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
//                throw RuntimeException("处理cursor==null情况！！！！！！！！！！！！！")
                MediaVideoInfo(path, contentUriPath, mimeType, size, displayName, width, height, "", duration)
            }
        } else {
            return MediaImageInfo(path, contentUriPath, mimeType, size, displayName, width, height)
        }
    }

    /**
     * loadBucket
     * */

    fun loadBucket(mimeType: Int, bucketListCall: (List<BucketInfo>) -> Unit, errorCall: ((Throwable) -> Unit)? = null) {
        launch(coroutineContext + CoroutineExceptionHandler { _, throwable ->
            if (errorCall != null) {
                errorCall.invoke(throwable)
            } else {
                throw RuntimeException("获取bucket异常: ${throwable}")
            }
        }) {
            val result = withContext(coroutineContext + Dispatchers.IO) {
                val resultList = mutableListOf<BucketInfo>()
                val context = contextReference.get() ?: return@withContext resultList
                val cursor = context.contentResolver.query(
                    QUERY_URL,
                    getBucketProjection(),
                    getBucketSelection(mimeType),
                    getBucketSelectionArgs(mimeType),
                    DEFAULT_SORT
                )
                cursor?.let { c ->
                    if (lessThanAndroidQ()) {
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
    private fun getBucketProjection(): Array<String> = if (lessThanAndroidQ()) {
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
                if (lessThanAndroidQ()) {
                    "${COLUMN_MEDIA_TYPE}=?" +
                            getGiftLimitSelection() +
                            " AND ${COLUMN_SIZE}>0)" +
                            " GROUP BY (${COLUMN_BUCKET_ID}"
                } else {
                    "${COLUMN_MEDIA_TYPE}=?" +
                            getGiftLimitSelection() +
                            " AND ${COLUMN_SIZE}>0"
                }
            }
            MIME_TYPE_VIDEO -> {
                if (lessThanAndroidQ()) {
                    "${COLUMN_MEDIA_TYPE}=?" +
                            " AND ${COLUMN_SIZE}>0)" +
                            " GROUP BY (${COLUMN_BUCKET_ID}"
                } else {
                    "${COLUMN_MEDIA_TYPE}=?" +
                            " AND ${COLUMN_SIZE}>0"
                }
            }
            else -> {
                if (lessThanAndroidQ()) {
                    "(${COLUMN_MEDIA_TYPE}=?" +
                            getGiftLimitSelection() +
                            " OR ${COLUMN_MEDIA_TYPE}=?)" +
                            " AND ${COLUMN_SIZE}>0)" +
                            " GROUP BY (${COLUMN_BUCKET_ID}"
                } else {
                    "(${COLUMN_MEDIA_TYPE}=?" +
                            getGiftLimitSelection() +
                            " OR ${COLUMN_MEDIA_TYPE}=?)" +
                            " AND ${COLUMN_SIZE}>0"
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
                arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
            }
            MIME_TYPE_VIDEO -> {
                arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
            }
            else -> {
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                )
            }
        }
    }

    /**
     * <[Build.VERSION_CODES.Q]获取每个bucket count方法
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
     * >=[Build.VERSION_CODES.Q]获取每个bucket count方法
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
        return if (lessThanAndroidQ()) {
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
}