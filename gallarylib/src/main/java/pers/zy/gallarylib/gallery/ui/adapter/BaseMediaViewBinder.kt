package pers.zy.gallarylib.gallery.ui.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.drakeet.multitype.ItemViewBinder
import pers.zy.gallarylib.R
import pers.zy.gallarylib.databinding.ItemMediaRootBinding
import pers.zy.gallarylib.gallery.model.MediaInfoWrapper

/**
 * date: 2020/6/30   time: 12:30 PM
 * author zy
 * Have a nice day :)
 **/
internal abstract class BaseMediaViewBinder<T : MediaInfoWrapper, VH : BaseMediaViewBinder.BaseMediaViewHolder<T>>(
    private val selectedWrapperList: MutableList<MediaInfoWrapper>,
    private val itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
) : ItemViewBinder<T, VH>() {

    companion object {
        const val PAYLOADS_UPDATE_SELECTED_INDEX = 1
        const val PAYLOADS_UPDATE_SELECTED_INDEX_WITH_ANIM = 2
    }

    abstract fun createViewHolder(
        inflater: LayoutInflater,
        rootBinding: ItemMediaRootBinding,
        itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
    ): VH

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VH {
        return createViewHolder(inflater, ItemMediaRootBinding.inflate(inflater), itemClick)
    }

    override fun onBindViewHolder(holder: VH, item: T, payloads: List<Any>) {
        if (payloads.isNotEmpty()) {
            when (payloads[0] as Int) {
                PAYLOADS_UPDATE_SELECTED_INDEX -> {
                    updateMediaCheckBox(holder, item)
                }
                PAYLOADS_UPDATE_SELECTED_INDEX_WITH_ANIM -> {
                    updateMediaCheckBox(holder, item)
                    if (item.selected) {
                        holder.selectAnim.start()
                    } else {
                        holder.selectAnim.reverse()
                    }
                }
            }
        } else {
            super.onBindViewHolder(holder, item, payloads)
        }
    }

    override fun onBindViewHolder(holder: VH, item: T) {
        holder.wrapper = item
        Glide.with(holder.itemView.context)
            .load(item.mediaInfo.contentUriPath)
            .transition(DrawableTransitionOptions.withCrossFade(100))
            .placeholder(R.drawable.place_holder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.rootBinding.ivMedia)
        if (item.selected) {
            holder.rootBinding.selectMask.alpha = 0.6f
        } else {
            holder.rootBinding.selectMask.alpha = 0f
        }
        updateMediaCheckBox(holder, item)
    }

    private fun updateMediaCheckBox(holder: VH, item: T) {
        if (item.selected) {
            holder.rootBinding.mediaCheckBox.binding.tvSelectMediaCount.text = "${selectedWrapperList.indexOf(item) + 1}"
            holder.rootBinding.mediaCheckBox.mediaSelected = true
        } else {
            holder.rootBinding.mediaCheckBox.binding.flSelectMedia.alpha = 0f
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context).clear(holder.rootBinding.ivMedia)
    }

    internal open class BaseMediaViewHolder<T : MediaInfoWrapper>(
        val rootBinding: ItemMediaRootBinding,
        private val itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
    ) : RecyclerView.ViewHolder(rootBinding.root) {
        lateinit var wrapper: T

        val selectAnim: ObjectAnimator by lazy {
            //TODO:ZY 动画可配置
//            ObjectAnimator.ofPropertyValuesHolder(rootBinding.ivMedia,
//                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f),
//                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f)).apply {
//                    duration = 150
//            }
            ObjectAnimator.ofFloat(rootBinding.selectMask, View.ALPHA, 0f, 0.6f).apply {
                duration = 150
            }
        }

        init {
            rootBinding.root.setOnClickListener {
//                if (wrapper.selected) {
//                    selectAnim.reverse()
//                } else {
//                    selectAnim.start()
//                }
                itemClick.invoke(wrapper, absoluteAdapterPosition)
            }
        }
    }
}