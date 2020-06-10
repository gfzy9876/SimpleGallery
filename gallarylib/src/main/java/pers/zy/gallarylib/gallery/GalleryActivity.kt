package pers.zy.gallarylib.gallery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import pers.zy.gallarylib.R
import pers.zy.gallarylib.databinding.ActivityGallaryBinding
import pers.zy.gallarylib.gallery.commons.getStatsBarHeight
import pers.zy.gallarylib.gallery.commons.ui.EndlessRecyclerViewScrollListener
import pers.zy.gallarylib.gallery.model.MediaImageInfo
import java.lang.RuntimeException

class GalleryActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, GalleryActivity::class.java))
        }
    }

    private lateinit var binding: ActivityGallaryBinding

    private val mediaList = mutableListOf<Any>()
    private val adapter = MultiTypeAdapter(mediaList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.trans_from_bottom_enter_anim, 0)
        binding = ActivityGallaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        GalleryLoader.getInstance(this).loadImage(0, imageListCall = {
            mediaList.addAll(it)
            adapter.notifyDataSetChanged()
        }, errorCall = {
            throw RuntimeException(it)
        })
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.trans_from_bottom_exit_anim)
    }

    private fun initView() {
        binding.titleBar.layoutParams = (binding.titleBar.layoutParams as LinearLayout.LayoutParams).apply {
            height += getStatsBarHeight()
        }
        binding.titleBar.setPadding(0, getStatsBarHeight(), 0, 0)
        binding.titleBar.setIconClickListener(View.OnClickListener { finish() })

        this@GalleryActivity.adapter.register(MediaImageInfo::class, MediaImageViewBinder())
        binding.rvGallery.apply {
            adapter = this@GalleryActivity.adapter
            val gridLayoutManager = GridLayoutManager(this@GalleryActivity, 3)
            layoutManager = gridLayoutManager
            addOnScrollListener(object : EndlessRecyclerViewScrollListener(gridLayoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    GalleryLoader.getInstance(this@GalleryActivity).loadImage(page, imageListCall = {
                        val orgSize = mediaList.size
                        mediaList.addAll(it)
                        this@GalleryActivity.adapter.notifyItemRangeInserted(orgSize, mediaList.size - orgSize)
                    }, errorCall = {
                        throw RuntimeException(it)
                    })
                }
            })
        }
    }
}