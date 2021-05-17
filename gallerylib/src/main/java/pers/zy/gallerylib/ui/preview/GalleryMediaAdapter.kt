package pers.zy.gallerylib.ui.preview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import pers.zy.gallerylib.model.MediaInfoWrapper

/**
 * date: 5/17/21   time: 10:58 AM
 * author zy
 * Have a nice day :)
 **/
class GalleryMediaAdapter(act: FragmentActivity, private val mediaList: List<MediaInfoWrapper>) : FragmentStateAdapter(act) {
    override fun getItemCount(): Int {
        return mediaList.size
    }

    override fun createFragment(position: Int): Fragment {
        return GalleryMediaPreviewFrag.newInstance(mediaList[position].mediaInfo)
    }
}