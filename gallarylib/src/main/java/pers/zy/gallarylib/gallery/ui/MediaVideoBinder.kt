package pers.zy.gallarylib.gallery.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drakeet.multitype.ItemViewBinder
import pers.zy.gallarylib.databinding.ItemMediaVideoBinding
import pers.zy.gallarylib.gallery.model.MediaVideoInfo

/**
 * date: 2020/6/28   time: 5:57 PM
 * author zy
 * Have a nice day :)
 **/
class MediaVideoBinder : ItemViewBinder<MediaVideoInfo, MediaVideoBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemMediaVideoBinding.inflate(inflater))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: MediaVideoInfo) {
        Glide.with(holder.itemView.context)
            .load(item.contentUriPath)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(holder.binding.ivMediaVideo)
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

    class ViewHolder(val binding: ItemMediaVideoBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}