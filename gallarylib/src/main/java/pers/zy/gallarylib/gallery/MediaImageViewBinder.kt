package pers.zy.gallarylib.gallery

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drakeet.multitype.ItemViewBinder
import pers.zy.gallarylib.databinding.ItemMediaImageBinding
import pers.zy.gallerymodel.gallery.model.MediaImageInfo
import java.io.File
import kotlin.math.floor

/**
 * date: 2020/6/7   time: 3:36 PM
 * author zy
 * Have a nice day :)
 **/
class MediaImageViewBinder : ItemViewBinder<MediaImageInfo, MediaImageViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemMediaImageBinding.inflate(inflater))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView).clear(holder.binding.ivMediaImage)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: MediaImageInfo) {
        holder.info = item
        Glide.with(holder.itemView)
            .load(item.path)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(holder.binding.ivMediaImage)
        if (!File(item.path).exists()) {
            holder.binding.root.setBackgroundColor(Color.BLACK)
        }
        holder.binding.tvMediaImage.text = item.displayName
        item.size?.let { size ->
            val floor = floor((size / 1000).toDouble())
            val format = String.format("%.2f", floor / 1000.toDouble())
            holder.binding.tvMediaSize.text = "${format}MB"
        }
    }

    class ViewHolder(val binding: ItemMediaImageBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var info: MediaImageInfo
        init {
            binding.root.setOnClickListener {
                MediaDetailsActivity.start(itemView.context, info)
            }
        }
    }
}