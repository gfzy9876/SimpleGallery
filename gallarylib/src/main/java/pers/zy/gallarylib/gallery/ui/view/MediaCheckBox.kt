package pers.zy.gallarylib.gallery.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import pers.zy.gallarylib.databinding.ViewMediaCheckBoxBinding

/**
 * date: 2020/6/30   time: 6:01 PM
 * author zy
 * Have a nice day :)
 **/
class MediaCheckBox(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    var binding: ViewMediaCheckBoxBinding = ViewMediaCheckBoxBinding.inflate(LayoutInflater.from(context), this, true)
    var mediaSelected: Boolean = false
        set(value) {
            field = value
            if (value) {
                binding.flSelectMedia.animate().alpha(1f).setDuration(200).start()
            } else {
                binding.flSelectMedia.animate().alpha(0f).setDuration(200).start()
                binding.tvSelectMediaCount.text = ""
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    fun setSelectNumber(number: Int) {
        binding.tvSelectMediaCount.text = number.toString()
    }
}