package pers.zy.gallarylib.gallery.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import pers.zy.gallarylib.databinding.ActivityMediaPreviewBinding
import pers.zy.gallarylib.gallery.tools.GalleryCommon
import pers.zy.gallarylib.gallery.model.MediaInfo

class MediaPreviewActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MEDIA_INFO = "extra_media_info"

        fun start(context: Context, mediaInfo: MediaInfo) {
            context.startActivity(Intent(context, MediaPreviewActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_INFO, mediaInfo)
            })
        }
    }

    private lateinit var binding: ActivityMediaPreviewBinding
    private lateinit var mediaImageInfo: MediaInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaImageInfo = intent.getParcelableExtra(EXTRA_MEDIA_INFO)!!
        initTitleBar()
        Glide.with(this)
            .load(mediaImageInfo.contentUriPath)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.ivMediaImage)
    }

    private fun initTitleBar() {
        val statsBarHeight = GalleryCommon.getStatsBarHeight()
        binding.titleBar.layoutParams = (binding.titleBar.layoutParams as LinearLayout.LayoutParams).apply {
            height += statsBarHeight
        }
        binding.titleBar.setPadding(0, statsBarHeight, 0, 0)
        binding.titleBar.setTitle(mediaImageInfo.displayName)
        binding.titleBar.setIconClickListener(View.OnClickListener { finish() })
    }
}