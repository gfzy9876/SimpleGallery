package pers.zy.gallerylib.ui.preview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import pers.zy.gallerylib.R
import pers.zy.gallerylib.config.MediaInfoConfig
import pers.zy.gallerylib.databinding.ActGalleryPreviewBinding
import pers.zy.gallerylib.tools.GalleryCommon
import pers.zy.gallerylib.model.MediaInfo
import pers.zy.gallerylib.model.MediaInfoWrapper
import pers.zy.gallerylib.ui.BaseGalleryMediaAct
import pers.zy.gallerylib.ui.MediaInfoResultGenerator

class GalleryMediaPreviewAct : BaseGalleryMediaAct(), CoroutineScope by MainScope() {

    companion object {
        const val RESULT_CODE_SHOW_MEDIA_PREVIEW = 9000
        const val REQUEST_CODE_SHOW_MEDIA_PREVIEW = 10000
        private const val EXTRA_CURRENT_SHOW_POSITION = "extra_current_show_position"
        private const val EXTRA_SELECTED_MEDIA_INFO_LIST = "extra_selected_media_info_list"
        @JvmStatic val sharedMediaInfoList = mutableListOf<MediaInfo>() //TODO:ZY 看下是否有更好的办法传递mediaInfo数据

        fun start(context: AppCompatActivity, currentShowPosition: Int, mediaInfoList: List<MediaInfo>, selectedMediaInfoList: ArrayList<MediaInfo>) {
            sharedMediaInfoList.clear()
            sharedMediaInfoList.addAll(mediaInfoList)
            context.startActivityForResult(Intent(context, GalleryMediaPreviewAct::class.java).apply {
                putExtra(EXTRA_CURRENT_SHOW_POSITION, currentShowPosition)
                putExtra(EXTRA_SELECTED_MEDIA_INFO_LIST, selectedMediaInfoList)
            }, REQUEST_CODE_SHOW_MEDIA_PREVIEW)
        }

        fun getSelectedMediaInfoList(data: Intent?): List<MediaInfo> {
            return data?.getParcelableArrayListExtra(EXTRA_SELECTED_MEDIA_INFO_LIST) ?: mutableListOf()
        }
    }

    private lateinit var binding: ActGalleryPreviewBinding
    private val mediaWrapperList = mutableListOf<MediaInfoWrapper>()
    private var currentShowPosition = 0
        set(value) {
            field = value
            binding.title = "${value + 1}/${mediaWrapperList.size}"
            updateIvSelectUI(mediaWrapperList[currentShowPosition])
        }
    private val selectedWrapperList = ArrayList<MediaInfoWrapper>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActGalleryPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    override fun onBackPressed() {
        setResult(RESULT_CODE_SHOW_MEDIA_PREVIEW, Intent().apply {
            putExtra(EXTRA_SELECTED_MEDIA_INFO_LIST, ArrayList(selectedWrapperList.map { it.mediaInfo }))
        })
        finish()
    }

    private fun initData() {
        selectedWrapperList.addAll(intent.getParcelableArrayListExtra<MediaInfo>(EXTRA_SELECTED_MEDIA_INFO_LIST)!!.map {
            it.createMediaInfoWrapper()!!
        })
        mediaWrapperList.addAll(sharedMediaInfoList.mapIndexed { index, mediaInfo ->
            mediaInfo.createMediaInfoWrapper()!!.apply {
                selected = selectedWrapperList.contains(mediaInfo.createMediaInfoWrapper())
            }
        })
        sharedMediaInfoList.clear()
        currentShowPosition = intent.getIntExtra(EXTRA_CURRENT_SHOW_POSITION, 0)
        if (mediaWrapperList.isEmpty()) {
            finish()
        }
    }

    private val adapter = GalleryMediaAdapter(this, mediaWrapperList)

    private fun initView() {
        val statsBarHeight = GalleryCommon.getStatsBarHeight()
        binding.flTitleBar.layoutParams = (binding.flTitleBar.layoutParams as ViewGroup.MarginLayoutParams).apply {
            height += statsBarHeight
        }
        binding.flTitleBar.setPadding(0, statsBarHeight, 0, 0)
        binding.ivLeftIcon.setOnClickListener { finish() }

        binding.vpPreview.adapter = adapter
        binding.vpPreview.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }

            override fun onPageSelected(position: Int) {
                currentShowPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) { }
        })
        binding.vpPreview.offscreenPageLimit = 2
        binding.vpPreview.setCurrentItem(currentShowPosition, false)

        binding.llSelect.setOnClickListener {
            val currentWrapper = mediaWrapperList[currentShowPosition]
            if (!currentWrapper.selected) {
                if (selectedWrapperList.size < MediaInfoConfig.maxMediaCount) {
                    currentWrapper.selected = true
                    selectedWrapperList.add(currentWrapper)
                } else {
                    GalleryCommon.makeToast("最多选择${MediaInfoConfig.maxMediaCount}个文件")
                }
            } else {
                currentWrapper.selected = false
                selectedWrapperList.remove(currentWrapper)
            }
            updateIvSelectUI(currentWrapper)
        }

        binding.tvSelect.setOnClickListener {
            binding.includeProgress.flProgress.visibility = View.VISIBLE
            val currentShowWrapper = mediaWrapperList[currentShowPosition]
            if (!selectedWrapperList.contains(currentShowWrapper)) {
                selectedWrapperList.add(currentShowWrapper)
            }
            MediaInfoResultGenerator.generateMediaInfoResult(
                this,
                ArrayList(selectedWrapperList.map { it.mediaInfo })
            )
        }
    }

    private fun updateIvSelectUI(currentWrapper: MediaInfoWrapper) {
        if (currentWrapper.selected) {
            binding.ivSelect.setImageResource(R.drawable.selected)
        } else {
            binding.ivSelect.setImageResource(R.drawable.shape_oval_c9_stroke1)
        }
    }
}