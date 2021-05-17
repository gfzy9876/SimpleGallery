package pers.zy.gallerylib.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import pers.zy.gallerylib.databinding.ItemCameraHolderBinding

/**
 * date: 4/21/21   time: 10:55 AM
 * author zy
 * Have a nice day :)
 **/
class CameraViewBinder(private val showCameraCall: () -> Unit) : ItemViewBinder<CameraItem, CameraViewBinder.Holder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(ItemCameraHolderBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, item: CameraItem) {
        holder.binding.root.setOnClickListener {
            showCameraCall()
        }
    }

    class Holder(val binding: ItemCameraHolderBinding) : RecyclerView.ViewHolder(binding.root)
}

class CameraItem