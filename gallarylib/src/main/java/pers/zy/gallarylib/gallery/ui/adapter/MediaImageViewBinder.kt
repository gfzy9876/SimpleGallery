package pers.zy.gallarylib.gallery.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import pers.zy.gallarylib.databinding.ItemMediaRootBinding
import pers.zy.gallarylib.databinding.ItemMediaImageBinding
import pers.zy.gallarylib.gallery.model.ImageMediaInfoWrapper
import pers.zy.gallarylib.gallery.model.MediaInfoWrapper
import pers.zy.gallarylib.gallery.ui.list.GalleryMediaClickListener

/**
 * date: 2020/6/7   time: 3:36 PM
 * author zy
 * Have a nice day :)
 **/
internal class MediaImageViewBinder(
    selectedMediaList: MutableList<MediaInfoWrapper>,
    listener: GalleryMediaClickListener
) : BaseMediaViewBinder<ImageMediaInfoWrapper, MediaImageViewBinder.ImageMediaViewHolder>(selectedMediaList, listener) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        rootBinding: ItemMediaRootBinding
    ): ImageMediaViewHolder {
        val binding = ItemMediaImageBinding.inflate(inflater, rootBinding.root as ViewGroup, true)
        return ImageMediaViewHolder(binding, rootBinding)
    }

    internal class ImageMediaViewHolder(
        val binding: ItemMediaImageBinding,
        rootBinding: ItemMediaRootBinding
    ) : BaseMediaViewHolder<ImageMediaInfoWrapper>(rootBinding) {

    }
}