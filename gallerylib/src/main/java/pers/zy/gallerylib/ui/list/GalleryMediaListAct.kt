package pers.zy.gallerylib.ui.list

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.coroutines.*
import pers.zy.gallerylib.R
import pers.zy.gallerylib.databinding.ActGalleryListBinding
import pers.zy.gallerylib.tools.GalleryCommon
import pers.zy.gallerylib.ui.GalleryMediaLoader
import pers.zy.gallerylib.model.*
import pers.zy.gallerylib.config.MediaInfoConfig
import pers.zy.gallerylib.tools.dp
import pers.zy.gallerylib.tools.e
import pers.zy.gallerylib.ui.BaseGalleryMediaAct
import pers.zy.gallerylib.ui.MediaInfoResultGenerator
import pers.zy.gallerylib.ui.common.EndlessRecyclerViewScrollListener
import pers.zy.gallerylib.ui.preview.GalleryMediaPreviewAct
import pers.zy.gallerylib.ui.adapter.*
import pers.zy.gallerylib.ui.adapter.BaseMediaViewBinder
import pers.zy.gallerylib.ui.adapter.MediaImageViewBinder
import pers.zy.gallerylib.ui.adapter.MediaVideoViewBinder

class GalleryMediaListAct : BaseGalleryMediaAct(), GalleryMediaClickListener, CoroutineScope by MainScope() {

    private val wrapperList = mutableListOf<Any>()
    private val selectedWrapperList = mutableListOf<MediaInfoWrapper>()
    private val bucketList = mutableListOf<BucketInfo>()
    private val cameraItem = CameraItem()
    private var selectBucketId: Long = GalleryMediaLoader.BUCKET_ID_NON_SELECTIVE

    private lateinit var binding: ActGalleryListBinding
    private val mediaAdapter = MultiTypeAdapter(wrapperList)
    private val bucketAdapter = MultiTypeAdapter(bucketList)
    private lateinit var mediaLayoutManager: GridLayoutManager

    private lateinit var galleryMediaLoader: GalleryMediaLoader

