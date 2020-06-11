package pers.zy.gallarylib.gallery.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import pers.zy.gallarylib.R
import pers.zy.gallarylib.databinding.ActivityGallaryBinding
import pers.zy.gallarylib.gallery.commons.getScreenHeight
import pers.zy.gallarylib.gallery.commons.getStatsBarHeight
import pers.zy.gallarylib.gallery.loader.GalleryImageLoader
import pers.zy.gallarylib.gallery.model.BaseMediaInfo
import pers.zy.gallarylib.gallery.model.BucketInfo
import pers.zy.gallarylib.gallery.model.MediaImageInfo
import java.lang.RuntimeException

class GalleryMediaActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MIME_TYPE = "extra_mime_type"
        private const val MIME_TYPE_IMAGE = 1
        private const val MIME_TYPE_VIDEO = 2
        private const val MIME_TYPE_ALL = 3

        fun startShowImage(context: Context) {
            context.startActivity(Intent(context, GalleryMediaActivity::class.java).apply {
                putExtra(
                    EXTRA_MIME_TYPE,
                    MIME_TYPE_IMAGE
                )
            })
        }

        fun startShowVideo(context: Context) {
            context.startActivity(Intent(context, GalleryMediaActivity::class.java).apply {
                putExtra(
                    EXTRA_MIME_TYPE,
                    MIME_TYPE_VIDEO
                )
            })
        }
    }

    private lateinit var imageLoader: GalleryImageLoader
    private lateinit var binding: ActivityGallaryBinding

    private val mediaList = mutableListOf<BaseMediaInfo>()
    private val mediaAdapter = MultiTypeAdapter(mediaList)
    private val bucketList = mutableListOf<BucketInfo>()
    private val bucketAdapter = MultiTypeAdapter(bucketList)

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
        ObjectAnimator.ofFloat(binding.flBucket, View.TRANSLATION_Y, 0f, -binding.flBucket.height.toFloat()).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.trans_from_bottom_enter_anim, 0)
        binding = ActivityGallaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        imageLoader = GalleryImageLoader(this, this)
        when (intent.getIntExtra(EXTRA_MIME_TYPE, -1)) {
            MIME_TYPE_IMAGE -> {
                imageLoader.loadMedia(mediaListCall = {
                    refreshMedia(it)
                }, errorCall = {
                    throw RuntimeException(it)
                })
                imageLoader.loadBucket({
                    bucketList.addAll(it)
                    bucketAdapter.notifyDataSetChanged()
                }, {
                    throw RuntimeException(it)
                })
            }
            MIME_TYPE_VIDEO -> {

            }
            MIME_TYPE_ALL -> {

            }
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
    }

    private fun initView() {
        binding.titleBar.layoutParams = (binding.titleBar.layoutParams as LinearLayout.LayoutParams).apply {
            height += getStatsBarHeight()
        }
        binding.titleBar.setPadding(0, getStatsBarHeight(), 0, 0)
        binding.titleBar.setIconClickListener(View.OnClickListener { finish() })

        mediaAdapter.register(MediaImageInfo::class, MediaImageBinder())
        binding.rvMedia.apply {
            adapter = this@GalleryMediaActivity.mediaAdapter
            val gridLayoutManager = GridLayoutManager(this@GalleryMediaActivity, 4)
            layoutManager = gridLayoutManager
            addOnScrollListener(object : EndlessRecyclerViewScrollListener(gridLayoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    loadMoreMedia(page)
                }
            })
        }
        bucketAdapter.register(BucketInfo::class.java, BucketBinder { bucketInfo ->
            refreshBucketSelectorState(false)
            if (imageLoader.selectBucketId != bucketInfo.id) {
                binding.root.postDelayed({
                    requestMediaWithBucketId(bucketInfo)
                }, 300)
                bucketExitAnim.start()
            }
        })
        binding.rvBucket.apply {
            adapter = this@GalleryMediaActivity.bucketAdapter
            layoutManager = LinearLayoutManager(this@GalleryMediaActivity, LinearLayoutManager.VERTICAL, false)
        }

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

        binding.rvBucket.maxRecyclerViewHeight = (getScreenHeight() * 0.5f).toInt()
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
        imageLoader.selectBucketId = bucketInfo.id
        imageLoader.loadMedia(mediaListCall = {
            refreshMedia(it)
        }, errorCall = {
            throw RuntimeException(it)
        })
    }

    private fun refreshMedia(result: List<BaseMediaInfo>) {
        mediaList.clear()
        mediaList.addAll(result)
        mediaAdapter.notifyDataSetChanged()
    }

    private fun loadMoreMedia(page: Int) {
        imageLoader.loadMedia(page, mediaListCall = {
            val orgSize = mediaList.size
            mediaList.addAll(it)
            mediaAdapter.notifyItemRangeInserted(orgSize, mediaList.size - orgSize)
        }, errorCall = {
            throw RuntimeException(it)
        })
    }
}