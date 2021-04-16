package pers.zy.gallarylib.gallery.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.drakeet.multitype.ItemViewBinder
import pers.zy.gallarylib.databinding.ItemBucketBinding
import pers.zy.gallarylib.gallery.model.BucketInfo

/**
 * date: 2020/6/11   time: 7:18 PM
 * author zy
 * Have a nice day :)
 **/
class BucketBinder(val bucketSelect: (BucketInfo) -> Unit) : ItemViewBinder<BucketInfo, BucketBinder.Holder>() {

    override fun onBindViewHolder(holder: Holder, item: BucketInfo) {
        holder.bucketInfo = item
        Glide.with(holder.itemView.context)
            .load(item.previewContentUri)
            .centerCrop()
            .override(200, 200)
            .into(holder.binder.ivBucket)
        holder.binder.tvBucket.text = item.displayName
        holder.binder.tvBucketCount.text = "(${item.count})"
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(ItemBucketBinding.inflate(inflater, parent, false))
    }

    inner class Holder(val binder: ItemBucketBinding) : RecyclerView.ViewHolder(binder.root) {
        lateinit var bucketInfo: BucketInfo

        init {
            binder.root.setOnClickListener { bucketSelect.invoke(bucketInfo) }
        }
    }
}