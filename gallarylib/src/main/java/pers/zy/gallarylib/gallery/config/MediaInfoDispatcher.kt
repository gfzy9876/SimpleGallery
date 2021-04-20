package pers.zy.gallarylib.gallery.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import pers.zy.gallarylib.gallery.ui.GalleryMediaLoader
import pers.zy.gallarylib.gallery.ui.list.GalleryMediaListAct

/**
 * date: 4/20/21   time: 10:34 AM
 * author zy
 * Have a nice day :)
 **/
class MediaInfoDispatcher {
    companion object {
        const val REQUEST_CODE_MEDIA_INFO = 1001
        const val RESULT_CODE_MEDIA_INFO = 1000
        const val EXTRA_REQUEST_MEDIA_INFO_CONFIG = "request_extra_media_info_config"
        const val EXTRA_RESULT_MEDIA_INFO = "extra_result_media_info"

        fun newInstance(): MediaInfoDispatcher = MediaInfoDispatcher()

        fun start(context: Context, target: Any, config: MediaInfoConfig) {
            MediaInfoTargetBinding.bind(target)
            val intent = Intent(context, GalleryMediaListAct::class.java).apply {
                putExtra(EXTRA_REQUEST_MEDIA_INFO_CONFIG, config)
            }
            if (context is Activity) {
                context.startActivityForResult(intent, REQUEST_CODE_MEDIA_INFO)
            } else {
                context.startActivity(intent)
            }
        }

        fun start(context: Activity, config: MediaInfoConfig) {
            MediaInfoTargetBinding.bind(context)
            val intent = Intent(context, GalleryMediaListAct::class.java).apply {
                putExtra(EXTRA_REQUEST_MEDIA_INFO_CONFIG, config)
            }
            context.startActivityForResult(intent, REQUEST_CODE_MEDIA_INFO)
        }
    }

    private var config: MediaInfoConfig = MediaInfoConfig()

    fun setMinMediaCount(count: Int): MediaInfoDispatcher {
        config.minMediaCount = count
        return this
    }

    fun setMaxMediaCount(count: Int): MediaInfoDispatcher {
        config.maxMediaCount = count
        return this
    }

    fun ofImage(): MediaInfoDispatcher {
        config.mimeType = GalleryMediaLoader.MIME_TYPE_IMAGE
        return this
    }

    fun ofVideo(): MediaInfoDispatcher {
        config.mimeType = GalleryMediaLoader.MIME_TYPE_VIDEO
        return this
    }

    fun ofMediaAll(): MediaInfoDispatcher {
        config.mimeType = GalleryMediaLoader.MIME_TYPE_ALL
        return this
    }

    fun showCamera(showCamera: Boolean): MediaInfoDispatcher {
        config.showCamera = showCamera
        return this
    }

    fun start(context: Context, target: Any) {
        config.targetName = target::class.java.simpleName
        MediaInfoDispatcher.start(context, target, config)
    }

    fun start(activity: Activity) {
        config.targetName = activity::class.java.simpleName
        MediaInfoDispatcher.start(activity, config)
    }
}