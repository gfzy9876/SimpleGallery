package pers.zy.gallarylib.gallery.loader

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.IntRange
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import pers.zy.gallarylib.gallery.commons.belowAndroidQ
import pers.zy.gallarylib.gallery.model.BaseMediaInfo
import pers.zy.gallarylib.gallery.model.BucketInfo
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * date: 2020/6/10   time: 7:09 PM
 * author zy
 * Have a nice day :)
 **/
abstract class BaseGalleryMediaLoader (context: Context) : CoroutineScope by MainScope(), LifecycleObserver {

    companion object {
        const val BUCKET_ID_NO_SELECT = -1L
        val QUERY_URL: Uri = MediaStore.Files.getContentUri("external")

        const val DEFAULT_SORT = "${MediaStore.Files.FileColumns._ID} DESC"

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
    }

    private val contextReference: WeakReference<Context> = WeakReference(context)
    var selectBucketId: Long = BUCKET_ID_NO_SELECT

    init {
        if (context !is LifecycleOwner) {
            throw RuntimeException("context: $context is not LifecycleOwner")
        }
        context.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cancel()
    }

    /**
     * @param page 获取图片第几页（从0开始索引）
     * @param perPage 每页加载图片个数
     * @param bucketId 获取某一bucket内的图片
     * 加载索引的位置从 ${page * perPage} 开始，加载 ${perPage}个
     * @param mediaListCall
     * @param errorCall
     * */
    fun loadMedia(
        mediaType: MediaType,
        @IntRange(from = 0) page: Int = 0,
        perPage: Int = 100,
        mediaListCall: (List<BaseMediaInfo>) -> Unit,
        errorCall: ((Throwable) -> Unit)? = null
    ) {
        launch(coroutineContext + CoroutineExceptionHandler { _, throwable ->
            if (errorCall != null) {
                errorCall.invoke(throwable)
            } else {
                throw RuntimeException("media获取异常: $throwable")
            }
        }) {
            val async: Deferred<MutableList<BaseMediaInfo>> = async(coroutineContext + Dispatchers.IO) {
                val resultList = mutableListOf<BaseMediaInfo>()
                val context = contextReference.get() ?: return@async resultList
                val cursor = context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    getMediaProjection(),
                    getMediaSelection(),
                    getMediaSelectionArgs(),
                    getMediaSortOrder(page, perPage)
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
                resultList
            }

            mediaListCall.invoke(async.await())
        }
    }

    /**
     * 获取file投射的列信息: MediaStore.Files.FileColumns._ID, MediaStore.MediaColumns.MIME_TYPE.....
     * */
    private fun getMediaProjection(): Array<String> = arrayOf(
        COLUMN_ID,
        COLUMN_DATA,
        COLUMN_MIME_TYPE,
        COLUMN_WIDTH,
        COLUMN_HEIGHT,
        COLUMN_SIZE,
        COLUMN_DISPLAY_NAME,
        COLUMN_DURATION
    )

    /**
     * 筛选条件(SQL)
     * */
    protected abstract fun getMediaSelection(): String

    /**
     * 筛选条件value，对应筛选条件中 '=?'
     * */
    protected abstract fun getMediaSelectionArgs(): Array<String>?

    /**
     * 排序规则(SQL)
     * */
    protected abstract fun getMediaSortOrder(@IntRange(from = 0) page: Int, perPage: Int): String

    protected abstract fun createMediaInfo(c: Cursor, context: Context): BaseMediaInfo

    protected fun createContentPathUri(id: Long): Uri = QUERY_URL.buildUpon().appendPath(id.toString()).build()

    /**
     * 获取Bucket List
     * */
    private fun getBucketSelection(): String = if (belowAndroidQ()) {
        "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?" +
                " AND ${COLUMN_MIME_TYPE}!='image/gif'" +
                " AND ${COLUMN_SIZE}>0" + ")" +
                " GROUP BY (${COLUMN_BUCKET_ID}"
    } else {
        "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?" +
                " AND ${COLUMN_MIME_TYPE}!='image/gif'" +
                " AND ${COLUMN_SIZE}>0"
    }

    private fun getBucketProjection(): Array<String> = if (belowAndroidQ()) {
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

    fun loadBucket(bucketListCall: (List<BucketInfo>) -> Unit, errorCall: (Throwable) -> Unit) {
        launch(coroutineContext + CoroutineExceptionHandler { _, throwable -> errorCall.invoke(throwable) }) {
            val async: Deferred<MutableList<BucketInfo>> = async(coroutineContext + Dispatchers.IO) {
                val resultList = mutableListOf<BucketInfo>()
                val context = contextReference.get() ?: return@async resultList
                val cursor = context.contentResolver.query(
                    QUERY_URL,
                    getBucketProjection(),
                    getBucketSelection(),
                    getMediaSelectionArgs(),
                    DEFAULT_SORT
                )
                cursor?.let { c ->
                    if (belowAndroidQ()) {
                        initBucketListBelowAndroidQ(c, resultList)
                    } else {
                        initBucketListAndroidQ(c, resultList)
                    }
                }
                cursor?.close()
                resultList
            }
            bucketListCall.invoke(async.await())
        }
    }

    private fun initBucketListAndroidQ(c: Cursor, resultList: MutableList<BucketInfo>) {
        if (c.moveToFirst()) {
            val bucketIdInfoMap = hashMapOf<Long, BucketInfo>()
            val subBucketInfo = createSumBucket(c)
            resultList.add(subBucketInfo)
            do {
                val bucketId = c.getLong(c.getColumnIndex(COLUMN_BUCKET_ID))
                val bucketInfo = if (bucketIdInfoMap[bucketId] == null) {
                    createBucket(c)
                } else {
                    bucketIdInfoMap[bucketId]!!
                }
                bucketInfo.count++
                subBucketInfo.count++
                bucketIdInfoMap[bucketId] = bucketInfo
            } while (c.moveToNext())
            bucketIdInfoMap.values.forEach {
                resultList.add(it)
            }
        }
    }

    private fun initBucketListBelowAndroidQ(c: Cursor, resultList: MutableList<BucketInfo>) {
        if (c.moveToFirst()) {
            val subBucketInfo = createSumBucket(c)
            resultList.add(subBucketInfo)
            do {
                val bucket = createBucket(c)
                resultList.add(bucket)
                subBucketInfo.count += bucket.count
            } while (c.moveToNext())
        }
    }

    private fun createBucket(c: Cursor): BucketInfo {
        val bucketId = c.getLong(c.getColumnIndex(COLUMN_BUCKET_ID))
        val bucketDisplayName = c.getString(c.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME)) ?: "其他"
        val path = c.getString(c.getColumnIndex(COLUMN_DATA))
        val id = c.getLong(c.getColumnIndex(COLUMN_ID))
        val contentUriPath = createContentPathUri(id).toString()
        return if (belowAndroidQ()) {
            val count = c.getInt(c.getColumnIndex("count"))
            BucketInfo(bucketId, bucketDisplayName, count, path, contentUriPath)
        } else {
            BucketInfo(bucketId, bucketDisplayName, 0, path, contentUriPath)
        }
    }

    private fun createSumBucket(c: Cursor): BucketInfo {
        val sumPreviewId = c.getLong(c.getColumnIndex(COLUMN_ID))
        val sumPreviewRealPath = c.getString(c.getColumnIndex(COLUMN_DATA))
        val sumPreviewContentUri = createContentPathUri(sumPreviewId)
        return BucketInfo(BUCKET_ID_NO_SELECT, "所有", 0, sumPreviewRealPath, sumPreviewContentUri.toString())
    }
}