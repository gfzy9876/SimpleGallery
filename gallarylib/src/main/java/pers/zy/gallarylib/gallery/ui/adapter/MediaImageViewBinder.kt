package pers.zy.gallarylib.gallery.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import pers.zy.gallarylib.databinding.ItemMediaRootBinding
import pers.zy.gallarylib.databinding.ItemMediaImageBinding
import pers.zy.gallarylib.gallery.model.ImageMediaInfoWrapper
import pers.zy.gallarylib.gallery.model.MediaInfoWrapper

/**
 * date: 2020/6/7   time: 3:36 PM
 * author zy
 * Have a nice day :)
 **/
internal class MediaImageViewBinder(
    selectedMediaList: MutableList<MediaInfoWrapper>,
    itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
) : BaseMediaViewBinder<ImageMediaInfoWrapper, MediaImageViewBinder.ImageMediaViewHolder>(selectedMediaList, itemClick) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        rootBinding: ItemMediaRootBinding,
        itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
    ): ImageMediaViewHolder {
        val binding = ItemMediaImageBinding.inflate(inflater, rootBinding.root as ViewGroup, true)
        return ImageMediaViewHolder(binding, rootBinding, itemClick)
    }

    internal class ImageMediaViewHolder(
        val binding: ItemMediaImageBinding,
        rootBinding: ItemMediaRootBinding,
        itemClick: (wrapper: MediaInfoWrapper, position: Int) -> Unit
    ) : BaseMediaViewHolder<ImageMediaInfoWrapper>(rootBinding, itemClick) {

    }
}