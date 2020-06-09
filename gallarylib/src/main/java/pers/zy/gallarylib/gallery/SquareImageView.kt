package pers.zy.gallarylib.gallery

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * date: 2020/6/7   time: 5:18 PM
 * author zy
 * Have a nice day :)
 **/
class SquareImageView(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}