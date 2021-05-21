package pers.zy.gallerylib.tools

import android.content.ContentResolver
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import pers.zy.gallerylib.model.MediaInfo
import java.io.*

/**
 * date: 2020/6/29   time: 12:22 PM
 * author zy
 * Have a nice day :)
 **/

class GalleryFileUtils {
    companion object {
        private val SEND_BOX_DIR = GalleryCommon.app.externalCacheDir!!.absolutePath +
                "${File.separatorChar}" +
                "GalleryModel"
        init {
            val dir = File(SEND_BOX_DIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }

        internal fun generateSendBoxFile(mediaInfo: MediaInfo, sendBoxFilePathCall: (String) -> Unit) {
            val dir = File(SEND_BOX_DIR)
            val sourceFile = File(mediaInfo.realPath)
            val sourceFileLength = sourceFile.length()
            var findSameFile = false

            dir.listFiles(FileFilterUtils.suffixFileFilter(
                getFileExtensionFromUri(Uri.parse(mediaInfo.realPath))) as FilenameFilter
            )?.forEach {
                if (sourceFileLength == it.length()
                    && sourceFile.name == it.name
                ) {
                    sendBoxFilePathCall(it.absolutePath)
                    findSameFile = true
                }
            }
            if (!findSameFile) {
                val sendBoxFile = File("${SEND_BOX_DIR}${File.separatorChar}${mediaInfo.displayName}")
                FileUtils.copyFile(sourceFile, sendBoxFile)
                sendBoxFilePathCall(sendBoxFile.absolutePath)
            }
        }

        /**
         * 获取图片后缀
         * */
        internal fun getFileExtensionFromUri(uri: Uri): String {
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
        internal fun getMimeTypeFromPath(path: String): String {
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

        internal fun getMimeTypeFromUri(uri: Uri): String {
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
    }
}


