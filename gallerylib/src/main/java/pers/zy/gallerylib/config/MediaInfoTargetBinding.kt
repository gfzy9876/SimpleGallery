package pers.zy.gallerylib.config

import pers.zy.apt_annotation.MediaInfoConstants
import pers.zy.gallerylib.model.MediaInfo
import java.lang.ref.WeakReference

/**
 * date: 4/19/21   time: 12:04 PM
 * author zy
 * Have a nice day :)
 **/
class MediaInfoTargetBinding {
    companion object {
        @JvmStatic val responseActivityMap = hashMapOf<String, Any>()

        @JvmStatic fun bind(target: Any) {
            val bindClazz = Class.forName(target::class.java.name + MediaInfoConstants.MEDIA_INFO_PROXY)
            val proxy = bindClazz.newInstance()
            val referenceField = bindClazz.getField("reference")
            referenceField.set(proxy, WeakReference(target))
            responseActivityMap[getKey(target)] = proxy
        }

        @JvmStatic fun invokeProxy(targetName: String, result: ArrayList<MediaInfo>): Boolean {
            val proxy = responseActivityMap[targetName]
            return if (proxy != null) {
                val bindClazz = Class.forName(proxy::class.java.name)
                val onMediaInfoReceivedMethod = bindClazz.getMethod("onMediaInfoReceived", List::class.java)
                onMediaInfoReceivedMethod.invoke(proxy, result)
                unbind(targetName)
                true
            } else {
                false
            }
        }

        @JvmStatic fun unbind(target: Any) {
            responseActivityMap.remove(getKey(target))
        }

        @JvmStatic fun getKey(target: Any): String = target::class.java.simpleName
    }
}