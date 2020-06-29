package pers.zy.gallarylib.gallery.commons

import android.os.Environment
import java.io.File

/**
 * date: 2020/6/29   time: 12:22 PM
 * author zy
 * Have a nice day :)
 **/

val GALLERY_ROOT_DIR = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}GalleryModel"
val THUMBNAIL_IMAGE_DIR = "${GALLERY_ROOT_DIR}${File.separator}thumbnail"

