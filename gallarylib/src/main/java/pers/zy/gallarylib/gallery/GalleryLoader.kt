package pers.zy.gallarylib.gallery

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.*
import pers.zy.gallerymodel.gallery.model.MediaImageInfo
import java.lang.ref.WeakReference

/**
 * date: 2020/6/9   time: 3:29 PM
 * author zy
 * Have a nice day :)
 **/
class GalleryLoader(private val contextReference: WeakReference<Context>) : CoroutineScope by MainScope() {

    companion object {
        private var INSTANCE: GalleryLoader? = null
        fun getInstance(context: Context): GalleryLoader {
            if (INSTANCE == null) {
                synchronized(GalleryLoader::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = GalleryLoader(WeakReference(context.applicationContext))
                    }
                    return INSTANCE!!
                }
            }
            return INSTANCE!!
        }

        private val QUERY_URL = MediaStore.Files.getContentUri("external")
        private const val IMAGE_SORT = "${MediaStore.Files.FileColumns._ID} DESC"
        private val IMAGE_PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID
        )
        private const val IMAGE_SELECTION = "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=?" +
                " AND ${MediaStore.Files.FileColumns.MIME_TYPE}!='image/gif')" +
                " AND ${MediaStore.Files.FileColumns.SIZE}>0"
        private val IMAGE_SELECTION_ARGS = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
    }

    fun loadImage(imageListCall: (List<MediaImageInfo>) -> Unit, errorCall: (Throwable) -> Unit) {
        launch(coroutineContext + CoroutineExceptionHandler { _, throwable -> errorCall.invoke(throwable) }) {
            val async = async(coroutineContext + Dispatchers.IO) {
                val resultList = mutableListOf<MediaImageInfo>()
                val context = contextReference.get() ?: return@async resultList
                val cursor = context.contentResolver.query(
                    QUERY_URL,
                    IMAGE_PROJECTION,
                    IMAGE_SELECTION,
                    IMAGE_SELECTION_ARGS,
                    IMAGE_SORT
                )
                cursor?.let { c ->
                    if (!c.moveToFirst()) {
                        return@let
                    }
                    do {
                        val id = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns._ID))
                        val path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA))
                        val mimeType = c.getString(c.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                        val width = c.getInt(c.getColumnIndex(MediaStore.Images.Media.WIDTH))
                        val height = c.getInt(c.getColumnIndex(MediaStore.Images.Media.HEIGHT))
                        val size = c.getLong(c.getColumnIndex(MediaStore.Images.Media.SIZE))
                        val bucketDisplayName = c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                        val displayName = c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                        val bucketId = c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))
                        resultList.add(MediaImageInfo(path, mimeType, width, height, size, displayName))
                    } while (c.moveToNext())
                }
                cursor?.close()
                resultList
            }

            val imageList = async.await()
            imageListCall.invoke(imageList)
        }
    }
}