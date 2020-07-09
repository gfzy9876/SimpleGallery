package pers.zy.gallarylib.gallery.ui

import android.view.LayoutInflater
import pers.zy.gallarylib.databinding.ItemMediaRootBinding
import pers.zy.gallarylib.databinding.ItemMediaVideoBinding
import pers.zy.gallarylib.gallery.model.LocalMediaInfo
import pers.zy.gallarylib.gallery.model.LocalMediaVideoInfo

/**
 * date: 2020/6/28   time: 5:57 PM
 * author zy
 * Have a nice day :)
 **/
internal class MediaVideoViewBinder(
    selectedMediaList: MutableList<LocalMediaInfo>,
    mediaItemClickListener: MediaItemClickListener
) : BaseMediaViewBinder<LocalMediaVideoInfo, MediaVideoViewBinder.VideoMediaViewHolder>(selectedMediaList, mediaItemClickListener) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        rootBinding: ItemMediaRootBinding,
        mediaItemClickListener: MediaItemClickListener
    ): VideoMediaViewHolder {
        val binding = ItemMediaVideoBinding.inflate(inflater, rootBinding.root, true)
        return VideoMediaViewHolder(binding, rootBinding, mediaItemClickListener)
    }

    override fun onBindViewHolder(holder: VideoMediaViewHolder, item: LocalMediaVideoInfo) {
        super.onBindViewHolder(holder, item)
        val timeInSeconds = item.duration / 1000
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        holder.binding.tvMediaDuration.text = if (minutes < 10) {
            "0$minutes"
        } else {
            "$minutes"
        } + ":" + if (seconds < 10) {
            "0$seconds"
        } else {
            "$seconds"
        }
    }

    internal class VideoMediaViewHolder(
        val binding: ItemMediaVideoBinding,
        rootBinding: ItemMediaRootBinding,
        mediaItemClickListener: MediaItemClickListener
    ) : BaseMediaViewBinder.BaseMediaViewHolder<LocalMediaVideoInfo>(rootBinding, mediaItemClickListener) {

        init {

        }
    }
}