    private val bucketEnterAnim: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.flBucket, View.TRANSLATION_Y, -binding.flBucket.height.toFloat(), 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    binding.flBucket.visibility = View.VISIBLE
                }
            })
        }
    }
    private val bucketExitAnim: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.flBucket, View.TRANSLATION_Y, binding.flBucket.translationY, -binding.flBucket.height.toFloat()).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.trans_from_bottom_enter_anim, 0)
        this.binding = ActGalleryListBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
        initMediaLoader()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GalleryMediaPreviewAct.REQUEST_CODE_SHOW_MEDIA_PREVIEW
            && resultCode == GalleryMediaPreviewAct.RESULT_CODE_SHOW_MEDIA_PREVIEW) {
            val resultSelectMediaInfoList = GalleryMediaPreviewAct.getSelectedMediaInfoList(data)
            selectedWrapperList.clear()
            wrapperList.filterIsInstance<MediaInfoWrapper>().forEach {
                if (resultSelectMediaInfoList.contains(it.mediaInfo)) {
                    it.selected = true
                    selectedWrapperList.add(it)
                } else {
                    it.selected = false
                }
            }
            mediaAdapter.notifyDataSetChanged()
            updateTvSelectOkayUI()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.trans_from_bottom_exit_anim)
    }

    override fun onDestroy() {
        super.onDestroy()
        bucketEnterAnim.removeAllListeners()
        bucketEnterAnim.end()
        bucketExitAnim.end()
        cancel()
    }

    override fun onSwitchClick(wrapper: MediaInfoWrapper, position: Int) {
        checkSelectMediaItem(wrapper, position)
    }

    override fun onMediaItemClick(wrapper: MediaInfoWrapper, position: Int) {
        if (MediaInfoConfig.clickPreview) {
            val filterMediaInfoList = wrapperList.filterIsInstance<MediaInfoWrapper>().map { it.mediaInfo }
            val currentSelectedPosition = filterMediaInfoList.indexOf(wrapper.mediaInfo)
            GalleryMediaPreviewAct.start(this,
                currentSelectedPosition,
                filterMediaInfoList,
                ArrayList(selectedWrapperList.map { it.mediaInfo })
            )
        } else {
            checkSelectMediaItem(wrapper, position)
        }
    }

    private fun checkSelectMediaItem(wrapper: MediaInfoWrapper, position: Int) {
        if (selectedWrapperList.size >= MediaInfoConfig.maxMediaCount && !wrapper.selected) {
            GalleryCommon.makeToast("最多选择${MediaInfoConfig.maxMediaCount}个文件")
            return
        }
        wrapper.selected = !wrapper.selected
        if (wrapper.selected) {
            selectedWrapperList.add(wrapper)
        } else {
            selectedWrapperList.remove(wrapper)
        }
        mediaAdapter.notifyItemChanged(position, BaseMediaViewBinder.PAYLOADS_UPDATE_SELECTED_INDEX_WITH_ANIM)
        val firstVisibleItemPosition = mediaLayoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = mediaLayoutManager.findLastVisibleItemPosition()
        selectedWrapperList.forEach {
            val index = wrapperList.indexOf(it)
            if (index in firstVisibleItemPosition .. lastVisibleItemPosition) {
                mediaAdapter.notifyItemChanged(index, BaseMediaViewBinder.PAYLOADS_UPDATE_SELECTED_INDEX)
            }
        }
        updateTvSelectOkayUI()
    }

    private fun updateTvSelectOkayUI() {
        binding.tvSelectOkay.apply {
            if (selectedWrapperList.isEmpty()) {
                isClickable = false
                setBackgroundResource(R.drawable.shape_rc_unselect_media)
            } else {
                isClickable = true
                setBackgroundResource(R.drawable.shape_rc_select_media)
            }
        }
    }

    private fun initView() {
        binding.titleBar.layoutParams = (binding.titleBar.layoutParams as LinearLayout.LayoutParams).apply {
            height += GalleryCommon.getStatsBarHeight()
        }
        binding.titleBar.setPadding(0, GalleryCommon.getStatsBarHeight(), 0, 0)

        mediaAdapter.register(ImageMediaInfoWrapper::class, MediaImageViewBinder(selectedWrapperList, this))
        mediaAdapter.register(VideoMediaInfoWrapper::class, MediaVideoViewBinder(selectedWrapperList, this))
        mediaAdapter.register(CameraItem::class, CameraViewBinder(::showCamera))
        mediaLayoutManager = GridLayoutManager(this@GalleryMediaListAct, MediaInfoConfig.columnCount)
        binding.rvMedia.apply {
            adapter = mediaAdapter
            layoutManager = mediaLayoutManager
            if (MediaInfoConfig.pagingLoad) {
                addOnScrollListener(object : EndlessRecyclerViewScrollListener(mediaLayoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        loadMoreMedia(page)
                    }
                })
            }
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val padding = 5f.dp
                    outRect.left = padding / 2
                    outRect.top = padding / 2
                    outRect.right = padding / 2
                    outRect.bottom = padding / 2
                }
            })
        }
        bucketAdapter.register(BucketInfo::class.java, BucketBinder { bucketInfo ->
            refreshBucketSelectorState(false)
            if (selectBucketId != bucketInfo.id) {
                binding.root.postDelayed({
                    binding.tvBucketSelector.text = bucketInfo.displayName
                    selectBucketId = bucketInfo.id
                    galleryMediaLoader.loadMedia(MediaInfoConfig.selectMimeType, selectBucketId, successCall = {
                        refreshMedia(it)
                    })
                }, 300)
                bucketExitAnim.start()
            }
        })
        binding.rvBucket.apply {
            adapter = this@GalleryMediaListAct.bucketAdapter
            layoutManager = LinearLayoutManager(this@GalleryMediaListAct, LinearLayoutManager.VERTICAL, false)
        }

        initListener()
    }

    private fun initListener() {
        binding.llBucketSelector.setOnClickListener {
            if (binding.llBucketSelector.tag == true) {
                refreshBucketSelectorState(false)
            } else {
                refreshBucketSelectorState(true)
            }
        }
        binding.flBucket.setOnClickListener {
            binding.llBucketSelector.tag = false
            binding.flBucketArrow.animate().rotation(180f).setDuration(300).start()
            bucketExitAnim.start()
        }

        binding.rvBucket.maxRecyclerViewHeight = (GalleryCommon.getScreenHeight() * 0.5f).toInt()

        binding.tvSelectOkay.setOnClickListener {
            val result = ArrayList(selectedWrapperList.map {
                it.mediaInfo
            })
            binding.includeProgress.flProgress.visibility = View.VISIBLE
            MediaInfoResultGenerator.generateMediaInfoResult(this, result)
        }
    }

    private fun initMediaLoader() {
        galleryMediaLoader = GalleryMediaLoader(this)
        RxPermissions(this).request(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
                    loadMedia()
                } else {
                    GalleryCommon.makeToast("请开启相关权限~")
                    finish()
                }
            }
    }

    private fun showCamera() {
        //TODO:ZY showCamera
    }

    private fun loadMedia() {
        galleryMediaLoader.loadMedia(MediaInfoConfig.selectMimeType, selectBucketId, successCall = {
            refreshMedia(it)
        })
        galleryMediaLoader.loadBucket(MediaInfoConfig.selectMimeType, {
            if (it.isNotEmpty()) {
                bucketList.addAll(it)
                bucketAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun refreshBucketSelectorState(show: Boolean) {
        if (binding.llBucketSelector.tag != show) {
            binding.llBucketSelector.tag = show
            if (show) {
                binding.flBucketArrow.animate().rotation(360f).setDuration(300).start()
                bucketEnterAnim.start()
            } else {
                binding.flBucketArrow.animate().rotation(180f).setDuration(300).start()
                bucketExitAnim.start()
            }
        }
    }

    private fun refreshMedia(result: List<MediaInfo>) {
        wrapperList.clear()
        addResult(result)
        mediaAdapter.notifyDataSetChanged()
    }

    private fun loadMoreMedia(page: Int) {
        galleryMediaLoader.loadMedia(MediaInfoConfig.selectMimeType, selectBucketId, page, successCall = {
            val oldSize = wrapperList.size
            addResult(it)
            if (wrapperList.size != oldSize) {
                mediaAdapter.notifyItemRangeInserted(oldSize, wrapperList.size - oldSize)
            }
        })
    }

    private fun addResult(result: List<MediaInfo>) {
        if (result.isEmpty()) return
        val wrapperResult = mutableListOf<MediaInfoWrapper>()
        result.forEach { mediaInfo ->
            mediaInfo.createMediaInfoWrapper()?.let {
                wrapperResult.add(it)
                if (selectedWrapperList.contains(it)) {
                    it.selected = true
                }
            }
        }
        wrapperList.addAll(wrapperResult)
        e("addResult ${wrapperList.size}")
        addCameraItemTopIfNeed()
    }

    private fun addCameraItemTopIfNeed() {
        wrapperList.remove(cameraItem)
        if (MediaInfoConfig.showCamera) {
            wrapperList.add(0, cameraItem)
        }
    }
}