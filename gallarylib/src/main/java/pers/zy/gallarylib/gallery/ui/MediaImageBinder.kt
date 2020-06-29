package pers.zy.gallarylib.gallery.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drakeet.multitype.ItemViewBinder
import pers.zy.gallarylib.R
import pers.zy.gallarylib.databinding.ItemMediaImageBinding
import pers.zy.gallarylib.gallery.commons.dp
import pers.zy.gallarylib.gallery.commons.dpF
import pers.zy.gallarylib.gallery.commons.e
import pers.zy.gallarylib.gallery.model.MediaImageInfo
import java.io.File
import kotlin.math.floor

/**
 * date: 2020/6/7   time: 3:36 PM
 * author zy
 * Have a nice day :)
 **/
class MediaImageBinder : ItemViewBinder<MediaImageInfo, MediaImageBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(
            ItemMediaImageBinding.inflate(inflater)
        )
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: MediaImageInfo) {
        holder.info = item
        Glide.with(holder.itemView)
            .load(item.contentUriPath)
            .override(100f.dp)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.place_holder)
            .into(holder.binding.ivMediaImage)
        if (!File(item.realPath).exists()) {
            holder.binding.root.setBackgroundColor(Color.BLACK)
        }
        holder.binding.tvMediaImage.text = item.displayName
        val floor = floor((item.size / 1000).toDouble())
        val format = String.format("%.2f", floor / 1000.toDouble())
        holder.binding.tvMediaSize.text = "$format MB"
        holder.binding.tvIndex.text = "${holder.absoluteAdapterPosition + 1}"
    }

    class ViewHolder(val binding: ItemMediaImageBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var info: MediaImageInfo
        init {
            binding.root.setOnClickListener {
                e("info: $info")
                MediaPreviewActivity.start(itemView.context, info)
            }
        }
    }
}