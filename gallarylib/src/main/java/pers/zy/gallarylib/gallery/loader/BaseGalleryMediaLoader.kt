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
import java.lang.ref.WeakReference

/**
 * date: 2020/6/10   time: 7:09 PM
 * author zy
 * Have a nice day :)
 **/
abstract class BaseGalleryMediaLoader<T>(context: Context) : CoroutineScope by MainScope(), LifecycleObserver {

    companion object {
        const val BUCKET_ID_NO_SELECT = -1L
        val QUERY_URL = MediaStore.Files.getContentUri("external")
        const val DEFAULT_IMAGE_SORT = "${MediaStore.Files.FileColumns._ID} DESC"
        val BUCKET_DISPLAY_NAME = if (belowAndroidQ()) {
            "bucket_display_name"
        } else {
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        }
        val BUCKET_ID = if (belowAndroidQ()) {
            "bucket_id"
        } else {
            MediaStore.Files.FileColumns.BUCKET_ID
        }
    }

    protected val contextReference: WeakReference<Context> = WeakReference(context)
    var selectBucketId: Long = BUCKET_ID_NO_SELECT

    init {
        if (context !is LifecycleOwner) {
            throw RuntimeException("context: $context is not LifecycleOwner")
        }
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
        @IntRange(from = 0) page: Int = 0,
        perPage: Int = 100,
        mediaListCall: (List<BaseMediaInfo>) -> Unit, errorCall: (Throwable) -> Unit
    ) {
        launch(coroutineContext + CoroutineExceptionHandler { _, throwable -> errorCall.invoke(throwable) }) {
            val async: Deferred<MutableList<BaseMediaInfo>> = async(coroutineContext + Dispatchers.IO) {
                val resultList = mutableListOf<BaseMediaInfo>()
                val context = contextReference.get() ?: return@async resultList
                val cursor = context.contentResolver.query(
                    QUERY_URL,
                    getMediaProjection(),
                    getMediaSelection(),
                    getMediaSelectionArgs(),
                    getMediaSortOrder(page, perPage)
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
                resultList
            }

            mediaListCall.invoke(async.await())
        }
    }

    protected abstract fun getMediaProjection(): Array<String>

    protected abstract fun getMediaSelection(): String

    protected abstract fun getMediaSelectionArgs(): Array<String>

    protected abstract fun getMediaSortOrder(page: Int, perPage: Int): String

    protected abstract fun createMediaInfo(c: Cursor): BaseMediaInfo

    protected fun createContentPathUri(id: Long): Uri = QUERY_URL.buildUpon().appendPath(id.toString()).build()


    /*获取Bucket List*/

    private fun getBucketSelection(): String = if (belowAndroidQ()) {
        "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?" +
                " AND ${MediaStore.Files.FileColumns.MIME_TYPE}!='image/gif'" +
                " AND ${MediaStore.Files.FileColumns.SIZE}>0" + ")" +
                " GROUP BY (${BUCKET_ID}"
    } else {
        "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?" +
                " AND ${MediaStore.Files.FileColumns.MIME_TYPE}!='image/gif'" +
                " AND ${MediaStore.Files.FileColumns.SIZE}>0"
    }

    private fun getBucketProjection(): Array<String> = if (belowAndroidQ()) {
        arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            BUCKET_ID,
            BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            "COUNT(*) AS count"
        )
    } else {
        arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            BUCKET_ID,
            BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
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
                    DEFAULT_IMAGE_SORT
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
                val bucketId = c.getLong(c.getColumnIndex(BUCKET_ID))
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
        val bucketId = c.getLong(c.getColumnIndex(BUCKET_ID))
        val bucketDisplayName = c.getString(c.getColumnIndex(BUCKET_DISPLAY_NAME)) ?: "其他"
        val path = c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.DATA))
        val id = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns._ID))
        val contentUriPath = createContentPathUri(id).toString()
        return if (belowAndroidQ()) {
            val count = c.getInt(c.getColumnIndex("count"))
            BucketInfo(bucketId, bucketDisplayName, count, path, contentUriPath)
        } else {
            BucketInfo(bucketId, bucketDisplayName, 0, path, contentUriPath)
        }
    }

    private fun createSumBucket(c: Cursor): BucketInfo {
        val sumPreviewId = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns._ID))
        val sumPreviewRealPath = c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.DATA))
        val sumPreviewContentUri = createContentPathUri(sumPreviewId)
        return BucketInfo(BUCKET_ID_NO_SELECT, "所有", 0, sumPreviewRealPath, sumPreviewContentUri.toString())
    }
}