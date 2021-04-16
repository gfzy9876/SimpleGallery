package pers.zy.gallarylib.gallery.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import pers.zy.gallarylib.databinding.ItemMediaRootBinding
import pers.zy.gallarylib.databinding.ItemMediaVideoBinding
import pers.zy.gallarylib.gallery.model.MediaInfoWrapper
import pers.zy.gallarylib.gallery.model.VideoMediaInfoWrapper

/**
 * date: 2020/6/28   time: 5:57 PM
 * author zy
 * Have a nice day :)
 **/
internal class MediaVideoViewBinder(
    selectedMediaList: MutableList<MediaInfoWrapper>,
    itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
) : BaseMediaViewBinder<VideoMediaInfoWrapper, MediaVideoViewBinder.VideoMediaViewHolder>(selectedMediaList, itemClick) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        rootBinding: ItemMediaRootBinding,
        itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
    ): VideoMediaViewHolder {
        val binding = ItemMediaVideoBinding.inflate(inflater, rootBinding.root as ViewGroup, true)
        return VideoMediaViewHolder(binding, rootBinding, itemClick)
    }

    override fun onBindViewHolder(holder: VideoMediaViewHolder, item: VideoMediaInfoWrapper) {
        super.onBindViewHolder(holder, item)
        val timeInSeconds = item.mediaInfo.duration / 1000
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
        itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
    ) : BaseMediaViewHolder<VideoMediaInfoWrapper>(rootBinding, itemClick) {

    }
}