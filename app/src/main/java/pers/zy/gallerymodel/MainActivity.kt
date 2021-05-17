package pers.zy.gallerymodel

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import pers.zy.apt_annotation.MediaInfoReceived
import pers.zy.gallerylib.ui.MediaInfoDispatcher
import pers.zy.gallerylib.model.MediaInfo
import pers.zy.gallerylib.tools.d

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == GalleryMediaListAct.REQUEST_CODE_MEDIA_INFO) {
//            val result = data?.getParcelableArrayListExtra<MediaInfo>(GalleryMediaListAct.EXTRA_RESULT_MEDIA_INFO) ?: return
//            val gson = GsonBuilder().serializeNulls().create()
//            result.forEach {
//                d("onActivityResult: ${gson.toJson(it)}")
//            }
//        }
//    }

    fun loadImage(view: View) {
        MediaInfoDispatcher.newInstance()
                .ofImage()
                .start(this)
    }

    fun loadVideo(view: View) {
        MediaInfoDispatcher.newInstance()
                .ofVideo()
                .start(this)
    }

    fun loadAll(view: View) {
        MediaInfoDispatcher.newInstance()
                .ofMediaAll()
                .start(this)
    }

    @MediaInfoReceived
    fun onMediaInfoReceived(asd: List<MediaInfo>) {
        val gson = Gson()
        asd.forEach {
            d("onMediaInfoReceived ${gson.toJson(it)}")
        }
        Glide.with(this)
            .load(asd[0].realPath)
            .into(iv_test)
    }
}