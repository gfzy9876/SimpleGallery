package pers.zy.gallarylib.gallery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import pers.zy.gallarylib.databinding.ActivityMediaDetailsBinding
import pers.zy.gallarylib.getStatsBarHeight
import pers.zy.gallerymodel.gallery.model.MediaImageInfo

class MediaDetailsActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MEDIA_INFO = "extra_media_info"

        fun start(context: Context, mediaInfo: MediaImageInfo) {
            context.startActivity(Intent(context, MediaDetailsActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_INFO, mediaInfo)
            })
        }
    }


    private lateinit var binding: ActivityMediaDetailsBinding
    private lateinit var mediaImageInfo: MediaImageInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaImageInfo = intent.getParcelableExtra(EXTRA_MEDIA_INFO)
        initTitleBar()
        Glide.with(this)
            .load(mediaImageInfo.path)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.ivMediaImage)
    }

    private fun initTitleBar() {
        val statsBarHeight = getStatsBarHeight(this@MediaDetailsActivity)
        binding.flTitleBar.layoutParams = (binding.flTitleBar.layoutParams as LinearLayout.LayoutParams).apply {
            height += statsBarHeight
        }
        binding.flTitleBar.setPadding(0, statsBarHeight, 0, 0)
        binding.tvTitleBar.text = mediaImageInfo.displayName
    }
}