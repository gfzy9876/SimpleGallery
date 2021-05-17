package pers.zy.gallerylib.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import pers.zy.gallerylib.R

/**
 * date: 2020/6/11   time: 7:27 PM
 * author zy
 * Have a nice day :)
 **/
class MaxHeightRecyclerView(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs) {

    var maxRecyclerViewHeight: Int
    init {
        context.obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView).apply {
            maxRecyclerViewHeight = getDimensionPixelOffset(R.styleable.MaxHeightRecyclerView_maxRecyclerHeight, -1)
            recycle()
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        if (measuredHeight > maxRecyclerViewHeight) {
            setMeasuredDimension(measuredWidth, maxRecyclerViewHeight)
        }
    }
}