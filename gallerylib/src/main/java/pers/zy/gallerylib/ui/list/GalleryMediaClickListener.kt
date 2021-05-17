package pers.zy.gallerylib.ui.list

import pers.zy.gallerylib.model.MediaInfoWrapper

/**
 * date: 5/13/21   time: 11:12 AM
 * author zy
 * Have a nice day :)
 **/
interface GalleryMediaClickListener {
    fun onSwitchClick(wrapper: MediaInfoWrapper, position: Int)
    fun onMediaItemClick(wrapper: MediaInfoWrapper, position: Int)
}