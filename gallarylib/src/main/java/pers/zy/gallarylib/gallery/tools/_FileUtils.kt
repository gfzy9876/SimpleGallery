package pers.zy.gallarylib.gallery.tools

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import pers.zy.gallarylib.gallery.model.MediaInfo
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * date: 2020/6/29   time: 12:22 PM
 * author zy
 * Have a nice day :)
 **/

class FileUtils {
    companion object {
        private val SEND_BOX_DIR = GalleryCommon.app.externalCacheDir!!.absolutePath +
                "${File.separatorChar}" +
                "GalleryModel"
        init {
            val file = File(SEND_BOX_DIR)
            if (!file.exists()) {
                file.mkdirs()
            }
        }

        fun createSendBoxFileAndroidQ(mediaInfo: MediaInfo): File {
            val sendBoxFile = File("${SEND_BOX_DIR}${File.separatorChar}cache_file_${mediaInfo.displayName}")
            ioCopyFile({
                GalleryCommon.app.contentResolver.openInputStream(Uri.parse(mediaInfo.contentUriPath))
            }, {
                FileOutputStream(sendBoxFile)
            }, { })
            return sendBoxFile
        }


        /**
         * 保存图片
         * */
        fun savePhoto(srcUri: Uri, parentFileName: String, dstFileName: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePhotosGreaterOrEqualQ(srcUri, parentFileName, dstFileName)
            } else {
                savePhotoBelowQ(srcUri, parentFileName, dstFileName)
            }
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        private fun savePhotosGreaterOrEqualQ(srcUri: Uri, parentFileName: String, dstFileName: String) {
            val contentResolver = GalleryCommon.app.contentResolver
            val dstContentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "${dstFileName}.${getFileExtensionFromUri(srcUri)}")
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeTypeFromUri(srcUri))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separatorChar + parentFileName)
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
            }
            val dstUri = contentResolver.insert(
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                dstContentValues
            ) ?: return

            ioCopyFile({
                contentResolver.openInputStream(srcUri)
            }, {
                contentResolver.openOutputStream(dstUri)
            }, {
                contentResolver.update(dstUri, dstContentValues, null, null)
            })
        }

        private fun savePhotoBelowQ(srcUri: Uri, parentFileName: String, dstFileName: String) {
            val parent = Environment.getExternalStoragePublicDirectory(parentFileName)
            if (!parent.exists()) {
                parent.mkdirs()
            }
            val dstFile = File(parent, "${dstFileName}${getFileExtensionFromUri(srcUri)}")
            if (!dstFile.exists()) {
                dstFile.createNewFile()
            }
            ioCopyFile({
                GalleryCommon.app.contentResolver.openInputStream(srcUri)
            }, {
                FileOutputStream(dstFile)
            }, {
                GalleryCommon.app.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dstFile)))
            })
        }
        //保存图片end


        /**
         * 获取图片后缀
         * */
        @JvmStatic fun getFileExtensionFromUri(uri: Uri): String {
            var extension: String? = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                val mimeTypeMap = MimeTypeMap.getSingleton()
                mimeTypeMap.getExtensionFromMimeType(GalleryCommon.app.contentResolver.getType(uri))
            } else {
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            }
            if (TextUtils.isEmpty(extension)) {
                extension = "jpg"
            }
            return extension!!
        }

        /**
         * 获取图片mime-type
         * */
        @JvmStatic fun getMimeTypeFromPath(path: String): String {
            val singleton = MimeTypeMap.getSingleton()
            val extension = MimeTypeMap.getFileExtensionFromUrl(path)
            var mimeType: String? = null
            if (!TextUtils.isEmpty(extension)) {
                mimeType = singleton.getMimeTypeFromExtension(extension)
            }
            if (TextUtils.isEmpty(mimeType)) {
                mimeType = "image/jpg"
            }
            return mimeType!!
        }

        @JvmStatic fun getMimeTypeFromUri(uri: Uri): String {
            var mimeType: String? = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                GalleryCommon.app.contentResolver.getType(uri)
            } else {
                getMimeTypeFromPath(uri.toString())
            }
            if (TextUtils.isEmpty(mimeType)) {
                mimeType = "image/jpg"
            }
            return mimeType!!
        }


        private fun ioCopyFile(insCall: () -> InputStream?, outsCall: () -> OutputStream?, completeCall: () -> Unit) {
            var ins: InputStream? = null
            var outs: OutputStream? = null
            try {
                ins = insCall()
                outs = outsCall()
                if (ins != null && outs != null) {
                    val bytes = ByteArray(1024)
                    var i: Int
                    while (ins.read(bytes).also { i = it } != -1) {
                        outs.write(bytes, 0, i)
                    }
                }
                completeCall()
            } catch (e: Exception) {
                d("ioCopyFile ${e.fillInStackTrace()}")
            } finally {
                ins?.close()
                outs?.close()
            }
        }
    }
}


