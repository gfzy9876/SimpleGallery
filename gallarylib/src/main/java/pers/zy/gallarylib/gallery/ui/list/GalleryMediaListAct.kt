package pers.zy.gallarylib.gallery.ui.list

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
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
import pers.zy.gallarylib.R
import pers.zy.gallarylib.databinding.ActivityGallaryBinding
import pers.zy.gallarylib.gallery.tools.GallaryCommon
import pers.zy.gallarylib.gallery.tools.FileUtils
import pers.zy.gallarylib.gallery.ui.GalleryMediaLoader
import pers.zy.gallarylib.gallery.model.*
import pers.zy.gallarylib.gallery.config.MediaInfoTargetBinding
import pers.zy.gallarylib.gallery.config.MediaInfoConfig
import pers.zy.gallarylib.gallery.config.MediaInfoDispatcher
import pers.zy.gallarylib.gallery.ui.EndlessRecyclerViewScrollListener
import pers.zy.gallarylib.gallery.ui.adapter.BaseMediaViewBinder
import pers.zy.gallarylib.gallery.ui.adapter.BucketBinder
import pers.zy.gallarylib.gallery.ui.adapter.MediaImageViewBinder
import pers.zy.gallarylib.gallery.ui.adapter.MediaVideoViewBinder

class GalleryMediaListAct : AppCompatActivity(), CoroutineScope by MainScope() {

//    private lateinit var fromContextName: String
//    private var mimeType: Int = GalleryMediaLoader.MIME_TYPE_ALL
    private lateinit var config: MediaInfoConfig

    private val wrapperList = mutableListOf<MediaInfoWrapper>()
    private val selectedWrapperList = mutableListOf<MediaInfoWrapper>()
    private val bucketList = mutableListOf<BucketInfo>()

    private lateinit var binding: ActivityGallaryBinding
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
        this.binding = ActivityGallaryBinding.inflate(LayoutInflater.from(this))
        this.config = intent.getParcelableExtra(MediaInfoDispatcher.EXTRA_REQUEST_MEDIA_INFO_CONFIG)
        setContentView(binding.root)
        initView()
        initMediaLoader()
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

    private fun initView() {
        binding.titleBar.layoutParams = (binding.titleBar.layoutParams as LinearLayout.LayoutParams).apply {
            height += GallaryCommon.getStatsBarHeight()
        }
        binding.titleBar.setPadding(0, GallaryCommon.getStatsBarHeight(), 0, 0)

        mediaAdapter.register(ImageMediaInfoWrapper::class, MediaImageViewBinder(selectedWrapperList, ::mediaItemClick))
        mediaAdapter.register(VideoMediaInfoWrapper::class, MediaVideoViewBinder(selectedWrapperList, ::mediaItemClick))
        mediaLayoutManager = GridLayoutManager(this@GalleryMediaListAct, 3)
        binding.rvMedia.apply {
            adapter = mediaAdapter
            layoutManager = mediaLayoutManager
            addOnScrollListener(object : EndlessRecyclerViewScrollListener(mediaLayoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    loadMoreMedia(page)
                }
            })
        }
        bucketAdapter.register(BucketInfo::class.java, BucketBinder { bucketInfo ->
            refreshBucketSelectorState(false)
            if (galleryMediaLoader.selectBucketId != bucketInfo.id) {
                binding.root.postDelayed({
                    requestMediaWithBucketId(bucketInfo)
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

        binding.rvBucket.maxRecyclerViewHeight = (GallaryCommon.getScreenHeight() * 0.5f).toInt()

        binding.tvSelectOkay.setOnClickListener {
            val result = ArrayList(selectedWrapperList.map {
                it.mediaInfo
            })
            if (GallaryCommon.lessThanAndroidQ()) {
                setMediaInfoResultAndFinish(result)
            } else {
                createSendBoxFile(result)
            }
        }
    }

    private fun setMediaInfoResultAndFinish(result: ArrayList<MediaInfo>) {
        val proxy = MediaInfoTargetBinding.responseActivityMap[config.targetName]
        if (proxy != null) {
            val bindClazz = Class.forName(proxy::class.java.name)
            val onMediaInfoReceivedMethod = bindClazz.getMethod("onMediaInfoReceived", List::class.java)
            onMediaInfoReceivedMethod.invoke(proxy, result)
            MediaInfoTargetBinding.unbind(config.targetName)
        } else {
            setResult(MediaInfoDispatcher.RESULT_CODE_MEDIA_INFO, Intent().apply {
                putParcelableArrayListExtra(MediaInfoDispatcher.EXTRA_RESULT_MEDIA_INFO, result)
            })
        }
        finish()
    }

    private fun createSendBoxFile(result: ArrayList<MediaInfo>) {
        binding.flProgress.visibility = View.VISIBLE
        launch(coroutineContext) {
            withContext(coroutineContext + Dispatchers.IO) {
                result.forEach {
                    val sendBoxFile = FileUtils.createSendBoxFileAndroidQ(it)
                    it.sendBoxPath = sendBoxFile.path
                }
            }
            binding.flProgress.visibility = View.GONE
            setMediaInfoResultAndFinish(result)
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
                    GallaryCommon.makeToast("请开启相关权限~")
                    finish()
                }
            }
    }

    private fun mediaItemClick(wrapper: MediaInfoWrapper, position: Int) {
        wrapper.selected = !wrapper.selected
        if (wrapper.selected) {
            selectedWrapperList.add(wrapper)
        } else {
            selectedWrapperList.remove(wrapper)
        }
        mediaAdapter.notifyItemChanged(position, BaseMediaViewBinder.PAYLOADS_UPDATE_SELECTED_INDEX)
        val firstVisibleItemPosition = mediaLayoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = mediaLayoutManager.findLastVisibleItemPosition()
        selectedWrapperList.forEach {
            val index = wrapperList.indexOf(it)
            if (index in firstVisibleItemPosition .. lastVisibleItemPosition) {
                mediaAdapter.notifyItemChanged(index, BaseMediaViewBinder.PAYLOADS_UPDATE_SELECTED_INDEX)
            }
        }
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

    private fun loadMedia() {
        galleryMediaLoader.loadMedia(this, config.mimeType, successCall = {
            refreshMedia(it)
        })
        galleryMediaLoader.loadBucket(this, config.mimeType, {
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

    private fun requestMediaWithBucketId(bucketInfo: BucketInfo) {
        binding.tvBucketSelector.text = bucketInfo.displayName
        galleryMediaLoader.selectBucketId = bucketInfo.id
        galleryMediaLoader.loadMedia(this, config.mimeType, successCall = {
            refreshMedia(it)
        })
    }

    private fun refreshMedia(result: List<MediaInfo>) {
        wrapperList.clear()
        addResult(result)
        mediaAdapter.notifyDataSetChanged()
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
    }

    private fun loadMoreMedia(page: Int) {
        galleryMediaLoader.loadMedia(this, config.mimeType, page, successCall = {
            val oldSize = wrapperList.size
            addResult(it)
            if (wrapperList.size != oldSize) {
                mediaAdapter.notifyItemRangeInserted(oldSize, wrapperList.size - oldSize)
            }
        })
    }
}