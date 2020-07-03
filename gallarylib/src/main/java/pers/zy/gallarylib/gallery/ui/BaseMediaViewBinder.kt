package pers.zy.gallarylib.gallery.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drakeet.multitype.ItemViewBinder
import pers.zy.gallarylib.R
import pers.zy.gallarylib.databinding.ItemMediaRootBinding
import pers.zy.gallarylib.gallery.model.LocalMediaInfo

/**
 * date: 2020/6/30   time: 12:30 PM
 * author zy
 * Have a nice day :)
 **/
internal abstract class BaseMediaViewBinder<T : LocalMediaInfo, VH : BaseMediaViewBinder.BaseMediaViewHolder<T>>(
    private val selectedMediaList: MutableList<LocalMediaInfo>,
    private val mediaItemClickListener: MediaItemClickListener
) : ItemViewBinder<T, VH>() {

    companion object {
        const val PAYLOADS_UPDATE_SELECTED_INDEX = 1
    }

    abstract fun createViewHolder(
        inflater: LayoutInflater,
        rootBinding: ItemMediaRootBinding,
        mediaItemClickListener: MediaItemClickListener
    ): VH

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VH {
        return createViewHolder(inflater, ItemMediaRootBinding.inflate(inflater), mediaItemClickListener)
    }

    override fun onBindViewHolder(holder: VH, item: T, payloads: List<Any>) {
        if (payloads.isNotEmpty() && payloads[0] as? Int == PAYLOADS_UPDATE_SELECTED_INDEX) {
            updateMediaCheckBox(holder, item)
        } else {
            super.onBindViewHolder(holder, item, payloads)
        }
    }

    override fun onBindViewHolder(holder: VH, item: T) {
        holder.mediaInfo = item
        Glide.with(holder.itemView.context)
            .load(item.contentUriPath)
            .placeholder(R.drawable.place_holder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.rootBinding.ivMedia)
        updateMediaCheckBox(holder, item)
    }

    private fun updateMediaCheckBox(holder: VH, item: T) {
        if (selectedMediaList.contains(item)) {
            holder.rootBinding.mediaCheckBox.setSelectNumber(selectedMediaList.indexOf(item) + 1)
            holder.rootBinding.mediaCheckBox.mediaSelected = true
        } else {
            holder.rootBinding.mediaCheckBox.binding.flSelectMedia.alpha = 0f
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context).clear(holder.rootBinding.ivMedia)
    }

    internal open class BaseMediaViewHolder<T : LocalMediaInfo>(
        val rootBinding: ItemMediaRootBinding,
        private val mediaItemClickListener: MediaItemClickListener
    ) : RecyclerView.ViewHolder(rootBinding.root) {
        lateinit var mediaInfo: T

        init {
            rootBinding.root.setOnClickListener {
                mediaItemClickListener.mediaItemClick(mediaInfo)
            }
        }
    }


    interface MediaItemClickListener {
        fun mediaItemClick(localMediaInfo: LocalMediaInfo)
    }
}