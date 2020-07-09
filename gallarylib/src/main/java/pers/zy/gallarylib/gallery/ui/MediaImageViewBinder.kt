package pers.zy.gallarylib.gallery.ui

import android.view.LayoutInflater
import pers.zy.gallarylib.databinding.ItemMediaRootBinding
import pers.zy.gallarylib.databinding.ItemMediaImageBinding
import pers.zy.gallarylib.gallery.model.LocalMediaImageInfo
import pers.zy.gallarylib.gallery.model.LocalMediaInfo

/**
 * date: 2020/6/7   time: 3:36 PM
 * author zy
 * Have a nice day :)
 **/
internal class MediaImageViewBinder(
    selectedMediaList: MutableList<LocalMediaInfo>,
    mediaItemClickListener: MediaItemClickListener
) : BaseMediaViewBinder<LocalMediaImageInfo, MediaImageViewBinder.ImageMediaViewHolder>(selectedMediaList, mediaItemClickListener) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        rootBinding: ItemMediaRootBinding,
        mediaItemClickListener: MediaItemClickListener
    ): ImageMediaViewHolder {
        val binding = ItemMediaImageBinding.inflate(inflater, rootBinding.root, true)
        return ImageMediaViewHolder(binding, rootBinding, mediaItemClickListener)
    }

    internal class ImageMediaViewHolder(
        val binding: ItemMediaImageBinding,
        rootBinding: ItemMediaRootBinding,
        mediaItemClickListener: MediaItemClickListener
    ) : BaseMediaViewHolder<LocalMediaImageInfo>(rootBinding, mediaItemClickListener) {

        init {
//            rootBinding.root.setOnClickListener {
//                e("info: ${mediaModel.localMediaInfo}")
//                MediaPreviewActivity.start(itemView.context, mediaModel.localMediaInfo)
//            }
        }
    }
}