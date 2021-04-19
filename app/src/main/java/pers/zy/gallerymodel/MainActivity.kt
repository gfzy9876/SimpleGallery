package pers.zy.gallerymodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.gson.GsonBuilder
import pers.zy.gallarylib.gallery.tools.d
import pers.zy.gallarylib.gallery.model.MediaInfo
import pers.zy.gallarylib.gallery.ui.list.GalleryMediaListAct

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GalleryMediaListAct.REQUEST_CODE_MEDIA_INFO) {
            val result = data?.getParcelableArrayListExtra<MediaInfo>(GalleryMediaListAct.EXTRA_RESULT_MEDIA_INFO) ?: return
            val gson = GsonBuilder().serializeNulls().create()
            result.forEach {
                d("onActivityResult: ${gson.toJson(it)}")
            }
        }
    }

    fun loadImage(view: View) {
        GalleryMediaListAct.startShowImage(this)
    }

    fun loadVideo(view: View) {
        GalleryMediaListAct.startShowVideo(this)
    }

    fun loadAll(view: View) {
        GalleryMediaListAct.startShowAll(this)
    }

    fun onMediaInfoReceived() {

    }
}