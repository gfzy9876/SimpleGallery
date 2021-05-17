package pers.zy.gallerylib.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pers.zy.gallerylib.events.GalleryMediaInfoFinishEvent

/**
 * date: 5/17/21   time: 6:03 PM
 * author zy
 * Have a nice day :)
 **/
abstract class BaseGalleryMediaAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleGalleryMediaInfoFinishEvent(event: GalleryMediaInfoFinishEvent) {
        finish()
    }
}