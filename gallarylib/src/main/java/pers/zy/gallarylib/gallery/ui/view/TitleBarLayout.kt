package pers.zy.gallarylib.gallery.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import pers.zy.gallarylib.R
import pers.zy.gallarylib.databinding.LayoutTitleBarBinding

/**
 * date: 2020/6/9   time: 5:10 PM
 * author zy
 * Have a nice day :)
 **/
class TitleBarLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val binding: LayoutTitleBarBinding = LayoutTitleBarBinding.inflate(LayoutInflater.from(context), this)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TitleBarLayout).apply {
            binding.tvTitle.text = getString(R.styleable.TitleBarLayout_titleText)
            val leftIconResId = getResourceId(R.styleable.TitleBarLayout_leftIconRes, -1)
            val rightIconResId = getResourceId(R.styleable.TitleBarLayout_rightIconRes, -1)

            if (leftIconResId == -1) {
                binding.ivLeftIcon.visibility = View.GONE
            } else {
                binding.ivLeftIcon.visibility = View.VISIBLE
                binding.ivLeftIcon.setImageResource(leftIconResId)
            }
            if (rightIconResId == -1) {
                binding.ivRightIcon.visibility = View.GONE
            } else {
                binding.ivRightIcon.visibility = View.VISIBLE
                binding.ivRightIcon.setImageResource(rightIconResId)
            }
            val titleTextColor = getColor(R.styleable.TitleBarLayout_titleTextColor, Color.WHITE)
            binding.tvTitle.setTextColor(titleTextColor)

            val iconTint = getColor(R.styleable.TitleBarLayout_iconTint, Color.BLACK)

            binding.ivLeftIcon.setColorFilter(iconTint)
            binding.ivRightIcon.setColorFilter(iconTint)

            val titleTextSize = getDimension(R.styleable.TitleBarLayout_titleTextSize, -1f)
            if (titleTextSize != -1f) {
                binding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
            }
            recycle()
        }
    }
    
    fun setTitle(title: CharSequence) {
        binding.tvTitle.text = title
    }
    
    fun setIconClickListener(leftCall: OnClickListener? = null, rightCall: OnClickListener? = null) {
        binding.ivLeftIcon.setOnClickListener(leftCall)
        binding.ivRightIcon.setOnClickListener(rightCall)
    }

}