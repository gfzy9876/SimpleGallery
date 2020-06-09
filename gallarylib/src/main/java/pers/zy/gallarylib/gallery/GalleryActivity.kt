package pers.zy.gallarylib.gallery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import pers.zy.gallarylib.R
import pers.zy.gallarylib.databinding.ActivityGallaryBinding
import pers.zy.gallerymodel.gallery.model.MediaImageInfo
import java.lang.RuntimeException
import java.lang.ref.WeakReference

class GalleryActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, GalleryActivity::class.java))
        }
    }

    private lateinit var viewBinding: ActivityGallaryBinding
    private lateinit var mediaHandler: MediaHandler

    private val mediaList = mutableListOf<Any>()
    private val adapter = MultiTypeAdapter(mediaList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.trans_from_bottom_enter_anim, 0)
        viewBinding = ActivityGallaryBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        mediaHandler = MediaHandler(WeakReference(this))
        initRv()
        GalleryLoader.getInstance(this).loadImage({
            mediaList.addAll(it)
            adapter.notifyDataSetChanged()
        }, {
            throw RuntimeException(it)
        })
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.trans_from_bottom_exit_anim)
    }

    private fun initRv() {
        this@GalleryActivity.adapter.register(MediaImageInfo::class, MediaImageViewBinder())
        viewBinding.rvGallery.apply {
            adapter = this@GalleryActivity.adapter
            layoutManager = GridLayoutManager(this@GalleryActivity, 4)
        }
    }

    private fun handleMedia(list: List<*>) {
        for (mediaInfo in list) {
            mediaInfo?.let { mediaInfo ->
                mediaList.add(mediaInfo)
            }
        }

        d("thread: ${Thread.currentThread().name}, size: ${mediaList.size}")
        adapter.notifyDataSetChanged()
    }

    private class MediaHandler(private val activityReference: WeakReference<GalleryActivity>)
        : Handler(Looper.getMainLooper()) {

        companion object {
            const val MSG_OBTAIN_MEDIA = 0x01
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_OBTAIN_MEDIA -> {
                    activityReference.get()?.handleMedia(msg.obj as List<*>)
                }
            }
        }
    }
}

fun d(msg: String) {
    Log.d("ZZZ", msg)
}