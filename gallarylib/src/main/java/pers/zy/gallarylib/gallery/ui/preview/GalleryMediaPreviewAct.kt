package pers.zy.gallarylib.gallery.ui.preview

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import pers.zy.gallarylib.databinding.ActGallaryPreviewBinding
import pers.zy.gallarylib.gallery.tools.GalleryCommon
import pers.zy.gallarylib.gallery.model.MediaInfo
import pers.zy.gallarylib.gallery.ui.GalleryMediaLoader

class GalleryMediaPreviewAct : AppCompatActivity() {

    companion object {
        private const val EXTRA_SELECTED_POSITION = "extra_selected_position"
        private const val EXTRA_TOTAL_MEDIA_SIZE = "extra_total_media_size"
        private const val EXTRA_INIT_MEDIA_LIST = "extra_init_media_list"
        private const val EXTRA_BUCKET_ID = "extra_bucket_id"

        fun start(
            context: Context,
            selectedPosition: Int,
            totalMediaSize: Int,
            initMediaList: ArrayList<MediaInfo>,
            bucketId: Long
        ) {
            context.startActivity(Intent(context, GalleryMediaPreviewAct::class.java).apply {
                putExtra(EXTRA_SELECTED_POSITION, selectedPosition)
                putExtra(EXTRA_TOTAL_MEDIA_SIZE, totalMediaSize)
                putExtra(EXTRA_INIT_MEDIA_LIST, initMediaList)
                putExtra(EXTRA_BUCKET_ID, bucketId)
            })
        }
    }

    private lateinit var binding: ActGallaryPreviewBinding
    private var selectedPosition = 0
    private var totalMediaSize = 0
    private val mediaList = mutableListOf<MediaInfo>()
    private var bucketId = GalleryMediaLoader.BUCKET_ID_NON_SELECTIVE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActGallaryPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    private fun initData() {
        intent.let {
            selectedPosition = it.getIntExtra(EXTRA_SELECTED_POSITION, 0)
            totalMediaSize = it.getIntExtra(EXTRA_TOTAL_MEDIA_SIZE, 0)
            mediaList.addAll(it.getParcelableArrayListExtra(EXTRA_INIT_MEDIA_LIST)!!)
            bucketId = it.getLongExtra(EXTRA_BUCKET_ID, 0L)
        }
        if (mediaList.isEmpty() || totalMediaSize == 0) {
            finish()
        }
    }

    private fun initView() {
        val statsBarHeight = GalleryCommon.getStatsBarHeight()
        binding.flTitleBar.layoutParams = (binding.flTitleBar.layoutParams as LinearLayout.LayoutParams).apply {
            height += statsBarHeight
        }
        binding.flTitleBar.setPadding(0, statsBarHeight, 0, 0)
        binding.ivLeftIcon.setOnClickListener { finish() }
        updateTitle()
        Glide.with(this)
            .load(mediaList[selectedPosition].contentUriPath)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.ivMediaImage)
    }

    private fun updateTitle() {
        binding.title = "${selectedPosition + 1}/${totalMediaSize}"
    }
}