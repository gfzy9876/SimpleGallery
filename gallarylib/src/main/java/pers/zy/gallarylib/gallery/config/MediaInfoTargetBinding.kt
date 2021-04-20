package pers.zy.gallarylib.gallery.config

import pers.zy.apt_annotation.MediaInfoConstants
import java.lang.ref.WeakReference

/**
 * date: 4/19/21   time: 12:04 PM
 * author zy
 * Have a nice day :)
 **/
class MediaInfoTargetBinding {
    companion object {
        val responseActivityMap = hashMapOf<String, Any>()

        @JvmStatic fun bind(target: Any) {
            val bindClazz = Class.forName(target::class.java.name + MediaInfoConstants.MEDIA_INFO_PROXY)
            val proxy = bindClazz.newInstance()
            val referenceField = bindClazz.getField("reference")
            referenceField.set(proxy, WeakReference(target))
            responseActivityMap[getKey(target)] = proxy
        }

        @JvmStatic fun unbind(target: Any) {
            responseActivityMap.remove(getKey(target))
        }

        private fun getKey(target: Any): String = target::class.java.simpleName
    }
}