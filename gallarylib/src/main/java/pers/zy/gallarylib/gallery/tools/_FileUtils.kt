package pers.zy.gallarylib.gallery.tools

import android.net.Uri
import pers.zy.gallarylib.gallery.model.MediaInfo
import java.io.File
import java.io.FileOutputStream

/**
 * date: 2020/6/29   time: 12:22 PM
 * author zy
 * Have a nice day :)
 **/

object FileUtils {

    val SEND_BOX_DIR = GallaryCommon.app.externalCacheDir!!.absolutePath +
            "${File.separatorChar}" +
            "GalleryModel"

    init {
        val file = File(SEND_BOX_DIR)
        if (!file.exists()) {
            file.mkdir()
        }
    }

    fun createSendBoxFileAndroidQ(mediaInfo: MediaInfo): File {
        val sendBoxFile = File(getSendBoxFilePath(mediaInfo))
        copyFileAndroidQ(Uri.parse(mediaInfo.contentUriPath), sendBoxFile)
        return sendBoxFile
    }

    private fun copyFileAndroidQ(sourceUri: Uri, destFile: File) {
        val desc = GallaryCommon.app.contentResolver.openAssetFileDescriptor(sourceUri, "r")
        val ins = desc!!.createInputStream()
        val outs = FileOutputStream(destFile)
        var i: Int
        val ba = ByteArray(1024)
        kotlin.run {
            while (true) {
                i = ins.read(ba)
                if (i == -1) {
                    return@run
                }
                outs.write(ba, 0, i)
            }
        }
        ins?.close()
        outs.close()
    }

    private fun getSendBoxFilePath(mediaInfo: MediaInfo): String {
        val suffix = getFileSuffix(mediaInfo.mimeType)
        return "${SEND_BOX_DIR}${File.separatorChar}FILE_${mediaInfo.id}${suffix}"
    }

    private fun getFileSuffix(mimeType: String): String {
        val defaultSuffix = ".png"
        val index = mimeType.indexOf("/")
        return if (index == -1) {
            defaultSuffix
        } else {
            try {
                "." + mimeType.substring(index + 1)
            } catch (e: Exception) {
                defaultSuffix
            }
        }
    }
}


