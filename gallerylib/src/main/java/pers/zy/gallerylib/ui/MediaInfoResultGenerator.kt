package pers.zy.gallerylib.ui

import android.app.Activity
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import pers.zy.gallerylib.config.MediaInfoConfig
import pers.zy.gallerylib.events.GalleryMediaInfoFinishEvent
import pers.zy.gallerylib.model.MediaInfo
import pers.zy.gallerylib.tools.GalleryFileUtils
import pers.zy.gallerylib.tools.GalleryCommon

/**
 * date: 5/17/21   time: 5:47 PM
 * author zy
 * Have a nice day :)
 **/
internal class MediaInfoResultGenerator {
    companion object {
        fun generateMediaInfoResult(activity: Activity, result: ArrayList<MediaInfo>) {
            if (GalleryCommon.lessThanAndroidQ()) {
                setMediaInfoResultAndFinish(activity, result)
            } else {
                createSendBoxFile(activity, result)
            }
        }

        private fun setMediaInfoResultAndFinish(activity: Activity, result: ArrayList<MediaInfo>) {
            val invoked = MediaInfoTargetBinding.invokeProxy(MediaInfoConfig.targetName, result)
            if (!invoked) {
                activity.setResult(MediaInfoDispatcher.RESULT_CODE_MEDIA_INFO, Intent().apply {
                    putParcelableArrayListExtra(MediaInfoDispatcher.EXTRA_RESULT_MEDIA_INFO, result)
                })
            }
            EventBus.getDefault().post(GalleryMediaInfoFinishEvent())
        }

        private fun createSendBoxFile(activity: Activity, result: ArrayList<MediaInfo>) {
            activity as CoroutineScope
            activity.launch(activity.coroutineContext) {
                withContext(coroutineContext + Dispatchers.IO) {
                    result.forEach { mediaInfo ->
                        GalleryFileUtils.generateSendBoxFile(mediaInfo) {
                            mediaInfo.sendBoxPath = it
                        }
                    }
                }
                setMediaInfoResultAndFinish(activity, result)
            }
        }
    }
}